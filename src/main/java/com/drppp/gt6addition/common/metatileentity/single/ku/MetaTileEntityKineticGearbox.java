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
    private static final String NBT_RATIO = "Ratio";
    private static final String NBT_LAST_INPUT = "LastInput";
    private static final String NBT_LAST_OUTPUT = "LastOutput";
    private static final int DATA_STATE = 521;
    private static final int[] RATIO_NUMERATORS = {1, 1, 1, 2, 4};
    private static final int[] RATIO_DENOMINATORS = {4, 2, 1, 1, 1};
    private static final String[] RATIO_NAMES = {"1:4", "1:2", "1:1", "2:1", "4:1"};
    private static final int DEFAULT_RATIO = 2;
    private static final Cuboid6 BODY = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    private final int color;
    private final int maxThroughput;
    private final boolean adjustable;
    private final int efficiency;
    private final IKineticEnergy kineticEnergy = new KineticEnergyHandler();
    private boolean active;
    private boolean jammed;
    private int ratioIndex = DEFAULT_RATIO;
    private int lastInput;
    private int lastOutput;

    public MetaTileEntityKineticGearbox(ResourceLocation metaTileEntityId, int color, int maxThroughput,
                                        boolean adjustable) {
        super(metaTileEntityId);
        this.color = color;
        this.maxThroughput = maxThroughput;
        this.adjustable = adjustable;
        this.efficiency = adjustable ? 90 : 95;
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
        lastInput = readBestInput();
        if (lastInput > maxThroughput) {
            jammed = true;
            clearOutput();
            writeCustomData(DATA_STATE, this::writeState);
            return;
        }
        long transformed = (long) lastInput * RATIO_NUMERATORS[ratioIndex] / RATIO_DENOMINATORS[ratioIndex];
        transformed = transformed * efficiency / 100L;
        if (transformed > maxThroughput) {
            jammed = true;
            clearOutput();
            writeCustomData(DATA_STATE, this::writeState);
            return;
        }
        lastOutput = (int) Math.max(0L, transformed);
        kineticEnergy.setKineticEnergy(lastOutput);
        setActive(lastOutput > 0);
    }

    private int readBestInput() {
        int bestInput = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side == getFrontFacing()) {
                continue;
            }
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));
            if (tileEntity == null ||
                    !tileEntity.hasCapability(CapabilityHandler.CAPABILITY_KINETIC_ENERGY, side.getOpposite())) {
                continue;
            }
            IKineticEnergy energy = tileEntity.getCapability(CapabilityHandler.CAPABILITY_KINETIC_ENERGY,
                    side.getOpposite());
            if (energy != null) {
                bestInput = Math.max(bestInput, Math.max(0, energy.getKinetic()));
            }
        }
        return bestInput;
    }

    private void clearOutput() {
        lastOutput = 0;
        kineticEnergy.setKineticEnergy(0);
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
            ratioIndex += player.isSneaking() ? -1 : 1;
            if (ratioIndex < 0) {
                ratioIndex = RATIO_NAMES.length - 1;
            } else if (ratioIndex >= RATIO_NAMES.length) {
                ratioIndex = 0;
            }
            player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.kinetic_gearbox.status.ratio",
                    getRatioName()), true);
            writeCustomData(DATA_STATE, this::writeState);
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
        data.setInteger(NBT_RATIO, ratioIndex);
        data.setInteger(NBT_LAST_INPUT, lastInput);
        data.setInteger(NBT_LAST_OUTPUT, lastOutput);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        active = data.getBoolean(NBT_ACTIVE);
        jammed = data.getBoolean(NBT_JAMMED);
        ratioIndex = Math.max(0, Math.min(RATIO_NAMES.length - 1, data.getInteger(NBT_RATIO)));
        lastInput = data.getInteger(NBT_LAST_INPUT);
        lastOutput = data.getInteger(NBT_LAST_OUTPUT);
        kineticEnergy.setKineticEnergy(lastOutput);
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
        buf.writeVarInt(ratioIndex);
    }

    private void readState(PacketBuffer buf) {
        active = buf.readBoolean();
        jammed = buf.readBoolean();
        ratioIndex = buf.readVarInt();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation,
                                     IVertexOperation[] pipeline) {
        KineticRenderHelper.renderAllFaces(renderState, translation, pipeline, BODY,
                "iconsets/GEARBOX", jammed ? 0xAA2222 : color);
        String gearSprite = active ? "iconsets/GEAR_CLOCKWISE" : "iconsets/GEAR";
        KineticRenderHelper.renderFace(renderState, translation, pipeline, getFrontFacing(), BODY,
                adjustable ? "iconsets/GEARBOX_AXLE" : gearSprite, color);
        KineticRenderHelper.renderOverlayFace(renderState, translation, pipeline, getFrontFacing(), BODY, gearSprite);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(KineticRenderHelper.getSprite("iconsets/GEARBOX"), color);
    }

    public String getRatioName() {
        return RATIO_NAMES[ratioIndex];
    }

    public boolean isJammed() {
        return jammed;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.1", maxThroughput));
        tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.2", efficiency + "%"));
        if (adjustable) {
            tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.adjustable"));
        } else {
            tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.fixed"));
        }
        tooltip.add(I18n.format("gt6addition.machine.kinetic_gearbox.tooltip.3"));
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
