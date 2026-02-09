package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Stack;
import java.util.Optional;

public class StackController extends ModuleController {

    @FXML private TextField inputField;
    @FXML private Button pushBtn;
    @FXML private Button popBtn;
    @FXML private Button peekBtn;
    @FXML private Button topBtn;
    @FXML private Button clearBtn;
    @FXML private VBox vizArea;
    @FXML private TextArea codeArea;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private TextArea storyArea;

    private final Stack<Integer> stack = new Stack<>();

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Stack");
        storyArea.setText("Stack is LIFO (Last In First Out).\nPush adds to top, Pop removes from top.\nThink of a stack of plates.");
        pushBtn.setOnAction(e -> pushElement());
        popBtn.setOnAction(e -> popElement());
        peekBtn.setOnAction(e -> peekElement());
        topBtn.setOnAction(e -> showTop());
        clearBtn.setOnAction(e -> clearStack());
        showCodeBtn.setOnAction(e -> toggleCode());
        copyCodeBtn.setOnAction(e -> copyCode());
        loadCodeFile();
    }

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
        try (InputStream is = getClass().getResourceAsStream("/codes/stack.txt")) {
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

    private void pushElement() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        try {
            int val = Integer.parseInt(text);
            stack.push(val);
            renderStack();
            inputField.clear();
        } catch (NumberFormatException e) {
            showAlert("Enter a valid number");
        }
    }

    private void popElement() {
        if (stack.isEmpty()) {
            showAlert("Stack underflow");
            return;
        }
        stack.pop();
        renderStack();
    }

    private void peekElement() {
        if (stack.isEmpty()) {
            showAlert("Stack is empty");
            return;
        }
        showAlert("Top element: " + stack.peek());
    }

    private void showTop() { peekElement(); }

    private void clearStack() {
        stack.clear();
        renderStack();
    }

    private void renderStack() {
        vizArea.getChildren().clear();
        for (int i = stack.size() - 1; i >= 0; i--) {
            Label lbl = new Label(String.valueOf(stack.get(i)));
            lbl.setStyle("-fx-border-color:black; -fx-border-width:2; -fx-padding:10; -fx-background-color:#ffcccc; -fx-font-size:14; -fx-font-weight:bold;");
            vizArea.getChildren().add(lbl);
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }
}