package com.energyxxer.xswing;

import com.energyxxer.util.logger.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ComponentResizer {
	// Constant for the distance from a component's edge for dragging to work
	public static final int DIST = 5;

	// The component that will be resized; this will also have mouse events added to it
	private JComponent resizable = null;
	// Optionally, a dialog to be resized AND moved. Mouse listeners will still be added to `resizable` though.
	private JDialog dialog = null;

	// Whether this resizer is enabled or not.
	private boolean enabled = true;

	// Whether resizing is enabled for the four edges: top, left, bottom, right
	private boolean[] edges = {
			false, false, false, false
	};

	// The edge index for the edge currently being dragged, on both axes
	private int xDraggedEdge = -1;
	private int yDraggedEdge = -1;

	// Contains the coordinates for the positive-most corner of the component being dragged, used for dialog positioning
	private Point positiveCorner = new Point();
	
	public ComponentResizer() {
	}
	
	public ComponentResizer(JComponent component) {
		setResizable(component);
	}

	public ComponentResizer(JComponent resizable, JDialog dialog) {
		setResizable(resizable);
		this.dialog = dialog;
	}

	/**
	 * Gives the specified component a resizable functionality.
	 * */
	public void setResizable(JComponent component) {
		resizable = component;
		
		MouseAdapter adapter = new MouseAdapter() {

			@Override
	        public void mouseMoved(MouseEvent me) {
				if(!enabled) return;
                component.setCursor(getCursor(me));
	        }

	        @Override
	        public void mouseExited(MouseEvent me) {
				component.setCursor(Cursor.getDefaultCursor());
	        }

			@Override
			public void mousePressed(MouseEvent e) {
				if(!enabled || resizable == null) return;
				Rectangle[] areas = getEdgeAreas();

				for(int i = 0; i < 4; i++) {
					if(areas[i].contains(e.getPoint())) {
						if(i % 2 == 0) {
							yDraggedEdge = i;
						} else {
							xDraggedEdge = i;
						}
						Debug.log("Start dragging " + i);
					}
				}

				positiveCorner.x = component.getLocationOnScreen().x + component.getWidth();
				positiveCorner.y = component.getLocationOnScreen().y + component.getHeight();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				xDraggedEdge = yDraggedEdge = -1;
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if(!enabled) return;

				Rectangle moveTo = new Rectangle(component.getLocationOnScreen(), component.getSize());

				if(yDraggedEdge > -1) {
					Dimension newDim = new Dimension(
							moveTo.width,
							MathUtil.clamp(
									(yDraggedEdge == 0 ? (positiveCorner.y - e.getYOnScreen())
											: (e.getYOnScreen() - moveTo.y)
									),
									component.getMinimumSize().height,
									component.getMaximumSize().height
							));

					if(dialog != null && yDraggedEdge == 0) moveTo.setLocation(moveTo.x, Math.min(e.getYOnScreen(), positiveCorner.y - dialog.getMinimumSize().height));
					moveTo.setSize(newDim);
				}

				if(xDraggedEdge > -1) {
					Dimension newDim = new Dimension(
							MathUtil.clamp(
									(xDraggedEdge == 1 ? (positiveCorner.x - e.getXOnScreen())
											: (e.getXOnScreen() - moveTo.x)
									),
									component.getMinimumSize().width,
									component.getMaximumSize().width
							), moveTo.height);
					if(dialog != null && xDraggedEdge == 1) moveTo.setLocation(Math.min(e.getXOnScreen(), positiveCorner.x - dialog.getMinimumSize().width), moveTo.y);
					moveTo.setSize(newDim);
				}
				if(dialog != null) {
					dialog.setBounds(moveTo);
				}
				component.setPreferredSize(moveTo.getSize());
				component.revalidate();
				component.repaint();
			}
		};

		component.addMouseListener(adapter);
		component.addMouseMotionListener(adapter);
		
	}
	
	public void removeResizable() {
		resizable = null;
	}
	
	/**
	 * Allows/disallows component resizing in all directions.
	 * */
	public void setResizable(boolean resizable) {
		edges[0] = edges[1] = edges[2] = edges[3] = resizable;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Allows/disallows component resizing in given directions.
	 * */
	public void setResizable(boolean top, boolean left, boolean bottom, boolean right) {
		edges[0] = top;
		edges[1] = left;
		edges[2] = bottom;
		edges[3] = right;
	}
	
	/**
	 * Returns an array of rectangles which contains the sections the mouse must be inside to resize.
	 * */
	private Rectangle[] getEdgeAreas() {
		if(resizable != null) {
			int w = resizable.getWidth();
			int h = resizable.getHeight();
			return new Rectangle[] {
				(edges[0]) ? new Rectangle(0, 0, w, DIST) : new Rectangle(),
				(edges[1]) ? new Rectangle(0, 0, DIST, h) : new Rectangle(),
				(edges[2]) ? new Rectangle(0, h-DIST, w, DIST) : new Rectangle(),
				(edges[3]) ? new Rectangle(w-DIST, 0, DIST, h) : new Rectangle()
			};
		} else return null;
	}

	/**
	 * Retrieves the resize cursor to use for the given mouse event.
	 * */
	private Cursor getCursor(MouseEvent me) {

		Rectangle[] areas = getEdgeAreas();
        Boolean[] intersections = new Boolean[] { false, false, false, false };

        for(int i = 0; i < 4; i++) {
        	if(areas[i].contains(me.getPoint())) {
        		intersections[i] = true;
        	}
        }
        
        int direction = -1;

        if(intersections[SwingConstants.TOP-1]) direction = SwingConstants.NORTH;
        if(intersections[SwingConstants.LEFT-1]) direction = SwingConstants.WEST;
        if(intersections[SwingConstants.BOTTOM-1]) direction = SwingConstants.SOUTH;
        if(intersections[SwingConstants.RIGHT-1]) direction = SwingConstants.EAST;
        if(intersections[SwingConstants.TOP-1] && intersections[SwingConstants.LEFT-1]) direction = SwingConstants.NORTH_WEST;
        if(intersections[SwingConstants.TOP-1] && intersections[SwingConstants.RIGHT-1]) direction = SwingConstants.NORTH_EAST;
        if(intersections[SwingConstants.BOTTOM-1] && intersections[SwingConstants.LEFT-1]) direction = SwingConstants.SOUTH_WEST;
        if(intersections[SwingConstants.BOTTOM-1] && intersections[SwingConstants.RIGHT-1]) direction = SwingConstants.SOUTH_EAST;
        
        int cursors[] = {
        	Cursor.N_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR, Cursor.E_RESIZE_CURSOR,
        	Cursor.SE_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR, Cursor.SW_RESIZE_CURSOR,
        	Cursor.W_RESIZE_CURSOR, Cursor.NW_RESIZE_CURSOR
        };
        
        if(direction < 0) {
        	return Cursor.getDefaultCursor();
        }
        return Cursor.getPredefinedCursor(cursors[direction-1]);
    }
}
