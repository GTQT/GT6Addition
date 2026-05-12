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

public class MetaTileEntityMiniPortalNether extends MetaTileEntityMiniPortal {

    private static final List<MetaTileEntityMiniPortal> OVERWORLD_PORTALS = new ArrayList<>();
    private static final List<MetaTileEntityMiniPortal> NETHER_PORTALS = new ArrayList<>();

    public MetaTileEntityMiniPortalNether(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMiniPortalNether(metaTileEntityId);
    }

    @Override
    protected int getRemoteDimensionId() {
        return -1;
    }

    @Override
    protected int getDistanceFactor() {
        return 8;
    }

    @Override
    protected int getDistanceMargin() {
        return 128;
    }

    @Override
    protected List<MetaTileEntityMiniPortal> getPrimaryPortalList() {
        return OVERWORLD_PORTALS;
    }

    @Override
    protected List<MetaTileEntityMiniPortal> getSecondaryPortalList() {
        return NETHER_PORTALS;
    }

    @Override
    protected boolean canActivateWithItem(ItemStack heldStack) {
        return !heldStack.isEmpty() && heldStack.getItem() == Items.FLINT_AND_STEEL;
    }

    @Override
    protected void onActivationItemUsed(EntityPlayer player, EnumHand hand, ItemStack heldStack) {
        if (!player.capabilities.isCreativeMode) {
            heldStack.damageItem(1, player);
        }
    }

    @Override
    protected String getPortalTextureId() {
        return "minecraft:blocks/portal";
    }

    @Override
    protected String getFrameTextureId() {
        return "minecraft:blocks/obsidian";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.portal_nether.tooltip.1"));
        tooltip.add(I18n.format("gt6addition.machine.portal_nether.tooltip.2"));
        tooltip.add(I18n.format("gt6addition.machine.portal.tooltip.common"));
    }
}
