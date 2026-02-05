package com.example.dsavisualizer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import com.example.dsavisualizer.manager.SceneManager;
import com.example.dsavisualizer.manager.ThemeManager;

public class HomeController {

    @FXML private Button themeBtn;

    @FXML private Button arrayBtn, recursionBtn, graphBtn, bstBtn,
            linkedBtn, stackBtn, queueBtn, sortingBtn;

    @FXML
    private void openModule(ActionEvent e) {

        Button btn = (Button) e.getSource();

        if (btn == arrayBtn) SceneManager.switchScene("modules/array.fxml");
        else if (btn == recursionBtn) SceneManager.switchScene("modules/recursion.fxml");
        else if (btn == graphBtn) SceneManager.switchScene("modules/graph.fxml");
        else if (btn == bstBtn) SceneManager.switchScene("modules/bst.fxml");
        else if (btn == linkedBtn) SceneManager.switchScene("modules/linkedlist.fxml");
        else if (btn == stackBtn) SceneManager.switchScene("modules/stack.fxml");
        else if (btn == queueBtn) SceneManager.switchScene("modules/queue.fxml");
        else if (btn == sortingBtn) SceneManager.switchScene("modules/sorting.fxml");
    }

    @FXML
    private void toggleTheme() {

        Scene scene = themeBtn.getScene();

        ThemeManager.toggleTheme(scene);

        themeBtn.setText(
                ThemeManager.isDark() ? "Light Mode" : "Dark Mode"
        );
    }
}

