package com.dooji.chatemojis.client;

import com.dooji.chatemojis.client.util.ColorUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class EmojiChatGui extends GuiChat {
    private static final Pattern EMOJI_PARTIAL_PATTERN = Pattern.compile("[a-zA-Z0-9_]*");

    private static final int listBgColor = ColorUtil.colorWithAlpha("000000", 0.9f);
    private static final int listTextColor = ColorUtil.colorWithAlpha("C0C0C0", 0.53f);
    private static final int listTextActiveColor = ColorUtil.colorWithAlpha("FFFFFF", 1f);
    private static final int listRowHoverColor = ColorUtil.colorWithAlpha("333333", 0.33f);
    private static final int listPaddingX = 4;
    private static final int listPaddingY = 3;
    private static final int listMaxVisible = 6;
    private static final int listIconSize = 8;
    private static final int listIconGap = 2;
    private static final int listSpacing = 6;

    private String lastPrefix = "";
    private ListState listState;

    public EmojiChatGui() {
        super();
    }

    public EmojiChatGui(String defaultText) {
        super(defaultText);
    }

    @Override
    public void initGui() {
        super.initGui();
        if (!(this.inputField instanceof EmojiChatTextField)) {
            GuiTextField old = this.inputField;
            this.inputField = new EmojiChatTextField(this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
            this.inputField.setMaxStringLength(old.getMaxStringLength());
            this.inputField.setEnableBackgroundDrawing(old.getEnableBackgroundDrawing());
            this.inputField.setFocused(old.isFocused());
            this.inputField.setText(old.getText());
            this.inputField.setCanLoseFocus(false);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 15) {
            CompletionContext context = findContext();
            if (context != null) {
                this.inputField.writeText(context.insert);
                return;
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        CompletionContext context = findContext();
        if (this.inputField instanceof EmojiChatTextField) {
            ((EmojiChatTextField) this.inputField).setSuggestionText(context == null ? "" : context.insert);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawEmojiList(mouseX, mouseY, context);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        if (listState == null || !listState.hover || listState.total <= listMaxVisible) {
            return;
        }

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }

        int offset = listState.offset + (wheel > 0 ? -1 : 1);
        int maxOffset = listState.total - listMaxVisible;
        if (offset < 0) offset = 0;
        if (offset > maxOffset) offset = maxOffset;
        listState.offset = offset;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && listState != null && listState.hover && listState.visible > 0) {
            int row = (mouseY - (listState.y + listPaddingY)) / listState.lineHeight;
            if (row >= 0 && row < listState.visible) {
                int index = listState.total - 1 - (listState.offset + row);
                if (index >= 0 && index < listState.matches.size()) {
                    applySelection(listState.matches.get(index));
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private CompletionContext findContext() {
        if (!(this.inputField instanceof EmojiChatTextField)) {
            return null;
        }

        if (this.inputField.getSelectionEnd() != this.inputField.getCursorPosition()) {
            return null;
        }

        if (!this.inputField.isFocused()) {
            return null;
        }

        String text = this.inputField.getText();
        int cursor = this.inputField.getCursorPosition();
        if (cursor < 0 || cursor > text.length()) {
            return null;
        }

        if (cursor < text.length() && text.charAt(cursor) != ':') {
            return null;
        }

        PartialInfo info = getPartialAtCursor(text, cursor);
        if (info == null) {
            return null;
        }

        List<String> matches = EmojiAutocomplete.getMatches(info.partialLower);
        if (matches.isEmpty()) {
            return null;
        }

        String best = matches.get(0);

        String insert = buildInsert(best, info.partialLower, info.hasClosing);
        if (insert.isEmpty()) {
            return null;
        }

        return new CompletionContext(info, matches, best, insert);
    }

    private void drawEmojiList(int mouseX, int mouseY, CompletionContext context) {
        if (context == null || context.matches.isEmpty()) {
            listState = null;
            return;
        }

        PartialInfo info = context.info;
        List<String> matches = context.matches;

        if (!info.partialLower.equals(lastPrefix)) {
            listState = null;
            lastPrefix = info.partialLower;
        }

        int lineScrollOffset = ((EmojiChatTextField) this.inputField).getLineScrollOffsetValue();
        if (lineScrollOffset > info.lastColon) {
            listState = null;
            return;
        }

        String text = this.inputField.getText();
        int baseX = this.inputField.xPosition + (this.inputField.getEnableBackgroundDrawing() ? 4 : 0);
        int baseY = this.inputField.yPosition + (this.inputField.getEnableBackgroundDrawing() ? (this.inputField.height - 8) / 2 : 0);
        String beforeColon = text.substring(lineScrollOffset, info.lastColon);
        int startX = baseX + this.fontRendererObj.getStringWidth(beforeColon);

        int maxWidth = 0;
        for (String name : matches) {
            int w = this.fontRendererObj.getStringWidth(name);
            if (w > maxWidth) {
                maxWidth = w;
            }
        }

        int extra = this.fontRendererObj.getStringWidth(" ");
        int listW = maxWidth + extra + listPaddingX * 2 + listIconSize + listIconGap;
        int listTotal = matches.size();
        int listVisible = Math.min(listTotal, listMaxVisible);
        int maxOffset = listTotal > listMaxVisible ? listTotal - listMaxVisible : 0;
        int offset = listState == null ? maxOffset : listState.offset;
        if (offset < 0 || offset > maxOffset || listState == null || !matches.equals(listState.matches)) {
            offset = maxOffset;
        }

        int listLineHeight = this.fontRendererObj.FONT_HEIGHT + 4;
        int listH = listVisible * listLineHeight + listPaddingY * 2;
        int listY = baseY - listH - listSpacing;

        int hoverRow = -1;
        boolean hover = mouseX >= startX && mouseX <= startX + listW && mouseY >= listY && mouseY <= listY + listH;
        if (hover) {
            hoverRow = (mouseY - (listY + listPaddingY)) / listLineHeight;
            if (hoverRow < 0 || hoverRow >= listVisible) {
                hoverRow = -1;
            }
        }

        drawRect(startX, listY, startX + listW, listY + listH, listBgColor);

        int y = listY + listPaddingY;
        for (int i = 0; i < listVisible; i++) {
            int index = listTotal - 1 - (offset + i);
            if (index < 0 || index >= listTotal) {
                break;
            }

            String name = matches.get(index);
            int color = name.equals(context.best) ? listTextActiveColor : listTextColor;
            int textX = startX + listPaddingX;
            int iconY = y + (listLineHeight - listIconSize) / 2;
            if (i == hoverRow) {
                drawRect(startX + 1, y - 1, startX + listW - 1, y + listLineHeight - 1, listRowHoverColor);
                if (!name.equals(context.best)) {
                    color = listTextActiveColor;
                }
            }

            ResourceLocation texture = EmojiHelper.getEmojiTexture(name);
            if (texture != null) {
                EmojiHelper.renderEmojiIcon(texture, textX, iconY, listIconSize);
            }

            textX += listIconSize + listIconGap;
            this.fontRendererObj.drawString(name, textX, y, color);
            y += listLineHeight;
        }

        listState = new ListState(matches, offset, listTotal, listVisible, listLineHeight, startX, listY, listW, listH, hover);
    }

    private void applySelection(String name) {
        if (name == null || this.inputField == null) {
            return;
        }

        String text = this.inputField.getText();
        int cursor = this.inputField.getCursorPosition();
        PartialInfo info = getPartialAtCursor(text, cursor);
        if (info == null) {
            return;
        }

        String insert = buildInsert(name, info.partialLower, info.hasClosing);
        if (insert.isEmpty()) {
            return;
        }

        this.inputField.writeText(insert);
    }

    private String buildInsert(String match, String partialLower, boolean hasClosing) {
        if (match == null || partialLower == null || !match.startsWith(partialLower)) {
            return "";
        }

        String suffix = match.substring(partialLower.length());
        return suffix + (hasClosing ? "" : ":");
    }

    private PartialInfo getPartialAtCursor(String text, int cursor) {
        if (text == null) {
            return null;
        }

        if (cursor < 0 || cursor > text.length()) {
            return null;
        }

        String prefix = text.substring(0, cursor);
        int lastColon = prefix.lastIndexOf(':');
        if (lastColon == -1) {
            return null;
        }

        String partial = prefix.substring(lastColon + 1);
        if (!EMOJI_PARTIAL_PATTERN.matcher(partial).matches()) {
            return null;
        }

        int prevColon = prefix.lastIndexOf(':', lastColon - 1);
        if (prevColon != -1) {
            String between = prefix.substring(prevColon + 1, lastColon);
            if (!between.isEmpty() && EMOJI_PARTIAL_PATTERN.matcher(between).matches()) {
                return null;
            }
        }

        boolean hasClosing = cursor < text.length() && text.charAt(cursor) == ':';
        return new PartialInfo(partial.toLowerCase(Locale.ENGLISH), lastColon, hasClosing);
    }

    private static class PartialInfo {
        private final String partialLower;
        private final int lastColon;
        private final boolean hasClosing;

        private PartialInfo(String partialLower, int lastColon, boolean hasClosing) {
            this.partialLower = partialLower;
            this.lastColon = lastColon;
            this.hasClosing = hasClosing;
        }
    }

    private static class ListState {
        private final List<String> matches;
        private int offset;
        private final int total;
        private final int visible;
        private final int lineHeight;
        private final int x;
        private final int y;
        private final int w;
        private final int h;
        private final boolean hover;

        private ListState(List<String> matches, int offset, int total, int visible, int lineHeight, int x, int y, int w, int h, boolean hover) {
            this.matches = matches;
            this.offset = offset;
            this.total = total;
            this.visible = visible;
            this.lineHeight = lineHeight;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.hover = hover;
        }
    }

    private static class CompletionContext {
        private final PartialInfo info;
        private final List<String> matches;
        private final String best;
        private final String insert;

        private CompletionContext(PartialInfo info, List<String> matches, String best, String insert) {
            this.info = info;
            this.matches = matches;
            this.best = best;
            this.insert = insert;
        }
    }
}
