package com.drppp.gt6addition.common.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import com.drppp.gt6addition.Tags;
import net.minecraft.util.ResourceLocation;

public final class GT6AdditionBlocks {
    public static final Block COKE_OVEN_BRICK = new BlockCokeOvenBrick();
    public static final ItemBlock COKE_OVEN_BRICK_ITEM = (ItemBlock) new ItemBlock(COKE_OVEN_BRICK)
            .setRegistryName(new ResourceLocation(Tags.MOD_ID, "coke_oven_brick"));

    private GT6AdditionBlocks() {}
}
