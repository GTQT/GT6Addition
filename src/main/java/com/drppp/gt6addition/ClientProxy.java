// ClientProxy.java
package com.drppp.gt6addition;

import com.drppp.gt6addition.common.item.GT6AdditionItems;
import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import com.drppp.gt6addition.common.block.tree.BlockGT6TreeLeaves;
import com.drppp.gt6addition.common.block.tree.BlockGT6TreeLog;
import com.drppp.gt6addition.common.block.tree.BlockGT6TreeSapling;
import com.drppp.gt6addition.common.block.tree.GT6TreeSpecies;
import net.minecraft.block.BlockSapling;
import net.minecraft.item.Item;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.statemap.StateMap;
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
        registerItemModel(GT6AdditionItems.DRAIN_COVER);
        registerItemModel(GT6AdditionItems.ANTHRACITE_ORE);
        registerTreeModels();
    }

    private static void registerTreeModels() {
        GT6TreeSpecies[] species = GT6TreeSpecies.values();
        for (int i = 0; i < species.length; i++) {
            ModelLoader.setCustomStateMapper(GT6AdditionBlocks.TREE_LOGS[i],
                    new StateMap.Builder().ignore(BlockGT6TreeLog.VARIANT).build());
            ModelLoader.setCustomStateMapper(GT6AdditionBlocks.TREE_LEAVES[i],
                    new StateMap.Builder().ignore(BlockGT6TreeLeaves.VARIANT).build());
            ModelLoader.setCustomStateMapper(GT6AdditionBlocks.TREE_SAPLINGS[i],
                    new StateMap.Builder().ignore(BlockSapling.TYPE).build());

            registerVariantModel(GT6AdditionItems.TREE_LOGS[i], "axis=y");
            registerVariantModel(GT6AdditionItems.TREE_LEAVES[i],
                    "check_decay=false,decayable=true");
            registerVariantModel(GT6AdditionItems.TREE_SAPLINGS[i], "stage=0");
        }
    }

    private static void registerVariantModel(Item item, String variant) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), variant));
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
