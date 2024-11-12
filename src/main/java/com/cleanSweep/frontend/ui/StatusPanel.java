package com.cleanSweep.frontend.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatusPanel extends VBox {

    private final Label batteryStatusLabel;
    private final Label dirtCapacityLabel;

    public StatusPanel() {
        batteryStatusLabel = new Label("Battery: 100%");
        dirtCapacityLabel = new Label("Dirt Capacity: 0 / 50");

        this.getChildren().addAll(batteryStatusLabel, dirtCapacityLabel);
    }

    /**
     * Updates the battery status label with the current battery life.
     */
    public void updateBatteryStatus(int batteryLife) {
        batteryStatusLabel.setText("Battery: " + batteryLife + "%");
    }

    /**
     * Updates the dirt capacity label with the current and maximum dirt capacity.
     */
    public void updateDirtCapacity(int currentDirt, int maxDirtCapacity) {
        dirtCapacityLabel.setText("Dirt Capacity: " + currentDirt + " / " + maxDirtCapacity);
    }
}
