package org.enginecraft.swing.util;

import java.awt.*;

public class ColorUtil {
    public static double luminance(Color color) {
        return (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255.0;
    }

    public static boolean isTooClose(Color c1, Color c2, double threshold) {
        return Math.abs(luminance(c1) - luminance(c2)) < threshold;
    }

    public static boolean isDarker(Color c1, Color c2) {
        return luminance(c1) < luminance(c2);
    }

    public static Color lighten(Color color, float factor) {
        int r = (int) (color.getRed() + (255 - color.getRed()) * factor);
        int g = (int) (color.getGreen() + (255 - color.getGreen()) * factor);
        int b = (int) (color.getBlue() + (255 - color.getBlue()) * factor);
        return new Color(clamp(r), clamp(g), clamp(b), color.getAlpha());
    }

    public static Color darken(Color color, float factor) {
        int r = (int) (color.getRed() * (1 - factor));
        int g = (int) (color.getGreen() * (1 - factor));
        int b = (int) (color.getBlue() * (1 - factor));
        return new Color(clamp(r), clamp(g), clamp(b), color.getAlpha());
    }

    public static Color[] adjustColors(Color bg, Color fg, boolean brighten, float step, double threshold) {
        Color newBg = brighten ? lighten(bg, step) : darken(bg, step);
        Color newFg = brighten ? darken(fg, step) : lighten(fg, step);

        if (isTooClose(newBg, newFg, threshold)) {
            Color temp = newBg;
            newBg = newFg;
            newFg = temp;
        }

        return new Color[]{newBg, newFg};
    }

    private static int clamp(int val) {
        return Math.min(255, Math.max(0, val));
    }

}
