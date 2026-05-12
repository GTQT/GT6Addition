package com.drppp.gt6addition.common.metatileentity.single.item;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityMiniPortalEnd extends MetaTileEntityMiniPortal {

    private static final List<MetaTileEntityMiniPortal> OVERWORLD_PORTALS = new ArrayList<>();
    private static final List<MetaTileEntityMiniPortal> END_PORTALS = new ArrayList<>();

    public MetaTileEntityMiniPortalEnd(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMiniPortalEnd(metaTileEntityId);
    }

    @Override
    protected int getRemoteDimensionId() {
        return 1;
    }

    @Override
    protected int getDistanceFactor() {
        return 128;
    }

    @Override
    protected int getDistanceMargin() {
        return 512;
    }

    @Override
    protected List<MetaTileEntityMiniPortal> getPrimaryPortalList() {
        return OVERWORLD_PORTALS;
    }

    @Override
    protected List<MetaTileEntityMiniPortal> getSecondaryPortalList() {
        return END_PORTALS;
    }

    @Override
    protected boolean canActivateWithItem(ItemStack heldStack) {
        return !heldStack.isEmpty() && heldStack.getItem() == Items.ENDER_EYE;
    }

    @Override
    protected void onActivationItemUsed(EntityPlayer player, EnumHand hand, ItemStack heldStack) {
        if (!player.capabilities.isCreativeMode) {
            heldStack.shrink(1);
            player.setHeldItem(hand, heldStack.isEmpty() ? ItemStack.EMPTY : heldStack);
        }
    }

    @Override
    protected String getPortalTextureId() {
        return "minecraft:blocks/portal";
    }

    @Override
    protected String getFrameTextureId() {
        return "minecraft:blocks/end_stone";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.portal_end.tooltip.1"));
        tooltip.add(I18n.format("gt6addition.machine.portal_end.tooltip.2"));
        tooltip.add(I18n.format("gt6addition.machine.portal.tooltip.common"));
    }
}
