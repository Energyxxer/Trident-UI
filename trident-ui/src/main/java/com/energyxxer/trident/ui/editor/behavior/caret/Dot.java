package com.energyxxer.trident.ui.editor.behavior.caret;

import com.energyxxer.trident.global.Preferences;
import com.energyxxer.trident.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.logger.Debug;

import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;

import static com.energyxxer.trident.ui.editor.behavior.AdvancedEditor.isPlatformControlDown;

/**
 * Created by User on 1/7/2017.
 */
public class Dot {
    private AdvancedEditor component;
    int index = 0;
    int mark = 0;
    int x = 0;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;

    public static Preferences.SettingPref<Boolean> SMART_KEYS_HOME = new Preferences.SettingPref<>("settings.editor.smart_keys.home", true, Boolean::parseBoolean);
    public static Preferences.SettingPref<Boolean> SMART_KEYS_INDENT = new Preferences.SettingPref<>("settings.editor.smart_keys.indent", true, Boolean::parseBoolean);
    public static Preferences.SettingPref<Boolean> SMART_KEYS_BRACES = new Preferences.SettingPref<>("settings.editor.smart_keys.braces", true, Boolean::parseBoolean);
    public static Preferences.SettingPref<Boolean> SMART_KEYS_QUOTES = new Preferences.SettingPref<>("settings.editor.smart_keys.quotes", false, Boolean::parseBoolean);


    public Dot(int index, AdvancedEditor component) {
        this(index, index, component);
    }

    public Dot(int index, int mark, AdvancedEditor component) {
        this.component = component;
        this.index = index;
        this.mark = mark;
        updateX();
    }

    void updateX() {
        try {
            Rectangle view = component.modelToView(index);
            if(view != null) this.x = view.x;
        } catch (BadLocationException x) {
            x.printStackTrace();
        }
    }

    public boolean handleEvent(KeyEvent e) {
        int key = e.getKeyCode();

        boolean actionPerformed = false;
        int nextPos = 0;
        boolean doUpdateX = false;

        if(key == KeyEvent.VK_LEFT) {
            if(!isPoint() && !e.isShiftDown()) {
                nextPos = Math.min(index, mark);
                if(isPlatformControlDown(e)) nextPos = getPositionBeforeWord();
            } else nextPos = (isPlatformControlDown(e)) ? getPositionBeforeWord() : getPositionBefore();
            doUpdateX = true;
            actionPerformed = true;
        } else if(key == KeyEvent.VK_RIGHT) {
            if(!isPoint() && !e.isShiftDown()) {
                nextPos = Math.max(index, mark);
                if(isPlatformControlDown(e)) nextPos = getPositionAfterWord();
            } else nextPos = (isPlatformControlDown(e)) ? getPositionAfterWord() : getPositionAfter();
            doUpdateX = true;
            actionPerformed = true;
        } else if(key == KeyEvent.VK_UP) {
            if(isPlatformControlDown(e)) {
                e.consume();
                return false;
            }
            nextPos = getPositionAbove();
            if(nextPos < 0) {
                nextPos = 0;
                doUpdateX = true;
            }
            actionPerformed = true;
        } else if(key == KeyEvent.VK_DOWN) {
            if(isPlatformControlDown(e)) {
                e.consume();
                return false;
            }
            nextPos = getPositionBelow();
            if(nextPos < 0) {
                nextPos = component.getDocument().getLength();
                doUpdateX = true;
            }
            actionPerformed = true;
        } else if(key == KeyEvent.VK_HOME) {
            if (!isPlatformControlDown(e)) {
                nextPos = getRowHome();
            }
            doUpdateX = true;
            actionPerformed = true;
        } else if(key == KeyEvent.VK_END) {
            if(isPlatformControlDown(e)) nextPos = component.getDocument().getLength();
            else nextPos = getRowEnd();
            doUpdateX = true;
            actionPerformed = true;
        }

        if(actionPerformed) {
            e.consume();
            index = nextPos;
            if(!e.isShiftDown()) mark = nextPos;
            if(doUpdateX) updateX();
        }
        return actionPerformed;
    }

    public void deselect() {
        mark = index;
    }

    public int getPositionBefore() {
        return Math.max(0, Math.min(component.getDocument().getLength(), index-1));
    }

    public int getPositionAfter() {
        return Math.max(0, Math.min(component.getDocument().getLength(), index+1));
    }

    public int getPositionAbove() {
        try {
            return Utilities.getPositionAbove(component, index, x);
        } catch(BadLocationException ble) {
            Debug.log(ble.getMessage(), Debug.MessageType.ERROR);
        }
        return index;
    }

    public int getPositionBelow() {
        try {
            return Utilities.getPositionBelow(component, index, x);
        } catch(BadLocationException ble) {
            Debug.log(ble.getMessage(), Debug.MessageType.ERROR);
        }
        return index;
    }

    public int getPositionBeforeWord() {
        try {
            return Math.max(component.getPreviousWord(index), Math.max(0,getRowStart()-1));
        } catch(BadLocationException ble) {
            Debug.log(ble.getMessage(), Debug.MessageType.ERROR);
        }
        return 0;
    }

    public int getPositionAfterWord() {
        try {
            int pos = component.getNextWord(index);
            int rowEnd = getRowEnd();
            return (index == rowEnd) ? pos : Math.min(pos, rowEnd);
        } catch(BadLocationException ble) {
            Debug.log(ble.getMessage(), Debug.MessageType.ERROR);
        }
        return component.getDocument().getLength();
    }

    public int getRowHome() {
        int rowStart = getRowStart();
        if(!SMART_KEYS_HOME.get()) return rowStart;
        int rowContentStart = getRowContentStart();
        if(index == rowStart) return rowContentStart;
        if(index <= rowContentStart) return rowStart;
        return rowContentStart;
    }

    public int getWordStart() {
        try {
            return Math.max(component.getWordStart(index), Math.max(0,getRowStart()-1));
        } catch(BadLocationException ble) {
            Debug.log(ble.getMessage(), Debug.MessageType.ERROR);
        }
        return 0;
    }

    public int getWordEnd() {
        try {
            int pos = component.getWordEnd(index);
            int rowEnd = getRowEnd();
            return (index == rowEnd) ? pos : Math.min(pos, rowEnd);
        } catch(BadLocationException ble) {
            Debug.log(ble.getMessage(), Debug.MessageType.ERROR);
        }
        return component.getDocument().getLength();
    }

    public int getRowStart() {
        try {
            return Utilities.getRowStart(component, index);
        } catch(BadLocationException ble) {
            Debug.log(ble.getMessage(), Debug.MessageType.ERROR);
        }
        return index;
    }

    public int getRowEnd() {
        try {
            return Utilities.getRowEnd(component, index);
        } catch(BadLocationException ble) {
            Debug.log(ble.getMessage(), Debug.MessageType.ERROR);
        }
        return index;
    }

    public int getRowContentStart() {
        try {
            return component.getNextNonWhitespace(getRowStart());
        } catch(BadLocationException ble) {
            Debug.log(ble.getMessage(), Debug.MessageType.ERROR);
        }
        return index;
    }

    public boolean isInIndentation() {
        return this.index >= getRowStart() && this.index <= getRowContentStart();
    }

    public StringBounds getBounds() {
        return new StringBounds(component.getLocationForOffset(index),component.getLocationForOffset(mark));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dot dot = (Dot) o;
        return index == dot.index &&
                mark == dot.mark;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, mark);
    }

    @Override
    public String toString() {
        return "Dot{" +
                "index=" + index +
                ", mark=" + mark +
                ", x=" + x +
                '}';
    }

    public int getMin() {
        return Math.min(this.index, this.mark);
    }

    public int getMax() {
        return Math.max(this.index, this.mark);
    }

    public boolean isPoint() {
        return this.index == this.mark;
    }

    public boolean intersects(Dot other) {
        if(other.getMin() < this.getMin()) {
            return other.intersects(this);
        }
        if(this.mark == other.mark && (this.isPoint() != other.isPoint())) return false;
        return (other.getMin() <= this.getMax());
    }

    public void absorb(Dot other) {
        int newMin = Math.min(this.getMin(), other.getMin());
        int newMax = Math.max(this.getMax(), other.getMax());

        if(other.mark <= newMin || this.mark <= newMin) {
            this.mark = newMin;
            this.index = newMax;
        } else/* if(other.mark >= newMax || this.mark >= newMax)*/ {
            this.mark = newMax;
            this.index = newMin;
        }
        Debug.log("* The dot absorbs the artifact");
    }

    public boolean contains(int index) {
        return this.getMin() <= index && index < this.getMax();
    }

}
