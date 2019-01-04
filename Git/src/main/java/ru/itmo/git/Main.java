package ru.itmo.git;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final File pwd = Paths.get("").toAbsolutePath().toFile();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("mygit: enter your command");
            return;
        }

        String command = args[0];

        if (command.equals("init")) {
            System.out.println("INIT");

            try {
                Git mygit = new Git(pwd);
                mygit.init();
                System.out.println("init success");
            } catch (FileNotFoundException e) {
                System.out.println("mygit: failed to init mygit");
            }

            return;
        }

        Git mygit;

        try {
            mygit = new Git(pwd);
            mygit.open();
        } catch (FileNotFoundException e) {
            checkPWD();
            return;
        }

        switch (command) {
            case "add":
                System.out.println("ADD");
                System.out.println(mygit);

                String fileName = args[1];
                String fullFileName = pwd.toString() + File.separator + fileName;
                System.out.println(fullFileName);
                File fileToAdd = new File(fullFileName);

                try {
                    mygit.add(fileToAdd.toPath());
                    System.out.println("add success");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    System.out.println("mygit: cannot add file " + fileName);
                }

                break;

            case "commit":
                System.out.println("COMMIT");

                String[] commitMessageInArray = Arrays.stream(args)
                        .filter(e -> !e.equals("mygit") && !e.equals("commit")).toArray(String[]::new);
                String commitMessage = String.join(" ", commitMessageInArray);

                mygit.commit(commitMessage);
                System.out.println("commit success");

                break;

            case "log":
                System.out.println("LOG");
                List<CommitInfo> log;

                System.out.println("log args = " + args.length);

                if (args.length == 1) {
                    System.out.println("log.if");
                    log = mygit.log();
                    System.out.println("log_size = " + log.size());
                } else {
                    int revisionNumber = Integer.parseInt(args[1]);
                    log = mygit.log(revisionNumber);
                }

                System.out.println("log_size = " + log.size());
                for (CommitInfo commitInfo : log) {
                    commitInfo.printCommitInfo();
                }

                break;

            case "checkout":
                System.out.println("CHECKOUT");

                try {
                    int revisionNumber = Integer.parseInt(args[1]);
                    mygit.checkout(revisionNumber);
                } catch (NumberFormatException e){
                    String fileToCheckout = args[1];
                    mygit.checkout(new File(fileToCheckout));
                }

                break;

            case "rm":
                System.out.println("RM");

                String fileToRm = args[1];
                mygit.rm(new File(fileToRm));

                break;

            case "status":
                System.out.println("STATUS");

                mygit.status();

                break;
            default:
                System.out.println("mygit: " + command + " not mygit command");
        }
    }

    private static void checkPWD() {
        File gitDir = new File(pwd.toString() + File.separator + ".git");

        if (!gitDir.exists() || !gitDir.isDirectory()) {
            System.out.println("mygit: not mygit repository");
        } else {
            System.out.println("mygit: at first use command 'mygit open'");
        }
    }
}
