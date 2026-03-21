package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import com.example.dsavisualizer.manager.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class DPController extends ModuleController {

    // use the same enum values as AlgorithmController for simplicity
    public enum Topic {
        KNAPSACK,
        CHANGE,
        FIB,
        LCS,
        SEQALIGN
    }

    @FXML private Button knapsackBtn;
    @FXML private Button changeBtn;
    @FXML private Button fibBtn;
    @FXML private Button lcsBtn;
    @FXML private Button seqBtn;

    // Override the fields for FXML injection
    @FXML protected Label titleLabel;
    @FXML protected Button themeBtn;
    @FXML protected Button backBtn;
    @FXML protected TextArea storyArea;

    @FXML private BorderPane root;

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Dynamic Programming");
        storyArea.setText("Choose a dynamic programming problem from the list below.\n" +
                "Each topic will let you explore recursive, memoized and table-based solutions.");

        knapsackBtn.setOnAction(e -> openTopic(Topic.KNAPSACK));
        changeBtn.setOnAction(e -> openTopic(Topic.CHANGE));
        fibBtn.setOnAction(e -> openTopic(Topic.FIB));
        lcsBtn.setOnAction(e -> openTopic(Topic.LCS));
        seqBtn.setOnAction(e -> openTopic(Topic.SEQALIGN));
    }

    private void openTopic(Topic t) {
        // translate topic enum to algorithm enum by name
        AlgorithmController.currentAlgorithm = AlgorithmController.Algorithm.valueOf(t.name());
        SceneManager.switchScene("modules/algorithm.fxml");
    }
}
