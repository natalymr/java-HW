package torrent.tracker;

import torrent.client.TorrentClientInfo;
import torrent.fileSystemManager.TorrentFileInfo;
import torrent.fileSystemManager.TorrentTrackerFileSystemManager;

import java.io.FileNotFoundException;
import java.util.*;

public class TorrentTrackerImpl implements TorrentTracker {

    private static final long                             TIME_THRESHOLD = 5 * 60 * 1000;
    private Map<TorrentFileInfo, List<TorrentClientInfo>> filesVSclients;
    private Set<TorrentClientInfo>                        availableClients;
    private Map<TorrentClientInfo, Long>                  clientVSlastTimePing;
    private int futureID;

    public TorrentTrackerImpl() throws FileNotFoundException {
        TorrentTrackerFileSystemManager fileSystemManager = new TorrentTrackerFileSystemManager();
        // restore list of files
        List<TorrentFileInfo> infoFiles = fileSystemManager.restoreAllFilesInfo();
        filesVSclients   = new HashMap<>();
        if (infoFiles != null) {
            for (TorrentFileInfo fileInfo : infoFiles) {
                filesVSclients.put(fileInfo, new ArrayList<>());
            }
        }

        availableClients = new HashSet<>();
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

    public boolean isAvailable(TorrentClientInfo client) {
        return availableClients.contains(client);
    }

    @Override
    public Set<TorrentFileInfo> list() {
        return filesVSclients.keySet();
    }

    @Override
    public int upload(String newFileName, long newFileSize) {
        int result = futureID;

        filesVSclients.put(new TorrentFileInfo(futureID, newFileName, newFileSize), new ArrayList<>());
        futureID++;

        return result;
    }

    @Override
    public List<TorrentClientInfo> sources(int id) {
        Set<TorrentFileInfo> files = filesVSclients.keySet();
        List<TorrentClientInfo> result = new ArrayList<>();

        for (TorrentFileInfo file : files) {
            if (file.getId() == id) {

                List<TorrentClientInfo> allClients = filesVSclients.get(file);
                for (TorrentClientInfo client : allClients) {
                    if (availableClients.contains(client)) {
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
