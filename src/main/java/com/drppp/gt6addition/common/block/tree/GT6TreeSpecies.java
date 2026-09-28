package com.drppp.gt6addition.common.block.tree;

import net.minecraft.block.BlockPlanks;

/** Tree species ported from the GT6 tree set. */
public enum GT6TreeSpecies {
    MAPLE("maple", BlockPlanks.EnumType.ACACIA),
    WILLOW("willow", BlockPlanks.EnumType.BIRCH),
    HAZEL("hazel", BlockPlanks.EnumType.OAK),
    COCONUT("coconut", BlockPlanks.EnumType.JUNGLE),
    BLUE_SPRUCE("blue_spruce", BlockPlanks.EnumType.SPRUCE);

    private final String key;
    private final BlockPlanks.EnumType vanillaWoodType;

    GT6TreeSpecies(String key, BlockPlanks.EnumType vanillaWoodType) {
        this.key = key;
        this.vanillaWoodType = vanillaWoodType;
    }

    public String getKey() {
        return key;
    }

    public BlockPlanks.EnumType getVanillaWoodType() {
        return vanillaWoodType;
    }
}
