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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class QueueController extends ModuleController {

    @FXML
    private TextField inputField;
    @FXML
    private Button enqueueBtn;
    @FXML
    private Button dequeueBtn;
    @FXML
    private Button frontBtn;
    @FXML
    private Button rearBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private CheckBox circularCheck;

    @FXML
    private HBox vizArea;
    @FXML
    private HBox resizeArea;
    @FXML
    private TextArea codeArea;
    @FXML
    private Button showCodeBtn;
    @FXML
    private Button copyCodeBtn;
    @FXML
    private TextArea storyArea;
    @FXML
    private Label queueSizeLabel;

    private final Queue<Integer> queue = new LinkedList<>();

    // dynamic circular buffer fields
    private int[] circularQueue;
    private int front = -1;
    private int rear = -1;
    private int capacity = 0;
    private int size = 0;
    private int[] resizePreview; // shown when resizing occurs

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Queue");
        storyArea.setText("Queue is FIFO (First In First Out).\nEnqueue at rear, Dequeue from front.\nCircular queue reuses space with 8 slots.");

        // initial queue listeners
        enqueueBtn.setOnAction(e -> enqueue());
        dequeueBtn.setOnAction(e -> dequeue());
        frontBtn.setOnAction(e -> showFront());
        rearBtn.setOnAction(e -> showRear());
        clearBtn.setOnAction(e -> clearQ());
        showCodeBtn.setOnAction(e -> toggleCode());
        copyCodeBtn.setOnAction(e -> copyCode());

        circularCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            queue.clear();
            if (newVal) {
                initCircular(2);
            } else {
                // drop circular buffer
                circularQueue = null;
                capacity = 0;
                size = 0;
                front = -1;
                rear = -1;
                resizePreview = null;
            }
            render();
        });
        
        // on startup ensure proper circular state
        if (circularCheck.isSelected()) {
            initCircular(2);
        }

        loadCodeFile();
        render();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enqueue() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        try {
            int val = Integer.parseInt(text);

            if (circularCheck.isSelected()) {
                resizePreview = null;
                enqueueCircular(val);
            } else {
                queue.add(val);
            }
            render();
            inputField.clear();
        } catch (NumberFormatException e) {
            showAlert("Valid number required");
        }
    }

    private void dequeue() {
        if (circularCheck.isSelected()) {
            resizePreview = null;
            if (front == -1) {
                showAlert("Queue underflow");
                return;
            }
            dequeueCircular();
        } else {
            if (queue.isEmpty()) {
                showAlert("Queue underflow");
                return;
            }
            queue.poll();
        }
        render();
    }

    private void showFront() {
        if (circularCheck.isSelected()) {
            if (size == 0) {
                showAlert("Queue empty");
                return;
            }
            showAlert("Front: " + circularQueue[front]);
        } else {
            if (queue.isEmpty()) {
                showAlert("Queue empty");
                return;
            }
            showAlert("Front: " + queue.peek());
        }
    }

    private void showRear() {
        if (circularCheck.isSelected()) {
            if (size == 0) {
                showAlert("Queue empty");
                return;
            }
            showAlert("Rear: " + circularQueue[rear]);
        } else {
            if (queue.isEmpty()) {
                showAlert("Queue empty");
                return;
            }
            Object[] arr = queue.toArray();
            showAlert("Rear: " + arr[arr.length - 1]);
        }
    }

    private void clearQ() {
        if (circularCheck.isSelected()) {
            // deallocate
            circularQueue = null;
            front = -1;
            rear = -1;
            capacity = 0;
            size = 0;
            resizePreview = null;
        } else {
            queue.clear();
        }
        render();
    }

    // Circular Queue Operations
    private void enqueueCircular(int val) {
        if (capacity == 0) {
            initCircular(2);
        }
        // resize if more than 50% full
        if (size > capacity * 0.5) {
            // show old array before resize
            resizePreview = Arrays.copyOf(circularQueue, capacity);
            resize(capacity * 2);
            // resizePreview now shows old, main shows new
        } else {
            resizePreview = null;
        }
        if (front == -1) front = 0;
        rear = (front + size) % capacity;
        circularQueue[rear] = val;
        size++;
        showAlert("Enqueued: " + val);
    }

    private void dequeueCircular() {
        if (size == 0) {
            showAlert("Queue underflow");
            return;
        }
        int val = circularQueue[front];
        circularQueue[front]=0;
        front = (front + 1) % capacity;
        size--;
        if (size == 0) {
            front = -1;
            rear = -1;
        }
        showAlert("Dequeued: " + val);
        // shrink if less than 25% full
        if (size < capacity * 0.25 && capacity > 2) {
            // show old array before shrink
            resizePreview = Arrays.copyOf(circularQueue, capacity);
            resize(Math.max(2, capacity / 2));
            // resizePreview shows old, main shows new
        } else {
            resizePreview = null;
        }
    }

    private int getCircularQueueSize() {
        return size;
    }

    private void render() {
        vizArea.getChildren().clear();

        if (circularCheck.isSelected()) {
            // Single central array visualization
            HBox queueContainer = new HBox();
            queueContainer.setStyle("-fx-spacing:5; -fx-alignment:center; -fx-padding:20;");

            for (int i = 0; i < capacity; i++) {
                Label lbl;
                if (size == 0) {
                    lbl = new Label("-");
                } else {
                    lbl = new Label(String.valueOf(circularQueue[i]));
                }

                String bgColor = "#f0f0f0"; // default empty color
                String textColor = "#666"; // default text color

                if (size > 0) {
                    // Check if this position is within the logical queue
                    boolean isInQueue = false;
                    for (int j = 0; j < size; j++) {
                        int idx = (front + j) % capacity;
                        if (i == idx) {
                            isInQueue = true;
                            break;
                        }
                    }

                    if (isInQueue) {
                        if (i == front) {
                            bgColor = "#4CAF50"; // Green for front
                            textColor = "white";
                        } else if (i == rear) {
                            bgColor = "#FF9800"; // Orange for rear
                            textColor = "white";
                        } else {
                            bgColor = "#2196F3"; // Blue for elements between front and rear
                            textColor = "white";
                        }
                    }
                }

                lbl.setStyle("-fx-border-color:black; -fx-border-width:2; -fx-padding:15; "
                        + "-fx-background-color:" + bgColor + "; -fx-text-fill:" + textColor + "; "
                        + "-fx-font-weight:bold; -fx-font-size:14; -fx-min-width:50; -fx-alignment:center;");
                queueContainer.getChildren().add(lbl);
            }

            vizArea.getChildren().add(queueContainer);

            // Show resize preview if exists
            if (resizePreview != null) {
                HBox previewContainer = new HBox();
                previewContainer.setStyle("-fx-spacing:5; -fx-alignment:center; -fx-padding:10;");

                Label previewLabel = new Label("Resizing to:");
                previewLabel.setStyle("-fx-font-weight:bold; -fx-padding:10;");
                previewContainer.getChildren().add(previewLabel);
                
                // Show elements in logical order after resize
                for (int i = 0; i < size; i++) {
                    int idx = (front + i) % capacity;
                    Label lbl = new Label(String.valueOf(circularQueue[idx]));
                    lbl.setStyle("-fx-border-color:#999; -fx-border-width:1; -fx-padding:10; "
                            + "-fx-background-color:#eee; -fx-font-weight:bold; -fx-font-size:12; "
                            + "-fx-min-width:40; -fx-alignment:center;");
                    previewContainer.getChildren().add(lbl);}
                resizeArea.getChildren().clear();
                resizeArea.getChildren().add(previewContainer);
            } else {
                resizeArea.getChildren().clear();
            }

            if (queueSizeLabel != null) {
                queueSizeLabel.setText("Size: " + getCircularQueueSize() + "/" + capacity +
                                     " | Front: " + (size > 0 ? front : "-") +
                                     " | Rear: " + (size > 0 ? rear : "-"));
            }
        } else {
            // Regular queue visualization
            HBox queueContainer = new HBox();
            queueContainer.setStyle("-fx-spacing:10; -fx-alignment:center;");

            List<Integer> queueList = new ArrayList<>(queue);
            for (int idx = 0; idx < queueList.size(); idx++) {
                Label lbl = new Label(String.valueOf(queueList.get(idx)));
                String bgColor = "#ccf";

                if (idx == 0) {
                    bgColor = "#90EE90"; // Green for front
                }
                if (idx == queueList.size() - 1) {
                    bgColor = "#FFD700"; // Gold for rear
                }
                if (queueList.size() == 1) {
                    bgColor = "#FF69B4"; // Pink if only one element
                }

                lbl.setStyle("-fx-border-color:black; -fx-border-width:2; -fx-padding:12; " +
                        "-fx-background-color:" + bgColor + "; -fx-font-weight:bold; -fx-font-size:16;");
                queueContainer.getChildren().add(lbl);
            }

            if (queueList.isEmpty()) {
                Label emptyLabel = new Label("Queue Empty");
                emptyLabel.setStyle("-fx-font-size:14; -fx-text-fill:#999;");
                queueContainer.getChildren().add(emptyLabel);
            }

            vizArea.getChildren().add(queueContainer);

            if (queueSizeLabel != null) {
                queueSizeLabel.setText("Size: " + queue.size());
            }
        }
    }

    // initialize circular buffer with given capacity
     private void initCircular(int cap) {
        capacity = cap;
        circularQueue = new int[capacity];
        front = -1;
        rear = -1;
        size = 0;
        resizePreview = null;
    }

    // resize underlying array to new capacity, reordering elements
    private void resize(int newCap) {
        int[] newArr = new int[newCap];
        for (int i = 0; i < size; i++) {
            newArr[i] = circularQueue[(front + i) % capacity];
        }
        circularQueue = newArr;
        capacity = newCap;
        front = 0;
        rear = size - 1;
    }
}
