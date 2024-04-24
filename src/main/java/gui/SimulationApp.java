package gui;

import businesslogic.SimulationManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class SimulationApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SimulationManager simulationManager = new SimulationManager();
        simulationManager.initializeFrame(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}