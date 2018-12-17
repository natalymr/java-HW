package torrent.fileSystemManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TorrentFile {

    private TorrentFileInfo       fileInfo;
    private List<TorrentFilePart> parts;

    public TorrentFile(TorrentFileInfo torrentFileInfo) {
        this.fileInfo = torrentFileInfo;
        this.parts    = new ArrayList<>();
    }

    public String getName() {
        return fileInfo.getName();
    }

    public void addNewPart(TorrentFilePart filePart) {
        parts.add(filePart);
    }

    public TorrentFilePart getTorrentFilePart(int partNumber) {
        for (TorrentFilePart part : parts) {
            if (part.getPartNumber() == partNumber) {
                return part;
            }
        }

        return null;
    }

    public List<Integer> getPartsNumber() {
        List<Integer> result = new ArrayList<>();

        for (TorrentFilePart part : parts) {
            result.add(part.getPartNumber());
        }

        return result;
    }

    public void mergeAllPartAndSaveInPWD(Path pwd, int partSize) throws IOException {
        System.out.println("merge: begin");
        String mergedFileName = pwd.toString() + File.separator + fileInfo.getName();
        File mergedFile = new File(mergedFileName);

        if (mergedFile.createNewFile()) {
            try (RandomAccessFile writer = new RandomAccessFile(mergedFileName, "rw")) {
                byte[] bytes = new byte[partSize];
                int i;
                for (TorrentFilePart part : parts) {

                    File partContentInFile = part.getPartFileContentInFile();
                    try (FileInputStream partContentReader = new FileInputStream(partContentInFile)) {
                        i = partContentReader.read(bytes);
                        System.out.println("merge count of read bytes " + i);
                    }

                    writer.write(bytes, 0, i);
                }
            }
        }
        System.out.println("merge: end");
    }
}
