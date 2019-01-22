package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import network.Connection;
import network.client.ClientManager;
import server.ServerType;
import statistics.StatisticsResultPerIteration;
import statistics.TestingParameters;
import statistics.VaryingParameter;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.List;

import static server.serverManager.ServerManager.RUN_SERVER;
import static server.serverManager.ServerManager.SEND_RESULTS;

import static server.ServerType.sortInThreadPool;
import static server.ServerType.threadPerClient;

public class Controller {

    // configurations
    private ObservableList<String> serversArchList = FXCollections
        .observableArrayList(
            ServerType.threadPerClient.getTypeInString(),
            ServerType.sortInThreadPool.getTypeInString(),
            ServerType.nonBlockingServer.getTypeInString());

    private ObservableList<String> varyingParameters = FXCollections
            .observableArrayList("M", "N", "delay");
    @FXML
    private TextField ipValue, portValue;
    @FXML
    private ComboBox<String> serverArchBox;
    @FXML
    private TextField X;
    @FXML
    private ComboBox<String> varyingParameterBox;
    @FXML
    private TextField min, max, step;
    @FXML
    private TextField M, N, delay;
    @FXML
    private Button runButton;


    // plots results
    @FXML
    private LineChart<Integer, Double> sortingTimeLineChart;
    @FXML
    private LineChart<Integer, Double> requestTimeLineChart;
    @FXML
    private LineChart<Integer, Double> averageTimeForAllRequestsLineChart;

    // progress indicator
//    @FXML
//    private ProgressIndicator progressIndicator;

    @FXML
    private void initialize() {
        serverArchBox.setItems(serversArchList);
        varyingParameterBox.setItems(varyingParameters);

        // for testing
        ipValue.setText("localhost"); portValue.setText("8080");
        X.setText("1");
        min.setText("50"); max.setText("100"); step.setText("20");
        M.setText("3"); N.setText("1000"); delay.setText("1000");
        checkBoxOrSetDefault(serverArchBox);
        checkBoxOrSetDefault(varyingParameterBox);
    }

    public void onRunButtonPressed(ActionEvent event) throws IOException {

        // plots
        sortingTimeLineChart.getData().clear();
        requestTimeLineChart.getData().clear();
        averageTimeForAllRequestsLineChart.getData().clear();

        // plots data
        // data results
        XYChart.Series<Integer, Double> sortingTimes = new XYChart.Series<>();
        XYChart.Series<Integer, Double> requestingTimes = new XYChart.Series<>();
        XYChart.Series<Integer, Double> timeForAllRequests = new XYChart.Series<>();

        // CHECK FILLED DATA
//        if (checkFieldsFillingFAILED()) {
//            System.out.println("Not all the fields were filled");
//            new Alert(Alert.AlertType.INFORMATION, "fill aaaaall fields please").showAndWait();
//            return;
//        }

        // PARSE FILLED DATA
        InetAddress inetAddress = parseInetAddress();
        short port = parsePort();
        TestingParameters testingParameters = parseParameters();

        boolean serverStatus = false;

        // SEND REQUEST TO RUN SERVER
        // TODO what port + inetaddress to choose
        try (Connection serverManager2gui = new Connection(new Socket("localhost", 6666))) {
            serverManager2gui.sendRequestByte(RUN_SERVER);
            serverManager2gui.sendServerType(parseServerTypeFromServerArchBox());
            serverManager2gui.sendInetAddress(inetAddress);
            serverManager2gui.sendPort(port);
            serverManager2gui.sendParameters(testingParameters);

            serverStatus =  serverManager2gui.getServerStatus();
        } catch (IOException e) {
            showAlertMessage();
            e.printStackTrace();
            return;
        }

        ClientManager clientManager = new ClientManager(testingParameters, inetAddress, port);

        if (serverStatus) {
            try {
                clientManager.runAllClients();
            } catch (IOException e) {
                showAlertMessage();
                e.printStackTrace();
                return;
            }
        }

        clientManager.shutdown();

        List<StatisticsResultPerIteration> statisticsResults = null;

        try (Connection serverManager2gui = new Connection(new Socket("localhost", 6666))) {
            serverManager2gui.sendRequestByte(SEND_RESULTS);

             statisticsResults = serverManager2gui.getStatisticsResults();

        } catch (IOException | ClassNotFoundException e) {
            showAlertMessage();
            e.printStackTrace();
            return;
        }

        if (statisticsResults != null) {
            for (StatisticsResultPerIteration result : statisticsResults) {
                sortingTimes.getData().add(new XYChart.Data<>(
                    result.getCurrentValueOfVaryingParameter(),
                    result.getAverageValues().getSortingTime()));

                requestingTimes.getData().add(new XYChart.Data<>(
                    result.getCurrentValueOfVaryingParameter(),
                    result.getAverageValues().getRequestingTime()));

                timeForAllRequests.getData().add(new XYChart.Data<>(
                    result.getCurrentValueOfVaryingParameter(),
                    result.getAverageValues().getTimeForAllRequests()));
            }

            sortingTimeLineChart.getData().add(sortingTimes);
            requestTimeLineChart.getData().add(requestingTimes);
            averageTimeForAllRequestsLineChart.getData().add(timeForAllRequests);
            writeResultsToFile(statisticsResults);
        }
    }

    private void writeResultsToFile(List<StatisticsResultPerIteration> statisticsResults) throws IOException {
        // write to file
        String fileForResultsName = FileSystems.getDefault().getPath(".").toAbsolutePath().toString()
            + File.separator
            + "PerformanceTesterResults"
            + System.currentTimeMillis()
            + ".csv";

        File fileForResults = new File(fileForResultsName);
        try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(fileForResults), StandardCharsets.UTF_8))) {
            for (StatisticsResultPerIteration result : statisticsResults) {
                fileWriter.write(result.getCurrentValueOfVaryingParameter() + ";"
                        + result.getAverageValues().getSortingTime() + ";"
                        + result.getAverageValues().getRequestingTime() + ";"
                        + result.getAverageValues().getTimeForAllRequests() + "\n"
                    );
            }
        } catch (UnsupportedEncodingException ignored) {
            new Alert(Alert.AlertType.INFORMATION, "Can not write results to file!").showAndWait();
        }
    }

    private ServerType parseServerTypeFromServerArchBox() {
        switch (serverArchBox.getValue()) {
            case "threadPerClient": {
                return ServerType.threadPerClient;
            }
            case "sortInThreadPool": {
                return ServerType.sortInThreadPool;
            }
            case "nonBlockingServer": {
                return ServerType.nonBlockingServer;
            }
        }

        return null;
    }

    private void showAlertMessage() {
        Alert alert = new Alert(
            Alert.AlertType.ERROR,
                "Can not show plots.\n" +
                "Please, try again"
        );
        alert.setTitle("Something went wrong!");
        alert.showAndWait();
    }

    private InetAddress parseInetAddress() throws UnknownHostException {
        return InetAddress.getByName(ipValue.getText());
    }

    private short parsePort() {
        return Short.parseShort(portValue.getText());
    }

    private boolean checkFieldsFillingFAILED() {
        return ipValue.getText() == null || portValue.getText() == null
                || X.getText() == null
                || min.getText() == null || max.getText() == null || step.getText() == null
                || M.getText() == null || N.getText() == null || delay.getText() == null;
    }

    private void checkBoxOrSetDefault(ComboBox box) {
        if (box.getValue() == null) {
            box.getSelectionModel().selectFirst();
        }
    }

    private TestingParameters parseParameters() {
        // initialize varying parameter
        VaryingParameter varyingParameter = null;
        switch (varyingParameterBox.getValue().toString()) {
            case "M":
                varyingParameter = VaryingParameter.M;
                break;
            case "N":
                varyingParameter = VaryingParameter.N;
                break;
            case "delay":
                varyingParameter = VaryingParameter.delay;
                break;
        }

        // create new TestingParameters
        return new TestingParameters(
                Integer.parseInt(M.getText()),
                Integer.parseInt(N.getText()),
                Integer.parseInt(delay.getText()),
                Integer.parseInt(X.getText()),
                varyingParameter,
                Integer.parseInt(min.getText()),
                Integer.parseInt(max.getText()),
                Integer.parseInt(step.getText())
        );
    }
}
