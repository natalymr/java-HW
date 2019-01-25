//package ru.itmo.git;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.filefilter.TrueFileFilter;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.TemporaryFolder;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.List;
//import java.util.Scanner;
//
//import static org.assertj.core.api.Assertions.*;
//
//public class MyGitTest {
//
//    @Rule
//    public final TemporaryFolder folder = new TemporaryFolder();
//
//    @Test
//    public void testInit() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        // check .mygit repo
//        File gitDir = new File(folder.getRoot(), ".mygit");
//        assertThat(gitDir).exists();
//        assertThat(gitDir).isDirectory();
//
//        // check .mygit/objects repo
//        File objectsDir = new File(folder.getRoot(), ".mygit" + File.separator + "objects");
//        assertThat(objectsDir).exists();
//        assertThat(objectsDir).isDirectory();
//
//        // check .mygit/index.txt
//        File indexFile = new File(folder.getRoot(), ".mygit" + File.separator + "index.txt");
//        assertThat(indexFile).exists();
//        assertThat(indexFile).isFile();
//        assertThat(indexFile).hasContent("");
//
//        // check .mygit/HEAD.txt
//        File HEADFile = new File(folder.getRoot(), ".mygit" + File.separator + "HEAD.txt");
//        assertThat(HEADFile).exists();
//        assertThat(HEADFile).isFile();
//        assertThat(HEADFile).hasContent("");
//    }
//
//    @Test
//    public void testAddOneFileInRootDir() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContent = "zzz";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);
//
//
//        git.add(file.toPath());
//
//        // check there is at least one file in objects
//        File objectsDir = new File(folder.getRoot(), ".mygit" + File.separator + "objects");
//        List<File> objectsContains = (List<File>)FileUtils.listFiles(objectsDir,
//                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
//
//        assertThat(objectsContains).isNotEmpty();
//        assertThat(objectsContains.size()).isEqualTo(1);
//
//        // check that this file is blob and it contains "zzz"
//        File blobFile = objectsContains.get(0);
//        String blobFileContent = FileUtils.readFileToString(blobFile, "UTF-8");
//        assertThat(blobFileContent).isEqualTo(fileContent);
//
//
//        // check that there is info about this file in index.txt
//        String hashOfFileExpected = blobFile.getName();
//        hashOfFileExpected = hashOfFileExpected.substring(0,hashOfFileExpected.length() - 4);
//        String fileNameInIndexExpected = folder.getRoot().toPath().resolve(fileName).toString();
//        File indexFile = folder.getRoot().toPath().resolve(".mygit").resolve("index.txt").toFile();
//        try (Scanner scanner = new Scanner(indexFile)) {
//            String[] line = scanner.nextLine().split(" ");
//            if (line.length == 2) {
//                String fileNameInIndex = line[0];
//                String hashFileContentInIndex = line[1];
//
//                assertThat(fileNameInIndex).isEqualTo(fileNameInIndexExpected);
//                assertThat(hashFileContentInIndex).isEqualTo(hashOfFileExpected);
//            }
//
//        } catch (FileNotFoundException e) {
//            e.getCause();
//        }
//    }
//
//    @Test
//    public void testOpenAdd() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContent = "zzz";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);
//
//        git.add(file.toPath());
//
//        Git gitOther = new Git(folder.getRoot());
//        gitOther.open();
//
//        assertThat(gitOther.getNumberOfObjectsFiles()).isEqualTo(1);
//    }
//
//    @Test
//    public void testOpenAddCommit() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContent = "zzz";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);
//
//        git.add(file.toPath());
//        git.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        Git gitOther = new Git(folder.getRoot());
//        gitOther.open();
//
//        assertThat(gitOther.getNumberOfObjectsFiles()).isEqualTo(3);
//    }
//
//    @Test
//    public void testOpenLog() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContentFirst = "zzz";
//        String fileContentSecond = "aaa";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);
//
//        git.add(file.toPath());
//        git.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);
//        git.add(file.toPath());
//        git.commitDeleteDeleted("Second commitDeleteDeleted!");
//
//        List<CommitInfo> log = git.log();
//        for (CommitInfo info : log) {
//            info.printCommitInfo();
//        }
//
//        Git gitOther = new Git(folder.getRoot());
//        gitOther.open();
//
//        List<CommitInfo> logOther = gitOther.log();
//        log.retainAll(logOther);
//
//        assertThat(log.size()).isEqualTo(0);
//    }
//
//    @Test
//    public void testLog() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContentFirst = "zzz";
//        String fileContentSecond = "aaa";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);
//
//        git.add(file.toPath());
//        git.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);
//        git.add(file.toPath());
//        git.commitDeleteDeleted("Second commitDeleteDeleted!");
//
//        List<CommitInfo> logBoth = git.log();
//
//        for(CommitInfo curCommit : logBoth) {
//            curCommit.printCommitInfo();
//        }
//    }
//
//    @Test
//    public void testLogFromRevision() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContentFirst = "zzz";
//        String fileContentSecond = "aaa";
//
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);
//
//        git.add(file.toPath());
//
//        git.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);
//
//        git.add(file.toPath());
//
//        git.commitDeleteDeleted("Second commitDeleteDeleted!");
//
//        List<CommitInfo> logBoth = git.log();
//        CommitInfo firstF = null;
//
//        for(CommitInfo curCommit : logBoth) {
//            curCommit.printCommitInfo();
//            if (curCommit.getRevisionNumber() == 1) {
//                firstF = curCommit;
//            }
//        }
//
//        List<CommitInfo> logSingle = git.log(1);
//        CommitInfo firstS = null;
//
//        for(CommitInfo curCommit : logSingle) {
//            curCommit.printCommitInfo();
//            if (curCommit.getRevisionNumber() == 1) {
//                firstS = curCommit;
//            }
//        }
//
//        if (firstF != null && firstS != null) {
//            assertThat(firstF.getCommitInfo()).isEqualTo(firstS.getCommitInfo());
//        }
//    }
//
//    @Test
//    public void testCheckoutRevisionChangeFileContent() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContentFirst = "zzz";
//        String fileContentSecond = "aaa";
//
//        File file = folder.newFile(fileName);
//
//        FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", false);
//        git.add(file.toPath());
//        git.commitDeleteDeleted("First commitDeleteDeleted!");
//
//
//        FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", false);
//        git.add(file.toPath());
//        git.commitDeleteDeleted("Second commitDeleteDeleted!");
//
//        git.checkout(1);
//
//        try (Scanner scanner = new Scanner(file)) {
//            String fileContentActual = scanner.nextLine();
//            assertThat(fileContentActual).isEqualTo(fileContentFirst);
//
//        } catch (FileNotFoundException e) {
//            e.getCause();
//        }
//    }
//
//    @Test
//    public void testCheckoutRevisionAddNewFiles() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileNameFirst = "file1.txt";
//        String fileNameSecond = "file2.txt";
//        String fileContent = "zzz";
//
//        File fileF = folder.newFile(fileNameFirst);
//        FileUtils.writeStringToFile(fileF, fileContent, "UTF-8", true);
//
//        git.add(fileF.toPath());
//        git.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        File fileS = folder.newFile(fileNameSecond);
//        FileUtils.writeStringToFile(fileS, fileContent, "UTF-8", true);
//
//        git.add(fileS.toPath());
//        git.commitDeleteDeleted("Second commitDeleteDeleted!");
//
//        /* delete second file */
//        git.checkout(1);
//
//        assertThat(fileS).doesNotExist();
//    }
//
//    @Test
//    public void testCheckoutFile() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContentFirst = "zzz";
//        String fileContentSecond = "aaa";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);
//
//        git.add(file.toPath());
//        git.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);
//
//        /* delete changes in file */
//        git.checkout(file);
//
//        assertThat(file).hasContent(fileContentFirst);
//    }
//
//    @Test
//    public void testRm() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContentFirst = "zzz";
//        String fileContentSecond = "aaa";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);
//
//        git.add(file.toPath());
//        git.commitDeleteDeleted("First commitDeleteDeleted! Add file.txt");
//
//        git.rm(file);
//        assertThat(file).exists();
//
//        git.commitDeleteDeleted("Second commitDeleteDeleted! Delete file.txt");
//
//        FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", false);
//
//        /* delete changes in file */
//        git.checkout(1);
//
//        assertThat(file).hasContent(fileContentFirst);
//    }
//
//    @Test
//    public void testStatus() throws IOException {
//        Git git = new Git(folder.getRoot());
//
//        git.init();
//
//        String fileName = "file.txt";
//        String fileContentFirst = "zzz";
//        String fileContentSecond = "aaa";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);
//
//        git.status();
//        git.add(file.toPath());
//        FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", false);
//        git.status();
//
//        git.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        git.status();
//        git.add(file.toPath());
//        git.commitDeleteDeleted("Second commitDeleteDeleted! Delete file.txt");
//
//
//
//        /* delete changes in file */
//        git.checkout(1);
//
//        assertThat(file).hasContent(fileContentSecond);
//    }
//}