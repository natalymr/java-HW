package torrent.network;

import torrent.client.TorrentClientImpl;
import torrent.client.TorrentClientInfo;
import torrent.fileSystemManager.TorrentClientFileSystemManager;
import torrent.fileSystemManager.TorrentFileInfo;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client {
    static final int TRACKER_PORT = 8081;

    static public void main(String[] args) throws IOException {
        short port = Short.parseShort(args[0]);

        TorrentClientFileSystemManager fileSystemManager = new TorrentClientFileSystemManager(
                "/tmp/torrentClientDirectory" + port
        );

        TorrentClientImpl client = new TorrentClientImpl(fileSystemManager);

        ExecutorService executor = Executors.newCachedThreadPool();

        // ping tracker every 5 minuets
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleWithFixedDelay(() -> {
                    try (Client2TrackerConnection pinger = new Client2TrackerConnection(
                            new Socket("localhost", TRACKER_PORT))) {

                        pinger.executeUpdateCommand(port, client.getFilesCount(), client.getFilesID());

                    } catch (UnknownHostException e) {
                        System.out.println("client: " +
                                "you will be unavailable for downloading from you: " +
                                "UnknownHostException: " + e.getLocalizedMessage());
                        e.printStackTrace();
                    } catch (IOException e) {
                        System.out.println("client: " +
                                "you will be unavailable for downloading from you: " +
                                "IOException: " + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                },
                5,
                5,
                TimeUnit.MINUTES
        );

        // connect with other client
        Client2ClientServer server = new Client2ClientServer(client, port);
        executor.execute(server);

        // parse command from command_line + connect to tracker
        Path pwd = FileSystems.getDefault().getPath(".");
        Scanner sc = new Scanner(System.in);

        while (sc.hasNext()) {
            String command = sc.next();

            try (Client2TrackerConnection c2t = new Client2TrackerConnection(
                    new Socket("localhost", TRACKER_PORT))
            ) {

                switch (command) {
                    case "list":
                        Set<TorrentFileInfo> listResult = c2t.executeListCommand();

                        for (TorrentFileInfo fileInfo : listResult) {
                            System.out.printf(
                                    "id: %d; name %s; size: %d;\n",
                                    fileInfo.getId(),
                                    fileInfo.getName(),
                                    fileInfo.getSize()
                            );
                        }

                        try {
                            client.addListResultToTorrentFileSystem(listResult);
                        } catch (IOException e) {
                            System.out.println("torrent client: do not save this info");
                        }

                        break;
                    case "upload":

                        String newFileName = sc.next();

                        // get file size by name
                        File newFile = new File(pwd.toString() + File.separator + newFileName);
                        long newFileSize = newFile.length();

                        int uploadResult = c2t.executeUploadCommand(newFileName, newFileSize);

                        System.out.printf(
                                "File with name %s and size %d was uploaded.\nid: %d\n",
                                newFileName,
                                newFileSize,
                                uploadResult
                        );

                        try {
                            client.createTorrentFile(pwd, uploadResult, newFileName, newFileSize);
                        } catch (IOException e) {
                            System.out.println("torrent client: can not publish file " + newFileName);
                            e.printStackTrace();
                        }

                        c2t.executeUpdateCommand(port, client.getFilesCount(), client.getFilesID());

                        break;
                    case "sources":

                        int fileID = sc.nextInt();

                        List<TorrentClientInfo> sourcesResult = c2t.executeSourcesCommand(fileID);

                        System.out.printf(
                                "count of peers = %d\n",
                                sourcesResult.size());

                        break;
                    case "update":

                        boolean updateResult = c2t.executeUpdateCommand(
                                port,
                                client.getFilesCount(),
                                client.getFilesID()
                        );

                        if (updateResult) {
                            System.out.println("update result: SUCCESS");
                        } else {
                            System.out.println("update result: FAIL");
                        }

                        break;
                    case "download":
                        int id = sc.nextInt();

                        List<TorrentClientInfo> sourceClients = c2t.executeSourcesCommand(id);

                        Downloader downloader = new Downloader(fileSystemManager);
                        boolean downloadStatus = downloader.download(id, sourceClients, pwd);
                        if (downloadStatus) {
                            System.out.println("File with id " + id + " was successfully downloaded");
                        }
                        break;
                    case "exit":
                        return;

                    default:
                        System.out.println("client: unknown command");
                }
            } catch (UnknownHostException e) {
                System.out.println("client: can not connect to tracker: UnknownHostException: " + e.getLocalizedMessage());
                return;
            } catch (IOException e) {
                System.out.println("client: can not connect to tracker: IOException: ");e.printStackTrace();
                return;
            }
        }


    }

    private static class Client2ClientServer implements Runnable {
        private final TorrentClientImpl client;
        private final short port;
        private final ServerSocket clientServer;
        private final byte STAT = 1;
        private final byte GET  = 2;

        Client2ClientServer(TorrentClientImpl client, short port) throws IOException {
            this.client = client;
            this.port   = port;
            clientServer = new ServerSocket(port);
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted() && !clientServer.isClosed()) {
                    System.out.println("Waiting for connection on port " + port);
                    try (Client2ClientConnection c2c = new Client2ClientConnection(clientServer.accept())) {
                        switch (c2c.readRequestByte()) {
                            case STAT:
                                System.out.println("STAT command");
                                int neededFileID = c2c.readNeededFiledID();

                                List<Integer> availableFileParts = client.stat(neededFileID);

                                c2c.writeAvailableFileParts(availableFileParts);

                                break;

                            case GET:
                                System.out.println("GET command");
                                FileGet fileToGet = c2c.readFileToGet();
                                System.out.println("part number " + fileToGet.getPartNumber());
                                c2c.writeTorrentFilePart(client.get(
                                        fileToGet.getID(),
                                        fileToGet.getPartNumber()
                                ));

                                break;

                            default:
                                System.out.println("client: undefined request");
                        }
                    }
                }
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }

        void shutdown() {
            try {
                clientServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
