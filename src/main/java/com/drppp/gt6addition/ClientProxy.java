// ClientProxy.java
package com.drppp.gt6addition;

import com.drppp.gt6addition.common.item.GT6AdditionItems;
import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import net.minecraft.item.Item;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        GT6AdditionMain.LOGGER.info("ClientProxy preInit");
        // 客户端特定的预初始化逻辑
    }

    @SubscribeEvent
    public static void registerItemModels(ModelRegistryEvent event) {
        registerItemModel(GT6AdditionItems.CLAY_CRUCIBLE);
        registerItemModel(GT6AdditionItems.CLAY_SPOUT);
        registerItemModel(GT6AdditionItems.CLAY_CHANNEL);
        registerItemModel(GT6AdditionItems.CLAY_BASIN);
        registerItemModel(GT6AdditionItems.CLAY_MOLD);
        registerItemModel(GT6AdditionBlocks.COKE_OVEN_BRICK_ITEM);
    }

    private static void registerItemModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
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
