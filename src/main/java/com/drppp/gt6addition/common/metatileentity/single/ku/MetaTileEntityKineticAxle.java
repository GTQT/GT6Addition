package com.drppp.gt6addition.common.metatileentity.single.ku;

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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityKineticAxle extends MetaTileEntity implements IEnergyOutShow {

    private static final String NBT_ACTIVE = "Active";
    private static final String NBT_LAST_TRANSFERRED = "LastTransferred";
    private static final double ROD_MIN = 0.3125D;
    private static final double ROD_MAX = 0.6875D;

    private final int color;
    private final int maxThroughput;
    private final IRotationEnergy rotationEnergy = new RotationEnergyHandler();
    private boolean active;
    private int lastTransferred;

    public MetaTileEntityKineticAxle(ResourceLocation metaTileEntityId, int color, int maxThroughput) {
        super(metaTileEntityId);
        this.color = color;
        this.maxThroughput = maxThroughput;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityKineticAxle(metaTileEntityId, color, maxThroughput);
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
        int input = Math.max(readRotationFrom(getFrontFacing()), readRotationFrom(getFrontFacing().getOpposite()));
        if (input > maxThroughput) {
            rotationEnergy.setRuEnergy(0);
            lastTransferred = 0;
            setActive(false);
            getWorld().destroyBlock(getPos(), true);
            return;
        }
        int output = Math.min(input, maxThroughput);
        rotationEnergy.setRuEnergy(output);
        lastTransferred = output;
        setActive(output > 0);
    }

    private int readRotationFrom(EnumFacing side) {
        TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));
        if (tileEntity == null ||
                !tileEntity.hasCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY, side.getOpposite())) {
            return 0;
        }
        IRotationEnergy energy = tileEntity.getCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY,
                side.getOpposite());
        return energy == null ? 0 : Math.max(0, energy.getEnergyOutput());
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    private boolean isAxial(@Nullable EnumFacing side) {
        return side == null || side.getAxis() == getFrontFacing().getAxis();
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY) {
            return isAxial(side);
        }
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return false;
        }
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && isAxial(side)) {
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
        data.setInteger(NBT_LAST_TRANSFERRED, lastTransferred);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        active = data.getBoolean(NBT_ACTIVE);
        lastTransferred = data.getInteger(NBT_LAST_TRANSFERRED);
        rotationEnergy.setRuEnergy(lastTransferred);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(active);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        active = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            active = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation,
                                     IVertexOperation[] pipeline) {
        Cuboid6 axle = KineticRenderHelper.axisBox(getFrontFacing().getAxis(), 0.0D, 1.0D,
                ROD_MIN, ROD_MAX, ROD_MIN, ROD_MAX);
        KineticRenderHelper.renderAllFaces(renderState, translation, pipeline, axle,
                active ? "machines/kinetic/iconsets/axle_clockwise" : "machines/kinetic/iconsets/axle", color);
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
        return Pair.of(KineticRenderHelper.getSprite("machines/kinetic/iconsets/axle"), color);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.kinetic_axle.tooltip.1", maxThroughput));
        tooltip.add(I18n.format("gt6addition.machine.kinetic_axle.tooltip.2"));
        tooltip.add(I18n.format("gt6addition.machine.kinetic_axle.tooltip.3"));
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
