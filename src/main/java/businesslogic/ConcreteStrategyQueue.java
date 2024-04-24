package businesslogic;
import model.Server;
import java.util.List;
import model.Task;

public class ConcreteStrategyQueue implements Strategy {
    @Override
    public void addTask(List<Server> servers, Task task) {
        if (servers.isEmpty()) {
            throw new IllegalArgumentException("No servers available");
        }

        Server shortestQueueServer = servers.get(0);
        for (Server server : servers) {
            if (server.getQueueSize() < shortestQueueServer.getQueueSize()) {
                shortestQueueServer = server;
            }
        }

        shortestQueueServer.addTask(task);
    }
}
