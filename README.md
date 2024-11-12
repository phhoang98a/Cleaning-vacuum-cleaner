# Clean Sweep System

**Group 8:** 
- Dylan Neal
- Huy Hoang Phan
- Zhihong He
- Faizan Moin Lateefuddin

The Clean Sweep project simulates an intelligent robotic vacuum designed to autonomously navigate and clean a typical household environment. Key features include obstacle avoidance, power management, dirt detection, and sensor simulation, making it a comprehensive system for robotic vacuum functionality.

**Tech Stack**: Spring Boot, JavaFX, Java  
**Architecture**: Modular and scalable, following clean architecture principles for separation of concerns.

## Project Overview
The Clean Sweep system consists of several core modules:

- **Navigation and Control**: The vacuum navigates autonomously, avoiding obstacles and returning to the charging station when power is low.
- **Dirt Detection and Cleaning**: Detects and cleans dirt based on surface type and dirt level.
- **Power Management**: Manages battery levels considering surface types and navigation/cleaning activities.
- **Sensor Simulation**: Simulates real-world sensor data for obstacle detection, surface recognition, and dirt levels.

## Project Structure

### ***BackEnd***
### 1. Common
- **Enums**: Direction (UP, DOWN, LEFT, RIGHT), FloorType (BARE_FLOOR, LOW_PILE_CARPET, HIGH_PILE_CARPET).

### 2. Model (Domain)
- **Cell**: Represents a floor map cell, containing floor type, dirt level, and obstacle information.
- **FloorMap**: Represents the floor plan with methods for initialization and loading.

### 3. Controller
- **LogEntryController**: Manages system logs (e.g., movements, cleaning actions, recharging).

### 4. Application (Services)
- **NavigationService**: Controls vacuum navigation using a modified DFS (Depth-First Search) algorithm combined with Dijkstra's algorithm for optimal pathfinding.
- **BatteryService**: Manages battery consumption and recharging.
- **DirtService**: Cleans detected dirt and updates the vacuum’s capacity.
- **SensorSimulatorService**: Simulates sensor behavior for dirt, obstacles, and floor types.

### 5. Infrastructure
- **ActivityLogger**: Logs navigation, dirt cleaned, obstacles, and battery usage.
- **LogEntryRepository**: Persists log entries for system activity history.

### ***Frontend (Visualization)***
- **FloorPlanVisualizer**: Renders the floor map, showing the vacuum, obstacles, and dirt.
- **RobotVisualizer**: Visualizes the vacuum's movements and actions.
- **HUDController**: Displays battery status, dirt level, and system status.

## Key Functionalities
- **DFS Navigation**: Ensures the vacuum systematically moves, avoiding obstacles.
- **Battery Management**: Returns the vacuum to the charging station when power is low.
- **Dirt Detection**: Cleans dirty cells as the vacuum traverses the floor.
- **Sensor Simulation**: Mimics real-world sensor data (e.g., floor types, dirt levels, obstacles).

## Testing
The system is covered by unit tests for all major modules:

- **NavigationServiceTest**: Tests BFS navigation, logging, and battery management.
- **SensorSimulatorServiceTest**: Validates sensor accuracy for dirt, floor type, and obstacles.
- **CleanSweepSystemApplicationTests**: Ensures seamless module integration.

## Resources
- **Application Properties**: Configured in `resources/application.properties` (e.g., grid size, battery capacity, dirt levels).

## Robot Clean 


https://github.com/user-attachments/assets/6c3fbc9a-ed42-43bf-b38e-030716b1f209



## BackEnd Running Condition
![73fd3f55a80079a7fe355f117cc89fb](https://github.com/user-attachments/assets/f907a2ca-377e-408e-b211-4fa8b63937ec)
![459250860cad3bf80059f33f54acae5](https://github.com/user-attachments/assets/e8960ed3-ac6e-4f58-8706-b3e05a91674b)

## API Test
![aa8b8da91389fc53ccc1c0f39485309](https://github.com/user-attachments/assets/39121677-8d9c-4fa2-a0df-187fb19e42a2)
![0a51b546c3ef78d03a3e96e11460de2](https://github.com/user-attachments/assets/e8b97fbc-13f5-4c42-9a33-eb12ba8b0304)

# H2 Database
![dcbb3714bba0e544b78ec673f8f45ec](https://github.com/user-attachments/assets/fb7bf006-bf15-48d9-8fe7-4b4a3a80e41b)

# Unit Test And Travis
![0d27c714a49e8d2862caa3803f8b238](https://github.com/user-attachments/assets/6bf3da48-1628-4ac0-993e-1aa6a7c65a1a)
![image](https://github.com/user-attachments/assets/ded2bba9-ff20-4c3c-8c30-08bd5326c62e)

### CI/CD Pipeline
[![GitHub Actions Status](https://github.com/DylanFromDepaul/Clean-Sweep/workflows/Clean%20Sweep%20CI%2FCD%20Pipeline/badge.svg)](https://github.com/DylanFromDepaul/Clean-Sweep/actions)
[![TeamCity Build Status](https://cleansweep.teamcity.com/app/rest/builds/buildType:(id:id135e61f841174c0fB288277eacd25adb_PipelineHead)/statusIcon)](https://cleansweep.teamcity.com/buildConfiguration/id135e61f841174c0fB288277eacd25adb_PipelineHead?mode=builds)


### Prerequisites
- **Java 19**
- **JavaFX 20**
- **Maven**

### Build and Run the Application
To build and run the application, use the following command:
```sh
mvn javafx:run
