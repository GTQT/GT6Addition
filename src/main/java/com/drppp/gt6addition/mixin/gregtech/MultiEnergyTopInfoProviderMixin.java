package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.intergations.top.provider.TopCommonProvider;
import gregtech.api.util.GTUtility;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Use the addon's rows instead of CEu's duplicate heat/progress or incorrect EU/t captions. */
@Pseudo
@Mixin(targets = "gregtech.integration.theoneprobe.provider.CapabilityInfoProvider", remap = false)
public abstract class MultiEnergyTopInfoProviderMixin {
    @Inject(method = "addProbeInfo(Lmcjty/theoneprobe/api/ProbeMode;Lmcjty/theoneprobe/api/IProbeInfo;" +
            "Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;" +
            "Lnet/minecraft/block/state/IBlockState;Lmcjty/theoneprobe/api/IProbeHitData;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void gt6addition$useMultiEnergyDisplay(ProbeMode mode, IProbeInfo info, EntityPlayer player,
                                                  World world, IBlockState state, IProbeHitData hit,
                                                  CallbackInfo callback) {
        String providerId = ((IProbeInfoProvider) (Object) this).getID();
        if (TopCommonProvider.replacesDefaultMachineInfo(providerId,
                GTUtility.getMetaTileEntity(world, hit.getPos()))) {
            callback.cancel();
        }
    }
}
