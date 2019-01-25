package ru.itmo.git.branch;

import org.apache.commons.io.FileUtils;
import ru.itmo.git.commons.ConsoleColors;
import ru.itmo.git.fileSystem.Index;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static ru.itmo.git.fileSystem.CommitInformation.COMMIT_EXTENSION;
import static ru.itmo.git.fileSystem.MyGitDirectory.getObjectsFileByHashAndType;

public class Branch implements Serializable {
    private final String branchName;
    private final File objectsDirectory;
    private Node current;

    public Branch(String branchName, File objectsDirectory) {
        this.branchName = branchName;
        this.objectsDirectory = objectsDirectory;
        current = null;
    }

    public Branch(String branchName, Node current, File objectsDirectory) {
        this.branchName = branchName;
        this.objectsDirectory = objectsDirectory;
        this.current = current;
    }

    public String getBranchName() {
        return branchName;
    }

    public Node getLastCommit() {
        return current;
    }

    public void setLastCommit(Node newNode) {
        current = newNode;
    }

    public void addNewCommit(Index index, int commitRevision) {
        Node newCommit = new Node(branchName, commitRevision, current, index);

        if (!(current == null)) {
            current.addChild(newCommit);
        }

        setLastCommit(newCommit);
    }

    public void log() throws IOException {
        log(current);
    }

    public void log(int commitRevision) throws IOException {
        Node tmp = current;
        // find needed node
        while (true) {
            if (tmp.getCommitRevision() == commitRevision || tmp.getParent() == null) {
                break;
            } else {
                tmp = tmp.getParent();
            }
        }

        log(tmp);
    }

    private void log(Node startNode) throws IOException {
        Node tmp = startNode;
        while (true) {
            int commitRevision = tmp.getCommitRevision();

            printCommitInfo(commitRevision);

            if (tmp.getParent() == null) {
                break;
            } else {
                tmp = tmp.getParent();
            }
        }
    }

    private void printCommitInfo(int commitRevision) throws IOException {
        String commitInfo = FileUtils.readFileToString(
            getObjectsFileByHashAndType(commitRevision, COMMIT_EXTENSION, objectsDirectory),
            StandardCharsets.UTF_8
        );

        System.out.println(ConsoleColors.YELLOW + "commit# " + commitRevision + ConsoleColors.RESET);
        System.out.println(commitInfo);
    }


    public Node getNeededNode(int commitRevision) {
        Node tmp = current;
        while (true) {
            if (tmp.getCommitRevision() == commitRevision) {
                return tmp;
            }

            if (tmp.getParent() == null) {
                return null;
            } else {
                tmp = tmp.getParent();
            }
        }
    }
}
