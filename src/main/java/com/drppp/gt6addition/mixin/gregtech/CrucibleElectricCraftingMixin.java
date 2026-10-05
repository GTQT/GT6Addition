package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleElectricProvenance;
import gregtech.common.crafting.ShapedOreEnergyTransferRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(value = ShapedOreEnergyTransferRecipe.class, remap = false)
public abstract class CrucibleElectricCraftingMixin {
    @Inject(method = "chargeStackFromComponents", at = @At("RETURN"), remap = false)
    private static void gt6addition$captureComponents(ItemStack output, IInventory ingredients,
                                                      Predicate<ItemStack> predicate, boolean transferMaxCharge,
                                                      CallbackInfo callback) {
        CrucibleElectricProvenance.capture(output, ingredients);
    }
}
