package ru.itmo.git.branch;

import ru.itmo.git.fileSystem.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {
    private final String branch;
    private final int commitRevision;
    private final Node parent;
    private final Index index;
    private final List<Node> children;

    Node(String branch, int commitRevision, Node parent, Index index) {
        this.branch = branch;
        this.commitRevision = commitRevision;
        this.parent = parent;
        this.index = index;
        children = new ArrayList<>();
    }

    public int getCommitRevision() {
        return commitRevision;
    }

    public Index getIndex() {
        return index;
    }

    public Node getParent() {
        return parent;
    }

    void addChild(Node node) {
        children.add(node);
    }
}
