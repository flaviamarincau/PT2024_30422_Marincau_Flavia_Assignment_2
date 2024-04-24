package businesslogic;

import model.Server;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private static List<Server> servers;
    private List<Thread> serverThreads;
    private Strategy strategy;
    private List<Task> tasks;

    public Scheduler(int maxNumberOfServers) {
        servers = new ArrayList<>();
        serverThreads = new ArrayList<>();
        tasks = new ArrayList<>();
        for (int i = 0; i < maxNumberOfServers; i++) {
            Server server = new Server();
            servers.add(server);
            Thread serverThread = new Thread(server);
            serverThreads.add(serverThread);
            serverThread.start();
        }
    }
    public void changeStrategy(SelectionPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Policy cannot be null");
        }
        switch (policy) {
            case SHORTEST_QUEUE:
                strategy = new ConcreteStrategyQueue();
                break;
            case SHORTEST_TIME:
                strategy = new ConcreteStrategyTime();
                break;
            default:
                throw new IllegalArgumentException("Unsupported policy: " + policy);
        }
    }

    public synchronized void dispatchTask(Task task) {
        strategy.addTask(servers, task);
        tasks.remove(task);
    }

    public synchronized List<Server> getServers() {
        return servers;
    }

    public boolean isEmpty() {
        for (Server server : servers) {
            if (server.getQueueSize() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean allQueuesAreEmpty() {
        for (Server server : servers) {
            if (server.getQueueSize() > 0) {
                return false;
            }
        }
        return tasks.isEmpty();
    }
}