package com.energyxxer.trident.ui.scrollbar;

import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;

public class OverlayScrollPane extends JScrollPane {

    private final ThemeListenerManager tlm;

    public OverlayScrollPane(ThemeListenerManager tlm, Component view) {
        super(view);
        this.tlm = tlm;
        setup();
    }

    public OverlayScrollPane(ThemeListenerManager tlm) {
        this.tlm = tlm;
        setup();
    }

    private void setup() {
        this.setLayout(new OverlayScrollPaneLayout(this, tlm));
        this.setBorder(BorderFactory.createEmptyBorder());
    }
}
