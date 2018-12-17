package torrent.network;

import torrent.client.TorrentClientInfo;
import torrent.fileSystemManager.TorrentFileInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Client2TrackerConnection extends Connection {
    private DataInputStream  inputStream;
    private DataOutputStream outputStream;

    Client2TrackerConnection(Socket socket) throws IOException {
        super(socket);

        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }


    private Set<TorrentFileInfo> readFileInfos(int count) throws IOException {
        Set<TorrentFileInfo> result = new HashSet<>();

        for (int i = 0; i < count; i++) {
            result.add(new TorrentFileInfo(
                    inputStream.readInt(),
                    inputStream.readUTF(),
                    inputStream.readLong()
            ));
        }

        return result;
    }

    Set<TorrentFileInfo> executeListCommand() throws IOException {
        outputStream.writeByte(1);

        outputStream.flush();

        int count = inputStream.readInt();
        return readFileInfos(count);
    }

    int executeUploadCommand(String fileName, long fileSize) throws IOException {
        outputStream.writeByte(2);

        outputStream.writeUTF(fileName);
        outputStream.writeLong(fileSize);

        outputStream.flush();

        return inputStream.readInt();
    }

    private List<TorrentClientInfo> readClientInfos(int count) throws IOException {
        List<TorrentClientInfo> result = new ArrayList<>();

        byte ip[] = new byte[4];

        for (int i = 0; i < count; i++) {
            inputStream.readFully(ip);
            result.add(new TorrentClientInfo(
                    InetAddress.getByAddress(ip),
                    inputStream.readShort()
            ));
        }

        return result;
    }

    List<TorrentClientInfo> executeSourcesCommand(int fileID) throws IOException {
        outputStream.writeByte(3);

        outputStream.writeInt(fileID);
        outputStream.flush();

        int count = inputStream.readInt();

        return readClientInfos(count);
    }

    boolean executeUpdateCommand(short port, int filesCount, Set<Integer> filesID) throws IOException {
        outputStream.writeByte(4);

        outputStream.writeShort(port);
        outputStream.writeInt(filesCount);
        for (Integer id : filesID) {
            outputStream.writeInt(id);
        }

        outputStream.flush();

        return inputStream.readBoolean();
    }
}
