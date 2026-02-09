package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArrayController extends ModuleController {

    @FXML private TextField inputField;
    @FXML private Button buildBtn;
    @FXML private Button insertBtn;
    @FXML private Button deleteBtn;
    @FXML private Button searchBtn;
    @FXML private Button reverseBtn;
    @FXML private Button updateBtn;
    @FXML private HBox vizArea;
    @FXML private TextArea codeArea;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private TextArea storyArea;

    private final List<Integer> data = new ArrayList<>();

    @Override
    protected void initialize() {
        super.initialize();

        titleLabel.setText("Array");
        storyArea.setText("Visualize arrays: indices shown above values.\nUse the input box to build an initial array.");

        // wire buttons
        buildBtn.setOnAction(e -> buildFromInput());
        insertBtn.setOnAction(e -> insertDialog());
        deleteBtn.setOnAction(e -> deleteDialog());
        searchBtn.setOnAction(e -> searchDialog());
        reverseBtn.setOnAction(e -> reverseAnimated());
        updateBtn.setOnAction(e -> updateDialog());
        showCodeBtn.setOnAction(e -> toggleCodeArea());
        copyCodeBtn.setOnAction(e -> copyCode());

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

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/array.txt")) {
            if (is == null) return;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                codeArea.setText(sb.toString());
                codeArea.setVisible(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildFromInput() {
        String text = inputField.getText();
        data.clear();
        if (text == null || text.trim().isEmpty()) {
            renderArray();
            return;
        }
        String[] parts = text.split("[,\\s]+");
        for (String p : parts) {
            try {
                data.add(Integer.parseInt(p.trim()));
            } catch (NumberFormatException ignore) {
            }
        }
        renderArray();
    }

    private void renderArray() {
        vizArea.getChildren().clear();
        for (int i = 0; i < data.size(); i++) {
            int val = data.get(i);
            VBox cell = buildCell(i, val);
            vizArea.getChildren().add(cell);
        }
    }

    private VBox buildCell(int index, int value) {
        Label idx = new Label(String.valueOf(index));
        idx.setFont(Font.font(12));
        idx.setStyle("-fx-padding: 0 0 4 0;");
        Rectangle rect = new Rectangle(80, 40, Color.web("#ffffff"));
        rect.setStroke(Color.GRAY);
        StackPane box = new StackPane(rect, new Label(String.valueOf(value)));
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        VBox v = new VBox(idx, box);
        v.setSpacing(4);
        v.setStyle("-fx-alignment:center; -fx-padding:6;");
        return v;
    }

    private void insertDialog() {
        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Insert");

        TextField valueField = new TextField();
        valueField.setPromptText("value");
        TextField indexField = new TextField();
        indexField.setPromptText("index (optional)");

        VBox box = new VBox(6, new Label("Value:"), valueField, new Label("Index (empty = append):"), indexField);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                List<Integer> res = new ArrayList<>();
                try {
                    res.add(Integer.parseInt(valueField.getText().trim()));
                } catch (Exception ex) {
                    return null;
                }
                try {
                    res.add(Integer.parseInt(indexField.getText().trim()));
                } catch (Exception ignore) {
                }
                return res;
            }
            return null;
        });

        Optional<List<Integer>> opt = dialog.showAndWait();
        opt.ifPresent(list -> {
            int value = list.get(0);
            Integer idx = list.size() > 1 ? list.get(1) : null;
            if (idx == null) {
                data.add(value);
                // animate append
                renderArray();
                flashCell(data.size() - 1);
            } else {
                int i = Math.max(0, Math.min(idx, data.size()));
                data.add(i, value);
                animateShiftRight(i);
            }
        });
    }

    private void deleteDialog() {
        TextInputDialog td = new TextInputDialog();
        td.setTitle("Delete");
        td.setHeaderText("Enter index to delete");
        Optional<String> res = td.showAndWait();
        res.ifPresent(s -> {
            try {
                int idx = Integer.parseInt(s.trim());
                if (idx >= 0 && idx < data.size()) {
                    animateRemove(idx);
                } else {
                    alert("Index out of range");
                }
            } catch (NumberFormatException ex) {
                alert("Invalid number");
            }
        });
    }

    private void searchDialog() {
        TextInputDialog td = new TextInputDialog();
        td.setTitle("Search");
        td.setHeaderText("Enter value to search");
        Optional<String> res = td.showAndWait();
        res.ifPresent(s -> {
            try {
                int v = Integer.parseInt(s.trim());
                animateSearch(v);
            } catch (NumberFormatException ex) {
                alert("Invalid number");
            }
        });
    }

    private void updateDialog() {
        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Update");

        TextField indexField = new TextField();
        indexField.setPromptText("index");
        TextField valueField = new TextField();
        valueField.setPromptText("new value");

        VBox box = new VBox(6, new Label("Index:"), indexField, new Label("New value:"), valueField);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                List<Integer> out = new ArrayList<>();
                try {
                    out.add(Integer.parseInt(indexField.getText().trim()));
                    out.add(Integer.parseInt(valueField.getText().trim()));
                    return out;
                } catch (Exception ex) {
                    return null;
                }
            }
            return null;
        });

        Optional<List<Integer>> opt = dialog.showAndWait();
        opt.ifPresent(list -> {
            int idx = list.get(0);
            int val = list.get(1);
            if (idx >= 0 && idx < data.size()) {
                data.set(idx, val);
                renderArray();
                flashCell(idx);
            } else alert("Index out of range");
        });
    }

    private void reverseAnimated() {
        Collections.reverse(data);
        // simple fade out/in animation
        FadeTransition ft = new FadeTransition(Duration.millis(250), vizArea);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        renderArray();
        ft.play();
    }

    private void animateShiftRight(int fromIndex) {
        // rebuild immediately but animate translation of new cell from left
        renderArray();
        if (fromIndex < 0 || fromIndex >= vizArea.getChildren().size()) return;
        Region node = (Region) vizArea.getChildren().get(fromIndex);
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
        tt.setFromX(-40);
        tt.setToX(0);
        tt.play();
    }

    private void animateRemove(int idx) {
        if (idx < 0 || idx >= data.size()) return;
        // fade out the cell then remove and shift
        Region node = (Region) vizArea.getChildren().get(idx);
        FadeTransition ft = new FadeTransition(Duration.millis(250), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            data.remove(idx);
            renderArray();
        });
        ft.play();
    }

    private void animateSearch(int value) {
        // highlight visited elements sequentially
        SequentialTransition seq = new SequentialTransition();
        boolean[] found = {false};
        for (int i = 0; i < data.size(); i++) {
            int idx = i;
            PauseTransition pt = new PauseTransition(Duration.millis(250));
            pt.setOnFinished(e -> {
                highlightCell(idx, Color.web("#ffe082"));
                if (data.get(idx) == value) {
                    found[0] = true;
                    highlightCell(idx, Color.web("#80deea"));
                }
            });
            seq.getChildren().add(pt);
        }
        seq.setOnFinished(e -> {
            if (!found[0]) alert("Value not found");
        });
        seq.play();
    }

    private void highlightCell(int idx, Color color) {
        if (idx < 0 || idx >= vizArea.getChildren().size()) return;
        VBox v = (VBox) vizArea.getChildren().get(idx);
        StackPane box = (StackPane) v.getChildren().get(1);
        Rectangle rect = (Rectangle) box.getChildren().getFirst();
        rect.setFill(color);
        PauseTransition pt = new PauseTransition(Duration.millis(450));
        pt.setOnFinished(e -> rect.setFill(Color.web("#ffffff")));
        pt.play();
    }

    private void flashCell(int idx) {
        if (idx < 0 || idx >= vizArea.getChildren().size()) return;
        VBox v = (VBox) vizArea.getChildren().get(idx);
        StackPane box = (StackPane) v.getChildren().get(1);
        Rectangle rect = (Rectangle) box.getChildren().get(0);
        FillTransition ft = new FillTransition(Duration.millis(300), rect, Color.web("#ffffff"), Color.web("#c8e6c9"));
        ft.setCycleCount(2);
        ft.setAutoReverse(true);
        ft.play();
    }

    private void alert(String msg) {
        showAlert(msg);
    }
}