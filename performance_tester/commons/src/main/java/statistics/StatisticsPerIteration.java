package statistics;

import java.util.*;
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
        int correctClientsCount = 0;
        double timeOne = 0;
        double timeTwo = 0;
        List<Double> timeThrees = new ArrayList<>();
        for (Map.Entry<Short, StatisticsPerClient> client : clientVSstatistics.entrySet()) {
            // filter
            List<TimeStamp> clientsTimeStamp = client.getValue().getTimeStamps();
            List<TimeStamp> filteredTimesPerClient = clientsTimeStamp
                .stream()
                .filter(timeStamp -> timeStamp.getStartRequest() >= start)
                .filter(timeStamp -> timeStamp.getEndRequest() <= end)
                .collect(Collectors.toList());

            // statistic
            count += filteredTimesPerClient.size();
            for (TimeStamp stamp : filteredTimesPerClient) {
                timeOne += (stamp.getEndSort() - stamp.getStartSort());
                timeTwo += (stamp.getEndRequest() - stamp.getStartRequest());
            }

            if (filteredTimesPerClient.size() > 0) {
                correctClientsCount += 1;

                double timeThree = (filteredTimesPerClient.get(filteredTimesPerClient.size() - 1).getEndRequest() -
                    filteredTimesPerClient.get(0).getStartRequest());

                timeThrees.add((timeThree / filteredTimesPerClient.size()));
            }

        }

        double averageClientTime = clientVSstatistics.values().stream()
            .map(statisticsPerClient -> {
                List<TimeStamp> correctTimeStamps = statisticsPerClient.getTimeStamps().stream()
                    .filter(timeStamp -> timeStamp.getStartRequest() >= start)
                    .filter(timeStamp -> timeStamp.getEndRequest() <= end)
                    .collect(Collectors.toList());

                if (correctTimeStamps.isEmpty()) {
                    return null;
                }

                TimeStamp firstCorrect = correctTimeStamps.get(0);
                TimeStamp lastCorrect = correctTimeStamps.get(correctTimeStamps.size() - 1);

                double totalClientTime = lastCorrect.getEndRequest() - firstCorrect.getStartRequest();
                return totalClientTime / correctTimeStamps.size() - delay;
            })
            .filter(Objects::nonNull)
            .mapToDouble(avg -> avg)
            .average()
            .orElse(0.0);

        timeOne /= (1. * count);
        timeTwo /= (1. * count);
        double timeThree = 0;
        for (Double times : timeThrees) {
            timeThree += times;
        }

        return new AverageValues(timeOne, timeTwo, averageClientTime/*(timeThree / correctClientsCount)*/);
    }

    private long findStartOfTimeWhereAllClientsConnectedToServer() {
        // compute as max
        long result = Long.MIN_VALUE;

        for (Map.Entry<Short, StatisticsPerClient> clientAndStatistics : clientVSstatistics.entrySet()) {
            if (clientAndStatistics.getValue().getTimeStamps().isEmpty()) {
                continue;
            }
            long tmp = clientAndStatistics.getValue().getTimeStamps().get(0).getStartRequest();

            if (tmp > result)
                result = tmp;
        }

        return result;
    }

    private long findEndOfTimeWhereAllClientsConnectedToServer() {
        // compute as min
        long result = Long.MAX_VALUE;

        for (Map.Entry<Short, StatisticsPerClient> clientAndStatistic : clientVSstatistics.entrySet()) {
            List<TimeStamp> times = clientAndStatistic.getValue().getTimeStamps();
            if (times.isEmpty()) {
                continue;
            }

            long tmp = times.get(times.size() - 1).getEndRequest();

            if (result > tmp)
                result = tmp;
        }

        return result;
    }
}
