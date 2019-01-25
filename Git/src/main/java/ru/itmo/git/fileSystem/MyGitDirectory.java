package ru.itmo.git.fileSystem;

import org.apache.commons.io.FileUtils;
import ru.itmo.git.branch.Branch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MyGitDirectory {
    public static final String myGitDirectoryName = ".mygit";
    public static final String CURRENT_BRANCH_LABEL = "current";
    private final File myGitDirectory;
    private final File objectsDirectory;
    private final File branchesDirectory;
    private final File indexFile;
    private final File headFile;

    public MyGitDirectory(File root, boolean toRestore) throws IOException {
        myGitDirectory = new File(root.toString() + File.separator + myGitDirectoryName);
        objectsDirectory = new File(myGitDirectory.toString() + File.separator + "objects");
        branchesDirectory = new File(myGitDirectory.toString() + File.separator + "branches");
        indexFile = new File(myGitDirectory.toString() + File.separator + "index");
        headFile = new File(myGitDirectory.toString() + File.separator + "head");

        if (!toRestore) {
            myGitDirectory.mkdir();
            objectsDirectory.mkdir();
            branchesDirectory.mkdir();
            indexFile.createNewFile();
            headFile.createNewFile();
        }
    }

    public File getObjectsDirectory() {
        return objectsDirectory;
    }

    public Branch getBranchByName(String branchName) throws IOException, ClassNotFoundException {
        ObjectInputStream ois;

        File[] fileList = branchesDirectory.listFiles();
        if (fileList != null && fileList.length > 0) {
            for (File file : fileList) {
                if (file.getName().equals(branchName)) {
                    ois = new ObjectInputStream(new FileInputStream(file));
                    return (Branch) ois.readObject();
                }
            }
        }

        return null;
    }

    // GET FILE BY TYPE AND HASH
    public static File getObjectsFileByHashAndType(int hash, String extension, File objectsDirectory) {
        String objectFileName = computeObjectsFileName(hash, extension, objectsDirectory);

        File[] objectsFiles = objectsDirectory.listFiles();
        if (objectsFiles != null && objectsFiles.length > 0) {
            for (File objectsFile : objectsFiles) {
                if (objectsFile.toString().equals(objectFileName)) {
                    return objectsFile;
                }
            }
        }

        throw new RuntimeException("can not find" + extension + "file");
    }

    // COMPUTE OBJECTS NAMES
    static String computeObjectsFileName(int hash, String extension, File objectsDirectory) {
        return objectsDirectory.toString() + File.separator + hash + extension;
    }

    // SAVE GIT FIELDS
    public void saveIndex(Index index) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile));
        oos.writeObject(index);
        oos.flush();
    }

    public void saveHead(Head head) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(headFile));
        oos.writeObject(head);
        oos.flush();
    }

    public void saveObjects(Objects objects) throws IOException {
        File objectsFile = new File(objectsDirectory.toString() + File.separator + "objects");
        objectsFile.createNewFile();

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(objectsFile));
        oos.writeObject(objects);
        oos.flush();
    }

    public void saveBranch(Branch branch) throws IOException {
        File branchFile = new File(branchesDirectory.toString() + File.separator + branch.getBranchName());
        branchFile.createNewFile();

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(branchFile));
        oos.writeObject(branch);
        oos.flush();

        File currentBranch = new File(branchesDirectory.toString() + File.separator + CURRENT_BRANCH_LABEL);
        if (!currentBranch.exists()) {
            currentBranch.createNewFile();
        }
        FileUtils.writeStringToFile(currentBranch, branch.getBranchName(), StandardCharsets.UTF_8);
    }

    // RESTORE GIT FIELDS
    public Index restoreIndex() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile));

        return (Index) ois.readObject();
    }

    public Head restoreHead() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(headFile));

        return (Head) ois.readObject();
    }

    public Objects restoreObjects() throws IOException, ClassNotFoundException {
        File objectsFile = new File(objectsDirectory.toString() + File.separator + "objects");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectsFile));

        return (Objects) ois.readObject();
    }

    public Branch restoreBranch() throws IOException, ClassNotFoundException {
        File currentBranchNameKeeper = new File(
            branchesDirectory.toString()
            + File.separator
            + CURRENT_BRANCH_LABEL);

        String currentBranchName = FileUtils.readFileToString(currentBranchNameKeeper, StandardCharsets.UTF_8);

        File currentBranch = new File(
            branchesDirectory.toString()
            + File.separator
            + currentBranchName
        );

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(currentBranch));

        return (Branch) ois.readObject();
    }
}
