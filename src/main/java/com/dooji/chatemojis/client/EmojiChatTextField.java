package com.dooji.chatemojis.client;

import com.dooji.chatemojis.client.util.ColorUtil;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.opengl.GL11;

public class EmojiChatTextField extends GuiTextField {
    private static final int SUGGESTION_COLOR = ColorUtil.colorWithAlpha("C0C0C0", 0.33f);
    private String suggestionText = "";
    private final FontRenderer fontRenderer;

    public EmojiChatTextField(FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(fontRenderer, x, y, width, height);
        this.fontRenderer = fontRenderer;
    }

    public void setSuggestionText(String text) {
        this.suggestionText = text == null ? "" : text;
    }

    public int getLineScrollOffsetValue() {
        return getLineScrollOffset();
    }

    @Override
    public void drawTextBox() {
        super.drawTextBox();
        if (!getVisible() || suggestionText.isEmpty()) {
            return;
        }

        int lineScrollOffset = getLineScrollOffset();
        String full = getText();
        int cursor = getCursorPosition();
        if (lineScrollOffset > full.length()) {
            lineScrollOffset = full.length();
        }

        if (cursor < lineScrollOffset || cursor > full.length()) {
            return;
        }

        String visible = this.fontRenderer.trimStringToWidth(full.substring(lineScrollOffset), getWidth());
        int cursorIndex = cursor - lineScrollOffset;
        if (cursorIndex < 0 || cursorIndex > visible.length()) {
            return;
        }

        int startX = this.xPosition + (getEnableBackgroundDrawing() ? 4 : 0);
        int startY = this.yPosition + (getEnableBackgroundDrawing() ? (this.height - 8) / 2 : 0);
        int beforeWidth = this.fontRenderer.getStringWidth(visible.substring(0, cursorIndex));
        int remainingWidth = getWidth() - beforeWidth;
        if (remainingWidth <= 0) {
            return;
        }

        String renderText = this.fontRenderer.trimStringToWidth(suggestionText, remainingWidth);
        if (renderText.isEmpty()) {
            return;
        }

        boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        int prevBlendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        int prevBlendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        this.fontRenderer.drawString(renderText, startX + beforeWidth, startY, SUGGESTION_COLOR);
        GL11.glBlendFunc(prevBlendSrc, prevBlendDst);
        if (!wasBlendEnabled) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    private int getLineScrollOffset() {
        try {
            Integer offset = ReflectionHelper.getPrivateValue(GuiTextField.class, this, "lineScrollOffset", "field_146225_q");
            return offset == null ? 0 : offset;
        } catch (Exception e) {
            return 0;
        }
    }
}
