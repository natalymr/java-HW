package ru.itmo.git;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

class Head {

    private File HeadFile;

    Head(Path pathToGit) {
        String indexFileName = pathToGit.toString() + File.separator + "HEAD.txt";
        HeadFile = new File(indexFileName);

        if (!HeadFile.exists()) {
            try {
                HeadFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Index constructor. Failed to create index file.");
                e.printStackTrace();
            }
        }
    }

    public void setHead(Integer newHash) {
        try {
            FileUtils.writeStringToFile(HeadFile, Integer.toString(newHash), "UTF-8", false);
        } catch (IOException e) {
            System.err.println("Head. changeHead. Failed to write to HEAD.txt.");
            e.printStackTrace();

        }

    }

    public Integer getHead() {
        String result = "-1";
        try {
            result = FileUtils.readFileToString(HeadFile, "UTF-8");
        } catch (IOException e) {
            System.err.println("Head. getHead. Failed to read HEAD.txt.");
            e.printStackTrace();
        }

        return Integer.parseInt(result);
    }

    public boolean isEmpty() {
        return HeadFile == null || HeadFile.length() == 0;
    }
}
