package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.api.capability.HeatTransferBudget;
import gregtech.api.capability.IHeatable;
import gregtech.common.pipelike.heat.net.HeatNetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** External CEu routing defect: each branch otherwise gets the complete source offer. */
@Mixin(value = HeatNetHandler.class, remap = false)
public abstract class HeatNetTransferBudgetMixin {
    @Shadow private boolean transfer;
    @Unique private HeatTransferBudget gt6addition$heatBudget;

    @Inject(method = "transferHeat(JI)J", at = @At("HEAD"), remap = false, require = 1)
    private void gt6addition$beginBudget(long amount, int temperature, CallbackInfoReturnable<Long> callback) {
        // The original transfer flag rejects recursion. A rejected recursive
        // call must not replace the active outer call's budget.
        if (!transfer) gt6addition$heatBudget = new HeatTransferBudget(amount);
    }

    @Redirect(method = "transferHeat(JI)J", remap = false,
            at = @At(value = "INVOKE", target = "Lgregtech/api/capability/IHeatable;transferHeat(JI)J", remap = false),
            require = 1)
    private long gt6addition$boundedTarget(IHeatable target, long routeOffer, int temperature) {
        try {
            return gt6addition$heatBudget == null ? 0L : gt6addition$heatBudget.transfer(routeOffer,
                    offered -> target.transferHeat(offered, temperature));
        } finally {
            // CEu normally resets this after the target call, but not when the
            // target throws. Keep its cycle guard during the call, then release.
            transfer = false;
        }
    }

    @Inject(method = "transferHeat(JI)J", at = @At("RETURN"), remap = false, require = 1)
    private void gt6addition$finishBudget(long amount, int temperature, CallbackInfoReturnable<Long> callback) {
        if (!transfer) gt6addition$heatBudget = null;
    }
}
