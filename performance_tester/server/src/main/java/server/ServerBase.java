package server;

import statistics.StatisticsResultPerIteration;
import statistics.TestingParameters;
import statistics.StatisticsPerIteration;

import java.io.IOException;
import java.net.InetAddress;

public abstract class ServerBase implements Runnable {
    // host, port
    // start
    // stop
    // statistic

    private final InetAddress inetAddress;
    private final short       port;
    protected StatisticsPerIteration statisticsPerIteration;
    private final TestingParameters testingParameters;

    protected ServerBase(InetAddress inetAddress, short port, TestingParameters testingParameters) {
        this.inetAddress = inetAddress;
        this.port = port;
        this.statisticsPerIteration = new StatisticsPerIteration();
        this.testingParameters = testingParameters;
    }

    public abstract void interrupt() throws IOException;

    public void clearStatisticsPerIteration() {
        statisticsPerIteration = new StatisticsPerIteration();
    }

    public StatisticsResultPerIteration getStatisticsResultPerIteration(int varyingParameterCurrentValue, int delay) {
        return new StatisticsResultPerIteration(
            varyingParameterCurrentValue,
            statisticsPerIteration.computeAverageTimes(delay));
    }

}
