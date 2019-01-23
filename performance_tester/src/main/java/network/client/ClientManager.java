package network.client;

import network.Connection;
import statistics.ParametersPerIteration;
import statistics.TestingParameters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static server.ServerManagerPort.SERVER_MANAGER_PORT;
import static server.serverManager.ServerManager.*;

public class ClientManager {
    private final InetAddress inetAddress;
    private final short port;
    private final TestingParameters testingParameters;
    private final ExecutorService executor;

    public ClientManager(TestingParameters testingParameters, InetAddress inetAddress, short port) {
        this.inetAddress = inetAddress;
        this.port = port;
        this.testingParameters = testingParameters;
        this.executor = Executors.newCachedThreadPool();
    }

    public void runAllClients() throws IOException {
        System.out.println("CM: run all clients");

        Iterator<ParametersPerIteration> parametersPerIterationIterator = testingParameters.iterator();

        while (parametersPerIterationIterator.hasNext()) {

            ParametersPerIteration currentIterationParameters = parametersPerIterationIterator.next();
            System.out.println("new iteration " + currentIterationParameters.getVaryingParameterCurrentValue());

            // connect to server manager; new iteration
            try (Connection clientM2serverM = new Connection(new Socket(inetAddress, SERVER_MANAGER_PORT))) {

                clientM2serverM.sendRequestByte(CLEAR_CUR_STATS);

            }

            List<Client> clients = Stream.generate(() ->
                new Client(
                    testingParameters.getX(),
                    currentIterationParameters.getN(),
                    currentIterationParameters.getDelay(),
                    inetAddress,
                    port
                ))
                .limit(currentIterationParameters.getM())
                .collect(Collectors.toList());

            List<CompletableFuture<Void>> clientsFutures = clients
                .stream()
                .map((client) -> CompletableFuture.runAsync(client, executor))
                .collect(Collectors.toList());

            CompletableFuture<Object> firstCompletedClient = CompletableFuture
                .anyOf(clientsFutures.toArray(new CompletableFuture[0]));

            try {
                firstCompletedClient.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            for (CompletableFuture<Void> clientFuture : clientsFutures) {
                try {
                    clientFuture.cancel(true);
                } catch (CancellationException ignored) {}
            }

            for (Client client : clients) {
                try {
                    client.stop();
                } catch (IOException ignored) {}
            }

            for (CompletableFuture<Void> clientFuture : clientsFutures) {
                try {
                    clientFuture.get();
                } catch (InterruptedException | ExecutionException | CancellationException ignored) {}
            }

            // connect to server manager; save statistics
            try (Connection clientM2serverM = new Connection(new Socket(inetAddress, SERVER_MANAGER_PORT))) {

                clientM2serverM.sendRequestByte(SAVE_CUR_STATS);
                clientM2serverM.sendVaryingParameterValue(currentIterationParameters.getVaryingParameterCurrentValue());

            }
        }

    }

    public void shutdown() {
        executor.shutdown();
    }
}