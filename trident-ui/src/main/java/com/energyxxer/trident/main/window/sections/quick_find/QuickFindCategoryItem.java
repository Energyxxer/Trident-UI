package com.energyxxer.trident.main.window.sections.quick_find;

import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;
import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.modules.ModuleToken;

import java.awt.*;
import java.awt.event.MouseEvent;

public class QuickFindCategoryItem extends ExplorerElement {
    private String label;

    int maxShown = 20;
    int limit = 100;

    private MoreItem moreButton = new MoreItem(this, "more", 20);
    private MoreItem backButton = new MoreItem(this, "back", -20);

    public QuickFindCategoryItem(ExplorerMaster master, String label) {
        super(master);
        this.label = label;
    }

    void addElement(ExplorerElement element) {
        children.add(element);
    }

    void clear() {
        children.clear();
        maxShown = 20;
    }

    @Override
    public void render(Graphics g) {
        if(children.isEmpty()) return;
        int y = master.getOffsetY();
        master.addToFlatList(this);

        int x = master.getInitialIndent();

        //File Name

        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColors().get("item.foreground"));
        }

        Font originalFont = g.getFont();

        g.setFont(g.getFont().deriveFont(Font.BOLD));

        FontMetrics metrics = g.getFontMetrics(g.getFont());

        g.drawString(label, x, master.getOffsetY() + metrics.getAscent() + ((master.getRowHeight() - metrics.getHeight())/2));
        x += metrics.stringWidth(label);
        g.fillRect(x + 8, master.getOffsetY() + metrics.getAscent(), master.getWidth()-x-8-16, 1);

        if(master.getFlag(ExplorerFlag.DEBUG_WIDTH)) {
            g.setColor(Color.YELLOW);
            g.fillRect(master.getContentWidth()-2, master.getOffsetY(), 2, master.getRowHeight());
            g.setColor(Color.GREEN);
            g.fillRect(x-2, master.getOffsetY(), 2, master.getRowHeight());
        }

        g.setFont(originalFont);

        master.setContentWidth(Math.max(master.getContentWidth(), x));
        master.renderOffset(this.getHeight());
        int i = Math.max(0, maxShown - limit);
        if(i > 0) {
            backButton.setShown(-1);
            backButton.render(g);
        }
        for(; i < children.size() && i < maxShown; i++) {
            children.get(i).render(g);
            if(i >= maxShown -1) {
                moreButton.setShown(i+1);
                moreButton.render(g);
                break;
            }
        }
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

    public int getTotal() {
        return children.size();
    }
}
