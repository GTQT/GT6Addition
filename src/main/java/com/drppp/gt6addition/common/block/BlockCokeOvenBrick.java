package com.drppp.gt6addition.common.block;

import com.drppp.gt6addition.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;

/** Independent GT6-style firebrick block used only by the coke-oven structure. */
public final class BlockCokeOvenBrick extends Block {
    public BlockCokeOvenBrick() {
        super(Material.ROCK);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "coke_oven_brick"));
        setTranslationKey(Tags.MOD_ID + ".coke_oven_brick");
        setHardness(3.0F);
        setResistance(5.0F);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }
}
