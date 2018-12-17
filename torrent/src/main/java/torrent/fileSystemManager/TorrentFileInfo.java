package torrent.fileSystemManager;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TorrentFileInfo fileInfo = (TorrentFileInfo) o;
        return id == fileInfo.id &&
                size == fileInfo.size &&
                Objects.equals(name, fileInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, size);
    }
}
