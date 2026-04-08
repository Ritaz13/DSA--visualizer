package com.example.dsavisualizer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import com.example.dsavisualizer.manager.SceneManager;
import com.example.dsavisualizer.manager.ThemeManager;

public class HomeController {

    @FXML
    private Button themeBtn;

    @FXML
    private Button heapBtn, recursionBtn, graphBtn, bstBtn,
            dpBtn, stackBtn, queueBtn, sortingBtn, arrayBtn, linkedBtn;

    @FXML
    private void openModule(ActionEvent e) {

        Button btn = (Button) e.getSource();

        String id = btn.getId();

        if ("heapBtn".equals(id))
            SceneManager.switchScene("modules/heap.fxml");
        else if ("recursionBtn".equals(id))
            SceneManager.switchScene("modules/recursion.fxml");
        else if ("graphBtn".equals(id))
            SceneManager.switchScene("modules/graph.fxml");
        else if ("bstBtn".equals(id))
            SceneManager.switchScene("modules/bst.fxml");
        else if ("dpBtn".equals(id))
            SceneManager.switchScene("modules/dp.fxml");
        else if ("stackBtn".equals(id))
            SceneManager.switchScene("modules/stack.fxml");
        else if ("queueBtn".equals(id))
            SceneManager.switchScene("modules/queue.fxml");
        else if ("sortingBtn".equals(id))
            SceneManager.switchScene("modules/sorting.fxml");
        else if ("arrayBtn".equals(id))
            SceneManager.switchScene("modules/array.fxml");
        else if ("linkedBtn".equals(id))
            SceneManager.switchScene("modules/linkedlist.fxml");
         

       
    }
  
    @FXML
    private void toggleTheme() {

        Scene scene = themeBtn.getScene();

        ThemeManager.toggleTheme(scene);

        themeBtn.setText(
                ThemeManager.isDark() ? "Light Mode" : "Dark Mode");
    }

}
