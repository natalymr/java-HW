package torrent.fileSystemManager;

import java.io.*;
import java.util.Scanner;

public class TorrentFileSystemManager {
    final String fileInfoExtension = ".info";
    static final String ENCODING   = "UTF-8";
    final File torrentDirectory;

    TorrentFileSystemManager(String torrentDirectoryName) {
        this.torrentDirectory = new File(torrentDirectoryName);
    }

    boolean checkTorrentDirectoryExisting() {

        if (!torrentDirectory.exists()) {
            return torrentDirectory.mkdirs();
        }

        return true;
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
}
