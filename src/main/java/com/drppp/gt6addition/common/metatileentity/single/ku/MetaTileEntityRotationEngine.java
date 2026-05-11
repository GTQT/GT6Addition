package com.drppp.gt6addition.common.metatileentity.single.ku;

import com.drppp.gt6addition.api.baseMTile.BaseEnergyOutputMetaTileEntity;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.KineticEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IKineticEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityRotationEngine extends BaseEnergyOutputMetaTileEntity {

    public final int outPutRu;
    public final int outPutRuMin;
    public final int outPutRuMax;
    public int AllRu = 0;

    protected final IKineticEnergy ku = new KineticEnergyHandler();

    public MetaTileEntityRotationEngine(ResourceLocation metaTileEntityId, int color, int outPutRu) {
        super(metaTileEntityId, color, Gt6AdditionTextures.RU_KU_ENGINE);
        this.outPutRu = outPutRu;
        this.outPutRuMin = outPutRu / 2;
        this.outPutRuMax = outPutRu * 2;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityRotationEngine(this.metaTileEntityId, this.color, this.outPutRu);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.1"));
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.2", this.outPutRu, this.outPutRuMin,
                this.outPutRuMax));
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.3"));
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.4"));
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.5"));
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote || getOffsetTimer() % 5 != 0) {
            return;
        }

        this.AllRu = collectInputRu();
        int output = calculateKineticOutput(this.AllRu);
        this.ku.setKineticEnergy(output);
        setActive(output > 0);
    }

    private int collectInputRu() {
        int totalRu = 0;
        EnumFacing back = this.getFrontFacing().getOpposite();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side == this.getFrontFacing() || side == back) {
                continue;
            }

            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));
            if (tileEntity == null || !tileEntity.hasCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY,
                    side.getOpposite())) {
                continue;
            }

            IRotationEnergy rotationEnergy = tileEntity.getCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY,
                    side.getOpposite());
            if (rotationEnergy != null) {
                totalRu += rotationEnergy.getEnergyOutput();
            }
        }
        return totalRu;
    }

    private int calculateKineticOutput(int inputRu) {
        if (inputRu < this.outPutRuMin) {
            return 0;
        }
        if (inputRu > this.outPutRuMax) {
            return this.outPutRuMax / 2;
        }
        return inputRu / 2;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityHandler.CAPABILITY_KINETIC_ENERGY
                && (facing == this.frontFacing || facing == this.frontFacing.getOpposite())) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_KINETIC_ENERGY
                && (side == this.frontFacing || side == this.frontFacing.getOpposite())) {
            return CapabilityHandler.CAPABILITY_KINETIC_ENERGY.cast(ku);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public String getEnergyName() {
        return "KU";
    }

    @Override
    public int getEnergyOut() {
        return this.ku.getKinetic();
    }
}
