package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** GT6 WD.fire's block selection, adapted to Forge 1.12's directional API. */
public final class CrucibleHazardFire {
    private CrucibleHazardFire() {}

    public static boolean tryIgnite(World world, BlockPos pos, boolean checkFlammability) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        if (material == Material.LAVA || material == Material.FIRE) return false;
        if (material != Material.CARPET && state.getCollisionBoundingBox(world, pos) != null) return false;
        // Forge 1.12 has no UNKNOWN facing. Check all real faces for the
        // replaceable block rather than guessing a single directional value.
        for (EnumFacing side : EnumFacing.VALUES) {
            if (state.getBlock().getFlammability(world, pos, side) > 0) {
                return world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
            }
        }
        if (!checkFlammability) return world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos neighbor = pos.offset(side);
            IBlockState adjacent = world.getBlockState(neighbor);
            if (adjacent.getBlock() == Blocks.CHEST || adjacent.getBlock() == Blocks.TRAPPED_CHEST ||
                    adjacent.getBlock().getFlammability(world, neighbor, side.getOpposite()) > 0) {
                return world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
            }
        }
        return false;
    }
}
