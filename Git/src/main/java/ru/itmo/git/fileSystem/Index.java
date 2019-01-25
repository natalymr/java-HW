package ru.itmo.git.fileSystem;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static ru.itmo.git.fileSystem.Blob.computeHash;

public class Index implements Serializable, Iterable<File> {
    private final Map<File, FileStatus> fileStatusMap;
    private final Map<File, Integer> fileHashMap;

    public Index() {
        fileStatusMap = new HashMap<>();
        fileHashMap = new HashMap<>();
    }

    public void add(File file) throws IOException {
        // если файл - директория
        if (file.isDirectory()) {
            // проходимся рекурсивно по всем внутренностям и добавляем их в индекс
            File[] fileList = file.listFiles();
            if (fileList != null && fileList.length > 0) {
                for (File subFile : fileList) {
                    if (subFile.isDirectory()) {
                        add(subFile);
                    } else {
                        addFileAndChangeStatus(subFile);
                    }
                }
            }
        } else {
            addFileAndChangeStatus(file);
        }
    }

    private void addFileAndChangeStatus(File file) throws IOException {
        if (knowsAbout(file)) {
            if (fileStatusMap.get(file) == FileStatus.Tracked) {
                int oldHash = fileHashMap.get(file);
                int currentHash = computeHash(file);

                if (currentHash != oldHash) {
                    fileStatusMap.put(file, FileStatus.Modified);
                    fileHashMap.put(file, currentHash);
                }
            }
        } else {
            fileStatusMap.put(file, FileStatus.Added);
            fileHashMap.put(file, computeHash(file));
        }
    }

    public void changeFileStatus(File file, FileStatus newStatus) {
        fileStatusMap.put(file, newStatus);
    }

    public boolean knowsAbout(File file) {
        return fileStatusMap.containsKey(file);
    }

    public FileStatus getFileStatus(File file) {
        return fileStatusMap.get(file);
    }

    public int getFileHash(File file) {
        return fileHashMap.get(file);
    }

    public void commitDeleteDeleted() {
        List<File> filesToRemove = new ArrayList<>();

        for (File file : this) {
            if (getFileStatus(file) == FileStatus.Deleted) {
                filesToRemove.add(file);
            }
        }

        for (File file : filesToRemove) {
            fileStatusMap.remove(file);
            fileHashMap.remove(file);
        }
    }

    public void commitFromAddedOrModifiedToTracked() throws IOException {
        List<File> filesToTracked = new ArrayList<>();

        for (File file : this) {
            if (getFileStatus(file) == FileStatus.Added ||
                getFileStatus(file) == FileStatus.Modified) {
                filesToTracked.add(file);
            }
        }

        for (File file : filesToTracked) {
            fileStatusMap.put(file, FileStatus.Tracked);
            fileHashMap.put(file, computeHash(file));
        }
    }

    @Override
    public Iterator<File> iterator() {
        Iterator<File> it = new Iterator<File>() {
            private Iterator<File> fileIterator= fileStatusMap.keySet().iterator();

            @Override
            public boolean hasNext() {
                return fileIterator.hasNext();
            }

            @Override
            public File next() {
                return fileIterator.next();
            }

        };

        return it;
    }
}
