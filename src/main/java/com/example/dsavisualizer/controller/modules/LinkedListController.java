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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinkedListController extends ModuleController {

    @FXML private TextField inputField;
    @FXML private Button buildBtn;
    @FXML private Button insertBtn;
    @FXML private Button deleteBtn;
    @FXML private Button searchBtn;
    @FXML private Button reverseBtn;
    @FXML private Button updateBtn;
    @FXML private CheckBox doublyCheck;
    @FXML private HBox vizArea;
    @FXML private TextArea codeArea;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private TextArea storyArea;

    private final List<Integer> data = new ArrayList<>();
    private boolean isDoubly = false;

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Linked List");
        storyArea.setText("A linked list uses arrows to show next pointers.\nEach node contains a value and points to the next node.\nDoubly linked lists also point backwards.");
        buildBtn.setOnAction(e -> buildFromInput());
        insertBtn.setOnAction(e -> insertDialog());
        deleteBtn.setOnAction(e -> deleteDialog());
        searchBtn.setOnAction(e -> searchDialog());
        reverseBtn.setOnAction(e -> reverseAnimated());
        updateBtn.setOnAction(e -> updateDialog());
        showCodeBtn.setOnAction(e -> toggleCodeArea());
        copyCodeBtn.setOnAction(e -> copyCode());
        doublyCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isDoubly = newVal;
            renderList();
        });
        loadCodeFile();
    }

    private void toggleCodeArea() {
        codeArea.setVisible(!codeArea.isVisible());
    }

    private void copyCode() {
        String code = codeArea.getText();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
        cc.putString(code);
        clipboard.setContent(cc);
        showAlert("Code copied to clipboard!");
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/linkedlist.txt")) {
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    codeArea.setText(sb.toString());
                    codeArea.setVisible(false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildFromInput() {
        String text = inputField.getText();
        data.clear();
        if (text == null || text.trim().isEmpty()) {
            renderList();
            return;
        }
        String[] parts = text.split("[,\\s]+");
        for (String p : parts) {
            try {
                data.add(Integer.parseInt(p.trim()));
            } catch (NumberFormatException ignore) {}
        }
        renderList();
    }

    private void renderList() {
        vizArea.getChildren().clear();
        for (int i = 0; i < data.size(); i++) {
            int val = data.get(i);
            VBox node = buildNode(val);
            vizArea.getChildren().add(node);
            if (i < data.size() - 1) {
                Label arrow = new Label(isDoubly ? "<=>" : "=>");
                arrow.setStyle("-fx-font-size:14; -fx-padding:0 8;");
                vizArea.getChildren().add(arrow);
            }
        }
    }

    private VBox buildNode(int value) {
        VBox box = new VBox(2);
        box.setStyle("-fx-border-color:black; -fx-border-width:2; -fx-padding:8; -fx-background-color:#e3f2fd; -fx-alignment:center;");
        Label val = new Label(String.valueOf(value));
        val.setStyle("-fx-font-size:14; -fx-font-weight:bold;");
        box.getChildren().add(val);
        return box;
    }

    private void insertDialog() {
        Dialog<List<Integer>> d = new Dialog<>();
        d.setTitle("Insert");
        TextField vf = new TextField();
        vf.setPromptText("value");
        TextField inf = new TextField();
        inf.setPromptText("index (optional)");
        VBox box = new VBox(6, new Label("Value:"), vf, new Label("Index:"), inf);
        d.getDialogPane().setContent(box);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                List<Integer> res = new ArrayList<>();
                try {
                    res.add(Integer.parseInt(vf.getText().trim()));
                    if (!inf.getText().trim().isEmpty()) {
                        res.add(Integer.parseInt(inf.getText().trim()));
                    }
                    return res;
                } catch (Exception e) { return null; }
            }
            return null;
        });
        Optional<List<Integer>> opt = d.showAndWait();
        opt.ifPresent(list -> {
            int idx = list.size() > 1 ? Math.min(list.get(1), data.size()) : data.size();
            data.add(idx, list.get(0));
            renderList();
        });
    }

    private void deleteDialog() {
        TextInputDialog td = new TextInputDialog();
        td.setTitle("Delete");
        td.setHeaderText("Index to delete:");
        Optional<String> res = td.showAndWait();
        res.ifPresent(s -> {
            try {
                int idx = Integer.parseInt(s.trim());
                if (idx >= 0 && idx < data.size()) {
                    data.remove(idx);
                    renderList();
                } else showAlert("Index out of range");
            } catch (NumberFormatException e) { showAlert("Invalid"); }
        });
    }

    private void searchDialog() {
        TextInputDialog td = new TextInputDialog();
        td.setTitle("Search");
        td.setHeaderText("Value to find:");
        Optional<String> res = td.showAndWait();
        res.ifPresent(s -> {
            try {
                int v = Integer.parseInt(s.trim());
                int idx = data.indexOf(v);
                if (idx >= 0) {
                    showAlert("Found at index " + idx);
                } else {
                    showAlert("Not found");
                }
            } catch (NumberFormatException e) { showAlert("Invalid"); }
        });
    }

    private void updateDialog() {
        Dialog<List<Integer>> d = new Dialog<>();
        d.setTitle("Update");
        TextField inf = new TextField();
        inf.setPromptText("index");
        TextField vf = new TextField();
        vf.setPromptText("new value");
        VBox box = new VBox(6, new Label("Index:"), inf, new Label("New value:"), vf);
        d.getDialogPane().setContent(box);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                List<Integer> res = new ArrayList<>();
                try {
                    res.add(Integer.parseInt(inf.getText().trim()));
                    res.add(Integer.parseInt(vf.getText().trim()));
                    return res;
                } catch (Exception e) { return null; }
            }
            return null;
        });
        Optional<List<Integer>> opt = d.showAndWait();
        opt.ifPresent(list -> {
            if (list.get(0) >= 0 && list.get(0) < data.size()) {
                data.set(list.get(0), list.get(1));
                renderList();
            } else showAlert("Index out of range");
        });
    }

    private void reverseAnimated() {
        java.util.Collections.reverse(data);
        renderList();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }
}