package com.energyxxer.trident.ui.styledcomponents;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.XIcon;

import java.awt.image.BufferedImage;

/**
 * Created by User on 2/11/2017.
 */
public class StyledIcon extends XIcon {

    private String iconName;
    private int width, height, hints;

    public StyledIcon(String icon, int width, int height, int hints, ThemeListenerManager tlm) {
        this.iconName = icon;
        this.width = width;
        this.height = height;
        this.hints = hints;
        tlm.addThemeChangeListener(t -> updateIcon());
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
        updateIcon();
    }

    private void updateIcon() {
        if(width + height < 0) {
            this.setImage((BufferedImage) Commons.getIcon(iconName).getScaledInstance(width, height, hints));
        } else {
            this.setImage(Commons.getIcon(iconName));
        }
    }
}
