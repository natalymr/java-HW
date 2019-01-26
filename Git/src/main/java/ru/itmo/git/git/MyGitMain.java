package ru.itmo.git.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyGitMain {

    private static final File pwd = Paths.get("").toAbsolutePath().toFile();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length == 0) {
            System.out.println("mygit: enter your command");
            return;
        }

        String command = args[0];

        if (command.equals("init")) {
            MyGit mygit = new MyGit(pwd);
            mygit.init();

            mygit.close();
            return;
        }

        MyGit mygit;

        try {
            mygit = new MyGit(pwd);
            mygit.open();
        } catch (FileNotFoundException ignored) {
            checkPWD();
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        switch (command) {
            case "add": {
                if (argsAreMissed(args)) {
                    break;
                }

                mygit.add(parseFilesInArgs(args));

                mygit.close();
                break;
            }

            case "commit": {
                String[] commitMessageInArray = Arrays.stream(args)
                    .filter(e -> !e.equals("mygit") && !e.equals("commit")).toArray(String[]::new);
                String commitMessage = String.join(" ", commitMessageInArray);

                try {
                    mygit.commit(commitMessage);
                } catch (IOException ignored) {
                    System.out.println("mygit: can not commit");
                }

                mygit.close();
                break;
            }
            case "log": {
                if (args.length == 1) {
                    mygit.log();
                } else {
                    int revisionNumber = Integer.parseInt(args[1]);
                    mygit.log(revisionNumber);
                }

                break;
            }
            case "checkout": {
                if (argsAreMissed(args)) {
                    break;
                }

                try {
                    int revisionNumber = Integer.parseInt(args[1]);
                    mygit.checkout(revisionNumber);
                } catch (NumberFormatException ignored) {

                    if (args[1].equals("-b")) {
                        mygit.createNewBranch(args[2]);
                    } else {
                        List<File> filesArguments = parseFilesInArgs(args);

                        if (filesArguments.size() == 1 && !filesArguments.get(0).exists()) {
                            mygit.switchToBranch(args[1]);
                        } else {
                            mygit.checkout(filesArguments);
                        }
                    }
                }

                mygit.close();
                break;
            }
            case "rm": {
                if (argsAreMissed(args)) {
                    break;
                }

                mygit.rm(parseFilesInArgs(args));

                mygit.close();
                break;
            }
            case "status": {
                mygit.status();

                break;
            }
            case "reset": {
                if (args.length == 1) {
                    mygit.reset();
                } else {
                    int revisionNumber = Integer.parseInt(args[1]);
                    mygit.reset(revisionNumber);
                }

                mygit.close();
                break;
            }
            case "branch": {
                mygit.currentBranchName();
                break;
            }
            case "merge": {
                mygit.mergeWithBranch(args[1]);
                mygit.close();
                break;
            }
            default:
                System.out.println("mygit: " + command + " not mygit command");
        }
    }

    private static List<File> parseFilesInArgs(String[] args) {
        List<File> result = new ArrayList<>();

        for (int i = 1; i < args.length; i++) {
            File file = new File(pwd.toString() + File.separator + args[i]);
            
            result.add(file);
        }

        return result;
    }

    private static boolean argsAreMissed(String[] args) {
        if (args.length == 1) {
            System.out.println("mygit: you forgot your arguments");
            return true;
        }

        return false;
    }

    private static void checkPWD() {
        File gitDir = new File(pwd.toString() + File.separator + ".mygit");

        if (!gitDir.exists() || !gitDir.isDirectory()) {
            System.out.println("mygit: not mygit repository");
        }
    }
}
