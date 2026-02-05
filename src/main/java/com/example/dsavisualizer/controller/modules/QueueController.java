package com.example.dsavisualizer.controller.modules;

import com.example.dsavisualizer.controller.ModuleController;

public class QueueController extends ModuleController {

    public void initialize() {
        super.initialize();

        setContent(
                "Queue",
                "Queue is FIFO (First In First Out).",
                "Enqueue at rear, dequeue from front."
        );

        if (pushBtn != null) pushBtn.setVisible(false);
        if (popBtn != null) popBtn.setVisible(false);
        if (pushField != null) pushField.setVisible(false);
    }
}
