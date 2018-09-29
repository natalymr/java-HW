package ru.itmo.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommitInfo {
    private final Integer revisionNumber;
    private final Integer treeHash;
    private final Integer parentHash;
    private final String data;
    private final String commitMessage;

    CommitInfo(Integer revNumber, Integer tr, Integer parHash, String comMessage) {
        revisionNumber = revNumber;
        treeHash = tr;
        parentHash = parHash;
        Date now = new Date();
        data = now.toString();
        commitMessage = comMessage;
    }

//    CommitInfo(File fileName) {
//        // convert commit file in map
//        Map<String, String> fileContentInMap = new HashMap<>();
//
//        try (Scanner scanner = new Scanner(fileName)) {
//            while (scanner.hasNext()) {
//                String key = scanner.next();
//                String value = scanner.next();
//
//                fileContentInMap.put(key, value);
//            }
//
//        } catch (FileNotFoundException e) {
//            System.err.println("CommitInfoConstructor. Failed to read commitInfoFile");
//            e.printStackTrace();
//        }
//
//        /* fill class fields from map */
//        revisionNumber = Integer.parseInt(fileContentInMap.get("COMMIT#"));
//        treeHash = Integer.parseInt(fileContentInMap.get("tree"));
//        parentHash = Integer.parseInt(fileContentInMap.get("parent"));
//        data = fileContentInMap.get("data");
//        commitMessage = fileContentInMap.get("message");
//    }

    public Integer getRevisionNumber() {
        return revisionNumber;
    }

    public String getCommitInfo() {

        return String.format("%-8s %d\n", "COMMIT#", revisionNumber) +
                String.format("%-8s %s\n", "tree", Integer.toString(treeHash)) +
                String.format("%-8s %d\n", "parent", parentHash) +
                String.format("%-8s %s\n", "data", data) +
                String.format("%-8s %s\n", "message", commitMessage);
    }

    public Integer getTreeHash() {
        return treeHash;
    }

    public void printCommitInfo() {
        System.out.println(this.getCommitInfo());
    }
}
