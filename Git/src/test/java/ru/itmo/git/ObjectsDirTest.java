//package ru.itmo.git.git.MyGit;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.filefilter.TrueFileFilter;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.TemporaryFolder;
//import ru.itmo.git.git.MyGit;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.List;
//import java.util.Scanner;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class ObjectsDirTest {
//
//    @Rule
//    public final TemporaryFolder folder = new TemporaryFolder();
//
//    @Test
//    public void testBlobFile() throws IOException {
//
//        MyGit MyGit = new MyGit(folder.getRoot());
//
//        MyGit.init();
//
//        String fileName = "file.txt";
//        String fileContent = "zzz";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);
//
//        MyGit.add(file.toPath());
//
//        File objectsDir = folder.getRoot().toPath().resolve(".myMyGit").resolve("objects").toFile();
//        List<File> objectsContains = (List<File>) FileUtils.listFiles(objectsDir,
//                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
//
//        for (File f : objectsContains) {
//            try (Scanner scanner = new Scanner(f)) {
//                String label = scanner.nextLine();
//                if (label.equals("BLOB")) {
//                    String fileContentActual = scanner.nextLine();
//                    String fileContentExpected = "zzz";
//                    assertThat(fileContentActual).isEqualTo(fileContentExpected);
//                }
//
//            } catch (FileNotFoundException e) {
//                e.getCause();
//            }
//        }
//    }
//
//    @Test
//    public void testTreeFile() throws IOException {
//        MyGit MyGit = new MyGit(folder.getRoot());
//
//        MyGit.init();
//
//        String fileName = "file.txt";
//        String fileContent = "zzz";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);
//
//        MyGit.add(file.toPath());
//
//        MyGit.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        File objectsDir = folder.getRoot().toPath().resolve(".myMyGit").resolve("objects").toFile();
//        List<File> objectsContains = (List<File>) FileUtils.listFiles(objectsDir,
//                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
//
//        for (File f : objectsContains) {
//            try (Scanner scanner = new Scanner(f)) {
//                String label = scanner.nextLine();
//                if (label.equals("TREE")) {
//                    String blobLabel = scanner.next();
//                    String blobFileName = scanner.next();
//                    String blobFileHash = scanner.next();
//
//                    assertThat(blobLabel).isEqualTo("blob");
//                    assertThat(blobFileName).isEqualTo(file.toString());
//
//                    blobFileHash += ".txt";
//                    File blobFileHashAbsolutePath = folder.getRoot().toPath()
//                            .resolve(".myMyGit").resolve("objects").resolve(blobFileHash).toFile();
//                    assertThat(objectsContains).contains(blobFileHashAbsolutePath);
//                }
//
//            } catch (FileNotFoundException e) {
//                e.getCause();
//            }
//        }
//    }
//
//    @Test
//    public void testCommitFile() throws IOException {
//
//        MyGit MyGit = new MyGit(folder.getRoot());
//
//        MyGit.init();
//
//        String fileName = "file.txt";
//        String fileContent = "zzz";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);
//
//        MyGit.add(file.toPath());
//
//        MyGit.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        File objectsDir = folder.getRoot().toPath().resolve(".myMyGit").resolve("objects").toFile();
//        List<File> objectsContains = (List<File>) FileUtils.listFiles(objectsDir,
//                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
//
//        for (File f : objectsContains) {
//            try (Scanner scanner = new Scanner(f)) {
//                String label = scanner.nextLine();
//                if (label.equals("COMMIT_EXTENSION")) {
//                    /* commitDeleteDeleted */
//                    assertThat(scanner.next()).isEqualTo("COMMIT_EXTENSION#");
//
//                    String cRevNumberActual = scanner.next();
//                    String cRevNumberExpected = "1";
//                    assertThat(cRevNumberActual).isEqualTo(cRevNumberExpected);
//
//                    /* tree */
//                    System.out.println(scanner.next() + " " + scanner.next());
//
//                    /* parent */
//                    assertThat(scanner.next()).isEqualTo("parent");
//
//                    String cParentActual = scanner.next(); // parent
//                    String cParentExpected = "-1";
//                    assertThat(cParentActual).isEqualTo(cParentExpected);
//
//                    /* date */
//                    System.out.println(scanner.nextLine() + scanner.nextLine());
//
//                    /* message */
//                    assertThat(scanner.next()).isEqualTo("message");
//
//                    String cMsgActual = scanner.next() + " " + scanner.next();
//                    String cMsgExpected = "First commitDeleteDeleted!";
//                    assertThat(cMsgActual).isEqualTo(cMsgExpected);
//                }
//
//            } catch (FileNotFoundException e) {
//                e.getCause();
//            }
//        }
//    }
//
//
//    @Test
//    public void testNumberOfFilesForAddAndCommitAndChange() throws IOException {
//        MyGit MyGit = new MyGit(folder.getRoot());
//
//        MyGit.init();
//
//        String fileName = "file.txt";
//        String fileContentFirst = "zzz";
//        String fileContentSecond = "aaa";
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContentFirst, "UTF-8", true);
//
//        MyGit.add(file.toPath());
//
//        MyGit.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        // check that in objects dir there are only:
//        // 1 blob file
//        // 1 tree file
//        // 1 commitDeleteDeleted file
//        assertThat(MyGit.getNumberOfObjectsFiles()).isEqualTo(3);
//
//        FileUtils.writeStringToFile(file, fileContentSecond, "UTF-8", true);
//
//        MyGit.add(file.toPath());
//
//        MyGit.commitDeleteDeleted("Second commitDeleteDeleted!");
//
//        // NOW
//        // check that in objects dir there are:
//        // 2 blob file
//        // 2 tree file
//        // 2 commitDeleteDeleted file
//        assertThat(MyGit.getNumberOfObjectsFiles()).isEqualTo(6);
//    }
//
//    @Test
//    public void testNumberOfFilesInCaseOfNestedDir() throws IOException {
//        MyGit MyGit = new MyGit(folder.getRoot());
//
//        MyGit.init();
//
//        String dirName = "NestedDir";
//        String fileName = dirName + File.separator + "file.txt";
//        String fileContent = "zzz";
//
//        File nestedDir = folder.newFolder(dirName);
//
//        File file = folder.newFile(fileName);
//        FileUtils.writeStringToFile(file, fileContent, "UTF-8", true);
//        MyGit.add(nestedDir.toPath());
//        MyGit.commitDeleteDeleted("First commitDeleteDeleted!");
//
//        // check that in objects dir there are only:
//        // 1 blob file
//        // 2 tree file
//        // 1 commitDeleteDeleted file
//        assertThat(MyGit.getNumberOfObjectsFiles()).isEqualTo(4);
//    }
//}