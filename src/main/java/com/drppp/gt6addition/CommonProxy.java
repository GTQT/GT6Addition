package com.drppp.gt6addition;

import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.utils.MaterialColorUtil;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.LiquidBurringInfo;
import com.drppp.gt6addition.common.recipes.GT6AdditionMachineRecipes;
import com.drppp.gt6addition.common.recipes.GT6AdditionRecipeMaps;
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
        GT6AdditionMain.LOGGER.info("CommonProxy preInit");
        MaterialColorUtil.init();
        CapabilityHandler.init();
        Gt6AdditionTextures.init();
        MetaTileEntityHandler.InitMte();
    }

    public void init(FMLInitializationEvent event) {
        TopInit.init();
    }

    public void postInit(FMLPostInitializationEvent event) {
        LiquidBurringInfo.init();
        GT6AdditionRecipeMaps.init();
        GT6AdditionMachineRecipes.init();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
    }
}
