package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    @FXML private ScrollPane vizScroll;
    @FXML private Label stepInfoLabel;
    @FXML private Label actionLabel;
    @FXML private HBox inputArrayBox;
    @FXML private HBox outputArrayBox;

    private int[] array;
    private int[] originalArray;
    private List<SortStep> sortSteps;
    private int currentStep = 0;
    private boolean isSorting = false;
    private boolean isPaused = false;
    private Timeline animationTimeline;
    private List<VBox> barVisuals;
    private static final int MAX_HEIGHT = 400;
    private static final int MAX_BARS = 50;

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Sorting Algorithms");
        storyArea.setText("""
                Sorting organizes data in ascending or descending order.
                Watch the visualization as we compare and swap elements.
                
                Common Algorithms:
                • Bubble Sort: Simple, O(n²)
                • Selection Sort: Find minimum, O(n²)
                • Insertion Sort: Build sorted array, O(n²)
                • Quick Sort: Divide and conquer, O(n log n)
                • Merge Sort: Divide and conquer, O(n log n)
                """);

        barVisuals = new ArrayList<>();
        sortSteps = new ArrayList<>();

        if (algoCombo != null) {
            algoCombo.getItems().addAll("Bubble Sort", "Selection Sort", "Insertion Sort", "Quick Sort", "Merge Sort");
            algoCombo.setValue("Bubble Sort");
        }

        buildBtn.setOnAction(e -> buildArray());
        startBtn.setOnAction(e -> startSorting());
        pauseBtn.setOnAction(e -> pauseSorting());
        resetBtn.setOnAction(e -> resetSort());
        prevBtn.setOnAction(e -> previousStep());
        nextBtn.setOnAction(e -> nextStep());
        showCodeBtn.setOnAction(e -> toggleCode());
        copyCodeBtn.setOnAction(e -> copyCode());

        pauseBtn.setDisable(true);
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);

        loadCodeFile();
    }

    private void buildArray() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            showAlert("Please enter numbers separated by commas");
            return;
        }

        try {
            String[] parts = text.split("[,\\s]+");
            if (parts.length > MAX_BARS) {
                showAlert("Maximum " + MAX_BARS + " elements allowed");
                return;
            }

            array = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                array[i] = Integer.parseInt(parts[i].trim());
            }

            originalArray = array.clone();
            currentStep = 0;
            sortSteps.clear();

            startBtn.setDisable(false);
            pauseBtn.setDisable(true);
            resetBtn.setDisable(false);
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);

            stepInfoLabel.setText("Step: 0/0");
            actionLabel.setText("");

            // Display input array
            displayInputArray(array);
            outputArrayBox.getChildren().clear();

            renderArray(new boolean[array.length], new boolean[array.length]);
            showAlert("Array built: " + array.length + " elements");

        } catch (NumberFormatException ex) {
            showAlert("Invalid input. Please enter integers only.");
        }
    }

    private void startSorting() {
        if (array == null || array.length == 0) {
            showAlert("Build an array first!");
            return;
        }

        if (currentStep == 0) {
            // Generate sort steps
            String algo = algoCombo.getValue();
            if ("Bubble Sort".equals(algo)) {
                generateBubbleSortSteps();
            } else if ("Selection Sort".equals(algo)) {
                generateSelectionSortSteps();
            } else if ("Insertion Sort".equals(algo)) {
                generateInsertionSortSteps();
            } else if ("Quick Sort".equals(algo)) {
                generateQuickSortSteps();
            } else if ("Merge Sort".equals(algo)) {
                generateMergeSortSteps();
            } else {
                showAlert("This algorithm is coming soon!");
                return;
            }
        }

        isSorting = true;
        isPaused = false;
        startBtn.setDisable(true);
        pauseBtn.setDisable(false);
        prevBtn.setDisable(true);
        nextBtn.setDisable(false);

        animateSortSteps();
    }

    private void pauseSorting() {
        isPaused = true;
        if (animationTimeline != null) {
            animationTimeline.pause();
        }
        pauseBtn.setDisable(true);
        startBtn.setDisable(false);
    }

    private void resetSort() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        isSorting = false;
        isPaused = false;
        currentStep = 0;
        array = originalArray.clone();
        sortSteps.clear();

        startBtn.setDisable(array == null || array.length == 0);
        pauseBtn.setDisable(true);
        resetBtn.setDisable(true);
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);

        stepInfoLabel.setText("Step: 0/0");
        actionLabel.setText("");
        outputArrayBox.getChildren().clear();

        renderArray(new boolean[array.length], new boolean[array.length]);
    }

    private void previousStep() {
        if (currentStep > 0) {
            currentStep--;
            updateVisualization();
        }
    }

    private void nextStep() {
        if (currentStep < sortSteps.size()) {
            updateVisualization();
            currentStep++;
        }
    }

    private void updateVisualization() {
        if (currentStep >= 0 && currentStep < sortSteps.size()) {
            SortStep step = sortSteps.get(currentStep);
            renderArray(step.comparing, step.sorted, step.array);

            // Update step info labels
            stepInfoLabel.setText(String.format("Step: %d/%d", currentStep + 1, sortSteps.size()));
            actionLabel.setText(step.description);
        }
    }

    private void renderArray(boolean[] comparing, boolean[] sorted, int[] currentArray) {
        vizArea.getChildren().clear();
        barVisuals.clear();

        if (currentArray == null || currentArray.length == 0) return;

        double maxVal = java.util.Arrays.stream(currentArray).max().orElse(1);
        double barWidth = Math.max(20, 800.0 / currentArray.length);

        for (int i = 0; i < currentArray.length; i++) {
            int val = currentArray[i];
            double height = (val / maxVal) * MAX_HEIGHT;

            VBox bar = createBar(val, (int) height, comparing[i], sorted[i], barWidth);
            vizArea.getChildren().add(bar);
            barVisuals.add(bar);
        }

        // Align all bars to bottom
        //vizArea.setStyle("-fx-alignment: BOTTOM_CENTER;");
        vizArea.setStyle("-fx-alignment:baseline-center");
    }

    private void renderArray(boolean[] comparing, boolean[] sorted) {
        if (array != null) {
            renderArray(comparing, sorted, array);
        }
    }

    private VBox createBar(int value, int height, boolean comparing, boolean sorted, double width) {
        // Bar rectangle with rounded corners
        Rectangle rect = new Rectangle(width - 6, height);
        rect.setArcWidth(8);
        rect.setArcHeight(8);

        if (sorted) {
            rect.setFill(Color.web("#4CAF50")); // Green - sorted
        } else if (comparing) {
            rect.setFill(Color.web("#FF9800")); // Orange - comparing
        } else {
            rect.setFill(Color.web("#2196F3")); // Blue - normal
        }

        rect.setStroke(Color.web("#333333"));
        rect.setStrokeWidth(2);

        // Add shadow effect
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setRadius(3);
        shadow.setOffsetY(2);
        shadow.setColor(Color.color(0, 0, 0, 0.3));
        rect.setEffect(shadow);

        // Number inside the bar (white text) - size proportional to bar height
        double fontSize = Math.max(10, Math.min(14, height / 8));
        Text barText = new Text(String.valueOf(value));
        barText.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, fontSize));
        barText.setFill(Color.WHITE);

        // Stack for bar with text centered
        StackPane barStack = new StackPane(rect, barText);
        barStack.setPrefHeight(height);
        barStack.setStyle("-fx-alignment: center;");

        VBox bar = new VBox(barStack);
        bar.setStyle("-fx-alignment: center; -fx-padding: 6;");
        bar.setPrefWidth(width);

        return bar;
    }

    private void generateBubbleSortSteps() {
        int[] temp = array.clone();
        sortSteps.clear();

        int n = temp.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                boolean[] comparing = new boolean[n];
                boolean[] sorted = new boolean[n];

                comparing[j] = true;
                comparing[j + 1] = true;

                // Mark already sorted elements
                for (int k = n - i; k < n; k++) {
                    sorted[k] = true;
                }

                String desc = String.format("Comparing arr[%d]=%d and arr[%d]=%d", j, temp[j], j + 1, temp[j + 1]);
                sortSteps.add(new SortStep(temp.clone(), comparing, sorted, desc));

                if (temp[j] > temp[j + 1]) {
                    // Swap
                    int swapTemp = temp[j];
                    temp[j] = temp[j + 1];
                    temp[j + 1] = swapTemp;

                    comparing[j] = false;
                    comparing[j + 1] = false;
                    String swapDesc = String.format("Swapped arr[%d] and arr[%d] ✓", j, j + 1);
                    sortSteps.add(new SortStep(temp.clone(), comparing, sorted, swapDesc));
                }
            }
        }

        // Final sorted state
        boolean[] finalSorted = new boolean[n];
        for (int i = 0; i < n; i++) {
            finalSorted[i] = true;
        }
        sortSteps.add(new SortStep(temp.clone(), new boolean[n], finalSorted, "Sorting Complete! ✓"));

        array = temp;
    }

    private void generateSelectionSortSteps() {
        int[] temp = array.clone();
        sortSteps.clear();
        int n = temp.length;

        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;

            for (int j = i + 1; j < n; j++) {
                boolean[] comparing = new boolean[n];
                boolean[] sorted = new boolean[n];

                comparing[minIdx] = true;
                comparing[j] = true;

                for (int k = 0; k < i; k++) {
                    sorted[k] = true;
                }

                String desc = String.format("Comparing arr[%d]=%d and arr[%d]=%d, min is %d",
                        minIdx, temp[minIdx], j, temp[j], minIdx);
                sortSteps.add(new SortStep(temp.clone(), comparing, sorted, desc));

                if (temp[j] < temp[minIdx]) {
                    minIdx = j;
                }
            }

            // Swap
            int swapTemp = temp[i];
            temp[i] = temp[minIdx];
            temp[minIdx] = swapTemp;

            boolean[] comparing = new boolean[n];
            boolean[] sorted = new boolean[n];
            comparing[i] = true;
            comparing[minIdx] = true;
            for (int k = 0; k <= i; k++) {
                sorted[k] = true;
            }
            String swapDesc = String.format("Found minimum at position %d, swapped with position %d ✓", minIdx, i);
            sortSteps.add(new SortStep(temp.clone(), comparing, sorted, swapDesc));
        }

        // Final sorted state
        boolean[] finalSorted = new boolean[n];
        for (int i = 0; i < n; i++) {
            finalSorted[i] = true;
        }
        sortSteps.add(new SortStep(temp.clone(), new boolean[n], finalSorted, "Sorting Complete! ✓"));

        array = temp;
    }

    private void generateInsertionSortSteps() {
        int[] temp = array.clone();
        sortSteps.clear();
        int n = temp.length;

        for (int i = 1; i < n; i++) {
            int key = temp[i];
            int j = i - 1;

            while (j >= 0 && temp[j] > key) {
                boolean[] comparing = new boolean[n];
                boolean[] sorted = new boolean[n];

                comparing[j] = true;
                comparing[j + 1] = true;

                for (int k = 0; k < i; k++) {
                    sorted[k] = true;
                }

                String desc = String.format("Comparing arr[%d]=%d > key=%d, shifting left", j, temp[j], key);
                sortSteps.add(new SortStep(temp.clone(), comparing, sorted, desc));

                temp[j + 1] = temp[j];
                j--;

                comparing = new boolean[n];
                sorted = new boolean[n];
                for (int k = 0; k <= i; k++) {
                    sorted[k] = true;
                }
                sortSteps.add(new SortStep(temp.clone(), comparing, sorted, "Shifted element left ↓"));
            }

            temp[j + 1] = key;

            boolean[] finalComparing = new boolean[n];
            boolean[] finalSorted = new boolean[n];
            for (int k = 0; k <= i; k++) {
                finalSorted[k] = true;
            }
            String desc = String.format("Inserted key=%d at position %d ✓", key, j + 1);
            sortSteps.add(new SortStep(temp.clone(), finalComparing, finalSorted, desc));
        }

        // Final sorted state
        boolean[] finalSorted = new boolean[n];
        for (int i = 0; i < n; i++) {
            finalSorted[i] = true;
        }
        sortSteps.add(new SortStep(temp.clone(), new boolean[n], finalSorted, "Sorting Complete! ✓"));

        array = temp;
    }

    private void generateQuickSortSteps() {
        int[] temp = array.clone();
        sortSteps.clear();
        int n = temp.length;

        quickSortHelper(temp, 0, n - 1);
        array = temp;
    }

    private void quickSortHelper(int[] arr, int low, int high) {
        if (low < high) {
            int pi = quickSortPartition(arr, low, high);
            quickSortHelper(arr, low, pi - 1);
            quickSortHelper(arr, pi + 1, high);
        } else if (low == high) {
            boolean[] sorted = new boolean[arr.length];
            sorted[low] = true;
            sortSteps.add(new SortStep(arr.clone(), new boolean[arr.length], sorted, "Single element is sorted ✓"));
        }
    }

    private int quickSortPartition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;

        // Show pivot selection
        boolean[] comparing = new boolean[arr.length];
        comparing[high] = true;
        sortSteps.add(new SortStep(arr.clone(), comparing, new boolean[arr.length],
                String.format("Selected pivot: %d at position %d", pivot, high)));

        for (int j = low; j < high; j++) {
            comparing = new boolean[arr.length];
            comparing[j] = true;
            comparing[high] = true;
            String desc = String.format("Comparing arr[%d]=%d with pivot %d", j, arr[j], pivot);
            sortSteps.add(new SortStep(arr.clone(), comparing, new boolean[arr.length], desc));

            if (arr[j] < pivot) {
                i++;
                // Swap
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;

                comparing = new boolean[arr.length];
                comparing[i] = true;
                comparing[j] = true;
                sortSteps.add(new SortStep(arr.clone(), comparing, new boolean[arr.length],
                        String.format("Swapped arr[%d] and arr[%d]", i, j)));
            }
        }

        // Final swap with pivot
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        boolean[] sorted = new boolean[arr.length];
        sorted[i + 1] = true;
        sortSteps.add(new SortStep(arr.clone(), new boolean[arr.length], sorted,
                String.format("Pivot %d placed at correct position %d ✓", pivot, i + 1)));

        return i + 1;
    }

    private void generateMergeSortSteps() {
        int[] temp = array.clone();
        sortSteps.clear();
        int n = temp.length;

        int[] result = new int[n];
        mergeSortHelper(temp, 0, n - 1, result);
        array = result;
    }

    private void mergeSortHelper(int[] arr, int left, int right, int[] result) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            // Show divide phase
            boolean[] dividing = new boolean[arr.length];
            for (int i = left; i <= right; i++) {
                dividing[i] = true;
            }
            sortSteps.add(new SortStep(arr.clone(), dividing, new boolean[arr.length],
                    String.format("Dividing: positions %d to %d", left, right)));

            mergeSortHelper(arr, left, mid, result);
            mergeSortHelper(arr, mid + 1, right, result);

            int[] temp = new int[right - left + 1];
            int i = left, j = mid + 1, k = 0;

            while (i <= mid && j <= right) {
                boolean[] comparing = new boolean[arr.length];
                comparing[i] = true;
                comparing[j] = true;
                sortSteps.add(new SortStep(arr.clone(), comparing, new boolean[arr.length],
                        String.format("Comparing arr[%d]=%d and arr[%d]=%d", i, arr[i], j, arr[j])));

                if (arr[i] <= arr[j]) {
                    temp[k++] = arr[i++];
                } else {
                    temp[k++] = arr[j++];
                }
            }

            while (i <= mid) temp[k++] = arr[i++];
            while (j <= right) temp[k++] = arr[j++];

            for (i = left, k = 0; i <= right; i++, k++) {
                arr[i] = temp[k];
            }

            boolean[] sorted = new boolean[arr.length];
            for (int idx = left; idx <= right; idx++) {
                sorted[idx] = true;
            }
            sortSteps.add(new SortStep(arr.clone(), new boolean[arr.length], sorted,
                    String.format("Merged positions %d to %d ✓", left, right)));
        } else if (left == right) {
            boolean[] sorted = new boolean[arr.length];
            sorted[left] = true;
            sortSteps.add(new SortStep(arr.clone(), new boolean[arr.length], sorted,
                    "Single element is sorted ✓"));
        }
    }

    private void animateSortSteps() {
        animationTimeline = new Timeline();
        animationTimeline.setCycleCount(1);

        double speedMultiplier = speedSlider.getValue();
        double delayPerStep = 800.0 / speedMultiplier; // Delay between each step in milliseconds

        for (int i = currentStep; i < sortSteps.size(); i++) {
            final int step = i;
            double cumulativeDelay = (i - currentStep) * delayPerStep; // Each step is delayed sequentially

            KeyFrame kf = new KeyFrame(
                    Duration.millis(cumulativeDelay),
                    event -> {
                        if (!isPaused) {
                            currentStep = step;

                            // Calculate which bars changed position
                            if (step > 0 && step < sortSteps.size()) {
                                SortStep prevStep = sortSteps.get(step - 1);
                                SortStep currStep = sortSteps.get(step);
                                animateBarChanges(prevStep.array, currStep.array, currStep.comparing, currStep.sorted, speedMultiplier);
                            } else {
                                updateVisualization();
                            }

                            if (step == sortSteps.size() - 1) {
                                isSorting = false;
                                startBtn.setDisable(false);
                                pauseBtn.setDisable(true);
                                startBtn.setStyle("-fx-text-fill: green;");

                                // Display final sorted output
                                if (sortSteps.size() > 0) {
                                    displayOutputArray(sortSteps.get(sortSteps.size() - 1).array);
                                }

                                showAlert("Sorting Complete! ✓");
                            }
                        }
                    }
            );

            animationTimeline.getKeyFrames().add(kf);
        }

        animationTimeline.play();
    }

    private void animateBarChanges(int[] prevArray, int[] currArray, boolean[] comparing, boolean[] sorted, double speed) {
        // First, render the current state to show colors
        renderArray(comparing, sorted, currArray);

        // Update step info
        if (currentStep >= 0 && currentStep < sortSteps.size()) {
            SortStep step = sortSteps.get(currentStep);
            stepInfoLabel.setText(String.format("Step: %d/%d", currentStep + 1, sortSteps.size()));
            actionLabel.setText(step.description);
        }
    }

    private void displayInputArray(int[] inputArr) {
        inputArrayBox.getChildren().clear();
        for (int i = 0; i < inputArr.length; i++) {
            Label numLabel = new Label(String.valueOf(inputArr[i]));
            numLabel.setStyle("-fx-padding: 8 12; -fx-background-color: #BBDEFB; -fx-border-color: #1976D2; -fx-border-radius: 4; -fx-border-width: 1; -fx-font-weight: bold; -fx-font-size: 12;");
            inputArrayBox.getChildren().add(numLabel);
        }
    }

    private void displayOutputArray(int[] outputArr) {
        outputArrayBox.getChildren().clear();
        for (int i = 0; i < outputArr.length; i++) {
            Label numLabel = new Label(String.valueOf(outputArr[i]));
            numLabel.setStyle("-fx-padding: 8 12; -fx-background-color: #C8E6C9; -fx-border-color: #388E3C; -fx-border-radius: 4; -fx-border-width: 1; -fx-font-weight: bold; -fx-font-size: 12;");
            outputArrayBox.getChildren().add(numLabel);
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void showAlert(String msg) {
//        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
//        a.showAndWait();
//    }
protected void showAlert(String msg) {
    if (statusLabel != null) {
        statusLabel.setText(msg);
    }
}
    // Inner class to store each step of the sorting process
    private static class SortStep {
        int[] array;
        boolean[] comparing;
        boolean[] sorted;
        String description;

        SortStep(int[] array, boolean[] comparing, boolean[] sorted, String description) {
            this.array = array;
            this.comparing = comparing;
            this.sorted = sorted;
            this.description = description;
        }

        SortStep(int[] array, boolean[] comparing, boolean[] sorted) {
            this(array, comparing, sorted, "");
        }
    }
}