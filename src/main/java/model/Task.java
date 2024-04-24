package model;

public class Task {
    private int arrivalTime;
    private int serviceTime;
    private int id;


    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }
    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime=arrivalTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public void setId(int i) {
        this.id=i;
    }

    @Override
    public String toString() {
        return "(" + id + "," + arrivalTime + "," + serviceTime +")";
    }

    public int getWaitingTime(int currentTime) {
        return currentTime - arrivalTime;
    }

}
