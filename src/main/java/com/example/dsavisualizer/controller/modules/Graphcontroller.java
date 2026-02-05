package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;

public class Graphcontroller extends ModuleController {

    public void initialize() {
        super.initialize();

        setContent(
                "Graph",
                "Graph is a set of vertices connected by edges.",
                "Used to model networks and relationships."
        );

        if (pushBtn != null) pushBtn.setVisible(false);
        if (popBtn != null) popBtn.setVisible(false);
        if (pushField != null) pushField.setVisible(false);
    }
}
