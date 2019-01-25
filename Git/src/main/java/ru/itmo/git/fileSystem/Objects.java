package ru.itmo.git.fileSystem;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static ru.itmo.git.fileSystem.CommitInformation.restoreCommitInfo;

public class Objects implements Serializable {
    private File objectsDirectory;
    private Tree rootDirectoryTree;

    public Objects(File objectsDirectory, File rootDirectoryTree) {
        this.objectsDirectory = objectsDirectory;
        this.rootDirectoryTree = new Tree(rootDirectoryTree, objectsDirectory);
    }

    public File getObjectsDirectory() {
        return objectsDirectory;
    }

    public void add(File file) {
        if (rootDirectoryTree.getDirectory().toString().equals(file.getParent())) {
            rootDirectoryTree.add(file);
        } else {
            rootDirectoryTree.addAllPath(file);
        }
    }

    public int commit(Index index, int parentHash, String message) throws IOException {
        int treeHash = rootDirectoryTree.commit(index);
        return new CommitInformation(treeHash, parentHash, message).commit(objectsDirectory);
    }


    public void restoreFromCommitRevision(int commitRevision, File directoryToRestore) throws IOException {
        CommitInformation commitInformation = restoreCommitInfo(commitRevision, objectsDirectory);

        int rootDirectoryTreeHash = commitInformation.getTreeHash();
        rootDirectoryTree = new Tree(rootDirectoryTreeHash, directoryToRestore, this);
    }
}
