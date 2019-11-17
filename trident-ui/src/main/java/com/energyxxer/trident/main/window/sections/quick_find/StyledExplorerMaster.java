package com.energyxxer.trident.main.window.sections.quick_find;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;
import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;

import java.awt.*;
import java.util.function.Predicate;

public class StyledExplorerMaster extends ExplorerMaster {

    private ThemeListenerManager tlm = new ThemeListenerManager();
    private boolean forceSelectNext;

    public StyledExplorerMaster() {
        this(null);
    }

    public StyledExplorerMaster(String namespace) {
        if(namespace == null) {
            tlm.addThemeChangeListener(t -> {

                colors.put("background", t.getColor(Color.WHITE, "Explorer.background"));
                colors.put("item.background", t.getColor(new Color(0, 0, 0, 0), "Explorer.item.background"));
                colors.put("item.foreground", t.getColor(Color.BLACK, "Explorer.item.foreground", "General.foreground"));
                colors.put("item.selected.background", t.getColor(Color.BLUE, "Explorer.item.selected.background", "Explorer.item.background"));
                colors.put("item.selected.foreground", t.getColor(Color.BLACK, "Explorer.item.selected.foreground", "Explorer.item.hover.foreground", "Explorer.item.foreground", "General.foreground"));
                colors.put("item.rollover.background", t.getColor(new Color(0, 0, 0, 0), "Explorer.item.hover.background", "Explorer.item.background"));
                colors.put("item.rollover.foreground", t.getColor(Color.BLACK, "Explorer.item.hover.foreground", "Explorer.item.foreground", "General.foreground"));

                rowHeight = Math.max(t.getInteger(20, "Explorer.item.height"), 1);

                selectionStyle = t.getString("Explorer.item.selectionStyle", "default:FULL");
                selectionLineThickness = Math.max(t.getInteger(2, "Explorer.item.selectionLineThickness"), 0);

                styleNumbers.put("button.border.thickness", Math.max(t.getInteger(1,"Explorer.button.border.thickness"),1));
                styleNumbers.put("button.rollover.border.thickness", Math.max(t.getInteger(1,"Explorer.button.hover.border.thickness", "Explorer.button.border.thickness"),1));
                styleNumbers.put("button.pressed.border.thickness", Math.max(t.getInteger(1,"Explorer.button.pressed.border.thickness", "Explorer.button.hover.border.thickness", "Explorer.button.border.thickness"),1));

                styleNumbers.put("checkbox.border.thickness", Math.max(t.getInteger(1,"Explorer.checkbox.border.thickness"),1));
                styleNumbers.put("checkbox.rollover.border.thickness", Math.max(t.getInteger(1,"Explorer.checkbox.hover.border.thickness", "Explorer.checkbox.border.thickness"),1));
                styleNumbers.put("checkbox.pressed.border.thickness", Math.max(t.getInteger(1,"Explorer.checkbox.pressed.border.thickness", "Explorer.checkbox.hover.border.thickness", "Explorer.checkbox.border.thickness"),1));

                this.setFont(t.getFont("Explorer.item", "General"));

                colors.put("button.background", t.getColor(Color.GRAY, "Explorer.button.background"));
                colors.put("button.rollover.background", t.getColor(Color.GRAY, "Explorer.button.hover.background", "Explorer.button.background"));
                colors.put("button.pressed.background", t.getColor(Color.GRAY, "Explorer.button.pressed.background","Explorer.button.hover.background", "Explorer.button.background"));
                colors.put("button.border.color", t.getColor(Color.BLACK, "Explorer.button.border.color"));
                colors.put("button.rollover.border.color", t.getColor(Color.BLACK, "Explorer.button.hover.border.color", "Explorer.button.border.color"));
                colors.put("button.pressed.border.color", t.getColor(Color.BLACK, "Explorer.button.pressed.border.color", "Explorer.button.hover.border.color", "Explorer.button.border.color"));

                colors.put("checkbox.background", t.getColor(Color.GRAY, "Explorer.checkbox.background"));
                colors.put("checkbox.rollover.background", t.getColor(Color.GRAY, "Explorer.checkbox.hover.background", "Explorer.checkbox.background"));
                colors.put("checkbox.pressed.background", t.getColor(Color.GRAY, "Explorer.checkbox.pressed.background","Explorer.checkbox.hover.background", "Explorer.checkbox.background"));
                colors.put("checkbox.border.color", t.getColor(Color.BLACK, "Explorer.checkbox.border.color"));
                colors.put("checkbox.rollover.border.color", t.getColor(Color.BLACK, "Explorer.checkbox.hover.border.color", "Explorer.checkbox.border.color"));
                colors.put("checkbox.pressed.border.color", t.getColor(Color.BLACK, "Explorer.checkbox.pressed.border.color", "Explorer.checkbox.hover.border.color", "Explorer.checkbox.border.color"));
                
                
                
                rowHeight = Math.max(t.getInteger(20, "Explorer.item.height"), 1);
                indentPerLevel = Math.max(t.getInteger(20, "Explorer.item.indent"), 0);
                initialIndent = Math.max(t.getInteger(0, "Explorer.item.initialIndent"), 0);

                assets.put("expand", Commons.getIcon("triangle_right").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                assets.put("collapse", Commons.getIcon("triangle_down").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                assets.put("info", Commons.getIcon("info").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                assets.put("warning", Commons.getIcon("warn").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                assets.put("error", Commons.getIcon("error").getScaledInstance(16, 16, Image.SCALE_SMOOTH));

                colors.put("item.find.background", t.getColor(Color.YELLOW.darker(), "Explorer.item.find.background"));

                children.forEach(e -> e.themeChanged(t));
            });
        } else {
            tlm.addThemeChangeListener(t -> {

                colors.put("background", t.getColor(Color.WHITE, namespace + ".background", "Explorer.background"));
                colors.put("item.background", t.getColor(new Color(0, 0, 0, 0), namespace + ".item.background", "Explorer.item.background"));
                colors.put("item.foreground", t.getColor(Color.BLACK, namespace + ".item.foreground", "Explorer.item.foreground", "General.foreground"));
                colors.put("item.selected.background", t.getColor(Color.BLUE, namespace + ".item.selected.background", namespace + ".item.background", "Explorer.item.selected.background", "Explorer.item.background"));
                colors.put("item.selected.foreground", t.getColor(Color.BLACK, namespace + ".item.selected.foreground", namespace + ".item.hover.foreground", namespace + ".item.foreground", "Explorer.item.selected.foreground", "Explorer.item.hover.foreground", "Explorer.item.foreground", "General.foreground"));
                colors.put("item.rollover.background", t.getColor(new Color(0, 0, 0, 0), namespace + ".item.hover.background", namespace + ".item.background", "Explorer.item.hover.background", "Explorer.item.background"));
                colors.put("item.rollover.foreground", t.getColor(Color.BLACK, namespace + ".item.hover.foreground", namespace + ".item.foreground", "Explorer.item.hover.foreground", "Explorer.item.foreground", "General.foreground"));

                rowHeight = Math.max(t.getInteger(20, namespace + ".item.height", "Explorer.item.height"), 1);

                selectionStyle = t.getString(namespace + ".item.selectionStyle", "Explorer.item.selectionStyle", "default:FULL");
                selectionLineThickness = Math.max(t.getInteger(2, namespace + ".item.selectionLineThickness", "Explorer.item.selectionLineThickness"), 0);

                styleNumbers.put("button.border.thickness", Math.max(t.getInteger(1,namespace + ".button.border.thickness", "Explorer.button.border.thickness"),1));
                styleNumbers.put("button.rollover.border.thickness", Math.max(t.getInteger(1,namespace + ".button.hover.border.thickness", namespace + ".button.border.thickness", "Explorer.button.hover.border.thickness", "Explorer.button.border.thickness"),1));
                styleNumbers.put("button.pressed.border.thickness", Math.max(t.getInteger(1,namespace + ".button.pressed.border.thickness", namespace + ".button.hover.border.thickness", namespace + ".button.border.thickness", "Explorer.button.pressed.border.thickness", "Explorer.button.hover.border.thickness", "Explorer.button.border.thickness"),1));

                styleNumbers.put("checkbox.border.thickness", Math.max(t.getInteger(1,namespace + ".checkbox.border.thickness", "Explorer.checkbox.border.thickness"),1));
                styleNumbers.put("checkbox.rollover.border.thickness", Math.max(t.getInteger(1,namespace + ".checkbox.hover.border.thickness", namespace + ".checkbox.border.thickness", "Explorer.checkbox.hover.border.thickness", "Explorer.checkbox.border.thickness"),1));
                styleNumbers.put("checkbox.pressed.border.thickness", Math.max(t.getInteger(1,namespace + ".checkbox.pressed.border.thickness", namespace + ".checkbox.hover.border.thickness", namespace + ".checkbox.border.thickness", "Explorer.checkbox.pressed.border.thickness", "Explorer.checkbox.hover.border.thickness", "Explorer.checkbox.border.thickness"),1));

                this.setFont(t.getFont(namespace + ".item", "Explorer.item", "General"));

                colors.put("button.background", t.getColor(Color.GRAY, namespace + ".button.background", "Explorer.button.background"));
                colors.put("button.rollover.background", t.getColor(Color.GRAY, namespace + ".button.hover.background", namespace + ".button.background", "Explorer.button.hover.background", "Explorer.button.background"));
                colors.put("button.pressed.background", t.getColor(Color.GRAY, namespace + ".button.pressed.background", namespace + ".button.hover.background", namespace + ".button.background", "Explorer.button.pressed.background", "Explorer.button.hover.background", "Explorer.button.background"));
                colors.put("button.border.color", t.getColor(Color.BLACK, namespace + ".button.border.color", "Explorer.button.border.color"));
                colors.put("button.rollover.border.color", t.getColor(Color.BLACK, namespace + ".button.hover.border.color", namespace + ".button.border.color", "Explorer.button.hover.border.color", "Explorer.button.border.color"));
                colors.put("button.pressed.border.color", t.getColor(Color.BLACK, namespace + ".button.pressed.border.color", namespace + ".button.hover.border.color", namespace + ".button.border.color", "Explorer.button.pressed.border.color", "Explorer.button.hover.border.color", "Explorer.button.border.color"));

                colors.put("checkbox.background", t.getColor(Color.GRAY, namespace + ".checkbox.background", "Explorer.checkbox.background"));
                colors.put("checkbox.rollover.background", t.getColor(Color.GRAY, namespace + ".checkbox.hover.background", namespace + ".checkbox.background", "Explorer.checkbox.hover.background", "Explorer.checkbox.background"));
                colors.put("checkbox.pressed.background", t.getColor(Color.GRAY, namespace + ".checkbox.pressed.background", namespace + ".checkbox.hover.background", namespace + ".checkbox.background", "Explorer.checkbox.pressed.background", "Explorer.checkbox.hover.background", "Explorer.checkbox.background"));
                colors.put("checkbox.border.color", t.getColor(Color.BLACK, namespace + ".checkbox.border.color", "Explorer.checkbox.border.color"));
                colors.put("checkbox.rollover.border.color", t.getColor(Color.BLACK, namespace + ".checkbox.hover.border.color", namespace + ".checkbox.border.color", "Explorer.checkbox.hover.border.color", "Explorer.checkbox.border.color"));
                colors.put("checkbox.pressed.border.color", t.getColor(Color.BLACK, namespace + ".checkbox.pressed.border.color", namespace + ".checkbox.hover.border.color", namespace + ".checkbox.border.color", "Explorer.checkbox.pressed.border.color", "Explorer.checkbox.hover.border.color", "Explorer.checkbox.border.color"));



                rowHeight = Math.max(t.getInteger(20, namespace + ".item.height", "Explorer.item.height"), 1);
                indentPerLevel = Math.max(t.getInteger(20, namespace + ".item.indent", "Explorer.item.indent"), 0);
                initialIndent = Math.max(t.getInteger(0, namespace + ".item.initialIndent", "Explorer.item.initialIndent"), 0);

                assets.put("expand", Commons.getIcon("triangle_right").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                assets.put("collapse", Commons.getIcon("triangle_down").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                assets.put("info", Commons.getIcon("info").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                assets.put("warning", Commons.getIcon("warn").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                assets.put("error", Commons.getIcon("error").getScaledInstance(16, 16, Image.SCALE_SMOOTH));

                colors.put("item.find.background", t.getColor(Color.YELLOW.darker(), namespace + ".item.find.background", "Explorer.item.find.background"));

                children.forEach(e -> e.themeChanged(t));
            });
        }

        explorerFlags.put(ExplorerFlag.DYNAMIC_ROW_HEIGHT, false);
    }

    public void addElement(ExplorerElement elem) {
        children.add(elem);
    }

    public void removeElement(ExplorerElement elem) {
        children.remove(elem);
    }

    public void removeElementIf(Predicate<ExplorerElement> elem) {
        children.removeIf(elem);
    }

    public void clear() {
        children.clear();
        this.repaint();
    }

    public int getCount() {
        return children.size();
    }

    public int getTotalCount() {
        return flatList.size();
    }

    public int getFirstSelectedIndex() {
        for(int i = 0; i < flatList.size(); i++) {
            if(selectedItems.contains(flatList.get(i))) return i;
        }
        return -1;
    }

    public void setSelectedIndex(int index) {
        if(index < 0 || index >= flatList.size()) return;
        ExplorerElement toSelect = flatList.get(index);
        setSelected(toSelect, null);
    }

    public void clearChildrenSelection() {
        for(ExplorerElement child : children) {
            child.setSelected(false);
            flatList.remove(child);
        }
    }

    public Rectangle getVisibleRect(int index) {
        return new Rectangle(0, index*this.getRowHeight(), this.getWidth(), this.getRowHeight());
    }

    public void setForceSelectNext(boolean forceSelectNext) {
        this.forceSelectNext = forceSelectNext;
    }

    public boolean isForceSelectNext() {
        return forceSelectNext;
    }

    @Override
    public void addToFlatList(ExplorerElement element) {
        super.addToFlatList(element);
        if(forceSelectNext && flatList.size() == 1) {
            setSelectedIndex(0);
            forceSelectNext = false;
        }
    }
}
