package com.drppp.gt6addition.common.cover;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.item.GT6AdditionItems;
import gregtech.api.cover.CoverDefinition;
import gregtech.common.covers.CoverBehaviors;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class GT6AdditionCovers {

    private static boolean registered;
    private static CoverDefinition drainCover;

    private GT6AdditionCovers() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        drainCover = CoverBehaviors.registerCover(
                new ResourceLocation(Tags.MOD_ID, "drain_cover"),
                new ItemStack(GT6AdditionItems.DRAIN_COVER),
                CoverGT6Drain::new);
        GT6AdditionItems.DRAIN_COVER.setCoverDefinition(drainCover);
    }
}
