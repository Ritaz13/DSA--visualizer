package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

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
    private Button nextBtn, prevBtn; // for step traversal
    @FXML
    private StackPane vizArea;
    @FXML
    private TextArea codeArea, storyArea;
    @FXML
    private Button showCodeBtn, copyCodeBtn;
    @FXML
    private Label statusLabel; // bottom message
    @FXML
    private TextArea inOrderResult, preOrderResult, postOrderResult, levelOrderResult;

    private Node root;
    private List<Node> traversalSteps = new ArrayList<>();
    private int stepIndex = -1;
    private Timer stepTimer;
    private boolean isRunning = false;
    @FXML
    private Button startBtn, stopBtn;

    private void startTraversalAuto() {
        if (traversalSteps.isEmpty()) return;
        if (isRunning) return;
        isRunning = true;
        startBtn.setText("⏸ Pause");
        stepTimer = new Timer();
        stepTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (stepIndex < traversalSteps.size() - 1) {
                        stepIndex++;
                        redraw();
                    } else {
                        stopTraversalAuto();
                    }
                });
            }
        }, 0, 1000); // every 1 second
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

    // Utility
    private void showMessage(String msg) {
        statusLabel.setText(msg);
    }

    // Build BST
    private void buildBST() {
        root = null;
        String[] parts = inputField.getText().split(",");
        for (String p : parts) {
            try {
                insert(Integer.parseInt(p.trim()));
            } catch (Exception ignored) {
            }
        }
        redraw();
        showMessage("BST built with " + parts.length + " nodes.");
    }

    private void insertNode() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            insert(val);
            redraw();
            showMessage("Inserted " + val);
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }

    private void deleteNode() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            root = deleteRec(root, val);
            redraw();
            showMessage("Deleted " + val);
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }

    private void searchNode() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            boolean found = search(root, val);
            showMessage(found ? "Found " + val : "Not found");
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }

    private void findSuccessor() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            Node succ = successor(root, val);
            showMessage(succ != null ? "Successor of " + val + " is " + succ.val : "No successor");
        } catch (Exception e) {
            showMessage("Invalid input");
        }
    }

    private void findPredecessor() {
        try {
            int val = Integer.parseInt(opField.getText().trim());
            Node pred = predecessor(root, val);
            showMessage(pred != null ? "Predecessor of " + val + " is " + pred.val : "No predecessor");
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
        
        // Display full result
        List<Integer> result = new ArrayList<>();
        collectInorder(root, result);
        if (inOrderResult != null) {
            inOrderResult.setText(result.isEmpty() ? "Empty tree" : result.toString().replaceAll("[\\[\\]]", ""));
        }
        redraw();
        startTraversalAuto();
    }

    private void startPreorderTraversal() {
        traversalSteps.clear();
        preorderSteps(root);
        stepIndex = 0;
        
        // Display full result
        List<Integer> result = new ArrayList<>();
        collectPreorder(root, result);
        if (preOrderResult != null) {
            preOrderResult.setText(result.isEmpty() ? "Empty tree" : result.toString().replaceAll("[\\[\\]]", ""));
        }
        redraw();
        startTraversalAuto();
    }

    private void startPostorderTraversal() {
        traversalSteps.clear();
        postorderSteps(root);
        stepIndex = 0;
        
        // Display full result
        List<Integer> result = new ArrayList<>();
        collectPostorder(root, result);
        if (postOrderResult != null) {
            postOrderResult.setText(result.isEmpty() ? "Empty tree" : result.toString().replaceAll("[\\[\\]]", ""));
        }
        redraw();
        startTraversalAuto();
    }

    private void startLevelorderTraversal() {
        traversalSteps.clear();
        levelorderSteps(root);
        stepIndex = 0;
        
        // Display full result
        List<Integer> result = new ArrayList<>();
        collectLevelorder(root, result);
        if (levelOrderResult != null) {
            levelOrderResult.setText(result.isEmpty() ? "Empty tree" : result.toString().replaceAll("[\\[\\]]", ""));
        }
        redraw();
        startTraversalAuto();
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
            redraw();
        }
    }

    private void prevStep() {
        if (stepIndex > 0) {
            stepIndex--;
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

    private Node minNode(Node node) {
        while (node.left != null) node = node.left;
        return node;
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
            nodeColor = Color.YELLOW; // highlight current node
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
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", 14));
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

