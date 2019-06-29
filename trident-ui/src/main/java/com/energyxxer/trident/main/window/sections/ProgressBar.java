package com.energyxxer.trident.main.window.sections;

import com.energyxxer.trident.ui.theme.Theme;
import com.energyxxer.trident.ui.theme.change.ThemeChangeListener;

import javax.swing.*;
import java.awt.*;

public class ProgressBar extends JPanel implements ThemeChangeListener {
    private float progress = 0.5f;

    private int height = 5;

    private float scrollingProgress = 0.0f;

    public ProgressBar() {
        this.setOpaque(false);
        ThemeChangeListener.addThemeChangeListener(this);
    }

    @Override
    public void themeChanged(Theme t) {
        this.setBackground(t.getColor(Color.BLACK, "General.progressBar.background"));
        this.setForeground(t.getColor(Color.WHITE, "General.progressBar.foreground"));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = this.getWidth();
        int y = (this.getHeight() - height) / 2;

        g.setColor(this.getBackground());
        g.fillRect(0, y, width, height);

        g.setColor(this.getForeground());

        if(progress == -1) {
            scrollingProgress = ((float)(System.currentTimeMillis()%1000)/500)-0.5f;
            g.fillRect((int)(width * scrollingProgress), y, width / 2, height);
            repaint();
        } else {
            g.fillRect(0, y, (int)(width * progress), height);
        }
    }

    public void setProgress(float progress) {
        this.progress = progress;
        repaint();
    }
}
