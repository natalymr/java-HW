package torrent.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection implements AutoCloseable {
    private Socket socket;

    Connection(Socket socket) {
        this.socket = socket;
    }

    InetAddress getIP() {
        return socket.getInetAddress();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}