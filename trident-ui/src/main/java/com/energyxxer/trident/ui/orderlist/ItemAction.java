package com.energyxxer.trident.ui.orderlist;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface ItemAction {
    void render(Graphics g, ItemActionHost host, int x, int y, int w, int h, int mouseState, boolean actionEnabled);
    boolean intersects(Point p, int w, int h);

    int getRenderedWidth();
    boolean isLeftAligned();

    String getHint();
    int getHintOffset();

    default int getActionCode() {
        return -1;
    }

    default void mouseClicked(MouseEvent e, ItemActionHost host) {

    }

    default void mousePressed(MouseEvent e, ItemActionHost host) {

    }

    default void mouseReleased(MouseEvent e, ItemActionHost host) {

    }
}
