package com.drppp.gt6addition.api.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CraftingGetItemUtils {
    public static ItemStack getItemStack(String itemstr) {
        return getItemStack(itemstr, 0);
    }

    public static ItemStack getItemStack(String itemstr, int num) {
        var item = getItemStack(itemstr, 0);
        item.setCount(num);
        return item;
    }
    public static ItemStack getItemStackFromCrt(String itemstr, int meta) {
        if (itemstr.startsWith("<") && itemstr.endsWith(">"))
            itemstr = itemstr.substring(1, itemstr.length() - 1);
        if(itemstr.startsWith("item"))
        {
            String content = itemstr.substring(itemstr.indexOf('(') + 1, itemstr.lastIndexOf(')'));
            String[] parts = content.split("\\s*,\\s*");
            itemstr=parts[0].replace('\'',' ').trim();
            meta=Integer.parseInt(parts[1]);
        }
        String[] str = itemstr.split(":");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(str[0], str[1]));
        if (item != null) {
            if (str.length == 3)
                return new ItemStack(item, 1, Integer.parseInt(str[2]));
            return new ItemStack(item, 1, meta);
        } else {
            return ItemStack.EMPTY;
        }
    }
}
