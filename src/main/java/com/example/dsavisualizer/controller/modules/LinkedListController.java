package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LinkedListController extends ModuleController {

    @FXML
    private StackPane sllVizArea, dllVizArea;
    @FXML
    private Button startBtn, playBtn, pauseBtn, prevBtn, nextBtn;
    @FXML
    private Button insertHeadBtn, insertTailBtn, insertIndexBtn, getHeadBtn, getTailBtn, getIndexBtn;
    @FXML
    private Button removeHeadBtn, removeTailBtn, removeIndexBtn, lengthBtn, findBtn, copyCodeBtn;
    @FXML
    private TextField indexField, valueField;
    @FXML
    private Label statusLabel;

    // Node classes
    private static class Node {
        int data;
        Node next;

        Node(int d) {
            data = d;
            next = null;
        }
    }

    private static class DNode {
        int data;
        DNode next, prev;

        DNode(int d) {
            data = d;
            next = null;
            prev = null;
        }
    }

    private Node sllHead = null;
    private DNode dllHead = null, dllTail = null;
    private List<Step> steps = new ArrayList<>();
    private int currentStep = -1;
    private Timeline playTimeline;

    // Step structure
    private static class Step {
        String message;
        List<Integer> sllHighlights, dllHighlights;

        Step(String msg, List<Integer> sll, List<Integer> dll) {
            message = msg;
            sllHighlights = new ArrayList<>(sll);
            dllHighlights = new ArrayList<>(dll);
        }
    }

    public void initialize() {
        super.initialize();
        titleLabel.setText("Linked List");
        storyArea.setText("Imagine a treasure hunt game .\n" +
                "Each clue leads to the next clue.\n" +
                "You start with the first clue:\n" +
                "Clue 1 → tells you where Clue 2 is\n" +
                " Clue 2 → tells you where Clue 3 is\n" +
                " Clue 3 → leads to the treasure \n" +
                "Each clue contains:\n" +
                "The current information\n" +
                "A direction to the next clue");
        setupControlButtons();
        setupVisualization();
    }

    private void setupControlButtons() {
        startBtn.setOnAction(e -> startAnimation());
        playBtn.setOnAction(e -> playAnimation());
        pauseBtn.setOnAction(e -> pauseAnimation());
        prevBtn.setOnAction(e -> prevStep());
        nextBtn.setOnAction(e -> nextStep());

        insertHeadBtn.setOnAction(e -> insertHead());
        insertTailBtn.setOnAction(e -> insertTail());
        insertIndexBtn.setOnAction(e -> insertIndex());
        getHeadBtn.setOnAction(e -> getHead());
        getTailBtn.setOnAction(e -> getTail());
        getIndexBtn.setOnAction(e -> getIndex());
        removeHeadBtn.setOnAction(e -> removeHead());
        removeTailBtn.setOnAction(e -> removeTail());
        removeIndexBtn.setOnAction(e -> removeIndex());
        lengthBtn.setOnAction(e -> getLength());
        findBtn.setOnAction(e -> findNode());
        showCodeBtn.setOnAction(e -> toggleCode());
        copyCodeBtn.setOnAction(e -> copyCode());
        loadCodeFile();

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
        try (InputStream is = getClass().getResourceAsStream("/codes/linkedlist.txt")) {
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

    private void setupVisualization() {
        drawSLL();
        drawDLL();
    }

    private void recordStep(String msg, List<Integer> sll, List<Integer> dll) {
        steps.add(new Step(msg, sll, dll));
    }

    private void redrawCurrentStep() {
        if (currentStep < 0 || currentStep >= steps.size())
            return;
        Step step = steps.get(currentStep);
        statusLabel.setText(step.message);
        drawSLL(step.sllHighlights);
        drawDLL(step.dllHighlights);
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

    // SLL operations
    private void sllInsertHead(int val) {
        Node newNode = new Node(val);
        newNode.next = sllHead;
        sllHead = newNode;
    }

    private void sllInsertTail(int val) {
        Node newNode = new Node(val);
        if (sllHead == null) {
            sllHead = newNode;
            return;
        }
        Node temp = sllHead;
        while (temp.next != null)
            temp = temp.next;
        temp.next = newNode;
    }

    private void sllInsertIndex(int idx, int val) {
        if (idx == 0) {
            sllInsertHead(val);
            return;
        }
        Node newNode = new Node(val);
        Node temp = sllHead;
        for (int i = 0; i < idx - 1 && temp != null; i++)
            temp = temp.next;
        if (temp == null)
            return;
        newNode.next = temp.next;
        temp.next = newNode;
    }

    private int sllGetHead() {
        return sllHead != null ? sllHead.data : -1;
    }

    private int sllGetTail() {
        if (sllHead == null)
            return -1;
        Node temp = sllHead;
        while (temp.next != null)
            temp = temp.next;
        return temp.data;
    }

    private int sllGetIndex(int idx) {
        Node temp = sllHead;
        for (int i = 0; i < idx && temp != null; i++)
            temp = temp.next;
        return temp != null ? temp.data : -1;
    }

    private void sllRemoveHead() {
        if (sllHead != null)
            sllHead = sllHead.next;
    }

    private void sllRemoveTail() {
        if (sllHead == null)
            return;
        if (sllHead.next == null) {
            sllHead = null;
            return;
        }
        Node temp = sllHead;
        while (temp.next.next != null)
            temp = temp.next;
        temp.next = null;
    }

    private void sllRemoveIndex(int idx) {
        if (idx == 0) {
            sllRemoveHead();
            return;
        }
        Node temp = sllHead;
        for (int i = 0; i < idx - 1 && temp != null; i++)
            temp = temp.next;
        if (temp != null && temp.next != null)
            temp.next = temp.next.next;
    }

    private int sllLength() {
        int len = 0;
        Node temp = sllHead;
        while (temp != null) {
            len++;
            temp = temp.next;
        }
        return len;
    }

    private int sllFind(int val) {
        Node temp = sllHead;
        int idx = 0;
        while (temp != null) {
            if (temp.data == val)
                return idx;
            temp = temp.next;
            idx++;
        }
        return -1;
    }

    // DLL operations
    private void dllInsertHead(int val) {
        DNode newNode = new DNode(val);
        if (dllHead == null) {
            dllHead = dllTail = newNode;
            return;
        }
        newNode.next = dllHead;
        dllHead.prev = newNode;
        dllHead = newNode;
    }

    private void dllInsertTail(int val) {
        DNode newNode = new DNode(val);
        if (dllHead == null) {
            dllHead = dllTail = newNode;
            return;
        }
        dllTail.next = newNode;
        newNode.prev = dllTail;
        dllTail = newNode;
    }

    private void dllInsertIndex(int idx, int val) {
        if (idx == 0) {
            dllInsertHead(val);
            return;
        }
        DNode newNode = new DNode(val);
        DNode temp = dllHead;
        for (int i = 0; i < idx - 1 && temp != null; i++)
            temp = temp.next;
        if (temp == null)
            return;
        newNode.next = temp.next;
        if (temp.next != null)
            temp.next.prev = newNode;
        temp.next = newNode;
        newNode.prev = temp;
    }

    private int dllGetHead() {
        return dllHead != null ? dllHead.data : -1;
    }

    private int dllGetTail() {
        return dllTail != null ? dllTail.data : -1;
    }

    private int dllGetIndex(int idx) {
        DNode temp = dllHead;
        for (int i = 0; i < idx && temp != null; i++)
            temp = temp.next;
        return temp != null ? temp.data : -1;
    }

    private void dllRemoveHead() {
        if (dllHead != null) {
            dllHead = dllHead.next;
            if (dllHead != null) {
                dllHead.prev = null;
            } else {
                dllTail = null;
            }
        }
    }

    private void dllRemoveTail() {
        if (dllTail == null)
            return;
        if (dllHead == dllTail) {
            dllHead = dllTail = null;
            return;
        }
        dllTail = dllTail.prev;
        dllTail.next = null;
    }

    private void dllRemoveIndex(int idx) {
        if (idx == 0) {
            dllRemoveHead();
            return;
        }
        DNode temp = dllHead;
        for (int i = 0; i < idx && temp != null; i++)
            temp = temp.next;
        if (temp == null)
            return;
        if (temp.prev != null)
            temp.prev.next = temp.next;
        if (temp.next != null)
            temp.next.prev = temp.prev;
    }

    private int dllLength() {
        int len = 0;
        DNode temp = dllHead;
        while (temp != null) {
            len++;
            temp = temp.next;
        }
        return len;
    }

    private int dllFind(int val) {
        DNode temp = dllHead;
        int idx = 0;
        while (temp != null) {
            if (temp.data == val)
                return idx;
            temp = temp.next;
            idx++;
        }
        return -1;
    }

    // Operations
    private void insertHead() {
        try {
            int val = Integer.parseInt(valueField.getText());
            steps.clear();
            sllInsertHead(val);
            dllInsertHead(val);
            setupVisualization();
            statusLabel.setText("Inserted " + val + " at head | Time: SLL O(1), DLL O(1)");
            recordStep("Inserted " + val + " at head | Time: SLL O(1), DLL O(1)", List.of(0), List.of(0));
            currentStep = 0;
            redrawCurrentStep();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid value input");
        }
    }

    private void insertTail() {
        try {
            int val = Integer.parseInt(valueField.getText());
            steps.clear();
            sllInsertTail(val);
            dllInsertTail(val);
            setupVisualization();
            statusLabel.setText("Inserted " + val + " at tail | Time: SLL O(n), DLL O(1)");
            recordStep("Inserted " + val + " at tail | Time: SLL O(n), DLL O(1)", List.of(sllLength() - 1),
                    List.of(dllLength() - 1));
            currentStep = 0;
            redrawCurrentStep();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid value input");
        }
    }

    private void insertIndex() {
        try {
            int idx = Integer.parseInt(indexField.getText());
            int val = Integer.parseInt(valueField.getText());
            if (idx < 0) {
                statusLabel.setText("Invalid index: " + idx + " (must be >= 0)");
                return;
            }
            steps.clear();
            sllInsertIndex(idx, val);
            dllInsertIndex(idx, val);
            setupVisualization();
            statusLabel.setText("Inserted " + val + " at index " + idx + " | Time: SLL O(n), DLL O(n)");
            recordStep("Inserted " + val + " at index " + idx + " | Time: SLL O(n), DLL O(n)", List.of(idx),
                    List.of(idx));
            currentStep = 0;
            redrawCurrentStep();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid input");
        }
    }

    private void getHead() {
        steps.clear();
        statusLabel.setText("Get Head: SLL O(1), DLL O(1)");
        int sllVal = sllGetHead();
        int dllVal = dllGetHead();
        recordStep("Head: SLL=" + sllVal + ", DLL=" + dllVal + " | Time: SLL O(1), DLL O(1)", List.of(0), List.of(0));
        currentStep = 0;
        redrawCurrentStep();
    }

    private void getTail() {
        steps.clear();
        statusLabel.setText("Get Tail: SLL O(n), DLL O(1)");
        int sllVal = sllGetTail();
        int dllVal = dllGetTail();
        recordStep("Tail: SLL=" + sllVal + ", DLL=" + dllVal + " | Time: SLL O(n), DLL O(1)", List.of(sllLength() - 1),
                List.of(dllLength() - 1));
        currentStep = 0;
        redrawCurrentStep();
    }

    private void getIndex() {
        try {
            int idx = Integer.parseInt(indexField.getText());
            if (idx < 0) {
                statusLabel.setText("Invalid index: " + idx + " (must be >= 0)");
                return;
            }
            steps.clear();
            statusLabel.setText("Get Index: SLL O(n), DLL O(n)");
            int sllVal = sllGetIndex(idx);
            int dllVal = dllGetIndex(idx);
            recordStep("Index " + idx + ": SLL=" + sllVal + ", DLL=" + dllVal + " | Time: SLL O(n), DLL O(n)",
                    List.of(idx), List.of(idx));
            currentStep = 0;
            redrawCurrentStep();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid index input");
        }
    }

    private void removeHead() {
        steps.clear();
        statusLabel.setText("Remove Head: SLL O(1), DLL O(1)");
        sllRemoveHead();
        dllRemoveHead();
        setupVisualization();
        recordStep("Removed head | Time: SLL O(1), DLL O(1)", List.of(0), List.of(0));
        currentStep = 0;
        redrawCurrentStep();
    }

    private void removeTail() {
        steps.clear();
        statusLabel.setText("Remove Tail: SLL O(n), DLL O(1)");
        sllRemoveTail();
        dllRemoveTail();
        setupVisualization();
        recordStep("Removed tail | Time: SLL O(n), DLL O(1)", List.of(sllLength()), List.of(dllLength()));
        currentStep = 0;
        redrawCurrentStep();
    }

    private void removeIndex() {
        try {
            int idx = Integer.parseInt(indexField.getText());
            if (idx < 0) {
                statusLabel.setText("Invalid index: " + idx + " (must be >= 0)");
                return;
            }
            steps.clear();
            statusLabel.setText("Remove Index: SLL O(n), DLL O(n)");
            sllRemoveIndex(idx);
            dllRemoveIndex(idx);
            setupVisualization();
            recordStep("Removed at index " + idx + " | Time: SLL O(n), DLL O(n)", List.of(idx), List.of(idx));
            currentStep = 0;
            redrawCurrentStep();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid index input");
        }
    }

    private void getLength() {
        steps.clear();
        statusLabel.setText("Get Length: SLL O(n), DLL O(n)");
        int sllLen = sllLength();
        int dllLen = dllLength();
        recordStep("Length: SLL=" + sllLen + ", DLL=" + dllLen + " | Time: SLL O(n), DLL O(n)", List.of(), List.of());
        currentStep = 0;
        redrawCurrentStep();
    }

    private void findNode() {
        try {
            int val = Integer.parseInt(valueField.getText());
            steps.clear();
            statusLabel.setText("Find Node: SLL O(n), DLL O(n)");
            int sllIdx = sllFind(val);
            int dllIdx = dllFind(val);
            List<Integer> sllH = sllIdx >= 0 ? List.of(sllIdx) : List.of();
            List<Integer> dllH = dllIdx >= 0 ? List.of(dllIdx) : List.of();
            recordStep("Find " + val + ": SLL idx=" + sllIdx + ", DLL idx=" + dllIdx + " | Time: SLL O(n), DLL O(n)",
                    sllH, dllH);
            currentStep = 0;
            redrawCurrentStep();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid value input");
        }
    }

    private void drawSLL() {
        drawSLL(List.of());
    }

    private void drawSLL(List<Integer> highlights) {
        sllVizArea.getChildren().clear();
        HBox hbox = new HBox(20);
        hbox.setAlignment(Pos.CENTER);
        Node temp = sllHead;
        int idx = 0;
        while (temp != null) {
            VBox nodeBox = new VBox(5);
            nodeBox.setAlignment(Pos.CENTER);
            Label dataLabel = new Label(String.valueOf(temp.data));
            dataLabel.setStyle(
                    "-fx-font-size:18; -fx-font-weight:bold; -fx-padding:10; -fx-border-color:black; -fx-border-width:2;");
            if (highlights.contains(idx)) {
                dataLabel.setStyle(
                        "-fx-font-size:18; -fx-font-weight:bold; -fx-padding:10; -fx-border-color:orange; -fx-border-width:4; -fx-background-color:yellow;");
            }
            nodeBox.getChildren().add(dataLabel);
            hbox.getChildren().add(nodeBox);
            if (temp.next != null) {
                Text arrow = new Text("→");
                arrow.setStyle("-fx-font-size:24; -fx-font-weight:bold;");
                hbox.getChildren().add(arrow);
            }
            temp = temp.next;
            idx++;
        }
        sllVizArea.getChildren().add(hbox);
    }

    private void drawDLL() {
        drawDLL(List.of());
    }

    private void drawDLL(List<Integer> highlights) {
        dllVizArea.getChildren().clear();
        HBox hbox = new HBox(20);
        hbox.setAlignment(Pos.CENTER);
        DNode temp = dllHead;
        int idx = 0;
        while (temp != null) {
            VBox nodeBox = new VBox(5);
            nodeBox.setAlignment(Pos.CENTER);
            Label dataLabel = new Label(String.valueOf(temp.data));
            dataLabel.setStyle(
                    "-fx-font-size:18; -fx-font-weight:bold; -fx-padding:10; -fx-border-color:black; -fx-border-width:2;");
            if (highlights.contains(idx)) {
                dataLabel.setStyle(
                        "-fx-font-size:18; -fx-font-weight:bold; -fx-padding:10; -fx-border-color:orange; -fx-border-width:4; -fx-background-color:yellow;");
            }
            nodeBox.getChildren().add(dataLabel);
            hbox.getChildren().add(nodeBox);
            if (temp.next != null) {
                Text arrow = new Text("↔");
                arrow.setStyle("-fx-font-size:24; -fx-font-weight:bold;");
                hbox.getChildren().add(arrow);
            }
            temp = temp.next;
            idx++;
        }
        dllVizArea.getChildren().add(hbox);
    }
}