package statistics;


/* POJO class */
public class TimeStamp {

    private volatile long startRequest;
    private volatile long startSort;
    private volatile long endSort;
    private volatile long endRequest;

    public TimeStamp(long startRequest, long startSort, long endSort, long endRequest) {
        this.startRequest = startRequest;
        this.startSort    = startSort;
        this.endSort      = endSort;
        this.endRequest   = endRequest;
    }

    public TimeStamp() {}

    public long getStartRequest() {
        return startRequest;
    }

    long getStartSort() {
        return startSort;
    }

    long getEndSort() {
        return endSort;
    }

    public long getEndRequest() {
        return endRequest;
    }

    public void setStartRequest(long startRequest) {
        this.startRequest = startRequest;
    }

    public void setStartSort(long startSort) {
        this.startSort = startSort;
    }

    public void setEndSort(long endSort) {
        this.endSort = endSort;
    }

    public void setEndRequest(long endRequest) {
        this.endRequest = endRequest;
    }
}
