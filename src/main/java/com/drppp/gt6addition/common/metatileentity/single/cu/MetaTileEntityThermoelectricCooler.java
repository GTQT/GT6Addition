package com.drppp.gt6addition.common.metatileentity.single.cu;

import com.drppp.gt6addition.api.baseMTile.BaseTieredEnergyOutputMetaTileEntity;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.ColdEnergyHandler;
import com.drppp.gt6addition.api.capability.impl.HeatEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IColdEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IHeatEnergy;
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

public class MetaTileEntityThermoelectricCooler extends BaseTieredEnergyOutputMetaTileEntity {

    public final double efficiency;
    public final int outPutRu;
    public final int minEuUse;
    public final int maxEuUse;

    protected final IColdEnergy cu = new ColdEnergyHandler();
    protected final IHeatEnergy hu = new HeatEnergyHandler();

    public MetaTileEntityThermoelectricCooler(ResourceLocation metaTileEntityId, int tier, int color,
                                              double efficiency, int outPutRu) {
        super(metaTileEntityId, tier, color, Gt6AdditionTextures.CU_THERMOELECTRIC_COOLER);
        this.efficiency = efficiency;
        this.outPutRu = outPutRu;
        this.minEuUse = (int) (this.outPutRu / this.efficiency);
        this.maxEuUse = (int) (this.outPutRu * 2 / this.efficiency);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityThermoelectricCooler(this.metaTileEntityId, this.getTier(), this.color,
                this.efficiency, this.outPutRu);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.cu.tc_generator.info.1", this.efficiency * 100 + "%"));
        tooltip.add(I18n.format("gt6addition.cu.tc_generator.info.2", this.outPutRu, this.outPutRu / 2,
                this.outPutRu * 2));
        tooltip.add(I18n.format("gt6addition.cu.tc_generator.info.3"));
        tooltip.add(I18n.format("gt6addition.cu.tc_generator.info.4"));
        tooltip.add(I18n.format("gt6addition.cu.tc_generator.info.5"));
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        int output = consumeEuForOutput(this.outPutRu, this.minEuUse, this.maxEuUse, this.efficiency);
        this.hu.setHuEnergy(output);
        this.cu.setCuEnergy(output);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_COLD_ENERGY && side == this.frontFacing) {
            return true;
        }
        if (capability == CapabilityHandler.CAPABILITY_HEAT_ENERGY && side == this.frontFacing.getOpposite()) {
            return true;
        }
        return super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_COLD_ENERGY && side == this.frontFacing) {
            return CapabilityHandler.CAPABILITY_COLD_ENERGY.cast(this.cu);
        }
        if (capability == CapabilityHandler.CAPABILITY_HEAT_ENERGY && side == this.frontFacing.getOpposite()) {
            return CapabilityHandler.CAPABILITY_HEAT_ENERGY.cast(this.hu);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public String getEnergyName() {
        return "CU";
    }

    @Override
    public int getEnergyOut() {
        return this.cu.getCold();
    }
}