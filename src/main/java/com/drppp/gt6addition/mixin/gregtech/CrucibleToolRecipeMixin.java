package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleToolRecycling;
import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleToolProvenance;
import gregtech.api.recipes.ModHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModHandler.class, remap = false)
public abstract class CrucibleToolRecipeMixin {
    @Inject(method = "addRecipe(Ljava/lang/String;Lnet/minecraft/item/ItemStack;ZZ[Ljava/lang/Object;)V",
            at = @At("RETURN"), remap = false)
    private static void gt6addition$captureToolMaterials(String name, ItemStack output,
                                                        boolean clearNbt, boolean mirrored,
                                                        Object[] recipe, CallbackInfo ci) {
        CrucibleToolProvenance.rememberRecipe(GameData.checkPrefix(name, false), clearNbt);
        CrucibleToolRecycling.capture(output, clearNbt, recipe);
    }

    @Inject(method = "addShapelessRecipe(Ljava/lang/String;Lnet/minecraft/item/ItemStack;Z[Ljava/lang/Object;)V",
            at = @At(value = "INVOKE",
                    target = "Lgregtech/api/recipes/ModHandler;registerRecipe(Lnet/minecraft/item/crafting/IRecipe;)V",
                    shift = At.Shift.AFTER), remap = false)
    private static void gt6addition$captureShapelessToolMaterials(String name, ItemStack output,
                                                                 boolean clearNbt, Object[] recipe,
                                                                 CallbackInfo ci) {
        CrucibleToolProvenance.rememberRecipe(GameData.checkPrefix(name, false), clearNbt);
        CrucibleToolRecycling.captureShapeless(output, clearNbt, recipe);
    }
}
