package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleToolRecycling;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeMap.class, remap = false)
public abstract class CrucibleToolMachineRecipeMixin {
    @Inject(method = "compileRecipe(Lgregtech/api/recipes/Recipe;)Z", at = @At("RETURN"), remap = false)
    private void gt6addition$captureMachineToolMaterials(Recipe recipe, CallbackInfoReturnable<Boolean> ci) {
        if (Boolean.TRUE.equals(ci.getReturnValue())) CrucibleToolRecycling.captureMachineRecipe(recipe);
    }
}
