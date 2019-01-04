package ru.itmo.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


class Git {
    /* where git is*/
    private final String pwd;

    /* git's files */
    private Path gitPath;
    private Index index;
    private Head head;
    private ObjectsDir objects;

    /* git's structure */
    private GitGraph gitGraph;

    /**
     * Constructor
     * @param rootDir - where .git dir will be created
     */
     Git(File rootDir) {
        pwd      = rootDir.getAbsolutePath();

        gitPath  = null;
        index    = null;
        head     = null;
        objects  = null;

        gitGraph = null;//new GitGraph();
    }

    /**
     * Init
     * Create git dir and other git's files and directories
     */
    void init() throws IOException {

        String gitDirName = pwd + File.separator + ".mygit";

        /* create .git */
        File gitDir = new File(gitDirName);
        if (!gitDir.exists()) {
            if (!gitDir.mkdir()) {
                throw new FileNotFoundException("mygit: init");
            }
        }
        gitPath = gitDir.toPath();

        /* create .git/objects */
        objects = new ObjectsDir(gitPath);

        /* create .git/index.txt */
        index = new Index(gitPath);

        /* create .git/HEAD.txt */
        head = new Head(gitPath);

        /* create gitGraph*/
        gitGraph = new GitGraph(gitPath, head, objects, index);
    }

    /**
     * Open
     */
    void open() throws IOException {

        String gitDirName = pwd + File.separator + ".mygit";

        File gitDir     = new File(gitDirName);
        File objectsDir = new File(gitDirName + File.separator + "objects");
        File indexFile  = new File(gitDirName + File.separator + "index.txt");
        File headFile   = new File(gitDirName + File.separator + "HEAD.txt");


        /* check that there are all git's files */
        if (!gitDir.exists() ||
                !objectsDir.exists() ||
                !indexFile.exists() ||
                !headFile.exists()) {

            throw new FileNotFoundException("mygit: open");
        } else {
            gitPath  = new File(gitDirName).toPath();
            objects  = new ObjectsDir(objectsDir);
            index    = new Index(indexFile);
            head     = new Head(headFile);
        }

        /* restore gitGraph */
        gitGraph = new GitGraph(gitPath, head, objects, index);
    }

    /**
     * Add
     * Add file for
     * @param pathToFile the file or the dir that will be added
     */
    void add(Path pathToFile) throws IOException {

        /* create blobFile or treeFile for inputFile and get hash(fileContent) */
        String hash = "";
        index.addNewFileForChecking(pathToFile.toString());
        if (Files.isDirectory(pathToFile)) {

            /* list all files in dir */
            File[] fileList = pathToFile.toFile().listFiles();
            if (fileList != null && fileList.length > 0) {
                /* iterate of all files in this dir
                   if cur file is file, just add this file
                   if cur file is subdir,  just add recursive call add for it */
                for (File curFile : fileList) {
                    index.addNewFileForChecking(curFile.toString());
                }
            }

            hash = objects.createNewTreeFile(pathToFile, index);
        } else {
            hash = objects.createNewBlobFile(pathToFile, index);
        }
    }

    /**
     * Commit
     * Commit files
     * @param commitMessage - a message of this commit
     */
    void commit(String commitMessage) throws IOException {
        /* compute all accessory info for commit */
        Path pathPWD = Paths.get(pwd);
        /* parse indexFile in map<String - fileName, String - fileContentHash> */
        String hashOfTreeFileForRoot = objects.createNewTreeFile(pathPWD, index);

        Map<String, String> indexFileInMap = index.convertToMap();
        if (indexFileInMap.containsKey(pathPWD.toString())) {
            index.replaceHash(indexFileInMap.get(pathPWD.toString()), hashOfTreeFileForRoot);
        } else {
            index.addNewFile(pathPWD.toString(), hashOfTreeFileForRoot);
        }


        /* get parent commit */
        String parentHashInString = "-1";
        if (!head.isEmpty()) {
            parentHashInString = head.getHead();
        }

        int curRevisionNumber = gitGraph.getNextRevisionNumber();
        CommitInfo curCommit = new CommitInfo(curRevisionNumber, hashOfTreeFileForRoot, parentHashInString, commitMessage);
        /* create new commitFile */
        String hash = objects.createNewCommitFile(curCommit);
        gitGraph.addNewCommit(curRevisionNumber, index.convertToMap(), curCommit);


        /* change commit in head.txt */
        head.setHead(hash);
    }

    /**
     * This is default log.
     * It prints info of all commits.
     * @return List of CommitInfo. Each of them can be printed by printCommitInfo()
     */
    List<CommitInfo> log() {


        List<CommitInfo> result = new ArrayList<>();

        for (int curRevisionNumber = gitGraph.getSize(); curRevisionNumber > 0; curRevisionNumber--) {
            result.add(gitGraph.findCommit(curRevisionNumber).get().getCommitInfo());
        }
        return result;
    }

    /**
     * This is a variant of log function.
     * It prints info about commit fromRevision and less.
     * @param fromRevision - the number of revision from which it be printed
     * @return a list of structures, CommitInfo
     */
    List<CommitInfo> log(final Integer fromRevision) {
        List<CommitInfo> result = new ArrayList<>();


        for (int curRevisionNumber = fromRevision; curRevisionNumber > 0; curRevisionNumber--) {
            result.add(gitGraph.findCommit(curRevisionNumber).get().getCommitInfo());
        }

        return result;
    }

    /**
     * Checkout.
     * @param revNumber - which commit should be downloaded.
     */
    void checkout(Integer revNumber) throws IOException {
        if (revNumber > gitGraph.getSize()) {
            System.out.println("git checkout: Incorrect revision number");
        }

        gitGraph.saveHeadOfBranch();

        /* find node which contains info about commit */
        GitGraph.Node node = gitGraph.findCommit(revNumber).get();
        /* set node as current in git graph */
        gitGraph.setCur(node);
        /* update index file */
        index.IndexFromMap(node.getIndexInMap());

        /* update all files */
        String treeHash = node.getCommitInfo().getTreeHash();
        try {
            objects.invertTreeHashFile(treeHash, pwd);
        } catch (IOException e) {
            System.out.println("Checkout failed.");
        }
    }

    void checkout(File fileToCheckout) {
        Map<String, String> indexInMap = index.convertToMap();

        if (indexInMap.containsKey(fileToCheckout.toString())) {
            String fileHash = indexInMap.get(fileToCheckout.toString());
            try {
                objects.invertBlobHashFile(fileToCheckout, fileHash);
            } catch (IOException e) {
                System.out.println("checkout failed.");
            }
        }
    }

    void rm(File fileToDelete) {
        Map<String, String> indexInMap = index.convertToMap();

        if (indexInMap.containsKey(fileToDelete.toString())) {
            indexInMap.remove(fileToDelete.toString());
            index.IndexFromMap(indexInMap);
        }
    }

    void status() throws IOException {
        Map<String, String> indexInMap = index.convertToMap();

        File rootDir = new File(pwd);

        List<File> files = (List<File>) FileUtils.listFiles(rootDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File file : files) {

            if (file.getName().equals(".git") ||
                    file.getName().equals("objects") ||
                    file.getParent().equals(gitPath + File.separator + "objects") ||
                    file.getName().equals("index.txt") ||
                    file.getName().equals("HEAD.txt")) {
                continue;
            }

            if (file.isFile()) {
                if (!indexInMap.containsKey(file.toString())) {
                    System.out.println("? " + file);
                } else {
                    if (objects.createNewBlobFile(file.toPath(), index) != indexInMap.get(file.toString())) {
                        System.out.println("M " + file);
                    }
                }
            }
        }
    }

    // this is for testing
    Integer getNumberOfObjectsFiles() {
        return objects.getNumberOfObjectsFiles();
    }

    public HashSet<String> getListOfObjectsFiles() {
        return objects.getListOfFiles();
    }

}
