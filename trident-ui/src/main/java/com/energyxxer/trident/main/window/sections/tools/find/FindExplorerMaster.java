package com.energyxxer.trident.main.window.sections.tools.find;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.main.window.sections.quick_find.StyledExplorerMaster;

public class FindExplorerMaster extends StyledExplorerMaster implements FindExplorerFilter {
    @Override
    public boolean groupByProject() {
        return TridentWindow.findBoard.groupByProject();
    }

    @Override
    public boolean groupBySubProject() {
        return TridentWindow.findBoard.groupBySubProject();
    }

    @Override
    public boolean groupByPath() {
        return TridentWindow.findBoard.groupByPath();
    }

    @Override
    public boolean groupByFile() {
        return TridentWindow.findBoard.groupByFile();
    }
}
