package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

public class QueueController extends ModuleController {

    @FXML private TextField inputField;
    @FXML private Button enqueueBtn;
    @FXML private Button dequeueBtn;
    @FXML private Button frontBtn;
    @FXML private Button rearBtn;
    @FXML private Button clearBtn;
    @FXML private CheckBox circularCheck;
    @FXML private CheckBox priorityCheck;
    @FXML private HBox vizArea;
    @FXML private TextArea codeArea;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private TextArea storyArea;

    private final Queue<Integer> queue = new LinkedList<>();

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Queue");
        storyArea.setText("Queue is FIFO (First In First Out).\nEnqueue at rear, Dequeue from front.\nCircular queue reuses space.");
        enqueueBtn.setOnAction(e -> enqueue());
        dequeueBtn.setOnAction(e -> dequeue());
        frontBtn.setOnAction(e -> showFront());
        rearBtn.setOnAction(e -> showRear());
        clearBtn.setOnAction(e -> clearQ());
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
        try (InputStream is = getClass().getResourceAsStream("/codes/queue.txt")) {
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

    private void enqueue() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        try {
            int val = Integer.parseInt(text);
            queue.add(val);
            render();
            inputField.clear();
        } catch (NumberFormatException e) {
            showAlert("Valid number required");
        }
    }

    private void dequeue() {
        if (queue.isEmpty()) {
            showAlert("Queue underflow");
            return;
        }
        queue.poll();
        render();
    }

    private void showFront() {
        if (queue.isEmpty()) {
            showAlert("Queue empty");
            return;
        }
        showAlert("Front: " + queue.peek());
    }

    private void showRear() {
        if (queue.isEmpty()) {
            showAlert("Queue empty");
            return;
        }
        Object[] arr = queue.toArray();
        showAlert("Rear: " + arr[arr.length-1]);
    }

    private void clearQ() {
        queue.clear();
        render();
    }

    private void render() {
        vizArea.getChildren().clear();
        for (int val : queue) {
            Label lbl = new Label(String.valueOf(val));
            lbl.setStyle("-fx-border-color:black; -fx-border-width:2; -fx-padding:8 12; -fx-background-color:#ccf; -fx-font-weight:bold;");
            vizArea.getChildren().add(lbl);
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }
}
