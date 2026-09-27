package com.drppp.gt6addition.common.block;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.material.GT6MachineMaterials;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockAnthraciteOre extends Block {

    public BlockAnthraciteOre() {
        super(Material.ROCK);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "anthracite_ore"));
        setTranslationKey(Tags.MOD_ID + ".anthracite_ore");
        setHardness(3.0F);
        setResistance(5.0F);
        setHarvestLevel("pickaxe", 0);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         net.minecraft.block.state.IBlockState state, int fortune) {
        ItemStack gem = OreDictUnifier.get(OrePrefix.rawOre, GT6MachineMaterials.ANTHRACITE);
        if (!gem.isEmpty()) {
            Random random = world instanceof World ? ((World) world).rand : RANDOM;
            gem.setCount(quantityDroppedWithBonus(fortune, random));
            drops.add(gem);
        }
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return 1 + random.nextInt(Math.min(Math.max(fortune, 0), 3) + 1);
    }
}
