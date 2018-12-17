package torrent.network;

import torrent.client.TorrentClientInfo;
import torrent.fileSystemManager.TorrentFileInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class FileUpload {
    private final String fileName;
    private final long   fileSize;

    FileUpload(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    String getFileName() {
        return fileName;
    }

    long getFileSize() {
        return fileSize;
    }
}

class ClientFilesUpdate {
    private final short         clientPort;
    private final List<Integer> filesID;

    ClientFilesUpdate(short clientPort, List<Integer> filesID) {
        this.clientPort = clientPort;
        this.filesID    = filesID;
    }

    short getClientPort() {
        return clientPort;
    }

    List<Integer> getFilesID() {
        return filesID;
    }
}

class Tracker2ClientConnection extends Connection {
    private DataOutputStream outputData;
    private DataInputStream  inputData;

    Tracker2ClientConnection(Socket socket) throws IOException {
        super(socket);
        inputData  = new DataInputStream(socket.getInputStream());
        outputData = new DataOutputStream(socket.getOutputStream());
    }

    int readRequestType() throws IOException {
        return inputData.read();
    }

    private void writeFileInfo(TorrentFileInfo file) throws IOException {
        outputData.writeInt(file.getId());
        outputData.writeUTF(file.getName());
        outputData.writeLong(file.getSize());
    }

    void writeFileInfos(Set<TorrentFileInfo> fileInfos) throws IOException {
        // write count of files
        outputData.writeInt(fileInfos.size());

        // write info about each file
        for (TorrentFileInfo file : fileInfos) {
            writeFileInfo(file);
        }

        outputData.flush();
    }

    FileUpload readFileUpload() throws IOException {
        return new FileUpload(inputData.readUTF(), inputData.readLong());
    }

    void writeFileID(int newFileID) throws IOException {
        outputData.writeInt(newFileID);

        outputData.flush();
    }

    int readFileId() throws IOException {
        return inputData.readInt();
    }

    private void writeClientInfo(TorrentClientInfo clientInfo) throws IOException {

        outputData.write(clientInfo.getIP().getAddress());
        outputData.writeShort(clientInfo.getPort());
    }

    void writeClientsInfos(List<TorrentClientInfo> clientInfos) throws IOException {
        outputData.writeInt(clientInfos.size());

        for (TorrentClientInfo clientInfo : clientInfos) {
            writeClientInfo(clientInfo);
        }

        outputData.flush();
    }

    private List<Integer> readFilesID(int count) throws IOException {
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            result.add(inputData.readInt());
        }

        return result;
    }

    ClientFilesUpdate readClientFilesUpdate() throws IOException {
        short clientPort = inputData.readShort();
        int count = inputData.readInt();
        List<Integer> filesID = readFilesID(count);

        return new ClientFilesUpdate(clientPort, filesID);
    }

    void writeUpdateStatus(boolean status) throws IOException {
        outputData.writeBoolean(status);

        outputData.flush();
    }

    public void writeEmptyClientInfos() throws IOException {
        outputData.writeInt(0);
        outputData.flush();
    }
}
