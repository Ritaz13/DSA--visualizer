package com.example.dsavisualizer.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.example.dsavisualizer.manager.SceneManager;
import com.example.dsavisualizer.manager.ThemeManager;

public class ModuleController {

    @FXML protected javafx.scene.Node moduleRoot;

    @FXML protected Label titleLabel;
    @FXML protected TextArea descArea;
    @FXML protected TextArea storyArea;
    @FXML protected VBox codeBox;
    @FXML protected TextArea codeArea;
    @FXML protected Button themeBtn;
    @FXML protected Button showCodeBtn;
    @FXML protected Button pushBtn;
    @FXML protected Button popBtn;
    @FXML protected Button backBtn;

    @FXML protected VBox moduleControls;

    @FXML
    protected void initialize() {
        // If this controller is used via inclusion, moduleRoot (the fx:include) will be injected.
        if (moduleRoot != null) {
            // lookup inner nodes from the included fragment and assign to fields if found
            Label t = (Label) moduleRoot.lookup("#titleLabel");
            if (t != null) titleLabel = t;

            TextArea d = (TextArea) moduleRoot.lookup("#descArea");
            if (d != null) descArea = d;

            TextArea s = (TextArea) moduleRoot.lookup("#storyArea");
            if (s != null) storyArea = s;

            VBox cb = (VBox) moduleRoot.lookup("#codeBox");
            if (cb != null) codeBox = cb;

            TextArea ca = (TextArea) moduleRoot.lookup("#codeArea");
            if (ca != null) codeArea = ca;

            Button th = (Button) moduleRoot.lookup("#themeBtn");
            if (th != null) themeBtn = th;

            Button sc = (Button) moduleRoot.lookup("#showCodeBtn");
            if (sc != null) showCodeBtn = sc;

            Button pb = (Button) moduleRoot.lookup("#pushBtn");
            if (pb != null) pushBtn = pb;

            Button popB = (Button) moduleRoot.lookup("#popBtn");
            if (popB != null) popBtn = popB;

            HBox v = (HBox) moduleRoot.lookup("#vizArea");
            if (v != null) vizArea = v;

            TextField pf = (TextField) moduleRoot.lookup("#pushField");
            if (pf != null) pushField = pf;

            VBox mc = (VBox) moduleRoot.lookup("#moduleControls");
            if (mc != null) moduleControls = mc;

            Button back = (Button) moduleRoot.lookup("#backBtn");
            if (back != null) backBtn = back;
        }

        // keep theme toggle label in sync when a module is shown
        if (themeBtn != null) themeBtn.setText(ThemeManager.isDark() ? "Light Mode" : "Dark Mode");

        // wire handlers here (included FXML no longer contains onAction)
        if (themeBtn != null) themeBtn.setOnAction(e -> toggleTheme());
        if (showCodeBtn != null) showCodeBtn.setOnAction(e -> toggleCode());
        if (pushBtn != null) pushBtn.setOnAction(e -> push());
        if (popBtn != null) popBtn.setOnAction(e -> pop());
        if (backBtn != null) backBtn.setOnAction(e -> SceneManager.switchScene("home.fxml"));
    }

    // Optional visualization fields available to modules
    @FXML protected HBox vizArea;
    @FXML protected TextField pushField;

    protected void setContent(String title, String desc, String story) {
        titleLabel.setText(title);
        descArea.setText(desc);
        storyArea.setText(story);
    }

    @FXML
    protected void toggleCode() {
        codeBox.setVisible(!codeBox.isVisible());
    }

    // Default no-op push/pop so modules without them won't fail
    @FXML
    protected void push() {}

    @FXML
    protected void pop() {}

    @FXML
    protected void toggleTheme() {
        Scene scene = themeBtn.getScene();
        ThemeManager.toggleTheme(scene);
        themeBtn.setText(
                ThemeManager.isDark() ? "Light Mode" : "Dark Mode"
        );
    }
}
