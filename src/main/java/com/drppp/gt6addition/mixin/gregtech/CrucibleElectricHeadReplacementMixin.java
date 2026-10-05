package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleElectricProvenance;
import gregtech.common.crafting.ToolHeadReplaceRecipe;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ToolHeadReplaceRecipe.class, remap = false)
public abstract class CrucibleElectricHeadReplacementMixin {
    // Actual dependency uses the SRG name; development uses the MCP name.
    @Inject(method = {"getCraftingResult", "func_77572_b"}, at = @At("RETURN"), remap = false)
    private void gt6addition$preservePowerMaterials(InventoryCrafting inventory,
                                                   CallbackInfoReturnable<ItemStack> callback) {
        CrucibleElectricProvenance.captureHeadReplacement(callback.getReturnValue(), inventory);
    }
}
