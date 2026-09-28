package com.drppp.gt6addition;

import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.utils.MaterialColorUtil;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.material.GT6AdditionOrePrefixes;
import com.drppp.gt6addition.common.item.GT6AdditionItems;
import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import com.drppp.gt6addition.common.material.GT6MachineMaterials;
import com.drppp.gt6addition.common.fluid.GT6PotionFluids;
import com.drppp.gt6addition.common.world.AnthraciteWorldGenerator;
import com.drppp.gt6addition.common.world.GT6TreeWorldGenerator;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.LiquidBurringInfo;
import com.drppp.gt6addition.common.recipes.GT6AdditionMachineRecipes;
import com.drppp.gt6addition.common.recipes.GT6AdditionPotionRecipes;
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
        com.drppp.gt6addition.common.recipes.AnthraciteProcessing.register();
    }

    public void preInit(FMLPreInitializationEvent event) {
        GT6AdditionMain.LOGGER.info("CommonProxy preInit");
        GT6PotionFluids.register();
        MaterialColorUtil.init();
        CapabilityHandler.init();
        Gt6AdditionTextures.init();
        MetaTileEntityHandler.InitMte();
        GameRegistry.registerFuelHandler(new IFuelHandler() {
            @Override
            public int getBurnTime(ItemStack fuel) {
                MaterialStack material = OreDictUnifier.getMaterial(fuel);
                return material != null && material.material == GT6MachineMaterials.ANTHRACITE &&
                        OreDictUnifier.getPrefix(fuel) == OrePrefix.gem ? GT6MachineMaterials.ANTHRACITE_BURN_TIME : 0;
            }
        });
        GameRegistry.registerWorldGenerator(new AnthraciteWorldGenerator(), 0);
        GameRegistry.registerWorldGenerator(new GT6TreeWorldGenerator(), 1);
    }

    public void init(FMLInitializationEvent event) {
        TopInit.init();
        registerTreeOreDictionaryEntries();
    }

    private static void registerTreeOreDictionaryEntries() {
        for (Item item : GT6AdditionItems.TREE_LOGS) {
            OreDictionary.registerOre("logWood", new ItemStack(item));
        }
        for (Item item : GT6AdditionItems.TREE_LEAVES) {
            OreDictionary.registerOre("treeLeaves", new ItemStack(item));
        }
        for (Item item : GT6AdditionItems.TREE_SAPLINGS) {
            OreDictionary.registerOre("treeSapling", new ItemStack(item));
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        LiquidBurringInfo.init();
        GT6AdditionRecipeMaps.init();
        GT6AdditionMachineRecipes.init();
        GT6AdditionPotionRecipes.init();
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
        event.getRegistry().register(GT6AdditionItems.ANTHRACITE_ORE);
        for (Item item : GT6AdditionItems.getTreeItems()) event.getRegistry().register(item);
        OreDictUnifier.registerOre(new ItemStack(GT6AdditionItems.ANTHRACITE_ORE),
                OrePrefix.ore, GT6MachineMaterials.ANTHRACITE);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<net.minecraft.block.Block> event) {
        event.getRegistry().register(GT6AdditionBlocks.ANTHRACITE_ORE);
        for (net.minecraft.block.Block block : GT6AdditionBlocks.getTreeBlocks()) event.getRegistry().register(block);
    }
}
