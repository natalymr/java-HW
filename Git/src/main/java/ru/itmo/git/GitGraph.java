package ru.itmo.git;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

class GitGraph {
    private Node root;
    private Node cur;
    private final File gitGraphDir;
    private final Head head;
    private final ObjectsDir object;
    private final Index index;
    private Set<Integer> size;

    GitGraph(Path gitPath, Head head, ObjectsDir object, Index index) throws IOException {
        this.head = head;
        this.object = object;
        this.index = index;

        String gitGraphDirName = gitPath.toString() + File.separator + "gitGraph";
        gitGraphDir = new File(gitGraphDirName);

        if (!gitGraphDir.exists()) {
            gitGraphDir.mkdir();
            root = null;
            cur = null;
            size = new HashSet<>();
        } else {
            restoreGraph();
        }
    }

    private void restoreGraph() throws IOException {
        size = new HashSet<>();

        String headCommit = head.getHead();
        if (headCommit.equals("")) {
            return;
        }

        Node head = restoreBranch(headCommit);
        setCur(head);

        File[] files = gitGraphDir.listFiles();
        if (files != null && files.length > 1) {
            for (File file : files) {
                if (file.getName().equals(headCommit)) {
                    continue;
                }

                restoreBranch(file.getName());
            }
        }
    }

    private Node restoreBranch(String headHash) {
        List<CommitInfo> listOfCommits = new ArrayList<>();

        CommitInfo commitInfoHead = object.getCommitInfo(headHash);
        listOfCommits.add(commitInfoHead);

        while (true) {
            CommitInfo child = listOfCommits.get(listOfCommits.size() - 1);
            String parentHash = child.getParentHash();
            if (parentHash.equals("-1")) {

                break;
            } else {
                listOfCommits.add(object.getCommitInfo(parentHash));
            }
        }

        // работаем с листом коммитов, у которого в начале лежит наиболее ранний коммит
        Map<String, String> indexInMap = index.convertToMap();

        // сначала - корень графа
        int listSize = listOfCommits.size();
        CommitInfo parentCI = listOfCommits.get(listSize - 1);
        Node parent = new Node(null, parentCI.getRevisionNumber(), indexInMap, parentCI);
        size.add(parent.getRevisionNumber());
        setRoot(parent);

        for (int i = listSize - 2; i >= 0; i--) {
            CommitInfo nextCI = listOfCommits.get(i);

            if (!size.contains(nextCI.getRevisionNumber())) {
                Node next = new Node(parent, nextCI.getRevisionNumber(), indexInMap, nextCI);
                size.add(nextCI.getRevisionNumber());
                parent.addChildren(next);

                parent = next;
            }
        }

        return parent;
    }

    private Optional<Node> getRoot() {
        return Optional.ofNullable(root);
    }

    private void setRoot(Node newRoot) {
        root = newRoot;
    }

    private Optional<Node> getCur() {
        return Optional.ofNullable(cur);
    }

    void setCur(Node newCur) {
        cur = newCur;

        if (!getRoot().isPresent()) {
            root = cur;
        }
    }

    int getNextRevisionNumber() {
        return size.size() + 1;
    }

    int getSize() {
        return size.size();
    }

    void addNewCommit(int revNumber, Map<String, String> index, CommitInfo commit) {

        if (getRoot().isPresent() && getCur().isPresent()) {
            Node parent = getCur().get();
            Node newNode = new Node(parent, revNumber, index, commit);

            parent.addChildren(newNode);

            setCur(newNode);
        } else {
            Node newNode = new Node(null, revNumber, index, commit);
            commit.printCommitInfo();
            setRoot(newNode);
            setCur(newNode);
        }

        size.add(revNumber);
    }

    Optional<Node> findCommit(int revNumber) {
        if (!getRoot().isPresent())
            return Optional.empty();
        // use BFS
        Queue<Node> queue = new ArrayDeque<>();
        HashSet<Integer> visited = new HashSet<>();

        visited.add(getRoot().get().getRevisionNumber());
        queue.add(getRoot().get());

        while (!queue.isEmpty()) {

            Node curNode = queue.poll();

            if (curNode.getRevisionNumber() == revNumber) {
                return Optional.of(curNode);
            } else {
                for (Node child : curNode.getChildren()) {
                    if (!visited.contains(child.getRevisionNumber())) {

                        visited.add(child.getRevisionNumber());
                        queue.add(child);
                    }
                }
            }
        }

        return Optional.empty();
    }

    public void saveHeadOfBranch() throws IOException {
        String curHead = head.getHead();
        String newFileName = gitGraphDir.toString() + File.separator + curHead;
        File newFile = new File(newFileName);
        FileUtils.write(newFile, curHead, "UTF-8");
    }


    class Node {
        /* field: structure */
        private final Node parent;
        private final ArrayList<Node> children;

        /* field: info */
        private final int revisionNumber;
        private final Map<String, String> indexInMap;
        private final CommitInfo commitInfo;


        Node(Node parent, int revisionNumber, Map<String, String> indexInMap, CommitInfo commitInfo) {
            this.parent = parent;
            children = new ArrayList<>();

            this.revisionNumber = revisionNumber;
            this.indexInMap = indexInMap;
            this.commitInfo = commitInfo;
        }

        int getRevisionNumber() {
            return revisionNumber;
        }

        Map<String, String> getIndexInMap() {
            return indexInMap;
        }

        private ArrayList<Node> getChildren() {
            return children;
        }

        void addChildren(Node child) {
            children.add(child);
        }

        CommitInfo getCommitInfo() {
            return commitInfo;
        }
    }


}
