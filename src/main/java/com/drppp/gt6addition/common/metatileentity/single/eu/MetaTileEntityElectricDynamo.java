package com.drppp.gt6addition.common.metatileentity.single.eu;

import com.drppp.gt6addition.api.baseMTile.BaseTieredEnergyOutputMetaTileEntity;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityElectricDynamo extends BaseTieredEnergyOutputMetaTileEntity {

    private static final SimpleSidedCubeRenderer BASE_RENDERER =
            new SimpleSidedCubeRenderer("gt6addition:machines/dynamos/electric_dynamo/colored");
    private static final OrientedOverlayRenderer OVERLAY_RENDERER =
            new OrientedOverlayRenderer("gt6addition:machines/dynamos/electric_dynamo");

    private static final double EFFICIENCY = 11.0D / 16.0D;

    private int currentOutput;

    public MetaTileEntityElectricDynamo(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, 0xFFFFFF, OVERLAY_RENDERER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityElectricDynamo(metaTileEntityId, getTier());
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 8L;
    }

    @Override
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return BASE_RENDERER;
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        int inputRu = getInputRu();
        int nominalInput = (int) GTValues.V[getTier()];
        if (inputRu < nominalInput) {
            currentOutput = 0;
            setActive(false);
            return;
        }

        int consumedRu = Math.min(inputRu, nominalInput * 2);
        int producedEu = Math.max(1, (int) Math.floor(consumedRu * EFFICIENCY));
        currentOutput = (int) Math.max(0L, energyContainer.changeEnergy(producedEu));
        setActive(currentOutput > 0);
    }

    private int getInputRu() {
        TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(getFrontFacing().getOpposite()));
        if (tileEntity == null ||
                !tileEntity.hasCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY, getFrontFacing())) {
            return 0;
        }

        IRotationEnergy rotationEnergy =
                tileEntity.getCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY, getFrontFacing());
        return rotationEnergy == null ? 0 : rotationEnergy.getEnergyOutput();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        int nominalInput = (int) GTValues.V[getTier()];
        int nominalOutput = (int) Math.floor(nominalInput * EFFICIENCY);
        tooltip.add(I18n.format("gt6addition.eu.dynamo.info.1", String.format("%.2f%%", EFFICIENCY * 100.0D)));
        tooltip.add(I18n.format("gt6addition.eu.dynamo.info.2", nominalInput, nominalInput * 2));
        tooltip.add(I18n.format("gt6addition.eu.dynamo.info.3", nominalOutput, nominalOutput * 2));
        tooltip.add(I18n.format("gt6addition.eu.dynamo.info.4"));
    }

    @Override
    public String getEnergyName() {
        return "EU";
    }

    @Override
    public int getEnergyOut() {
        return currentOutput;
    }
}
