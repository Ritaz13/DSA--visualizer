package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class arrayController extends ModuleController {

    @FXML
    private StackPane vizArea;
    @FXML
    private Button startBtn, playBtn, pauseBtn, prevBtn, nextBtn;
    @FXML
    private Button insertBtn, appendBtn, removeBtn, twoSumBtn, getBtn, searchBtn, showCodeBtn, copyCodeBtn;
    @FXML
    private TextField indexField, valueField, targetField;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea codeArea;

    private GridPane arrayGrid;
    private List<Step> steps = new ArrayList<>();
    private int currentStep = -1;
    private Timeline playTimeline;
    private Map<String, Label> cellMap = new HashMap<>();
    private List<Integer> arr = new ArrayList<>(Arrays.asList(5, 8, 2, 9, 1));

    // Step structure
    private static class Step {
        String message;
        List<Integer> highlightIndices;

        Step(String msg, List<Integer> indices) {
            message = msg;
            highlightIndices = new ArrayList<>(indices);
        }
    }

    @FXML
    public void initialize() {
        super.initialize();
        titleLabel.setText("Array");
        storyArea.setText("Imagine you have a smart backpack .\n" +
                "At the beginning, it has space for only 3 books. You neatly place your books one after another:\n" +
                "Math →Physics →Chemistry\n" +
                "Everything is arranged in order. You can quickly grab any book just by knowing its position (index).");

        setupControlButtons();
        setupArrayGrid();
        loadCodeFile();
    }

    private void setupControlButtons() {
        startBtn.setOnAction(e -> startAnimation());
        playBtn.setOnAction(e -> playAnimation());
        pauseBtn.setOnAction(e -> pauseAnimation());
        prevBtn.setOnAction(e -> prevStep());
        nextBtn.setOnAction(e -> nextStep());

        insertBtn.setOnAction(e -> insertElement());
        appendBtn.setOnAction(e -> appendElement());
        removeBtn.setOnAction(e -> removeElement());
        getBtn.setOnAction(e -> getElement());
        searchBtn.setOnAction(e -> searchElement());
        twoSumBtn.setOnAction(e -> twoSumSearch());
        showCodeBtn.setOnAction(e -> toggleCode());
        copyCodeBtn.setOnAction(e -> copyCode());
    }


    protected void toggleCode() {
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
        try (InputStream is = getClass().getResourceAsStream("/codes/array.txt")) {
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                        sb.append(line).append('\n');
                    codeArea.setText(sb.toString());
                    codeArea.setVisible(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupArrayGrid() {
        arrayGrid = new GridPane();
        arrayGrid.setPadding(new Insets(10));
        arrayGrid.setHgap(10);

        vizArea.getChildren().clear();
        cellMap.clear();

        for (int i = 0; i < arr.size(); i++) {
            Label cell = new Label(String.valueOf(arr.get(i)));
            cell.setPrefSize(60, 60);
            cell.setStyle("-fx-border-color:black; -fx-font-size:18; -fx-alignment:center;");
            arrayGrid.add(cell, i, 0);
            cellMap.put("0," + i, cell);
        }

        vizArea.getChildren().add(arrayGrid);
    }

    private void recordStep(String msg, List<Integer> indices) {
        steps.add(new Step(msg, indices));
    }

    private void redrawCurrentStep() {
        if (currentStep < 0 || currentStep >= steps.size())
            return;
        Step step = steps.get(currentStep);
        statusLabel.setText(step.message);

        // Reset all cells
        for (Label cell : cellMap.values()) {
            cell.setStyle("-fx-border-color:black; -fx-font-size:18; -fx-alignment:center;");
        }

        for (int idx : step.highlightIndices) {
            Label cur = cellMap.get("0," + idx);
            if (cur != null) {
                cur.setStyle(
                        "-fx-background-color:orange; -fx-border-color:black; -fx-font-size:18; -fx-alignment:center;");
            }
        }
    }


    private void startAnimation() {
        currentStep = 0;
        redrawCurrentStep();
    }

    private void playAnimation() {
        if (steps.isEmpty())
            return;
        playTimeline = new Timeline();
        for (int i = 0; i < steps.size(); i++) {
            int idx = i;
            KeyFrame kf = new KeyFrame(Duration.seconds(1.0 * i), e -> {
                currentStep = idx;
                redrawCurrentStep();
            });
            playTimeline.getKeyFrames().add(kf);
        }
        playTimeline.play();
    }

    private void pauseAnimation() {
        if (playTimeline != null)
            playTimeline.pause();
    }

    private void prevStep() {
        if (currentStep > 0) {
            currentStep--;
            redrawCurrentStep();
        }
    }

    private void nextStep() {
        if (currentStep < steps.size() - 1) {
            currentStep++;
            redrawCurrentStep();
        }
    }

    private void insertElement() {
        steps.clear();
        try {
            int idx = Integer.parseInt(indexField.getText().trim());
            int val = Integer.parseInt(valueField.getText().trim());
            arr.add(idx, val);
            setupArrayGrid();
            statusLabel.setText("Inserted " + val + " at index " + idx + " | Time: O(n)");
            recordStep("Inserted " + val + " at index " + idx, List.of(idx));
            currentStep = 0;
            redrawCurrentStep();
        } catch (Exception e) {
            statusLabel.setText("Enter valid index and value!");
        }
    }

    private void appendElement() {
        steps.clear();
        try {
            int val = Integer.parseInt(valueField.getText().trim());
            arr.add(val);
            setupArrayGrid();
            statusLabel.setText("Appended " + val + " at end | Time: O(1) amortized");
            recordStep("Appended " + val + " at end", List.of(arr.size() - 1));
            currentStep = 0;
            redrawCurrentStep();
        } catch (Exception e) {
            statusLabel.setText("Enter valid value!");
        }
    }

    private void removeElement() {
        steps.clear();
        try {
            int idx = Integer.parseInt(indexField.getText().trim());
            int val = arr.remove(idx);
            setupArrayGrid();
            statusLabel.setText("Removed " + val + " from index " + idx + " | Time: O(n)");
            recordStep("Removed " + val + " from index " + idx, List.of(idx));
            currentStep = 0;
            redrawCurrentStep();
        } catch (Exception e) {
            statusLabel.setText("Enter valid index!");
        }
    }

    private void getElement() {
        steps.clear();
        try {
            int idx = Integer.parseInt(indexField.getText().trim());
            int val = arr.get(idx);
            statusLabel.setText("Get Operation: Time O(1) - Direct index access, constant time");
            recordStep("Accessed index " + idx + " = " + val + " | Time: O(1)", List.of(idx));
            currentStep = 0;
            redrawCurrentStep();
        } catch (Exception e) {
            statusLabel.setText("Enter valid index!");
        }
    }

    private void searchElement() {
        steps.clear();
        try {
            int val = Integer.parseInt(valueField.getText().trim());
            statusLabel.setText("Search Operation: Time O(n) - Linear search checks each element until found");
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i) == val) {
                    recordStep("Found " + val + " at index " + i + " | Time: O(n)", List.of(i));
                    currentStep = 0;
                    redrawCurrentStep();
                    return;
                }
            }
            recordStep("Value " + val + " not found | Time: O(n)", List.of());
            currentStep = 0;
            redrawCurrentStep();
        } catch (Exception e) {
            statusLabel.setText("Enter valid value!");
        }
    }

    private void twoSumSearch() {
        steps.clear();
        try {
            int target = Integer.parseInt(targetField.getText().trim());
            statusLabel.setText("Two Sum Operation: Time O(n²) - Nested loops check all pairs");
            boolean found = false;
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                for (int j = i + 1; j < arr.size(); j++) {
                    if (arr.get(i) + arr.get(j) == target) {
                        indices.add(i);
                        indices.add(j);
                        found = true;
                        break;
                    }
                }
                if (found)
                    break;
            }
            if (found) {
                recordStep("Two Sum found: " + arr.get(indices.get(0)) + " + " + arr.get(indices.get(1)) + " = "
                        + target + " | Time: O(n^2)", indices);
            } else {
                recordStep("No two sum pair found for target " + target + " | Time: O(n^2)", List.of());
            }
            currentStep = 0;
            redrawCurrentStep();
        } catch (Exception e) {
            statusLabel.setText("Enter valid target!");
        }
    }
}