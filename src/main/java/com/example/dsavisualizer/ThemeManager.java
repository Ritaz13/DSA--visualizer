package com.example.dsavisualizer;

import javafx.scene.Scene;

public class ThemeManager {

    private static final String LIGHT =
            ThemeManager.class.getResource("/css/light.css").toExternalForm();

    private static final String DARK =
            ThemeManager.class.getResource("/css/dark.css").toExternalForm();

    public static void applyLight(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(LIGHT);
    }

    public static void applyDark(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(DARK);
    }
}
