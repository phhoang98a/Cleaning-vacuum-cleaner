package com.cleanSweep.backend.application;

import com.cleanSweep.backend.infrastructure.ActivityLogger;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BatteryService {

    @Getter
    private double battery;

    @Value("${clean-sweep.battery.low-threshold}")
    private int lowBatteryThreshold;

    @Getter
    @Value("${clean-sweep.battery.full-charge}")
    private int fullChargeValue;

    @Autowired
    private ActivityLogger activityLogger;

    @PostConstruct
    public void init() {
        this.battery = fullChargeValue;  // Initialize after dependency injection is completed
    }

    /**
     * Consumes a specified amount of power from the battery.
     * If the battery is insufficient, it triggers a recharge.
     */
    public void consumePower(double units) {
        if (battery > units) {
            battery -= units;
            activityLogger.logBatteryUsage(battery);
        } else {
            System.out.println("Battery depleted. Returning to charging station.");
            recharge();
        }
    }

    /**
     * Checks if a recharge is needed based on the current battery level
     * and the battery needed to reach the charging station.
     */
    public boolean isRechargeNeeded(double batteryToReachStation) {
        return battery <= batteryToReachStation + 7;
    }

    /**
     * Checks if the battery is depleted.
     */
    public boolean isBatteryDepleted() {
        return battery <= 0;
    }

    /**
     * Checks if the battery has sufficient power above the low threshold.
     */
    public boolean hasSufficientPower() {
        return battery > lowBatteryThreshold;
    }

    /**
     * Recharges the battery to its full capacity.
     */
    public void recharge() {
        battery = fullChargeValue;
        activityLogger.logRecharge();
    }
}

