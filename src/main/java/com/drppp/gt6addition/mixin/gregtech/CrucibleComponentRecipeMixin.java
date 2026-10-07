package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleComponentProvenance;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Only external CEu entry points; all accounting lives in the mod's own source. */
@Mixin(value = AbstractRecipeLogic.class, remap = false)
public abstract class CrucibleComponentRecipeMixin {
    @Shadow protected List<ItemStack> itemOutputs;
    @Shadow protected boolean isOutputsFull;
    @Shadow protected abstract IItemHandlerModifiable getOutputInventory();
    @Unique private CrucibleComponentProvenance.Preparation gt6addition$components;

    @Inject(method = "setupAndConsumeRecipeInputs(Lgregtech/api/recipes/Recipe;Lnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/IMultipleTankHandler;)Lgregtech/api/recipes/Recipe;",
            at = @At("HEAD"), remap = false)
    private void gt6addition$clearComponents(Recipe recipe, IItemHandlerModifiable items, IMultipleTankHandler fluids,
                                            CallbackInfoReturnable<Recipe> ci) {
        gt6addition$components = null;
    }

    @Redirect(method = "setupAndConsumeRecipeInputs(Lgregtech/api/recipes/Recipe;Lnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/IMultipleTankHandler;)Lgregtech/api/recipes/Recipe;",
            at = @At(value = "INVOKE", target = "Lgregtech/api/recipes/Recipe;matches(ZLnet/minecraftforge/items/IItemHandlerModifiable;Lgregtech/api/capability/IMultipleTankHandler;)Z"), remap = false)
    private boolean gt6addition$captureConsumedComponents(Recipe recipe, boolean consume,
                                                          IItemHandlerModifiable items, IMultipleTankHandler fluids) {
        if (!consume || !CrucibleComponentProvenance.tracks(recipe)) return recipe.matches(consume, items, fluids);
        CrucibleComponentProvenance.Preparation prepared = CrucibleComponentProvenance.prepare(recipe, items, fluids);
        // The host's preceding plain-template check cannot detect account NBT
        // mismatches. Recheck real outputs before any input/energy is consumed.
        AbstractRecipeLogic host = (AbstractRecipeLogic) (Object) this;
        if (!host.getMetaTileEntity().canVoidRecipeItemOutputs() &&
                !GTTransferUtils.addItemsToItemHandler(getOutputInventory(), true,
                        prepared.decorate(recipe.getAllItemOutputs()))) {
            isOutputsFull = true;
            return false;
        }
        if (!recipe.matches(true, items, fluids)) return false;
        prepared.verifyConsumed(items, fluids);
        gt6addition$components = prepared;
        return true;
    }

    @Inject(method = "setupRecipe", at = @At("RETURN"), remap = false)
    private void gt6addition$bindComponentOutputs(Recipe recipe, CallbackInfo ci) {
        if (gt6addition$components != null) {
            itemOutputs = gt6addition$components.decorate(itemOutputs);
            gt6addition$components = null;
        }
        // ItemOutputs already have the host's normal in-progress NBT lifecycle.
    }
}
