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
    @FXML private CheckBox maxHeapCheck;
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
    
    // Heapsort animation state
    private List<HeapsortStep> heapsortSteps = new ArrayList<>();
    private int currentHeapsortStep = -1;
    private boolean isHeapsortRunning = false;
    private Timeline heapsortTimeline;

    // Step structure
    private static class HeapStep {
        int idx1, idx2;
        boolean swapped;
        List<Integer> snapshot;

        HeapStep(int i, int j, boolean swapped, List<Integer> heap) {
            this.idx1 = i;
            this.idx2 = j;
            this.swapped = swapped;
            this.snapshot = new ArrayList<>(heap);
        }
    }
    
    // Heapsort step structure
    private static class HeapsortStep {
        List<Integer> heapSnapshot;
        List<Integer> sortedSnapshot;
        int highlightIdx; // heap index being highlighted
        boolean isExtracted;

        HeapsortStep(List<Integer> heap, List<Integer> sorted, int highlightIdx, boolean isExtracted) {
            this.heapSnapshot = new ArrayList<>(heap);
            this.sortedSnapshot = new ArrayList<>(sorted);
            this.highlightIdx = highlightIdx;
            this.isExtracted = isExtracted;
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

        maxHeapCheck.setSelected(isMaxHeap);
        maxHeapCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isMaxHeap = newVal;
            redraw();
        });

        // Speed slider setup
        if (speedSlider != null) {
            speedSlider.setMin(0.5);
            speedSlider.setMax(3);
            speedSlider.setValue(1);
        }

        insertBtn.setOnAction(e -> insertElement());
        deleteBtn.setOnAction(e -> deleteRootAnimated());
        extractBtn.setOnAction(e -> extractRoot());
        buildBtn.setOnAction(e -> buildHeapFromInput());
        randomBtn.setOnAction(e -> generateRandom());
        clearBtn.setOnAction(e -> clearHeap());
        
        heapsortBtn.setOnAction(e -> startHeapsort());
        if (startBtn != null) startBtn.setOnAction(e -> playHeapsort());
        if (pauseBtn != null) pauseBtn.setOnAction(e -> pauseHeapsort());
        if (stopBtn != null) stopBtn.setOnAction(e -> stopHeapsort());
        if (nextBtn != null) nextBtn.setOnAction(e -> nextHeapsortStep());
        if (prevBtn != null) prevBtn.setOnAction(e -> prevHeapsortStep());
        
        showCodeBtn.setOnAction(e -> toggleCodeArea());
        copyCodeBtn.setOnAction(e -> copyCode());

        loadCodeFile();
        redraw();
    }

    // ---------------- Heap Operations ----------------
    private void insertElement() {
        try {
            int val = Integer.parseInt(inputField.getText().trim());
            heap.add(val);
            heapifyUpAnimated(heap.size() - 1);
            inputField.clear();
            setMessage("Inserted " + val);
        } catch (NumberFormatException ex) {
            setMessage("Enter a valid number");
        }
    }

    private void deleteRootAnimated() {
        if (heap.isEmpty()) {
            setMessage("Heap is empty!");
            return;
        }
        int removed = heap.get(0);
        heap.set(0, heap.get(heap.size() - 1));
        heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) heapifyDownAnimated(0);
        setMessage("Removed root " + removed);
    }

    private void extractRoot() {
        if (heap.isEmpty()) {
            setMessage("Heap is empty!");
            return;
        }
        setMessage("Root: " + heap.get(0));
    }

    private void clearHeap() {
        heap.clear();
        inputField.clear();
        setMessage("Heap cleared");
        redraw();
    }

    private void buildHeapFromInput() {
        try {
            String input = inputField.getText().trim();
            if (input.isEmpty()) {
                setMessage("Enter numbers separated by commas");
                return;
            }
            String[] parts = input.split(",");
            heap.clear();
            for (String part : parts) {
                int val = Integer.parseInt(part.trim());
                heap.add(val);
            }
            buildHeapAnimated();
        } catch (NumberFormatException ex) {
            setMessage("Enter valid numbers separated by commas");
        }
    }

    private void buildHeapAnimated() {
        if (heap.isEmpty()) {
            setMessage("Heap is empty!");
            return;
        }
        List<HeapStep> allSteps = new ArrayList<>();
        // always show the original order first
        allSteps.add(new HeapStep(-1, -1, false, heap));
        for (int i = heap.size() / 2 - 1; i >= 0; i--) {
            allSteps.addAll(getHeapifyDownSteps(i));
        }
        animateHeapSteps(allSteps);
        setMessage("Built heap with animation");
    }

    private void generateRandom() {
        heap.clear();
        Random rand = new Random();
        int size = 7 + rand.nextInt(6);
        for (int i = 0; i < size; i++) {
            heap.add(rand.nextInt(100) + 1);
        }
        buildHeapAnimated();
        setMessage("Generated random heap with " + size + " elements");
    }

    // ============ HEAPSORT ============
    private void startHeapsort() {
        if (heap.isEmpty()) {
            setMessage("Heap is empty!");
            return;
        }
        
        sortedArray.clear();
        heapsortSteps.clear();
        currentHeapsortStep = 0;
        
        // Create a copy for heapsort
        List<Integer> tempHeap = new ArrayList<>(heap);
        
        // Record initial state
        heapsortSteps.add(new HeapsortStep(tempHeap, sortedArray, -1, false));
        
        // Extract root repeatedly
        while (!tempHeap.isEmpty()) {
            int root = tempHeap.get(0);
            sortedArray.add(root);
            
            tempHeap.set(0, tempHeap.get(tempHeap.size() - 1));
            tempHeap.remove(tempHeap.size() - 1);
            
            heapsortSteps.add(new HeapsortStep(tempHeap, sortedArray, 0, true)); // Extracted
            
            // Heapify down steps
            if (!tempHeap.isEmpty()) {
                List<HeapsortStep> heapifySteps = getHeapifySortSteps(tempHeap, sortedArray);
                heapsortSteps.addAll(heapifySteps);
            }
        }
        
        setMessage("Heapsort steps prepared. Click Play or Next to start.");
        redrawHeapsort(currentHeapsortStep);
    }
    
    private List<HeapsortStep> getHeapifySortSteps(List<Integer> heap, List<Integer> sorted) {
        List<HeapsortStep> steps = new ArrayList<>();
        int index = 0;
        int size = heap.size();
        
        while (true) {
            int target = index;
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            
            if (left < size && heap.get(left) > heap.get(target)) target = left;
            if (right < size && heap.get(right) > heap.get(target)) target = right;
            
            if (target != index) {
                swap(heap, index, target);
                steps.add(new HeapsortStep(heap, sorted, target, false));
                index = target;
            } else {
                break;
            }
        }
        
        return steps;
    }
    
    private void playHeapsort() {
        if (heapsortSteps.isEmpty()) {
            setMessage("Start Heapsort first!");
            return;
        }
        
        isHeapsortRunning = true;
        if (startBtn != null) startBtn.setDisable(true);
        
        double speed = speedSlider != null ? speedSlider.getValue() : 1.0;
        int delayMs = (int)(1000 / speed);
        
        heapsortTimeline = new Timeline();
        for (int i = currentHeapsortStep; i < heapsortSteps.size(); i++) {
            int stepIdx = i;
            KeyFrame frame = new KeyFrame(Duration.millis((i - currentHeapsortStep) * delayMs), e -> {
                currentHeapsortStep = stepIdx;
                redrawHeapsort(stepIdx);
            });
            heapsortTimeline.getKeyFrames().add(frame);
        }
        
        heapsortTimeline.setOnFinished(e -> stopHeapsort());
        heapsortTimeline.play();
    }
    
    private void pauseHeapsort() {
        if (heapsortTimeline != null) {
            heapsortTimeline.pause();
            isHeapsortRunning = false;
            if (startBtn != null) startBtn.setDisable(false);
            setMessage("Heapsort paused");
        }
    }
    
    private void stopHeapsort() {
        if (heapsortTimeline != null) {
            heapsortTimeline.stop();
        }
        isHeapsortRunning = false;
        currentHeapsortStep = 0;
        heapsortSteps.clear();
        sortedArray.clear();
        if (startBtn != null) startBtn.setDisable(false);
        setMessage("Heapsort stopped");
        redraw();
    }
    
    private void nextHeapsortStep() {
        if (heapsortSteps.isEmpty()) {
            setMessage("Start Heapsort first!");
            return;
        }
        
        if (currentHeapsortStep < heapsortSteps.size() - 1) {
            currentHeapsortStep++;
            redrawHeapsort(currentHeapsortStep);
        } else {
            setMessage("Heapsort complete!");
        }
    }
    
    private void prevHeapsortStep() {
        if (currentHeapsortStep > 0) {
            currentHeapsortStep--;
            redrawHeapsort(currentHeapsortStep);
        }
    }

    // ============ HEAPSORT VISUALIZATION ============
    private void redrawHeapsort(int stepIdx) {
        if (stepIdx < 0 || stepIdx >= heapsortSteps.size()) return;
        
        HeapsortStep step = heapsortSteps.get(stepIdx);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        double canvasHeight = canvas.getHeight();
        
        // Draw heap
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 14));
        gc.fillText("Current Heap:", 20, 30);
        drawHeapArrayBoxesHeapsort(step.heapSnapshot, step.highlightIdx);
        
        drawHeapTreeHeapsort(step.heapSnapshot, step.highlightIdx);
        
        // Draw sorted array
        gc.setFill(Color.BLACK);
        gc.fillText("Sorted Array:", 20, 320);
        drawSortedArrayBoxes(step.sortedSnapshot);
    }
    
    private void drawHeapArrayBoxesHeapsort(List<Integer> snapshot, int highlightIdx) {
        double startX = (canvas.getWidth() - snapshot.size() * 50) / 2;
        double y = 50;
        double boxSize = 40;
        
        for (int i = 0; i < snapshot.size(); i++) {
            double x = startX + i * 50;
            
            Color fillColor = Color.WHITE;
            if (i == highlightIdx) {
                fillColor = Color.YELLOW;
            }
            
            gc.setFill(fillColor);
            gc.fillRect(x, y, boxSize, boxSize);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, boxSize, boxSize);
            
            gc.setFill(Color.BLACK);
            gc.setFont(new Font(14));
            gc.fillText(String.valueOf(snapshot.get(i)), x + 12, y + 25);
        }
    }
    
    private void drawHeapTreeHeapsort(List<Integer> snapshot, int highlightIdx) {
        double centerX = canvas.getWidth() / 2;
        double startY = 130;
        double verticalGap = 60;
        drawNodeHeapsort(snapshot, 0, centerX, startY, canvas.getWidth() / 4, verticalGap, highlightIdx);
    }
    
    private void drawNodeHeapsort(List<Integer> snapshot, int idx, double x, double y,
                                   double horizontalGap, double verticalGap, int highlightIdx) {
        if (idx >= snapshot.size()) return;
        
        int leftChild = 2 * idx + 1;
        int rightChild = 2 * idx + 2;
        double nextY = y + verticalGap;
        
        if (leftChild < snapshot.size()) {
            double leftX = x - horizontalGap;
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, leftX, nextY);
            drawNodeHeapsort(snapshot, leftChild, leftX, nextY, horizontalGap / 2, verticalGap, highlightIdx);
        }
        
        if (rightChild < snapshot.size()) {
            double rightX = x + horizontalGap;
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, rightX, nextY);
            drawNodeHeapsort(snapshot, rightChild, rightX, nextY, horizontalGap / 2, verticalGap, highlightIdx);
        }
        
        double radius = 20;
        Color fillColor = (idx == highlightIdx) ? Color.YELLOW : Color.web("#FF6B6B");
        gc.setFill(fillColor);
        gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);
        
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(14));
        String text = String.valueOf(snapshot.get(idx));
        gc.fillText(text, x - 6, y + 5);
    }
    
    private void drawSortedArrayBoxes(List<Integer> sorted) {
        double boxSize = 40;
        double spacing = 50;
        double startX = (canvas.getWidth() - sorted.size() * spacing) / 2;
        double y = 340;
        
        for (int i = 0; i < sorted.size(); i++) {
            double x = startX + i * spacing;
            
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRect(x, y, boxSize, boxSize);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, boxSize, boxSize);
            
            gc.setFill(Color.BLACK);
            gc.setFont(new Font(14));
            gc.fillText(String.valueOf(sorted.get(i)), x + 12, y + 25);
        }
    }

    // ---------------- Heapify with Step Recording ----------------
    private void heapifyUpAnimated(int index) {
        List<HeapStep> steps = new ArrayList<>();
        while (index > 0) {
            int parentIdx = (index - 1) / 2;
            boolean swapCandidate = (isMaxHeap ? heap.get(index) > heap.get(parentIdx) : heap.get(index) < heap.get(parentIdx));
            steps.add(new HeapStep(index, parentIdx, swapCandidate, heap));
            if (swapCandidate) {
                swap(index, parentIdx);
                index = parentIdx;
            } else break;
        }
        animateHeapSteps(steps);
    }

    private void heapifyDownAnimated(int index) {
        List<HeapStep> steps = getHeapifyDownSteps(index);
        animateHeapSteps(steps);
    }

    private List<HeapStep> getHeapifyDownSteps(int index) {
        int size = heap.size();
        List<HeapStep> steps = new ArrayList<>();
        while (true) {
            int target = index;
            int left = 2 * index + 1;
            int right = 2 * index + 2;

            if (left < size) {
                boolean swapCandidate = (isMaxHeap ? heap.get(left) > heap.get(target) : heap.get(left) < heap.get(target));
                steps.add(new HeapStep(index, left, swapCandidate, heap));
                if (swapCandidate) target = left;
            }
            if (right < size) {
                boolean swapCandidate = (isMaxHeap ? heap.get(right) > heap.get(target) : heap.get(right) < heap.get(target));
                steps.add(new HeapStep(index, right, swapCandidate, heap));
                if (swapCandidate) target = right;
            }

            if (target != index) {
                swap(index, target);
                index = target;
            } else break;
        }
        return steps;
    }

    private void swap(int i, int j) {
        int temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
    
    // Swap for heapsort
    private void swap(List<Integer> arr, int i, int j) {
        int temp = arr.get(i);
        arr.set(i, arr.get(j));
        arr.set(j, temp);
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
        // after animation completes show the current heap state (it has already been mutated)
        timeline.setOnFinished(e -> redraw());
        timeline.play();
    }

    // redraw with an optional step for highlighting
    private void redrawSnapshot(List<Integer> snapshot, HeapStep step) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawHeapArrayBoxes(snapshot);
        drawHeapTreeSnapshot(snapshot, step);
        updateHeapArray(snapshot);

        if (step != null) {
            // highlight the two indices in the array
            highlightArrayBox(step.idx1, step.swapped ? Color.ORANGE : Color.YELLOW, snapshot);
            highlightArrayBox(step.idx2, step.swapped ? Color.ORANGE : Color.YELLOW, snapshot);
        }
    }

    // convenience overload when there is no step information
    private void redrawSnapshot(List<Integer> snapshot) {
        redrawSnapshot(snapshot, null);
    }

    // ---------------- Visualization ----------------
    private void redraw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawHeapArrayBoxes(heap);
        updateHeapArray(heap);

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

    private void highlightArrayBox(int idx, Color color,List<Integer>snapshot) {
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

    /**
     * Draws the heap tree. When a step is provided, only the two indices involved in the
     * comparison/swapping are coloured (yellow or orange), and all other nodes are drawn
     * with a white fill. If step is null the normal heap-type colour is used.
     */
    private void drawHeapTreeSnapshot(List<Integer> snapshot, HeapStep step) {
        double centerX = canvas.getWidth() / 2;
        double startY = 100;
        double verticalGap = 60;
        // if no step or a dummy step (negative indices) we simply draw with the normal heap colour
        if (step == null || step.idx1 < 0) {
            drawNodeSnapshot(snapshot, 0, centerX, startY, canvas.getWidth() / 4, verticalGap, null);
        } else {
            drawNodeSnapshot(snapshot, 0, centerX, startY, canvas.getWidth() / 4, verticalGap, step);
        }
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
            gc.setLineWidth(2);
            gc.strokeLine(x, y, leftX, nextY);
            drawNodeSnapshot(snapshot, leftChild, leftX, nextY, horizontalGap / 2, verticalGap, step);
        }

        if (rightChild < snapshot.size()) {
            double rightX = x + horizontalGap;
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, rightX, nextY);
            drawNodeSnapshot(snapshot, rightChild, rightX, nextY, horizontalGap / 2, verticalGap, step);
        }

        // Draw the node circle
        double radius = 20;
        Color fillColor;
        if (step != null) {
            if (idx == step.idx1 || idx == step.idx2) {
                fillColor = step.swapped ? Color.ORANGE : Color.YELLOW;
            } else {
                fillColor = Color.WHITE;
            }
        } else {
            fillColor = isMaxHeap ? Color.web("#FF6B6B") : Color.web("#4ECDC4");
        }
        gc.setFill(fillColor);
        gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);

        // Draw the value
        //gc.setFill(step != null ? Color.BLACK : Color.WHITE);
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(14));
        String text = String.valueOf(snapshot.get(idx));
        gc.fillText(text, x - 6, y + 5);
    }

    private void updateHeapArray(List<Integer> snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("Heap Array (").append(isMaxHeap ? "Max" : "Min").append(" Heap):\n\n");
        sb.append("[ ");
        for (int i = 0; i < snapshot.size(); i++) {
            sb.append(snapshot.get(i));
            if (i < snapshot.size() - 1) sb.append(", ");
        }
        sb.append(" ]");

    }

    private void toggleCodeArea() {
        if (codeArea != null) {
            codeArea.setVisible(!codeArea.isVisible());
        }
    }

    private void copyCode() {
        String code = codeArea.getText();
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
        cc.putString(code);
        clipboard.setContent(cc);
        setMessage("Code copied to clipboard!");
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/heap.txt")) {
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
            } else {
                codeArea.setText("// Heap insertion example\n" +
                        "private void heapifyUp(int index) {\n" +
                        "    while (index > 0) {\n" +
                        "        int parent = (index - 1) / 2;\n" +
                        "        if (heap[index] > heap[parent]) {\n" +
                        "            swap(index, parent);\n" +
                        "            index = parent;\n" +
                        "        } else break;\n" +
                        "    }\n" +
                        "}");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setMessage(String msg) {
        lastMessage = msg;
        if (statusLabel != null) {
            statusLabel.setText(msg);
        }
    }
}

