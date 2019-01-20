package network;

import protobuf.IntArray;
import statistics.StatisticsResultPerIteration;
import statistics.TestingParameters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Connection implements AutoCloseable {
    private Socket socket;
    private ObjectOutputStream outputData;
    private ObjectInputStream inputData;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.outputData = new ObjectOutputStream(socket.getOutputStream());
        this.inputData = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public List<Integer> getArray() throws IOException {
        // get an answer in protobuf mode
        byte[] bytes = new byte[inputData.readInt()];
        inputData.readFully(bytes);
        IntArray intArray = IntArray.parseFrom(bytes);
        return intArray.getArrayList();
    }

    public void sendArray(List<Integer> array) throws IOException {
        // convert array in protobuf mode
        IntArray.Builder array2send = IntArray.newBuilder();
        array2send.addAllArray(array);
        IntArray result = array2send.build();

        // write array size
        outputData.writeInt(result.getSerializedSize());

        // write array
        result.writeTo(outputData);
        outputData.flush();
    }

    public String getServerType() throws IOException {
        return inputData.readUTF();
    }

    public TestingParameters getParameters() throws IOException, ClassNotFoundException {
//        int m, n, x, varyingParameterInInt, min, max, step;
//        int delay;
//
//        // read data
//        m = inputData.readInt();
//        n = inputData.readInt();
//        delay = inputData.readInt();
//        x = inputData.readInt();
//        varyingParameterInInt = inputData.readInt();
//        min = inputData.readInt();
//        max = inputData.readInt();
//        step = inputData.readInt();
//        VaryingParameter varyingParameter = null;
//
//        // convert from int to enum
//        switch (varyingParameterInInt) {
//            case 0:
//                varyingParameter = VaryingParameter.M;
//                break;
//            case 1:
//                varyingParameter = VaryingParameter.N;
//                break;
//            case 2:
//                varyingParameter = VaryingParameter.delay;
//                break;
//        }
//        // create new object
//        return new TestingParameters(m, n, delay, x, varyingParameter, min, max, step);

        return (TestingParameters) inputData.readObject();

    }

    public InetAddress getInetAddress() throws IOException, ClassNotFoundException {
        //return InetAddress.getByName(inputData.readUTF());
        return (InetAddress) inputData.readObject();
    }

    public Short getPort() throws IOException {
        return inputData.readShort();
    }

    public void sendServerType(String serverType) throws IOException {
        outputData.writeUTF(serverType);
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
        //outputData.writeUTF(inetAddress.getHostAddress());
        outputData.writeObject(inetAddress);
        outputData.flush();
    }

    public void sendPort(short port) throws IOException {
        outputData.writeShort(port);
        outputData.flush();
    }

    public void sendParameters(TestingParameters testingParameters) throws IOException {
//        outputData.writeInt(testingParameters.getM());
//        outputData.writeInt(testingParameters.getN());
//        outputData.writeLong(testingParameters.getDelay());
//        outputData.writeInt(testingParameters.getX());
//        outputData.writeInt(testingParameters.getVaryingParameter().getValue());
//        outputData.writeInt(testingParameters.getMinVP());
//        outputData.writeInt(testingParameters.getMaxVP());
//        outputData.writeInt(testingParameters.getStepVP());
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

    public int getNumberOfStatisticsResults() throws IOException {
        return inputData.readInt();
    }

    public StatisticsResultPerIteration readStatisticsResultPerIteration() throws IOException, ClassNotFoundException {
//        return new StatisticsResultPerIteration(
//            inputData.readInt(),
//            new AverageValues(
//                inputData.readDouble(),
//                inputData.readDouble(),
//                inputData.readDouble())
//        );

        return (StatisticsResultPerIteration) inputData.readObject();
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