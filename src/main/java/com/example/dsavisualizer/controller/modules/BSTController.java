package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class BSTController extends ModuleController {

    @FXML
    private HBox vizArea;

    @FXML
    private javafx.scene.control.TextField pushField;

    private BST bst;

    public void initialize() {
        super.initialize();

        setContent(
                "Binary Search Tree",
                "BST is a tree data structure where left < root < right.",
                "Used for sorted data and fast lookups."
        );

        bst = new BST();

        // sample code
        codeArea.setText("// Simple BST implementation\nclass Node {\n    int val;\n    Node left, right;\n    Node(int val) { this.val = val; }\n}\n\nclass BST {\n    Node root;\n    void insert(int val) {\n        root = insertHelper(root, val);\n    }\n    private Node insertHelper(Node node, int val) {\n        if (node == null) return new Node(val);\n        if (val < node.val) node.left = insertHelper(node.left, val);\n        else node.right = insertHelper(node.right, val);\n        return node;\n    }\n}");
    }

    @FXML
    protected void push() {
        if (pushField == null || pushField.getText().trim().isEmpty()) return;
        String val = pushField.getText().trim();

        try {
            int num = Integer.parseInt(val);
            bst.insert(num);
            redraw();
            pushField.clear();
        } catch (NumberFormatException e) {
            // ignore non-numeric input
        }
    }

    @FXML
    protected void pop() {
        if (bst.root == null) return;
        // For demo, remove the last inserted node (would need more complex logic for true deletion)
        bst = new BST();
        redraw();
    }

    private void redraw() {
        vizArea.getChildren().clear();
        if (bst.root != null) {
            drawNode(bst.root, vizArea);
        }
    }

    private void drawNode(BST.Node node, HBox parent) {
        if (node == null) return;

        VBox nodeView = new VBox();
        Label lbl = new Label(String.valueOf(node.val));
        lbl.setStyle("-fx-border-color: #333; -fx-padding: 8; -fx-background-color: lightgreen; -fx-border-radius: 5; -fx-background-radius: 5;");
        lbl.setFont(Font.font(12));
        nodeView.getChildren().add(lbl);

        parent.getChildren().add(nodeView);
    }

    // Simple BST implementation
    private static class BST {
        Node root;

        void insert(int val) {
            root = insertHelper(root, val);
        }

        private Node insertHelper(Node node, int val) {
            if (node == null) return new Node(val);
            if (val < node.val) node.left = insertHelper(node.left, val);
            else node.right = insertHelper(node.right, val);
            return node;
        }

        static class Node {
            int val;
            Node left, right;

            Node(int val) {
                this.val = val;
            }
        }
    }
}
