package com.cleanSweep.backend.application;

import com.cleanSweep.backend.domain.Cell;
import com.cleanSweep.backend.domain.FloorMap;
import com.cleanSweep.backend.common.Direction;
import com.cleanSweep.backend.infrastructure.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This service implements a modified DFS (Depth-First Search) algorithm for robot navigation
 * combined with Dijkstra's algorithm for finding optimal paths to charging stations.
 * It handles the robot's movement, cleaning operations, and charging station navigation
 * while maintaining efficient path planning and battery management.
 */

/**
 * Helper class to store coordinates and minimum distance
 */
class Node {
    double dist;
    int x;
    int y;

    public Node(double dist, int x, int y) {
        this.dist = dist;
        this.x = x;
        this.y = y;
    }
}

/**
 * Helper class to store coordinates for path reconstruction.
 */
class Point {
    int x, y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

@Service
public class NavigationService {

    @Autowired
    private FloorMap floorMap;

    @Autowired
    private DirtService dirtService;

    @Autowired
    private BatteryService batteryService;

    @Autowired
    private SensorSimulatorService sensorSimulatorService;

    @Autowired
    private ActivityLogger activityLogger;

    private Deque<Cell[]> stack = new ArrayDeque<>();
    private List<int[]> stationPath = new ArrayList<>();
    private int stationIdx;

    private boolean isNavigationCompleted = false;
    private int currentX = 0;
    private int currentY = 0;

    private int lastCleaningX;
    private int lastCleaningY;
    private boolean isReturningFromStation = false;

    /**
     * Initializes the navigation process from a starting position.
     * Sets up initial paths to all charging stations and begins the cleaning
     * operation.
     */
    public void startNavigation(int startX, int startY) {
        if (!isNavigationCompleted && stack.isEmpty()) {
            startX = 0;
            startY = 0;

            // Initialize paths from all charging stations before starting navigation
            initializeAllPaths();

            Cell startCell = floorMap.getCells()[startX][startY];
            stack.push(new Cell[] { null, startCell });
            startCell.setVisited(true);

            currentX = startX;
            currentY = startY;
            activityLogger.logMovement(startX, startY, "Start");
        }
    }

    /**
     * Initializes the shortest paths from a charging station to all cells using
     * BFS.
     * This method is used to pre-compute paths for efficient navigation.
     */
    public void initializeWayToChargingStation(int startX, int startY) {
        Cell startCell = floorMap.getCells()[startX][startY];
        List<int[]> startPath = new ArrayList<>();
        startPath.add(new int[] { startX, startY });
        startCell.setWayToChargingStation(startPath);
        boolean[][] visit = new boolean[floorMap.getCells().length][floorMap.getCells()[0].length];
        visit[startX][startY] = true;
        Deque<Cell> queue = new ArrayDeque<>();
        queue.add(startCell);
        while (!queue.isEmpty()) {
            Cell cell = queue.poll();
            int x = cell.getX();
            int y = cell.getY();
            List<int[]> path = cell.getWayToChargingStation();
            for (Direction direction : Direction.values()) {
                int newX = x + direction.getXOffset();
                int newY = y + direction.getYOffset();
                if (isValidMove(newX, newY) && !visit[newX][newY] && !sensorSimulatorService.isObstacle(newX, newY)) {
                    Cell neighborCell = floorMap.getCells()[newX][newY];
                    visit[newX][newY] = true;
                    List<int[]> newPath = new ArrayList<>(path);
                    newPath.add(new int[] { newX, newY });
                    neighborCell.setWayToChargingStation(newPath);
                    queue.add(neighborCell);
                }
            }
        }
    }

    /**
     * Handles the robot's movement to and from charging stations.
     * Manages charging, dirt removal, and return path navigation.
     */
    public void stationNavigation() {
        if (stationPath == null || stationPath.isEmpty() || stationIdx >= stationPath.size()) {
            stationPath = null;
            stationIdx = 0;
            return;
        }
        int[] currCell = stationPath.get(stationIdx);
        currentX = currCell[0];
        currentY = currCell[1];
        
        // Reduce the battery if the current cell is not the lastest cell because the lastest cell is overlapped
        if (currentX != lastCleaningX || currentY != lastCleaningY) {
            Cell currentCell = floorMap.getCells()[currentX][currentY];
            Cell previousCell = null;
            if (stationIdx > 0) {
                int[] preCell = stationPath.get(stationIdx - 1);
                previousCell = floorMap.getCells()[preCell[0]][preCell[1]];
            }
            double movingPowerCost = sensorSimulatorService.getMovingPowerCost(previousCell, currentCell);
            batteryService.consumePower(movingPowerCost);
        }

        String direction = isReturningFromStation ? "Returning to cleaning position" : "Moving to charging station";
        activityLogger.logMovement(currentX, currentY, direction);

        // Arriving at the charging station and not on the way back
        if (isAtAnyChargingStation() && !isReturningFromStation) {
            dirtService.removeDirt();
            batteryService.recharge();

            if (stack.isEmpty()) {
                isNavigationCompleted = true;
                stationPath = null;
                stationIdx = 0;
            } else {
                // Prepare to return to the final cleaning location
                dirtService.setCleaningMode();
                Cell lastCleaningCell = floorMap.getCells()[lastCleaningX][lastCleaningY];
                stationPath = new ArrayList<>(lastCleaningCell.getWayToChargingStation());
                stationIdx = 0;
                isReturningFromStation = true;
            }
        } else if (isReturningFromStation && currentX == lastCleaningX && currentY == lastCleaningY) {
            // Returned to the last cleaning position
            stationPath = null;
            stationIdx = 0;
            isReturningFromStation = false;
        } else if (stationIdx < stationPath.size() - 1) {
            stationIdx++;
        }
    }

    /**
     * Main navigation step function that determines whether to continue cleaning
     * or handle charging station navigation.
     */
    public void stepNavigation() {
        if (stationPath != null && !stationPath.isEmpty()) {
            stationNavigation();
        } else if (dirtService.isCleaningActive()) {
            cleaningNavigation();
        }
    }

    /**
     * Implements the cleaning navigation logic using DFS.
     * Handles dirt cleaning, battery monitoring, and path planning to charging
     * stations.
     */
    private void cleaningNavigation() {
        if (stack.isEmpty() || isNavigationCompleted) {
            return;
        }

        Cell[] cellPair = stack.peek();
        Cell previousCell = cellPair[0]; // This could be null
        Cell currentCell = cellPair[1];
        currentX = currentCell.getX();
        currentY = currentCell.getY();

        if (sensorSimulatorService.isObstacle(currentX, currentY)) {
            activityLogger.logObstacle(currentX, currentY);
            stack.pop();
            return;
        }

        double movingPowerCost = sensorSimulatorService.getMovingPowerCost(previousCell, currentCell);
        double cleaningPowerCost = sensorSimulatorService.getCleaningPowerCost(currentCell);
        batteryService.consumePower(movingPowerCost + cleaningPowerCost);

        if (currentCell.getDirtLevel() > 0) {
            dirtService.cleanDirt(currentX, currentY);
        }

        boolean isCleanAll = sensorSimulatorService.isCleanAll(dirtService.getCleanedDirtCellCount());

        // Check if returning to the charging station is needed
        if (isCleanAll || dirtService.isFullDirt() || batteryService.isRechargeNeeded(currentCell.getDistanceToStation())) {
            // Store the last cleaning position
            lastCleaningX = currentX;
            lastCleaningY = currentY;
            // If all dirts cells are cleaned, robot stops cleaning and comes back the best station
            if (isCleanAll){
                stack.clear();
            }
            dirtService.stopCleaningMode();
            if (currentCell.getWayToChargingStation() != null) {
                stationPath = new ArrayList<>(currentCell.getWayToChargingStation());
                Collections.reverse(stationPath); // Reverse the path to reach the charging station
                stationIdx = 0;
                isReturningFromStation = false;
                return;
            }
        }

        activityLogger.logMovement(currentX, currentY, "Visiting");

        Cell nextCell = getNeighborCell();
        if (nextCell != null) {
            stack.push(new Cell[] { currentCell, nextCell });
            nextCell.setVisited(true);
        } else {
            stack.pop();
        }

        if (stack.isEmpty()) {
            if (!isAtAnyChargingStation()) {
                lastCleaningX = currentX;
                lastCleaningY = currentY;
                dirtService.stopCleaningMode();
                if (currentCell.getWayToChargingStation() != null) {
                    stationPath = new ArrayList<>(currentCell.getWayToChargingStation());
                    Collections.reverse(stationPath);
                    stationIdx = 0;
                    isReturningFromStation = false;
                }
            } else {
                activityLogger.logMovement(currentX, currentY, "All cells visited, navigation completed");
                isNavigationCompleted = true;
            }
        }
    }

    /**
     * Finds an unvisited neighboring cell for DFS navigation.
     * Returns null if no valid neighbors are available.
     */
    private Cell getNeighborCell() {
        for (Direction direction : Direction.values()) {
            int newX = currentX + direction.getXOffset();
            int newY = currentY + direction.getYOffset();
            if (isValidMove(newX, newY)) {
                Cell neighborCell = floorMap.getCells()[newX][newY];
                if (!neighborCell.isVisited() && !sensorSimulatorService.isObstacle(newX, newY)) {
                    return neighborCell;
                }
            }
        }
        return null;
    }

    /**
     * Validates if a move to the specified coordinates is within bounds.
     */
    private boolean isValidMove(int x, int y) {
        return x >= 0 && y >= 0 && x < floorMap.getCells().length && y < floorMap.getCells()[0].length;
    }

    /**
     * Returns the current position of the robot as an array [x, y].
     */
    public int[] getCurrentPosition() {
        return new int[] { currentX, currentY };
    }

    /**
     * Checks if the navigation process is completed.
     */
    public boolean isNavigationCompleted() {
        return isNavigationCompleted;
    }

    /**
     * Initializes optimal paths from all charging stations to all cells
     * using Dijkstra's algorithm.
     */
    private void initializeAllPaths() {
        // Find paths from all charging stations and update each cell with the shortest
        // path
        List<int[]> chargingStations = findAllChargingStations();
        for (Cell[] row : floorMap.getCells()) {
            for (Cell cell : row) {
                List<int[]> shortestPath = null;
                double shortestDistance = Double.MAX_VALUE;

                // Find shortest path from each charging station
                for (int[] station : chargingStations) {
                    Object[] pathFromStationAndDistance = findPathWithLowestPower(station[0], station[1], cell.getX(),
                            cell.getY());
                    @SuppressWarnings("unchecked")
                    List<int[]> pathFromStation = (List<int[]>) pathFromStationAndDistance[0];
                    double distance = (double) pathFromStationAndDistance[1];
                    if (pathFromStation != null && distance < shortestDistance) {
                        shortestDistance = distance;
                        shortestPath = pathFromStation;
                    }
                }

                if (shortestPath != null) {
                    cell.setWayToChargingStation(shortestPath);
                    cell.setDistanceToStation(shortestDistance);
                }
            }
        }
    }

    /**
     * Returns a list of all charging station coordinates in the floor map.
     */
    private List<int[]> findAllChargingStations() {
        List<int[]> stations = new ArrayList<>();
        int size = floorMap.getCells().length;

        // Add the four corner charging stations
        stations.add(new int[] { 0, 0 }); // Top-left
        stations.add(new int[] { 0, size - 1 }); // Top-right
        stations.add(new int[] { size - 1, 0 }); // Bottom-left
        stations.add(new int[] { size - 1, size - 1 }); // Bottom-right

        return stations;
    }

    /**
     * Implements Dijkstra's algorithm to find the path with lowest power between
     * two points.
     * Avoids obstacles and considers valid moves only.
     */
    private Object[] findPathWithLowestPower(int startX, int startY, int targetX, int targetY) {
        int rows = floorMap.getCells().length;
        int cols = floorMap.getCells()[0].length;

        // Initialize distances and visited array
        double[][] distances = new double[rows][cols];
        boolean[][] visited = new boolean[rows][cols];
        // Change to store parent coordinates as a 2D array of Point objects
        Point[][] parent = new Point[rows][cols];

        // Initialize distances to infinity
        for (int i = 0; i < rows; i++) {
            Arrays.fill(distances[i], Integer.MAX_VALUE);
        }

        // Priority queue to store cells to visit (distance, x, y)
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> Double.compare(a.dist, b.dist));

        // Start from charging station
        distances[startX][startY] = 0;
        pq.offer(new Node(0, startX, startY));
        while (!pq.isEmpty()) {
            Node current = pq.poll();
            double currentCost = current.dist;
            int x = current.x;
            int y = current.y;
            Cell currentCell = floorMap.getCells()[x][y];
            if (visited[x][y])
                continue;
            visited[x][y] = true;
            // If we reached the target cell
            if (x == targetX && y == targetY) {
                return reconstructPath(parent, startX, startY, targetX, targetY);
            }

            // Check all four directions
            for (Direction direction : Direction.values()) {
                int newX = x + direction.getXOffset();
                int newY = y + direction.getYOffset();

                if (isValidMove(newX, newY) && !visited[newX][newY] && !sensorSimulatorService.isObstacle(newX, newY)) {
                    Cell nextCell = floorMap.getCells()[newX][newY];
                    // weight is moving power cost
                    double newDist = currentCost + sensorSimulatorService.getMovingPowerCost(currentCell, nextCell);
                    if (newDist < distances[newX][newY]) {
                        distances[newX][newY] = newDist;
                        parent[newX][newY] = new Point(x, y); // Store parent coordinates as Point
                        pq.offer(new Node(newDist, newX, newY));
                    }
                }
            }
        }

        return new Object[] { null, 0.0 }; // No path found
    }

    /**
     * Reconstructs the path from parent pointers after pathfinding.
     */
    private Object[] reconstructPath(Point[][] parent, int startX, int startY, int targetX, int targetY) {
        List<int[]> path = new ArrayList<>();
        int currentX = targetX;
        int currentY = targetY;
        double distance = 0.0;

        // Build path from target back to start
        while (currentX != startX || currentY != startY) {
            path.add(0, new int[] { currentX, currentY });
            Point p = parent[currentX][currentY];
            if (p == null) {
                return null; // No valid path exists
            }
            Cell curCell = floorMap.getCells()[currentX][currentY];
            Cell nxtCell = floorMap.getCells()[p.x][p.y];
            distance = distance + sensorSimulatorService.getMovingPowerCost(curCell, nxtCell);
            currentX = p.x;
            currentY = p.y;
        }

        // Add the starting point
        path.add(0, new int[] { startX, startY });
        return new Object[] { path, distance };
    }

    /**
     * Checks if the robot is currently at any charging station.
     */
    private boolean isAtAnyChargingStation() {
        return floorMap.getCells()[currentX][currentY].isChargingStation();
    }
}
