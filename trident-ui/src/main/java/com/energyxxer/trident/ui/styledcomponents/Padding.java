package com.energyxxer.trident.ui.styledcomponents;

import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ScalableDimension;

import java.awt.*;

/**
 * Created by User on 5/12/2017.
 */
public class Padding extends com.energyxxer.xswing.Padding {

    public Padding(int size) {
        Dimension dim = new ScalableDimension(size, size);
        this.setPreferredSize(dim);
        this.setMaximumSize(dim);
    }

    public Padding(int size, ThemeListenerManager tlm, String... keys) {
        tlm.addThemeChangeListener(t -> {
            int realSize = t.getInteger(size, keys);
            Dimension dim = new ScalableDimension(realSize, realSize);
            this.setPreferredSize(dim);
            this.setMaximumSize(dim);
        });
    }
}
