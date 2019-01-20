package statistics;

import java.util.ArrayList;
import java.util.List;

public class StatisticsPerClient {

    private final short id;
    private final List<TimeStamp> timeStamps;

    public StatisticsPerClient(short id) {
        this.id = id;
        timeStamps = new ArrayList<>();
    }

    short getId() {
        return id;
    }

    public void addNewStamp(TimeStamp timeStamp) {
        timeStamps.add(timeStamp);
    }

    public List<TimeStamp> getTimeStamps() {
        return timeStamps;
    }
}


