package ru.itmo.git;

import com.google.common.hash.*;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class ObjectsDir {
    /* objects' file */
    private File objectDir;

    /* objects' structure */
    private final HashSet<String> listOfFiles;

    /* removing magic constants */
    private final String ENCODING = "UTF-8";
    private final int BLOB_LENGTH = "BLOB\n".length();

    /**
     * Constructor
     * @param pathToGit - where .git/objects dir will be created
     */
    ObjectsDir(Path pathToGit) {
        String objectsDirName = pathToGit.toString() + File.separator + "objects";
        objectDir = new File(objectsDirName);

        if (!objectDir.exists()) {
            if (!objectDir.mkdir()) {
                objectDir = null;
            }
        }

        listOfFiles = new HashSet<>();
    }

    ObjectsDir(File oldObjectsDir) {
        objectDir = oldObjectsDir;

        /* add all files in hashSet */
        listOfFiles = new HashSet<>();
        if (objectDir.isDirectory()) {

            /* list all files in objects dir */
            List<File> files = (List<File>) FileUtils.listFiles(objectDir,
                    TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            for (File curFile : files) {
                listOfFiles.add(curFile.getName());
            }
        }
    }

    private void addNewFile(String fileName, String fileContent) throws IOException {
        String fullFileName = objectDir.toString() + File.separator + fileName + ".txt";

        File newFile = new File(fullFileName);
        FileUtils.writeStringToFile(newFile, fileContent, ENCODING);

        listOfFiles.add(fileName);
    }

    String createNewBlobFile(Path pathToFile, Index index) throws IOException {
        Map<String, String> indexInMap = index.convertToMap();
        File file = pathToFile.toFile();

        HashCode fileHash = Files.hash(file, Hashing.murmur3_32());

        /* hash BLOB + file content */
        HashCode hashCode = Hashing.murmur3_32()
                .newHasher()
                .putString("BLOB\n",  StandardCharsets.UTF_8)
                .putString(fileHash.toString(), StandardCharsets.UTF_8)
                .hash();

        String hashInString = hashCode.toString();

        /* create new blob file in object if there is no such file */
        if (!isInObjectsDir(hashInString)) {
            String fullBlobFileName = objectDir.toString() + File.separator + hashInString + ".txt";
            File newBlobFile = new File(fullBlobFileName);

            FileUtils.copyFile(file, newBlobFile);

            listOfFiles.add(hashInString);
        }

        String fileNameInString = pathToFile.toString();
        if (indexInMap.containsKey(fileNameInString) && indexInMap.get(fileNameInString).equals("")) {
            index.replaceEmptyHash(fileNameInString, hashInString);
        } else if (indexInMap.containsKey(fileNameInString)) {
            index.replaceHash(indexInMap.get(fileNameInString), hashInString);
        }

        return hashInString;
    }

    String createNewTreeFile(Path pathToDir, Index index) throws IOException {
        Map<String, String> indexInMap = index.convertToMap();

        File dir = pathToDir.toFile();
        StringBuilder treeFileContent = new StringBuilder();

        if (dir.isDirectory()) {
            /* list all files in dir */
            File[] fileList = pathToDir.toFile().listFiles();
            if (fileList != null && fileList.length > 0) {
                /* iterate of all files in this dir
                   if cur file is file, just add "blob file_name hash(file_content)"
                   if cur file is dir,  just add "tree dir_name  hash(tree_file_content)" */
                for (File curFile : fileList) {
                    /* skip all git's files */
                    String fileName = curFile.getName();
                    if (fileName.equals(".mygit") ||
                            fileName.equals("objects") ||
                            curFile.getParentFile().equals(objectDir) ||
                            fileName.equals("index.txt") ||
                            fileName.equals("HEAD.txt")) {
                        continue;
                    }

                    if (curFile.isFile() && indexInMap.containsKey(curFile.toString())) {

                        String hashOfBlobFile = createNewBlobFile(curFile.toPath(), index);
                        String fileInfo = "blob" + " " + curFile.toString() + " " + hashOfBlobFile + "\n";
                        treeFileContent.append(fileInfo);

                    } else if (curFile.isDirectory() && indexInMap.containsKey(curFile.toString())) {
                        String hashOfTreeFileForThisSubDir = createNewTreeFile(curFile.toPath(), index);
                        String dirInfo = "tree" + " " + curFile.toString() + " " + hashOfTreeFileForThisSubDir + "\n";
                        treeFileContent.append(dirInfo);
                    }
                }
            }
        }

        String treeFileContentInString = treeFileContent.toString();

        /* hash TREE file content */
        HashCode hashCode = Hashing.murmur3_32()
                .newHasher()
                .putString("TREE\n",  StandardCharsets.UTF_8)
                .putString(treeFileContentInString, StandardCharsets.UTF_8)
                .hash();

        String hashInString = hashCode.toString();

        /* if in .git/objects there is file with name like hash, do nothing
           else - create new file and write in it tree file content */
        if (!isInObjectsDir(hashInString)) {
            String fullTreeFileName = objectDir.toString() + File.separator + hashInString + ".txt";

            File newTreeFile = new File(fullTreeFileName);
            FileUtils.writeStringToFile(newTreeFile, "TREE\n" + treeFileContentInString, ENCODING);

            listOfFiles.add(hashInString);
        }

        String dirNameInString = pathToDir.toString();
        if (indexInMap.containsKey(dirNameInString) && indexInMap.get(dirNameInString).equals("")) {
            index.replaceEmptyHash(dirNameInString, hashInString);
        } else if (indexInMap.containsKey(dirNameInString)) {
            index.replaceHash(indexInMap.get(dirNameInString), hashInString);
        }

        return hashInString;
    }

    String createNewCommitFile(CommitInfo curCommitInfo) throws IOException {
        curCommitInfo.printCommitInfo();
        String commitFileContentInString = curCommitInfo.getCommitInfo();

        /* hash COMMIT file content */
        HashCode hashCode = Hashing.murmur3_32()
                .newHasher()
                .putString("COMMIT\n",  StandardCharsets.UTF_8)
                .putString(commitFileContentInString, StandardCharsets.UTF_8)
                .hash();

        String hashInString = hashCode.toString();

        if (!isInObjectsDir(hashInString)) {

            addNewFile(hashInString, "COMMIT\n" + commitFileContentInString);
        }

        return hashInString;
    }

    void invertTreeHashFile(String treeHash, String pwd) throws IOException {
        String treeHashFileNameInString = treeHash;

        if(isInObjectsDir(treeHashFileNameInString)) {
            File treeFile = new File(getFullPathByName(treeHashFileNameInString));
            File restoredDir = new File(pwd);
            List<File> restoredDirContains = (List<File>)FileUtils.listFiles(restoredDir,
                    TrueFileFilter.INSTANCE, FalseFileFilter.FALSE);

            try (Scanner scanner = new Scanner(treeFile)) {
                String fileLabel = scanner.nextLine();

                if (!fileLabel.equals("TREE")) {
                    return;
                } else {
                    String label = scanner.next();
                    String path = scanner.next();
                    String hash = scanner.next();

                    /* file is removed from restoredDirContains if this file was handled */
                    restoredDirContains.remove(new File(path));

                    if (label.equals("tree")) {
                        /* call this function recursively for this subDir */
                        invertTreeHashFile(hash, path);
                    } else {

                        /* get this blob file content */
                        if (isInObjectsDir(hash)) {
                            invertBlobHashFile(new File(path), hash);
                        }
                    }
                }
            }

            if (!restoredDirContains.isEmpty()) {
                for (File f : restoredDirContains) {
                    FileUtils.forceDelete(f);
                }
            }
        }
    }

    void invertBlobHashFile(File fileToCheckout, String hashBlobFile) throws IOException {

        if (isInObjectsDir(hashBlobFile)) {

            File blobFile = new File(getFullPathByName(hashBlobFile));
            FileUtils.copyFile(blobFile, fileToCheckout, false);
        }
    }

    private boolean isInObjectsDir(String fileName) {
        return listOfFiles.contains(fileName);
    }

    private String getFullPathByName(String hashInString) {
        if (listOfFiles.contains(hashInString)) {
            return objectDir.toString() + File.separator + hashInString + ".txt";
        }

        return null;
    }

    CommitInfo getCommitInfo(String fileName) {
        File[] files = objectDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.getName().equals(fileName + ".txt")) {
                    return new CommitInfo(file);
                }
            }
        }

        return null;
    }

    /* this is for testing */
    int getNumberOfObjectsFiles() {
        return listOfFiles.size();
    }

    HashSet<String> getListOfFiles() {
        return listOfFiles;
    }
}
