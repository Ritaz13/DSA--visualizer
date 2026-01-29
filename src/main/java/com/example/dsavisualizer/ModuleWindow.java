package com.example.dsavisualizer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ModuleWindow {

    public static void open(String title) {

        try {
            FXMLLoader loader =
                    new FXMLLoader(ModuleWindow.class.getResource("/fxml/module.fxml"));

            Scene scene = new Scene(loader.load(), 500, 400);

            ThemeManager.applyLight(scene);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
