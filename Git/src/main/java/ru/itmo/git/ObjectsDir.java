package ru.itmo.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class ObjectsDir {
    private File objectDir;
    private final HashSet<String> listOfFiles;

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

    private boolean isInObjectsDir(String fileName) {
        return listOfFiles.contains(fileName);
    }

    private void addNewFile(String fileName, String fileContent) {
        String fullFileName = objectDir.toString() + File.separator + fileName + ".txt";

        try {
            File newFile = new File(fullFileName);
            FileUtils.writeStringToFile(newFile, fileContent, "UTF-8");
        } catch (IOException e) {
            System.err.println("Object. addNewFile. Failed to create new file " + fileName);
            e.printStackTrace();
        }

        listOfFiles.add(fileName);
    }

    public String getFullPathByName(String hashInString) {
        if (listOfFiles.contains(hashInString)) {
            return objectDir.toString() + File.separator + hashInString + ".txt";
        }

        return "";
    }

    public Integer createNewBlobFile(Path pathToFile) {
        File file = pathToFile.toFile();

        String blobFileContent= "BLOB\n";
        // read file content from file
        try {
            blobFileContent += FileUtils.readFileToString(file, "UTF-8");

        } catch (IOException e) {
            System.err.println("ObjectsDir. createNewBlobFile. Failed to read " + pathToFile);
            e.printStackTrace();
        }

        Integer hash = blobFileContent.hashCode();
        String hashInString = Integer.toString(hash);

        // create new blob file in object if there is no such file
        if (!isInObjectsDir(hashInString)) {
            addNewFile(hashInString, blobFileContent);
        }

        return hash;
    }

    public Integer createNewTreeFile(Path pathToDir, Map<String, Integer> indexInMap) {
        File dir = pathToDir.toFile();
        StringBuilder treeFileContent = new StringBuilder("TREE\n");

        if (dir.isDirectory()) {
            // list all files in dir
            List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            // iterate of all files in this dir
            // if cur file is file, just add "blob file_name hash(file_content)"
            // if cur file is dir,  just add "tree dir_name  hash(tree_file_content)"
            for (File curFile : files) {
                // skip all git's files
                if (curFile.getName().equals(".git") ||
                        curFile.getName().equals("objects") ||
                        curFile.getParent().equals(objectDir.toString()) ||
                        curFile.getName().equals("index.txt") ||
                        curFile.getName().equals("HEAD.txt")) {
                    continue;
                }

                if (curFile.isDirectory() && indexInMap.containsKey(curFile.toString())) {
                    String hashOfTreeFileForThisSubDir = Integer.toString(createNewTreeFile(curFile.toPath(), indexInMap));
                    String dirInfo = "tree" + " " + curFile.toString() + " " + hashOfTreeFileForThisSubDir + "\n";
                    treeFileContent.append(dirInfo);
                } else if (curFile.isFile() && indexInMap.containsKey(curFile.toString())) {
                    String hashOfBlobFile = Integer.toString(createNewBlobFile(curFile.toPath()));
                    String fileInfo = "blob" + " " + curFile.toString() + " " + hashOfBlobFile + "\n";
                    treeFileContent.append(fileInfo);
                }
            }
        }

        Integer hash = treeFileContent.toString().hashCode();
        String hashInString = Integer.toString(hash);

        // if in .git/objects there is file with name like hash, do nothing
        // else - create new file and write in it tree file content
        if (!isInObjectsDir(hashInString)) {
            addNewFile(hashInString, treeFileContent.toString());
        }

        return hash;
    }

    public Integer createNewCommitFile(CommitInfo curCommitInfo) {
        String commitFileContent = "COMMIT";

        commitFileContent += curCommitInfo.getCommitInfo();

        Integer hash = commitFileContent.hashCode();
        String hashInString = Integer.toString(hash);

        if (!isInObjectsDir(hashInString)) {
            addNewFile(hashInString, commitFileContent);
        }

        return hash;
    }

    public void invertTreeHashFile(Integer treeHash, String pwd) throws IOException {
        String treeHashFileNameInString = Integer.toString(treeHash);

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
                    if (restoredDirContains.contains(new File(path))) {
                        restoredDirContains.remove(new File(path));
                    }

                    if (label.equals("tree")) {
                        /* call this function recursively for this subDir */
                        invertTreeHashFile(Integer.parseInt(hash), path);
                    } else {

                        /* get this blob file content */
                        if (isInObjectsDir(hash)) {
                            String blobFileContent = FileUtils.readFileToString(new File(getFullPathByName(hash)),
                                    "UTF-8");
                            blobFileContent = blobFileContent.substring(5, blobFileContent.length());

                        /* overwrite this file */
                            FileUtils.writeStringToFile(new File(path), blobFileContent, "UTF-8", false);
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

    public void invertBlobHashFile(File fileToCheckout, Integer hashBlobFile) throws IOException {
        String hashBlobFileInString = Integer.toString(hashBlobFile);

        if (isInObjectsDir(hashBlobFileInString)) {
            String fullPathToBlobFile = getFullPathByName(hashBlobFileInString);
            String blobFileContent = FileUtils.readFileToString(new File(fullPathToBlobFile), "UTF-8");

            blobFileContent = blobFileContent.substring(5, blobFileContent.length());

            FileUtils.writeStringToFile(fileToCheckout, blobFileContent, "UTF-8", false);
        }
    }

    /* this is for testing */
    public Integer getNumberOfObjectsFiles() {
        return listOfFiles.size();
    }
}
