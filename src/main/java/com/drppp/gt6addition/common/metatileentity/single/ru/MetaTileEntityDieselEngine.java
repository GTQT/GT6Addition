package com.drppp.gt6addition.common.metatileentity.single.ru;

import com.drppp.gt6addition.api.baseMTile.BaseEnergyOutputMetaTileEntity;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.RotationEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.metatileentity.single.hu.LiquidBurringInfo;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityDieselEngine extends BaseEnergyOutputMetaTileEntity {

    private static final String NBT_BURN_TIME = "currentItemBurnTime";
    private static final String NBT_BURNED_TIME = "currentItemHasBurnedTime";
    private static final int TANK_CAPACITY = 1000;

    public final int outPutRu;
    public int currentItemBurnTime = 0;
    public int currentItemHasBurnedTime = 0;

    protected final IRotationEnergy ru = new RotationEnergyHandler();
    protected FluidTank fuelFluidTank;
    protected FluidTank outFluidTank;

    public MetaTileEntityDieselEngine(ResourceLocation metaTileEntityId, int color, int outPutRu) {
        super(metaTileEntityId, color, Gt6AdditionTextures.RU_DIESEL_ENGINE);
        this.outPutRu = outPutRu;
        this.initializeInventory();
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.importFluids = this.createImportFluidHandler();
        this.exportFluids = this.createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        this.fuelFluidTank = new FuelFluidTank(TANK_CAPACITY);
        return new FluidTankList(false, fuelFluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        this.outFluidTank = new FilteredFluidHandler(TANK_CAPACITY).setFilter(new IFilter<FluidStack>() {
            @Override
            public boolean test(@NotNull FluidStack fluid) {
                return CommonFluidFilters.matchesFluid(fluid, Materials.CarbonDioxide);
            }

            @Override
            public int getPriority() {
                return IFilter.whitelistPriority(1);
            }
        });
        return new FluidTankList(false, outFluidTank);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityDieselEngine(this.metaTileEntityId, this.color, this.outPutRu);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger(NBT_BURN_TIME, currentItemBurnTime);
        data.setInteger(NBT_BURNED_TIME, currentItemHasBurnedTime);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        currentItemBurnTime = data.getInteger(NBT_BURN_TIME);
        currentItemHasBurnedTime = data.getInteger(NBT_BURNED_TIME);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.1", 100 + "%"));
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.2", this.outPutRu));
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.3"));
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.4"));
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.5"));
    }

    public boolean canActive() {
        FluidStack fuel = this.fuelFluidTank.getFluid();
        return fuel != null && this.fuelFluidTank.getFluidAmount() * LiquidBurringInfo.getMlHu(fuel) * 2 >= this.outPutRu;
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            rotateEntitiesAbove(1.0f);
            return;
        }

        if (!isActive) {
            if (this.fuelFluidTank.getFluidAmount() <= 0 || this.outFluidTank.getFluidAmount() >= TANK_CAPACITY) {
                clearOut();
            } else {
                setActive(true);
            }
            return;
        }

        if (this.currentItemHasBurnedTime >= this.currentItemBurnTime) {
            if (!canActive() || this.outFluidTank.getFluidAmount() >= TANK_CAPACITY) {
                clearOut();
                return;
            }
            loadFuelBurn();
        } else {
            this.currentItemHasBurnedTime += this.outPutRu;
        }
        this.ru.setRuEnergy(this.outPutRu);
    }

    private void loadFuelBurn() {
        int mlHu = LiquidBurringInfo.getMlHu(this.fuelFluidTank.getFluid()) * 2;
        int fuelAmount = mlHu > this.outPutRu ? 1 : this.outPutRu / mlHu + this.outPutRu % mlHu;
        this.currentItemBurnTime = mlHu * fuelAmount;
        this.currentItemHasBurnedTime = 0;
        this.fuelFluidTank.drain(fuelAmount, true);
        this.outFluidTank.fill(Materials.CarbonDioxide.getFluid(100), true);
    }

    private void clearOut() {
        this.ru.setRuEnergy(0);
        setActive(false);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && facing == this.frontFacing) {
            return true;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && side == this.frontFacing) {
            return CapabilityHandler.CAPABILITY_ROTATION_ENERGY.cast(ru);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side == this.frontFacing.getOpposite()) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.outFluidTank);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                && side != this.frontFacing
                && side != this.frontFacing.getOpposite()) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.fuelFluidTank);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return null;
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

    private class FuelFluidTank extends FluidTank {

        private FuelFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return LiquidBurringInfo.ContainsFuel(fluid) && super.canFillFluidType(fluid);
        }
    }
}
