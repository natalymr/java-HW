package ru.itmo.git;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Index {

    // field
    private File indexFile;

    // constructor
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

    public void IndexFromMap(Map<String, Integer> indexInMap) {
        StringBuilder indexFileContent = new StringBuilder();

        for (Object fileName : indexInMap.keySet()) {
            Integer fileHash = indexInMap.get(fileName);

            indexFileContent.append(fileName).append(" ").append(Integer.toString(fileHash));
        }

        try {
            FileUtils.writeStringToFile(indexFile, indexFileContent.toString(), "UTF-8", false);
        } catch (IOException e) {
            System.err.println("Index: Constructor. Failed to write to index.txt");
        }
    }

    // other methods
    public Map<String, Integer> convertToMap() {
        Map<String, Integer> result = new HashMap<>();

        try (Scanner scanner = new Scanner(indexFile)) {
            while (scanner.hasNext()) {
                String key = scanner.next();
                String value = scanner.next();

              result.put(key, Integer.parseInt(value));
            }

        } catch (FileNotFoundException e) {
            System.err.println("Index: convertToMap. Failed to read index.txt");
            e.printStackTrace();
        }

        return result;
    }

    public void replaceHash(int oldHash, int newHash) {

        try {
            String content = FileUtils.readFileToString(indexFile, "UTF-8");
            content = content.replaceAll(Integer.toString(oldHash), Integer.toString(newHash));
            FileUtils.writeStringToFile(indexFile, content, "UTF-8", false);
        } catch (IOException e) {
            System.err.println("Index: replaceHash. Failed to read or write to index.txt.");
            e.printStackTrace();
        }
    }

    public void addNewFile(String fileName, int fileHash) {

        try {
            FileUtils.writeStringToFile(indexFile,
                    fileName + " " + Integer.toString(fileHash) + "\n",
                    "UTF-8",
                    true);
        } catch (IOException e) {
            System.err.println("Index: addNewFile. Failed to write to index.txt.");
            e.printStackTrace();
        }
    }
}
