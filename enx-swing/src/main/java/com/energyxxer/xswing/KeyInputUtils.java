package com.energyxxer.xswing;

import java.awt.event.KeyEvent;

public class KeyInputUtils {
    private static int doublePressSpeed = 300;
    private static long timeKeyDown = 0;
    public static int lastKeyPressedCode;

    public static boolean isDoublePress(KeyEvent ke) {
        if ((ke.getWhen() - timeKeyDown) < doublePressSpeed) {
            return true;
        } else {
            timeKeyDown = ke.getWhen();
        }
        lastKeyPressedCode = ke.getKeyCode();
        return false;
    }
}
