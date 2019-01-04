package ru.itmo.git;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

class Head {
    /* head's file */
    private File headFile;

    /* removing magic constant */
    private final String ENCODING = "UTF-8";

    /**
     * Constructor
     * @param pathToGit - where .git/HEAD.txt file will be created
     */
    Head(Path pathToGit) {
        String indexFileName = pathToGit.toString() + File.separator + "HEAD.txt";
        headFile = new File(indexFileName);

        if (!headFile.exists()) {
            try {
                headFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Index constructor. Failed to create index file.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructor
     * @param oldHeadFile - it was git repo before and it was its head file
     */
    Head(File oldHeadFile) {
        headFile = oldHeadFile;
    }

    void setHead(String newHash) {
        try {
            FileUtils.writeStringToFile(headFile, newHash, ENCODING, false);
        } catch (IOException e) {
            System.err.println("Head. changeHead. Failed to write to HEAD.txt.");
            e.printStackTrace();

        }

    }

    String getHead() throws IOException {
        return FileUtils.readFileToString(headFile, ENCODING);
    }

    boolean isEmpty() {
        return headFile == null || headFile.length() == 0;
    }
}
