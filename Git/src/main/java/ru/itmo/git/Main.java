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
            try {
                Git mygit = new Git(pwd);
                mygit.init();
            } catch (FileNotFoundException ignored) {
                System.out.println("mygit: failed to init mygit");
            }

            return;
        }

        Git mygit;

        try {
            mygit = new Git(pwd);
            mygit.open();
        } catch (FileNotFoundException ignored) {
            checkPWD();
            return;
        }

        switch (command) {
            case "add":
                if (argsAreMissed(args)) {
                    break;
                }

                String fileName = args[1];
                String fullFileName = pwd.toString() + File.separator + fileName;
                File fileToAdd = new File(fullFileName);

                try {
                    mygit.add(fileToAdd.toPath());
                } catch (IOException ignored) {
                    System.out.println("mygit: can not add file " + fileName);
                }

                break;

            case "commit":
                String[] commitMessageInArray = Arrays.stream(args)
                        .filter(e -> !e.equals("mygit") && !e.equals("commit")).toArray(String[]::new);
                String commitMessage = String.join(" ", commitMessageInArray);

                try {
                    mygit.commit(commitMessage);
                } catch (IOException ignored) {
                    System.out.println("mygit: can not commit");
                }

                break;

            case "log":
                List<CommitInfo> log;

                if (args.length == 1) {
                    log = mygit.log();
                } else {
                    int revisionNumber = Integer.parseInt(args[1]);
                    log = mygit.log(revisionNumber);
                }

                for (CommitInfo commitInfo : log) {
                    commitInfo.printCommitInfo();
                }

                break;

            case "checkout":
                if (argsAreMissed(args)) {
                    break;
                }

                try {
                    try {
                        int revisionNumber = Integer.parseInt(args[1]);
                        mygit.checkout(revisionNumber);
                    } catch (NumberFormatException ignored){
                        String fileToCheckout = args[1];
                        mygit.checkout(new File(fileToCheckout));
                    }
                } catch (IOException ignored) {
                    System.out.println("mygit: can not checkout");
                }

                break;

            case "rm":
                if (argsAreMissed(args)) {
                    break;
                }

                String fileToRm = args[1];

                mygit.rm(new File(fileToRm));
                break;

            case "status":
                try {
                    mygit.status();
                } catch (IOException ignored) {
                    System.out.println("mygit: can not status");
                }
                break;
            case "reset":
                try {
                    mygit.reset();
                } catch (IOException ignored) {
                    System.out.println("mygit: can not reset");
                }
                break;
            default:
                System.out.println("mygit: " + command + " not mygit command");
        }
    }

    private static boolean argsAreMissed(String[] args) {
        if (args.length == 1) {
            System.out.println("You forget your arguments");
            return true;
        }

        return false;
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
