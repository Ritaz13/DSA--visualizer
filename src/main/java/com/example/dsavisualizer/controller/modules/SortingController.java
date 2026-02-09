package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SortingController extends ModuleController {

    @FXML private TextField inputField;
    @FXML private ComboBox<String> algoCombo;
    @FXML private Slider speedSlider;
    @FXML private Button buildBtn;
    @FXML private Button startBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resetBtn;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private HBox vizArea;
    @FXML private TextArea codeArea;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private TextArea storyArea;

    private int currentStep = 0;
    private int[] arraySteps;

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Sorting Algorithms");
        storyArea.setText("Visualize sorting: Bubble, Insertion, Selection, Quick, Merge.\nWatch comparisons and swaps.");
        if (algoCombo != null) {
            algoCombo.getItems().addAll("Bubble Sort", "Insertion Sort", "Selection Sort", "Quick Sort", "Merge Sort");
            algoCombo.setValue("Bubble Sort");
        }
        buildBtn.setOnAction(e -> showAlert("Build coming soon"));
        startBtn.setOnAction(e -> showAlert("Start coming soon"));
        pauseBtn.setOnAction(e -> showAlert("Pause coming soon"));
        resetBtn.setOnAction(e -> showAlert("Reset coming soon"));
        prevBtn.setOnAction(e -> previousStep());
        nextBtn.setOnAction(e -> nextStep());
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

    private void previousStep() {
        if (currentStep > 0) {
            currentStep--;
            updateVisualization();
        }
    }

    private void nextStep() {
        currentStep++;
        updateVisualization();
    }

    private void updateVisualization() {
        // TODO: Implement step-by-step visualization
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/sorting.txt")) {
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