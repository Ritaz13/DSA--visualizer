package com.example.dsavisualizer.controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    @FXML protected Button popBtn;
    @FXML protected Button backBtn;
    @FXML protected Label statusLabel;

    @FXML protected VBox moduleControls;

    @FXML
    protected void initialize() {

        if (moduleRoot != null) {

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

            Button popB = (Button) moduleRoot.lookup("#popBtn");
            if (popB != null) popBtn = popB;

            VBox mc = (VBox) moduleRoot.lookup("#moduleControls");
            if (mc != null) moduleControls = mc;

            Button back = (Button) moduleRoot.lookup("#backBtn");
            if (back != null) backBtn = back;
        }

        if (themeBtn != null) themeBtn.setText(ThemeManager.isDark() ? "Light Mode" : "Dark Mode");


        if (themeBtn != null) themeBtn.setOnAction(e -> toggleTheme());
        if (showCodeBtn != null) showCodeBtn.setOnAction(e -> toggleCode());
        if (backBtn != null) backBtn.setOnAction(e-> SceneManager.switchScene("home.fxml"));
    }


    /*protected void setContent(String title, String desc, String story) {
        titleLabel.setText(title);
        descArea.setText(desc);
        storyArea.setText(story);
    }*/

    @FXML
    protected void toggleCode() {
        codeBox.setVisible(!codeBox.isVisible());
    }



    @FXML
    protected void toggleTheme() {
        Scene scene = themeBtn.getScene();
        ThemeManager.toggleTheme(scene);
        themeBtn.setText(
                ThemeManager.isDark() ? "Light Mode" : "Dark Mode"
        );
    }


    protected void showAlert(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: dark blue; -fx-font-size: 14;");
        }
    }
}
