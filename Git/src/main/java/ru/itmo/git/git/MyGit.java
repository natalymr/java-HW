package ru.itmo.git.git;

import ru.itmo.git.branch.Branch;
import ru.itmo.git.branch.Node;
import ru.itmo.git.commons.ConsoleColors;
import ru.itmo.git.fileSystem.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.itmo.git.fileSystem.Blob.*;
import static ru.itmo.git.fileSystem.MyGitDirectory.CURRENT_BRANCH_LABEL;
import static ru.itmo.git.fileSystem.MyGitDirectory.myGitDirectoryName;

class MyGit {
    private final File pwd;
    private MyGitDirectory myGitDirectory;
    private Objects objects;
    private Branch branch;
    private Index index;
    private Head head;

    MyGit(File pwd) {
        this.pwd = pwd;
    }

    void init() throws IOException {
        myGitDirectory = new MyGitDirectory(pwd, false);

        index = new Index();
        head = new Head();
        objects = new Objects(myGitDirectory.getObjectsDirectory(), pwd);
        branch = new Branch("master", myGitDirectory.getObjectsDirectory());
    }

    void open() throws IOException, ClassNotFoundException {
        myGitDirectory = new MyGitDirectory(pwd, true);

        head = myGitDirectory.restoreHead();
        index = myGitDirectory.restoreIndex();
        objects = myGitDirectory.restoreObjects();
        branch = myGitDirectory.restoreBranch();
    }

    void close() throws IOException {
        myGitDirectory.saveHead(head);
        myGitDirectory.saveIndex(index);
        myGitDirectory.saveObjects(objects);
        myGitDirectory.saveBranch(branch);
    }

    void add(List<File> files) throws IOException {
        for (File file : files) {
            index.add(file);
            objects.add(file);
        }
    }

    void rm(List<File> files) {
        for (File file : files) {
            index.changeFileStatus(file, FileStatus.Deleted);
        }
    }

    void currentBranchName() {
        System.out.println(ConsoleColors.BLUE + "*" + branch.getBranchName() + ConsoleColors.RESET);
    }

    void commit(String message) throws IOException {
        // delete all deleted
        index.commitDeleteDeleted();

        // compute parent commit revision
        int parentCommitRevision;
        if (branch.getLastCommit() == null) { // current commit is the first
            parentCommitRevision = 0;
        } else {
            parentCommitRevision = branch.getLastCommit().getCommitRevision();
        }

        // create needed tree and blob files
        // also create commit file and return its hash
        int commitRevision = objects.commit(index, parentCommitRevision, message);

        index.commitFromAddedOrModifiedToTracked();

        // add this commitDeleteDeleted to branch
        branch.addNewCommit(index, commitRevision);

        // set last commit as top
        head.setHeadCommitRevision(commitRevision);
    }

    void log() throws IOException {
        branch.log();
    }

    void log(int commitRevision) throws IOException {
        branch.log(commitRevision);
    }

    void reset() throws IOException {
        Node commitToReset = branch.getLastCommit().getParent();

        if (commitToReset == null) {
            throw new RuntimeException("there is no previous commit");
        }

        branch.setLastCommit(commitToReset);
        head.setHeadCommitRevision(commitToReset.getCommitRevision());
        index = branch.getLastCommit().getIndex();
        objects.restoreFromCommitRevision(branch.getLastCommit().getCommitRevision(), pwd);
    }

    void reset(int commitRevision) throws IOException {
        Node commitToReset = branch.getNeededNode(commitRevision);

        if (commitToReset == null) {
            throw new RuntimeException("there is no such commit");
        }

        branch.setLastCommit(commitToReset);
        head.setHeadCommitRevision(commitRevision);
        index = branch.getLastCommit().getIndex();
        objects.restoreFromCommitRevision(branch.getLastCommit().getCommitRevision(), pwd);
    }

    void createNewBranch(String branchName) {
        if (branchName.equals(CURRENT_BRANCH_LABEL)) {
            throw new RuntimeException("you can not choose this name for your branch");
        }

        Node old = branch.getLastCommit();
        branch = new Branch(branchName, old, objects.getObjectsDirectory());
    }

    void switchToBranch(String branchName) throws IOException, ClassNotFoundException {
        Branch branchToSwitch = myGitDirectory.getBranchByName(branchName);

        if (branchToSwitch == null) {
            throw new RuntimeException("there is no such commit");
        }

        branch = branchToSwitch;
        int commitRevision = branch.getLastCommit().getCommitRevision();

        checkout(commitRevision);
    }

    void checkout(int commitRevision) throws IOException {
        Node commitToReset = branch.getNeededNode(commitRevision);

        if (commitToReset == null) {
            throw new RuntimeException("there is no such commit");
        }

        branch.setLastCommit(commitToReset);
        head.setHeadCommitRevision(commitRevision);
        index = branch.getLastCommit().getIndex();
        objects.restoreFromCommitRevision(branch.getLastCommit().getCommitRevision(), pwd);
    }

    void checkout(List<File> files) throws IOException {
        for (File file : files) {
            checkoutBlobFileContent(file, index.getFileHash(file), objects.getObjectsDirectory());
        }
    }

    void mergeWithBranch(String otherBranchName) throws IOException, ClassNotFoundException {
        Index otherBranchIndex = myGitDirectory
            .getBranchByName(otherBranchName)
            .getLastCommit()
            .getIndex();

        List<File> filesToAdd = new ArrayList<>();
        List<File> filesToMerge = new ArrayList<>();

        for (File otherFile : otherBranchIndex) {
            if (otherBranchIndex.getFileStatus(otherFile) == FileStatus.Tracked) {

                if (index.knowsAbout(otherFile)) {
                    if (index.getFileHash(otherFile) != otherBranchIndex.getFileHash(otherFile)) {
                        filesToMerge.add(otherFile);
                    }
                } else {
                    filesToAdd.add(otherFile);
                }
            }
        }

        for (File file : filesToAdd) {
            // add files
            objects.add(file);

            // restore-create files to add
            checkoutBlobFileContent(file, otherBranchIndex.getFileHash(file), objects.getObjectsDirectory());

            // add to index
            index.add(file);
        }

        for (File file : filesToMerge) {
            mergeBlobFilesContent(otherBranchIndex.getFileHash(file), file, objects.getObjectsDirectory());
        }

        commit(branch.getBranchName() + " merged with branch " + otherBranchName);
    }

    void status() throws IOException {
        currentBranchName();

        File[] fileList = pwd.listFiles();
        if (fileList != null && fileList.length > 0) {
            for (File subFile : fileList) {

                if (subFile.getName().equals(myGitDirectoryName)) {
                    continue;
                }

                if (subFile.isDirectory()) {
                    statusForSubDirectory(subFile);
                } else {
                    statusForFile(subFile);
                }
            }
        }
    }

    private void statusForSubDirectory(File file) throws IOException {
        File[] fileList = file.listFiles();

        if (fileList != null && fileList.length > 0) {
            for (File subFile : fileList) {
                if (subFile.isDirectory()) {
                    statusForSubDirectory(subFile);
                } else {
                    statusForFile(subFile);
                }
            }
        }
    }

    private void statusForFile(File file) throws IOException {
        if (index.knowsAbout(file)) {
            switch (index.getFileStatus(file)) {
                case Modified: {
                    System.out.println(
                        ConsoleColors.GREEN +
                            "Modified: " +
                            file.toString() +
                            ConsoleColors.RESET
                    );
                    break;
                }
                case Added: {
                    System.out.println(
                        ConsoleColors.GREEN +
                            "Added: " +
                            file.toString() +
                            ConsoleColors.RESET
                    );
                    break;
                }
                case Deleted: {
                    System.out.println(
                        ConsoleColors.GREEN +
                            "Deleted: " +
                            file.toString() +
                            ConsoleColors.RESET
                    );
                    break;
                }
                case Tracked: {
                    int oldHash = index.getFileHash(file);
                    int currentHash = computeHash(file);

                    if (currentHash != oldHash) {
                        System.out.println(
                            ConsoleColors.RED +
                                "Modified: " +
                                file.toString() +
                                ConsoleColors.RESET
                        );
                    }
                    break;
                }
            }
        } else {
            System.out.println(
                ConsoleColors.RED +
                    "? " +
                    file.toString() +
                    ConsoleColors.RESET
            );
        }
    }
}
