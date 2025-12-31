package com.dooji.chatemojis.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

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
        int appliedColor = color;

        if ((appliedColor & -67108864) == 0) {
            appliedColor |= -16777216;
        }

        if (dropShadow) {
            appliedColor = (appliedColor & 16579836) >> 2 | appliedColor & -16777216;
        }

        while (matcher.find()) {
            String emojiName = matcher.group(1);
            if (EmojiResourceManager.hasEmoji(emojiName)) {
                String before = text.substring(lastEnd, matcher.start());
                currentX = original.drawString(before, currentX, y, color, dropShadow);

                ResourceLocation texture = EmojiResourceManager.getEmojiTexture(emojiName);
                if (texture != null) {
                    boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
                    int prevBlendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
                    int prevBlendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);

                    Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                    double zLevel = 0;

                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glPushMatrix();
                    GL11.glTranslated(currentX, y + 1, 0);

                    Tessellator tessellator = Tessellator.instance;
                    tessellator.startDrawingQuads();
                    tessellator.addVertexWithUV(0, 8, zLevel, 0, 1);
                    tessellator.addVertexWithUV(8, 8, zLevel, 1, 1);
                    tessellator.addVertexWithUV(8, 0, zLevel, 1, 0);
                    tessellator.addVertexWithUV(0, 0, zLevel, 0, 0);
                    tessellator.draw();

                    GL11.glPopMatrix();
                    GL11.glBlendFunc(prevBlendSrc, prevBlendDst);

                    if (!wasBlendEnabled) {
                        GL11.glDisable(GL11.GL_BLEND);
                    }

                    GL11.glColor4f(
                        (float)(appliedColor >> 16 & 255) / 255.0F,
                        (float)(appliedColor >> 8 & 255) / 255.0F,
                        (float)(appliedColor & 255) / 255.0F,
                        (float)(appliedColor >> 24 & 255) / 255.0F);

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
            if (EmojiResourceManager.hasEmoji(emojiName)) {
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
