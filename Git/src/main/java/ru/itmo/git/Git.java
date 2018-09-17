package ru.itmo.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Git {
    private String pwd;
    private Path objects;
    private File HEAD;
    private File index;
    private Map<Integer, String> revisions;
    private Integer nextRevisionNumber;

    Git(File rootDir) {
        pwd = rootDir.getAbsolutePath();
        revisions = new HashMap<>();
        nextRevisionNumber = 1;
    }

    /**
     * Вспомогательная функция, которая создает новую директорию с именем, которое
     * передали в качестве параметра. Используется в git init.
     * @param newDirName
     * @return true - если удалось создать новую директорию,
     * false - если не получилось и поймали исключение
     */
    private boolean createNewDir(String newDirName) {

        File newDir = new File(newDirName);
        if (!newDir.exists()) {
            return newDir.mkdir();
        }

        return false;
    }

    /**
     * Вспомогательная функция, которая создает новый файл с именем, которое передали в
     * качестве параметра. Испольщуется в git init.
     * @param newFileName
     * @return true - если удалось создать новый файл
     * false - если не получилось и поймали исключение
     */
    private boolean createNewFile(String newFileName) {

        File newFile = new File(newFileName);
        if (!newFile.exists()) {
            try {
                return newFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("createNewFileInPwd. Failed to create " + newFileName + " file.");
                return false;
            }
        }

        return false;
    }

    public void init() {

        String gitDirName = pwd + File.separator + ".git";
        String objectsDirName = gitDirName + File.separator+ "objects";
        String indexFileName = gitDirName + File.separator + "index.txt";
        String HEADFileName = gitDirName + File.separator + "HEAD.txt";

        // create .git
        if (createNewDir(gitDirName)) {
            System.err.println("init. .git/ directory is created!");
        } else {
            System.err.println("init. Failed to create git directory.");
        }

        // create .git/objects
        if (createNewDir(objectsDirName)) {
            System.err.println("init. .git/objects/ directory is created!");
        } else {
            System.err.println("init. Failed to create .git/objects directory.");
        }
        objects = (new File(objectsDirName)).toPath();

        // create .git/index.txt
        if (createNewFile(indexFileName)) {
            System.err.println("init. .git/index.txt file is created!");
        } else {
            System.err.println("init. Failed to create .git/index.txt file.");
        }
        index = new File(indexFileName);

        // create .git/HEAD.txt
        if (createNewFile(HEADFileName)) {
            System.err.println("init. .git/HEAD.txt is created!");
        } else {
            System.err.println("init. Failed to create .git/HEAD.txt file.");
        }
        HEAD = new File(HEADFileName);
    }


    ////////////////////////////// add  ///////////////////////////////////

    /**
     * данная функция читает файл, путь до которого ей дали,
     * вычисляет хэш от "blob\0" + СодержимоеФайла
     * проверяет, если в папке .git/objects файл, у которого имя такое же, как и найденный кэш,
     * и если такого файла нет, то создает новый файл
     * и записывает в новый blob файл все содержимое из файла, который передали данной функции
     * @param pathToFile - имя файла, для которого сделали add
     * @return hash от содержимого файла
     */
    private int createBlobFile(Path pathToFile) {
        File file = pathToFile.toFile();

        String content = "";

        // read file content from file
        try {
            content = FileUtils.readFileToString(file, "UTF-8");

        } catch (IOException e) {
            System.err.println("createBlobFile. Failed to read " + pathToFile);
            e.printStackTrace();
        }

        content = "BLOB\n" + content;

        // create new blob file in object if there is no such file
        int hash = content.hashCode();
        try {
            File blobFile = new File(objects.toString() + File.separator + Integer.toString(hash) + ".txt");
            FileUtils.writeStringToFile(blobFile, content, "UTF-8");
        } catch (IOException e) {
            System.err.println("createBlobFile. Failed to create new blob file for " + pathToFile);
            e.printStackTrace();
        }

        // return hash of file content
        return hash;
    }

    /**
     * Вспомогательная функция, которая читает содержимое файла .git/index.txt.
     * Она парсит содержимое: file_name hash(file_content)
     * и возвращает мапу, у которой ключ - это file_name, а значение - hash(file_content).
     * Используется для git add <file_name>
     * @return map
     */
    private Map<String, Integer> convertIndexToMap() {
        Map<String, Integer> result = new HashMap<>();

        try (Scanner scanner = new Scanner(index)) {
            while (scanner.hasNext()) {
                String key = scanner.next();
                String value = scanner.next();

                result.put(key, Integer.parseInt(value));
            }

        } catch (FileNotFoundException e) {
            System.err.println("convertIndexToMap. Failed to read index.txt");
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Вспомогательная функция, которая обновляет hash(file_content).
     * Используется в git add, если содержимое файла поменялось и надо обновить
     * hash(file_content) в .git/index.txt
     * @param oldHash
     * @param newHash
     */
    private void replaceHashInIndex(int oldHash, int newHash) {

        try {
            String content = FileUtils.readFileToString(index, "UTF-8");
            content = content.replaceAll(Integer.toString(oldHash), Integer.toString(newHash));
            FileUtils.writeStringToFile(index, content, "UTF-8", false);
        } catch (IOException e) {
            System.err.println("replaceHashInIndex. Failed to read or write to index.txt.");
            e.printStackTrace();
        }
    }

    /**
     * Вспомогательная функция, которая добавляет информацию о новом файле.
     * Дозаписывает в .git/index.txt file_name hash(file_content).
     * Используется в git add
     * @param fileName
     * @param fileHash
     */
    private void addNewFileInIndex(String fileName, int fileHash) {

        try {
            FileUtils.writeStringToFile(index,
                                        fileName + " " + Integer.toString(fileHash) + ".txt",
                                        "UTF-8",
                                        true);
        } catch (IOException e) {
            System.err.println("addNewFileInIndex. Failed to write to index.txt.");
            e.printStackTrace();
        }
    }

    /**
     *
     * @param pathToFile
     */
    private void addFile(Path pathToFile) {
        // compute hash
        int hash = createBlobFile(pathToFile);

        // parse .git/index.txt file in map<String, Integer>
        Map<String, Integer> indexFileInMap = convertIndexToMap();

        // if there is this blob file in index.txt just replace hash
        // else - add new file and its hash
        if (indexFileInMap.containsKey(pathToFile.toString())) {
            replaceHashInIndex(indexFileInMap.get(pathToFile.toString()), hash);
        } else {
            addNewFileInIndex(pathToFile.toString(), hash);
        }
    }

    /**
     * add
     * @param pathToFile
     */
    public void add(Path pathToFile) {
        addFile(pathToFile);
    }

    ///////////////////////// commit /////////////////////////

    /**
     * Это рекурсивная функция.
     * Она собирает всю необходимую информацию для treeFile для данной директории.
     * Для всех файлов, которые хранятся в переданной директории, она записывает:
     * если это поддиректория, то записываем "tree subdir_name hash(subdir_tree_file_content)",
     * если это просто файл, то записываем   "blob file_name hash(file-content)"
     *
     * После того, как обошли все файлы, мы получаем нужный treeFile.
     * Высчитаваем hash и если в .git/objects есть такой файл, ничего не делаем,
     * если такого файла нет, то создаем новый, записываем в него собранную информацию.
     * Используется в git commit.
     * @param pathToDir
     * @return hash(tree_file_content)
     */
    private int createTreeFile(Path pathToDir) {
        File dir = pathToDir.toFile();

        int hash = -1;

        if (dir.isDirectory()) {
            // list all files in dir
            List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            String treeFileContent = "TREE\n";

            // iterate of all files in this dir
            // if cur file is file, just add "blob file_name hash(file_content)"
            // if cur file is dir,  just add "tree dir_name  hash(tree_file_content)"
            for (File curFile : files) {
                // skip all git's files
                if (curFile.getName().equals(".git") ||
                        curFile.getName().equals("objects") ||
                        curFile.getParent().equals(objects.toString()) ||
                        curFile.getName().equals("index.txt") ||
                        curFile.getName().equals("HEAD.txt")) {
                    continue;
                }

                if (curFile.isDirectory()) {
                    String hashOfTreeFileForThisSubdir = Integer.toString(createTreeFile(curFile.toPath()));
                    String dirInfo = "tree" + " " + curFile.toString() + " " + hashOfTreeFileForThisSubdir + "\n";
                    treeFileContent += dirInfo;
                } else {
                    String hashOfBlobFile = Integer.toString(createBlobFile(curFile.toPath()));
                    String fileInfo = "blob" + " " + curFile.toString() + " " + hashOfBlobFile + "\n";
                    treeFileContent += fileInfo;
                }
            }

            hash = treeFileContent.hashCode();

            // if in .git/objects there is file with name like hash, do nothing
            // else - create new file and write in it tree file content
            try {
                File treeFile = new File(objects.toString() + File.separator +
                        Integer.toString(hash) + ".txt");

                FileUtils.writeStringToFile(treeFile, treeFileContent, "UTF-8");
            } catch (IOException e) {
                System.err.println("createTreeFile. Failed to write to tree file.");
                e.printStackTrace();
            }
        }

        return hash;
    }

    public void commit(String commitMessage) {
        int hash = -1;

        // compute all accessory info for commit

        int hashOfTreeFileForRoot = createTreeFile(Paths.get(pwd));

        // get parent commit
        String parentHashString;
        Integer parentHashInt = -1;
        try {
            if (HEAD.length() > 0) {
                parentHashString = FileUtils.readFileToString(HEAD, "UTF-8");
                parentHashInt = Integer.parseInt(parentHashString);
            }
        } catch (IOException e) {
            System.err.println("commit. Failed to read from HEAD.txt.");
            e.printStackTrace();
        }

        CommitInfo curCommit = new CommitInfo(nextRevisionNumber, hashOfTreeFileForRoot,
                parentHashInt,commitMessage);
        nextRevisionNumber++;

        hash = curCommit.getCommitInfo().hashCode();

        // create new file for this commit
        try {
            File commitFile = new File(objects.toString() + File.separator + Integer.toString(hash) + ".txt");
            FileUtils.writeStringToFile(commitFile, "COMMIT" + curCommit.getCommitInfo(), "UTF-8");
        } catch (IOException e) {
            System.err.println("commit. Failed to write to commit file.");
            e.printStackTrace();
        }

        // change commit in head.txt
        try {
            FileUtils.writeStringToFile(HEAD, Integer.toString(hash), "UTF-8", false);
        } catch (IOException e) {
            System.err.println("commit. Failed to write to HEAD.txt.");
            e.printStackTrace();
        }
    }


    /**
     * log
     */
    public class CommitInfo {

        Integer revisionNumber_;
        Integer tree_;
        Integer parentHash_;
        String data_;
        String commitMessage_;

        CommitInfo(Integer revisionNumber, Integer tree, Integer parentHash, String commitMessage) {
            revisionNumber_ = revisionNumber;
            tree_ = tree;
            parentHash_ = parentHash;
            Date now = new Date();
            data_ = now.toString();
            commitMessage_ = commitMessage;
        }

        CommitInfo(File fileName) {
            // convert commit file in map
            Map<String, String> fileContentInMap = new HashMap<>();

            try (Scanner scanner = new Scanner(fileName)) {
                while (scanner.hasNext()) {
                    String key = scanner.next();
                    String value = scanner.next();

                    fileContentInMap.put(key, value);
                }

            } catch (FileNotFoundException e) {
                System.err.println("CommitInfoConstructor. Failed to read commitInfoFile");
                e.printStackTrace();
            }

            // fill class fileds from map
            revisionNumber_ = Integer.parseInt(fileContentInMap.get("COMMIT#"));
            tree_ = Integer.parseInt(fileContentInMap.get("tree"));
            parentHash_ = Integer.parseInt(fileContentInMap.get("parent"));
            data_ = fileContentInMap.get("data");
            commitMessage_ = fileContentInMap.get("message");
        }

        private String getCommitInfo() {
            StringBuilder result = new StringBuilder();

            //result.append("COMMIT#" + " " + revisionNumber_ + "\n");
            result.append(String.format("COMMIT# %d\n", revisionNumber_));
            result.append(String.format("tree %s\n", Integer.toString(tree_)));
            result.append(String.format("parent %d\n", parentHash_));
            result.append(String.format("data %s\n", data_));
            result.append(String.format("message %s\n", commitMessage_));

            return result.toString();
        }

        public void printCommitInfo() {
            System.out.println(this.getCommitInfo());
        }
    }

    public List<CommitInfo> log() {

        List<CommitInfo> result = new ArrayList<>();

        for (Integer tmpRevision = nextRevisionNumber - 1; tmpRevision > 0; tmpRevision--){

            File file = new File(revisions.get(tmpRevision));

            result.add(new CommitInfo(file));
        }

        return result;
    }


}
