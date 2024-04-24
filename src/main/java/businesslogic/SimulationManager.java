package businesslogic;

import gui.SimulationFrameController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Server;
import model.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SimulationManager implements Runnable {
    private static SimulationManager instance = null;
    private BufferedWriter writer;
    private SelectionPolicy selectionPolicy = SelectionPolicy.SHORTEST_TIME; //this is a default value
    private Scheduler scheduler;
    private List<Task> generatedTasks;

    private int timeLimit;
    private int maxProcessingTime;
    private int minProcessingTime;
    private int numberOfServers;
    private int numberOfClients;
    private int minArrivalTime;
    private int maxArrivalTime;

    public void setSelectionPolicy(SelectionPolicy selectionPolicy) {
        this.selectionPolicy = selectionPolicy;
        if (scheduler != null) {
            scheduler.changeStrategy(selectionPolicy);
        }
    }

    public static synchronized SimulationManager getInstance() {
        if (instance == null) {
            instance = new SimulationManager();
        }
        return instance;
    }

    public void setNumberOfServers(int numberOfServers) {
        this.numberOfServers = numberOfServers;
    }

    public SimulationManager() {
        try {
            writer = new BufferedWriter(new FileWriter("simulation_log.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        scheduler = new Scheduler(numberOfServers);

        for (int i = 0; i < numberOfServers; i++) {
            Thread serverThread = new Thread(new Server());
            serverThread.start();
        }
        scheduler.changeStrategy(selectionPolicy);
        generateNRandomTasks();
    }


    public void initializeFrame(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/simulationFrame.fxml"));
            Parent root = loader.load();
            stage.setTitle("Simulation Frame");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateNRandomTasks() {
        generatedTasks = new ArrayList<>();

        for (int i = 0; i < numberOfClients; i++) {
            int arrivalTime = ThreadLocalRandom.current().nextInt(minArrivalTime, maxArrivalTime + 1);
            int processingTime = ThreadLocalRandom.current().nextInt(minProcessingTime, maxProcessingTime + 1);

            Task task = new Task();
            task.setArrivalTime(arrivalTime);
            task.setServiceTime(processingTime);
            task.setId(i+1);
            System.out.println("Generated task with arrival time: " + arrivalTime + " and service time: " + processingTime);

            generatedTasks.add(task);
        }
        generatedTasks.sort(Comparator.comparingInt(Task::getArrivalTime));
    }

    double sumServiceTime = 0;
    @Override
    public void run() {
        int currentTime = 0;
        int[] tasksPerHour = new int[timeLimit];

        while(currentTime < timeLimit && (!generatedTasks.isEmpty() || !scheduler.allQueuesAreEmpty())) {
            Iterator<Task> taskIterator = generatedTasks.iterator();
            while(taskIterator.hasNext()){
                Task task = taskIterator.next();
                if(task.getArrivalTime() <= currentTime) {
                    sumServiceTime+=task.getServiceTime();
                    scheduler.dispatchTask(task);
                    taskIterator.remove();
                    tasksPerHour[currentTime] += scheduler.getServers().stream().mapToInt(Server::getQueueSize).sum();
                }
            }
            logStatus(currentTime);
            currentTime++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        displayResults(currentTime, tasksPerHour);
        System.out.println("Simulation ended");
    }
    private double computeAverageWaitingTime(int currentTime, int numberOfTasksProcessed) {
        int totalWaitingTime = 0;
        for (Server server : scheduler.getServers()) {
            for (Task task : server.getProcessedTasks()) {
                totalWaitingTime += task.getWaitingTime(currentTime);
            }
        }
        return numberOfTasksProcessed > 0 ? (double) totalWaitingTime / numberOfTasksProcessed : 0;
    }

    private int computePeakHour(int[] tasksPerHour) {
        int peakHour = 0;
        for (int i = 1; i < tasksPerHour.length; i++) {
            if (tasksPerHour[i] > tasksPerHour[peakHour]) {
                peakHour = i;
            }
        }
        return peakHour;
    }

    private void displayResults(int currentTime, int[] tasksPerHour) {
        int numberOfTasksProcessed = scheduler.getServers().stream().mapToInt(server -> server.getProcessedTasks().size()).sum();
        double averageWaitingTime = computeAverageWaitingTime(currentTime, numberOfTasksProcessed);
        double averageServiceTime = sumServiceTime / numberOfClients;
        int peakHour = computePeakHour(tasksPerHour);

        try {
            writer.write(String.format("Average Waiting Time: %.2f\n", averageWaitingTime));
            writer.write(String.format("Average Service Time: %.2f\n", averageServiceTime));
            writer.write("Peak Hour: " + peakHour + "\n");
            writer.flush();
          //  writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> SimulationFrameController.getInstance().updateSimulationResults(averageWaitingTime, averageServiceTime, peakHour));
    }

    private void logStatus(int currentTime) {
        try {
            String status = "Time " + currentTime + "\n" +
                    "Waiting clients: " + generatedTasks.toString() + "\n";
            for (int i = 0; i < scheduler.getServers().size(); i++) {
                Server server = scheduler.getServers().get(i);
                status += "Queue " + (i+1) + ": " + (server.getTasks().length > 0 ? Arrays.toString(server.getTasks()) : "closed") + "\n";
            }
            status += "\n";
            writer.write(status);
            writer.flush();
            String finalStatus = status;
            Platform.runLater(() -> SimulationFrameController.getInstance().updateSimulationTextArea(finalStatus));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupSimulation(int numberOfClients, int numberOfServers, int timeLimit, int minArrivalTime, int maxArrivalTime,  int minProcessingTime, int maxProcessingTime) {
        this.numberOfClients = numberOfClients;
        this.numberOfServers = numberOfServers;
        this.timeLimit = timeLimit;
        this.minArrivalTime = minArrivalTime;
        this.maxArrivalTime = maxArrivalTime;
        this.minProcessingTime = minProcessingTime;
        this.maxProcessingTime = maxProcessingTime;

        generateNRandomTasks();
        scheduler = new Scheduler(numberOfServers);
        scheduler.changeStrategy(selectionPolicy);
    }


    public static void main(String[] args) {
        SimulationManager simulationManager = new SimulationManager();
        Thread t = new Thread(simulationManager);
        t.start();
    }


}