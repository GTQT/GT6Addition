package com.drppp.gt6addition.common.item;

import com.drppp.gt6addition.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class GT6AdditionItems {

    public static final Item CLAY_CRUCIBLE = clayItem("clay_crucible");
    public static final Item CLAY_SPOUT = clayItem("clay_spout");
    public static final Item CLAY_CHANNEL = clayItem("clay_channel");
    public static final Item CLAY_BASIN = clayItem("clay_basin");
    public static final Item CLAY_MOLD = clayItem("clay_mold");

    private GT6AdditionItems() {
    }

    private static Item clayItem(String name) {
        return new Item()
                .setRegistryName(new ResourceLocation(Tags.MOD_ID, name))
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setCreativeTab(CreativeTabs.MISC);
    }
}
