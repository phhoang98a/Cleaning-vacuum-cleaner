package com.cleanSweep;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import com.cleanSweep.backend.application.*;
import com.cleanSweep.backend.domain.FloorMap;
import com.cleanSweep.frontend.visualization.FloorPlanVisualizer;
import com.cleanSweep.frontend.visualization.RobotVisualizer;

@SpringBootApplication
@EnableConfigurationProperties(CleanSweepConfig.class)  // Ensure properties are loaded
public class CleanSweepApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private AnimationTimer timer;

    @Override
    public void init() throws Exception {
        // Initialize Spring context
        springContext = new SpringApplicationBuilder(CleanSweepApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) {
        // Manually obtain CleanSweetConfig bean
        CleanSweepConfig cleanSweepConfig = springContext.getBean(CleanSweepConfig.class);

        // Use the properties in cleanSweepConfig
        int gridSize = cleanSweepConfig.getFloorGridSize();
        int cellSize = cleanSweepConfig.getCellSize();

        // Retrieve other beans from the Spring context
        FloorMap floorMap = springContext.getBean(FloorMap.class);
        NavigationService navigationService = springContext.getBean(NavigationService.class);
        BatteryService batteryService = springContext.getBean(BatteryService.class);
        DirtService dirtService = springContext.getBean(DirtService.class);
        SensorSimulatorService sensorSimulatorService = springContext.getBean(SensorSimulatorService.class);

        // Adjust canvas size using gridSize and cellSize
        Canvas canvas = new Canvas(gridSize * cellSize, gridSize * cellSize + 150); // 150 is reserved space for legends
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Passing dependencies to FloorPlanVisualizer and RobotVisualizer
        FloorPlanVisualizer floorPlanVisualizer = new FloorPlanVisualizer(sensorSimulatorService, dirtService, batteryService, floorMap, gridSize, cellSize);
        RobotVisualizer robotVisualizer = new RobotVisualizer(navigationService, cellSize);

        // Rendering Grid
        floorPlanVisualizer.render(gc);
        robotVisualizer.render(gc);  // beginning at(0, 0)

        HBox controls = getBox(navigationService, gc, floorPlanVisualizer, robotVisualizer);

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(controls);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Clean Sweep Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates a control panel with Start and Stop buttons for the simulation.
     * Initializes the navigation service and starts the animation.
     */
    private HBox getBox(NavigationService navigationService, GraphicsContext gc, FloorPlanVisualizer floorPlanVisualizer, RobotVisualizer robotVisualizer) {
        Button startButton = new Button("Start");
        Button stopButton = new Button("Stop");

        startButton.setOnAction(e -> {
            navigationService.startNavigation(0, 0);  // Initialize navigation
            startAnimation(gc, floorPlanVisualizer, robotVisualizer, navigationService);
        });
        stopButton.setOnAction(e -> stopAnimation());

        HBox controls = new HBox(10, startButton, stopButton);
        controls.setPadding(new Insets(10));
        return controls;
    }

    /**
     * Starts the animation timer to update the robot's state and render the visuals.
     */
    private void startAnimation(GraphicsContext gc, FloorPlanVisualizer floorPlanVisualizer,
                                RobotVisualizer robotVisualizer, NavigationService navigationService) {
        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 500_000_000) {  // Update every 500ms
                    navigationService.stepNavigation();  // Update the robot's state
                    
                    // Render current state
                    gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
                    floorPlanVisualizer.render(gc);
                    robotVisualizer.render(gc);
                    
                    lastUpdate = now;

                    if (navigationService.isNavigationCompleted()) {
                        stopAnimation();  // Stop animation when navigation is completed
                    }
                }
            }
        };
        timer.start();
    }

    /**
     * Stops the animation timer when the simulation is completed.
     */
    private void stopAnimation() {
        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    public void stop() throws Exception {
        // Close Spring context and exit the application
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args); // Launch JavaFX application
    }
}





