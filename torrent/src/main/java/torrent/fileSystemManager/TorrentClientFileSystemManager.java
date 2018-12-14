package torrent.fileSystemManager;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TorrentClientFileSystemManager extends TorrentFileSystemManager {
    private static final String filePartExtension = ".part";
    private static final int    filePartSize      = 10 * 1_000_000;

    public TorrentClientFileSystemManager() {
        super("/tmp/torrentClientDirectory");
    }

    public int getFilePartSize() {
        return filePartSize;
    }

    File createNewFilePart(int id, int partNumber, InputStream inputStream) throws IOException {
        String DirNameIsID = torrentDirectory.toString() + File.separator + id;
        String newFileName = DirNameIsID + File.separator + partNumber + filePartExtension;

        File dir = new File(DirNameIsID);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File newFile = new File(newFileName);
        if (newFile.exists()) {
            return newFile;
        }

        newFile.createNewFile();

        // copy content
        Files.copy(inputStream, newFile.toPath());

        IOUtils.closeQuietly(inputStream);

        return newFile;
    }

    public Map<Integer, TorrentFile> getStoredFiles() throws FileNotFoundException {
        Map<Integer, TorrentFile> result = new HashMap<>();
        if (!checkTorrentDirectoryExisting()) {
            return result;
        }

        File[] fileList = torrentDirectory.listFiles();
        if (fileList != null) {

            for (File file : fileList) {
                if (file.isDirectory()) {
                    int id = Integer.parseInt(file.getName());
                    TorrentFileInfo torrentFileInfo = getFileInfoByID(id);
                    TorrentFile torrentFile = new TorrentFile(torrentFileInfo);

                    File[] filesInSubDir = file.listFiles();
                    if (filesInSubDir != null) {

                        for (File part : filesInSubDir) {
                            String partName = part.getName();

                            if (partName.endsWith(filePartExtension)) {
                                int partNumber = Integer.parseInt(
                                        partName.substring(
                                                0,
                                                partName.length() - filePartExtension.length())
                                );
                                torrentFile.addNewPart(
                                        new TorrentFilePart(
                                                torrentFileInfo,
                                                partNumber,
                                                part
                                        )
                                );
                            }
                        }
                    }
                }
            }
        }

        return result;
    }





    private File createDirForFileParts(int id) {
        String newDirName = torrentDirectory.toString()
                + File.separator
                + id;

        File result = new File(newDirName);
        if (!result.exists() || result.exists() && !result.isDirectory()) {
            result.mkdir();
        }

        return result;
    }

    public TorrentFile addNewTorrentFile(Path pwd, int id, String name, long size) throws IOException {
        File fileToReadFrom = new File(
                pwd.toString()
                 + File.separator
                 + name
                );

        if (!fileToReadFrom.exists()) {
            return null;
        }

        checkTorrentDirectoryExisting();

        TorrentFileInfo fileInfo = getFileInfoByID(id);
        TorrentFile result = new TorrentFile(fileInfo);

        File dirID = createDirForFileParts(id);

        int totalPartsCount = (int)(size / ((double) size)) + 1;
        try(FileInputStream reader = new FileInputStream(fileToReadFrom)) {
            for (int curPartNumber = 1; curPartNumber < totalPartsCount; curPartNumber++) {
                byte[] bytesArray = new byte[filePartSize];
                int offSet = (curPartNumber - 1) * filePartSize;
                reader.read(bytesArray, offSet, filePartSize);
                result.addNewPart(
                        new TorrentFilePart(
                                fileInfo,
                                curPartNumber,
                                new ByteArrayInputStream(bytesArray)
                        )
                );
            }
        }
        return result;
    }
}
