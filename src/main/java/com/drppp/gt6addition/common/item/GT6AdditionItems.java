package com.drppp.gt6addition.common.item;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

public final class GT6AdditionItems {

    public static final Item CLAY_CRUCIBLE = clayItem("clay_crucible");
    public static final Item CLAY_SPOUT = clayItem("clay_spout");
    public static final Item CLAY_CHANNEL = clayItem("clay_channel");
    public static final Item CLAY_BASIN = clayItem("clay_basin");
    public static final Item CLAY_MOLD = clayItem("clay_mold");
    public static final ItemDrainCover DRAIN_COVER = new ItemDrainCover();
    public static final ItemBlock ANTHRACITE_ORE = createAnthraciteOreItem();
    public static final ItemBlock[] TREE_LOGS = createBlockItems(GT6AdditionBlocks.TREE_LOGS);
    public static final ItemBlock[] TREE_LEAVES = createBlockItems(GT6AdditionBlocks.TREE_LEAVES);
    public static final ItemBlock[] TREE_SAPLINGS = createBlockItems(GT6AdditionBlocks.TREE_SAPLINGS);

    private GT6AdditionItems() {
    }

    private static Item clayItem(String name) {
        return new Item()
                .setRegistryName(new ResourceLocation(Tags.MOD_ID, name))
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(CreativeTabs.MISC);
    }

    private static ItemBlock createAnthraciteOreItem() {
        ItemBlock item = new ItemBlock(GT6AdditionBlocks.ANTHRACITE_ORE);
        item.setRegistryName(GT6AdditionBlocks.ANTHRACITE_ORE.getRegistryName());
        return item;
    }

    private static ItemBlock[] createBlockItems(net.minecraft.block.Block[] blocks) {
        ItemBlock[] items = new ItemBlock[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            items[i] = new ItemBlock(blocks[i]);
            items[i].setRegistryName(blocks[i].getRegistryName());
        }
        return items;
    }

    public static ItemBlock[] getTreeItems() {
        ItemBlock[] items = new ItemBlock[TREE_LOGS.length + TREE_LEAVES.length + TREE_SAPLINGS.length];
        int index = 0;
        for (ItemBlock item : TREE_LOGS) items[index++] = item;
        for (ItemBlock item : TREE_LEAVES) items[index++] = item;
        for (ItemBlock item : TREE_SAPLINGS) items[index++] = item;
        return items;
    }
}
