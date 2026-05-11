package com.drppp.gt6addition.common.metatileentity.single.eu;

import com.drppp.gt6addition.api.baseMTile.BaseTieredEnergyOutputMetaTileEntity;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.LaserEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.ILaserEnergy;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityElectricCo2Laser extends BaseTieredEnergyOutputMetaTileEntity {

    private static final SimpleSidedCubeRenderer BASE_RENDERER =
            new SimpleSidedCubeRenderer("gt6addition:machines/lasers/co2_laser/colored");
    private static final OrientedOverlayRenderer OVERLAY_RENDERER =
            new OrientedOverlayRenderer("gt6addition:machines/lasers/co2_laser");

    private static final double EFFICIENCY = 0.5D;

    private final ILaserEnergy laserEnergy = new LaserEnergyHandler();

    public MetaTileEntityElectricCo2Laser(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, 0xFFFFFF, OVERLAY_RENDERER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityElectricCo2Laser(metaTileEntityId, getTier());
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

        int nominalInput = (int) GTValues.V[getTier()];
        long stored = energyContainer.getEnergyStored();
        if (stored < nominalInput) {
            laserEnergy.setLuEnergy(0);
            laserEnergy.setOutPut(false);
            setActive(false);
            return;
        }

        int consumedEu = (int) Math.min(stored, nominalInput * 2L);
        energyContainer.removeEnergy(consumedEu);
        laserEnergy.setLuEnergy(Math.max(1, (int) Math.floor(consumedEu * EFFICIENCY)));
        laserEnergy.setOutPut(true);
        setActive(true);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityHandler.CAPABILITY_LASER_ENERGY && facing == getFrontFacing()) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_LASER_ENERGY && side == getFrontFacing()) {
            return CapabilityHandler.CAPABILITY_LASER_ENERGY.cast(laserEnergy);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        int nominalInput = (int) GTValues.V[getTier()];
        int nominalOutput = nominalInput / 2;
        tooltip.add(I18n.format("gt6addition.lu.laser.info.1", String.format("%.0f%%", EFFICIENCY * 100.0D)));
        tooltip.add(I18n.format("gt6addition.lu.laser.info.2", nominalInput, nominalInput * 2));
        tooltip.add(I18n.format("gt6addition.lu.laser.info.3", nominalOutput, nominalOutput * 2));
        tooltip.add(I18n.format("gt6addition.lu.laser.info.4"));
    }

    @Override
    public String getEnergyName() {
        return "LU";
    }

    @Override
    public int getEnergyOut() {
        return laserEnergy.getEnergyOutput();
    }
}
