package server.threadPoolServer;

import network.ClientConnection;
import network.Connection;
import server.ServerBase;
import statistics.StatisticsPerClient;
import statistics.StatisticsPerIteration;
import statistics.TimeStamp;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static sorting.BubbleSort.sort;

public class ThreadPoolServer extends ServerBase {
    private final ServerSocket threadPoolServerSocket;
    private ExecutorService pool;

    public ThreadPoolServer(InetAddress inetAddress, short port, int threadPoolSize) throws IOException {
        super(inetAddress, port);
        threadPoolServerSocket = new ServerSocket(port);
        pool = Executors.newFixedThreadPool(threadPoolSize);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Socket newSocket = threadPoolServerSocket.accept();
                short clientID = (short) newSocket.getPort();

                new Thread(new ThreadPoolServerRunnable(
                    newSocket,
                    pool,
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
        threadPoolServerSocket.close();
        pool.shutdown();
    }
}

class ThreadPoolServerRunnable implements Runnable {

    private final Socket socket;
    private final StatisticsPerClient statisticsPerClient;
    private final StatisticsPerIteration globalStatisticsPerIteration;
    private final ExecutorService singleThread;
    private final ExecutorService threadPoolForSorting;

    ThreadPoolServerRunnable(Socket socket, ExecutorService threadPoolForSorting,
                             StatisticsPerClient statisticsPerClient, StatisticsPerIteration globalStatistics) {
        this.socket = socket;
        this.statisticsPerClient = statisticsPerClient;
        this.globalStatisticsPerIteration = globalStatistics;
        this.threadPoolForSorting = threadPoolForSorting;
        singleThread = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        try (ClientConnection server2client = new ClientConnection(socket)) {
            while (true) {
                TimeStamp times = new TimeStamp();

                // get
                List<Integer> array;
                try {
                    times.setStartRequest(System.currentTimeMillis());

                    array = server2client.getArray();

                } catch (EOFException e) {
                    // if there is no msg from client we should stop working
                    break;
                }

                // sort
                ArrayList<Integer> sortedArray = new ArrayList<>(array);
                threadPoolForSorting.execute(() -> {
                    times.setStartSort(System.currentTimeMillis());
                    sort(sortedArray);
                    times.setEndSort(System.currentTimeMillis());

                    // send
                    if (singleThread.isShutdown()) return;
                    singleThread.execute(() -> {
                        try {
                            server2client.sendArray(sortedArray);
                        } catch (IOException e) {
                            System.out.println("can not send result to client");
                            e.printStackTrace();
                        }
                        times.setEndRequest(System.currentTimeMillis());

                        // add times to statisticsPerClient
                        statisticsPerClient.addNewStamp(times);
                    });


                });

            }

        } catch (Exception e) {
            System.out.println("ThreadedServerRunnable : Connection " + e.getMessage());
        } finally {
            globalStatisticsPerIteration.addNewStatisticsPerClient(statisticsPerClient);
            singleThread.shutdown();
        }
    }
}
