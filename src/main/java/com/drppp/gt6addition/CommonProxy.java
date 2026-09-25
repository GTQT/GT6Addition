package com.drppp.gt6addition;

import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.utils.MaterialColorUtil;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.material.GT6AdditionOrePrefixes;
import com.drppp.gt6addition.common.item.GT6AdditionItems;
import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.LiquidBurringInfo;
import com.drppp.gt6addition.common.recipes.GT6AdditionMachineRecipes;
import com.drppp.gt6addition.common.recipes.GT6AdditionRecipeMaps;
import com.drppp.gt6addition.intergations.top.TopInit;
import gregtech.api.unification.material.event.PostMaterialEvent;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class CommonProxy {

    @SubscribeEvent
    public static void registerOrePrefixes(PostMaterialEvent event) {
        // GTCEu posts this after creating material registries and before MetaItems.init().
        GT6AdditionOrePrefixes.register();
    }

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

    public void registerRenderers() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(GT6AdditionItems.CLAY_CRUCIBLE);
        event.getRegistry().register(GT6AdditionItems.CLAY_SPOUT);
        event.getRegistry().register(GT6AdditionItems.CLAY_CHANNEL);
        event.getRegistry().register(GT6AdditionItems.CLAY_BASIN);
        event.getRegistry().register(GT6AdditionItems.CLAY_MOLD);
    }
}
