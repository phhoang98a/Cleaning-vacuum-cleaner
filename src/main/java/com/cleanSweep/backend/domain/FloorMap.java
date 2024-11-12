package com.cleanSweep.backend.domain;

import com.cleanSweep.backend.common.FloorType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
public class FloorMap {

    private Cell[][] cells;

    /**
     * Initialize the grid based on the given gridSize.
     * @param size grid size
     */
    public void initializeGrid(int size) {
        cells = new Cell[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell(i, j, FloorType.BARE_FLOOR, false, false, false, false, 0, null,0);
            }
        }
    }

    @Getter
    static class FloorPlanData {
        private int floorSize;
        private List<CellData> cells;
    }

    @Getter
    static class CellData {
        private int x;
        private int y;
        private FloorType floorType;

        @JsonProperty("isChargingStation")
        private boolean isChargingStation;

        @JsonProperty("isStairs")
        private boolean isStairs;
    }
}
