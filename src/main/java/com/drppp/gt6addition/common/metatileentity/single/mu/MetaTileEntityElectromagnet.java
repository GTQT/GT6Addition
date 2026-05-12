package com.drppp.gt6addition.common.metatileentity.single.mu;

import com.drppp.gt6addition.api.baseMTile.BaseTieredEnergyOutputMetaTileEntity;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.MagnetEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IMagnetEnergy;
import com.drppp.gt6addition.api.utils.EnergyConversionHelper;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityElectromagnet extends BaseTieredEnergyOutputMetaTileEntity {

    public final double efficiency;
    public final int outPutRu;
    public final int minSteamUse;
    public final int maxSteamUse;

    protected final IMagnetEnergy mu = new MagnetEnergyHandler();

    public MetaTileEntityElectromagnet(ResourceLocation metaTileEntityId, int tier, int color, double efficiency,
                                       int outPutRu) {
        super(metaTileEntityId, tier, color, Gt6AdditionTextures.MU_ELECTROMAGNET);
        this.efficiency = efficiency;
        this.outPutRu = outPutRu;
        this.minSteamUse = EnergyConversionHelper.minimumInputForNominalOutput(this.outPutRu, this.efficiency);
        this.maxSteamUse = EnergyConversionHelper.maximumInputForDoubleOutput(this.outPutRu, this.efficiency);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityElectromagnet(this.metaTileEntityId, this.getTier(), this.color, this.efficiency,
                this.outPutRu);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.mu.em_generator.info.1", this.efficiency * 100 + "%"));
        tooltip.add(I18n.format("gt6addition.mu.em_generator.info.2", this.outPutRu, this.outPutRu / 2,
                this.outPutRu * 2));
        tooltip.add(I18n.format("gt6addition.mu.em_generator.info.3"));
        tooltip.add(I18n.format("gt6addition.mu.em_generator.info.4"));
        tooltip.add(I18n.format("gt6addition.mu.em_generator.info.5"));
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }
        this.mu.setMuEnergy(consumeEuForOutput(this.outPutRu, this.minSteamUse, this.maxSteamUse, this.efficiency));
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_MAGNET_ENERGY
                && (side == this.frontFacing || side == this.frontFacing.getOpposite())) {
            return true;
        }
        return super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_MAGNET_ENERGY
                && (side == this.frontFacing || side == this.frontFacing.getOpposite())) {
            return CapabilityHandler.CAPABILITY_MAGNET_ENERGY.cast(mu);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public String getEnergyName() {
        return "MU";
    }

    @Override
    public int getEnergyOut() {
        return this.mu.getMagnet();
    }
}
