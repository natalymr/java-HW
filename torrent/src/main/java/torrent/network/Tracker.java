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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Tracker {

    private static final int  TRACKER_PORT  = 8081;
    private static final long TIME_TO_SLEEP = 60_000;
    private static final byte LIST          = 1;
    private static final byte UPLOAD        = 2;
    private static final byte SOURCES       = 3;
    private static final byte UPDATE        = 4;

    static public void main(String[] args) throws FileNotFoundException {
        TorrentTrackerImpl tracker = new TorrentTrackerImpl();

        ExecutorService executor = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        executor.execute(() -> {
            try (ServerSocket trackerSocket = new ServerSocket(TRACKER_PORT)) {
                while (true) {
                    try (Tracker2ClientConnection client = new Tracker2ClientConnection(trackerSocket.accept())) {
                        int requestByte;
                        while ((requestByte = client.readRequestType()) != -1) {
                            switch (requestByte) {
                                case LIST:
                                    System.out.println("List command");
                                    Set<TorrentFileInfo> fileInfos = tracker.list();

                                    client.writeFileInfos(fileInfos);

                                    break;

                                case UPLOAD:

                                    System.out.println("Upload command");
                                    FileUpload fileUpload = client.readFileUpload();

                                    int newFileID = tracker.upload(
                                            fileUpload.getFileName(),
                                            fileUpload.getFileSize()
                                    );

                                    client.writeFileID(newFileID);

                                    break;

                                case SOURCES:
                                    System.out.println("Sources command");
                                    int fileID = client.readFileId();

                                    if (tracker.isCorrectID(fileID)) {

                                        List<TorrentClientInfo> clientInfos = tracker.sources(fileID);
                                        client.writeClientsInfos(clientInfos);

                                    } else {
                                        client.writeEmptyClientInfos();
                                    }


                                    break;
                                case UPDATE:
                                    System.out.println("Update command");
                                    ClientFilesUpdate clientFilesUpdate = client.readClientFilesUpdate();

                                    TorrentClientInfo clientInfo = new TorrentClientInfo(
                                            client.getIP(),
                                            clientFilesUpdate.getClientPort()
                                    );

                                    tracker.addClient(clientInfo);
                                    boolean status = tracker.update(
                                            clientInfo,
                                            clientFilesUpdate.getFilesID()
                                    );

                                    client.writeUpdateStatus(status);

                                    break;
                                default:
                                    System.out.println("tracker: undefined request");
                            }
                        }

                        System.out.println("Request " + requestByte + " is handled!");
                    }
                }
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
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
