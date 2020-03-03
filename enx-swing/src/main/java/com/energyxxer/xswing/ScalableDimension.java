package com.energyxxer.xswing;

import java.awt.*;
import java.awt.event.MouseEvent;

public class ScalableDimension extends Dimension {
    public ScalableDimension() {
    }

    public ScalableDimension(Dimension d) {
        super((int)Math.ceil(d.width * (d instanceof ScalableDimension ? 1 : ScalableGraphics2D.SCALE_FACTOR)), (int)Math.ceil(d.height * (d instanceof ScalableDimension ? 1 : ScalableGraphics2D.SCALE_FACTOR)));
    }

    public ScalableDimension(int width, int height) {
        super((int)Math.ceil(width * ScalableGraphics2D.SCALE_FACTOR), (int)Math.ceil(height * ScalableGraphics2D.SCALE_FACTOR));
    }

    public static MouseEvent descaleEvent(MouseEvent e) {
        return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)Math.round(e.getX() / ScalableGraphics2D.SCALE_FACTOR), (int)Math.round(e.getY() / ScalableGraphics2D.SCALE_FACTOR), e.getClickCount(), e.isPopupTrigger(), e.getButton());
    }
}
