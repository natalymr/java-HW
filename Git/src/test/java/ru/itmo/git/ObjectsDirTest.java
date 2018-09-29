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

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectsDirTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testBlobFile() {

        Git git = new Git(folder.getRoot());

        git.init();

        String fileName = "file.txt";
        String fileContent = "zzz";

        try {
            File file = folder.newFile(fileName);
            FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);

            git.add(file.toPath());

            File objectsDir = folder.getRoot().toPath().resolve(".git").resolve("objects").toFile();
            List<File> objectsContains = (List<File>)FileUtils.listFiles(objectsDir,
                    TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            for (File f : objectsContains) {
                try (Scanner scanner = new Scanner(f)) {
                    String label = scanner.nextLine();
                    if (label.equals("BLOB")) {
                        String fileContentActual = scanner.nextLine();
                        String fileContentExpected = "zzz";
                        assertThat(fileContentActual).isEqualTo(fileContentExpected);
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
                    String label = scanner.nextLine();
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
                    String label = scanner.nextLine();
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
    public void testNumberOfFilesForAddAndCommitAndChange() {
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

            // check that in objects dir there are only:
            // 1 blob file
            // 1 tree file
            // 1 commit file
            assertThat(git.getNumberOfObjectsFiles()).isEqualTo(3);

            FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);

            git.add(file.toPath());

            git.commit("Second commit!");

            // NOW
            // check that in objects dir there are:
            // 2 blob file
            // 2 tree file
            // 2 commit file
            assertThat(git.getNumberOfObjectsFiles()).isEqualTo(6);
        } catch (IOException e) {
            System.err.println("Test. testNumberOfFilesForAddAndCommitAndChange");
        }
    }
}