package com.energyxxer.trident.ui.styledcomponents;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ScalableGraphics2D;
import com.energyxxer.xswing.XCheckBox;

import java.awt.*;

public class StyledCheckBox extends XCheckBox {

    private String namespace = null;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    public StyledCheckBox(String label) {
        this(label, null, null);
    }

    public StyledCheckBox(String label, String namespace) {
        this(label, namespace, null);
    }

    public StyledCheckBox(String label, Image icon) {
        this(label, null, icon);
    }

    public StyledCheckBox(String label, String namespace, Image icon) {
        this.setText(label);
        this.setCheckMarkIcon(icon);
        if(namespace != null) this.setNamespace(namespace);

        tlm.addThemeChangeListener(t -> {
            if(this.namespace != null) {
                setBackground       (t.getColor(new Color(215, 215, 215), this.namespace + ".checkbox.background","General.checkbox.background"));
                setForeground       (t.getColor(Color.BLACK, this.namespace + ".checkbox.foreground","General.checkbox.foreground","General.foreground"));
                setBorder           (t.getColor(new Color(200, 200, 200), this.namespace + ".checkbox.border.color","General.checkbox.border.color"), Math.max(t.getInteger(1,this.namespace + ".checkbox.border.thickness", "General.checkbox.border.thickness"),0));
                setRolloverColor    (t.getColor(new Color(200, 202, 205), this.namespace + ".checkbox.hover.background","General.checkbox.hover.background"));
                setPressedColor     (t.getColor(Color.WHITE, this.namespace + ".checkbox.pressed.background","General.checkbox.pressed.background"));
                setFont(t.getFont(this.namespace+".checkbox","General.checkbox","General"));
                setCheckMarkIcon(Commons.getScaledIcon("checkmark", 16, 16));
            } else {
                setBackground       (t.getColor(new Color(215, 215, 215), "General.checkbox.background"));
                setForeground       (t.getColor(Color.BLACK, "General.checkbox.foreground","General.foreground"));
                setBorder(t.getColor(new Color(200, 200, 200), "General.checkbox.border.color"),Math.max(t.getInteger(1,"General.checkbox.border.thickness"),0));
                setRolloverColor    (t.getColor(new Color(200, 202, 205), "General.checkbox.hover.background"));
                setPressedColor     (t.getColor(Color.WHITE, "General.checkbox.pressed.background"));
                setFont(t.getFont("General.checkbox","General"));
                setCheckMarkIcon(Commons.getScaledIcon("checkmark", 16, 16));
            }
            this.setCheckBoxSize((int) (16 * ScalableGraphics2D.SCALE_FACTOR));
            this.setIconTextGap((int) (4 * ScalableGraphics2D.SCALE_FACTOR));
            revalidate();
        });

    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }
}
