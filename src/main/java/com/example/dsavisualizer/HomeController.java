package com.example.dsavisualizer;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;

public class HomeController {

    @FXML
    private ToggleButton themeToggle;

    @FXML
    public void initialize() {

        themeToggle.setOnAction(e -> {
            Scene scene = themeToggle.getScene();

            if (themeToggle.isSelected())
                ThemeManager.applyDark(scene);
            else
                ThemeManager.applyLight(scene);
        });
    }

    @FXML
    private void openModule(javafx.event.ActionEvent event) {

        String text = ((javafx.scene.control.Button)event.getSource()).getText();

        ModuleWindow.open(text);
    }
}
