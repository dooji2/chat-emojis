package com.dooji.chatemojis.client;

import com.dooji.chatemojis.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void init() {
        super.init();
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.fontRenderer = new EmojiFontRenderer(minecraft.fontRenderer);
        ((IReloadableResourceManager) minecraft.getResourceManager()).registerReloadListener(new IResourceManagerReloadListener() {
                    @Override
                    public void onResourceManagerReload(IResourceManager resourceManager) {
                        EmojiResourceManager.clearCache();
                    }
        });
    }
}
