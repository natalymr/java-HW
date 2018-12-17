package torrent.client;

import java.net.InetAddress;
import java.util.Objects;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TorrentClientInfo that = (TorrentClientInfo) o;
        return port == that.port &&
                Objects.equals(IP, that.IP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IP, port);
    }
}
