package com.example.dsavisualizer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.Scene;

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
        Button btn = (Button) event.getSource();
        String moduleName = (btn.getUserData() != null) ? btn.getUserData().toString() : btn.getText();
        ModuleWindow.open(moduleName);
    }
}
