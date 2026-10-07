package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleToolProvenance;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The host GT recipes inherit these result methods; this hook never targets addon classes. */
@Mixin(value = {ShapedOreRecipe.class, ShapelessOreRecipe.class}, remap = false)
public abstract class CrucibleToolCraftingMixin {
    @Inject(method = {"getCraftingResult", "func_77572_b"}, at = @At("RETURN"), remap = false)
    private void gt6addition$captureActualToolInputs(InventoryCrafting inventory,
                                                     CallbackInfoReturnable<ItemStack> callback) {
        CrucibleToolProvenance.capture((IRecipe) (Object) this, callback.getReturnValue(), inventory);
    }
}
