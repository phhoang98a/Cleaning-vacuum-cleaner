package com.cleanSweep.frontend.visualization;

import com.cleanSweep.backend.application.BatteryService;
import com.cleanSweep.backend.application.DirtService;
import com.cleanSweep.backend.application.SensorSimulatorService;
import com.cleanSweep.backend.domain.Cell;
import com.cleanSweep.backend.domain.FloorMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class FloorPlanVisualizer {

    private final SensorSimulatorService sensorSimulatorService;
    private final DirtService dirtService;
    private final BatteryService batteryService;
    private final FloorMap floorMap;
    private final int gridSize; // Injected grid size
    private final int cellSize; // Injected cell size

    public FloorPlanVisualizer(SensorSimulatorService sensorSimulatorService, DirtService dirtService,
            BatteryService batteryService,
            FloorMap floorMap, int gridSize, int cellSize) {
        this.sensorSimulatorService = sensorSimulatorService;
        this.dirtService = dirtService;
        this.batteryService = batteryService;
        this.floorMap = floorMap;
        this.gridSize = gridSize;
        this.cellSize = cellSize;
    }

    /**
     * Renders the floor plan, including cells, dirt, and obstacles.
     */
    public void render(GraphicsContext gc) {
        // Render the grid
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Cell currentCell = floorMap.getCells()[x][y];

                // First render floor type
                switch (currentCell.getFloorType()) {
                    case BARE_FLOOR:
                        gc.setFill(Color.LIGHTGRAY);
                        break;
                    case LOW_PILE_CARPET:
                        gc.setFill(Color.KHAKI);
                        break;
                    case HIGH_PILE_CARPET:
                        gc.setFill(Color.SANDYBROWN);
                        break;
                }
                gc.fillRect(y * cellSize, x * cellSize, cellSize, cellSize);

                // Then render charging stations
                if (currentCell.isChargingStation()) {
                    gc.setFill(Color.GREEN);
                    gc.fillRect(y * cellSize, x * cellSize, cellSize, cellSize);
                }

                // Then render dirt on top if present
                if (currentCell.getDirtLevel() > 0) {
                    gc.setFill(Color.DARKGRAY); // A darker gray for the dirt dots
    
                    // Calculate the center of the cell
                    double centerX = (y * cellSize) + (cellSize / 2);
                    double centerY = (x * cellSize) + (cellSize / 2);
                    double dotSize = cellSize * 0.1; // Small dot size
    
                    // Draw 5 dots around the center
                    gc.fillOval(centerX - dotSize * 2, centerY - dotSize * 2, dotSize, dotSize); // Top-left
                    gc.fillOval(centerX + dotSize, centerY - dotSize * 2, dotSize, dotSize);      // Top-right
                    gc.fillOval(centerX - dotSize, centerY, dotSize, dotSize);                    // Center
                    gc.fillOval(centerX - dotSize * 2, centerY + dotSize, dotSize, dotSize);      // Bottom-left
                    gc.fillOval(centerX + dotSize, centerY + dotSize, dotSize, dotSize);          // Bottom-right
                }

                // Finally render obstacles
                if (sensorSimulatorService.isObstacle(x, y)) {
                    gc.setFill(Color.BLACK);
                    gc.fillRect(y * cellSize, x * cellSize, cellSize, cellSize);
                }

                // Draw grid lines last to ensure they're always visible
                gc.setStroke(Color.GRAY);
                gc.strokeRect(y * cellSize, x * cellSize, cellSize, cellSize);
            }
        }

        renderColorLegend(gc);
    }

    /**
     * Renders the color legend for the floor types and status indicators.
     */
    private void renderColorLegend(GraphicsContext gc) {
        gc.setFont(new Font(14));

        double leftLegendX = 10;
        double rightLegendX = 270;
        double legendStartY = gridSize * cellSize + 20;
        double entrySpacing = 22;
        double barWidth = 100;
        double barHeight = 20;

        // Battery Progress Bar
        double batteryPercentage = (double) batteryService.getBattery() / batteryService.getFullChargeValue();
        
        // Draw battery progress bar background
        gc.setFill(Color.GRAY);
        gc.fillRect(leftLegendX, legendStartY, barWidth, barHeight);
        
        // Set color based on battery percentage
        Color batteryColor;
        if (batteryPercentage > 0.75) {
            batteryColor = Color.GREEN;  // 75-100% green
        } else if (batteryPercentage > 0.5) {
            batteryColor = Color.ORANGE;  // 50-75% orange
        } else if (batteryPercentage > 0.25) {
            batteryColor = Color.YELLOW;  // 25-50% yellow
        } else {
            batteryColor = Color.RED;  // 0-25% red
        }
        
        gc.setFill(batteryColor);
        gc.fillRect(leftLegendX, legendStartY, barWidth * batteryPercentage, barHeight);
        gc.setFill(Color.BLACK);
        gc.fillText("Battery: " + batteryService.getBattery() + "/" + batteryService.getFullChargeValue(),
                leftLegendX + barWidth + 10, legendStartY + barHeight - 5);

        // Dirt Progress Bar
        double dirtPercentage = (double) dirtService.getCurrentCapacity() / dirtService.getDirtCapacity();
        
        // Draw dirt progress bar background
        gc.setFill(Color.GRAY);
        gc.fillRect(leftLegendX, legendStartY + entrySpacing, barWidth, barHeight);
        
        // Set color based on dirt capacity percentage
        Color dirtColor;
        if (dirtPercentage < 0.25) {
            dirtColor = Color.GREEN;  // 0-25% green
        } else if (dirtPercentage < 0.5) {
            dirtColor = Color.YELLOW;  // 25-50% yellow
        } else if (dirtPercentage < 0.75) {
            dirtColor = Color.ORANGE;  // 50-75% orange
        } else {
            dirtColor = Color.RED;  // 75-100% red
        }
        
        gc.setFill(dirtColor);
        gc.fillRect(leftLegendX, legendStartY + entrySpacing, barWidth * dirtPercentage, barHeight);
        gc.setFill(Color.BLACK);
        gc.fillText("Dirt: " + dirtService.getCurrentCapacity() + "/" + dirtService.getDirtCapacity(),
                leftLegendX + barWidth + 10, legendStartY + entrySpacing + barHeight - 5);

        // Status text
        String txt = dirtService.isCleaningActive()
                ? "The vacuum is cleaning"
                : "The vacuum is returning to the\nstation to recharge and empty dirt";

        String[] lines = txt.split("\n");
        gc.fillText("Status:", leftLegendX, legendStartY + entrySpacing * 2 + barHeight - 5);
        double lineYOffset = legendStartY + entrySpacing * 2 + barHeight - 5;
        double lineSpacing = 15;
        for (int i = 0; i < lines.length; i++) {
            gc.fillText(lines[i], leftLegendX + 50, lineYOffset + (i * lineSpacing));
        }

        // Floor Types Legend (Right Side)
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(rightLegendX, legendStartY, 15, 15);
        gc.setFill(Color.BLACK);
        gc.fillText("Bare Floor (1 power unit)", rightLegendX + 30, legendStartY + 15);

        gc.setFill(Color.KHAKI);
        gc.fillRect(rightLegendX, legendStartY + entrySpacing, 15, 15);
        gc.setFill(Color.BLACK);
        gc.fillText("Low Pile Carpet (2 power units)", rightLegendX + 30, legendStartY + entrySpacing + 15);

        gc.setFill(Color.SANDYBROWN);
        gc.fillRect(rightLegendX, legendStartY + entrySpacing * 2, 15, 15);
        gc.setFill(Color.BLACK);
        gc.fillText("High Pile Carpet (3 power units)", rightLegendX + 30, legendStartY + entrySpacing * 2 + 15);

        gc.setFill(Color.DARKGRAY); // Set color to match dirt dots
        double dotSize = 11; // Size of the dot in the legend
        gc.fillOval(rightLegendX, legendStartY + entrySpacing * 3, dotSize, dotSize); // Draw dot instead of a square
        
        gc.setFill(Color.BLACK);
        // Adjusted Y position to center the text better
        gc.fillText("Dirt", rightLegendX + 30, legendStartY + entrySpacing * 3 + dotSize + 2); 

        gc.setFill(Color.BLACK);
        gc.fillRect(rightLegendX, legendStartY + entrySpacing * 4, 15, 15);
        gc.setFill(Color.BLACK);
        gc.fillText("Obstacle", rightLegendX + 30, legendStartY + entrySpacing * 4 + 15);

        gc.setFill(Color.GREEN);
        gc.fillRect(rightLegendX, legendStartY + entrySpacing * 5, 15, 15);
        gc.setFill(Color.BLACK);
        gc.fillText("Charging Station", rightLegendX + 30, legendStartY + entrySpacing * 5 + 15);
    }

}
