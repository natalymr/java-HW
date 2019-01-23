package server.threadedServer;

import network.ClientConnection;
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

    public ThreadedServer(InetAddress inetAddress, short port) throws IOException {
        super(inetAddress, port);
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
        try (ClientConnection server2client = new ClientConnection(socket)) {
            while (true) {
                long startRequest; long endRequest;
                long startSort; long endSort;


                int arraySize;
                try {
                    arraySize = server2client.getArraySize();
                    startRequest = System.currentTimeMillis();

                } catch (EOFException e) {
                    // if there is no msg from client we should stop working
                    globalStatisticsPerIteration.addNewStatisticsPerClient(statistics);
                    break;
                }
                List<Integer> array = server2client.getArray(arraySize);

                ArrayList<Integer> sortedArray = new ArrayList<>(array);

                startSort = System.currentTimeMillis();
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