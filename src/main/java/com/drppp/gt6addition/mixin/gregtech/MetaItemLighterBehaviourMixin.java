package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.api.machine.IAutomaticIgnitable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.items.behaviors.LighterBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LighterBehaviour.class)
public abstract class MetaItemLighterBehaviourMixin {

    @Inject(method = "onItemUseFirst", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt6addition$igniteAutomaticIgnitable(EntityPlayer player, World world, BlockPos pos,
                                                       EnumFacing side, float hitX, float hitY, float hitZ,
                                                       EnumHand hand,
                                                       CallbackInfoReturnable<EnumActionResult> callback) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.isEmpty()) {
            return;
        }

        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(world, pos);
        if (!(metaTileEntity instanceof IAutomaticIgnitable)) {
            return;
        }
        if (world.isRemote) {
            callback.setReturnValue(EnumActionResult.SUCCESS);
            return;
        }

        IAutomaticIgnitable ignitable = (IAutomaticIgnitable) metaTileEntity;
        LighterBehaviour lighter = (LighterBehaviour) (Object) this;
        if (ignitable.igniteFromAutomaticIgniter() && lighter.consumeFuel(player, heldStack)) {
            callback.setReturnValue(EnumActionResult.SUCCESS);
        }
    }
}
