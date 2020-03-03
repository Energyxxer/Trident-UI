package com.energyxxer.trident.ui;

import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.ScalableGraphics2D;

import javax.swing.*;
import java.awt.*;

/**
 * It's literally just a line.
 */
public class ToolbarSeparator extends JComponent {

	private Color left = Color.BLACK;
	private Color right = Color.WHITE;

	public ToolbarSeparator(ThemeListenerManager tlm) {

		this.setOpaque(true);
		this.setBackground(new Color(0,0,0,0));

		tlm.addThemeChangeListener(t -> {
			this.setPreferredSize(new ScalableDimension(15, 25));
			left = t.getColor(new Color(150, 150, 150), "Toolbar.separator.dark");
			right = t.getColor(new Color(235, 235, 235), "Toolbar.separator.light");
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int x = this.getWidth()/2-1;
		g = new ScalableGraphics2D(g);

		g.setColor(left);
		g.fillRect(x,2,1,this.getHeight()-4);
		x++;
		g.setColor(right);
		g.fillRect(x,2,1,this.getHeight()-4);

		g.dispose();
	}
}
