package network.client;

import network.ClientConnection;
import network.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


public class Client implements Runnable {

    private final int x;
    private final int n;
    private final long delay;

    private final InetAddress inetAddress;
    private final short port;
    private volatile ClientConnection client2server;

    private final List<List<Integer>> data = new ArrayList<>();

    Client(int x, int n, long delay, InetAddress inetAddress, short port) {
        this.x = x;
        this.n = n;
        this.delay = delay;

        this.inetAddress = inetAddress;
        this.port = port;

        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < x; i++) {
            int[] randomArray = random.ints(0, 100).limit(n).toArray();
            data.add(Arrays.stream(randomArray).boxed().collect(Collectors.toList()));
        }
    }

    @Override
    public void run() {
        try (ClientConnection client2server  = new ClientConnection(new Socket(inetAddress, port))) {
            this.client2server = client2server;

            System.out.println("Client # " + Thread.currentThread().getId());
            for (int i = 0; i < x; i++) {
                client2server.sendArray(data.get(i));
                List<Integer> integers = client2server.getArray();

                //System.out.println("unsorted " + data.get(i) + " sorted " + integers);

                System.out.println("send data");

                Thread.sleep(delay);
            }

        } catch (IOException e) {
            System.out.println("Failed in client while connect to server " + e.getMessage());
        } catch (InterruptedException ignored) {}
    }

    public void stop() throws IOException {
        if (client2server != null) {
            client2server.close();
        }
    }
}
