package network;

import protobuf.IntArray;
import server.ServerType;
import statistics.StatisticsResultPerIteration;
import statistics.TestingParameters;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Connection implements AutoCloseable {
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private Socket socket;
    private ObjectOutputStream outputData;
    private ObjectInputStream inputData;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.outputData = new ObjectOutputStream(socket.getOutputStream());
        this.inputData = new ObjectInputStream(socket.getInputStream());

        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }


    public ServerType getServerType() throws IOException, ClassNotFoundException {
        return (ServerType) inputData.readObject();
    }

    public TestingParameters getParameters() throws IOException, ClassNotFoundException {
        return (TestingParameters) inputData.readObject();

    }

    public InetAddress getInetAddress() throws IOException, ClassNotFoundException {
        return (InetAddress) inputData.readObject();
    }

    public Short getPort() throws IOException {
        return inputData.readShort();
    }

    public void sendServerType(ServerType serverType) throws IOException {
        outputData.writeObject(serverType);
        outputData.flush();
    }

    public byte readRequestByte() throws IOException {
        return inputData.readByte();
    }

    public void sendRequestByte(byte request) throws IOException {
        outputData.writeByte(request);
        outputData.flush();
    }

    public void sendInetAddress(InetAddress inetAddress) throws IOException {
        outputData.writeObject(inetAddress);
        outputData.flush();
    }

    public void sendPort(short port) throws IOException {
        outputData.writeShort(port);
        outputData.flush();
    }

    public void sendParameters(TestingParameters testingParameters) throws IOException {
        outputData.writeObject(testingParameters);
        outputData.flush();
    }

    public boolean getServerStatus() throws IOException {
        return inputData.readBoolean();
    }

    public void sendServerStatus(boolean status) throws IOException {
        outputData.writeBoolean(status);
        outputData.flush();
    }

    public void sendVaryingParameterValue(int currentValue) throws IOException {
        outputData.writeInt(currentValue);
        outputData.flush();
    }

    public int getVaryingParameterCurrentValue() throws IOException {
        return inputData.readInt();
    }

    public void sendStatisticsResults(List<StatisticsResultPerIteration> statisticsResults) throws IOException {
        outputData.writeInt(statisticsResults.size());

        for (StatisticsResultPerIteration statisticsResult : statisticsResults) {
            outputData.writeObject(statisticsResult);
        }

        outputData.flush();

    }

    public List<StatisticsResultPerIteration> getStatisticsResults() throws IOException, ClassNotFoundException {
        List<StatisticsResultPerIteration> result = new ArrayList<>();

        int resultsCount = inputData.readInt();

        for (int i = 0; i < resultsCount; i++) {
            result.add((StatisticsResultPerIteration) inputData.readObject());
        }

        return result;
    }
}