package com.dooji.chatemojis;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ChatEmojis.MODID, version = ChatEmojis.VERSION)
public class ChatEmojis {
    public static final String MODID = "chatemojis";
    public static final String VERSION = "1.0.1";

    @SidedProxy(clientSide = "com.dooji.chatemojis.client.ClientProxy", serverSide = "com.dooji.chatemojis.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }
}
