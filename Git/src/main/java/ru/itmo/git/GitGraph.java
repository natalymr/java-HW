package ru.itmo.git;

import java.util.*;

class GitGraph {
    private Optional<Node> root;
    private Optional<Node> cur;
    private Integer size;

    GitGraph() {
        root = Optional.empty();
        cur = Optional.empty();
        size = 0;
    }

    public Integer getNextRevisionNumber() {
        return size + 1;
    }

    public Integer getSize() {
        return size;
    }

    public void addNewCommit(Integer revNumber, Map<String, Integer> index, CommitInfo commit) {
        if (root.isPresent()) {

            Node parent = cur.get();
            Node newNode = new Node(parent, revNumber, index, commit);

            parent.addChildren(newNode);

            cur = Optional.of(newNode);
        } else {
            Node newNode = new Node(null, revNumber, index, commit);

            root = cur = Optional.of(newNode);
        }

        size++;
    }

    public Optional<Node> findCommit(Integer revNumber) {

        if (!root.isPresent())
            return Optional.empty();
        // use BFS
        Queue<Node> queue = new ArrayDeque<>();
        HashSet<Integer> visited = new HashSet<>();

        visited.add(root.get().getRevisionNumber());
        queue.add(root.get());

        while (!queue.isEmpty()) {

            Node curNode = queue.poll();


            if (curNode.getRevisionNumber().equals(revNumber)) {
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

    public void setCur(Node newCur) {
        if (root.isPresent()) {
            cur = Optional.of(newCur);
        } else {
            root = cur = Optional.of(newCur);
        }
    }

    class Node {
        /* field: structure */
        private final Node parent;
        private final ArrayList<Node> children;

        /* field: info */
        private final Integer revisionNumber;
        private final Map<String, Integer> indexInMap;
        private final CommitInfo commitInfo;


        Node(Node par, Integer revNumber, Map<String, Integer> index, CommitInfo comInfo) {
            parent = par;
            children = new ArrayList<>();

            revisionNumber = revNumber;
            indexInMap = index;
            commitInfo = comInfo;
        }

        Integer getRevisionNumber() {
            return revisionNumber;
        }

        public Map<String, Integer> getIndexInMap() {
            return indexInMap;
        }

        private ArrayList<Node> getChildren() {
            return children;
        }

        void addChildren(Node child) {
            children.add(child);
        }

        public CommitInfo getCommitInfo() {
            return commitInfo;
        }
    }


}
