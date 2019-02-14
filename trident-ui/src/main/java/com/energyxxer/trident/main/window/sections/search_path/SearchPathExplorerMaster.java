package com.energyxxer.trident.main.window.sections.search_path;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.explorer.base.ExplorerFlag;
import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;

import java.awt.*;

public class SearchPathExplorerMaster extends ExplorerMaster {

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public SearchPathExplorerMaster() {
        tlm.addThemeChangeListener(t -> {
            colors.put("background",t.getColor(Color.WHITE, "Explorer.background"));
            colors.put("item.background",t.getColor(new Color(0,0,0,0), "Explorer.item.background"));
            colors.put("item.foreground",t.getColor(Color.BLACK, "Explorer.item.foreground","General.foreground"));
            colors.put("item.selected.background",t.getColor(Color.BLUE, "Explorer.item.selected.background","Explorer.item.background"));
            colors.put("item.selected.foreground",t.getColor(Color.BLACK, "Explorer.item.selected.foreground","Explorer.item.hover.foreground","Explorer.item.foreground","General.foreground"));
            colors.put("item.rollover.background",t.getColor(new Color(0,0,0,0), "Explorer.item.hover.background","Explorer.item.background"));
            colors.put("item.rollover.foreground",t.getColor(Color.BLACK, "Explorer.item.hover.foreground","Explorer.item.foreground","General.foreground"));

            rowHeight = Math.max(t.getInteger(20,"Explorer.item.height"), 1);
            indentPerLevel = Math.max(t.getInteger(20,"Explorer.item.indent"), 0);
            initialIndent = Math.max(t.getInteger(0,"Explorer.item.initialIndent"), 0);

            selectionStyle = t.getString("Explorer.item.selectionStyle","default:FULL");
            selectionLineThickness = Math.max(t.getInteger(2,"Explorer.item.selectionLineThickness"), 0);

            this.setFont(t.getFont("Explorer.item","General"));

            assets.put("expand", Commons.getIcon("triangle_right").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            assets.put("collapse",Commons.getIcon("triangle_down").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            assets.put("info",Commons.getIcon("info").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            assets.put("warning",Commons.getIcon("warn").getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            assets.put("error",Commons.getIcon("error").getScaledInstance(16, 16, Image.SCALE_SMOOTH));



            colors.put("item.find.background",t.getColor(Color.YELLOW.darker(), "FindInPath.item.find.background"));
        });

        explorerFlags.put(ExplorerFlag.DYNAMIC_ROW_HEIGHT, false);
    }

    void addElement(ExplorerElement elem) {
        children.add(elem);
    }

    void clear() {
        children.clear();
    }

    int getCount() {
        return children.size();
    }
}
