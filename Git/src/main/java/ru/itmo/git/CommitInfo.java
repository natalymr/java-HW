package ru.itmo.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class CommitInfo {
    private final int revisionNumber;
    private final String treeHash;
    private final String parentHash;
    private final String data;
    private final String commitMessage;

    CommitInfo(int revNumber, String hashTree, String parHash, String comMessage) {
        revisionNumber = revNumber;
        treeHash = hashTree;
        parentHash = parHash;
        Date now = new Date();
        data = now.toString();
        commitMessage = comMessage;
    }

    CommitInfo(File fileName) {
        // convert commit file in map
        Map<String, String> fileContentInMap = new HashMap<>();

        try (Scanner scanner = new Scanner(fileName)) {
            while (scanner.hasNext()) {
                String linee = scanner.nextLine();
                if (linee.equals("COMMIT")) {
                    continue;
                }
                String[] line = linee.split(" ");
                String key = line[0];
                String value = line[line.length - 1];


                fileContentInMap.put(key, value);
            }

        } catch (FileNotFoundException e) {
            System.err.println("CommitInfoConstructor. Failed to read commitInfoFile");
            e.printStackTrace();
        }

        /* fill class fields from map */
        revisionNumber = Integer.parseInt(fileContentInMap.get("COMMIT#"));
        treeHash = fileContentInMap.get("tree");
        parentHash = fileContentInMap.get("parent");
        data = fileContentInMap.get("data");
        commitMessage = fileContentInMap.get("message");
    }

    int getRevisionNumber() {
        return revisionNumber;
    }

    String getParentHash() {
        return parentHash;
    }

    String getCommitInfo() {

        return String.format("%-8s %d\n", "COMMIT#", revisionNumber) +
                String.format("%-8s %s\n", "tree", treeHash) +
                String.format("%-8s %s\n", "parent", parentHash) +
                String.format("%-8s %s\n", "data", data) +
                String.format("%-8s %s\n", "message", commitMessage);
    }

    String getTreeHash() {
        return treeHash;
    }

    void printCommitInfo() {
        System.out.println(getCommitInfo());
    }
}
