package com.cleanSweep.backend.application.interfaces;

import com.cleanSweep.backend.domain.Cell;

public interface Sensor {
    boolean isDirtPresent(int x, int y);
    void cleanDirt(int x, int y);
    boolean isObstacle(int x, int y);
    String getSurfaceType(int x, int y);
    double getMovingPowerCost(Cell previousCell, Cell currentCell);
    double getCleaningPowerCost(Cell cell);
}
