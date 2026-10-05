package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleElectricProvenance;
import gregtech.api.items.toolitem.ItemGTAxe;
import gregtech.api.items.toolitem.ItemGTHoe;
import gregtech.api.items.toolitem.ItemGTSword;
import gregtech.api.items.toolitem.ItemGTTool;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {ItemGTTool.class, ItemGTAxe.class, ItemGTHoe.class, ItemGTSword.class}, remap = false)
public abstract class CrucibleElectricCraftingBreakMixin {
    @Inject(method = "getContainerItem", at = @At("RETURN"), cancellable = true, remap = false)
    private void gt6addition$inheritCraftingBreakMaterials(ItemStack original,
                                                          CallbackInfoReturnable<ItemStack> callback) {
        // These concrete methods pass the original into the default method,
        // which damages its own copy. Normal returned tools stay unchanged.
        ItemStack result = callback.getReturnValue();
        ItemStack inherited = CrucibleElectricProvenance.inheritBrokenPowerUnit(result, original);
        if (inherited != result) callback.setReturnValue(inherited);
    }
}
