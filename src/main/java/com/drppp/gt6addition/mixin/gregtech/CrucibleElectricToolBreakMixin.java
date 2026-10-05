package com.drppp.gt6addition.mixin.gregtech;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleElectricProvenance;
import gregtech.api.items.toolitem.IGTToolDefinition;
import gregtech.common.ToolEventHandlers;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ToolEventHandlers.class, remap = false)
public abstract class CrucibleElectricToolBreakMixin {
    @Redirect(method = "onPlayerDestroyItem", at = @At(value = "INVOKE",
            target = "Lgregtech/api/items/toolitem/IGTToolDefinition;getBrokenStack()Lnet/minecraft/item/ItemStack;"),
            remap = false)
    private static ItemStack gt6addition$preserveBrokenPowerUnit(IGTToolDefinition definition,
                                                               PlayerDestroyItemEvent event) {
        return CrucibleElectricProvenance.inheritBrokenPowerUnit(definition.getBrokenStack(), event.getOriginal());
    }
}
