package com.example.dsavisualizer.manager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class SceneManager {

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void switchScene(String fxml) {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(SceneManager.class.getResource("/view/" + fxml))
            );

            Scene scene = new Scene(root);

            ThemeManager.applyTheme(scene);

            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

