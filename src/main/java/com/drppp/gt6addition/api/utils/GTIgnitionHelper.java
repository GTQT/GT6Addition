package com.drppp.gt6addition.api.utils;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import gregtech.common.items.behaviors.LighterBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public final class GTIgnitionHelper {

    private GTIgnitionHelper() {
    }

    public static boolean isIgnitionItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (isVanillaOrGTFlintAndSteel(stack)
                || MetaItems.TOOL_MATCHES.isItemEqual(stack)
                || MetaItems.TOOL_MATCHBOX.isItemEqual(stack)) {
            return true;
        }
        return hasFuel(stack, MetaItems.TOOL_LIGHTER_INVAR)
                || hasFuel(stack, MetaItems.TOOL_LIGHTER_PLATINUM);
    }

    public static boolean consumeIgnitionUse(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() == Items.FLINT_AND_STEEL) {
            if (!player.capabilities.isCreativeMode) {
                stack.damageItem(1, player);
            }
            return true;
        }
        if (isGTFlintAndSteel(stack)) {
            // GT's tool behavior damages this item even when used by a creative player.
            stack.damageItem(1, player);
            return true;
        }
        return consumeFuel(player, stack, MetaItems.TOOL_MATCHES)
                || consumeFuel(player, stack, MetaItems.TOOL_MATCHBOX)
                || consumeFuel(player, stack, MetaItems.TOOL_LIGHTER_INVAR)
                || consumeFuel(player, stack, MetaItems.TOOL_LIGHTER_PLATINUM);
    }

    private static boolean isVanillaOrGTFlintAndSteel(ItemStack stack) {
        return stack.getItem() == Items.FLINT_AND_STEEL || isGTFlintAndSteel(stack);
    }

    private static boolean isGTFlintAndSteel(ItemStack stack) {
        ItemStack gtFlintAndSteel = ToolItems.FLINT_AND_STEEL.getRaw();
        return !gtFlintAndSteel.isEmpty() && stack.getItem() == gtFlintAndSteel.getItem();
    }

    private static boolean hasFuel(ItemStack stack, MetaItem<?>.MetaValueItem lighter) {
        if (!lighter.isItemEqual(stack)) {
            return false;
        }
        IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,
                null);
        if (fluidHandler == null) {
            return false;
        }
        FluidStack fluid = fluidHandler.drain(Integer.MAX_VALUE, false);
        return fluid != null && fluid.amount > 0;
    }

    private static boolean consumeFuel(EntityPlayer player, ItemStack stack, MetaItem<?>.MetaValueItem lighter) {
        if (!lighter.isItemEqual(stack)) {
            return false;
        }
        for (IItemBehaviour behaviour : lighter.getBehaviours()) {
            if (behaviour instanceof LighterBehaviour) {
                return ((LighterBehaviour) behaviour).consumeFuel(player, stack);
            }
        }
        return false;
    }
}
