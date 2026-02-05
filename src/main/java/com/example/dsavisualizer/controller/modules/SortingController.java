package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;

public class SortingController extends ModuleController {

    public void initialize() {
        super.initialize();

        setContent(
                "Sorting",
                "Sorting rearranges elements into order (ascending/descending).",
                "Common algorithms: bubble, quick, merge."
        );

        if (pushBtn != null) pushBtn.setVisible(false);
        if (popBtn != null) popBtn.setVisible(false);
        if (pushField != null) pushField.setVisible(false);
    }
}
