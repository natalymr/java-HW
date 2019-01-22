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
        timeStamps.add(new TimeStamp());
        return timeStamps.get(timeStamps.size() - 1);
    }
}


