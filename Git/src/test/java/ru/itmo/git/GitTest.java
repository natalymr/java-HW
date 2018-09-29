package ru.itmo.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.*;

public class GitTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testInit() {
        Git git = new Git(folder.getRoot());

        git.init();

        // check .git repo
        File gitDir = new File(folder.getRoot(), ".git");
        assertThat(gitDir).exists();
        assertThat(gitDir).isDirectory();

        // check .git/objects repo
        File objectsDir = new File(folder.getRoot(), ".git" + File.separator + "objects");
        assertThat(objectsDir).exists();
        assertThat(objectsDir).isDirectory();

        // check .git/index.txt
        File indexFile = new File(folder.getRoot(), ".git" + File.separator + "index.txt");
        assertThat(indexFile).exists();
        assertThat(indexFile).isFile();
        assertThat(indexFile).hasContent("");

        // check .git/HEAD.txt
        File HEADFile = new File(folder.getRoot(), ".git" + File.separator + "HEAD.txt");
        assertThat(HEADFile).exists();
        assertThat(HEADFile).isFile();
        assertThat(HEADFile).hasContent("");
    }

    @Test
    public void testAddOneFileInRootDir() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContent = "zzz";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);

            git.add(file.toPath());

            // check there is at least one file in objects
            File objectsDir = new File(folder.getRoot(), ".git" + File.separator + "objects");
            List<File> objectsContains = (List<File>)FileUtils.listFiles(objectsDir,
                    TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            assertThat(objectsContains).isNotEmpty();
            assertThat(objectsContains.size()).isEqualTo(1);

            // check that this file is blob and it contains "zzz"
            File blobFile = objectsContains.get(0);
            try (Scanner scanner = new Scanner(blobFile)) {
                String blobFlag = scanner.nextLine();
                String blobFileContent = scanner.nextLine();

                assertThat(blobFlag).isEqualTo("BLOB");
                assertThat(blobFileContent).isEqualTo(fileContent);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // check that there is info about this file in index.txt
            String hashOfFileExpected = blobFile.getName();
            hashOfFileExpected = hashOfFileExpected.substring(0,hashOfFileExpected.length() - 4);
            String fileNameInIndexExpected = folder.getRoot().toPath().resolve(fileName).toString();
            File indexFile = folder.getRoot().toPath().resolve(".git").resolve("index.txt").toFile();
            try (Scanner scanner = new Scanner(indexFile)) {
                String fileNameInIndex = scanner.next();
                String hashFileContentInIndex = scanner.next();

                assertThat(fileNameInIndex).isEqualTo(fileNameInIndexExpected);
                assertThat(hashFileContentInIndex).isEqualTo(hashOfFileExpected);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLog() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContentFirst = "zzz";
        String fileContentSecond = "aaa";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);

            git.add(file.toPath());

            git.commit("First commit!");

            FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);

            git.add(file.toPath());

            git.commit("Second commit!");

            List<CommitInfo> logBoth = git.log();

            for(CommitInfo curCommit : logBoth) {
                curCommit.printCommitInfo();
            }


        } catch (IOException e) {
            System.err.println("TestLog. Failed to write to temporary file");
        }
    }

    @Test
    public void testLogFromRevision() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContentFirst = "zzz";
        String fileContentSecond = "aaa";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);

            git.add(file.toPath());

            git.commit("First commit!");

            FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);

            git.add(file.toPath());

            git.commit("Second commit!");

            List<CommitInfo> logBoth = git.log();
            CommitInfo firstF = null;

            for(CommitInfo curCommit : logBoth) {
                curCommit.printCommitInfo();
                if (curCommit.getRevisionNumber() == 1) {
                    firstF = curCommit;
                }
            }

            List<CommitInfo> logSingle = git.log(1);
            CommitInfo firstS = null;

            for(CommitInfo curCommit : logSingle) {
                curCommit.printCommitInfo();
                if (curCommit.getRevisionNumber() == 1) {
                    firstS = curCommit;
                }
            }

            if (firstF != null && firstS != null) {
                assertThat(firstF.getCommitInfo()).isEqualTo(firstS.getCommitInfo());
            }

        } catch (IOException e) {
            System.err.println("TestLog. Failed to write to temporary file");
        }
    }

    @Test
    public void testCheckoutRevisionChangeFileContent() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContentFirst = "zzz";
        String fileContentSecond = "aaa";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);

            git.add(file.toPath());

            git.commit("First commit!");


            FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);

            git.add(file.toPath());

            git.commit("Second commit!");

            git.checkout(1);
            try (Scanner scanner = new Scanner(file)) {
                String fileContentActual = scanner.nextLine();

                assertThat(fileContentActual).isEqualTo(fileContentFirst);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Test. testNumberOfFilesForAddAndCommitAndChange");
        }
    }

    @Test
    public void testCheckoutRevisionAddNewFiles() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileNameFirst = "file1.txt";
        String fileNameSecond = "file2.txt";
        String fileContent = "zzz";

        try {
            File fileF = folder.newFile(fileNameFirst);
            FileUtils.writeStringToFile(fileF, fileContent, "UTF-8", true);

            git.add(fileF.toPath());
            git.commit("First commit!");

            File fileS = folder.newFile(fileNameSecond);
            FileUtils.writeStringToFile(fileS, fileContent, "UTF-8", true);

            git.add(fileS.toPath());
            git.commit("Second commit!");

            /* delete second file */
            git.checkout(1);

            assertThat(fileS).doesNotExist();

        } catch (IOException e) {
            System.err.println("Test. testNumberOfFilesForAddAndCommitAndChange");
        }
    }

    @Test
    public void testCheckoutFile() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContentFirst = "zzz";
        String fileContentSecond = "aaa";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);

            git.add(file.toPath());
            git.commit("First commit!");

            FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);

            /* delete changes in file */
            git.checkout(file);

            assertThat(file).hasContent(fileContentFirst);

        } catch (IOException e) {
            System.err.println("Test. testNumberOfFilesForAddAndCommitAndChange");
        }
    }

    @Test
    public void testRm() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContentFirst = "zzz";
        String fileContentSecond = "aaa";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);

            git.add(file.toPath());
            git.commit("First commit! Add file.txt");

            git.rm(file);
            assertThat(file).exists();

            git.commit("Second commit! Delete file.txt");

            FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", false);

            /* delete changes in file */
            git.checkout(1);

            assertThat(file).hasContent(fileContentFirst);
        } catch (IOException e) {
            System.err.println("Test. testNumberOfFilesForAddAndCommitAndChange");
        }
    }

    @Test
    public void testStatus() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContentFirst = "zzz";
        String fileContentSecond = "aaa";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);

            git.status();
            git.add(file.toPath());
            FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", false);
            git.status();

            git.commit("First commit!");

            git.status();
            git.add(file.toPath());
            git.commit("Second commit! Delete file.txt");



            /* delete changes in file */
            git.checkout(1);

            assertThat(file).hasContent(fileContentSecond);
        } catch (IOException e) {
            System.err.println("Test. testNumberOfFilesForAddAndCommitAndChange");
        }
    }

}