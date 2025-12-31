package com.dooji.chatemojis.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class EmojiResourceManager {
    private static final Map<String, ResourceLocation> emojiCache = new HashMap<String, ResourceLocation>();

    public static ResourceLocation getEmojiTexture(String name) {
        if (emojiCache.containsKey(name)) {
            return emojiCache.get(name);
        }

        ResourceLocation location = new ResourceLocation("chatemojis", "textures/emojis/" + name + ".png");

        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            BufferedImage image = ImageIO.read(resource.getInputStream());

            if (image != null) {
                TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
                DynamicTexture dynamicTexture = new DynamicTexture(image);
                ResourceLocation textureLocation = textureManager.getDynamicTextureLocation("chatemojis_" + name, dynamicTexture);

                emojiCache.put(name, textureLocation);
                return textureLocation;
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static boolean hasEmoji(String name) {
        if (emojiCache.containsKey(name)) {
            return true;
        }

        return getEmojiTexture(name) != null;
    }

    public static void clearCache() {
        emojiCache.clear();
    }
}
