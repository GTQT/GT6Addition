package com.drppp.gt6addition.common.metatileentity.single.ku;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.KineticEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IKineticEnergy;
import com.drppp.gt6addition.api.top.IEnergyOutShow;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityKineticSteamEngine extends MetaTileEntity implements IEnergyOutShow {

    private static final String NBT_ACTIVE = "Active";
    private static final String NBT_STOPPED = "Stopped";
    private static final String NBT_ENERGY = "Energy";
    private static final String NBT_STATE = "State";
    private static final String NBT_PISTON = "Piston";
    private static final int DATA_RENDER_STATE = 520;
    private static final int STEAM_PER_WATER = 200;
    private static final int STEAM_PER_KU = 100;
    private static final int[] ENGINE_STATE_COLORS = {
            0x0000FF, 0x0011EE, 0x0022DD, 0x0033CC, 0x0044BB, 0x0055AA, 0x006699, 0x007788,
            0x008877, 0x009966, 0x00AA55, 0x00BB44, 0x00CC33, 0x00DD22, 0x00EE11, 0x00FF00,
            0x00FF00, 0x11EE00, 0x22DD00, 0x33CC00, 0x44BB00, 0x55AA00, 0x669900, 0x778800,
            0x887700, 0x996600, 0xAA5500, 0xBB4400, 0xCC3300, 0xDD2200, 0xEE1100, 0xFF0000
    };
    private static final double P1 = 1.0D / 16.0D;
    private static final double P2 = 2.0D / 16.0D;
    private static final double P3 = 3.0D / 16.0D;
    private static final double P4 = 4.0D / 16.0D;
    private static final double P6 = 6.0D / 16.0D;
    private static final double P10 = 10.0D / 16.0D;
    private static final double P12 = 12.0D / 16.0D;
    private static final double P13 = 13.0D / 16.0D;
    private static final double P14 = 14.0D / 16.0D;
    private static final double P15 = 15.0D / 16.0D;
    private static final Cuboid6 CAGE = new Cuboid6(P1, P1, P1, P15, P15, P15);

    private final int color;
    private final int outputKu;
    private final int efficiency;
    private final long capacity;
    private final int tankCapacity;
    private final IKineticEnergy kineticEnergy = new KineticEnergyHandler();

    private FluidTank steamTank;
    private FluidTank waterTank;
    private boolean active;
    private boolean stopped;
    private long storedEnergy;
    private int state;
    private int piston;

    public MetaTileEntityKineticSteamEngine(ResourceLocation metaTileEntityId, int color,
                                            int outputKu, int efficiency) {
        super(metaTileEntityId);
        this.color = color;
        this.outputKu = outputKu;
        this.efficiency = Math.max(1, Math.min(100, efficiency));
        this.capacity = Math.max(1L, outputKu * 10000L);
        this.tankCapacity = STEAM_PER_WATER * outputKu * 2;
        initializeInventory();
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.importFluids = createImportFluidHandler();
        this.exportFluids = createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(importFluids, exportFluids);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        this.steamTank = new FilteredFluidHandler(tankCapacity).setFilter(CommonFluidFilters.STEAM);
        return new FluidTankList(false, steamTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        this.waterTank = new FilteredFluidHandler(tankCapacity).setFilter(new IFilter<FluidStack>() {
            @Override
            public boolean test(@NotNull FluidStack fluid) {
                return CommonFluidFilters.matchesFluid(fluid, Materials.DistilledWater);
            }

            @Override
            public int getPriority() {
                return IFilter.whitelistPriority(1);
            }
        });
        return new FluidTankList(false, waterTank);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityKineticSteamEngine(metaTileEntityId, color, outputKu, efficiency);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            markDirty();
            if (getWorld() != null && !getWorld().isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            if (active && getOffsetTimer() % Math.max(1, 8 - Math.min(7, state / 4)) == 0) {
                piston = (piston + 1) & 3;
                scheduleRenderUpdate();
            }
            return;
        }
        convertSteam();
        updateStateAndOutput();
        if (getOffsetTimer() % 10 == 0) {
            writeCustomData(DATA_RENDER_STATE, this::writeRenderState);
        }
    }

    private void convertSteam() {
        if (stopped || steamTank == null || steamTank.getFluidAmount() < STEAM_PER_WATER) {
            return;
        }
        long conversions = steamTank.getFluidAmount() / STEAM_PER_WATER;
        long freeConversions = Math.max(0L, (capacity - storedEnergy) / 2L);
        conversions = Math.min(conversions, freeConversions);
        if (conversions <= 0L) {
            return;
        }
        int steamUsed = (int) Math.min(Integer.MAX_VALUE, conversions * STEAM_PER_WATER);
        steamTank.drain(steamUsed, true);
        storedEnergy += conversions * 2L * efficiency / 100L;
        if (waterTank != null) {
            waterTank.fill(Materials.DistilledWater.getFluid((int) Math.min(Integer.MAX_VALUE, conversions)), true);
        }
    }

    private void updateStateAndOutput() {
        state = (int) Math.min(31L, storedEnergy * 32L / Math.max(1L, capacity));
        int targetOutput = outputKu * (state + 1) / 16;
        boolean canRun = !stopped && storedEnergy > targetOutput && targetOutput * 2 > outputKu;
        if (canRun) {
            int output = Math.min(outputKu * 2, Math.max(1, targetOutput));
            kineticEnergy.setKineticEnergy(output);
            storedEnergy = Math.max(0L, storedEnergy - output);
            setActive(true);
        } else {
            kineticEnergy.setKineticEnergy(0);
            setActive(false);
        }
        if (storedEnergy >= capacity) {
            storedEnergy = capacity - 1L;
            if (state > 30) {
                stopped = true;
                if (steamTank != null) {
                    steamTank.drain(Integer.MAX_VALUE, true);
                }
            } else {
                state = 31;
            }
        }
        if (stopped && storedEnergy > 0L) {
            storedEnergy = Math.max(0L, storedEnergy - Math.max(1L, capacity / 64L));
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote && player.isSneaking()) {
            stopped = !stopped;
            if (!stopped) {
                storedEnergy = Math.min(storedEnergy, capacity / 2L);
            }
            player.sendStatusMessage(new TextComponentTranslation(stopped ?
                    "gt6addition.machine.kinetic_steam_engine.status.stopped" :
                    "gt6addition.machine.kinetic_steam_engine.status.running"), true);
            markDirty();
        }
        return true;
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_KINETIC_ENERGY) {
            return side == getFrontFacing();
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return side != getFrontFacing();
        }
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return false;
        }
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_KINETIC_ENERGY && side == getFrontFacing()) {
            return CapabilityHandler.CAPABILITY_KINETIC_ENERGY.cast(kineticEnergy);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (side == getFrontFacing().getOpposite()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(steamTank);
            }
            if (side != getFrontFacing()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(waterTank);
            }
            return null;
        }
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(NBT_ACTIVE, active);
        data.setBoolean(NBT_STOPPED, stopped);
        data.setLong(NBT_ENERGY, storedEnergy);
        data.setInteger(NBT_STATE, state);
        data.setInteger(NBT_PISTON, piston);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        active = data.getBoolean(NBT_ACTIVE);
        stopped = data.getBoolean(NBT_STOPPED);
        storedEnergy = data.getLong(NBT_ENERGY);
        state = data.getInteger(NBT_STATE);
        piston = data.getInteger(NBT_PISTON);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        writeRenderState(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        readRenderState(buf);
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            active = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == DATA_RENDER_STATE) {
            readRenderState(buf);
            scheduleRenderUpdate();
        }
    }

    private void writeRenderState(PacketBuffer buf) {
        buf.writeBoolean(active);
        buf.writeBoolean(stopped);
        buf.writeVarInt(state);
        buf.writeVarInt(piston);
    }

    private void readRenderState(PacketBuffer buf) {
        active = buf.readBoolean();
        stopped = buf.readBoolean();
        state = buf.readVarInt();
        piston = buf.readVarInt();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation,
                                     IVertexOperation[] pipeline) {
        renderCage(renderState, translation, pipeline);
        renderEndCaps(renderState, translation, pipeline);
        renderPipes(renderState, translation, pipeline);
        renderPiston(renderState, translation, pipeline);
        renderEngineHull(renderState, translation, pipeline);
    }

    @SideOnly(Side.CLIENT)
    private void renderCage(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        EnumFacing.Axis axis = getFrontFacing().getAxis();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side.getAxis() != axis) {
                renderColoredOverlayFace(renderState, translation, pipeline, side, CAGE,
                        "machines/engines/kinetic_steam/colored/cage",
                        "machines/engines/kinetic_steam/overlay/cage", color);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderEndCaps(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        EnumFacing.Axis axis = getFrontFacing().getAxis();
        Cuboid6 backCap = KineticRenderHelper.axisBox(axis, 0.0D, P2, 0.0D, 1.0D, 0.0D, 1.0D);
        Cuboid6 frontCap = KineticRenderHelper.axisBox(axis, P14, 1.0D, 0.0D, 1.0D, 0.0D, 1.0D);
        renderOrientedBox(renderState, translation, pipeline, backCap);
        renderOrientedBox(renderState, translation, pipeline, frontCap);
    }

    @SideOnly(Side.CLIENT)
    private void renderOrientedBox(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                   Cuboid6 bounds) {
        for (EnumFacing side : EnumFacing.VALUES) {
            String face = side == getFrontFacing() ? "front" :
                    side == getFrontFacing().getOpposite() ? "back" : "side";
            renderColoredOverlayFace(renderState, translation, pipeline, side, bounds,
                    "machines/engines/kinetic_steam/colored/" + face,
                    "machines/engines/kinetic_steam/overlay/" + face, color);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderPipes(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        EnumFacing.Axis mainAxis = getFrontFacing().getAxis();
        for (EnumFacing.Axis axis : EnumFacing.Axis.values()) {
            if (axis == mainAxis) {
                continue;
            }
            Cuboid6 pipe = KineticRenderHelper.axisBox(axis, 0.0D, 1.0D, P6, P10, P6, P10);
            renderPipe(renderState, translation, pipeline, axis, pipe);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderPipe(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            EnumFacing.Axis pipeAxis, Cuboid6 bounds) {
        for (EnumFacing side : EnumFacing.VALUES) {
            String face = side.getAxis() == pipeAxis ? "pipe_side" : "pipe";
            renderColoredOverlayFace(renderState, translation, pipeline, side, bounds,
                    "machines/engines/kinetic_steam/colored/" + face,
                    "machines/engines/kinetic_steam/overlay/" + face, color);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderPiston(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        EnumFacing.Axis axis = getFrontFacing().getAxis();
        Cuboid6 engine = KineticRenderHelper.axisBox(axis, 0.0D, 1.0D, P4, P12, P4, P12);
        int stateColor = ENGINE_STATE_COLORS[Math.max(0, Math.min(31, state))];
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side != getFrontFacing().getOpposite()) {
                renderColoredOverlayFace(renderState, translation, pipeline, side, engine,
                        "machines/engines/kinetic_steam/colored/engine",
                        "machines/engines/kinetic_steam/overlay/engine", stateColor);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderEngineHull(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        EnumFacing.Axis axis = getFrontFacing().getAxis();
        Cuboid6 hull = KineticRenderHelper.axisBox(axis, 0.0D, 1.0D, P3, P13, P3, P13);
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side.getAxis() != axis) {
                renderColoredOverlayFace(renderState, translation, pipeline, side, hull,
                        "machines/engines/kinetic_steam/colored/engine_hull",
                        "machines/engines/kinetic_steam/overlay/engine_hull", color);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderColoredOverlayFace(CCRenderState renderState, Matrix4 translation,
                                          IVertexOperation[] pipeline, EnumFacing side, Cuboid6 bounds,
                                          String coloredPath, String overlayPath, int faceColor) {
        KineticRenderHelper.renderFace(renderState, translation, pipeline, side, bounds, coloredPath, faceColor);
        KineticRenderHelper.renderOverlayFace(renderState, translation, pipeline, side, bounds, overlayPath);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(KineticRenderHelper.getSprite("machines/engines/kinetic_steam/colored/side"), color);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.kinetic_steam_engine.tooltip.1", efficiency + "%"));
        tooltip.add(I18n.format("gt6addition.machine.kinetic_steam_engine.tooltip.2", outputKu / 2, outputKu * 2));
        tooltip.add(I18n.format("gt6addition.machine.kinetic_steam_engine.tooltip.3", tankCapacity));
        tooltip.add(I18n.format("gt6addition.machine.kinetic_steam_engine.tooltip.4"));
    }

    public int getSteamAmount() {
        return steamTank == null ? 0 : steamTank.getFluidAmount();
    }

    public int getWaterAmount() {
        return waterTank == null ? 0 : waterTank.getFluidAmount();
    }

    public int getTankCapacity() {
        return tankCapacity;
    }

    public long getStoredEnergy() {
        return storedEnergy;
    }

    public long getEnergyCapacity() {
        return capacity;
    }

    public boolean isStopped() {
        return stopped;
    }

    @Override
    public String getEnergyName() {
        return "KU";
    }

    @Override
    public int getEnergyOut() {
        return kineticEnergy.getKinetic();
    }
}
