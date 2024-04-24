package businesslogic;
import model.Server;
import java.util.List;
import model.Task;

public class ConcreteStrategyTime implements Strategy {
   @Override
    public void addTask(List<Server> servers, Task task) {
        Server shortestTimeServer = servers.get(0);
        int shortestTime = shortestTimeServer.getTotalProcessingTime();
        for (Server server : servers) {
            int totalTime = server.getTotalProcessingTime();
            if (totalTime < shortestTime) {
                shortestTimeServer = server;
                shortestTime = totalTime;
            }
        }
        System.out.println("Adding task: " + task + " to server with shortest time");
        shortestTimeServer.addTask(task);
    }
}