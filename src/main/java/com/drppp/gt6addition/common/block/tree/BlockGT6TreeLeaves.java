package com.drppp.gt6addition.common.block.tree;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Species-specific foliage using vanilla leaf-decay behavior. */
public final class BlockGT6TreeLeaves extends BlockLeaves {

    public static final PropertyEnum<BlockPlanks.EnumType> VARIANT = BlockPlanks.VARIANT;

    private final GT6TreeSpecies species;
    private final BlockPlanks.EnumType woodType;

    public BlockGT6TreeLeaves(GT6TreeSpecies species) {
        this.species = species;
        this.woodType = species.getVanillaWoodType();
        String name = "tree_leaves_" + species.getKey();
        setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        setTranslationKey(Tags.MOD_ID + "." + name);
        setDefaultState(blockState.getBaseState()
                .withProperty(VARIANT, woodType)
                .withProperty(DECAYABLE, true)
                .withProperty(CHECK_DECAY, false));
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(0.2F);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(DECAYABLE, (meta & 4) == 0)
                .withProperty(CHECK_DECAY, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (!state.getValue(DECAYABLE)) meta |= 4;
        if (state.getValue(CHECK_DECAY)) meta |= 8;
        return meta;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(GT6AdditionBlocks.getTreeSapling(species));
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(this);
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return woodType;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack tool, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        int chance = 50;
        if (fortune > 0) chance = Math.max(5, chance - (5 << fortune));
        Random random = world instanceof World ? ((World) world).rand : new Random();
        boolean saplingDropped = random.nextInt(chance)
                < (species == GT6TreeSpecies.COCONUT ? 2 : 1);
        if (saplingDropped) {
            drops.add(new ItemStack(GT6AdditionBlocks.getTreeSapling(species)));
        } else if (species == GT6TreeSpecies.WILLOW || species == GT6TreeSpecies.HAZEL) {
            if (random.nextInt(chance) < 2) drops.add(new ItemStack(net.minecraft.init.Items.STICK));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{VARIANT, CHECK_DECAY, DECAYABLE});
    }
}
