package torrent.fileSystemManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * POJO - Plain Old Java Object
 */
public class TorrentFilePart {
    private final TorrentFileInfo fileInfo;
    private final int             partNumber;
    private final File            partFileContent;

    public TorrentFilePart(TorrentFileInfo fileInfo, int partNumber, InputStream partFileContent) throws IOException {
        this.fileInfo   = fileInfo;
        this.partNumber = partNumber;

        TorrentFileSystemManager fileSystemManager = new TorrentFileSystemManager();
        this.partFileContent   = fileSystemManager.createNewFilePart(
                fileInfo.getId(),
                partNumber,
                partFileContent
        );
    }

    TorrentFilePart(TorrentFileInfo fileInfo, int partNumber, File partFileContent) {
        this.fileInfo        = fileInfo;
        this.partNumber      = partNumber;
        this.partFileContent = partFileContent;
    }

    int getPartNumber() {
        return partNumber;
    }

    public File getPartFileContentInFile() {
        return partFileContent;
    }
}
