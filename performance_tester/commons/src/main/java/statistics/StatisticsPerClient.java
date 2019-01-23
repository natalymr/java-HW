package statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsPerClient {

    private final short id;
    private final List<TimeStamp> timeStamps;

    public StatisticsPerClient(short id) {
        this.id = id;
        timeStamps = Collections.synchronizedList(new ArrayList<>());
    }

    short getId() {
        return id;
    }

    public void addNewStamp(TimeStamp timeStamp) {
        timeStamps.add(timeStamp);
    }

    List<TimeStamp> getTimeStamps() {
        return timeStamps;
    }

    public TimeStamp getLast() {
        if (timeStamps.isEmpty()) {
            TimeStamp newTimeStamp = new TimeStamp();
            timeStamps.add(newTimeStamp);
            return newTimeStamp;
        }

        return timeStamps.get(timeStamps.size() - 1);
    }

    public boolean checkLastIsNotCompleted() {
        if (timeStamps.isEmpty()) {
            return true;
        }

        TimeStamp last = timeStamps.get(timeStamps.size() - 1);

        if (last.getStartRequest() != 0 && last.getStartSort() == 0) {
            return false;
        }

        if (last.getStartRequest() == 0 &&
            last.getStartSort() == 0 &&
            last.getEndSort() == 0 &&
            last.getEndRequest() == 0) {
            return false;
        }

        return true;
    }
}


