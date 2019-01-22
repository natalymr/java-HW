package network;

import protobuf.IntArray;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientConnection implements AutoCloseable {
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private Socket socket;

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;

        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public List<Integer> getArray() throws IOException {
        // get an answer in protobuf mode
        int arraySize = dataInputStream.readInt();
        byte[] bytes = new byte[arraySize];
        dataInputStream.readFully(bytes);
        IntArray intArray = IntArray.parseFrom(bytes);
        return intArray.getArrayList();
    }

    public void sendArray(List<Integer> array) throws IOException {
        // convert array in protobuf mode
        IntArray.Builder array2send = IntArray.newBuilder();
        array2send.addAllArray(array);
        IntArray result = array2send.build();

        // write array size
        dataOutputStream.writeInt(result.getSerializedSize());

        // write array
        result.writeTo(dataOutputStream);
        dataOutputStream.flush();
    }

}
