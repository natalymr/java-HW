package torrent.tracker;

import torrent.client.TorrentClientInfo;
import torrent.fileSystemManager.TorrentFileInfo;
import torrent.fileSystemManager.TorrentTrackerFileSystemManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class TorrentTrackerImpl implements TorrentTracker {

    private final TorrentTrackerFileSystemManager         fileSystemManager;
    private static final long                             TIME_THRESHOLD = 5 * 60 * 1000;
    private Map<TorrentFileInfo, List<TorrentClientInfo>> filesVSclients;
    private Set<TorrentClientInfo>                        availableClients;
    private Map<TorrentClientInfo, Long>                  clientVSlastTimePing;
    private int futureID;

    public TorrentTrackerImpl() throws FileNotFoundException {
        fileSystemManager = new TorrentTrackerFileSystemManager();
        // restore list of files
        List<TorrentFileInfo> infoFiles = fileSystemManager.restoreAllFilesInfo();
        filesVSclients = new HashMap<>();
        if (infoFiles != null) {
            for (TorrentFileInfo fileInfo : infoFiles) {
                filesVSclients.put(fileInfo, new ArrayList<>());
            }
        }

        availableClients = new HashSet<>();
        clientVSlastTimePing = new HashMap<>();
        futureID = fileSystemManager.getLastIDNumber() + 1;
    }

    public void addClient(TorrentClientInfo client) {
        availableClients.add(client);
        clientVSlastTimePing.put(client, System.currentTimeMillis());
    }

    private void deleteFromAvailableClients(TorrentClientInfo client) {
        availableClients.remove(client);
    }

    private void checkClientForAvailability(TorrentClientInfo client, long currentTimeMillis) {
        if (currentTimeMillis - clientVSlastTimePing.get(client) > TIME_THRESHOLD) {
            deleteFromAvailableClients(client);
        }
    }

    public void checkAllClientsForAvailability() {
        for (TorrentClientInfo client : availableClients) {
            checkClientForAvailability(client, System.currentTimeMillis());
        }
    }

    public boolean isCorrectID(int id) {
        return id >= 0 && id < futureID;
    }

    @Override
    public Set<TorrentFileInfo> list() {
        return filesVSclients.keySet();
    }

    @Override
    public int upload(String newFileName, long newFileSize) throws IOException {
        int result = futureID;

        TorrentFileInfo fileInfo = new TorrentFileInfo(futureID, newFileName, newFileSize);

        filesVSclients.put(fileInfo, new ArrayList<>());
        futureID++;

        fileSystemManager.addNewInfoFile(fileInfo);

        return result;
    }

    @Override
    public List<TorrentClientInfo> sources(int id) {
        Set<TorrentFileInfo> files = filesVSclients.keySet();
        System.out.println("sources len " + files.size());
        for (TorrentFileInfo file : files) {
            System.out.println(file.getName());
        }
        List<TorrentClientInfo> result = new ArrayList<>();

        for (TorrentFileInfo file : files) {
            if (file.getId() == id) {

                Set<TorrentClientInfo> allClients = new HashSet<>(filesVSclients.get(file));
                for (TorrentClientInfo client : allClients) {
                    if (availableClients.contains(client)) {
                        System.out.println("file " + file.getName() + " is available on client " + client.getPort());
                        result.add(client);
                    }
                }

                return result;
            }
        }

        return null;
    }

    @Override
    public boolean update(TorrentClientInfo clientInfo, List<Integer> fileIDs) {
        for (Integer id : fileIDs) {
            System.out.printf("update: client port = %d; id = %d \n", clientInfo.getPort(), id);
        }

        Set<TorrentFileInfo> files = filesVSclients.keySet();

        Map<Integer, TorrentFileInfo> idVSTorrentFileInfo = new HashMap<>();
        for (TorrentFileInfo file : files) {
            idVSTorrentFileInfo.put(file.getId(), file);
        }

        for (Integer fileID : fileIDs) {
            TorrentFileInfo fileInfo = idVSTorrentFileInfo.get(fileID);

            if (fileInfo == null) { // tracker does not know about this file
                return false;
            }

            List<TorrentClientInfo> clients = filesVSclients.get(fileInfo);
            if (!clients.contains(clientInfo)) {
                filesVSclients.get(fileInfo).add(clientInfo);
            }

        }

        return true;
    }
}
