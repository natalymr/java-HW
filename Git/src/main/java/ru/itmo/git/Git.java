package ru.itmo.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
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

    /* git's structures */
    private final GitGraph gitGraph;

    /**
     * Constructor
     * @param rootDir - where .git dir will be created
     */
     Git(File rootDir) {
        pwd = rootDir.getAbsolutePath();

        gitPath = null;
        index = null;
        head = null;
        objects = null;

        gitGraph = new GitGraph();
    }

    /**
     * Init
     * Create git dir and other git's files and directories
     */
    public void init() {

        String gitDirName = pwd + File.separator + ".git";

        // create .git
        File gitDir = new File(gitDirName);
        if (!gitDir.exists()) {
            if (!gitDir.mkdir()) {
                System.out.println("git init failed");

                return;
            }
        }
        gitPath = gitDir.toPath();

        // create .git/objects
        objects = new ObjectsDir(gitPath);

        // create .git/index.txt
        index = new Index(gitPath);

        // create .git/HEAD.txt
        head = new Head(gitPath);
    }

    /**
     * Add
     * Add file for
     * @param pathToFile a file that will be added
     */
    public void add(Path pathToFile) {
        /* create blobFile for this file and get hash(blobFileContent) */
        Integer hash = objects.createNewBlobFile(pathToFile);

        /* parse indexFile in map<String, Integer> */
        Map<String, Integer> indexFileInMap = index.convertToMap();

        /* if (there is this blob file in index.txt) {
                just replace hash
           } else {
                add new file and its hash } */
        if (indexFileInMap.containsKey(pathToFile.toString())) {
            index.replaceHash(indexFileInMap.get(pathToFile.toString()), hash);
        } else {
            index.addNewFile(pathToFile.toString(), hash);
        }
    }

    /**
     * Commit
     * Commit files
     * @param commitMessage - a message of this commit
     */
    public void commit(String commitMessage) {
        Integer hash;

        // compute all accessory info for commit
        Integer hashOfTreeFileForRoot = objects.createNewTreeFile(Paths.get(pwd), index.convertToMap());

        // get parent commit
        Integer parentHashInt = -1;
        if (!head.isEmpty()) {
            parentHashInt = head.getHead();
        }

        Integer curRevisionNumber = gitGraph.getNextRevisionNumber();
        CommitInfo curCommit = new CommitInfo(curRevisionNumber, hashOfTreeFileForRoot, parentHashInt, commitMessage);

        /* create new commitFile */
        hash = objects.createNewCommitFile(curCommit);
        gitGraph.addNewCommit(curRevisionNumber, index.convertToMap(), curCommit);

        /* change commit in head.txt */
        head.setHead(hash);
    }

    /**
     * This is default log.
     * It prints info of all commits.
     * @return List of CommitInfo. Each of them can be printed by printCommitInfo()
     */
    public List<CommitInfo> log() {

        List<CommitInfo> result = new ArrayList<>();


        for (Integer curRevisionNumber = gitGraph.getSize(); curRevisionNumber > 0; curRevisionNumber--) {
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
    public List<CommitInfo> log(final Integer fromRevision) {
        List<CommitInfo> result = new ArrayList<>();


        for (Integer curRevisionNumber = fromRevision; curRevisionNumber > 0; curRevisionNumber--) {
            result.add(gitGraph.findCommit(curRevisionNumber).get().getCommitInfo());
        }

        return result;
    }

    /**
     * Checkout.
     * @param revNumber - which commit should be downloaded.
     */
    public void checkout(Integer revNumber) {
        if (revNumber > gitGraph.getSize()) {
            System.out.println("git checkout: Incorrect revision number");
        }

        /* find node which contains info about commit */
        GitGraph.Node node = gitGraph.findCommit(revNumber).get();

        /* set node as current in git graph */
        gitGraph.setCur(node);

        /* update index file */
        index.IndexFromMap(node.getIndexInMap());

        /* update all files */
        Integer treeHash = node.getCommitInfo().getTreeHash();
        try {
            objects.invertTreeHashFile(treeHash, pwd);
        } catch (IOException e) {
            System.out.println("Checkout failed.");
        }
    }

    public void checkout(File fileToCheckout) {
        Map<String, Integer> indexInMap = index.convertToMap();

        if (indexInMap.containsKey(fileToCheckout.toString())) {
            Integer fileHash = indexInMap.get(fileToCheckout.toString());
            try {
                objects.invertBlobHashFile(fileToCheckout, fileHash);
            } catch (IOException e) {
                System.out.println("checkout failed.");
            }
        }
    }

    public void rm(File fileToDelete) {
        Map<String, Integer> indexInMap = index.convertToMap();

        if (indexInMap.containsKey(fileToDelete.toString())) {
            indexInMap.remove(fileToDelete.toString());
            index.IndexFromMap(indexInMap);
        }
    }

    public void status() {
        Map<String, Integer> indexInMap = index.convertToMap();

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
                    if (!objects.createNewBlobFile(file.toPath()).equals(indexInMap.get(file.toString()))) {
                        System.out.println("M " + file);
                    }
                }
            }
        }
    }

    // this is for testing
    public Integer getNumberOfObjectsFiles() {
        return objects.getNumberOfObjectsFiles();
    }

}
