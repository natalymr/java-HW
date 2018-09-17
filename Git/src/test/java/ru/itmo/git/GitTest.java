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
    public TemporaryFolder folder = new TemporaryFolder();

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
            String hashOfFileExpected = blobFile.getName().toString();
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
    public void testMatchHashingOfFiles() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContent = "zzz";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);

            git.add(file.toPath());

            git.commit("First commit!");

            File objectsDir = folder.getRoot().toPath().resolve(".git").resolve("objects").toFile();
            List<File> objectsContains = (List<File>) FileUtils.listFiles(objectsDir,
                    TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            // check that in objects only:
            // 1 blob file
            // 1 tree file
            // 1 commit file
            assertThat(objectsContains.size()).isEqualTo(3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTreeFile() {
        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContent = "zzz";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);

            git.add(file.toPath());

            git.commit("First commit!");

            File objectsDir = folder.getRoot().toPath().resolve(".git").resolve("objects").toFile();
            List<File> objectsContains = (List<File>)FileUtils.listFiles(objectsDir,
                    TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            for (File f : objectsContains) {
                try (Scanner scanner = new Scanner(f)) {
                    String label = scanner.next();
                    if (label.equals("TREE")) {
                        String blobLabel = scanner.next();
                        String blobFileName = scanner.next();
                        String blobFileHash = scanner.next();

                        assertThat(blobLabel).isEqualTo("blob");
                        assertThat(blobFileName).isEqualTo(file.toString());

                        blobFileHash += ".txt";
                        File blobFileHashAbsolutePath = folder.getRoot().toPath()
                                .resolve(".git").resolve("objects").resolve(blobFileHash).toFile();
                        assertThat(objectsContains).contains(blobFileHashAbsolutePath);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCommitFile() {

        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContent = "zzz";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);

            git.add(file.toPath());

            git.commit("First commit!");

            File objectsDir = folder.getRoot().toPath().resolve(".git").resolve("objects").toFile();
            List<File> objectsContains = (List<File>)FileUtils.listFiles(objectsDir,
                    TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            for (File f : objectsContains) {
                try (Scanner scanner = new Scanner(f)) {
                    String label = scanner.next();
                    if (label.equals("COMMIT")) {
                        String cRevNumberActual = scanner.nextLine();
                        String cRevNumberExpected = "COMMIT# 1";
                        assertThat(cRevNumberActual).isEqualTo(cRevNumberExpected);

                        scanner.nextLine(); //tree
                        String cParentActual = scanner.nextLine(); // parent
                        String cParentExpected = "parent -1";
                        assertThat(cParentActual).isEqualTo(cParentExpected);

                        scanner.nextLine(); // date
                        String cMsgActual = scanner.nextLine();
                        String cMsgExpected = "message First commit!";
                        assertThat(cMsgActual).isEqualTo(cMsgExpected);
                        System.out.println("here");
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}