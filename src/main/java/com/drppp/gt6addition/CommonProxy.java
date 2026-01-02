// CommonProxy.java
package com.drppp.gt6addition;

import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.intergations.top.TopInit;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class CommonProxy {


    public void preInit(FMLPreInitializationEvent event) {
        // 服务端和客户端共同的预初始化逻辑
        GT6AdditionMain.LOGGER.info("CommonProxy preInit");
        Gt6AdditionTextures.init();
        MetaTileEntityHandler.InitMte();
    }

    public void init(FMLInitializationEvent event) {
        // 服务端和客户端共同的初始化逻辑
        TopInit.init();

    }

    public void postInit(FMLPostInitializationEvent event) {
        // 服务端和客户端共同的后初始化逻辑
    }

    // 其他需要在服务端和客户端都执行的方法
    public void registerRenderers() {
        // 客户端渲染相关的注册，在服务端不执行
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {

    }
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {

    }
}