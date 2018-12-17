package torrent.network;

import torrent.client.TorrentClientImpl;
import torrent.client.TorrentClientInfo;
import torrent.fileSystemManager.TorrentFileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;


public class ClientSendsRequest<V> implements Callable<V> {

    private static final int  TRACKER_PORT  = 8081;
    private final TorrentClientImpl client;
    private final short port;
    private final String command;

    private String fileName;
    private long   fileSize;

    private int id;

    private int filesCount;
    private Set<Integer> filesID;

    // list & update
    ClientSendsRequest(String command, short port) throws FileNotFoundException {
        this.client  = new TorrentClientImpl();
        this.port    = port;
        this.command = command;
    }

    // upload
    ClientSendsRequest(String command, short port, String fileName, long fileSize) throws FileNotFoundException {
        this.client  = new TorrentClientImpl();
        this.port    = port;
        this.command = command;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    // sources
    ClientSendsRequest(String command, short port, int id) throws FileNotFoundException {
        this.client  = new TorrentClientImpl();
        this.port    = port;
        this.command = command;
        this.id = id;
    }

    @Override
    public V call() {
        while(true) {
            try (Client2TrackerConnection c2t = new Client2TrackerConnection(
                    new Socket("localhost", TRACKER_PORT))) {
                Path pwd = FileSystems.getDefault().getPath(".");

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

                        return (V) listResult;
                    case "upload":

                        String newFileName = fileName;
                        long   newFileSize = fileSize;

                        int uploadResult = c2t.executeUploadCommand(newFileName, newFileSize);

                        System.out.printf(
                                "File with name %s and size %d was uploaded.\nid: %d\n",
                                newFileName,
                                newFileSize,
                                uploadResult
                        );

                        client.createTorrentFile(pwd, uploadResult, newFileName, newFileSize);

                        return (V) new Integer(uploadResult);
                    case "sources":

                        int fileID = id;

                        List<TorrentClientInfo> sourcesResult = c2t.executeSourcesCommand(fileID);

                        System.out.printf(
                                "count of seeds = %d\n",
                                sourcesResult.size());

                        return (V) sourcesResult;
                    case "update":

                        boolean updateResult = c2t.executeUpdateCommand(
                                port,
                                client.getFilesCount(),
                                client.getFilesID()
                        );

                        if (updateResult) {
                            System.out.println("test update result: SUCCESS");
                        } else {
                            System.out.println("test update result: FAIL");
                        }

                        return (V) new Boolean(updateResult);
//                    case "download":
//                        int id = Integer.parseInt(sc.next());
//
//                        List<TorrentClientInfo> sourceClients = c2t.executeSourcesCommand(id);
//
//                        Downloader downloader = new Downloader();
//                        downloader.download(id, sourceClients, pwd);

                    default:
                        System.out.println("test client: unknown command");

                }
            } catch (UnknownHostException e) {
                System.out.println("test client: can not connect to tracker: UnknownHostException: " + e.getLocalizedMessage());;
            } catch (IOException e) {
                System.out.println("test client: can not connect to tracker: IOException: " + e.getLocalizedMessage());
            }

        }

    }

}
