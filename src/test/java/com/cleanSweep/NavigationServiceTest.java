package com.cleanSweep;

import com.cleanSweep.backend.application.*;
import com.cleanSweep.backend.common.FloorType;
import com.cleanSweep.backend.domain.Cell;
import com.cleanSweep.backend.domain.FloorMap;
import com.cleanSweep.backend.infrastructure.ActivityLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

class NavigationServiceTest {

    @Mock
    private FloorMap floorMap;
    @Mock
    private DirtService dirtService;
    @Mock
    private BatteryService batteryService;
    @Mock
    private SensorSimulatorService sensorSimulatorService;
    @Mock
    private ActivityLogger activityLogger;

    @InjectMocks
    private NavigationService navigationService;

    private Cell[][] cells;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize a 5x5 grid
        cells = new Cell[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                cells[i][j] = new Cell(i, j, FloorType.BARE_FLOOR, false, false, false, false, 0, null,0);
            }
        }
        when(floorMap.getCells()).thenReturn(cells);

        // Mock batteryService and sensor behaviors
        when(batteryService.hasSufficientPower()).thenReturn(true);
        when(sensorSimulatorService.isObstacle(anyInt(), anyInt())).thenReturn(false);
        when(dirtService.isCleaningActive()).thenReturn(true);
    }

    @Test
    void testStartNavigation() {
        navigationService.startNavigation(0, 0);
        verify(activityLogger).logMovement(0, 0, "Start");
    }

    @Test
    void testStepNavigationWithDirt() {
        // Place dirt on cell (0, 0)
        cells[0][0].setDirtLevel(3);
        navigationService.startNavigation(0, 0);
        navigationService.stepNavigation();

        // Verify that the dirt was cleaned and logged
        verify(dirtService).cleanDirt(0, 0);
        verify(activityLogger).logMovement(0, 0, "Visiting");
    }

    @Test
    void testNavigationToChargingStation() {
        // Set up a low battery scenario
        when(batteryService.isRechargeNeeded(anyDouble())).thenReturn(true);
        cells[0][0].setChargingStation(true);

        navigationService.startNavigation(1, 1);
        navigationService.stepNavigation();

        // Verify that the robot attempts to return to charging station
        verify(dirtService).stopCleaningMode();
    }

    @Test
    void testStepNavigationCompletion() {
        // Set up initial conditions
        cells[0][0].setChargingStation(true);
        when(dirtService.isCleaningActive()).thenReturn(true);  // Return true to allow cleaning
        when(batteryService.isRechargeNeeded(anyInt())).thenReturn(false);
        
        // Mock the path to charging station
        List<int[]> path = new ArrayList<>();
        path.add(new int[]{0, 0});
        cells[0][0].setWayToChargingStation(path);
        
        // Start navigation
        navigationService.startNavigation(0, 0);
        
        // Simulate steps until completion
        for (int i = 0; i < 100; i++) {  // Increased loop count to ensure completion
            navigationService.stepNavigation();
            // Break if navigation is completed
            if (navigationService.isNavigationCompleted()) {
                break;
            }
        }

        // Verify the completion
        verify(activityLogger, atLeastOnce()).logMovement(eq(navigationService.getCurrentPosition()[0]), eq(navigationService.getCurrentPosition()[1]), eq("All cells visited, navigation completed"));
        assertTrue(navigationService.isNavigationCompleted());
    }

}
