package com.drppp.gt6addition.common.block.tree;

import com.drppp.gt6addition.Tags;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;

/** A GT6 species log, with its species fixed by the block registry entry. */
public final class BlockGT6TreeLog extends BlockLog {

    public static final PropertyEnum<BlockPlanks.EnumType> VARIANT = BlockPlanks.VARIANT;

    private final BlockPlanks.EnumType woodType;

    public BlockGT6TreeLog(GT6TreeSpecies species) {
        this.woodType = species.getVanillaWoodType();
        String name = "tree_log_" + species.getKey();
        setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        setTranslationKey(Tags.MOD_ID + "." + name);
        setDefaultState(blockState.getBaseState()
                .withProperty(VARIANT, woodType)
                .withProperty(LOG_AXIS, EnumAxis.Y));
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHarvestLevel("axe", 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int axisBits = meta & 12;
        EnumAxis axis = axisBits == 4 ? EnumAxis.X
                : axisBits == 8 ? EnumAxis.Z
                : axisBits == 12 ? EnumAxis.NONE : EnumAxis.Y;
        return getDefaultState().withProperty(LOG_AXIS, axis);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumAxis axis = state.getValue(LOG_AXIS);
        return axis == EnumAxis.X ? 4 : axis == EnumAxis.Z ? 8 : axis == EnumAxis.NONE ? 12 : 0;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{VARIANT, LOG_AXIS});
    }
}
