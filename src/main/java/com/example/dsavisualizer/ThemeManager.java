package com.example.dsavisualizer;

import javafx.scene.Scene;

public class ThemeManager {

    public static void applyLight(Scene scene) {
        scene.getRoot().getStyleClass().remove("dark-mode");
        if (!scene.getRoot().getStyleClass().contains("light-mode"))
            scene.getRoot().getStyleClass().add("light-mode");
    }

    public static void applyDark(Scene scene) {
        scene.getRoot().getStyleClass().remove("light-mode");
        if (!scene.getRoot().getStyleClass().contains("dark-mode"))
            scene.getRoot().getStyleClass().add("dark-mode");
    }
}
