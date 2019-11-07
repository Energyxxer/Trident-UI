package com.energyxxer.trident.ui.orderlist;

import com.energyxxer.trident.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.trident.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.util.Factory;
import com.energyxxer.xswing.ChoiceListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class OrderListDropdownAction<T> implements OrderListAction {
    protected boolean leftAligned = false;

    private OrderListMaster master;

    private ArrayList<T> options = new ArrayList<>();
    private ArrayList<ImageIcon> icons = new ArrayList<>();

    protected int selected = -1;

    private Factory<JPopupMenu> popupFactory = JPopupMenu::new;
    private Factory<JMenuItem> itemFactory = JMenuItem::new;

    private ArrayList<ChoiceListener<T>> choiceListeners = new ArrayList<>();



    protected String label = "";
    protected ImageIcon icon = null;

    public OrderListDropdownAction() {
        this.setPopupFactory(StyledPopupMenu::new);
        this.setPopupItemFactory(StyledMenuItem::new);
    }

    public OrderListDropdownAction(T[] options) {
        setOptions(options);

        this.setPopupFactory(StyledPopupMenu::new);
        this.setPopupItemFactory(StyledMenuItem::new);
    }



    public ArrayList<T> getOptions() {
        return options;
    }

    public void setOptions(T[] options) {
        clear();
        addOptions(options);
    }

    public void addOptions(T[] options) {
        for(T o : options) {
            addOption(o);
        }
    }

    public void addOption(T option) {
        this.options.add(option);
        this.icons.add(null);
        updateOptions();
    }

    private void updateOptions() {
        if(selected == -1 && options.size() > 0) {
            selected = 0;
            this.setLabel(options.get(0).toString());
            this.setIcon(icons.get(0));
        } else {
            this.setLabel(options.get(selected).toString());
            this.setIcon(icons.get(selected));
        }
    }

    private void setLabel(String label) {
        this.label = label;
    }

    private void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public void setPopupFactory(@NotNull Factory<JPopupMenu> f) {
        this.popupFactory = f;
    }

    public void setPopupItemFactory(@NotNull Factory<JMenuItem> f) {
        this.itemFactory = f;
    }

    public void addChoiceListener(@NotNull ChoiceListener<T> l) {choiceListeners.add(l);}

    private void registerChoice(int index) {
        selected = index;
        updateOptions();
        T selected = options.get(index);
        if(master != null) master.repaint();
        for(ChoiceListener<T> listener : choiceListeners) listener.onChoice(selected);
    }

    public T getValue() {
        if(selected < 0) return null;
        if(selected >= options.size()) return null;
        return options.get(selected);
    }

    public int getValueIndex() {
        return selected;
    }

    public void setValue(T value) {
        int index = options.indexOf(value);
        if(index >= 0) {
            registerChoice(index);
        }
    }

    public void setValueIndex(int index) {
        if(index >= 0 && index < options.size()) registerChoice(index);
    }

    public void setIcon(int index, Image img) {
        this.icons.set(index, new ImageIcon(img.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        updateOptions();
    }

    public void clear() {
        selected = -1;
        options.clear();
        icons.clear();
    }

    @Override
    public void mouseClicked(MouseEvent e, StandardOrderListItem parent) {

        JPopupMenu pm = popupFactory.createInstance();

        int height = 2;
        int width = 10;

        for(int i = 0; i < options.size(); i++) {
            T option = options.get(i);
            JMenuItem item = itemFactory.createInstance();
            item.setText(option.toString());
            item.setIcon(icons.get(i));
            int choice = i;
            item.addActionListener(arg0 -> registerChoice(choice));
            pm.add(item);
            height += item.getPreferredSize().getHeight();
            width = Math.max(width, item.getPreferredSize().width);
        }

        pm.setPreferredSize(new Dimension(Math.max(lastRenderedWidth, width),height));
        pm.show(parent.master, lastRenderedX - 1, lastRenderedY + lastRenderedHeight - 4);
    }






    private static final int buttonHGap = 6;
    private static final int buttonVSize = 20;
    private static final int iconMargin = (buttonVSize - 16) / 2;

    private int lastRenderedWidth = 0;
    private int lastRenderedHeight = 20;
    private int lastRenderedX = 0;
    private int lastRenderedY = 0;

    @Override
    public boolean isLeftAligned() {
        return leftAligned;
    }

    public void setLeftAligned(boolean leftAligned) {
        this.leftAligned = leftAligned;
    }

    @Override
    public void render(Graphics g, OrderListMaster master, int x, int y, int w, int h, int mouseState, boolean actionEnabled) {
        this.master = master;

        int buttonVGap = (h - buttonVSize) / 2;

        String styleKeyVariation = mouseState == 2 ? ".pressed" : mouseState == 1 ? ".rollover" : "";

        FontMetrics fm = g.getFontMetrics(g.getFont());

        int buttonWidth = 2 * iconMargin;
        buttonWidth += fm.stringWidth(label);
        if(icon != null) {
            buttonWidth += 16 + iconMargin;
        }

        if(!isLeftAligned()) x -= buttonWidth;

        lastRenderedX = x;


        int buttonBorderThickness = master.getStyleNumbers().get("button" + styleKeyVariation + ".border.thickness");
        g.setColor(master.getColors().get("button" + styleKeyVariation + ".border.color"));
        g.fillRect(x - buttonBorderThickness, y + buttonVGap - buttonBorderThickness, buttonWidth + 2*buttonBorderThickness, buttonVSize + 2*buttonBorderThickness);

        g.setColor(master.getColors().get("button" + styleKeyVariation + ".background"));
        g.fillRect(x, y + buttonVGap, buttonWidth, buttonVSize);

        x += iconMargin;

        if(this.icon != null) {
            g.drawImage(this.icon.getImage(), x + iconMargin, y + buttonVGap + iconMargin, null);
            x += 16;
            x += iconMargin;
        }

        if(mouseState >= 1) {
            g.setColor(master.getColors().get("item.rollover.foreground"));
        } else {
            g.setColor(master.getColors().get("item.foreground"));
        }

        g.drawString(label, x, y + fm.getAscent() + ((h - fm.getHeight())/2));

        lastRenderedWidth = buttonWidth;
        lastRenderedHeight = h;
        lastRenderedY = y;
    }

    @Override
    public int getRenderedWidth() {
        return buttonHGap + lastRenderedWidth;
    }

    @Override
    public String getHint() {
        return null;
    }

    @Override
    public int getHintOffset() {
        return 0;
    }

    @Override
    public boolean intersects(Point p, int w, int h) {
        int buttonVGap = (h - buttonVSize) / 2;

        return (p.getY() >= buttonVGap && p.getY() < buttonVGap + buttonVSize) &&
                (p.getX() >= 0 && p.getX() < lastRenderedWidth);
    }
}
