package com.energyxxer.trident.ui.explorer.base;

import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.modules.ModuleToken;
import com.energyxxer.trident.ui.orderlist.CompoundActionModuleToken;
import com.energyxxer.trident.ui.orderlist.ItemAction;
import com.energyxxer.trident.ui.orderlist.ItemActionHost;
import com.energyxxer.trident.ui.theme.Theme;
import com.energyxxer.trident.util.ImageUtil;
import com.energyxxer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class ActionHostExplorerItem extends ExplorerElement implements ItemActionHost {

    @NotNull
    private final CompoundActionModuleToken token;

    private Image icon = null;
    private String name = null;

    private int y = 0;
    private int height = 20;

    private int actionRolloverIndex = -1;
    private int pressedStart = -1;
    private Point toolTipLocation = new Point();

    private List<ItemAction> actions;

    public ActionHostExplorerItem(ExplorerMaster master, @NotNull CompoundActionModuleToken token) {
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
        master.flatList.add(this);

        int x = 0;
        this.y = master.getOffsetY();
        this.lastRecordedOffset = y;
        int w = master.getWidth();
        int h = master.getRowHeight();

        this.height = h;
        toolTipLocation.y = (int) (height * 0.65);

        int offsetY = y;

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

        int leftX = 6;
        int rightX = master.getWidth() - 6;
        {

            if(master.getSelectionStyle().equals("LINE_LEFT")) {
                leftX += master.getSelectionLineThickness();
            } else if(master.getSelectionStyle().equals("LINE_RIGHT")) {
                rightX -= master.getSelectionLineThickness();
            }

            int actionIndex = 0;
            for(ItemAction action : actions) {
                action.render(g, this, (action.isLeftAligned() ? leftX : rightX), offsetY, w, h, (actionIndex == actionRolloverIndex) ? (pressedStart == actionIndex ? 2 : 1) : 0, isActionEnabled(actionIndex));
                int actionOffset = action.getRenderedWidth();

                if(action.isLeftAligned()) leftX += actionOffset;
                else rightX -= actionOffset;

                actionIndex++;
            }

            x = leftX;
        }

        int margin = ((h - 16) / 2);
        x += 10;

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
            int stringWidth = fm.stringWidth(name);
            g.setColor(Color.WHITE);
            if(x + stringWidth > rightX) {
                float estimatedCharacterWidth = (float)stringWidth/name.length();
                int overlap = x + stringWidth - rightX;

                int stripAmount = (int) Math.ceil((overlap / estimatedCharacterWidth) + 6);
                String stripped;
                if(stripAmount >= name.length()) {
                    stripped = "";
                } else if(token.ellipsisFromLeft()) {
                    stripped = "..." + name.substring(stripAmount);
                } else {
                    stripped = name.substring(0, name.length() - stripAmount) + "...";
                }
                g.drawString(stripped, x, offsetY + fm.getAscent() + ((h - fm.getHeight())/2));
            } else {
                g.drawString(name, x, offsetY + fm.getAscent() + ((h - fm.getHeight())/2));
            }
        }

        master.renderOffset(this.getHeight());
    }

    private boolean isActionEnabled(int index) {
        ItemAction action = actions.get(index);
        return action.getActionCode() == -1;
    }

    private int getActionRolloverIndex(MouseEvent e) {
        return getActionRolloverIndex(e, false);
    }

    private int getActionRolloverIndex(MouseEvent e, boolean update) {
        if(update) {
            toolTipLocation.x = master.getWidth() / 2;
            actionRolloverIndex = -1;
        }

        int leftX = 6;
        int rightX = master.getWidth() - 6;

        if(master.getSelectionStyle().equals("LINE_LEFT")) {
            leftX += master.getSelectionLineThickness();
        } else if(master.getSelectionStyle().equals("LINE_RIGHT")) {
            rightX -= master.getSelectionLineThickness();
        }

        for(int i = 0; i < actions.size(); i++) {
            ItemAction action = actions.get(i);
            if(action.intersects(new Point(action.isLeftAligned() ? (e.getX() - leftX) : (rightX - e.getX()), e.getY() - lastRecordedOffset), master.getWidth(), getHeight())) {
                if(isActionEnabled(i)) {
                    if(update) {
                        actionRolloverIndex = i;
                        toolTipLocation.x = (action.isLeftAligned() ? leftX : (rightX - action.getRenderedWidth())) + action.getHintOffset();
                    }
                    return i;
                } else {
                    return -1;
                }
            }
            int actionOffset = action.getRenderedWidth();
            if(action.isLeftAligned()) leftX += actionOffset;
            else rightX -= actionOffset;
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
        return actions.get(actionRolloverIndex).getHint();
    }

    @Override
    public Point getToolTipLocation() {
        return toolTipLocation;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ItemAction action = getActionForMouseEvent(e);
        if(action != null) action.mouseClicked(e, this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && select(e)) {
            master.setSelected(this, e);
        }
        int index = getActionRolloverIndex(e);
        if(index >= 0) {
            pressedStart = index;
            actions.get(index).mousePressed(e, this);
        } else {
            pressedStart = -1;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) master.setSelected(this, e);
        int index = getActionRolloverIndex(e);
        if(pressedStart >= 0 && pressedStart == index) {
            actions.get(pressedStart).mouseReleased(e, this);
        }
        pressedStart = -1;
        if(!e.isConsumed() && e.isPopupTrigger()) {
            JPopupMenu menu = this.generatePopup();
            if(menu != null) menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private ItemAction getActionForMouseEvent(MouseEvent e) {
        int index = getActionRolloverIndex(e);
        return index >= 0 ? actions.get(index) : null;
    }

    public void performOperation(int code) {
        //No codes for this explorer item
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        actionRolloverIndex = -1;
        pressedStart = -1;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        token.onInteract();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        getActionRolloverIndex(e, true);
    }

    public Image getIcon() {
        return icon;
    }

    private JPopupMenu generatePopup() {
        JPopupMenu menu = token.generateMenu(ModuleToken.MenuContext.EXPLORER);
        //TODO: add "move up/down" options
        return menu;
    }

    @Override
    public void themeChanged(Theme t) {
        this.updateIcon();
    }

    @NotNull
    public CompoundActionModuleToken getToken() {
        return token;
    }

    @Override
    public JComponent getComponent() {
        return master;
    }

    @Override
    public StyleProvider getStyleProvider() {
        return master;
    }
}
