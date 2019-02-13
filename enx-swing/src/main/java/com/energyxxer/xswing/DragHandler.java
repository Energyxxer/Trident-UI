package com.energyxxer.xswing;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DragHandler extends MouseAdapter {
    private Point start = new Point(0, 0);
    private final Component frame;

    public DragHandler(Component frame) {
        this.frame = frame;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        start = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point point = e.getLocationOnScreen();
        frame.setLocation(point.x -start.x, point.y - start.y);
    }
}
