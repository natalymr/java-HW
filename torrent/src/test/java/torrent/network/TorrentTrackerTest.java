package torrent.network;

import org.junit.Test;
import torrent.fileSystemManager.TorrentFileInfo;
import torrent.tracker.TorrentTrackerImpl;

import java.util.Set;

import static org.junit.Assert.*;

public class TorrentTrackerTest {

    @Test
    public void testListUpload() {
        TorrentTrackerImpl tracker = new TorrentTrackerImpl();

        String fileName1 = "fileName1";
        long   fileSize1 = 100000;
        String fileName2 = "fileName2";
        long   fileSize2 = 200000;


        // test returned value of upload()
        int id1 = tracker.upload(fileName1, fileSize1);
        assertEquals(1, id1);

        int id2 = tracker.upload(fileName2, fileSize2);
        assertEquals(2, id2);

        int id3 = tracker.upload(fileName1, fileSize1);
        assertEquals(3, id3);


        // test returned value of list()
        Set<TorrentFileInfo> listedFiles = tracker.list();
        for (TorrentFileInfo fileInfo : listedFiles) {
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
    }

}