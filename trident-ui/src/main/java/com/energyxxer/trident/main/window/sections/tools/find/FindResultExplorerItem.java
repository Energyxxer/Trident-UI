package com.energyxxer.trident.main.window.sections.tools.find;

import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.trident.ui.explorer.base.elements.ExplorerElement;
import com.energyxxer.trident.ui.modules.ModuleToken;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Predicate;

public class FindResultExplorerItem extends StandardExplorerItem {

    private Predicate<FindExplorerFilter> filter;

    public FindResultExplorerItem(ModuleToken token, StandardExplorerItem parent, Predicate<FindExplorerFilter> filter) {
        super(token, parent, new ArrayList<>());
        this.filter = filter;
    }

    public FindResultExplorerItem(ModuleToken token, ExplorerMaster master, Predicate<FindExplorerFilter> filter) {
        super(token, master, new ArrayList<>());
        this.filter = filter;
    }

    @Override
    public void render(Graphics g) {
        if(!(this.master instanceof FindExplorerFilter) || filter.test(((FindExplorerFilter) this.master))) {
            super.render(g);
        } else {
            if(!expanded) {
                this.expand(new ArrayList<>());
            }
            for(ExplorerElement element : children) {
                element.render(g);
            }
        }
    }
}
