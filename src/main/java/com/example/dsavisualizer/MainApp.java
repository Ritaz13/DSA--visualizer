
package com.example.dsavisualizer;

import javafx.application.Application;
import javafx.stage.Stage;
import com.example.dsavisualizer.manager.SceneManager;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        SceneManager.setStage(stage);
        SceneManager.switchScene("home.fxml");

        stage.setTitle("DSA Visualizer");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

