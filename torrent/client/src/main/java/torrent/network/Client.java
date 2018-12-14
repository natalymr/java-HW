package torrent.network;

import torrent.client.TorrentClientImpl;
import torrent.client.TorrentClientInfo;
import torrent.fileSystemManager.TorrentFileInfo;

import java.io.FileNotFoundException;
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

public class Client {
    static final int TRACKER_PORT = 8081;

    static public void main(String[] args) throws FileNotFoundException {
        short port = Short.parseShort(args[0]);
        final long TIME_TO_SLEEP = 3 * 60 * 1000;

        TorrentClientImpl client = new TorrentClientImpl();

        ExecutorService executor = Executors.newCachedThreadPool();

        // connect with other client
        executor.execute(() -> {
            try (ServerSocket clientServer = new ServerSocket(port)) {

                    while (true) {
                        try (Client2ClientConnection c2c = new Client2ClientConnection(clientServer.accept())) {
                            switch (c2c.readRequestByte()) {
                                case 1:

                                    int neededFileID = c2c.readNeededFiledID();

                                    List<Integer> availableFileParts = client.stat(neededFileID);

                                    c2c.writeAvailableFileParts(availableFileParts);

                                    break;

                                case 2:

                                    FileGet fileToGet = c2c.readFileToGet();

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

            } catch (IOException ignored) {}
        });

        // parse command from command_line + connect to tracker
        executor.execute(() -> {
            while(true) {
                try (Client2TrackerConnection c2t = new Client2TrackerConnection(
                        new Socket("localhost", TRACKER_PORT))) {
                    Path pwd = FileSystems.getDefault().getPath(".");

                    Scanner sc = new Scanner(System.in);
                    String command = sc.next();

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

                            client.addListResultToTorrentFileSystem(listResult);

                            break;
                        case "upload":

                            String newFileName = sc.next();
                            long   newFileSize = Long.parseLong(sc.next());

                            int uploadResult = c2t.executeUploadCommand(newFileName, newFileSize);

                            System.out.printf(
                                    "File with name %s and size %d was uploaded.\nid: %d\n",
                                    newFileName,
                                    newFileSize,
                                    uploadResult
                            );

                            client.createTorrentFile(pwd, uploadResult, newFileName, newFileSize);

                            break;
                        case "sources":

                            int fileID = Integer.parseInt(sc.next());

                            List<TorrentClientInfo> sourcesResult = c2t.executeSourcesCommand(fileID);

                            System.out.printf(
                                    "count of seeds = %d\n",
                                    sourcesResult.size());

                            break;
                        case "update":

                            boolean updateResult = c2t.executeUpdateCommand(port, client.getFilesCount(), client.getFilesID());

                            if (updateResult) {
                                System.out.println("update result: SUCCESS");
                            } else {
                                System.out.println("update result: FAIL");
                            }

                            break;
                        case "download":
                            int id = Integer.parseInt(sc.next());

                            List<TorrentClientInfo> sourceClients = c2t.executeSourcesCommand(id);

                            Downloader downloader = new Downloader();
                            downloader.download(id, sourceClients, pwd);

                        default:
                            System.out.println("client: unknown command");
                    }
                } catch (UnknownHostException e) {
                    System.out.println("client: can not connect to tracker: UnknownHostException: " + e.getLocalizedMessage());;
                } catch (IOException e) {
                    System.out.println("client: can not connect to tracker: IOException: " + e.getLocalizedMessage());
                }

            }

        });

        // ping tracker every 5 minuets
        executor.execute(() -> {
            try (Client2TrackerConnection pinger = new Client2TrackerConnection(
                    new Socket("localhost", TRACKER_PORT))) {

                pinger.executeUpdateCommand(port, client.getFilesCount(), client.getFilesID());
                Thread.sleep(TIME_TO_SLEEP);

            } catch (UnknownHostException e) {
                System.out.println("client: " +
                        "you will be unavailable for downloading from you: " +
                        "UnknownHostException: " + e.getLocalizedMessage());;
            } catch (IOException e) {
                System.out.println("client: " +
                        "you will be unavailable for downloading from you: " +
                        "IOException: " + e.getLocalizedMessage());
            } catch (InterruptedException e) {
                System.out.println("client: " +
                        "you will be unavailable for downloading from you: " +
                        "InterruptedException: " + e.getLocalizedMessage());
            }
        });
    }
}
