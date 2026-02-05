package com.example.dsavisualizer.manager;

import javafx.scene.Scene;

public class ThemeManager {

    private static boolean dark = false;

    public static void toggleTheme(Scene scene) {
        dark = !dark;
        applyTheme(scene);
    }

    public static void applyTheme(Scene scene) {

        scene.getStylesheets().clear();

        String css = dark ? "/css/dark.css" : "/css/light.css";

        scene.getStylesheets().add(
                ThemeManager.class.getResource(css).toExternalForm()
        );
    }

    public static boolean isDark() {
        return dark;
    }
}
