package com.dooji.chatemojis.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class EmojiHelper {
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
        } catch (Exception ignored) {
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

    public static void renderEmojiIcon(ResourceLocation texture, int x, int y, int size) {
        if (texture == null || size <= 0) {
            return;
        }

        boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        int prevBlendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        int prevBlendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, 0);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0, size, 0, 0, 1);
        tessellator.addVertexWithUV(size, size, 0, 1, 1);
        tessellator.addVertexWithUV(size, 0, 0, 1, 0);
        tessellator.addVertexWithUV(0, 0, 0, 0, 0);
        tessellator.draw();

        GL11.glPopMatrix();
        GL11.glBlendFunc(prevBlendSrc, prevBlendDst);
        if (!wasBlendEnabled) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
