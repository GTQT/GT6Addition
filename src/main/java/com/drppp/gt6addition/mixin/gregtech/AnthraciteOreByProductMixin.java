package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.material.GT6MachineMaterials;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.integration.jei.basic.OreByProduct;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** CEu's overview is a schematic, not generated from the registered machine recipes. */
@Mixin(value = OreByProduct.class, remap = false)
public abstract class AnthraciteOreByProductMixin {
    @Shadow @Final private static int NUM_INPUTS;
    @Shadow @Final private List<List<ItemStack>> outputs;
    @Shadow @Final private Int2ObjectMap<ChancedItemOutput> chances;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gt6addition$anthraciteByproducts(Material material, CallbackInfo ci) {
        if (material != GT6MachineMaterials.ANTHRACITE) return;
        for (int i = 0; i < outputs.size(); i++) {
            List<ItemStack> stacks = outputs.get(i);
            for (int j = 0; j < stacks.size(); j++) {
                ItemStack stack = stacks.get(j);
                MaterialStack outputMaterial = OreDictUnifier.getMaterial(stack);
                if (outputMaterial != null && outputMaterial.material == Materials.Coal) {
                    stacks.set(j, OreDictUnifier.get(OrePrefix.dust, Materials.Coal, stack.getCount()));
                    if (chances.containsKey(i + NUM_INPUTS)) {
                        chances.put(i + NUM_INPUTS, new ChancedItemOutput(ItemStack.EMPTY, 5000, 0));
                    }
                }
            }
        }
    }
}
