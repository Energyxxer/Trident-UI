package com.energyxxer.trident.ui.styledcomponents;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.ui.theme.Theme;
import com.energyxxer.trident.ui.theme.change.ThemeChangeListener;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;

import javax.swing.*;
import java.awt.*;

/**
 * Created by User on 12/15/2016.
 */
public class StyledLabel extends JLabel implements ThemeChangeListener {
    private String namespace = null;

    private int style = 0;
    private int size = 12;

    private String icon = null;

    private boolean changeable = true;

    private Theme theme;

    public StyledLabel(String text, ThemeListenerManager tlm) {
        super(text);
        setNamespaceInit(null, tlm);
    }
    //New

    public StyledLabel(String text, String namespace, ThemeListenerManager tlm) {
        super(text);
        setNamespaceInit(namespace, tlm);
    }

    private void setNamespaceInit(String namespace, ThemeListenerManager tlm) {
        setNamespace(namespace);

        if(tlm != null) {
            tlm.addThemeChangeListener(this);
        } else {
            ThemeChangeListener.addThemeChangeListener(this);
        }
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
        this.update();
        revalidate();
        repaint();
    }

    public int getFontSize() {
        return size;
    }

    public void setFontSize(int size) {
        this.size = size;
        this.update();
        revalidate();
        repaint();
    }

    public void setIconName(String icon) {
        this.icon = icon;
        this.update();
        revalidate();
        repaint();
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public void themeChanged(Theme t0) {
        this.theme = t0;
        this.update();
    }

    public void setTextThemeDriven(boolean changeable) {
        this.changeable = changeable;
    }

    private void update() {
        Theme t = this.theme;

        if (this.namespace != null) {
            setForeground(t.getColor(Color.BLACK, this.namespace + ".label.foreground","General.label.foreground","General.foreground"));
            Font font = t.getFont(this.namespace+".label","General.label","General");
            font = font.deriveFont(font.getStyle() | style);
            setFont(font);
            if(changeable) this.setText(t.getString(this.namespace + ".label.text","default:" + getText()));
        } else {
            setForeground(t.getColor(Color.BLACK, "General.label.foreground","General.foreground"));
            Font font = t.getFont("General.label","General");
            font = font.deriveFont(font.getStyle() | style);
            setFont(font);
        }
        if (icon != null) {
            this.setIcon(new ImageIcon(Commons.getScaledIcon(icon, 16, 16)));
        } else {
            this.setIcon(null);
        }

        this.setPreferredSize(null);
    }
}
