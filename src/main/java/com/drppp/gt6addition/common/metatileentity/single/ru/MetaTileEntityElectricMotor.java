package com.drppp.gt6addition.common.metatileentity.single.ru;

import com.drppp.gt6addition.api.baseMTile.BaseTieredEnergyOutputMetaTileEntity;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.RotationEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
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

public class MetaTileEntityElectricMotor extends BaseTieredEnergyOutputMetaTileEntity {

    public final double efficiency;
    public final int outPutRu;
    public final int minEuUse;
    public final int maxEuUse;

    protected final IRotationEnergy ru = new RotationEnergyHandler();

    public MetaTileEntityElectricMotor(ResourceLocation metaTileEntityId, int tier, int color, double efficiency,
                                       int outPutRu) {
        super(metaTileEntityId, tier, color, Gt6AdditionTextures.RU_ELECTRIC_MOTOR);
        this.efficiency = efficiency;
        this.outPutRu = outPutRu;
        this.minEuUse = (int) (this.outPutRu / this.efficiency);
        this.maxEuUse = (int) (this.outPutRu * 2 / this.efficiency);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityElectricMotor(this.metaTileEntityId, this.getTier(), this.color, this.efficiency,
                this.outPutRu);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.1", this.efficiency * 100 + "%"));
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.2", this.outPutRu, this.outPutRu / 2,
                this.outPutRu * 2));
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.3"));
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.4"));
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.5"));
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            rotateEntitiesAbove(6.0f);
            return;
        }

        this.ru.setRuEnergy(consumeEuForOutput(this.outPutRu, this.minEuUse, this.maxEuUse, this.efficiency));
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && facing == this.frontFacing) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && side == this.frontFacing) {
            return CapabilityHandler.CAPABILITY_ROTATION_ENERGY.cast(ru);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public String getEnergyName() {
        return "RU";
    }

    @Override
    public int getEnergyOut() {
        return this.ru.getEnergyOutput();
    }
}