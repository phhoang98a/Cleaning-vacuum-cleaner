package com.cleanSweep.backend.application;

import com.cleanSweep.backend.domain.FloorMap;
import com.cleanSweep.backend.domain.Cell;
import com.cleanSweep.backend.common.FloorType;
import com.cleanSweep.backend.infrastructure.ActivityLogger;
import com.cleanSweep.backend.application.interfaces.Sensor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SensorSimulatorService implements Sensor {

    private boolean[][] obstacleGrid;
    private boolean[][] chargingStationGrid;

    @Autowired
    private FloorMap floorMap;

    @Autowired
    private ActivityLogger activityLogger;

    @Value("${clean-sweep.floor-grid-size}")
    private int gridSize;

    private int numberOfDirtCell;

    @PostConstruct
    public void initializeObstacleAndDirt() {
        floorMap.initializeGrid(gridSize);
        this.obstacleGrid = generateObstacles(gridSize, gridSize);
        this.chargingStationGrid = generateChargingStations(gridSize, gridSize);
        this.numberOfDirtCell = 0;

        Random random = new Random();
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Cell cell = floorMap.getCells()[x][y];
                if (obstacleGrid[x][y]) {
                    cell.setObstacle(true);
                } else if (chargingStationGrid[x][y]) {
                    cell.setChargingStation(true);
                } else {
                    int floorTypeRandom = random.nextInt(4);
                    switch (floorTypeRandom) {
                        case 0:
                        case 1: // 50% chance for BARE_FLOOR
                            cell.setFloorType(FloorType.BARE_FLOOR);
                            break;
                        case 2:
                            cell.setFloorType(FloorType.LOW_PILE_CARPET);
                            break;
                        case 3:
                            cell.setFloorType(FloorType.HIGH_PILE_CARPET);
                            break;
                    }
                    if (random.nextInt(3) != 0){
                        cell.setDirtLevel(1);
                        numberOfDirtCell++;
                    }
                }
            }
        }
    }

    /**
     * Randomly generates obstacle grids.
     * @param width  Width of the grid
     * @param height Height of the grid
     * @return A two-dimensional array representing obstacles
     */
    private boolean[][] generateObstacles(int width, int height) {
        boolean[][] grid = new boolean[width][height];
        Random random = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if ((x == 0 && y == 0) || (x == 0 && y == width - 1) || (x == height - 1 && y == 0)
                        || (x == height - 1 && y == width - 1)) {
                    continue;
                }
                grid[x][y] = random.nextInt(7) == 0;
            }
        }
        return grid;
    }

    /**
     * Generates charging station locations on the grid.
     * @param width  Width of the grid
     * @param height Height of the grid
     * @return A two-dimensional array representing charging stations
     */
    private boolean[][] generateChargingStations(int width, int height) {
        boolean[][] grid = new boolean[width][height];

        grid[0][0] = true;
        grid[0][width - 1] = true;
        grid[height - 1][0] = true;
        grid[height - 1][width - 1] = true;

        return grid;
    }

    @Override
    public boolean isDirtPresent(int x, int y) {
        return floorMap.getCells()[x][y].getDirtLevel() > 0;
    }

    @Override
    public boolean isObstacle(int x, int y) {
        return obstacleGrid[x][y] == true;
    }

    @Override
    public void cleanDirt(int x, int y) {
        Cell cell = floorMap.getCells()[x][y];
        if (cell.getDirtLevel() > 0) {
            cell.reduceDirtLevel();
            activityLogger.logCleaning(x, y);
        } else {
            activityLogger.logNoDirtAtPosition(x, y);
        }
    }

    @Override
    public String getSurfaceType(int x, int y) {
        return floorMap.getCells()[x][y].getFloorType().toString();
    }

    /**
     * Move from location A to location B is the average of
     * the required charge costs for the surfaces at the two locations
     */
    @Override
    public double getMovingPowerCost(Cell previousCell, Cell currentCell) {
        if (previousCell == null) {
            return 0;
        }
        int previousUnit = getSurfaceUnit(previousCell);
        int currentUnit = getSurfaceUnit(currentCell);
        double result = (previousUnit + currentUnit) / 2.0; // Ensure the result is a double
        return Math.round(result * 10) / 10.0; // Round to 1 decimal place
    }

    /**
     * Costs the same amount of surface unit to clean the current location
     */
    @Override
    public double getCleaningPowerCost(Cell cell) {
        if (cell.getDirtLevel() > 0){
            return getSurfaceUnit(cell);
        } else {
            return 0;
        }
    }

    public int getSurfaceUnit(Cell cell) {
        FloorType floorType = cell.getFloorType();
        switch (floorType) {
            case BARE_FLOOR:
                return 1;
            case LOW_PILE_CARPET:
                return 2;
            case HIGH_PILE_CARPET:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Checks if all dirt cells are cleaned
     */
    public boolean isCleanAll(int cleanedDirtCellCount){
        return numberOfDirtCell == cleanedDirtCellCount;
    }

    /**
     * Checks if the specified coordinates are a charging station.
     */
    public boolean isChargingStation(int x, int y) {
        return chargingStationGrid[x][y];
    }
}
