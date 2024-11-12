package com.cleanSweep.backend.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogger {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogger.class);

    @Autowired
    private LogRepoImpl logRepo;

    public void logMovement(int x, int y, String direction) {
        String message = String.format("Moved to position (%d, %d) in direction: %s", x, y, direction);
        logger.info(message);
        logRepo.saveLog(message);
    }

    public void logBatteryUsage(double batteryLife) {
        String message = String.format("Battery remaining: %.1f units", batteryLife);
        logger.info(message);
        logRepo.saveLog(message);
    }

    public void logCleaning(int x, int y) {
        String message = String.format("Cleaned dirt at (%d, %d)", x, y);
        logger.info(message);
        logRepo.saveLog(message);
    }

    public void logDirtFull() {
        String message = "Dirt capacity is full";
        logger.info(message);
        logRepo.saveLog(message);
    }

    public void logRecharge() {
        String message = "Battery recharged to full capacity";
        logger.info(message);
        logRepo.saveLog(message);
    }

    public void logNoDirtAtPosition(int x, int y) {
        String message = String.format("No dirt found at position (%d, %d)", x, y);
        logger.info(message);
        logRepo.saveLog(message);
    }

    public void logObstacle(int x, int y) {
        String message = String.format("Encountered obstacle at position (%d, %d)", x, y);
        logger.info(message);
        logRepo.saveLog(message);
    }

    public void logLowBattery() {
        String message = "Low battery, returning to charging station";
        logger.info(message);
        logRepo.saveLog(message);
    }
}

