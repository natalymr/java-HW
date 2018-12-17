package torrent.network;

import torrent.client.TorrentClientInfo;
import torrent.fileSystemManager.TorrentFile;
import torrent.fileSystemManager.TorrentFileInfo;
import torrent.fileSystemManager.TorrentFilePart;
import torrent.fileSystemManager.TorrentClientFileSystemManager;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class Downloader {
    private final TorrentClientFileSystemManager fileSystemManager;

    Downloader(TorrentClientFileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }

    boolean download(int id, List<TorrentClientInfo> sourceClients, Path pwd) throws IOException {
        TorrentFileInfo fileInfo;
        try {
            fileInfo = fileSystemManager.getFileInfoByID(id);
        } catch (FileNotFoundException e) {
            return false;
        }

        int partSize   = fileSystemManager.getFilePartSize();
        int partsCount = (int) Math.ceil(fileInfo.getSize() / ((double)partSize));

        Set<Integer> availableParts = new HashSet<>();
        Set<Integer> allPartsNumber = new HashSet<>();
        Map<Integer, TorrentClientInfo> partNumberVSclient = new HashMap<>();
        for (int i = 0; i < partsCount; i++) {
            allPartsNumber.add(i);
        }

        // check whether all parts of file now is available
        for (TorrentClientInfo sourceClient : sourceClients) {
            System.out.println("try to connect to " + sourceClient.getPort());
            try (Client2ClientConnection knocker = new Client2ClientConnection(
                    new Socket(sourceClient.getIP(), sourceClient.getPort()))
            ) {
                System.out.println("i have connected to " + sourceClient.getPort());
                StatResult availableAtThisClientParts = knocker.sendStatRequest(id);
                List<Integer> partsNumber = availableAtThisClientParts.getPartsNumber();
                for (Integer partNumber : partsNumber) {
                    partNumberVSclient.put(partNumber, sourceClient);
                }

                availableParts.addAll(availableAtThisClientParts.getPartsNumber());

            } catch (UnknownHostException e) {
                System.out.println("Downloader: UnknownHostException: " + e.getLocalizedMessage());
            } catch (IOException e) {
                System.out.println("Downloader: IOException: ");
                e.printStackTrace();
            }
        }

        if (!availableParts.equals(allPartsNumber)) {
            return false;
        }

        TorrentFile torrentFile = new TorrentFile(fileInfo);
        System.out.println("create new Torrent File");
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch LATCH = new CountDownLatch(partsCount);

        for(Map.Entry<Integer, TorrentClientInfo> partVSclient : partNumberVSclient.entrySet()) {
            executor.execute(() -> {
                try (Client2ClientConnection downloader = new Client2ClientConnection(
                        new Socket(
                                partVSclient.getValue().getIP(),
                                partVSclient.getValue().getPort())
                        )
                    ) {

                    int partNumber = partVSclient.getKey();
                    System.out.println("before downloading " + partNumber + " part");
                    byte[] fileContent = downloader.sendGetRequest(
                            id,
                            partNumber,
                            fileInfo.getSize(),
                            partsCount,
                            fileSystemManager.getFilePartSize()
                    );

                    System.out.println("have got part " + partNumber);
                    TorrentFilePart part = new TorrentFilePart(
                            fileSystemManager,
                            fileInfo,
                            partNumber,
                            new ByteArrayInputStream(fileContent)
                    );

                    torrentFile.addNewPart(part);
                    LATCH.countDown();

                } catch (UnknownHostException e) {
                    System.out.println("Downloader: UnknownHostException: " + e.getLocalizedMessage());
                } catch (IOException e) {
                    System.out.println("Downloader: IOException: ");
                    e.printStackTrace();
                }
            });
        }

        System.out.println("have got all parts");
        while (LATCH.getCount() > 0);
        torrentFile.mergeAllPartAndSaveInPWD(pwd, fileSystemManager.getFilePartSize());

        System.out.println("merged all parts");

        return true;
    }

//    private boolean allPartsIsNotDownloaded(AtomicBoolean[] barrier) {
//        boolean result = false;
//        for (AtomicBoolean atomicBoolean : barrier) {
//            if (!atomicBoolean.get()) {
//                return true;
//            }
//        }
//
//        return false;
//    }
}
