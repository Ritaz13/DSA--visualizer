package com.example.dsavisualizer;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ModuleWindow {

    public static void open(String moduleName) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20;");

        TextArea description = new TextArea();
        description.setWrapText(true);
        description.setEditable(false);

        // Example description per module
        switch (moduleName) {
            case "Recursion":
                description.setText("Recursion is ... [add details]");
                break;
            case "Array":
                description.setText("Array is ...");
                break;
            case "Graph":
                description.setText("Graph is ...");
                break;
            case "BST":
                description.setText("Binary Search Tree ...");
                break;
            case "Linked List":
                description.setText("Linked List ...");
                break;
            case "Stack":
                description.setText("Stack ...");
                break;
            case "Queue":
                description.setText("Queue ...");
                break;
            case "Sorting":
                description.setText("Sorting algorithms ...");
                break;
            default:
                description.setText("No description available.");
        }

        root.getChildren().add(description);

        Scene scene = new Scene(root, 400, 300);
        stage.setTitle(moduleName);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
