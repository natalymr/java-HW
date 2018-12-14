package torrent.network;

import torrent.fileSystemManager.TorrentFileInfo;
import torrent.client.TorrentClientInfo;
import torrent.tracker.TorrentTrackerImpl;

import java.io.*;
import java.net.ServerSocket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tracker {

    private static final int  TRACKER_PORT  = 8081;
    private static final long TIME_TO_SLEEP = 60_000;

    static public void main(String[] args) throws FileNotFoundException {
        TorrentTrackerImpl tracker = new TorrentTrackerImpl();

        ExecutorService executor = Executors.newCachedThreadPool();

        executor.execute(() -> {
            try (ServerSocket trackerSocket = new ServerSocket(TRACKER_PORT)) {
                while (true) {
                    try (Tracker2ClientConnection client = new Tracker2ClientConnection(trackerSocket.accept())) {

                        switch (client.readRequestType()) {
                            case 1:
                                //System.out.println("List command");
                                Set<TorrentFileInfo> fileInfos = tracker.list();

                                client.writeFileInfos(fileInfos);

                                break;

                            case 2:

                                //System.out.println("Upload command");
                                FileUpload fileUpload = client.readFileUpload();

                                int newFileID = tracker.upload(
                                        fileUpload.getFileName(),
                                        fileUpload.getFileSize()
                                );

                                client.writeFileID(newFileID);

                                break;

                            case 3:
                                //System.out.println("Sources command");
                                int fileID = client.readFileId();

                                List<TorrentClientInfo> clientInfos = tracker.sources(fileID);

                                client.writeClientsInfos(clientInfos);

                                break;
                            case 4:
                                //System.out.println("Update command");
                                ClientFilesUpdate clientFilesUpdate = client.readClientFilesUpdate();

                                TorrentClientInfo clientInfo = new TorrentClientInfo(
                                        client.getIP(),
                                        clientFilesUpdate.getClientPort()
                                );

                                boolean status = tracker.update(
                                        clientInfo,
                                        clientFilesUpdate.getFilesID()
                                );

                                client.writeUpdateStatus(status);
                                tracker.addClient(clientInfo);

                                break;
                            default:
                                System.out.println("tracker: undefined request");
                        }
                    }
                }
            } catch (IOException ignored) {}
        });

        executor.execute(() -> {
            while (!Thread.interrupted()) {
                tracker.checkAllClientsForAvailability();

                try {
                    Thread.sleep(TIME_TO_SLEEP);
                } catch (InterruptedException ignored) {}
            }
        });
    }
}
