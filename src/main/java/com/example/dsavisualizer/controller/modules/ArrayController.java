package com.example.dsavisualizer.controller.modules;

import javafx.fxml.FXML;
import com.example.dsavisualizer.controller.ModuleController;

public class ArrayController extends ModuleController {

    public void initialize() {
        super.initialize();

        setContent(
                "Array",
                "Array stores elements in contiguous memory.",
                "Think of boxes side by side for each element."
        );

        // this module uses custom insert/delete/search controls, so hide generic push/pop
        if (pushBtn != null) pushBtn.setVisible(false);
        if (popBtn != null) popBtn.setVisible(false);
        if (pushField != null) pushField.setVisible(false);
    }
}
