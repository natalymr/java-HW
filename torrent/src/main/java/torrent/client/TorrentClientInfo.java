package torrent.client;

import java.net.InetAddress;

/**
 * POJO - Plain Old Java Object
 */
public class TorrentClientInfo {

    private final InetAddress IP;
    private final short       port;

    public TorrentClientInfo(InetAddress IP, short port) {
        this.IP   = IP;
        this.port = port;
    }

    public short getPort() {
        return port;
    }

    public InetAddress getIP() {
        return IP;
    }
}
