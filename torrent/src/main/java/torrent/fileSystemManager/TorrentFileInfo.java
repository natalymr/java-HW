package torrent.fileSystemManager;

public class TorrentFileInfo {

    private final int    id;
    private final String name;
    private final long   size;

    public TorrentFileInfo(int id, String name, long size) {
        this.id   = id;
        this.name = name;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
