package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BSTController extends ModuleController {

    @FXML
    private TextField inputField, opField;
    @FXML
    private Button buildBtn, insertBtn, deleteBtn, searchBtn;
    @FXML
    private Button inOrderBtn, preOrderBtn, postOrderBtn, levelOrderBtn;
    @FXML
    private Button successorBtn, predecessorBtn, randomBtn;
    @FXML
    private Button minimumBtn, maximumBtn, clearVizBtn;
    @FXML
    private Button nextBtn, prevBtn; // for step traversal or op animation
    @FXML
    private Slider speedSlider;
    @FXML
    private StackPane vizArea;
    @FXML
    private TextArea codeArea, storyArea;
    @FXML
    private Button showCodeBtn, copyCodeBtn;
    @FXML
    private Label statusLabel; // bottom message
    @FXML
    private TextArea traversalResult;

    private Node root;
    private List<Node> traversalSteps = new ArrayList<>();
    private int stepIndex = -1;
    private Timer stepTimer;
    private boolean isRunning = false;
    @FXML
    private Button startBtn, stopBtn;
    
    // For build from array visualization
    private List<Integer> buildInput = new ArrayList<>();
    private int buildStepIndex = -1;

    // common animation path for operations
    private List<Node> opPath = new ArrayList<>();
    private Timeline opTimeline;

    private void startTraversalAuto() {
        if (traversalSteps.isEmpty()) return;
        if (isRunning) return;
        isRunning = true;
        startBtn.setText("⏸ Pause");
        stepTimer = new Timer();

        double speed = speedSlider != null ? speedSlider.getValue() : 1.0;
        int delayMs = (int)(1000 / speed);
        int initialDelay = delayMs + 200; // slight extra delay for first node

        stepTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (stepIndex < traversalSteps.size() - 1) {
                        stepIndex++;
                        redraw();
                        if (traversalResult != null) {
                            traversalResult.setText(traversalResult.getText().split(":")[0] + ": " + getTraversalString());
                        }
                    } else {
                        stopTraversalAuto();
                    }
                });
            }
        }, initialDelay, delayMs);
    }

    private void stopTraversalAuto() {
        if (stepTimer != null) {
            stepTimer.cancel();
            stepTimer = null;
        }
        isRunning = false;
        startBtn.setText("▶ Start");
    }

    private void togglePauseTraversal() {
        if (isRunning) {
            stopTraversalAuto();
        } else {
            startTraversalAuto();
        }
    }



    private Canvas canvas;
    private GraphicsContext gc;

    @Override
    protected void initialize() {
        super.initialize();
        titleLabel.setText("Binary Search Tree");
        storyArea.setText("BST: left < parent < right.\nUsed for sorted data and fast lookups.");

        canvas = new Canvas(700, 400);
        gc = canvas.getGraphicsContext2D();
        vizArea.getChildren().add(canvas);

        if (speedSlider != null) {
            speedSlider.setMin(0.5);
            speedSlider.setMax(3);
            speedSlider.setValue(1);
        }

        buildBtn.setOnAction(e -> buildBST());
        insertBtn.setOnAction(e -> insertNode());
        deleteBtn.setOnAction(e -> deleteNode());
        searchBtn.setOnAction(e -> searchNode());
        inOrderBtn.setOnAction(e -> startInorderTraversal());
        preOrderBtn.setOnAction(e -> startPreorderTraversal());
        postOrderBtn.setOnAction(e -> startPostorderTraversal());
        levelOrderBtn.setOnAction(e -> startLevelorderTraversal());
        successorBtn.setOnAction(e -> findSuccessor());
        predecessorBtn.setOnAction(e -> findPredecessor());
        randomBtn.setOnAction(e -> generateRandomBST());

        nextBtn.setOnAction(e -> nextStep());
        prevBtn.setOnAction(e -> prevStep());
        startBtn.setOnAction(e -> togglePauseTraversal());
        stopBtn.setOnAction(e -> stopTraversalAuto());

        // operation buttons
        successorBtn.setOnAction(e -> animateSuccessor());
        predecessorBtn.setOnAction(e -> animatePredecessor());
        minimumBtn.setOnAction(e -> animateMinimum());
        maximumBtn.setOnAction(e -> animateMaximum());
        clearVizBtn.setOnAction(e -> clearVisualization());

        showCodeBtn.setOnAction(e -> toggleCodeArea());
        copyCodeBtn.setOnAction(e -> copyCode());
        loadCodeFile();
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
    }

    private void loadCodeFile() {
        try (InputStream is = getClass().getResourceAsStream("/codes/bst.txt")) {
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
                codeArea.setText("No bst.txt file found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            codeArea.setText("Error loading bst.txt");
        }
    }

    // Node class
    private static class Node {
        int val;
        Node left, right;

        Node(int v) {
            val = v;
        }
    }

    private Node findNode(Node root, int val) {
        Node current = root;
        while (current != null) {
            if (val == current.val) return current;
            if (val < current.val) current = current.left;
            else current = current.right;
        }
        return null;
    }

    // Utility
    private void showMessage(String msg) {
        statusLabel.setText(msg);
    }

    // Build BST
    private void buildBST() {
        root = null;
        buildInput.clear();
        buildStepIndex = 0;
        
        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            showMessage("Enter numbers separated by commas");
            return;
        }
        
        String[] parts = input.split(",");
        for (String p : parts) {
            try {
                buildInput.add(Integer.parseInt(p.trim()));
            } catch (Exception ignored) {
            }
        }
        
        if (buildInput.isEmpty()) {
            showMessage("No valid numbers found");
            return;
        }
        
        // Animate insertion
        animateBuildSteps();
    }
    
    private void animateBuildSteps() {
        Timeline timeline = new Timeline();
        
        for (int i = 0; i < buildInput.size(); i++) {
            final int idx = i;
            KeyFrame frame = new KeyFrame(Duration.millis(i * 800), e -> {
                buildStepIndex = idx;
                insert(buildInput.get(idx));
                redraw();
                showMessage("Inserted " + buildInput.get(idx) + " (" + (idx + 1) + "/" + buildInput.size() + ")");
            });
            timeline.getKeyFrames().add(frame);
        }
        
        timeline.setOnFinished(e -> {
            buildStepIndex = -1;
            showMessage("BST built with " + buildInput.size() + " nodes.");
            redraw();
        });
        timeline.play();
    }

    private void insertNode() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            opPath.clear();
            collectPathForInsert(root, val);
            animateNodePath(opPath, "Inserting " + val, () -> {
                insert(val);
                redraw();
            });
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }

    private void deleteNode() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            Node targetNode = findNode(root, val);
            if (targetNode == null) {
                showMessage("Node not found");
                return;
            }
            
            // Start detailed delete animation
            animateDeleteSteps(targetNode);
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }
    
    private void animateDeleteSteps(Node targetNode) {
        if (opTimeline != null) {
            opTimeline.stop();
        }
        opTimeline = new Timeline();
        double speed = speedSlider != null ? speedSlider.getValue() : 1.0;
        int delayMs = (int)(1000 / speed);
        
        // Step 1: Highlight the node to be deleted
        KeyFrame step1 = new KeyFrame(Duration.millis(0), e -> {
            highlightSingleNode(targetNode);
            showMessage("Step 1: Node " + targetNode.val + " will be deleted");
        });
        
        opTimeline.getKeyFrames().add(step1);
        
        // Check if node has two children
        if (targetNode.left != null && targetNode.right != null) {
            // Step 2: Find successor
            Node successor = minNode(targetNode.right);
            List<Node> pathToSuccessor = new ArrayList<>();
            collectPathToSuccessor(targetNode.right, successor, pathToSuccessor);
            
            // Add path from target to successor
            pathToSuccessor.add(0, targetNode);
            
            // Step 2: Show path to successor
            for (int i = 0; i < pathToSuccessor.size(); i++) {
                Node node = pathToSuccessor.get(i);
                int stepIndex = i + 1;
                KeyFrame step = new KeyFrame(Duration.millis(stepIndex * delayMs), e -> {
                    highlightNodeInPath(node);
                    if (node == successor) {
                        showMessage("Step " + stepIndex + ": Found successor " + successor.val);
                    } else {
                        showMessage("Step " + stepIndex + ": Traversing to find successor");
                    }
                });
                opTimeline.getKeyFrames().add(step);
            }
            
            // Step 3: Replace value
            int finalStepIndex = pathToSuccessor.size() + 1;
            KeyFrame step3 = new KeyFrame(Duration.millis(finalStepIndex * delayMs), e -> {
                targetNode.val = successor.val;
                highlightSingleNode(targetNode);
                showMessage("Step " + finalStepIndex + ": Replaced value with successor " + successor.val);
            });
            opTimeline.getKeyFrames().add(step3);
            
            // Step 4: Delete the successor node
            int deleteStepIndex = finalStepIndex + 1;
            KeyFrame step4 = new KeyFrame(Duration.millis(deleteStepIndex * delayMs), e -> {
                // Remove successor from tree
                root = deleteSuccessorNode(root, successor.val);
                redraw();
                showMessage("Step " + deleteStepIndex + ": Removed successor node from tree");
            });
            opTimeline.getKeyFrames().add(step4);
            
        } else {
            // Node has 0 or 1 child - direct deletion
            int deleteStepIndex = 1;
            KeyFrame step2 = new KeyFrame(Duration.millis(deleteStepIndex * delayMs), e -> {
                root = deleteRec(root, targetNode.val);
                redraw();
                showMessage("Step " + deleteStepIndex + ": Node " + targetNode.val + " deleted");
            });
            opTimeline.getKeyFrames().add(step2);
        }
        
        opTimeline.setOnFinished(e -> {
            showMessage("Delete operation completed");
        });
        opTimeline.play();
    }
    
    private void collectPathToSuccessor(Node node, Node successor, List<Node> path) {
        if (node == null) return;
        path.add(node);
        if (node == successor) return;
        if (successor.val < node.val) {
            collectPathToSuccessor(node.left, successor, path);
        } else {
            collectPathToSuccessor(node.right, successor, path);
        }
    }
    
    private Node deleteSuccessorNode(Node node, int val) {
        if (node == null) return null;
        if (val < node.val) {
            node.left = deleteSuccessorNode(node.left, val);
        } else if (val > node.val) {
            node.right = deleteSuccessorNode(node.right, val);
        } else {
            // Node to delete found
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            // This shouldn't happen for successor node
            return node;
        }
        return node;
    }
    
    private void addPathToSuccessor(Node node, Node successor) {
        // Add path from node.right to successor (minimum of right subtree)
        if (node.right != null) {
            Node current = node.right;
            if (current != successor) {
                opPath.add(current);
                while (current.left != null && current != successor) {
                    current = current.left;
                    opPath.add(current);
                }
            }
        }
    }

    private void searchNode() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            opPath.clear();
            boolean found = collectPathForSearch(root, val);
            animateNodePath(opPath, found ? "Found " + val : "Not found");
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }

    private void findSuccessor() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            Node keyNode = findNode(root, val);
            if (keyNode == null) {
                showMessage("Node not found");
                return;
            }
            opPath.clear();
            opPath.add(keyNode);
            Node succ = findSuccessor(keyNode);
            if (succ != null) opPath.add(succ);
            animateNodePath(opPath, succ != null ? "Successor of " + val + " is " + succ.val : "No successor");
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }

    /* animation helpers and collect path methods */
    private void animateNodePath(List<Node> path, String endMsg) {
        animateNodePath(path, endMsg, null);
    }

    private void animateNodePath(List<Node> path, String endMsg, Runnable onFinish) {
        if (path.isEmpty()) {
            showMessage(endMsg);
            if (onFinish != null) onFinish.run();
            return;
        }
        if (opTimeline != null) {
            opTimeline.stop();
        }
        opTimeline = new Timeline();
        double speed = speedSlider != null ? speedSlider.getValue() : 1.0;
        int delayMs = (int)(1000 / speed);
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            int idx = i;
            KeyFrame frame = new KeyFrame(Duration.millis(i * delayMs + (i==0?200:0)), e -> {
                // highlight current node and show path
                highlightNodeInPath(node);
                if (idx == path.size() - 1) {
                    showMessage(endMsg);
                    if (onFinish != null) onFinish.run();
                }
            });
            opTimeline.getKeyFrames().add(frame);
        }
        opTimeline.play();
    }
    
    private void highlightNodeInPath(Node highlight) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawTreeHighlight(root, canvas.getWidth() / 2, 40, canvas.getWidth() / 4, highlight, opPath);
    }

private void highlightSingleNode(Node highlight) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawTreeHighlight(root, canvas.getWidth() / 2, 40, canvas.getWidth() / 4, highlight, opPath);
    }

    private void drawTreeHighlight(Node node, double x, double y, double offset, Node highlight, List<Node> pathList) {
        if (node == null) return;

        // Check if node is in the path
        Color nodeColor = Color.LIGHTBLUE;
        boolean isInPath = false;
        boolean isLastInPath = false;
        
        for (int i = 0; i < pathList.size(); i++) {
            if (pathList.get(i) == node) {
                isInPath = true;
                isLastInPath = (i == pathList.size() - 1);
                break;
            }
        }
        
        if (isInPath) {
            nodeColor = isLastInPath ? Color.ORANGE : Color.LIGHTGREEN;
        }
        
        // If this is the highlighted node, use ORANGE
        if (node == highlight) {
            nodeColor = Color.ORANGE;
        }
        
        gc.setFill(nodeColor);
        gc.fillOval(x - 20, y - 20, 40, 40);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - 20, y - 20, 40, 40);
        gc.setFill(Color.BLACK);
        gc.fillText(String.valueOf(node.val), x - 10, y + 5);

        double childY = y + 80;
        if (node.left != null) {
            double childX = x - offset;
            drawArrow(x - 20, y, childX, childY - 20);
            drawTreeHighlight(node.left, childX, childY, offset / 2, highlight, pathList);
        }
        if (node.right != null) {
            double childX = x + offset;
            drawArrow(x + 20, y, childX, childY - 20);
            drawTreeHighlight(node.right, childX, childY, offset / 2, highlight, pathList);
        }
    }

    // path collection methods
    private boolean collectPathForSearch(Node node, int key) {
        if (node == null) return false;
        opPath.add(node);
        if (key == node.val) return true;
        if (key < node.val) return collectPathForSearch(node.left, key);
        else return collectPathForSearch(node.right, key);
    }

    private void collectPathForInsert(Node node, int key) {
        if (node == null) return;
        opPath.add(node);
        if (key < node.val) collectPathForInsert(node.left, key);
        else if (key > node.val) collectPathForInsert(node.right, key);
    }

    private void collectPathForDelete(Node node, int key) {
        if (node == null) return;
        opPath.add(node);
        if (key < node.val) collectPathForDelete(node.left, key);
        else if (key > node.val) collectPathForDelete(node.right, key);
    }

    private void animateSuccessor() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            Node target = findNode(root, val);
            if (target == null) {
                showMessage("Node not found");
                return;
            }
            
            // Animate the search process step by step
            animateSuccessorSearch(val);
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }
    
    private void animateSuccessorSearch(int val) {
        if (opTimeline != null) {
            opTimeline.stop();
        }
        opTimeline = new Timeline();
        double speed = speedSlider != null ? speedSlider.getValue() : 1.0;
        int delayMs = (int)(1000 / speed);
        
        // Collect the search path
        List<Node> searchPath = new ArrayList<>();
        Node successor = findSuccessorWithPath(root, val, searchPath);
        
        if (searchPath.isEmpty()) {
            showMessage("No successor found");
            return;
        }
        
        // Step 1: Start from root
        KeyFrame startFrame = new KeyFrame(Duration.millis(0), e -> {
            highlightSingleNode(searchPath.get(0));
            showMessage("Step 1: Starting search from root");
        });
        opTimeline.getKeyFrames().add(startFrame);
        
        // Animate each step of the search
        for (int i = 1; i < searchPath.size(); i++) {
            Node currentNode = searchPath.get(i);
            int stepNum = i + 1;
            KeyFrame stepFrame = new KeyFrame(Duration.millis(i * delayMs), e -> {
                // Color visited nodes in light blue, current node in orange
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                drawTreeWithVisitedNodes(root, searchPath.subList(0, stepNum), currentNode, null);
                showMessage("Step " + stepNum + ": Visiting node " + currentNode.val);
            });
            opTimeline.getKeyFrames().add(stepFrame);
        }
        
        // Final step: Highlight only the successor
        int finalStep = searchPath.size() + 1;
        KeyFrame finalFrame = new KeyFrame(Duration.millis(finalStep * delayMs), e -> {
            highlightSingleNode(successor);
            showMessage("Successor of " + val + " is " + successor.val);
        });
        opTimeline.getKeyFrames().add(finalFrame);
        
        opTimeline.play();
    }
    
    private void animatePredecessor() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            Node target = findNode(root, val);
            if (target == null) {
                showMessage("Node not found");
                return;
            }
            
            // Animate the search process step by step
            animatePredecessorSearch(val);
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }
    
    private void animatePredecessorSearch(int val) {
        if (opTimeline != null) {
            opTimeline.stop();
        }
        opTimeline = new Timeline();
        double speed = speedSlider != null ? speedSlider.getValue() : 1.0;
        int delayMs = (int)(1000 / speed);
        
        // Collect the search path
        List<Node> searchPath = new ArrayList<>();
        Node predecessor = findPredecessorWithPath(root, val, searchPath);
        
        if (searchPath.isEmpty()) {
            showMessage("No predecessor found");
            return;
        }
        
        // Step 1: Start from root
        KeyFrame startFrame = new KeyFrame(Duration.millis(0), e -> {
            highlightSingleNode(searchPath.get(0));
            showMessage("Step 1: Starting search from root");
        });
        opTimeline.getKeyFrames().add(startFrame);
        
        // Animate each step of the search
        for (int i = 1; i < searchPath.size(); i++) {
            Node currentNode = searchPath.get(i);
            int stepNum = i + 1;
            KeyFrame stepFrame = new KeyFrame(Duration.millis(i * delayMs), e -> {
                // Color visited nodes in light blue, current node in orange
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                drawTreeWithVisitedNodes(root, searchPath.subList(0, stepNum), currentNode, null);
                showMessage("Step " + stepNum + ": Visiting node " + currentNode.val);
            });
            opTimeline.getKeyFrames().add(stepFrame);
        }
        
        // Final step: Highlight only the predecessor
        int finalStep = searchPath.size() + 1;
        KeyFrame finalFrame = new KeyFrame(Duration.millis(finalStep * delayMs), e -> {
            highlightSingleNode(predecessor);
            showMessage("Predecessor of " + val + " is " + predecessor.val);
        });
        opTimeline.getKeyFrames().add(finalFrame);
        
        opTimeline.play();
    }
    
    private void drawTreeWithVisitedNodes(Node node, List<Node> visitedNodes, Node currentNode, Node finalNode) {
        if (node == null) return;

        double x = canvas.getWidth() / 2;
        double y = 40;
        double offset = canvas.getWidth() / 4;
        drawTreeWithVisitedNodesHelper(node, x, y, offset, visitedNodes, currentNode, finalNode);
    }
    
    private void drawTreeWithVisitedNodesHelper(Node node, double x, double y, double offset, 
                                               List<Node> visitedNodes, Node currentNode, Node finalNode) {
        if (node == null) return;

        // Determine node color
        Color nodeColor = Color.LIGHTBLUE; // default
        
        if (visitedNodes.contains(node) && node != currentNode && node != finalNode) {
            nodeColor = Color.LIGHTGREEN; // visited nodes
        }
        if (node == currentNode) {
            nodeColor = Color.ORANGE; // current node being visited
        }
        if (node == finalNode) {
            nodeColor = Color.RED; // final successor/predecessor
        }
        
        // Draw the node
        gc.setFill(nodeColor);
        gc.fillOval(x - 20, y - 20, 40, 40);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - 20, y - 20, 40, 40);
        gc.setFill(Color.BLACK);
        gc.fillText(String.valueOf(node.val), x - 10, y + 5);

        double childY = y + 80;
        if (node.left != null) {
            double childX = x - offset;
            drawArrow(x - 20, y, childX, childY - 20);
            drawTreeWithVisitedNodesHelper(node.left, childX, childY, offset / 2, visitedNodes, currentNode, finalNode);
        }
        if (node.right != null) {
            double childX = x + offset;
            drawArrow(x + 20, y, childX, childY - 20);
            drawTreeWithVisitedNodesHelper(node.right, childX, childY, offset / 2, visitedNodes, currentNode, finalNode);
        }
    }
    
    private Node findSuccessorInAncestors(Node root, int val) {
        Node succ = null;
        Node current = root;
        while (current != null) {
            if (val < current.val) {
                succ = current;
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return succ;
    }
    
    private Node findPredecessorInAncestors(Node root, int val) {
        Node pred = null;
        Node current = root;
        while (current != null) {
            if (val > current.val) {
                pred = current;
                current = current.right;
            } else {
                current = current.left;
            }
        }
        return pred;
    }

    private void animateMinimum() {
        opPath.clear();
        Node cur = root;
        while (cur != null) {
            opPath.add(cur);
            cur = cur.left;
        }
        animateNodePath(opPath, cur == null ? "Minimum is " + (opPath.isEmpty()?"none":opPath.get(opPath.size()-1).val) : "");
    }

    private void animateMaximum() {
        opPath.clear();
        Node cur = root;
        while (cur != null) {
            opPath.add(cur);
            cur = cur.right;
        }
        animateNodePath(opPath, cur == null ? "Maximum is " + (opPath.isEmpty()?"none":opPath.get(opPath.size()-1).val) : "");
    }

    private void clearVisualization() {
        root = null;
        traversalSteps.clear();
        stepIndex = -1;
        redraw();
        if (traversalResult != null) traversalResult.clear();
        showMessage("Visualization cleared");
    }

    private void findPredecessor() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            Node keyNode = findNode(root, val);
            if (keyNode == null) {
                showMessage("Node not found");
                return;
            }
            opPath.clear();
            opPath.add(keyNode);
            Node pred = findPredecessor(keyNode);
            if (pred != null) opPath.add(pred);
            animateNodePath(opPath, pred != null ? "Predecessor of " + val + " is " + pred.val : "No predecessor");
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }

    private void generateRandomBST() {
        root = null;
        Random r = new Random();
        int n = 5 + r.nextInt(6);
        for (int i = 0; i < n; i++) insert(r.nextInt(100));
        redraw();
        showMessage("Random BST generated with " + n + " nodes.");
    }

    // Traversal step logic
    private void inorderSteps(Node node) {
        if (node == null) return;
        inorderSteps(node.left);
        traversalSteps.add(node);
        inorderSteps(node.right);
    }

    private void startInorderTraversal() {
        traversalSteps.clear();
        inorderSteps(root);
        stepIndex = 0;
        
        // Display traversal type
        if (traversalResult != null) {
            traversalResult.setText("In-Order Traversal: " + getTraversalString());
        }
        redraw();
        startTraversalAuto();
    }

    private void startPreorderTraversal() {
        traversalSteps.clear();
        preorderSteps(root);
        stepIndex = 0;
        
        if (traversalResult != null) {
            traversalResult.setText("Pre-Order Traversal: " + getTraversalString());
        }
        redraw();
        startTraversalAuto();
    }

    private void startPostorderTraversal() {
        traversalSteps.clear();
        postorderSteps(root);
        stepIndex = 0;
        
        if (traversalResult != null) {
            traversalResult.setText("Post-Order Traversal: " + getTraversalString());
        }
        redraw();
        startTraversalAuto();
    }

    private void startLevelorderTraversal() {
        traversalSteps.clear();
        levelorderSteps(root);
        stepIndex = 0;
        
        if (traversalResult != null) {
            traversalResult.setText("Level-Order Traversal: " + getTraversalString());
        }
        redraw();
        startTraversalAuto();
    }
    
    private String getTraversalString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < traversalSteps.size(); i++) {
            if (i > 0) sb.append(", ");
            if (i == stepIndex) {
                sb.append(">>> ").append(traversalSteps.get(i).val).append(" <<<");
            } else {
                sb.append(traversalSteps.get(i).val);
            }
        }
        sb.append(" ]");
        return sb.toString();
    }
    // Preorder traversal steps
    private void preorderSteps(Node node) {
        if (node == null) return;
        traversalSteps.add(node);
        preorderSteps(node.left);
        preorderSteps(node.right);
    }

    // Postorder traversal steps
    private void postorderSteps(Node node) {
        if (node == null) return;
        postorderSteps(node.left);
        postorderSteps(node.right);
        traversalSteps.add(node);
    }

    // Level order traversal steps
    private void levelorderSteps(Node node) {
        if (node == null) return;
        Queue<Node> q = new LinkedList<>();
        q.add(node);
        while (!q.isEmpty()) {
            Node cur = q.poll();
            traversalSteps.add(cur);
            if (cur.left != null) q.add(cur.left);
            if (cur.right != null) q.add(cur.right);
        }
    }


    // Helper methods to collect traversal results
    private void collectInorder(Node node, List<Integer> result) {
        if (node == null) return;
        collectInorder(node.left, result);
        result.add(node.val);
        collectInorder(node.right, result);
    }
    
    private void collectPreorder(Node node, List<Integer> result) {
        if (node == null) return;
        result.add(node.val);
        collectPreorder(node.left, result);
        collectPreorder(node.right, result);
    }
    
    private void collectPostorder(Node node, List<Integer> result) {
        if (node == null) return;
        collectPostorder(node.left, result);
        collectPostorder(node.right, result);
        result.add(node.val);
    }
    
    private void collectLevelorder(Node node, List<Integer> result) {
        if (node == null) return;
        Queue<Node> q = new LinkedList<>();
        q.add(node);
        while (!q.isEmpty()) {
            Node cur = q.poll();
            result.add(cur.val);
            if (cur.left != null) q.add(cur.left);
            if (cur.right != null) q.add(cur.right);
        }
    }

    private void nextStep() {
        if (stepIndex < traversalSteps.size() - 1) {
            stepIndex++;
            if (traversalResult != null) {
                traversalResult.setText(traversalResult.getText().split(":")[0] + ": " + getTraversalString());
            }
            redraw();
        }
    }

    private void prevStep() {
        if (stepIndex > 0) {
            stepIndex--;
            if (traversalResult != null) {
                traversalResult.setText(traversalResult.getText().split(":")[0] + ": " + getTraversalString());
            }
            redraw();
        }
    }

    // Helper methods
    private void insert(int val) {
        root = insertRec(root, val);
    }

    private Node insertRec(Node node, int val) {
        if (node == null) return new Node(val);
        if (val < node.val) node.left = insertRec(node.left, val);
        else if (val > node.val) node.right = insertRec(node.right, val);
        return node;
    }

    private Node deleteRec(Node node, int val) {
        if (node == null) return null;
        if (val < node.val) node.left = deleteRec(node.left, val);
        else if (val > node.val) node.right = deleteRec(node.right, val);
        else {
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            Node succ = minNode(node.right);
            node.val = succ.val;
            node.right = deleteRec(node.right, succ.val);
        }
        return node;
    }

//    private Node minNode(Node node) {
//        while (node.left != null) node = node.left;
//        return node;
//    }

    private boolean search(Node node, int val) {
        if (node == null) return false;
        if (val == node.val) return true;
        return val < node.val ? search(node.left, val) : search(node.right, val);
    }

    private Node successor(Node root, int key) {
        Node succ = null;
        while (root != null) {
            if (key < root.val) {
                succ = root;
                root = root.left;
            } else {
                root = root.right;
            }
        }
        return succ;
    }

    private Node successorWithPath(Node root, int key, List<Node> path) {
        Node succ = null;
        Node current = root;
        while (current != null) {
            path.add(current);
            if (key < current.val) {
                succ = current;
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return succ;
    }

    private Node predecessor(Node root, int key) {
        Node pred = null;
        while (root != null) {
            if (key > root.val) {
                pred = root;
                root = root.right;
            } else {
                root = root.left;
            }
        }
        return pred;
    }

    private Node predecessorWithPath(Node root, int key, List<Node> path) {
        Node pred = null;
        Node current = root;
        while (current != null) {
            path.add(current);
            if (key > current.val) {
                pred = current;
                current = current.right;
            } else {
                current = current.left;
            }
        }
        return pred;
    }

    private Node minNode(Node node) {
        while (node.left != null) node = node.left;
        return node;
    }

    private Node findSuccessorWithPath(Node root, int key, List<Node> path) {
        Node succ = null;
        Node current = root;
        while (current != null) {
            path.add(current);
            if (key < current.val) {
                succ = current;
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return succ;
    }

    private Node findPredecessorWithPath(Node root, int key, List<Node> path) {
        Node pred = null;
        Node current = root;
        while (current != null) {
            path.add(current);
            if (key > current.val) {
                pred = current;
                current = current.right;
            } else {
                current = current.left;
            }
        }
        return pred;
    }

    private Node findPredecessor(Node node) {
        if (node.left != null) {
            Node current = node.left;
            while (current.right != null) current = current.right;
            return current;
        }
        Node pred = null;
        Node current = root;
        while (current != null) {
            if (node.val > current.val) {
                pred = current;
                current = current.right;
            } else if (node.val < current.val) {
                current = current.left;
            } else {
                break;
            }
        }
        return pred;
    }

    private Node findSuccessor(Node node) {
        if (node.right != null) {
            return minNode(node.right);
        }
        Node succ = null;
        Node current = root;
        while (current != null) {
            if (node.val < current.val) {
                succ = current;
                current = current.left;
            } else if (node.val > current.val) {
                current = current.right;
            } else {
                break;
            }
        }
        return succ;
    }

    /*private Node findSuccessorWithPath(Node root, int val, List<Node> searchPath) {
        // First find the node with the given value and collect the search path
        Node target = findNodeWithPath(root, val, searchPath);
        if (target == null) return null;

        // Now find successor using the same logic as findSuccessor but collect path
        if (target.right != null) {
            Node current = target.right;
            searchPath.add(current);
            while (current.left != null) {
                current = current.left;
                searchPath.add(current);
            }
            return current;
        }

        // Find successor in ancestors
        Node succ = null;
        Node current = root;
        List<Node> ancestorPath = new ArrayList<>();
        while (current != null) {
            ancestorPath.add(current);
            if (val < current.val) {
                succ = current;
                current = current.left;
            } else if (val > current.val) {
                current = current.right;
            } else {
                break;
            }
        }
        // Add the ancestor path to search path (excluding already visited nodes)
        for (Node node : ancestorPath) {
            if (!searchPath.contains(node)) {
                searchPath.add(node);
            }
        }
        return succ;
    }

    private Node findPredecessorWithPath(Node root, int val, List<Node> searchPath) {
        // First find the node with the given value and collect the search path
        Node target = findNodeWithPath(root, val, searchPath);
        if (target == null) return null;

        // Now find predecessor using the same logic as findPredecessor but collect path
        if (target.left != null) {
            Node current = target.left;
            searchPath.add(current);
            while (current.right != null) {
                current = current.right;
                searchPath.add(current);
            }
            return current;
        }

        // Find predecessor in ancestors
        Node pred = null;
        Node current = root;
        List<Node> ancestorPath = new ArrayList<>();
        while (current != null) {
            ancestorPath.add(current);
            if (val > current.val) {
                pred = current;
                current = current.right;
            } else if (val < current.val) {
                current = current.left;
            } else {
                break;
            }
        }
        // Add the ancestor path to search path (excluding already visited nodes)
        for (Node node : ancestorPath) {
            if (!searchPath.contains(node)) {
                searchPath.add(node);
            }
        }
        return pred;
    }*/

    private Node findNodeWithPath(Node node, int val, List<Node> path) {
        if (node == null) return null;
        path.add(node);
        if (val == node.val) {
            return node;
        } else if (val < node.val) {
            return findNodeWithPath(node.left, val, path);
        } else {
            return findNodeWithPath(node.right, val, path);
        }
    }

    private int height(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    private int balanceFactor(Node node) {
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }

    private void redraw() {
        int treeHeight = height(root);
        double canvasHeight = Math.max(400, treeHeight * 100);
        double canvasWidth = Math.max(700, Math.pow(2, treeHeight) * 50); // nodes spread

        canvas.setWidth(canvasWidth);
        canvas.setHeight(canvasHeight);

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawTree(root, canvas.getWidth() / 2, 40, canvas.getWidth() / 4);

        if (root != null) {
            showMessage("Root height = " + treeHeight);
        }
    }

    // Helper method to draw an arrow from (x1,y1) to (x2,y2)
    private void drawArrow(double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);

        // Calculate angle of the line
        double angle = Math.atan2(y2 - y1, x2 - x1);

        // Arrowhead size and angle
        double arrowLength = 12;
        double arrowAngle = Math.toRadians(25);

        // Points for arrowhead
        double xArrow1 = x2 - arrowLength * Math.cos(angle - arrowAngle);
        double yArrow1 = y2 - arrowLength * Math.sin(angle - arrowAngle);

        double xArrow2 = x2 - arrowLength * Math.cos(angle + arrowAngle);
        double yArrow2 = y2 - arrowLength * Math.sin(angle + arrowAngle);

        gc.strokeLine(x2, y2, xArrow1, yArrow1);
        gc.strokeLine(x2, y2, xArrow2, yArrow2);
    }

    private void drawTree(Node node, double x, double y, double offset) {
        if (node == null) return;

        // Highlight current traversal step
        Color nodeColor = Color.LIGHTBLUE;
        if (stepIndex >= 0 && stepIndex < traversalSteps.size() && traversalSteps.get(stepIndex) == node) {
            nodeColor = Color.ORANGE; // highlight current node in orange
        }
        
        // Highlight all traversed nodes up to current step
        boolean isTraversed = false;
        if (stepIndex >= 0) {
            for (int i = 0; i <= stepIndex && i < traversalSteps.size(); i++) {
                if (traversalSteps.get(i) == node && i < stepIndex) {
                    nodeColor = Color.LIGHTGREEN; // already traversed
                    isTraversed = true;
                    break;
                }
            }
        }

        // Draw current node
        gc.setFill(nodeColor);
        gc.fillOval(x - 20, y - 20, 40, 40);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - 20, y - 20, 40, 40);
        gc.setFill(Color.BLACK);
        gc.fillText(String.valueOf(node.val), x - 10, y + 5);

        // Height and Balance Factor
        gc.setFill(Color.DARKGREEN);
        gc.setFont(javafx.scene.text.Font.font("Segue UI", 14));
        gc.fillText("h=" + height(node) + ", bf=" + balanceFactor(node), x - 25, y + 30);

        // Draw left child
        if (node.left != null) {
            double childX = x - offset;
            double childY = y + 80;
            drawArrow(x-20, y , childX, childY - 20);
            drawTree(node.left, childX, childY, offset / 2);
        }

        // Draw right child
        if (node.right != null) {
            double childX = x + offset;
            double childY = y + 80;
            drawArrow(x+20, y , childX, childY - 20);
            drawTree(node.right, childX, childY, offset / 2);
        }
    }
}

