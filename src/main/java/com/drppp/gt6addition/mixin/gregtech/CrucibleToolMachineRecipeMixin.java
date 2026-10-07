package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleToolRecycling;
import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleComponentProvenance;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.map.MapItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeMap.class, remap = false)
public abstract class CrucibleToolMachineRecipeMixin {
    @Redirect(method = "buildFromItemStacks", at = @At(value = "NEW",
            target = "gregtech/api/recipes/map/MapItemStackIngredient"), remap = false)
    private MapItemStackIngredient gt6addition$indexWithoutBookkeeping(ItemStack stack, int metadata, NBTTagCompound nbt) {
        ItemStack normalized = CrucibleComponentProvenance.matchingCopy(stack);
        // Only the ordinary lookup key changes. Explicit NBT lookup alternatives
        // still receive the original stack and all original NBT fields.
        return new MapItemStackIngredient(normalized, metadata, normalized == stack ? nbt : normalized.getTagCompound());
    }

    @Inject(method = "compileRecipe(Lgregtech/api/recipes/Recipe;)Z", at = @At("RETURN"), remap = false)
    private void gt6addition$captureMachineToolMaterials(Recipe recipe, CallbackInfoReturnable<Boolean> ci) {
        if (Boolean.TRUE.equals(ci.getReturnValue())) CrucibleToolRecycling.captureMachineRecipe(recipe);
    }
}
