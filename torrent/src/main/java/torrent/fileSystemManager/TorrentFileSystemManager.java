package torrent.fileSystemManager;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TorrentFileSystemManager {
    private final File          torrentDirectory;
    private static final String filePartExtension = ".part";
    private static final String fileInfoExtension = ".info";
    private static final String ENCODING          = "UTF-8";
    private static final int    filePartSize      = 10 * 1_000_000;

    public TorrentFileSystemManager() {
        torrentDirectory = new File("/tmp/torrentDirectory");
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
        for (File file : fileList) {
            if (file.isDirectory()) {
                int id = Integer.parseInt(file.getName());
                TorrentFileInfo torrentFileInfo = getFileInfoByID(id);
                TorrentFile torrentFile = new TorrentFile(torrentFileInfo);

                File[] filesInSubDir = file.listFiles();
                for (File part : filesInSubDir) {
                    String partName = part.getName();
                    if (partName.endsWith(filePartExtension)) {
                        int partNumber = Integer.parseInt(partName.substring(0, filePartExtension.length()));
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

        return result;
    }

    private boolean checkTorrentDirectoryExisting() {
        boolean result = true;

        if (!torrentDirectory.exists()) {
            torrentDirectory.mkdirs();
            result = false;
        }

        return result;
    }

    public void addNewInfoFile(TorrentFileInfo fileInfo) throws IOException {
        checkTorrentDirectoryExisting();

        String newInfoFileName = torrentDirectory.toString()
                + File.separator
                + fileInfo.getId()
                + fileInfoExtension;

        // create new sizeFile
        File newSizeFile = new File(newInfoFileName);
        newSizeFile.createNewFile();

        // write to file size
        try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(newInfoFileName), ENCODING))) {
            fileWriter.write(Long.toString(fileInfo.getSize()) + "\n");
            fileWriter.write(fileInfo.getName());
        } catch (UnsupportedEncodingException ignored) {}

    }

    public TorrentFileInfo getFileInfoByID(int id) throws FileNotFoundException {
        if (!checkTorrentDirectoryExisting()) {
            return null;
        }

        String fileInfoName = torrentDirectory.toString()
                + File.separator
                + id
                + fileInfoExtension;
        File fileInfo = new File(fileInfoName);

        Scanner sc = new Scanner(fileInfo);
        long size = Long.parseLong(sc.nextLine());
        String name = sc.nextLine();

        return new TorrentFileInfo(id, name, size);
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
