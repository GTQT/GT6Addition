package com.drppp.gt6addition.common.item;

import com.drppp.gt6addition.Tags;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.items.behavior.CoverItemBehavior;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemDrainCover extends Item {

    private CoverItemBehavior coverItemBehavior;

    public ItemDrainCover() {
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "drain_cover"));
        setTranslationKey(Tags.MOD_ID + ".drain_cover");
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC);
    }

    public void setCoverDefinition(CoverDefinition definition) {
        this.coverItemBehavior = new CoverItemBehavior(definition);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side,
                                            float hitX, float hitY, float hitZ, EnumHand hand) {
        return coverItemBehavior == null ? EnumActionResult.PASS :
                coverItemBehavior.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        for (int i = 1; i <= 4; i++) {
            tooltip.add(TextFormatting.GRAY + I18n.format("gt6addition.cover.drain.tooltip." + i));
        }
    }
}
