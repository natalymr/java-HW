package ru.itmo.git.fileSystem;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static ru.itmo.git.fileSystem.MyGitDirectory.computeObjectsFileName;
import static ru.itmo.git.fileSystem.MyGitDirectory.getObjectsFileByHashAndType;

public class Blob implements Serializable {
    private static final String BLOB_EXTENSION = ".blob";
    private final File objectsDirectory;
    private final File blobFile;

    Blob(File blobFile, File objectsDirectory) {
        this.blobFile = blobFile;
        this.objectsDirectory = objectsDirectory;
    }

    Blob(int blobHash, File file, Objects objects) throws IOException {
        this.blobFile = file;
        objectsDirectory = objects.getObjectsDirectory();

        File blobFile = getObjectsFileByHashAndType(blobHash, BLOB_EXTENSION, objectsDirectory);
        FileUtils.copyFile(blobFile, file);
    }

    File getBlobFile() {
        return blobFile;
    }


    int computeHash() throws IOException {
        return computeHash(blobFile);
    }

    public static int computeHash(File file) throws IOException {
        HashCode fileHash = Files.hash(file, Hashing.murmur3_32());

        HashCode hashCode = Hashing.murmur3_32()
            .newHasher()
            .putString(fileHash.toString(), StandardCharsets.UTF_8)
            .hash();

        return hashCode.asInt();
    }

    void newBlobFile(int hash) throws IOException {
        String fileName = computeObjectsFileName(hash, BLOB_EXTENSION, objectsDirectory);
        File blobFile = new File(fileName);
        if (blobFile.createNewFile()) {
            FileUtils.copyFile(this.blobFile, blobFile);
        }
    }

    public static void checkoutBlobFileContent(File fileToCkeckout, int hash, File objectsDirectory) throws IOException {
        if (!fileToCkeckout.exists()) {
            fileToCkeckout.createNewFile();
        }

        File blobFile = getObjectsFileByHashAndType(hash, BLOB_EXTENSION, objectsDirectory);
        FileUtils.copyFile(blobFile, fileToCkeckout);
    }

    public static void mergeBlobFilesContent(int hashFileThatIsAdded, File fileToWhichAdd, File objectsDirectory) {
        File blobFileThatIsAdded = getObjectsFileByHashAndType(hashFileThatIsAdded, BLOB_EXTENSION, objectsDirectory);

        try (FileInputStream inputStream = new FileInputStream(blobFileThatIsAdded);
             FileOutputStream outputStream = new FileOutputStream(fileToWhichAdd, true)) {

            IOUtils.copy(inputStream, outputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Blob blob = (Blob) o;
        return objectsDirectory.equals(blob.objectsDirectory) &&
            blobFile.equals(blob.blobFile);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(objectsDirectory, blobFile);
    }
}
