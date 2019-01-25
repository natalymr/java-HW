package ru.itmo.git.fileSystem;

import java.io.Serializable;

public class Head implements Serializable {
    private int headCommitRevision;

    public void setHeadCommitRevision(int headCommitRevision) {
        this.headCommitRevision = headCommitRevision;
    }
}
