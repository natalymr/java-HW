package statistics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StatisticsPerIteration {

    private final Map<Short, StatisticsPerClient> clientVSstatistics;

    public StatisticsPerIteration() {
        clientVSstatistics = new ConcurrentHashMap<>();
    }

    public synchronized void addNewStatisticsPerClient(StatisticsPerClient statisticsPerClient) {
        clientVSstatistics.put(statisticsPerClient.getId(), statisticsPerClient);
    }

    public AverageValues computeAverageTimes(int delay) {
        // 1. find start of time when all clients connected to server
        long start = findStartOfTimeWhereAllClientsConnectedToServer();

        // 2. find end of time when all clients connected to server
        long end = findEndOfTimeWhereAllClientsConnectedToServer();

        // 3. filter times & compute statistics
        int count = 0;
        long timeOne = 0; long timeTwo = 0; long timeThree = 0;
        for (Map.Entry<Short, StatisticsPerClient> client : clientVSstatistics.entrySet()) {
            // filter
            List<TimeStamp> clientsTimeStamp = client.getValue().getTimeStamps();
            List<TimeStamp> filteredTimesPerClient = clientsTimeStamp
                .stream()
                .filter((timeStamp) -> timeStamp.getStartRequest() >= start)
                .filter((timeStamp) -> timeStamp.getEndRequest() <= end)
                .collect(Collectors.toList());

            // statistic
            count += filteredTimesPerClient.size();
            for (TimeStamp stamp : filteredTimesPerClient) {
                timeOne += (stamp.getEndSort() - stamp.getStartSort());
                timeTwo += (stamp.getEndRequest() - stamp.getStartRequest());
            }
            System.out.println("filtered size = " + filteredTimesPerClient.size());

            if (filteredTimesPerClient.size() > 0) {
                timeThree += (filteredTimesPerClient.get(filteredTimesPerClient.size() - 1).getEndRequest() -
                    filteredTimesPerClient.get(0).getStartRequest()) - delay;
            }
        }

        timeOne /= (1. * count);
        timeTwo /= (1. * count);
        timeThree /= (1. * count);

        return new AverageValues(timeOne, timeTwo, timeThree);
    }

    private long findStartOfTimeWhereAllClientsConnectedToServer() {
        // compute as max
        long result = Long.MIN_VALUE;

        for (Map.Entry<Short, StatisticsPerClient> client : clientVSstatistics.entrySet()) {
            long tmp = client.getValue().getTimeStamps().get(0).getStartRequest();

            if (tmp > result)
                result = tmp;
        }

        return result;
    }

    private long findEndOfTimeWhereAllClientsConnectedToServer() {
        // compute as min
        long result = Long.MAX_VALUE;

        for (Map.Entry<Short, StatisticsPerClient> client : clientVSstatistics.entrySet()) {
            List<TimeStamp> times = client.getValue().getTimeStamps();
            long tmp = times.get(times.size() - 1).getEndRequest();

            if (result > tmp)
                result = tmp;
        }

        return result;
    }
}
