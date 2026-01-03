package com.dooji.chatemojis.client.util;

// I wanna easily just use hex colors so :D
public class ColorUtil {
    public static int colorWithAlpha(String hex, float alpha) {
        return colorWithAlpha(parseHex(hex), alpha);
    }

    public static int colorWithAlpha(int rgb, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255))) << 24;
        return a | (rgb & 0xFFFFFF);
    }

    public static int parseHex(String hex) {
        if (hex == null) {
            return 0;
        }

        String clean = hex.replace("#", "").trim();
        if (clean.length() > 6) {
            clean = clean.substring(clean.length() - 6);
        }

        try {
            return Integer.parseInt(clean, 16) & 0xFFFFFF;
        } catch (Exception e) {
            return 0;
        }
    }
}
