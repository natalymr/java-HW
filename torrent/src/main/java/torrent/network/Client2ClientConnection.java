package torrent.network;

import torrent.fileSystemManager.TorrentFileInfo;
import torrent.fileSystemManager.TorrentFilePart;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class FileGet {
    private final int ID;
    private final int partNumber;

    FileGet(int ID, int partNumber) {
        this.ID         = ID;
        this.partNumber = partNumber;
    }

    int getID() {
        return ID;
    }

    int getPartNumber() {
        return partNumber;
    }
}

class StatResult {
    private final int count;
    private final List<Integer> partsNumber;

    StatResult(int count, List<Integer> partsNumber) {
        this.count = count;
        this.partsNumber = partsNumber;
    }

    int getCount() {
        return count;
    }

    List<Integer> getPartsNumber() {
        return partsNumber;
    }
}

class Client2ClientConnection extends Connection {

    private DataOutputStream outputData;
    private DataInputStream  inputData;

    Client2ClientConnection(Socket socket) throws IOException {
        super(socket);

        inputData  = new DataInputStream(socket.getInputStream());
        outputData = new DataOutputStream(socket.getOutputStream());
    }


    byte readRequestByte() throws IOException {
        return inputData.readByte();
    }

    int readNeededFiledID() throws IOException {
        return inputData.readInt();
    }

    void writeAvailableFileParts(List<Integer> availableFileParts) throws IOException {
        outputData.writeInt(availableFileParts.size());

        for (Integer availableFilePartNumber : availableFileParts) {
            outputData.writeInt(availableFilePartNumber);
        }

        outputData.flush();
    }

    FileGet readFileToGet() throws IOException {
        return new FileGet(
                inputData.readInt(),
                inputData.readInt()
        );
    }

    void writeTorrentFilePart(TorrentFilePart torrentFilePart) throws IOException {
        Files.copy(torrentFilePart.getPartFileContentInFile().toPath(), outputData);
    }

    StatResult sendStatRequest(int id) throws IOException {
        outputData.writeByte(1);
        outputData.writeInt(id);
        outputData.flush();

        int count = inputData.readInt();
        List<Integer> partsNumber = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            partsNumber.add(inputData.readInt());
        }

        return new StatResult(count, partsNumber);
    }

    byte[] sendGetRequest(int id, int partNumber, long size, int partCount, int partSize) throws IOException {
        outputData.writeByte(2);
        outputData.writeInt(id);
        outputData.writeInt(partNumber);
        outputData.flush();

        int bytesCountToRead;
        if (partNumber == partCount - 1) {
            bytesCountToRead = (int) (size - (partCount - 1) * partSize);
        } else {
            bytesCountToRead = partSize;
        }

        byte[] byteArray = new byte[bytesCountToRead];
        inputData.readFully(byteArray);

        return byteArray;
    }
}
