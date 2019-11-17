package com.energyxxer.trident.ui.dialogs.settings;

import com.energyxxer.trident.ui.orderlist.ItemAction;
import com.energyxxer.trident.ui.orderlist.ItemActionHost;

import java.awt.*;

public abstract class ItemKeybindsAction implements ItemAction {
    @Override
    public void render(Graphics g, ItemActionHost host, int x, int y, int w, int h, int mouseState, boolean actionEnabled) {

    }

    @Override
    public boolean intersects(Point p, int w, int h) {
        return false;
    }

    @Override
    public int getRenderedWidth() {
        return 0;
    }

    @Override
    public boolean isLeftAligned() {
        return false;
    }

    @Override
    public String getHint() {
        return null;
    }

    @Override
    public int getHintOffset() {
        return 0;
    }
}
