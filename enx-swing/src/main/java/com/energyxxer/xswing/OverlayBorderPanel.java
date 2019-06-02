package com.energyxxer.xswing;

import javax.swing.*;
import java.awt.*;

public class OverlayBorderPanel extends JPanel implements OverlayBorderComponent {
    private Insets insets;

    public OverlayBorderPanel(Insets insets) {
        this.insets = insets;
    }

    public OverlayBorderPanel(LayoutManager layout, Insets insets) {
        super(layout);
        this.insets = insets;
    }

    @Override
    public Insets getOverlayDimensions() {
        return insets;
    }
}
