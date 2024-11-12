package com.cleanSweep.frontend.visualization;

import com.cleanSweep.backend.application.NavigationService;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RobotVisualizer {

    private final NavigationService navigationService;
    private final int cellSize; // Injected cell size

    public RobotVisualizer(NavigationService navigationService, int cellSize) {
        this.navigationService = navigationService;
        this.cellSize = cellSize;
    }

    /**
     * Renders the robot's current position on the canvas.
     */
    public void render(GraphicsContext gc) {
        int[] position = navigationService.getCurrentPosition();
        gc.setFill(Color.BLUE);
        gc.fillOval(position[1] * cellSize, position[0] * cellSize, cellSize, cellSize);
    }
}
