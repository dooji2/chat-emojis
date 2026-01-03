package com.dooji.chatemojis.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiFontRenderer extends FontRenderer {
    private static final Pattern EMOJI_PATTERN = Pattern.compile(":([a-zA-Z0-9_]+):");
    private final FontRenderer original;

    public EmojiFontRenderer(FontRenderer original) {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().getTextureManager(), false);
        this.original = original;
        this.FONT_HEIGHT = original.FONT_HEIGHT;
        super.setUnicodeFlag(original.getUnicodeFlag());
        super.setBidiFlag(original.getBidiFlag());
        this.onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());
    }

    @Override
    public int drawString(String text, int x, int y, int color, boolean dropShadow) {
        if (text == null) return 0;

        Matcher matcher = EMOJI_PATTERN.matcher(text);
        int currentX = x;
        int lastEnd = 0;

        original.drawString("", currentX, y, color, dropShadow);

        while (matcher.find()) {
            String emojiName = matcher.group(1);
            if (EmojiHelper.hasEmoji(emojiName)) {
                String before = text.substring(lastEnd, matcher.start());
                if (!before.isEmpty()) {
                    currentX = original.drawString(before, currentX, y, color, dropShadow);
                }

                ResourceLocation texture = EmojiHelper.getEmojiTexture(emojiName);
                if (texture != null) {
                    EmojiHelper.renderEmojiIcon(texture, currentX, y + 1, 8);
                    original.drawString("", currentX, y, color, dropShadow);
                    currentX += 10;
                }

                lastEnd = matcher.end();
            }
        }

        if (lastEnd < text.length()) {
            currentX = original.drawString(text.substring(lastEnd), currentX, y, color, dropShadow);
        }

        return currentX;
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null) return 0;

        int width = 0;
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            String emojiName = matcher.group(1);
            if (EmojiHelper.hasEmoji(emojiName)) {
                width += original.getStringWidth(text.substring(lastEnd, matcher.start()));
                width += 10;
                lastEnd = matcher.end();
            }
        }

        if (lastEnd < text.length()) {
            width += original.getStringWidth(text.substring(lastEnd));
        }

        return width;
    }

    @Override
    public String trimStringToWidth(String text, int width) {
        return trimStringToWidth(text, width, false);
    }

    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        if (text == null) return "";

        StringBuilder builder = new StringBuilder();
        if (reverse) {
            for (int i = text.length() - 1; i >= 0; i--) {
                builder.insert(0, text.charAt(i));
                if (getStringWidth(builder.toString()) > width) {
                    builder.deleteCharAt(0);
                    break;
                }
            }
        } else {
            for (int i = 0; i < text.length(); i++) {
                builder.append(text.charAt(i));
                if (getStringWidth(builder.toString()) > width) {
                    builder.deleteCharAt(builder.length() - 1);
                    break;
                }
            }
        }

        return builder.toString();
    }

    @Override
    public int drawStringWithShadow(String text, int x, int y, int color) {
        return this.drawString(text, x, y, color, true);
    }

    @Override
    public void setUnicodeFlag(boolean unicodeFlag) {
        super.setUnicodeFlag(unicodeFlag);
        if (original != null) {
            original.setUnicodeFlag(unicodeFlag);
        }
    }

    @Override
    public void setBidiFlag(boolean bidiFlag) {
        super.setBidiFlag(bidiFlag);
        if (original != null) {
            original.setBidiFlag(bidiFlag);
        }
    }
}
