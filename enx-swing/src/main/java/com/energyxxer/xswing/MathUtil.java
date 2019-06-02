package com.energyxxer.xswing;

public class MathUtil {
    public static int clamp(int val, int min, int max) {
        return Math.max(Math.min(val, max), min);
    }
}