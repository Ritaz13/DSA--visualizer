/*package com.example.dsavisualizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/dsavisualizer/Home.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/dsavisualizer/light-theme.css")).toExternalForm());

        HomeController controller = loader.getController();
        controller.setScene(scene); // pass scene reference to controller

        stage.setScene(scene);
        stage.setTitle("DSA Visualizer");
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}*/
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

