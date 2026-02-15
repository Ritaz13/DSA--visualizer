package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeapController extends ModuleController {

    @FXML private TextField inputField;
    @FXML private Button insertBtn;
    @FXML private Button deleteBtn;
    @FXML private Button extractBtn;
    @FXML private Button buildBtn;
    @FXML private Button randomBtn;
    @FXML private Button clearBtn;
    @FXML private CheckBox maxHeapCheck;
    @FXML private StackPane vizArea;
    @FXML private TextArea codeArea;
    @FXML private TextArea storyArea;
    @FXML private Button showCodeBtn;
    @FXML private Button copyCodeBtn;
    @FXML private Label statusLabel;
    @FXML private TextArea heapArrayArea;

    private Canvas canvas;
    private GraphicsContext gc;
    private List<Integer> heap = new ArrayList<>();
    private boolean isMaxHeap = true;
    private String lastMessage = "";

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Heap");
        storyArea.setText("Heap is a complete binary tree with heap property.\nMax Heap: Parent >= Children\nMin Heap: Parent <= Children\nUsed in priority queues and sorting.");

        canvas = new Canvas(700, 400);
        gc = canvas.getGraphicsContext2D();
        vizArea.getChildren().add(canvas);

        maxHeapCheck.setSelected(isMaxHeap);
        maxHeapCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isMaxHeap = newVal;
            redraw();
        });

        insertBtn.setOnAction(e -> insertElement());
        deleteBtn.setOnAction(e -> deleteMin());
        extractBtn.setOnAction(e -> extractRoot());
        buildBtn.setOnAction(e -> buildFromInput());
        randomBtn.setOnAction(e -> generateRandom());
        clearBtn.setOnAction(e -> clearHeap());
        showCodeBtn.setOnAction(e -> toggleCodeArea());
        copyCodeBtn.setOnAction(e -> copyCode());

        loadCodeFile();
        redraw();
    }

    private void insertElement() {
        try {
            int val = Integer.parseInt(inputField.getText().trim());
            heap.add(val);
            heapifyUp(heap.size() - 1);
            inputField.clear();
            setMessage("Inserted " + val);
            redraw();
        } catch (NumberFormatException ex) {
            setMessage("Enter a valid number");
        }
    }

    private void deleteMin() {
        if (heap.isEmpty()) {
            setMessage("Heap is empty!");
            return;
        }
        int removed = heap.get(0);
        heap.set(0, heap.get(heap.size() - 1));
        heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) heapifyDown(0);
        setMessage("Removed " + removed);
        redraw();
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

    private void buildFromInput() {
        try {
            String[] parts = inputField.getText().split(",");
            heap.clear();
            for (String part : parts) {
                heap.add(Integer.parseInt(part.trim()));
            }
            buildHeap();
            inputField.clear();
            setMessage("Built heap from input. Elements: " + heap.size());
            redraw();
        } catch (NumberFormatException ex) {
            setMessage("Enter comma-separated numbers");
        }
    }

    private void generateRandom() {
        heap.clear();
        Random rand = new Random();
        int size = 7 + rand.nextInt(6);
        for (int i = 0; i < size; i++) {
            heap.add(rand.nextInt(100) + 1);
        }
        buildHeap();
        setMessage("Generated random heap with " + size + " elements");
        redraw();
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIdx = (index - 1) / 2;
            if (isMaxHeap) {
                if (heap.get(index) > heap.get(parentIdx)) {
                    swap(index, parentIdx);
                    index = parentIdx;
                } else break;
            } else {
                if (heap.get(index) < heap.get(parentIdx)) {
                    swap(index, parentIdx);
                    index = parentIdx;
                } else break;
            }
        }
    }

    private void heapifyDown(int index) {
        int size = heap.size();
        while (true) {
            int largest = index;
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;

            if (isMaxHeap) {
                if (leftChild < size && heap.get(leftChild) > heap.get(largest)) {
                    largest = leftChild;
                }
                if (rightChild < size && heap.get(rightChild) > heap.get(largest)) {
                    largest = rightChild;
                }
            } else {
                if (leftChild < size && heap.get(leftChild) < heap.get(largest)) {
                    largest = leftChild;
                }
                if (rightChild < size && heap.get(rightChild) < heap.get(largest)) {
                    largest = rightChild;
                }
            }

            if (largest != index) {
                swap(index, largest);
                index = largest;
            } else break;
        }
    }

    private void buildHeap() {
        for (int i = heap.size() / 2 - 1; i >= 0; i--) {
            heapifyDown(i);
        }
    }

    private void swap(int i, int j) {
        int temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    private void redraw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        updateHeapArray();

        if (heap.isEmpty()) {
            gc.setFill(Color.GRAY);
            gc.setFont(new Font(16));
            gc.fillText("Heap is empty. Add elements to visualize.", 200, 200);
            return;
        }

        drawHeapTree();
    }

    private void drawHeapTree() {
        double centerX = canvas.getWidth() / 2;
        double startY = 30;
        double verticalGap = 60;

        drawNode(0, centerX, startY, canvas.getWidth() / 4, verticalGap);
    }

    private void drawNode(int idx, double x, double y, double horizontalGap, double verticalGap) {
        if (idx >= heap.size()) return;

        int leftChild = 2 * idx + 1;
        int rightChild = 2 * idx + 2;
        double nextY = y + verticalGap;

        if (leftChild < heap.size()) {
            double leftX = x - horizontalGap;
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, leftX, nextY);
            drawNode(leftChild, leftX, nextY, horizontalGap / 2, verticalGap);
        }

        if (rightChild < heap.size()) {
            double rightX = x + horizontalGap;
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, rightX, nextY);
            drawNode(rightChild, rightX, nextY, horizontalGap / 2, verticalGap);
        }

        // Draw the node circle
        double radius = 20;
        Color nodeColor = isMaxHeap ? Color.web("#FF6B6B") : Color.web("#4ECDC4");
        gc.setFill(nodeColor);
        gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);

        // Draw the value
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(14));
        String text = String.valueOf(heap.get(idx));
        gc.fillText(text, x - 6, y + 5);
    }

    private void updateHeapArray() {
        StringBuilder sb = new StringBuilder();
        sb.append("Heap Array (").append(isMaxHeap ? "Max" : "Min").append(" Heap):\n\n");
        sb.append("[ ");
        for (int i = 0; i < heap.size(); i++) {
            sb.append(heap.get(i));
            if (i < heap.size() - 1) sb.append(", ");
        }
        sb.append(" ]");
        heapArrayArea.setText(sb.toString());
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
                codeArea.setText("// Heap insertion\nprivate void heapifyUp(int index) {\n" +
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
