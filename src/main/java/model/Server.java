package model;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

public class Server implements Runnable {

    private LinkedBlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;
    private List<Task> processedTasks;

    public Server() {
        tasks = new LinkedBlockingQueue<>();
        waitingPeriod = new AtomicInteger(0);
        processedTasks = new ArrayList<>();
    }

    public void addTask(Task newTask) {
        tasks.add(newTask);
        waitingPeriod.getAndAdd(newTask.getServiceTime());
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Task task;
                synchronized (this) {
                    task = tasks.peek();
                }
                if (task != null) {
                    task.setServiceTime(task.getServiceTime() - 1);
                    if (task.getServiceTime() == 0) {
                        synchronized (this) {
                            tasks.remove(task);
                            processedTasks.add(task);
                        }
                        waitingPeriod.getAndDecrement();
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.err.println("Server thread interrupted: " + e.getMessage());
        }
    }

    public Task[] getTasks() {
        return tasks.toArray(new Task[0]); //convert queue to array
    }

    public int getQueueSize() {
        return tasks.size();
    }

    public int getTotalProcessingTime() {
        return waitingPeriod.get();
    }

    public List<Task> getProcessedTasks() {
        return processedTasks;
    }
}