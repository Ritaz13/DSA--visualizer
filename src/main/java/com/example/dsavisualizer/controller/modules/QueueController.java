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


    private static final int CIRCULAR_SIZE = 8;
    private int[] circularQueue;
    private int front = -1;
    private int rear = -1;

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Queue");
        storyArea.setText("Queue is FIFO (First In First Out).\nEnqueue at rear, Dequeue from front.\nCircular queue reuses space with 8 slots.");

        circularQueue = new int[CIRCULAR_SIZE];

        enqueueBtn.setOnAction(e -> enqueue());
        dequeueBtn.setOnAction(e -> dequeue());
        frontBtn.setOnAction(e -> showFront());
        rearBtn.setOnAction(e -> showRear());
        clearBtn.setOnAction(e -> clearQ());
        showCodeBtn.setOnAction(e -> toggleCode());
        copyCodeBtn.setOnAction(e -> copyCode());

        circularCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            queue.clear();
            front = -1;
            rear = -1;
            render();
        });

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
            if (front == -1) {
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
            if (rear == -1) {
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
            front = -1;
            rear = -1;
        } else {
            queue.clear();
        }
        render();
    }

    // Circular Queue Operations
    private void enqueueCircular(int val) {
        if ((rear + 1) % CIRCULAR_SIZE == front) {
            showAlert("Circular Queue is FULL");
            return;
        }

        if (front == -1) front = 0;
        rear = (rear + 1) % CIRCULAR_SIZE;
        circularQueue[rear] = val;
        showAlert("Enqueued: " + val);
    }

    private void dequeueCircular() {
        int val = circularQueue[front];

        if (front == rear) {
            front = -1;
            rear = -1;
        } else {
            front = (front + 1) % CIRCULAR_SIZE;
        }

        showAlert("Dequeued: " + val);
    }

    private int getCircularQueueSize() {
        if (front == -1) return 0;
        if (rear >= front) {
            return rear - front + 1;
        } else {
            return CIRCULAR_SIZE - front + rear + 1;
        }
    }

    private void render() {
        vizArea.getChildren().clear();

        if (circularCheck.isSelected()) {

            HBox circularRow = new HBox();
            circularRow.setStyle("-fx-spacing:15; -fx-alignment:center;");

            for (int i = 0; i < CIRCULAR_SIZE; i++) {

                VBox slot = new VBox();
                slot.setStyle("-fx-alignment:center; -fx-spacing:5;");


                Label indexLabel = new Label(String.valueOf(i));
                indexLabel.setStyle("-fx-font-size:12; -fx-text-fill:#555;");


                Label valueLabel = new Label(
                        circularQueue[i] == 0 &&
                                !((front != -1 && i == front) || (rear != -1 && i == rear))
                                ? "-"
                                : String.valueOf(circularQueue[i])
                );

                String bgColor = "#f0f0f0";

                if (front != -1 && rear != -1) {
                    if (front <= rear) {
                        if (i >= front && i <= rear) bgColor = "#ffcccc";
                    } else {
                        if (i >= front || i <= rear) bgColor = "#ffcccc";
                    }
                }

                if (i == front && front != -1) bgColor = "#90EE90";
                if (i == rear && rear != -1) bgColor = "#FFD700";

                valueLabel.setStyle(
                        "-fx-border-color:black; " +
                                "-fx-border-width:2; " +
                                "-fx-padding:15; " +
                                "-fx-background-color:" + bgColor + "; " +
                                "-fx-font-weight:bold; " +
                                "-fx-font-size:14;"
                );

                slot.getChildren().addAll(indexLabel, valueLabel);
                circularRow.getChildren().add(slot);
            }

            vizArea.getChildren().add(circularRow);


            if (queueSizeLabel != null) {
                queueSizeLabel.setText("Size: " + getCircularQueueSize() + "/8");
            }
        } else {


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


    protected void showAlert(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
        }
    }
}
