package statistics;


/* POJO class */
public class TimeStamp {

    private final long startRequest;
    private final long startSort;
    private final long endSort;
    private final long endRequest;

    public TimeStamp(long startRequest, long startSort, long endSort, long endRequest) {
        this.startRequest = startRequest;
        this.startSort    = startSort;
        this.endSort      = endSort;
        this.endRequest   = endRequest;
    }

    long getStartRequest() {
        return startRequest;
    }

    long getStartSort() {
        return startSort;
    }

    long getEndSort() {
        return endSort;
    }

    long getEndRequest() {
        return endRequest;
    }
}
