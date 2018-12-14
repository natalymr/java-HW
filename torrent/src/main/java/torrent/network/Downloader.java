package torrent.network;

import torrent.client.TorrentClientInfo;
import torrent.fileSystemManager.TorrentFile;
import torrent.fileSystemManager.TorrentFileInfo;
import torrent.fileSystemManager.TorrentFilePart;
import torrent.fileSystemManager.TorrentClientFileSystemManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Downloader {
    private final TorrentClientFileSystemManager fileSystemManager;

    Downloader() {
        fileSystemManager = new TorrentClientFileSystemManager();
    }

    boolean download(int id, List<TorrentClientInfo> sourceClients, Path pwd) throws IOException {
        TorrentFileInfo fileInfo;
        try {
            fileInfo = fileSystemManager.getFileInfoByID(id);
        } catch (FileNotFoundException e) {
            return false;
        }

        int partSize   = fileSystemManager.getFilePartSize();
        int partsCount = (int)(fileInfo.getSize() / ((double)partSize)) + 1;

        Set<Integer> availableParts = new HashSet<>();
        Set<Integer> allPartsNumber = new HashSet<>();
        Map<Integer, TorrentClientInfo> partNumberVSclient = new HashMap<>();
        for (int i = 0; i < partsCount; i++) {
            allPartsNumber.add(i);
        }

        // check whether all parts of file now is available
        for (TorrentClientInfo sourceClient : sourceClients) {
            try (Client2ClientConnection knocker = new Client2ClientConnection(new Socket("localhost", sourceClient.getPort()))) {

                StatResult availableAtThisClientParts = knocker.sendStatRequest(id);
                List<Integer> partsNumber = availableAtThisClientParts.getPartsNumber();
                for (Integer partNumber : partsNumber) {
                    partNumberVSclient.put(partNumber, sourceClient);
                }

                availableParts.addAll(availableAtThisClientParts.getPartsNumber());

            } catch (UnknownHostException e) {
                System.out.println("Downloader: UnknownHostException: " + e.getLocalizedMessage());
            } catch (IOException e) {
                System.out.println("Downloader: IOException: " + e.getLocalizedMessage());
            }
        }

        if (!availableParts.equals(allPartsNumber)) {
            return false;
        }

        TorrentFile torrentFile = new TorrentFile(fileInfo);
        ExecutorService executor = Executors.newCachedThreadPool();

        for(Map.Entry<Integer, TorrentClientInfo> partVSclient : partNumberVSclient.entrySet()) {
            executor.execute(() -> {
                try (Client2ClientConnection downloader = new Client2ClientConnection(
                        new Socket(
                                "localhost",
                                partVSclient.getValue().getPort())
                        )
                    ) {

                    TorrentFilePart part = downloader.sendGetRequest(
                            id,
                            partVSclient.getKey(),
                            fileInfo,
                            fileSystemManager.getFilePartSize()
                    );

                    torrentFile.addNewPart(part);

                } catch (UnknownHostException e) {
                    System.out.println("Downloader: UnknownHostException: " + e.getLocalizedMessage());
                } catch (IOException e) {
                    System.out.println("Downloader: IOException: " + e.getLocalizedMessage());
                }
            });
        }

        torrentFile.mergeAllPartAndSaveInPWD(pwd, fileSystemManager.getFilePartSize());


        return true;
    }
}
