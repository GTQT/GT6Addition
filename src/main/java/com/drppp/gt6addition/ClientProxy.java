// ClientProxy.java
package com.drppp.gt6addition;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        GT6AdditionMain.LOGGER.info("ClientProxy preInit");
        // 客户端特定的预初始化逻辑
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // 客户端特定的初始化逻辑
        registerRenderers();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        // 客户端特定的后初始化逻辑
    }

    @Override
    public void registerRenderers() {
        // 客户端渲染注册的具体实现
        GT6AdditionMain.LOGGER.info("Registering client renderers");
        // 这里可以注册方块、物品的渲染器等
    }


}