package statistics;

import java.io.Serializable;

/* POJO */
public class AverageValues implements Serializable {
    private double sortingTime;
    private double requestingTime;
    private double timeForAllRequests;

    AverageValues(double sortingTime, double requestingTime, double timeForAllRequests) {
        this.sortingTime = sortingTime;
        this.requestingTime = requestingTime;
        this.timeForAllRequests = timeForAllRequests;
    }

    public double getSortingTime() {
        return sortingTime;
    }

    public double getRequestingTime() {
        return requestingTime;
    }

    public double getTimeForAllRequests() {
        return timeForAllRequests;
    }
}
