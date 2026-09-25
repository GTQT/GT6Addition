package com.drppp.gt6addition.common.metatileentity.single.ru;

import com.drppp.gt6addition.api.baseMTile.BaseEnergyOutputMetaTileEntity;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.RotationEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import com.drppp.gt6addition.api.utils.EnergyConversionHelper;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntitySteamTurbine extends BaseEnergyOutputMetaTileEntity {

    private static final int DATA_RENDER_SPEED = 521;
    private static final String NBT_FAST_ANIMATION = "fastAnimation";

    public final double efficiency;
    public final int outPutRu;
    public final int minSteamUse;
    public final int maxSteamUse;

    protected final int tank_size;
    protected final IRotationEnergy ru = new RotationEnergyHandler();
    protected FluidTank steamFluidTank;
    protected FluidTank waterFluidTank;
    private boolean fastAnimation;

    public MetaTileEntitySteamTurbine(ResourceLocation metaTileEntityId, int color, double efficiency, int outPutRu,
                                      int tank_size) {
        super(metaTileEntityId, color, Gt6AdditionTextures.RU_STEAM_TURBINE);
        this.efficiency = efficiency;
        this.outPutRu = outPutRu;
        this.tank_size = tank_size;
        // GT6's steam unit ratio is 2 Steam per energy unit; apply efficiency after the unit conversion.
        this.minSteamUse = EnergyConversionHelper.minimumInputForHalfNominalOutput(
                this.outPutRu, this.efficiency, 2.0D);
        this.maxSteamUse = EnergyConversionHelper.maximumInputForDoubleOutput(
                this.outPutRu, this.efficiency, 2.0D);
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
        this.steamFluidTank = new FilteredFluidHandler(tank_size).setFilter(CommonFluidFilters.STEAM);
        return new FluidTankList(false, steamFluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        this.waterFluidTank = new FilteredFluidHandler(tank_size).setFilter(new IFilter<FluidStack>() {
            @Override
            public boolean test(@NotNull FluidStack fluid) {
                return CommonFluidFilters.matchesFluid(fluid, Materials.DistilledWater);
            }

            @Override
            public int getPriority() {
                return IFilter.whitelistPriority(1);
            }
        });
        return new FluidTankList(false, waterFluidTank);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntitySteamTurbine(this.metaTileEntityId, this.color, this.efficiency, this.outPutRu,
                this.tank_size);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.ru.generator.info.1", this.efficiency * 100 + "%"));
        tooltip.add(I18n.format("gt6addition.ru.generator.info.2", this.outPutRu, this.outPutRu / 2,
                this.outPutRu * 2));
        tooltip.add(I18n.format("gt6addition.ru.generator.info.3"));
        tooltip.add(I18n.format("gt6addition.ru.generator.info.4"));
        tooltip.add(I18n.format("gt6addition.ru.generator.info.5"));
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            rotateEntitiesAbove(1.0f);
            return;
        }

        if (this.steamFluidTank.getFluidAmount() < this.minSteamUse
                || this.waterFluidTank.getFluidAmount() >= this.tank_size) {
            setFastAnimation(false);
            clearOut();
            return;
        }

        setActive(true);
        if (this.steamFluidTank.getFluidAmount() >= this.maxSteamUse) {
            setFastAnimation(true);
            this.importFluids.drain(this.maxSteamUse, true);
            this.ru.setRuEnergy(this.outPutRu * 2);
            this.waterFluidTank.fill(Materials.DistilledWater.getFluid((int) (this.maxSteamUse * 0.1)), true);
            return;
        }

        setFastAnimation(false);
        int amount = this.steamFluidTank.getFluidAmount();
        this.importFluids.drain(amount, true);
        this.ru.setRuEnergy(EnergyConversionHelper.scaledOutputFromInput(
                amount, this.outPutRu, this.efficiency, 2.0D));
        this.waterFluidTank.fill(Materials.DistilledWater.getFluid((int) (amount * 0.1)), true);
    }

    private void clearOut() {
        this.ru.setRuEnergy(0);
        setActive(false);
    }

    private void setFastAnimation(boolean fast) {
        if (this.fastAnimation == fast) {
            return;
        }
        this.fastAnimation = fast;
        markDirty();
        if (getWorld() != null && !getWorld().isRemote) {
            writeCustomData(DATA_RENDER_SPEED, buf -> buf.writeBoolean(fast));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(NBT_FAST_ANIMATION, this.fastAnimation);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.fastAnimation = data.getBoolean(NBT_FAST_ANIMATION);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.fastAnimation);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.fastAnimation = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == DATA_RENDER_SPEED) {
            this.fastAnimation = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected ICubeRenderer getMachineRenderer() {
        if (!this.isActive) {
            return Gt6AdditionTextures.RU_STEAM_TURBINE;
        }
        return this.fastAnimation
                ? Gt6AdditionTextures.RU_STEAM_TURBINE_FAST
                : Gt6AdditionTextures.RU_STEAM_TURBINE_SLOW;
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
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.steamFluidTank);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                && side != this.frontFacing
                && side != this.frontFacing.getOpposite()) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.waterFluidTank);
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
}
