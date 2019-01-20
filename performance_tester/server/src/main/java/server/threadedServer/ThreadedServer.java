package server.threadedServer;

import network.Connection;
import server.ServerBase;
import statistics.StatisticsPerClient;
import statistics.StatisticsPerIteration;
import statistics.TestingParameters;
import statistics.TimeStamp;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static sorting.BubbleSort.sort;

public class ThreadedServer extends ServerBase {

    private final ServerSocket threadedServerSocket;

    public ThreadedServer(InetAddress inetAddress, short port, TestingParameters testingParameters) throws IOException {
        super(inetAddress, port, testingParameters);
        threadedServerSocket = new ServerSocket(port);
    }


    public void run() {
        while (!Thread.interrupted()) {
            try {
                Socket newSocket = threadedServerSocket.accept();
                short clientID = (short) newSocket.getPort();

                new Thread(new ThreadedServerRunnable(
                        newSocket,
                        new StatisticsPerClient(clientID),
                        statisticsPerIteration
                )).start();

            } catch (IOException ignored) {
                break;
            }
        }
    }

    @Override
    public void interrupt() throws IOException {
        threadedServerSocket.close();
    }
}

class ThreadedServerRunnable implements Runnable {

    private final Socket socket;
    private final StatisticsPerClient statistics;
    private final StatisticsPerIteration globalStatisticsPerIteration;

    ThreadedServerRunnable(Socket socket, StatisticsPerClient statistics, StatisticsPerIteration globalStatisticsPerIteration) {
        this.socket = socket;
        this.statistics = statistics;
        this.globalStatisticsPerIteration = globalStatisticsPerIteration;
    }

    @Override
    public void run() {
        try (Connection server2client = new Connection(socket)) {
            while (true) {
                System.out.println("start to sort");
                long startRequest; long endRequest;
                long startSort; long endSort;

                List<Integer> array;

                try {
                    startRequest = System.currentTimeMillis();
                    array = server2client.getArray();

                } catch (EOFException e) {
                    // if there is no msg from client we should stop working
                    globalStatisticsPerIteration.addNewStatisticsPerClient(statistics);
                    break;
                }

                System.out.println("get data");
                startSort = System.currentTimeMillis();

                ArrayList<Integer> sortedArray = new ArrayList<>(array);
                sort(sortedArray);
                endSort = System.currentTimeMillis();

                server2client.sendArray(sortedArray);

                endRequest = System.currentTimeMillis();

                // statisticsPerIteration
                statistics.addNewStamp(new TimeStamp(
                        startRequest,
                        startSort,
                        endSort,
                        endRequest)
                );
            }

        } catch (IOException e) {
            System.out.println("ThreadedServerRunnable : Connection " + e.getMessage());
        }
    }
}