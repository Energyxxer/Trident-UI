package com.energyxxer.trident.ui;

import com.energyxxer.trident.global.Commons;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.Constant;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.ScalableGraphics2D;
import com.energyxxer.xswing.hints.Hint;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Represents a single button in the toolbar.
 */
public class ToolbarButton extends JButton implements MouseListener, MouseMotionListener, ActionListener {

    private static final int MARGIN = 1;
    private static final int BORDER_THICKNESS = 1;
    //public static final int SIZE = 25;

    private Color background = Color.GRAY;
    private Color rolloverBackground = Color.GRAY;
    private Color pressedBackground = Color.GRAY;
    private Color border = Color.BLACK;
    private Color rolloverBorder = Color.BLACK;
    private Color pressedBorder = Color.BLACK;

	private String hintText = "";
	private Constant preferredHintPos = Hint.BELOW;

	private boolean rollover = false;

	private String icon;
	private boolean iconDirty = false;
	private int iconSize;

	private boolean sizeValid = false;

	private int stringWidth = 0;
	private int fontHeight = 0;
	private int fontAscent = 0;
	private int fontDescent = 0;

	public ToolbarButton(String icon, ThemeListenerManager tlm) {
		this.setContentAreaFilled(false);
		this.setOpaque(false);
		this.setBackground(new Color(0,0,0,0));

		this.icon = icon;

		//this.setPreferredSize(new ScalableDimension(25, 25));
		this.setBorder(BorderFactory.createEmptyBorder());

		tlm.addThemeChangeListener(t -> {
            this.setPreferredSize(new ScalableDimension(25,25));
            this.background = t.getColor(Color.GRAY, "Toolbar.button.background", "General.button.background");
            this.rolloverBackground = t.getColor(Color.GRAY, "Toolbar.button.hover.background", "General.button.hover.background", "Toolbar.button.background", "General.button.background");
            this.pressedBackground = t.getColor(Color.GRAY, "Toolbar.button.pressed.background", "General.button.pressed.background", "Toolbar.button.hover.background", "General.button.hover.background", "Toolbar.button.background", "General.button.background");
            this.border = t.getColor(Color.BLACK, "Toolbar.button.border.color", "General.button.border.color");
            this.rolloverBorder = t.getColor(Color.BLACK, "Toolbar.button.hover.border.color", "General.button.hover.border.color", "Toolbar.button.border.color", "General.button.border.color");
            this.pressedBorder = t.getColor(Color.BLACK, "Toolbar.button.pressed.border.color", "General.button.pressed.border.color", "Toolbar.button.hover.border.color", "General.button.hover.border.color", "Toolbar.button.border.color", "General.button.border.color");

			this.setForeground(t.getColor(Color.BLACK, "Toolbar.button.foreground", "General.button.foreground", "General.foreground"));
			this.setFont(t.getFont( "Toolbar.button", "General.button", "General"));

			iconDirty = true;
			updateSize();
        });

		this.setFocusPainted(false);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addActionListener(this);

		updateSize();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		Composite previousComposite = g2.getComposite();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(getModel().isPressed() ? pressedBackground : (getModel().isRollover() || hasFocus()) ? rolloverBackground : background);
        g.fillRect(MARGIN+BORDER_THICKNESS,MARGIN+BORDER_THICKNESS,this.getWidth()-2*MARGIN-2*BORDER_THICKNESS,this.getHeight()-2*MARGIN-2*BORDER_THICKNESS);
        g.setColor(getModel().isPressed() ? pressedBorder : (getModel().isRollover() || hasFocus()) ? rolloverBorder : border);
        g.fillRect(MARGIN,MARGIN,this.getWidth()-2*MARGIN-BORDER_THICKNESS,BORDER_THICKNESS);
        g.fillRect(this.getWidth()-MARGIN-BORDER_THICKNESS,MARGIN,BORDER_THICKNESS,this.getHeight()-2*MARGIN-BORDER_THICKNESS);
        g.fillRect(MARGIN+BORDER_THICKNESS,this.getHeight()-MARGIN-BORDER_THICKNESS,this.getWidth()-2*MARGIN-BORDER_THICKNESS,BORDER_THICKNESS);
        g.fillRect(MARGIN,MARGIN+BORDER_THICKNESS,BORDER_THICKNESS,this.getHeight()-2*MARGIN-BORDER_THICKNESS);

		g2.setComposite(previousComposite);

		super.paintComponent(g);

		g = new ScalableGraphics2D(g);

		if(this.getText() != null && !this.getText().isEmpty()) {
			FontMetrics fm = g.getFontMetrics();
			stringWidth = fm.stringWidth(this.getText());
			fontHeight = fm.getHeight();
			fontAscent = fm.getAscent();
			fontDescent = fm.getDescent();
		} else {
			stringWidth = 0;
		}

		if(!sizeValid) {
			updateSize();
			this.getParent().revalidate();
			this.repaint();
		}
	}

	public String getHintText() {
		return hintText;
	}

	public ToolbarButton setHintText(String hintText) {
		this.hintText = hintText;
		return this;
	}

	public Constant getPreferredHintPos() {
		return preferredHintPos;
	}

	public void setPreferredHintPos(Constant preferredPos) {
		this.preferredHintPos = preferredPos;
	}

    @Override
	public void setText(String text) {
		super.setText(text);
		updateSize();
	}

	public ToolbarButton changeText(String text) {
		setText(text);
		return this;
	}

	private Dimension getBestSize() {
		int width = 0;
		int height = 0;

		if(icon != null) width += 16;
		width += 9;

		sizeValid = true;

		String text = this.getText();
		if(text != null && text.length() > 0) {
			width += 6;
			width += stringWidth;

			if(stringWidth <= 0) sizeValid = false;

			height += 25;
		} else {
			height += 16;
			height += 9;
		}

		return new Dimension(width, height);
	}

	private void updateSize() {
		//First, resize icon.
		if(this.icon != null) {
			this.setIcon(new ImageIcon(Commons.getScaledIcon(icon, 16, 16)));
		} else {
			this.setIcon(null);
		}

		//text is probably already resized

		this.setPreferredSize(new ScalableDimension(getBestSize()));
		updateIcon();
	}

	private void updateIcon() {
	}

	private static Dimension adjustSize(Dimension size) {
		size.width = Math.max(size.width,25);
		size.height = Math.max(size.height,25);
		size.height /= 25;
		size.height *= 25;
		return new ScalableDimension(size);
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(Math.max(width,25), Math.max(height,25));
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		rollover = true;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		rollover = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		rollover = true;
		TextHint hint = TridentWindow.toolbar.hint;
		if(!hint.isShowing()) {
			hint.setText(hintText);
			hint.setPreferredPos(this.preferredHintPos);
			Point point = this.getLocationOnScreen();
			point.x += this.getWidth()/2;
			point.y += this.getHeight()/2;
			HintStylizer.style(hint);
			hint.show(point, () -> rollover && this.isShowing());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}
}
