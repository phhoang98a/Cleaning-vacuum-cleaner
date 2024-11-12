package com.cleanSweep.frontend.ui;

import com.cleanSweep.backend.application.BatteryService;
import com.cleanSweep.backend.application.DirtService;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;

public class HUDController extends VBox {

    private final Label batteryLabel;
    private final Label dirtCapacityLabel;

    @Autowired
    private BatteryService batteryService;

    @Autowired
    private DirtService dirtService;

    public HUDController() {
        batteryLabel = new Label();
        dirtCapacityLabel = new Label();

        this.getChildren().addAll(batteryLabel, dirtCapacityLabel);
        update();
    }

    /**
     * Updates the HUD with the current battery and dirt capacity status.
     */
    public void update() {
        batteryLabel.setText("Battery: " + batteryService.getBattery() + "%");
        dirtCapacityLabel.setText("Dirt Capacity: "
                + dirtService.getCurrentCapacity() + " / " + dirtService.getDirtCapacity());
    }
}
