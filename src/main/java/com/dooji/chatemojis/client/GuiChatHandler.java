package com.dooji.chatemojis.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.GuiOpenEvent;

public class GuiChatHandler {
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null || event.gui instanceof EmojiChatGui || !(event.gui instanceof GuiChat)) {
            return;
        }

        String text = null;
        try {
            text = ReflectionHelper.getPrivateValue(GuiChat.class, (GuiChat) event.gui, "defaultInputFieldText", "field_146409_v");
        } catch (Exception ignored) {
        }

        if (text == null) {
            event.gui = new EmojiChatGui();
        } else {
            event.gui = new EmojiChatGui(text);
        }
    }
}
