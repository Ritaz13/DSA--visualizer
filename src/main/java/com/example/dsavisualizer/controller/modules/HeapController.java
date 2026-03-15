package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HeapController extends ModuleController {

    @FXML private TextField inputField;
    @FXML private Button insertBtn, deleteBtn, extractBtn, buildBtn, randomBtn, clearBtn;
    @FXML private Button heapsortBtn, startBtn, pauseBtn, stopBtn, nextBtn, prevBtn;
    @FXML private ChoiceBox<String> heapTypeChoice;
    @FXML private Slider speedSlider;
    @FXML private StackPane vizArea;
    @FXML private TextArea codeArea, storyArea;
    @FXML private Button showCodeBtn, copyCodeBtn;
    @FXML private Label statusLabel;

    private Canvas canvas;
    private GraphicsContext gc;
    private List<Integer> heap = new ArrayList<>();
    private List<Integer> sortedArray = new ArrayList<>();
    private boolean isMaxHeap = true;
    private String lastMessage = "";

    private List<HeapStep> currentSteps = new ArrayList<>();
    private List<HeapsortStep> currentHeapsortSteps = new ArrayList<>();
    private int currentStepIndex = -1;
    private Timeline currentTimeline;
    private boolean isOperationRunning = false;
    private boolean isCurrentHeapsort = false;

    // ---------------- Step Classes ----------------
    private static class HeapStep {
        int idx1, idx2;
        boolean swapped;
        List<Integer> snapshot;
        HeapStep(int i, int j, boolean swapped, List<Integer> heap) {
            this.idx1 = i; this.idx2 = j; this.swapped = swapped;
            this.snapshot = new ArrayList<>(heap);
        }
    }

    private static class HeapsortStep {
        List<Integer> heapSnapshot, sortedSnapshot;
        int highlightIdx; boolean isExtracted;
        HeapsortStep(List<Integer> heap, List<Integer> sorted, int highlightIdx, boolean isExtracted) {
            this.heapSnapshot = new ArrayList<>(heap);
            this.sortedSnapshot = new ArrayList<>(sorted);
            this.highlightIdx = highlightIdx; this.isExtracted = isExtracted;
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Heap");
        storyArea.setText("Heap is a complete binary tree with heap property.\n" +
                "Max Heap: Parent >= Children\nMin Heap: Parent <= Children\n" +
                "Used in priority queues and sorting.");

        canvas = new Canvas(700, 400);
        gc = canvas.getGraphicsContext2D();
        vizArea.getChildren().add(canvas);

        heapTypeChoice.setValue("Max Heap");
        heapTypeChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            isMaxHeap = "Max Heap".equals(newVal);
            redraw();
        });

        if (speedSlider != null) { speedSlider.setMin(0.5); speedSlider.setMax(3); speedSlider.setValue(1); }

        insertBtn.setOnAction(e -> insert());
        deleteBtn.setOnAction(e -> deleteRoot());
        extractBtn.setOnAction(e -> extractRoot());
        buildBtn.setOnAction(e -> buildHeap());
        randomBtn.setOnAction(e -> randomHeap());
        clearBtn.setOnAction(e -> clearHeap());
        heapsortBtn.setOnAction(e -> heapsort());

        if (startBtn != null) startBtn.setOnAction(e -> playCurrentOperation());
        if (pauseBtn != null) pauseBtn.setOnAction(e -> pauseCurrentOperation());
        if (stopBtn != null) stopBtn.setOnAction(e -> stopCurrentOperation());
        if (nextBtn != null) nextBtn.setOnAction(e -> nextCurrentStep());
        if (prevBtn != null) prevBtn.setOnAction(e -> prevCurrentStep());

        showCodeBtn.setOnAction(e -> toggleCodeArea());
        copyCodeBtn.setOnAction(e -> copyCode());

        loadCodeFile();
        redraw();
    }

    // ---------------- Heap Operations ----------------
    private void insert() {
        try {
            int val = Integer.parseInt(inputField.getText().trim());
            heap.add(val);
            currentSteps.clear(); currentStepIndex = 0; isCurrentHeapsort = false;
            currentSteps.add(new HeapStep(-1, -1, false, heap));
            currentSteps.addAll(getHeapifyUpSteps(heap.size() - 1));
            heapifyUp(heap.size() - 1); // enforce property
            playCurrentOperation(); inputField.clear();
        } catch (NumberFormatException ex) { setMessage("Enter a valid number"); }
    }

    private void deleteRoot() {
        if (heap.isEmpty()) { setMessage("Heap is empty!"); return; }
        heap.set(0, heap.get(heap.size() - 1)); heap.remove(heap.size() - 1);
        currentSteps.clear(); currentStepIndex = 0; isCurrentHeapsort = false;
        currentSteps.add(new HeapStep(-1, -1, false, heap));
        if (!heap.isEmpty()) { currentSteps.addAll(getHeapifyDownSteps(0)); heapifyDown(0); }
        playCurrentOperation();
    }

    private void extractRoot() {
        if (heap.isEmpty()) { setMessage("Heap is empty!"); return; }
        setMessage("Root: " + heap.get(0));
    }

    private void clearHeap() { heap.clear(); inputField.clear(); setMessage("Heap cleared"); redraw(); }

    private void buildHeap() {
        try {
            String[] parts = inputField.getText().split(",");
            heap.clear(); for (String part : parts) heap.add(Integer.parseInt(part.trim()));
            currentSteps.clear(); currentStepIndex = 0; isCurrentHeapsort = false;
            currentSteps.add(new HeapStep(-1, -1, false, heap));
            for (int i = heap.size()/2 - 1; i >= 0; i--) {
                currentSteps.addAll(getHeapifyDownSteps(i)); heapifyDown(i);
            }
            playCurrentOperation(); inputField.clear();
        } catch (NumberFormatException ex) { setMessage("Enter valid numbers separated by commas"); }
    }

    private void randomHeap() {
        heap.clear(); Random rand = new Random();
        int size = 7 + rand.nextInt(6);
        for (int i = 0; i < size; i++) heap.add(rand.nextInt(100) + 1);
        currentSteps.clear(); currentStepIndex = 0; isCurrentHeapsort = false;
        currentSteps.add(new HeapStep(-1, -1, false, heap));
        for (int i = heap.size()/2 - 1; i >= 0; i--) {
            currentSteps.addAll(getHeapifyDownSteps(i)); heapifyDown(i);
        }
        playCurrentOperation();
    }

    // ---------------- Heapify ----------------
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIdx = (index - 1)/2;
            if (isMaxHeap ? heap.get(index) > heap.get(parentIdx) : heap.get(index) < heap.get(parentIdx)) {
                swap(heap, index, parentIdx); index = parentIdx;
            } else break;
        }
    }

    private List<HeapStep> getHeapifyUpSteps(int index) {
        List<HeapStep> steps = new ArrayList<>();
        while (index > 0) {
            int parentIdx = (index - 1)/2;
            boolean swapCandidate = (isMaxHeap ? heap.get(index) > heap.get(parentIdx) : heap.get(index) < heap.get(parentIdx));
            steps.add(new HeapStep(index, parentIdx, swapCandidate, new ArrayList<>(heap)));
            if (swapCandidate) { 
                swap(heap, index, parentIdx); 
                steps.add(new HeapStep(index, parentIdx, true, new ArrayList<>(heap))); 
                index = parentIdx; 
            } else {
                break;
            }
        }
        return steps;
    }
    private List<HeapStep> getHeapifyDownSteps(int index) {
        int size = heap.size();
        List<HeapStep> steps = new ArrayList<>();

        while (true) {
            int target = index;
            int left = 2 * index + 1;
            int right = 2 * index + 2;

            // Check left child
            if (left < size) {
                boolean swapCandidate = (isMaxHeap ? heap.get(left) > heap.get(target)
                        : heap.get(left) < heap.get(target));
                steps.add(new HeapStep(index, left, swapCandidate, new ArrayList<>(heap)));
                if (swapCandidate) target = left;
            }

            // Check right child against target (which might be left or index)
            if (right < size) {
                boolean swapCandidate = (isMaxHeap ? heap.get(right) > heap.get(target)
                        : heap.get(right) < heap.get(target));
                steps.add(new HeapStep(index, right, swapCandidate, new ArrayList<>(heap)));
                if (swapCandidate) target = right;
            }

            // Perform the swap if needed
            if (target != index) {
                swap(heap, index, target);
                steps.add(new HeapStep(index, target, true, new ArrayList<>(heap)));
                index = target; // continue down
            } else break;
        }
        // After the while loop ends, add final state
        steps.add(new HeapStep(-1, -1, false, new ArrayList<>(heap)));

        return steps;
    }

    // ---------------- Animation ----------------
    private void animateHeapSteps(List<HeapStep> steps) {
        Timeline timeline = new Timeline();
        int delay = 1000; // ms per step

        for (int i = 0; i < steps.size(); i++) {
            HeapStep step = steps.get(i);
            KeyFrame frame = new KeyFrame(Duration.millis(i * delay), e -> {
                redrawSnapshot(step.snapshot, step);
            });
            timeline.getKeyFrames().add(frame);
        }

        timeline.setCycleCount(1);
        //timeline.setOnFinished(e -> redraw());
        timeline.setOnFinished(e -> {
            if (!steps.isEmpty()) {
                HeapStep last = steps.get(steps.size() - 1);
                redrawSnapshot(last.snapshot, null);
            }
        });


        timeline.play();
    }
    // --- Step Navigation ---
    private void nextCurrentStep() {
        int totalSteps = isCurrentHeapsort ? currentHeapsortSteps.size() : currentSteps.size();
        if (totalSteps == 0) { setMessage("No operation prepared!"); return; }
        if (currentStepIndex < totalSteps - 1) {
            currentStepIndex++; redrawCurrentStep();
        } else setMessage("Operation complete!");
    }

    private void prevCurrentStep() {
        if (currentStepIndex > 0) { currentStepIndex--; redrawCurrentStep(); }
    }


    // --- Heapsort Visualization ---


    private void drawHeapArrayBoxesHeapsort(List<Integer> snapshot, int highlightIdx) {
        double startX = (canvas.getWidth() - snapshot.size() * 50) / 2;
        double y = 50, boxSize = 40;
        for (int i = 0; i < snapshot.size(); i++) {
            double x = startX + i * 50;
            Color fillColor = (i == highlightIdx) ? Color.YELLOW : Color.WHITE;
            gc.setFill(fillColor); gc.fillRect(x, y, boxSize, boxSize);
            gc.setStroke(Color.BLACK); gc.strokeRect(x, y, boxSize, boxSize);
            gc.setFill(Color.BLACK); gc.setFont(new Font(14));
            gc.fillText(String.valueOf(snapshot.get(i)), x + 12, y + 25);
        }
    }

    private void drawHeapTreeHeapsort(List<Integer> snapshot, int highlightIdx) {
        double centerX = canvas.getWidth() / 2, startY = 130, verticalGap = 60;
        drawNodeHeapsort(snapshot, 0, centerX, startY, canvas.getWidth() / 4, verticalGap, highlightIdx);
    }

    private void drawNodeHeapsort(List<Integer> snapshot, int idx, double x, double y,
                                  double horizontalGap, double verticalGap, int highlightIdx) {
        if (idx >= snapshot.size()) return;
        int leftChild = 2 * idx + 1, rightChild = 2 * idx + 2;
        double nextY = y + verticalGap;

        if (leftChild < snapshot.size()) {
            double leftX = x - horizontalGap;
            gc.setStroke(Color.GRAY); gc.strokeLine(x, y, leftX, nextY);
            drawNodeHeapsort(snapshot, leftChild, leftX, nextY, horizontalGap / 2, verticalGap, highlightIdx);
        }
        if (rightChild < snapshot.size()) {
            double rightX = x + horizontalGap;
            gc.setStroke(Color.GRAY); gc.strokeLine(x, y, rightX, nextY);
            drawNodeHeapsort(snapshot, rightChild, rightX, nextY, horizontalGap / 2, verticalGap, highlightIdx);
        }

        double radius = 20;
        Color fillColor = (idx == highlightIdx) ? Color.YELLOW : Color.web("#FF6B6B");
        gc.setFill(fillColor); gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        gc.setStroke(Color.BLACK); gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);
        gc.setFill(Color.BLACK); gc.setFont(new Font(14));
        gc.fillText(String.valueOf(snapshot.get(idx)), x - 6, y + 5);
    }

    private void drawSortedArrayBoxes(List<Integer> sorted) {
        double boxSize = 40, spacing = 50;
        double startX = (canvas.getWidth() - sorted.size() * spacing) / 2;
        double y = 340;
        for (int i = 0; i < sorted.size(); i++) {
            double x = startX + i * spacing;
            gc.setFill(Color.LIGHTGREEN); gc.fillRect(x, y, boxSize, boxSize);
            gc.setStroke(Color.BLACK); gc.strokeRect(x, y, boxSize, boxSize);
            gc.setFill(Color.BLACK); gc.setFont(new Font(14));
            gc.fillText(String.valueOf(sorted.get(i)), x + 12, y + 25);
        }
    }

    // --- Utility ---
    private void toggleCodeArea() {
        if (codeArea != null) codeArea.setVisible(!codeArea.isVisible());
    }

    private void copyCode() {
        String code = codeArea.getText();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
        cc.putString(code); clipboard.setContent(cc);
        setMessage("Code copied to clipboard!");
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/heap.txt")) {
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder(); String line;
                    while ((line = br.readLine()) != null) sb.append(line).append('\n');
                    codeArea.setText(sb.toString()); codeArea.setVisible(false);
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void setMessage(String msg) {
        lastMessage = msg;
        if (statusLabel != null) statusLabel.setText(msg);
    }


    private void redrawSnapshot(List<Integer> snapshot, HeapStep step) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawHeapArrayBoxes(snapshot);
        drawHeapTreeSnapshot(snapshot, step);
        //updateHeapArray(snapshot);

        if (step != null) {
            highlightArrayBox(step.idx1, step.swapped ? Color.ORANGE : Color.YELLOW, snapshot);
            highlightArrayBox(step.idx2, step.swapped ? Color.ORANGE : Color.YELLOW, snapshot);
        }
    }

    private void redrawSnapshot(List<Integer> snapshot) {
        redrawSnapshot(snapshot, null);
    }

    // ---------------- Visualization ----------------
    private void redraw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawHeapArrayBoxes(heap);
        //updateHeapArray(heap);

        if (heap.isEmpty()) {
            gc.setFill(Color.GRAY);
            gc.setFont(new Font(16));
            gc.fillText("Heap is empty. Add elements to visualize.", 200, 200);
            return;
        }
        drawHeapTreeSnapshot(heap, null);
    }

    private void drawHeapArrayBoxes(List<Integer> snapshot) {
        double startX = (canvas.getWidth() - snapshot.size() * 50) / 2;
        double y = 20;
        double boxSize = 40;

        for (int i = 0; i < snapshot.size(); i++) {
            double x = startX + i * 50;
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, boxSize, boxSize);

            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, boxSize, boxSize);

            gc.setFill(Color.BLACK);
            gc.setFont(new Font(14));
            gc.fillText(String.valueOf(snapshot.get(i)), x + 12, y + 25);
        }
    }

    private void highlightArrayBox(int idx, Color color, List<Integer> snapshot) {
        if (idx < 0 || idx >= snapshot.size()) return;
        double startX = (canvas.getWidth() - snapshot.size() * 50) / 2;
        double y = 20;
        double boxSize = 40;
        double x = startX + idx * 50;

        gc.setFill(color);
        gc.fillRect(x, y, boxSize, boxSize);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, boxSize, boxSize);

        gc.setFill(Color.BLACK);
        gc.setFont(new Font(14));
        gc.fillText(String.valueOf(snapshot.get(idx)), x + 12, y + 25);
    }

    private void drawHeapTreeSnapshot(List<Integer> snapshot, HeapStep step) {
        double centerX = canvas.getWidth() / 2;
        double startY = 100;
        double verticalGap = 60;
        drawNodeSnapshot(snapshot, 0, centerX, startY, canvas.getWidth() / 4, verticalGap, step);
    }

    private void drawNodeSnapshot(List<Integer> snapshot, int idx, double x, double y,
                                  double horizontalGap, double verticalGap, HeapStep step) {
        if (idx >= snapshot.size()) return;

        int leftChild = 2 * idx + 1;
        int rightChild = 2 * idx + 2;
        double nextY = y + verticalGap;

        if (leftChild < snapshot.size()) {
            double leftX = x - horizontalGap;
            gc.setStroke(Color.GRAY);
            gc.strokeLine(x, y, leftX, nextY);
            drawNodeSnapshot(snapshot, leftChild, leftX, nextY, horizontalGap / 2, verticalGap, step);
        }

        if (rightChild < snapshot.size()) {
            double rightX = x + horizontalGap;
            gc.setStroke(Color.GRAY);
            gc.strokeLine(x, y, rightX, nextY);
            drawNodeSnapshot(snapshot, rightChild, rightX, nextY, horizontalGap / 2, verticalGap, step);
        }

        double radius = 20;
        Color fillColor = (step != null && (idx == step.idx1 || idx == step.idx2))
                ? (step.swapped ? Color.ORANGE : Color.YELLOW)
                : (isMaxHeap ? Color.web("#FF6B6B") : Color.web("#4ECDC4"));

        gc.setFill(fillColor);
        gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);

        gc.setFill(Color.BLACK);
        gc.setFont(new Font(14));
        gc.fillText(String.valueOf(snapshot.get(idx)), x - 6, y + 5);
    }

//    private void updateHeapArray(List<Integer> snapshot) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Heap Array (").append(isMaxHeap ? "Max" : "Min").append(" Heap):\n\n");
//        sb.append("[ ");
//        for (int i = 0; i < snapshot.size(); i++) {
//            sb.append(snapshot.get(i));
//            if (i < snapshot.size() - 1) sb.append(", ");
//        }
//        sb.append(" ]");
//        if (statusLabel != null) statusLabel.setText(sb.toString());
//    }
    // --- Heapsort core ---
    private void heapsort() {
        if (heap.isEmpty()) { setMessage("Heap is empty!"); return; }
        sortedArray.clear(); currentHeapsortSteps.clear(); currentSteps.clear();
        currentStepIndex = 0; isCurrentHeapsort = true;

        List<Integer> tempHeap = new ArrayList<>(heap);
        currentHeapsortSteps.add(new HeapsortStep(tempHeap, sortedArray, -1, false));

        while (!tempHeap.isEmpty()) {
            int root = tempHeap.get(0);
            sortedArray.add(root);

            tempHeap.set(0, tempHeap.get(tempHeap.size() - 1));
            tempHeap.remove(tempHeap.size() - 1);

            currentHeapsortSteps.add(new HeapsortStep(new ArrayList<>(tempHeap),
                    new ArrayList<>(sortedArray), 0, true));

            if (!tempHeap.isEmpty()) {
                currentHeapsortSteps.addAll(getHeapifySortSteps(tempHeap, sortedArray));
            }
        }
        playCurrentOperation();
    }

    private List<HeapsortStep> getHeapifySortSteps(List<Integer> heap, List<Integer> sorted) {
        List<HeapsortStep> steps = new ArrayList<>();
        int index = 0, size = heap.size();

        while (true) {
            int target = index;
            int left = 2 * index + 1, right = 2 * index + 2;

            if (left < size) {
                steps.add(new HeapsortStep(new ArrayList<>(heap), new ArrayList<>(sorted), left, false));
                if (heap.get(left) > heap.get(target)) target = left;
            }
            if (right < size) {
                steps.add(new HeapsortStep(new ArrayList<>(heap), new ArrayList<>(sorted), right, false));
                if (heap.get(right) > heap.get(target)) target = right;
            }

            if (target != index) {
                swap(heap, index, target);
                steps.add(new HeapsortStep(new ArrayList<>(heap), new ArrayList<>(sorted), target, true));
                index = target;
            } else break;


        }
        steps.add(new HeapsortStep(new ArrayList<>(heap), new ArrayList<>(sorted), -1, false));

        return steps;
    }

    // --- Timeline control ---
    private void playCurrentOperation() {
        if ((isCurrentHeapsort && currentHeapsortSteps.isEmpty()) || (!isCurrentHeapsort && currentSteps.isEmpty())) {
            setMessage("No operation prepared!"); return;
        }
        isOperationRunning = true;
        if (startBtn != null) startBtn.setDisable(true);

        double speed = speedSlider != null ? speedSlider.getValue() : 1.0;
        int delayMs = (int)(1000 / speed);

        currentTimeline = new Timeline();
        int totalSteps = isCurrentHeapsort ? currentHeapsortSteps.size() : currentSteps.size();
        for (int i = currentStepIndex; i < totalSteps; i++) {
            int stepIdx = i;
            KeyFrame frame = new KeyFrame(Duration.millis((i - currentStepIndex) * delayMs), e -> {
                currentStepIndex = stepIdx; redrawCurrentStep();
            });
            currentTimeline.getKeyFrames().add(frame);
        }
        currentTimeline.setOnFinished(e -> stopCurrentOperation());
        currentTimeline.play();
//        currentTimeline.setOnFinished(e -> {
//            if (isCurrentHeapsort && !currentHeapsortSteps.isEmpty()) {
//                HeapsortStep last = currentHeapsortSteps.get(currentHeapsortSteps.size() - 1);
//                redrawHeapsort(currentHeapsortSteps.size() - 1);
//            } else if (!isCurrentHeapsort && !currentSteps.isEmpty()) {
//                HeapStep last = currentSteps.get(currentSteps.size() - 1);
//                redrawSnapshot(last.snapshot, null);
//            }
//            isOperationRunning = false;
//            if (startBtn != null) startBtn.setDisable(false);
//        });

    }

    private void pauseCurrentOperation() {
        if (currentTimeline != null) {
            currentTimeline.pause(); isOperationRunning = false;
            if (startBtn != null) startBtn.setDisable(false);
            setMessage("Operation paused");
        }
    }

    private void stopCurrentOperation() {
        if (currentTimeline != null) currentTimeline.stop();
        isOperationRunning = false;
        //currentStepIndex = 0;
        if (startBtn != null) startBtn.setDisable(false);
        setMessage("Operation stopped");
        //redrawCurrentStep();
    }

    // --- Step navigation ---


    private void redrawCurrentStep() {
        if (isCurrentHeapsort) redrawHeapsort(currentStepIndex);
        else {
            if (currentStepIndex >= 0 && currentStepIndex < currentSteps.size()) {
                HeapStep step = currentSteps.get(currentStepIndex);
                redrawSnapshot(step.snapshot, step);
            } else redraw();
        }
    }

    // --- Heapsort visualization ---
    private void redrawHeapsort(int stepIdx) {
        if (stepIdx < 0 || stepIdx >= currentHeapsortSteps.size()) return;
        HeapsortStep step = currentHeapsortSteps.get(stepIdx);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.BLACK); gc.setFont(new Font("Arial", 20));
        gc.fillText("Current Heap:", 2, 60);
        drawHeapArrayBoxesHeapsort(step.heapSnapshot, step.highlightIdx);
        drawHeapTreeHeapsort(step.heapSnapshot, step.highlightIdx);

        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 20));
        gc.fillText("Sorted Array:", 2, 370);
        drawSortedArrayBoxes(step.sortedSnapshot);
    }
    private void heapifyDown(int index) {
        int size = heap.size();
        while (true) {
            int target = index;
            int left = 2 * index + 1;
            int right = 2 * index + 2;

            if (left < size && (isMaxHeap ? heap.get(left) > heap.get(target)
                    : heap.get(left) < heap.get(target))) {
                target = left;
            }
            if (right < size && (isMaxHeap ? heap.get(right) > heap.get(target)
                    : heap.get(right) < heap.get(target))) {
                target = right;
            }

            if (target != index) {
                // enforce property by swapping
                int temp = heap.get(index);
                heap.set(index, heap.get(target));
                heap.set(target, temp);
                index = target; // continue down
            } else break;
        }
    }

    // Swap two elements in the heap list
    private void swap( List<Integer> heap,int i, int j) {
        int temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }










}
