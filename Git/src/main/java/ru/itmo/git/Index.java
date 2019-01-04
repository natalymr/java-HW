package ru.itmo.git;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

class Index {
    /* index's file */
    private File indexFile;

    /* removing magic constant */
    private final String ENCODING = "UTF-8";

    /**
     * Constructor
     * @param pathToGit - where .git/index.txt file will be created
     */
    Index(Path pathToGit) {
        String indexFileName = pathToGit.toString() + File.separator + "index.txt";
        indexFile = new File(indexFileName);

        if (!indexFile.exists()) {
            try {
                indexFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Index: Constructor. Failed to create index file.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructor
     * @param oldIndexFile - it was git repo before and it was its index file
     */
    Index(File oldIndexFile) {
        indexFile = oldIndexFile;
    }

    void IndexFromMap(Map<String, String> indexInMap) {
        StringBuilder indexFileContent = new StringBuilder();

        for (Object fileName : indexInMap.keySet()) {
            String fileHash = indexInMap.get(fileName);

            indexFileContent.append(fileName).append(" ").append(fileHash);
        }

        try {
            FileUtils.writeStringToFile(indexFile, indexFileContent.toString(), ENCODING, false);
        } catch (IOException e) {
            System.err.println("Index: Constructor. Failed to write to index.txt");
        }
    }

    // other methods
    Map<String, String> convertToMap() {
        Map<String, String> result = new HashMap<>();

        try (Scanner scanner = new Scanner(indexFile)) {
            while (scanner.hasNext()) {
                String[] line = scanner.nextLine().split(" ");

                String key = "";
                String value = "";

                if (line.length == 2) {
                    key = line[0];
                    value = line[1];
                } else if (line.length == 1) {
                    key = line[0];
                    value = "";
                }

                result.put(key, value);
            }

        } catch (FileNotFoundException e) {
            System.err.println("Index: convertToMap. Failed to read index.txt");
            e.printStackTrace();
        }

        return result;
    }

    void replaceHash(String oldHash, String newHash) {

        try {
            String content = FileUtils.readFileToString(indexFile, ENCODING);
            content = content.replaceAll(oldHash, newHash);
            FileUtils.writeStringToFile(indexFile, content, ENCODING, false);
        } catch (IOException e) {
            System.err.println("Index: replaceHash. Failed to read or write to index.txt.");
            e.printStackTrace();
        }
    }

    void addNewFileForChecking(String fileName) throws IOException {
        String content = FileUtils.readFileToString(indexFile, ENCODING);
        if (!content.contains(fileName)) {
            addNewFile(fileName, "\n");
        }
    }

    void addNewFile(String fileName, String fileHash) throws IOException {
        FileUtils.writeStringToFile(indexFile,fileName + " " + fileHash + "\n", ENCODING,true);
    }

    public void replaceEmptyHash(String fileNameInString, String hashInString) throws IOException {
        String content = FileUtils.readFileToString(indexFile, ENCODING);
        content = content.replaceAll(fileNameInString + " \\n", fileNameInString + " " + hashInString + "\n");
        FileUtils.writeStringToFile(indexFile, content, ENCODING, false);
    }
}
