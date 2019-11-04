package com.energyxxer.trident.ui.orderlist;

import com.energyxxer.trident.ui.theme.Theme;
import com.energyxxer.trident.util.ImageUtil;
import com.energyxxer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;


public class StandardOrderListItem extends OrderListElement {
    @NotNull
    private final OrderListToken token;

    private Image icon = null;
    private String name = null;

    private int y = 0;
    private int height = 20;

    private int actionRolloverIndex = -1;
    private Point toolTipLocation = new Point();

    private List<OrderListAction> actions;

    public StandardOrderListItem(OrderListMaster master, @NotNull OrderListToken token) {
        super(master);
        this.token = token;
        this.updateName();
        this.updateIcon();
        this.actions = token.getActions();
    }

    private void updateName() {
        this.name = token.getTitle();
        if(this.name != null) this.name = StringUtil.ellipsis(this.name,100);
    }

    private void updateIcon() {
        this.icon = token.getIcon();
        if(this.icon != null) {
            this.icon = ImageUtil.fitToSize(this.icon, 16, 16);
        }
    }

    @Override
    public void render(Graphics g) {
        g.setFont(master.getFont());
        FontMetrics fm = g.getFontMetrics();

        int x = 0;
        this.y = master.getOffsetY();
        this.lastRecordedOffset = y;
        int w = master.getWidth();
        int h = master.getRowHeight();

        this.height = h;
        toolTipLocation.y = (int) (height * 0.65);

        int offsetY = y;
        if(master.draggedElement == this) offsetY = (int) (master.dragPoint.y - (h * master.dragPivot));

        g.setColor((this.rollover || this.selected) ? master.getColors().get("item.rollover.background") : master.getColors().get("item.background"));
        g.fillRect(0, offsetY, master.getWidth(), h);
        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.background"));

            switch(master.getSelectionStyle()) {
                case "FULL": {
                    g.fillRect(0, offsetY, w, h);
                    break;
                }
                case "LINE_LEFT": {
                    g.fillRect(0, offsetY, master.getSelectionLineThickness(), h);
                    break;
                }
                case "LINE_RIGHT": {
                    g.fillRect(master.getWidth() - master.getSelectionLineThickness(), offsetY, master.getSelectionLineThickness(), h);
                    break;
                }
                case "LINE_TOP": {
                    g.fillRect(0, offsetY, w, master.getSelectionLineThickness());
                    break;
                }
                case "LINE_BOTTOM": {
                    g.fillRect(0, offsetY + h - master.getSelectionLineThickness(), master.getWidth(), master.getSelectionLineThickness());
                    break;
                }
            }
        }

        int margin = ((h - 16) / 2);
        x += 16;

        //File Icon
        if (icon != null) {
            g.drawImage(this.icon, x + 8 - icon.getWidth(null) / 2, offsetY + margin + 8 - icon.getHeight(null) / 2, null);
            x += 24;
        }

        //File Name

        if(this.selected) {
            g.setColor(master.getColors().get("item.selected.foreground"));
        } else if(this.rollover) {
            g.setColor(master.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColors().get("item.foreground"));
        }

        if(name != null) {
            g.drawString(name, x, offsetY + fm.getAscent() + ((h - fm.getHeight())/2));
        }


        int buttonHGap = 6;
        int buttonSize = 20;
        int buttonVGap = (h - buttonSize) / 2;
        int iconMargin = (buttonSize - 16) / 2;
        x = master.getWidth() - buttonHGap - buttonSize;

        int actionIndex = 0;
        for(OrderListAction action : actions) {
            int buttonBorderThickness = actionIndex == actionRolloverIndex ? master.getRolloverButtonBorderThickness() : master.getButtonBorderThickness();
            g.setColor(master.getColors().get("button" + (actionRolloverIndex == actionIndex ? ".rollover" : "") + ".border.color"));
            g.fillRect(x - buttonBorderThickness, offsetY + buttonVGap - buttonBorderThickness, buttonSize + 2*buttonBorderThickness, buttonSize + 2*buttonBorderThickness);

            g.setColor(master.getColors().get("button" + (actionRolloverIndex == actionIndex ? ".rollover" : "") + ".background"));
            g.fillRect(x, offsetY + buttonVGap, buttonSize, buttonSize);

            g.drawImage(action.getIcon(), x + iconMargin, offsetY + buttonVGap + iconMargin, null);

            x -= buttonHGap + buttonSize;
            actionIndex++;
        }

        g.dispose();
    }

    private int getActionRolloverIndex(MouseEvent e) {
        int buttonHGap = 6;
        int buttonSize = 20;
        int buttonVGap = (height - buttonSize) / 2;

        int x = master.getWidth() - buttonHGap - buttonSize;

        for(int i = 0; i < actions.size(); i++) {
            if(e.getY() >= lastRecordedOffset + buttonVGap && e.getY() < lastRecordedOffset + buttonVGap + buttonSize) {
                if(e.getX() >= x && e.getX() < x + buttonSize) {
                    return i;
                }
            }
            x -= buttonHGap + buttonSize;
        }

        return -1;
    }

    @Override
    public boolean select(MouseEvent e) {
        return getActionRolloverIndex(e) < 0;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public String getToolTipText() {
        if(actionRolloverIndex < 0) return token.getHint();
        return actions.get(actionRolloverIndex).getDescription();
    }

    @Override
    public Point getToolTipLocation() {
        return toolTipLocation;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(select(e)) master.selectElement(this);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && getActionRolloverIndex(e) >= 0) {
            OrderListAction action = actions.get(getActionRolloverIndex(e));
            int code = action.perform();
            switch(code) {
                case 0: {
                    //Remove
                    master.removeElement(this);
                    break;
                }
                case 1: {
                    //Move down
                    master.moveDown(this);
                    break;
                }
                case 2: {
                    //Move up
                    master.moveUp(this);
                    break;
                }
            }
        } else if(e.isPopupTrigger()) {
            JPopupMenu menu = this.generatePopup();
            if(menu != null) menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        actionRolloverIndex = -1;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        actionRolloverIndex = getActionRolloverIndex(e);
        if(actionRolloverIndex >= 0) {
            toolTipLocation.x = master.getWidth() - 16 - 26 * (actionRolloverIndex);
        } else {
            toolTipLocation.x = master.getWidth() / 2;
        }
    }

    public Image getIcon() {
        return icon;
    }

    private JPopupMenu generatePopup() {
        JPopupMenu menu = token.generateMenu();
        //TODO: add "move up/down" options
        return menu;
    }

    @Override
    public void themeChanged(Theme t) {
        this.updateIcon();
    }

    @NotNull
    public OrderListToken getToken() {
        return token;
    }
}
