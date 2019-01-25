package ru.itmo.git.fileSystem;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ru.itmo.git.fileSystem.MyGitDirectory.computeObjectsFileName;
import static ru.itmo.git.fileSystem.MyGitDirectory.getObjectsFileByHashAndType;

class Tree implements Serializable {
    private static final String TREE_EXTENSION = ".tree";
    private final File objectsDirectory;
    private final List<Tree> trees;
    private final List<Blob> blobs;
    private final File directory;

    Tree(File directory, File objectsDirectory) {
        this.objectsDirectory = objectsDirectory;
        this.directory = directory;
        trees = new ArrayList<>();
        blobs = new ArrayList<>();

        // рекурсивно проходимся по всем внутренностям папки и сохраняем их в виде блобов или три
        File[] fileList = directory.listFiles();
        if (fileList != null && fileList.length > 0) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    if (!isNotMyGitServiceDirectory(file)) {
                        trees.add(new Tree(file, objectsDirectory));
                    }
                } else {
                    blobs.add(new Blob(file, objectsDirectory));
                }
            }
        }
    }

    Tree(int treeHash, File directory, Objects objects) {
        this.objectsDirectory = objects.getObjectsDirectory();
        this.directory = directory;
        trees = new ArrayList<>();
        blobs = new ArrayList<>();

        // удалим все, что было в папке
        File[] fileList = directory.listFiles();
        if (fileList != null && fileList.length > 0) {
            for (File file : fileList) {
                file.delete();
            }
        }

        // восстановим файлы
        File treeFile = getObjectsFileByHashAndType(treeHash, TREE_EXTENSION, objectsDirectory);
        try {
            try (Scanner scanner = new Scanner(treeFile)) {
                while (true) {
                    String label = scanner.next();
                    String filename = scanner.next();
                    int hash = scanner.nextInt();

                    File file = new File(filename);
                    if (label.equals("tree")) {
                        file.mkdir();

                        trees.add(new Tree(hash, file, objects));
                    } else if (label.equals("blob")) {
                        file.createNewFile();

                        blobs.add(new Blob(hash, file, objects));
                    }
                }

            } catch (NoSuchElementException ignored) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    File getDirectory() {
        return directory;
    }

    void add(File file) {
        if (file.isDirectory()) {
            Tree newTree = new Tree(file, objectsDirectory);
            if (!trees.contains(newTree)) {
                trees.add(newTree);
            }
        } else if (file.isFile()) {
            Blob newBlob = new Blob(file, objectsDirectory);
            if (!blobs.contains(newBlob)) {
                blobs.add(newBlob);
            }
        }
    }

    void addAllPath(File file) {
        File tmp = file;

        while (!(tmp.getParentFile().toString().equals(directory.toString()))) {
            tmp = tmp.getParentFile();
        }

        add(tmp);
    }

    int commit(Index index) throws IOException {
        return newTreeFile(index);
    }

    private int newTreeFile(Index index) throws IOException {
        StringBuilder treeFileContent = new StringBuilder();

        for (Tree tree : trees) {
            int hash = tree.newTreeFile(index);
            treeFileContent
                .append("tree")
                .append(" ")
                .append(tree.getDirectory().toString())
                .append(" ")
                .append(hash)
                .append("\n");
        }

        for (Blob blob : blobs) {
            if (index.knowsAbout(blob.getBlobFile())) {
                int hash = blob.computeHash();
                treeFileContent
                    .append("blob")
                    .append(" ")
                    .append(blob.getBlobFile().toString())
                    .append(" ")
                    .append(hash)
                    .append("\n");

                blob.newBlobFile(hash);
            }
        }

        String contentInString = treeFileContent.toString();

        HashCode hashCode = Hashing.murmur3_32()
            .newHasher()
            .putString(contentInString, StandardCharsets.UTF_8)
            .hash();

        int hash = hashCode.asInt();

        File treeFile = new File(computeObjectsFileName(hash, TREE_EXTENSION, objectsDirectory));
        if (treeFile.createNewFile()) {
            FileUtils.writeStringToFile(treeFile, contentInString, StandardCharsets.UTF_8);
        }

        return hash;
    }

    private boolean isNotMyGitServiceDirectory(File file) {
        return  file.getName().equals(".mygit");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tree tree = (Tree) o;
        return objectsDirectory.equals(tree.objectsDirectory) &&
            trees.equals(tree.trees) &&
            blobs.equals(tree.blobs) &&
            directory.equals(tree.directory);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(objectsDirectory, trees, blobs, directory);
    }
}
