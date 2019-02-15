package com.energyxxer.trident.main.window.sections.tools.find;

import com.energyxxer.trident.main.window.sections.tools.ToolBoard;
import com.energyxxer.trident.main.window.sections.tools.ToolBoardMaster;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.trident.ui.scrollbar.OverlayScrollPane;

import java.awt.*;
import java.util.ArrayList;

public class FindBoard extends ToolBoard {

    private FindExplorerMaster explorer = new FindExplorerMaster();

    public FindBoard(ToolBoardMaster parent) {
        super(parent);
        this.setLayout(new BorderLayout());

        this.setPreferredSize(new Dimension(1, 300));

        this.add(new OverlayScrollPane(explorer), BorderLayout.CENTER);
    }

    @Override
    public String getName() {
        return "Find";
    }

    @Override
    public String getIconName() {
        return "search";
    }

    public void showResults(FindResults results) {
        explorer.clear();
        StandardExplorerItem item = new StandardExplorerItem(results, explorer, new ArrayList<>());
        item.setDetailed(true);
        explorer.addElement(item);
        this.repaint();
    }
}
