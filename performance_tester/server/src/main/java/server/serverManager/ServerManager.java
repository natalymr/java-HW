package server.serverManager;

import network.Connection;
import server.ServerBase;
import server.threadedServer.ThreadedServer;
import statistics.StatisticsResultPerIteration;
import statistics.TestingParameters;
import statistics.VaryingParameter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ServerManager {

    public static final byte RUN_SERVER      = 1;
    public static final byte SEND_RESULTS    = 2;
    public static final byte CLEAR_CUR_STATS = 3;
    public static final byte SAVE_CUR_STATS  = 4;

    public static final short SERVER_MANAGER_PORT = 6666;
    private static ServerBase server;

    public static void main(String[] args) {

        System.out.println("SM hw");

        List<StatisticsResultPerIteration> statisticsResults = new ArrayList<>();
        TestingParameters testingParameters = null;

        // all in one; 4 commands
        try (ServerSocket serverManager = new ServerSocket(SERVER_MANAGER_PORT)) {
            while (true) {
                try (Connection guiORclientM2serverM = new Connection(serverManager.accept())) {
                    switch (guiORclientM2serverM.readRequestByte()) {

                        // listen GUI (get parameters about testing and run server with needed type)
                        case RUN_SERVER: {
                            System.out.println("SM: request 1");

                            // get testingParameters about testing
                            String serverType = guiORclientM2serverM.getServerType();
                            InetAddress inetAddress = guiORclientM2serverM.getInetAddress();
                            Short port = guiORclientM2serverM.getPort();
                            testingParameters = guiORclientM2serverM.getParameters();

                            // run server
                            switch (serverType) {
                                case "first": {
                                    System.out.println("first server");
                                    server = new ThreadedServer(inetAddress, port, testingParameters);
                                    break;
                                }
                                case "second": {
                                    break;
                                }
                                case "third": {
                                    break;
                                }
                            }

                            new Thread(server).start();

                            guiORclientM2serverM.sendServerStatus(true);
                            break;
                        }
                        // listen GUI (get request about statistics results, send it)
                        case SEND_RESULTS: {
                            System.out.println("SM: request 2");
                            guiORclientM2serverM.sendStatisticsResults(statisticsResults);

                            // clear current result
                            statisticsResults.clear();

                            // stop server
                            if (server != null) {
                                server.interrupt();
                            }

                            break;
                        }
                        // lister client manager (clear statistics)
                        case CLEAR_CUR_STATS: {
                            System.out.println("SM: request 3");
                            server.clearStatisticsPerIteration();
                            break;
                        }
                        // lister client manager (compute statistics)
                        case SAVE_CUR_STATS: {
                            System.out.println("SM: request 4");
                            // get all needed data
                            int varyingParameterCurrentValue = guiORclientM2serverM.getVaryingParameterCurrentValue();
                            if (testingParameters == null) break;
                            int delay = (testingParameters.getVaryingParameter() == VaryingParameter.delay)
                                ? varyingParameterCurrentValue
                                : testingParameters.getDelay();

                            // compute statistics for this iteration and save it to statResults
                            statisticsResults.add(server.getStatisticsResultPerIteration(
                                varyingParameterCurrentValue,
                                delay));
                            break;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("SM: can not read input data " + e.getCause());
                }
            }
        } catch (IOException e) {
            System.out.println("connection to ServerManager failed, because " + e.getMessage());;
        }
    }
}
