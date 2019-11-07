package com.energyxxer.trident.ui.orderlist;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface OrderListAction {
    void render(Graphics g, OrderListMaster master, int x, int y, int w, int h, int mouseState, boolean actionEnabled);
    boolean intersects(Point p, int w, int h);

    int getRenderedWidth();
    boolean isLeftAligned();

    String getHint();
    int getHintOffset();

    default int getActionCode() {
        return -1;
    }

    default void mouseClicked(MouseEvent e, StandardOrderListItem item) {

    }

    default void mousePressed(MouseEvent e, StandardOrderListItem item) {

    }

    default void mouseReleased(MouseEvent e, StandardOrderListItem item) {

    }
}
