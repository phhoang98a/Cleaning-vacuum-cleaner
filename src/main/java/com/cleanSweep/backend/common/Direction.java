package com.cleanSweep.backend.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Direction {
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    private final int xOffset;
    private final int yOffset;

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }
}
