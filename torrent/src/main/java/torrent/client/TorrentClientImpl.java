package torrent.client;

import torrent.fileSystemManager.TorrentFileInfo;
import torrent.fileSystemManager.TorrentClientFileSystemManager;
import torrent.fileSystemManager.TorrentClientFile;
import torrent.fileSystemManager.TorrentFilePart;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class TorrentClientImpl implements TorrentClient {

    private Map<Integer, TorrentClientFile> storedFiles;
    private TorrentClientFileSystemManager  fileSystemManager;

    public TorrentClientImpl() throws FileNotFoundException {
        fileSystemManager = new TorrentClientFileSystemManager();
        storedFiles = fileSystemManager.getStoredFiles();
    }

    public synchronized int getFilesCount() {
        return storedFiles.size();
    }

    public synchronized Set<Integer> getFilesID() {
        return storedFiles.keySet();
    }

    public void addListResultToTorrentFileSystem(Set<TorrentFileInfo> fileInfos) throws IOException {
        for (TorrentFileInfo fileInfo : fileInfos) {
            fileSystemManager.addNewInfoFile(fileInfo);
        }
    }

    public void createTorrentFile(Path pwd, int id, String name, long size) throws IOException {
        TorrentClientFile torrentFile = fileSystemManager.addNewTorrentFile(pwd, id, name, size);

        storedFiles.put(id, torrentFile);
    }

    @Override
    public synchronized List<Integer> stat(int id) {
        return storedFiles.get(id).getPartsNumber();
    }

    @Override
    public synchronized TorrentFilePart get(int id, int part) {
        return storedFiles.get(id).getTorrentFilePart(part);
    }
}
