package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleComponentProvenance;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GTRecipeItemInput.class, remap = false)
public abstract class CrucibleComponentIngredientMixin {
    @Inject(method = "acceptsStack", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt6addition$matchWithoutBookkeeping(ItemStack input, CallbackInfoReturnable<Boolean> ci) {
        GTRecipeItemInput ingredient = (GTRecipeItemInput) (Object) this;
        if (ingredient.getNBTMatcher() != null) return;
        ItemStack normalized = CrucibleComponentProvenance.matchingCopy(input);
        if (normalized != input) ci.setReturnValue(ingredient.acceptsStack(normalized));
        // The copy no longer has our tag, so this delegation cannot recurse again.
    }
}
