package com.drppp.gt6addition.common.block.tree;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.world.GT6TreeGenerator;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockPlanks;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.Random;

/** Sapling with the matching GT6 tree shape. */
public final class BlockGT6TreeSapling extends BlockSapling {

    private final GT6TreeSpecies species;

    public BlockGT6TreeSapling(GT6TreeSpecies species) {
        this.species = species;
        String name = "tree_sapling_" + species.getKey();
        setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        setTranslationKey(Tags.MOD_ID + "." + name);
        setDefaultState(blockState.getBaseState()
                .withProperty(TYPE, species.getVanillaWoodType())
                .withProperty(STAGE, 0));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(STAGE, meta & 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STAGE);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, 0));
    }

    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal(getTranslationKey() + ".name");
    }

    @Override
    public void generateTree(World world, BlockPos pos, IBlockState state, Random random) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
        if (!new GT6TreeGenerator(species).generate(world, random, pos)) {
            world.setBlockState(pos, state, 4);
        }
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        if (species == GT6TreeSpecies.COCONUT && world.getBlockState(pos.down()).getBlock() == Blocks.SAND) {
            return true;
        }
        return super.canBlockStay(world, pos, state);
    }
}
