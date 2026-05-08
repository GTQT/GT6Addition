package com.drppp.gt6addition.common.metatileentity.single.ku;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.RotationEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import com.drppp.gt6addition.api.top.IEnergyOutShow;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityKineticGearbox extends MetaTileEntity implements IEnergyOutShow {

    private static final String NBT_ACTIVE = "Active";
    private static final String NBT_JAMMED = "Jammed";
    private static final String NBT_OUTPUT_SIDE = "OutputSide";
    private static final String NBT_LAST_INPUT = "LastInput";
    private static final String NBT_LAST_OUTPUT = "LastOutput";
    private static final int DATA_STATE = 521;
    private static final Cuboid6 BODY = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    private final int color;
    private final int maxThroughput;
    private final boolean adjustable;
    private final IRotationEnergy rotationEnergy = new RotationEnergyHandler();
    private boolean active;
    private boolean jammed;
    private EnumFacing outputSide;
    private int lastInput;
    private int lastOutput;

    public MetaTileEntityKineticGearbox(ResourceLocation metaTileEntityId, int color, int maxThroughput,
                                        boolean adjustable) {
        super(metaTileEntityId);
        this.color = color;
        this.maxThroughput = maxThroughput;
        this.adjustable = adjustable;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityKineticGearbox(metaTileEntityId, color, maxThroughput, adjustable);
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
            return;
        }
        if (jammed) {
            clearOutput();
            return;
        }
        lastInput = readInputRu();
        if (lastInput > maxThroughput) {
            jammed = true;
            clearOutput();
            writeCustomData(DATA_STATE, this::writeState);
            return;
        }
        lastOutput = lastInput;
        rotationEnergy.setRuEnergy(lastOutput);
        setActive(lastOutput > 0);
    }

    private int readInputRu() {
        int totalInput = 0;
        EnumFacing output = getOutputSide();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side == output) {
                continue;
            }
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));
            if (tileEntity == null ||
                    !tileEntity.hasCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY, side.getOpposite())) {
                continue;
            }
            IRotationEnergy energy = tileEntity.getCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY,
                    side.getOpposite());
            if (energy != null) {
                totalInput += Math.max(0, energy.getEnergyOutput());
                if (totalInput > maxThroughput) {
                    return totalInput;
                }
            }
        }
        return totalInput;
    }

    private void clearOutput() {
        lastOutput = 0;
        rotationEnergy.setRuEnergy(0);
        setActive(false);
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            return true;
        }
        if (jammed && player.isSneaking()) {
            jammed = false;
            player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.kinetic_gearbox.status.cleared"), true);
            writeCustomData(DATA_STATE, this::writeState);
            markDirty();
            return true;
        }
        if (adjustable) {
            outputSide = cycleOutputSide(player.isSneaking());
            player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.kinetic_gearbox.status.output",
                    getOutputSide().getName()), true);
            writeCustomData(DATA_STATE, this::writeState);
            markDirty();
        }
        return true;
    }

    private EnumFacing cycleOutputSide(boolean reverse) {
        EnumFacing[] values = EnumFacing.VALUES;
        int ordinal = getOutputSide().ordinal() + (reverse ? -1 : 1);
        if (ordinal < 0) {
            ordinal = values.length - 1;
        } else if (ordinal >= values.length) {
            ordinal = 0;
        }
        return values[ordinal];
    }

    private EnumFacing getOutputSide() {
        return adjustable ? outputSide == null ? getFrontFacing() : outputSide : getFrontFacing();
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY) {
            return side == getOutputSide();
        }
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return false;
        }
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && side == getOutputSide()) {
            return CapabilityHandler.CAPABILITY_ROTATION_ENERGY.cast(rotationEnergy);
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
        data.setBoolean(NBT_JAMMED, jammed);
        data.setInteger(NBT_OUTPUT_SIDE, getOutputSide().ordinal());
        data.setInteger(NBT_LAST_INPUT, lastInput);
        data.setInteger(NBT_LAST_OUTPUT, lastOutput);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        active = data.getBoolean(NBT_ACTIVE);
        jammed = data.getBoolean(NBT_JAMMED);
        int sideOrdinal = data.hasKey(NBT_OUTPUT_SIDE) ? data.getInteger(NBT_OUTPUT_SIDE) : getFrontFacing().ordinal();
        outputSide = EnumFacing.VALUES[Math.max(0, Math.min(EnumFacing.VALUES.length - 1, sideOrdinal))];
        lastInput = data.getInteger(NBT_LAST_INPUT);
        lastOutput = data.getInteger(NBT_LAST_OUTPUT);
        rotationEnergy.setRuEnergy(lastOutput);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        writeState(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        readState(buf);
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            active = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == DATA_STATE) {
            readState(buf);
            scheduleRenderUpdate();
        }
    }

    private void writeState(PacketBuffer buf) {
        buf.writeBoolean(active);
        buf.writeBoolean(jammed);
        buf.writeVarInt(getOutputSide().ordinal());
    }

    private void readState(PacketBuffer buf) {
        active = buf.readBoolean();
        jammed = buf.readBoolean();
        int sideOrdinal = buf.readVarInt();
        outputSide = EnumFacing.VALUES[Math.max(0, Math.min(EnumFacing.VALUES.length - 1, sideOrdinal))];
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation,
                                     IVertexOperation[] pipeline) {
        KineticRenderHelper.renderAllFaces(renderState, translation, pipeline, BODY,
                "machines/kinetic/iconsets/gearbox", jammed ? 0xAA2222 : color);
        String gearSprite = active ? "machines/kinetic/iconsets/gear_clockwise" : "machines/kinetic/iconsets/gear";
        KineticRenderHelper.renderFace(renderState, translation, pipeline, getOutputSide(), BODY,
                adjustable ? "machines/kinetic/iconsets/gearbox_axle" : gearSprite, color);
        KineticRenderHelper.renderOverlayFace(renderState, translation, pipeline, getOutputSide(), BODY, gearSprite);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(KineticRenderHelper.getSprite("machines/kinetic/iconsets/gearbox"), color);
    }

    public String getRatioName() {
        return getOutputSide().getName();
    }

    public boolean isJammed() {
        return jammed;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.1", maxThroughput));
        tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.2"));
        if (adjustable) {
            tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.adjustable"));
        } else {
            tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.fixed"));
        }
        tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.3"));
    }

    @Override
    public String getEnergyName() {
        return "RU";
    }

    @Override
    public int getEnergyOut() {
        return rotationEnergy.getEnergyOutput();
    }
}
