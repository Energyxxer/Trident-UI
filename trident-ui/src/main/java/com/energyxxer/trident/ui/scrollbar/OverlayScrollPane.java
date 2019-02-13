package com.energyxxer.trident.ui.scrollbar;

import javax.swing.*;
import java.awt.*;

public class OverlayScrollPane extends JScrollPane {

    public OverlayScrollPane(Component view) {
        super(view);
        setup();
    }

    public OverlayScrollPane() {
        setup();
    }

    private void setup() {
        this.setLayout(new OverlayScrollPaneLayout(this));
        this.setBorder(BorderFactory.createEmptyBorder());
    }
}
