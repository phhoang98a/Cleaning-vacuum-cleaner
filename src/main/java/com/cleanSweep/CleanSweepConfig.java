package com.cleanSweep;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "clean-sweep")
public class CleanSweepConfig {
    // Getters and setters
    private int floorGridSize;
    private int cellSize;

    public void setFloorGridSize(int floorGridSize) {
        this.floorGridSize = floorGridSize;
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
    }
}



