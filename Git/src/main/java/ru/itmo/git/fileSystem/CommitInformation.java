package ru.itmo.git.fileSystem;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static ru.itmo.git.fileSystem.MyGitDirectory.computeObjectsFileName;
import static ru.itmo.git.fileSystem.MyGitDirectory.getObjectsFileByHashAndType;

public class CommitInformation implements Serializable {
    public static final String  COMMIT_EXTENSION = ".commit";
    private static final String TREE_HASH_LABEL     = "Tree Hash: ";
    private static final String PARENT_COMMIT_LABEL = "Parent Commit: ";
    private static final String DATA_LABEL          = "Data: ";
    private static final String MESSAGE_LABEL       = "Message: ";
    private int treeHash;
    private int parent;
    private String data;
    private String message;

    CommitInformation(int treeHash, int parent, String message) {
        this.treeHash = treeHash;
        this.parent = parent;
        this.message = message;
        this.data = new Date().toString();
    }

    public String toString() {
        return TREE_HASH_LABEL +
            treeHash +
            "\n" +
            PARENT_COMMIT_LABEL +
            parent +
            "\n" +
            DATA_LABEL +
            data +
            "\n" +
            MESSAGE_LABEL +
            message +
            "\n";
    }

    private CommitInformation(File commitFile) throws IOException {
        try(Scanner scanner = new Scanner(commitFile)) {
            String line = scanner.nextLine();

            if (line.startsWith(TREE_HASH_LABEL)) {
                treeHash = Integer.parseInt(line.substring(TREE_HASH_LABEL.length()));

            } else if (line.startsWith(PARENT_COMMIT_LABEL)) {
                parent = Integer.parseInt(line.substring(PARENT_COMMIT_LABEL.length()));

            } else if (line.startsWith(DATA_LABEL)) {
                data = line.substring(DATA_LABEL.length());

            } else if (line.startsWith(MESSAGE_LABEL)) {
                message = line.substring(MESSAGE_LABEL.length());
            }
        } catch (NoSuchElementException ignored) {}
    }

    int getTreeHash() {
        return treeHash;
    }

    int commit(File objectsDirectory) throws IOException {
        String commitInformationInString = toString();

        HashCode hashCode = Hashing.murmur3_32()
            .newHasher()
            .putString(commitInformationInString, StandardCharsets.UTF_8)
            .hash();

        int hash = hashCode.asInt();

        File commitFile = new File(computeObjectsFileName(hash, COMMIT_EXTENSION, objectsDirectory));
        if (commitFile.createNewFile()) {
            FileUtils.writeStringToFile(commitFile, commitInformationInString, StandardCharsets.UTF_8);
        }

        return hash;
    }

    static CommitInformation restoreCommitInfo(int commitRevision, File objectsDirectory) throws IOException {
        File commitFile = getObjectsFileByHashAndType(commitRevision, COMMIT_EXTENSION, objectsDirectory);
        return new CommitInformation(commitFile);
    }
}
