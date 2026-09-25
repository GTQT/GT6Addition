package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.api.IComparatorSignalProvider;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Mixin;

/** Bridges directional GregTech machine comparator signals to vanilla comparators. */
@Mixin(Block.class)
public abstract class BlockMachineComparatorMixin {

    // Comparator capability is selected by block type, but GT MTE behavior lives in
    // the tile entity. Advertise the hook for GT machine blocks, then filter by MTE.
    @Inject(method = "hasComparatorInputOverride", at = @At("HEAD"), cancellable = true)
    private void gt6addition$hasComparatorInputOverride(IBlockState state,
                                                        CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof BlockMachine) callback.setReturnValue(true);
    }

    @Inject(method = "getComparatorInputOverride", at = @At("HEAD"), cancellable = true)
    private void gt6addition$getComparatorInputOverride(IBlockState state, World world, BlockPos pos,
                                                        CallbackInfoReturnable<Integer> callback) {
        if (!((Object) this instanceof BlockMachine) || world == null) return;
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof MetaTileEntityHolder)) return;
        MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tile).getMetaTileEntity();
        if (metaTileEntity instanceof IComparatorSignalProvider) {
            callback.setReturnValue(Math.max(0, Math.min(15,
                    ((IComparatorSignalProvider) metaTileEntity).getComparatorSignal())));
        }
    }
}
