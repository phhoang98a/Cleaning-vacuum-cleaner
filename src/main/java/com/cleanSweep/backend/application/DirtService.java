package com.cleanSweep.backend.application;

import com.cleanSweep.backend.domain.Cell;
import com.cleanSweep.backend.domain.FloorMap;
import com.cleanSweep.backend.infrastructure.ActivityLogger;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class DirtService {

    @Autowired
    private SensorSimulatorService sensorSimulatorService;

    @Autowired
    private FloorMap floorMap;

    @Autowired
    private BatteryService batteryService;

    @Autowired
    private ActivityLogger activityLogger;

    @Value("${clean-sweep.dirt.capacity}")
    private int dirtCapacity;

    private int currentCapacity = 0;

    private int mode = 0; // 0 is cleaning, 1 is stop cleaning

    private int cleanedDirtCellCount = 0;

    /**
     * Cleans dirt at the specified coordinates if dirt is present and capacity allows.
     */
    public void cleanDirt(int x, int y) {
        Cell cell = floorMap.getCells()[x][y];
        int dirtLevel = cell.getDirtLevel();

        if (dirtLevel > 0 && currentCapacity < dirtCapacity) {
            // Reduce dirt
            cell.reduceDirtLevel();  // Clean up dirt
            currentCapacity++;
            cleanedDirtCellCount++;
            activityLogger.logCleaning(x, y);

            if (currentCapacity >= dirtCapacity) {
                activityLogger.logDirtFull();
            }
        }
    }

    /**
     * Resets the current dirt capacity to zero.
     */
    public void removeDirt() {
        currentCapacity = 0;
    }

    /**
     * Sets the service to cleaning mode.
     */
    public void setCleaningMode() {
        mode = 0;
    }

    /**
     * Stops the cleaning mode.
     */
    public void stopCleaningMode() {
        mode = 1;
    }

    /**
     * Checks if the cleaning mode is active.
     */
    public boolean isCleaningActive() {
        return mode == 0;
    }

    /**
     * Checks if the dirt capacity is full.
     */
    public boolean isFullDirt() {
        return currentCapacity >= dirtCapacity;
    }

    /**
     * Checks if dirt is present at the specified coordinates using the sensor.
     */
    public boolean isDirtPresent(int x, int y) {
        return sensorSimulatorService.isDirtPresent(x, y);
    }
}


