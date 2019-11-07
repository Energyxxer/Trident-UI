package com.energyxxer.xswing;

import com.energyxxer.util.Factory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class XDropdownMenu<T> extends XButton {

    private ArrayList<T> options = new ArrayList<>();
    private ArrayList<ImageIcon> icons = new ArrayList<>();

    protected int selected = -1;

    private Factory<JPopupMenu> popupFactory = JPopupMenu::new;
    private Factory<JMenuItem> itemFactory = JMenuItem::new;

    private ArrayList<ChoiceListener<T>> choiceListeners = new ArrayList<>();

    public XDropdownMenu() {
        super(" ");
    }

    public XDropdownMenu(T[] options) {
        super(" ");
        setOptions(options);
    }

    {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                XDropdownMenu.this.mouseClicked(e);
            }
        });
        setHorizontalAlignment(SwingConstants.LEFT);
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
            this.setText(options.get(0).toString());
            this.setIcon(icons.get(0));
        } else {
            this.setText(options.get(selected).toString());
            this.setIcon(icons.get(selected));
        }
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

    public void mouseClicked(MouseEvent e) {

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

        pm.setPreferredSize(new Dimension(Math.max(this.getWidth(), width),height));
        pm.show(this,0 ,this.getHeight()-1);
    }
}
