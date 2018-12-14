package torrent.network;

import org.junit.Test;
import torrent.fileSystemManager.TorrentFileInfo;
import torrent.tracker.TorrentTrackerImpl;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class TrackerTest {

    @Test
    public void testTrackerAnswerListUpload() throws FileNotFoundException, ExecutionException, InterruptedException {
        TorrentTrackerImpl tracker = new TorrentTrackerImpl();

        ExecutorService executor = Executors.newCachedThreadPool();

        executor.execute(new TrackerResponds(tracker));

        /* 0 - empty result of list
         */
        Future<Set<TorrentFileInfo>> listFuture1 = executor.submit(
                new ClientSendsRequest<Set<TorrentFileInfo>>("list", (short) 111)
        );

        Set<TorrentFileInfo> listResult1 = listFuture1.get();
        assertEquals(0, listResult1.size());

        /* 1 - add one file and check upload at the same time
         */
        String fileName1 = "fileName1";
        long fileSize1 = 10000000;
        Future<Integer> uploadFuture1 = executor.submit(
                new ClientSendsRequest<Integer>("upload", (short) 112, fileName1, fileSize1)
        );
        int uploadResult1 = uploadFuture1.get();
        assertEquals(1, uploadResult1);

        Future<Set<TorrentFileInfo>> listFuture2 = executor.submit(
                new ClientSendsRequest<Set<TorrentFileInfo>>("list", (short) 113)
        );
        Set<TorrentFileInfo> listResult2 = listFuture2.get();
        List<TorrentFileInfo> listResult2InList = new ArrayList<TorrentFileInfo>(listResult2);

        assertEquals(1, listResult2InList.size());
        assertEquals(fileName1, listResult2InList.get(0).getName());
        assertEquals(fileSize1, listResult2InList.get(0).getSize());
        assertEquals(uploadResult1, listResult2InList.get(0).getId());

        /* 3
         */
        String fileName2 = "fileName2";
        long   fileSize2 = 20000000;
        // upload
        Future<Integer> uploadFuture2 = executor.submit(
                new ClientSendsRequest<Integer>("upload", (short) 114, fileName2, fileSize2)
        );
        int uploadResult2 = uploadFuture2.get();

        Future<Integer> uploadFuture3 = executor.submit(
                new ClientSendsRequest<Integer>("upload", (short) 115, fileName1, fileSize1)
        );
        int uploadResult3 = uploadFuture3.get();

        // list
        Future<Set<TorrentFileInfo>> listFuture3 = executor.submit(
                new ClientSendsRequest<Set<TorrentFileInfo>>("list", (short) 113)
        );
        Set<TorrentFileInfo> listResult3 = listFuture3.get();

        for (TorrentFileInfo fileInfo : listResult3) {
            switch (fileInfo.getId()) {
                case 1:
                    assertEquals(fileName1, fileInfo.getName());
                    assertEquals(fileSize1, fileInfo.getSize());
                    break;
                case 2:
                    assertEquals(fileName2, fileInfo.getName());
                    assertEquals(fileSize2, fileInfo.getSize());
                    break;
                case 3:
                    assertEquals(fileName1, fileInfo.getName());
                    assertEquals(fileSize1, fileInfo.getSize());
                    break;
            }
        }

        executor.shutdown();
    }

}