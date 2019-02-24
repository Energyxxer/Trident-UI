package com.energyxxer.trident.main.window.sections.quick_find;

import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.modules.ModuleToken;

import java.awt.*;
import java.awt.event.MouseEvent;

public class MoreItem extends ExplorerElement {
    private QuickFindCategoryItem parent;
    private int shown;

    private String label;
    private int delta;

    public MoreItem(QuickFindCategoryItem parent, String label, int delta) {
        super(parent.getMaster());
        this.parent = parent;
        this.label = label;
        this.delta = delta;
    }

    @Override
    public void render(Graphics g) {
        master.addToFlatList(this);

        int x = master.getInitialIndent();

        x += 24;

        //File Name

        if(this.selected) {
            g.setColor(master.getColorMap().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColorMap().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColorMap().get("item.foreground"));
        }

        Font originalFont = g.getFont();

        g.setFont(g.getFont().deriveFont(Font.BOLD));

        FontMetrics metrics = g.getFontMetrics(g.getFont());
        String label = "... " + this.label;
        if(shown >= 0) {
            label += " (" + shown + "/" + parent.getTotal() + ")";
        } else {
            label += " (showing a maximum of 100 results at once)";
        }

        g.drawString(label, x, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
        x += metrics.stringWidth(label);

        if(master.getFlag(ExplorerFlag.DEBUG_WIDTH)) {
            g.setColor(Color.YELLOW);
            g.fillRect(master.getContentWidth()-2, master.getOffsetY(), 2, master.getRowHeight());
            g.setColor(Color.GREEN);
            g.fillRect(x-2, master.getOffsetY(), 2, master.getRowHeight());
        }

        g.setFont(originalFont);

        master.setOffsetY(master.getOffsetY() + master.getRowHeight());
        master.setContentWidth(Math.max(master.getContentWidth(), x));
    }

    @Override
    public ModuleToken getToken() {
        return null;
    }

    @Override
    public int getHeight() {
        return master.getRowHeight();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        parent.maxShown += delta;
        master.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public void setShown(int shown) {
        this.shown = shown;
    }

    public int getShown() {
        return shown;
    }
}
