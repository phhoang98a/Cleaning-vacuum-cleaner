package com.cleanSweep;

import com.cleanSweep.backend.application.SensorSimulatorService;
import com.cleanSweep.backend.common.FloorType;
import com.cleanSweep.backend.domain.Cell;
import com.cleanSweep.backend.domain.FloorMap;
import com.cleanSweep.backend.infrastructure.ActivityLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SensorSimulatorServiceTest {

    @Mock
    private FloorMap floorMap;
    @Mock
    private ActivityLogger activityLogger;

    @InjectMocks
    private SensorSimulatorService sensorSimulatorService;

    private Cell[][] cells;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set the gridSize field using reflection
        try {
            java.lang.reflect.Field gridSizeField = SensorSimulatorService.class.getDeclaredField("gridSize");
            gridSizeField.setAccessible(true);
            gridSizeField.set(sensorSimulatorService, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize a 5x5 grid
        cells = new Cell[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                cells[i][j] = new Cell(i, j, FloorType.BARE_FLOOR, false, false, false, false, 0, null,0);
            }
        }
        when(floorMap.getCells()).thenReturn(cells);

        // Now initialize the service
        sensorSimulatorService.initializeObstacleAndDirt();
    }

    @Test
    void testIsDirtPresent() {
        cells[2][2].setDirtLevel(2);
        assertTrue(sensorSimulatorService.isDirtPresent(2, 2));

        cells[3][3].setDirtLevel(0);
        assertFalse(sensorSimulatorService.isDirtPresent(3, 3));
    }

    @Test
    void testCleanDirt() {
        cells[2][2].setDirtLevel(3);
        sensorSimulatorService.cleanDirt(2, 2);

        assertEquals(0, cells[2][2].getDirtLevel());
        verify(activityLogger).logCleaning(2, 2);
    }

    @Test
    void testCleanNoDirt() {
        cells[3][3].setDirtLevel(0);
        sensorSimulatorService.cleanDirt(3, 3);
        verify(activityLogger).logNoDirtAtPosition(3, 3);
    }

    @Test
    void testGetSurfacePowerCost() {
        cells[1][1].setFloorType(FloorType.BARE_FLOOR);
        cells[2][2].setFloorType(FloorType.LOW_PILE_CARPET);
        cells[3][3].setFloorType(FloorType.HIGH_PILE_CARPET);

        assertEquals(1, sensorSimulatorService.getSurfaceUnit(cells[1][1]));
        assertEquals(2, sensorSimulatorService.getSurfaceUnit(cells[2][2]));
        assertEquals(3, sensorSimulatorService.getSurfaceUnit(cells[3][3]));
    }

    @Test
    void testChargingStationLocations() {
        // Test charging stations at corners
        assertTrue(sensorSimulatorService.isChargingStation(0, 0));
        assertTrue(sensorSimulatorService.isChargingStation(0, 4));
        assertTrue(sensorSimulatorService.isChargingStation(4, 0));
        assertTrue(sensorSimulatorService.isChargingStation(4, 4));
        
        // Test non-charging station location
        assertFalse(sensorSimulatorService.isChargingStation(2, 2));
    }
}
