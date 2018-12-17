package torrent.fileSystemManager;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TorrentClientFileSystemManager extends TorrentFileSystemManager {
    private static final String filePartExtension = ".part";
    private static final int    filePartSize      = 10 * 1_000_000;

    public TorrentClientFileSystemManager(String dirName) {
        super(dirName);
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
            IOUtils.closeQuietly(inputStream);
            return newFile;
        }

        if (newFile.createNewFile()) {
            // copy content
            Files.copy(inputStream, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            IOUtils.closeQuietly(inputStream);

            return newFile;
        }

        return null;
    }

    public Map<Integer, TorrentFile> getStoredFiles() throws FileNotFoundException {
        Map<Integer, TorrentFile> result = new HashMap<>();
        if (!checkTorrentDirectoryExisting()) {
            return result;
        }

        File[] fileList = torrentDirectory.listFiles();
        if (fileList != null && fileList.length > 0) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    int id = Integer.parseInt(file.getName());
                    TorrentFileInfo torrentFileInfo = getFileInfoByID(id);
                    TorrentFile torrentFile = new TorrentFile(torrentFileInfo);

                    File[] filesInSubDir = file.listFiles();
                    if (filesInSubDir != null && filesInSubDir.length > 0) {

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

                    result.put(id, torrentFile);
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

        TorrentFileInfo fileInfo = new TorrentFileInfo(id, name, size);
        addNewInfoFile(fileInfo);

        TorrentFile result = new TorrentFile(fileInfo);

        File dirID = createDirForFileParts(id);

        int totalPartsCount = (int)Math.ceil(size / ((double) filePartSize));

        try (FileInputStream reader = new FileInputStream(fileToReadFrom)) {
            byte[] buffer = new byte[filePartSize];
            for (int partNumber = 0; partNumber < totalPartsCount; partNumber++) {
                int count = reader.read(buffer);
                byte[] byteArrayToWriteFrom = Arrays.copyOfRange(buffer, 0, count);
                result.addNewPart(
                        new TorrentFilePart(
                                this,
                                fileInfo,
                                partNumber,
                                new ByteArrayInputStream(byteArrayToWriteFrom)
                        )
                );
            }
        }
        return result;
    }
}
