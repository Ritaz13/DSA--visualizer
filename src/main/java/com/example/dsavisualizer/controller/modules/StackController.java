package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class StackController extends ModuleController {

    @FXML
    private HBox vizArea;

    @FXML
    private javafx.scene.control.TextField pushField;

    public void initialize() {
        super.initialize();

        setContent(
                "Stack",
                "Stack is a LIFO (Last In First Out) data structure.\nUse push to add an item and pop to remove the top item.",
                "A stack is like a stack of plates: you put on top and take from the top."
        );

        // sample code
        codeArea.setText("// Simple stack using an array\nclass Stack {\n    int[] a = new int[100];\n    int top = -1;\n\n    void push(int v) { a[++top] = v; }\n    int pop() { return a[top--]; }\n}");
    }

    @FXML
    protected void push() {
        if (pushField == null || pushField.getText().trim().isEmpty()) return;
        String val = pushField.getText().trim();

        Label lbl = new Label(val);
        lbl.setStyle("-fx-border-color: #333; -fx-padding: 10; -fx-background-color: -fx-control-inner-background;");
        lbl.setFont(Font.font(14));

        // add as first child so it appears as the top
        vizArea.getChildren().add(0, lbl);

        pushField.clear();
    }

    @FXML
    protected void pop() {
        if (vizArea == null || vizArea.getChildren().isEmpty()) return;

        // remove the top (first child)
        vizArea.getChildren().remove(0);
    }
}

