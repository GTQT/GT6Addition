package com.drppp.gt6addition.mixin.minecraft;

import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** External removal boundary only; all crucible behavior stays in its own source. */
@Mixin(PlayerInteractionManager.class)
public abstract class PlayerBlockRemovalMixin {
    // Both this overload and removedByPlayer are Forge-added methods, not
    // vanilla MCP/SRG methods. Match their exact descriptors without remapping.
    // Forge's cancellation, tool and harvest checks have already run here.
    @Redirect(method = "removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z", remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;removedByPlayer(" +
                    "Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;" +
                    "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/EntityPlayer;Z)Z", remap = false),
            require = 1)
    private boolean gt6addition$removeWithPlayerContext(Block block, IBlockState state, World world,
                                                        BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (block instanceof BlockMachine) {
            MetaTileEntity machine = GTUtility.getMetaTileEntity(world, pos);
            if (machine instanceof MetaTileEntityCrucible) {
                return ((MetaTileEntityCrucible) machine).performPlayerRemoval(player,
                        () -> block.removedByPlayer(state, world, pos, player, willHarvest));
            }
        }
        return block.removedByPlayer(state, world, pos, player, willHarvest);
    }
}
