package com.dooji.chatemojis.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

public class EmojiResourceManager {
    private static final Map<String, ResourceLocation> emojiCache = new HashMap<String, ResourceLocation>();
    private static final Map<String, Boolean> existenceCache = new HashMap<String, Boolean>();

    public static ResourceLocation getEmojiTexture(String name) {
        if (emojiCache.containsKey(name)) {
            return emojiCache.get(name);
        }

        ResourceLocation location = new ResourceLocation("chatemojis", "textures/emojis/" + name + ".png");

        try {
            Minecraft.getMinecraft().getResourceManager().getResource(location);
            emojiCache.put(name, location);
            existenceCache.put(name, true);
            return location;
        } catch (Exception e) {
            existenceCache.put(name, false);
            return null;
        }
    }

    public static boolean hasEmoji(String name) {
        if (existenceCache.containsKey(name)) {
            return existenceCache.get(name);
        }

        return getEmojiTexture(name) != null;
    }

    public static void clearCache() {
        emojiCache.clear();
        existenceCache.clear();
    }
}
