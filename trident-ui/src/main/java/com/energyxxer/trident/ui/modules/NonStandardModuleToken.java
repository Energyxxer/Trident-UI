package com.energyxxer.trident.ui.modules;

import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;

public interface NonStandardModuleToken {
    ExplorerElement createElement(StandardExplorerItem parent);
    ExplorerElement createElement(ExplorerMaster parent);
}
