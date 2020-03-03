package com.energyxxer.trident.ui.styledcomponents;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.XToggleButton;

import java.awt.*;

public class StyledToggleButton extends XToggleButton {

    private String namespace = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public StyledToggleButton(String iconName) {
        this(iconName, null);
    }

    public StyledToggleButton(String iconName, String namespace) {
        if(namespace != null) this.setNamespace(namespace);

        this.setPreferredSize(new ScalableDimension(24, 24));

        tlm.addThemeChangeListener(t -> {
            if(this.namespace != null) {
                setBackground       (t.getColor(new Color(215, 215, 215), this.namespace + ".button.background","General.button.background"));
                setForeground       (t.getColor(Color.BLACK, this.namespace + ".button.foreground","General.button.foreground","General.foreground"));
                setBorder           (t.getColor(new Color(200, 200, 200), this.namespace + ".button.border.color","General.button.border.color"), Math.max(t.getInteger(1,this.namespace + ".button.border.thickness", "General.button.border.thickness"),0));
                setRolloverColor    (t.getColor(new Color(200, 202, 205), this.namespace + ".button.hover.background","General.button.hover.background"));
                setPressedColor     (t.getColor(Color.WHITE, this.namespace + ".button.pressed.background","General.button.pressed.background"));
                setFont(t.getFont(this.namespace+".button","General.button","General"));
            } else {
                setBackground       (t.getColor(new Color(215, 215, 215), "General.button.background"));
                setForeground       (t.getColor(Color.BLACK, "General.button.foreground","General.foreground"));
                setBorder(t.getColor(new Color(200, 200, 200), "General.button.border.color"),Math.max(t.getInteger(1,"General.button.border.thickness"),0));
                setRolloverColor    (t.getColor(new Color(200, 202, 205), "General.button.hover.background"));
                setPressedColor     (t.getColor(Color.WHITE, "General.button.pressed.background"));
                setFont(t.getFont("General.button","General"));
            }
            this.setToggleIcon(Commons.getIcon(iconName).getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        });
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }
}
