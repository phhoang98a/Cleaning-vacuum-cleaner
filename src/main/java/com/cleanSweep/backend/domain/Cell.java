package com.cleanSweep.backend.domain;

import java.util.List;

import com.cleanSweep.backend.common.FloorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Cell {
    private int x;
    private int y;
    private FloorType floorType;
    private boolean isVisited;
    private boolean isChargingStation;
    private boolean isObstacle;
    private boolean isStairs;
    private int dirtLevel;
    private List<int[]> wayToChargingStation;
    private double distanceToStation;

    public void reduceDirtLevel() {
        if (dirtLevel > 0) {
            dirtLevel = 0;
        }
    }
}
