package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class BSTController extends ModuleController {

    @FXML private TextField inputField;
    @FXML private TextField opField;
    @FXML private Button buildBtn;
    @FXML private Button insertBtn;
    @FXML private Button deleteBtn;
    @FXML private Button searchBtn;
    @FXML private Button inOrderBtn;
    @FXML private Button preOrderBtn;
    @FXML private Button postOrderBtn;
    @FXML private CheckBox heightCheck;
    @FXML private CheckBox balanceCheck;
    @FXML private StackPane vizArea;
    @FXML private TextArea codeArea;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private TextArea storyArea;

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Binary Search Tree");
        storyArea.setText("BST: left < parent < right.\nUsed for sorted data and fast lookups.");
        buildBtn.setOnAction(e -> showAlert("Build feature coming soon"));
        insertBtn.setOnAction(e -> showAlert("Insert feature coming soon"));
        deleteBtn.setOnAction(e -> showAlert("Delete feature coming soon"));
        searchBtn.setOnAction(e -> showAlert("Search feature coming soon"));
        inOrderBtn.setOnAction(e -> showAlert("In-Order traversal coming soon"));
        preOrderBtn.setOnAction(e -> showAlert("Pre-Order traversal coming soon"));
        postOrderBtn.setOnAction(e -> showAlert("Post-Order traversal coming soon"));
        showCodeBtn.setOnAction(e -> toggleCode());
        copyCodeBtn.setOnAction(e -> copyCode());
        loadCodeFile();
    }

    @Override
    protected void toggleCode() { codeArea.setVisible(!codeArea.isVisible()); }

    private void copyCode() {
        String code = codeArea.getText();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
        cc.putString(code);
        clipboard.setContent(cc);
        showAlert("Code copied to clipboard!");
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/bst.txt")) {
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append('\n');
                    codeArea.setText(sb.toString());
                    codeArea.setVisible(false);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }
}