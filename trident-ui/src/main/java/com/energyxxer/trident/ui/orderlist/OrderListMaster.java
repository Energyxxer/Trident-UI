package com.energyxxer.trident.ui.orderlist;

import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.HintStylizer;
import com.energyxxer.trident.ui.explorer.base.StyleProvider;
import com.energyxxer.trident.ui.theme.change.ThemeListenerManager;
import com.energyxxer.xswing.hints.TextHint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.*;

public class OrderListMaster extends JComponent implements MouseListener, MouseMotionListener, StyleProvider {
    ArrayList<OrderListElement> children = new ArrayList<>();
    private int y = 0;

    private OrderListElement rolloverElement = null;
    private OrderListElement selectedElement = null;

    private HashMap<String, Integer> styleNumbers = new HashMap<>();
    private HashMap<String, Color> colors = new HashMap<>();
    private String selectionStyle = "FULL";
    private int selectionLineThickness = 2;
    private int rowHeight = 20;

    Point dragPoint = null;
    float dragPivot = -1;
    OrderListElement draggedElement = null;

    private TextHint hint = TridentWindow.hintManager.createTextHint("a");

    private ThemeListenerManager tlm = new ThemeListenerManager();
    private String rolloverText = null;

    public OrderListMaster() {
        tlm.addThemeChangeListener(t -> {

            colors.put("background", t.getColor(Color.WHITE, "OrderList.background"));
            colors.put("item.background", t.getColor(new Color(0, 0, 0, 0), "OrderList.item.background"));
            colors.put("item.foreground", t.getColor(Color.BLACK, "OrderList.item.foreground", "General.foreground"));
            colors.put("item.selected.background", t.getColor(Color.BLUE, "OrderList.item.selected.background", "OrderList.item.background"));
            colors.put("item.selected.foreground", t.getColor(Color.BLACK, "OrderList.item.selected.foreground", "OrderList.item.hover.foreground", "OrderList.item.foreground", "General.foreground"));
            colors.put("item.rollover.background", t.getColor(new Color(0, 0, 0, 0), "OrderList.item.hover.background", "OrderList.item.background"));
            colors.put("item.rollover.foreground", t.getColor(Color.BLACK, "OrderList.item.hover.foreground", "OrderList.item.foreground", "General.foreground"));

            rowHeight = Math.max(t.getInteger(20, "OrderList.item.height"), 1);

            selectionStyle = t.getString("OrderList.item.selectionStyle", "default:FULL");
            selectionLineThickness = Math.max(t.getInteger(2, "OrderList.item.selectionLineThickness"), 0);

            styleNumbers.put("button.border.thickness", Math.max(t.getInteger(1,"OrderList.button.border.thickness"),1));
            styleNumbers.put("button.rollover.border.thickness", Math.max(t.getInteger(1,"OrderList.button.hover.border.thickness", "OrderList.button.border.thickness"),1));
            styleNumbers.put("button.pressed.border.thickness", Math.max(t.getInteger(1,"OrderList.button.pressed.border.thickness", "OrderList.button.hover.border.thickness", "OrderList.button.border.thickness"),1));

            styleNumbers.put("checkbox.border.thickness", Math.max(t.getInteger(1,"OrderList.checkbox.border.thickness"),1));
            styleNumbers.put("checkbox.rollover.border.thickness", Math.max(t.getInteger(1,"OrderList.checkbox.hover.border.thickness", "OrderList.checkbox.border.thickness"),1));
            styleNumbers.put("checkbox.pressed.border.thickness", Math.max(t.getInteger(1,"OrderList.checkbox.pressed.border.thickness", "OrderList.checkbox.hover.border.thickness", "OrderList.checkbox.border.thickness"),1));

            this.setFont(t.getFont("OrderList.item", "General"));
            
            colors.put("button.background", t.getColor(Color.GRAY, "OrderList.button.background"));
            colors.put("button.rollover.background", t.getColor(Color.GRAY, "OrderList.button.hover.background", "OrderList.button.background"));
            colors.put("button.pressed.background", t.getColor(Color.GRAY, "OrderList.button.pressed.background","OrderList.button.hover.background", "OrderList.button.background"));
            colors.put("button.border.color", t.getColor(Color.BLACK, "OrderList.button.border.color"));
            colors.put("button.rollover.border.color", t.getColor(Color.BLACK, "OrderList.button.hover.border.color", "OrderList.button.border.color"));
            colors.put("button.pressed.border.color", t.getColor(Color.BLACK, "OrderList.button.pressed.border.color", "OrderList.button.hover.border.color", "OrderList.button.border.color"));

            colors.put("checkbox.background", t.getColor(Color.GRAY, "OrderList.checkbox.background"));
            colors.put("checkbox.rollover.background", t.getColor(Color.GRAY, "OrderList.checkbox.hover.background", "OrderList.checkbox.background"));
            colors.put("checkbox.pressed.background", t.getColor(Color.GRAY, "OrderList.checkbox.pressed.background","OrderList.checkbox.hover.background", "OrderList.checkbox.background"));
            colors.put("checkbox.border.color", t.getColor(Color.BLACK, "OrderList.checkbox.border.color"));
            colors.put("checkbox.rollover.border.color", t.getColor(Color.BLACK, "OrderList.checkbox.hover.border.color", "OrderList.checkbox.border.color"));
            colors.put("checkbox.pressed.border.color", t.getColor(Color.BLACK, "OrderList.checkbox.pressed.border.color", "OrderList.checkbox.hover.border.color", "OrderList.checkbox.border.color"));

            children.forEach(e -> e.themeChanged(t));
        });

        hint.setOutDelay(1);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(colors.get("background"));
        g.fillRect(0,0,this.getWidth(), this.getHeight());

        this.y = 0;

        int draggedY = -1;

        ArrayList<OrderListElement> toRender = new ArrayList<>(children);

        for(OrderListElement element : toRender) {
            if(element != draggedElement) {
                element.render(g.create());
            } else draggedY = y;
            this.y += element.getHeight();
        }

        Dimension newSize = new Dimension(this.getWidth(), this.y);

        if(!newSize.equals(this.getPreferredSize())) {
            this.setPreferredSize(newSize);
            this.getParent().revalidate();
        }

        if(draggedElement != null) {
            this.y = draggedY;
            draggedElement.render(g.create());
        }
    }

    public void addItem(OrderListElement item) {
        this.children.add(item);
    }

    public void preAddItem(OrderListElement item) {
        this.children.add(0, item);
    }

    int getOffsetY() {
        return y;
    }

    void setOffsetY(int y) {
        this.y = y;
    }

    String getSelectionStyle() {
        return selectionStyle;
    }

    int getSelectionLineThickness() {
        return selectionLineThickness;
    }

    public Map<String, Color> getColors() {
        return colors;
    }

    public Map<String, Integer> getStyleNumbers() {
        return styleNumbers;
    }

    private OrderListElement getElementAtMousePos(MouseEvent e) {
        int y = 0;
        for(OrderListElement element : children) {
            y += element.getHeight();
            if(e.getY() < y) return element;
        }
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        OrderListElement element = getElementAtMousePos(e);
        if(element != null) element.mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        OrderListElement element = getElementAtMousePos(e);

        if(e.getButton() == MouseEvent.BUTTON1 && element != null && element.select(e)) {
            dragPoint = e.getPoint();
            draggedElement = element;
            dragPivot = -1;
        }
        if(element == null) {
            selectElement(null);
            repaint();
            return;
        }

        int y = 0;
        for(OrderListElement elem : children) {
            int h = elem.getHeight();
            if(e.getY() < y + h) {
                dragPivot = (float) (e.getY()-y)/h;
                break;
            }
            y += h;
        }

        if(e.getButton() == MouseEvent.BUTTON1 && element.select(e)) selectElement(element);

        element.mousePressed(e);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragPoint = null;
        draggedElement = null;
        dragPivot = -1;
        OrderListElement element = getElementAtMousePos(e);
        if(element != null) element.mouseReleased(e);
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if(rolloverElement != null) {
            rolloverElement.setRollover(false);
            rolloverElement.mouseExited(e);
            rolloverElement = null;
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(draggedElement != null) {
            draggedElement.mouseDragged(e);
            dragPoint = e.getPoint();
            int y = 0;
            for (int i = 0; i < children.size(); i++) {
                OrderListElement element = children.get(i);
                int h = element.getHeight();
                int center = (int) (e.getY() + (0.5 - dragPivot * h));
                if (center >= y && center < y + h) {
                    children.remove(draggedElement);
                    if (center <= y + h / 2) {
                        children.add(i, draggedElement);
                    } else {
                        children.add(Math.min(i + 1, children.size()), draggedElement);
                    }
                    break;
                }
                y += h;
            }
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        OrderListElement element = getElementAtMousePos(e);
        if(rolloverElement != null) {
            rolloverElement.setRollover(false);
            if(rolloverElement != element) {
                rolloverElement.mouseExited(e);
            }
        }
        if(element != null) {
            element.setRollover(true);
            if(rolloverElement != element) {
                element.mouseEntered(e);
            }
            String text = element.getToolTipText();
            this.rolloverText = text;
            if(text != null && (rolloverElement != null || !hint.isShowing()) && (!Objects.equals(hint.getText(), text) || !hint.isShowing())) {
                hint.setText(text);
                HintStylizer.style(hint);
                hint.show(new Point(this.getLocationOnScreen().x + element.getToolTipLocation().x, this.getLocationOnScreen().y + element.getLastRecordedOffset() + element.getToolTipLocation().y), () -> rolloverElement == element && Objects.equals(hint.getText(), rolloverText));
            }
            element.mouseMoved(e);
        } else {
            rolloverText = null;
        }
        repaint();
        rolloverElement = element;
    }

    public void removeElement(OrderListElement element) {
        children.remove(element);
        if(selectedElement == element) selectedElement = null;
        if(rolloverElement == element) rolloverElement = null;
        repaint();
    }

    public void removeAllElements() {
        children.clear();
        selectedElement = null;
        rolloverElement = null;
        repaint();
    }

    public void selectElement(OrderListElement element) {
        if(selectedElement != null) {
            selectedElement.setSelected(false);
        }
        selectedElement = element;
        if(element != null) {
            element.setSelected(true);
        }
        repaint();
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void moveUp(OrderListElement element) {
        int index = children.indexOf(element);
        if(index <= 0) return;
        children.remove(index);
        children.add(index-1, element);
        selectElement(element);
    }

    public void moveDown(OrderListElement element) {
        int index = children.indexOf(element);
        if(index < 0) return;
        if(index >= children.size()-1) return;
        children.remove(index);
        children.add(index+1, element);
        selectElement(element);
    }

    public List<OrderListElement> getAllElements() {
        return children;
    }
}
