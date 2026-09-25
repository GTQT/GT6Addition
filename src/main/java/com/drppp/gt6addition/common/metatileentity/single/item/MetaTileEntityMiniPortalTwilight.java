package com.drppp.gt6addition.common.metatileentity.single.item;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Optional GT6-style Overworld/Twilight Forest mini portal without a hard TF dependency. */
public class MetaTileEntityMiniPortalTwilight extends MetaTileEntityMiniPortal {

    private static final int DIMENSION_NOT_FOUND = Integer.MIN_VALUE;
    private static int cachedTwilightDimension = DIMENSION_NOT_FOUND;
    private static final List<MetaTileEntityMiniPortal> OVERWORLD_PORTALS = new ArrayList<>();
    private static final List<MetaTileEntityMiniPortal> TWILIGHT_PORTALS = new ArrayList<>();

    public MetaTileEntityMiniPortalTwilight(ResourceLocation id) {
        super(id);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMiniPortalTwilight(metaTileEntityId);
    }

    @Override
    protected int getRemoteDimensionId() {
        if (!Loader.isModLoaded("twilightforest")) return Integer.MIN_VALUE;
        if (cachedTwilightDimension != DIMENSION_NOT_FOUND) return cachedTwilightDimension;
        int byType = findTwilightDimensionFromRegisteredTypes();
        cachedTwilightDimension = byType != DIMENSION_NOT_FOUND ? byType : findTwilightDimensionFromModConfig();
        return cachedTwilightDimension;
    }

    private int findTwilightDimensionFromRegisteredTypes() {
        Integer[] dimensions = DimensionManager.getStaticDimensionIDs();
        if (dimensions == null) return Integer.MIN_VALUE;
        for (Integer dimension : dimensions) {
            if (dimension == null || dimension == 0 || dimension == -1 || dimension == 1) continue;
            DimensionType type = DimensionManager.getProviderType(dimension);
            if (type == null) continue;
            String typeName = type.getName().toLowerCase(java.util.Locale.ROOT);
            String providerName = type.createDimension().getClass().getName().toLowerCase(java.util.Locale.ROOT);
            if (typeName.contains("twilight") || providerName.contains("twilightforest")) return dimension;
        }
        return Integer.MIN_VALUE;
    }

    private int findTwilightDimensionFromModConfig() {
        try {
            Class<?> modClass = Class.forName("twilightforest.TwilightForestMod", false,
                    getClass().getClassLoader());
            for (String fieldName : new String[]{"dimensionID", "dimensionId", "DIMENSION_ID"}) {
                try {
                    Field field = modClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return ((Number) field.get(null)).intValue();
                } catch (NoSuchFieldException ignored) {
                    // Try the next known field spelling.
                }
            }
        } catch (Throwable ignored) {
            // Twilight Forest is optional; a missing/unreadable API leaves this portal inactive.
        }
        return Integer.MIN_VALUE;
    }

    @Override
    protected int getDistanceFactor() {
        return 1;
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
        return TWILIGHT_PORTALS;
    }

    @Override
    protected boolean canActivateWithItem(ItemStack heldStack) {
        return !heldStack.isEmpty() && heldStack.getItem() == Items.DIAMOND;
    }

    @Override
    protected void onActivationItemUsed(EntityPlayer player, EnumHand hand, ItemStack heldStack) {
        if (!player.capabilities.isCreativeMode) {
            heldStack.shrink(1);
            player.setHeldItem(hand, heldStack.isEmpty() ? ItemStack.EMPTY : heldStack);
        }
        if (getWorld() != null && !getWorld().isRemote) {
            getWorld().addWeatherEffect(new net.minecraft.entity.effect.EntityLightningBolt(
                    getWorld(), getPos().getX() + 0.5D, getPos().getY(), getPos().getZ() + 0.5D, false));
        }
    }

    @Override
    protected String getPortalTextureId() {
        return "minecraft:blocks/portal";
    }

    @Override
    protected String getFrameTextureId() {
        return "minecraft:blocks/grass_top";
    }

    @Override
    protected int getFrameColor() {
        // GT6 tints the vanilla grass-top frame green.
        return 0x66AA44;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.portal_twilight.tooltip.1"));
        tooltip.add(I18n.format("gt6addition.machine.portal_twilight.tooltip.2"));
        tooltip.add(I18n.format("gt6addition.machine.portal.tooltip.common"));
    }
}
