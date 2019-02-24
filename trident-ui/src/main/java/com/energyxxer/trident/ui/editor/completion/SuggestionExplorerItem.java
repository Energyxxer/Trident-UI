package com.energyxxer.trident.ui.editor.completion;

import com.energyxxer.trident.ui.explorer.base.ExplorerMaster;
import com.energyxxer.trident.ui.explorer.base.StandardExplorerItem;

import java.awt.*;
import java.util.ArrayList;

public class SuggestionExplorerItem extends StandardExplorerItem {

    SuggestionToken token;

    public SuggestionExplorerItem(SuggestionToken token, ExplorerMaster master) {
        super(token, master, new ArrayList<>());
        this.token = token;
    }

    @Override
    public void render(Graphics g) {
        if(token.isEnabled()) {
            super.render(g);
        }
    }
}
