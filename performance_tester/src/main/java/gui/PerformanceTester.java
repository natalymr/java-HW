package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class PerformanceTester extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL url = PerformanceTester.class.getClassLoader().getResource("gui.fxml");
        Parent panel = FXMLLoader.load(url);

        Scene scene = new Scene(panel);

        stage.setTitle("Performance Tester");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
