package gui;


import businesslogic.SelectionPolicy;
import businesslogic.SimulationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class SimulationFrameController {

    @FXML
    private Label resultsLabel;
    @FXML
    private ComboBox<String> chooseStrategyComboBox;
    private final ObservableList<String> strategies = FXCollections.observableArrayList("SHORTEST_QUEUE", "SHORTEST_TIME");
    @FXML
    private TextArea simulationTextArea;
    @FXML
    private TextField clientTextField;
    @FXML
    private TextField queueTextField;
    @FXML
    private TextField simulationTextField;
    @FXML
    private TextField minArrivalTimeTextField;
    @FXML
    private TextField maxArrivalTimeTextField;
    @FXML
    private TextField minServiceTimeTextField;
    @FXML
    private TextField maxServiceTimeTextField;
    @FXML
    private Label errorLabel;
    private static SimulationFrameController instance;

    public static SimulationFrameController getInstance() {
        return instance;
    }

    public void updateSimulationTextArea(String status) {
        simulationTextArea.appendText(status);
    }

    @FXML
    public void resetButtonOnAction() {
        simulationTextArea.clear();
        resultsLabel.setText("");
        errorLabel.setText("");
        queueTextField.clear();
        clientTextField.clear();
        simulationTextField.clear();
        minArrivalTimeTextField.clear();
        maxArrivalTimeTextField.clear();
        minServiceTimeTextField.clear();
        maxServiceTimeTextField.clear();
    }

    public void initialize() {
        instance = this;
        chooseStrategyComboBox.setItems(strategies);
        chooseStrategyComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            switch (newValue) {
                case "SHORTEST_QUEUE":
                    SimulationManager.getInstance().setSelectionPolicy(SelectionPolicy.SHORTEST_QUEUE);
                    break;
                case "SHORTEST_TIME":
                    SimulationManager.getInstance().setSelectionPolicy(SelectionPolicy.SHORTEST_TIME);
                    break;
            }
        });
    }

    @FXML
    public void validateInputButtonOnAction() {
        int numberOfClients = getClients();
        int numberOfQueues = getQueues();
        int simulationInterval = getSimulationTime();
        int minArrivalTime = getMinArrivalTime();
        int maxArrivalTime = getMaxArrivalTime();
        int minServiceTime = getMinServiceTime();
        int maxServiceTime = getMaxServiceTime();

        if (numberOfClients == 0 || numberOfQueues == 0 || simulationInterval == 0 ||
                minArrivalTime == 0 || maxArrivalTime == 0 || minServiceTime == 0 || maxServiceTime == 0) {
            errorLabel.setText("All fields are required");
            return;
        }

        if (numberOfClients < 0 || numberOfQueues < 0 || simulationInterval < 0 ||
                minArrivalTime < 0 || maxArrivalTime < 0 || minServiceTime < 0 || maxServiceTime < 0) {
            errorLabel.setText("All fields must be positive");
            return;
        }

        if (minArrivalTime >= maxArrivalTime || minServiceTime >= maxServiceTime) {
            errorLabel.setText("Minimum time must be less than maximum time");
            return;
        }

        if (maxArrivalTime > simulationInterval || maxServiceTime > simulationInterval) {
            errorLabel.setText("Maximum time cannot exceed simulation interval");
            return;
        }

        errorLabel.setText("");
        resultsLabel.setText("Validation successful!");
    }

    @FXML
    public void startSimulationButtonOnAction() {
        validateInputButtonOnAction();
        if (!errorLabel.getText().isEmpty()) {
            return;
        }
        if (chooseStrategyComboBox.getValue() == null) {
            errorLabel.setText("Please select a strategy");
            return;
        }
        int numberOfClients = getClients();
        int numberOfQueues = getQueues();
        int simulationInterval = getSimulationTime();
        int minArrivalTime = getMinArrivalTime();
        int maxArrivalTime = getMaxArrivalTime();
        int minServiceTime = getMinServiceTime();
        int maxServiceTime = getMaxServiceTime();

        if (numberOfClients == 0 || numberOfQueues == 0 || simulationInterval == 0 ||
                minArrivalTime == 0 || maxArrivalTime == 0 || minServiceTime == 0 || maxServiceTime == 0) {
            errorLabel.setText("All fields are required");
            return;
        }
        SimulationManager simulationManager = SimulationManager.getInstance();
        simulationManager.setNumberOfServers(getNumberOfServers());
        simulationManager.setupSimulation(numberOfClients, numberOfQueues, simulationInterval,
                minArrivalTime, maxArrivalTime, minServiceTime, maxServiceTime);
        Thread simulationThread = new Thread(simulationManager);
        simulationThread.start();
        errorLabel.setText("Simulation started");
    }

    public int getQueues() {
        String text = queueTextField.getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    @FXML
    public void updateSimulationResults(double averageWaitingTime, double averageServiceTime, int peakHour) {
        resultsLabel.setText(String.format("Avg waiting time: %.2f, Avg service time: %.2f, Peak hour: %d",
                averageWaitingTime, averageServiceTime, peakHour));
    }
    public int getClients() {
        String text = clientTextField.getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public int getNumberOfServers() {
        String text = queueTextField.getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public int getSimulationTime(){
        String text = simulationTextField.getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public int getMinArrivalTime() {
        String text = minArrivalTimeTextField.getText().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }


    public int getMaxArrivalTime() {
        String text = maxArrivalTimeTextField.getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public int getMinServiceTime() {
        String text = minServiceTimeTextField.getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public int getMaxServiceTime() {
        String text = maxServiceTimeTextField.getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

}
