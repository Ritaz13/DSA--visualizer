package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class LinkedListController extends ModuleController {

    @FXML
    private HBox vizArea;

    @FXML
    private javafx.scene.control.TextField pushField;

    public void initialize() {
        super.initialize();

        setContent(
                "Linked List",
                "LinkedList consists of nodes with next pointers.",
                "Good for frequent insertions/deletions. Each node holds a value and points to the next node."
        );

        // sample code
        codeArea.setText("// Simple LinkedList using nodes\nclass Node {\n    int data;\n    Node next;\n    Node(int data) { this.data = data; }\n}\n\nclass LinkedList {\n    Node head;\n    \n    void insert(int data) {\n        Node newNode = new Node(data);\n        if (head == null) head = newNode;\n        else {\n            Node temp = head;\n            while (temp.next != null) temp = temp.next;\n            temp.next = newNode;\n        }\n    }\n}");
    }

    @FXML
    protected void push() {
        if (pushField == null || pushField.getText().trim().isEmpty()) return;
        String val = pushField.getText().trim();

        Label lbl = new Label(val);
        lbl.setStyle("-fx-border-color: #333; -fx-padding: 10; -fx-background-color: lightblue;");
        lbl.setFont(Font.font(14));

        // add to the end (simulating insertion)
        vizArea.getChildren().add(lbl);

        // add arrow if not the first element
        if (vizArea.getChildren().size() > 1) {
            Label arrow = new Label("→");
            vizArea.getChildren().add(vizArea.getChildren().size() - 1, arrow);
        }

        pushField.clear();
    }

    @FXML
    protected void pop() {
        if (vizArea == null || vizArea.getChildren().isEmpty()) return;

        // remove the last child (node)
        vizArea.getChildren().remove(vizArea.getChildren().size() - 1);

        // remove arrow before it if exists
        if (!vizArea.getChildren().isEmpty() && vizArea.getChildren().get(vizArea.getChildren().size() - 1).toString().contains("Label")) {
            Label last = (Label) vizArea.getChildren().get(vizArea.getChildren().size() - 1);
            if (last.getText().equals("→")) {
                vizArea.getChildren().remove(vizArea.getChildren().size() - 1);
            }
        }
    }
}
