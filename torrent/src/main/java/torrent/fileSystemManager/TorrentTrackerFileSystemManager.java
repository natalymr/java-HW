package torrent.fileSystemManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TorrentTrackerFileSystemManager extends TorrentFileSystemManager {

    public TorrentTrackerFileSystemManager() {
        super("/tmp/torrentTrackerDirectory");
    }

    public List<TorrentFileInfo> restoreAllFilesInfo() throws FileNotFoundException {
        List<TorrentFileInfo> result = new ArrayList<>();

        File[] fileList = torrentDirectory.listFiles();
        if (fileList != null) {
            for (File file : fileList) {

                System.out.println(file.getName());
                String fileName = file.getName();
                if (file.isFile() && fileName.endsWith(fileInfoExtension)) {
                    String idInString = fileName.substring(
                            0,
                            fileName.length() - fileInfoExtension.length()
                    );
                    int id = Integer.parseInt(idInString);

                    TorrentFileInfo torrentFileInfo = getFileInfoByID(id);
                    result.add(torrentFileInfo);
                }
            }
        }

        return result;
    }

    public int getLastIDNumber() {
        List<Integer> allID = new ArrayList<>();

        File[] fileList = torrentDirectory.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                String fileName = file.getName();
                if (file.isFile() && fileName.endsWith(fileInfoExtension)) {
                    String idInString = fileName.substring(
                            0,
                            fileName.length() - fileInfoExtension.length()
                    );
                    allID.add(Integer.parseInt(idInString));
                }
            }

            allID.sort(Comparator.naturalOrder());
            return allID.get(allID.size() - 1);
        }

        return 0;
    }
}
