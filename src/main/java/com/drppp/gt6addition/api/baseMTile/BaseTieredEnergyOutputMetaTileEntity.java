package com.drppp.gt6addition.api.baseMTile;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.top.IEnergyOutShow;
import com.drppp.gt6addition.api.utils.EnergyConversionHelper;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BaseTieredEnergyOutputMetaTileEntity extends TieredMetaTileEntity implements IEnergyOutShow {

    protected static final String NBT_ACTIVE = "isActive";

    public final int color;
    public boolean isActive;

    private final ICubeRenderer machineRenderer;

    protected BaseTieredEnergyOutputMetaTileEntity(ResourceLocation metaTileEntityId, int tier, int color,
                                                  ICubeRenderer machineRenderer) {
        super(metaTileEntityId, tier);
        this.color = color;
        this.machineRenderer = machineRenderer;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive == active) {
            return;
        }

        this.isActive = active;
        this.markDirty();
        if (getWorld() != null && !getWorld().isRemote) {
            this.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 4L;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return Gt6AdditionTextures.BASE_NULL_TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Gt6AdditionTextures.BASE_NULL_TEXTURE.getParticleSprite(), this.color);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IVertexOperation[] coloredPipeline = ArrayUtils.add(
                pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.color)));
        this.getBaseRenderer().render(renderState, translation, coloredPipeline);
        this.machineRenderer.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isActive, isActive);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(NBT_ACTIVE, isActive);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean(NBT_ACTIVE);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return !this.hasFrontFacing() || this.getFrontFacing() != facing;
    }

    protected int consumeEuForOutput(int output, int minEuUse, int maxEuUse, double efficiency) {
        long stored = this.energyContainer.getEnergyStored();
        if (stored < minEuUse) {
            setActive(false);
            return 0;
        }

        setActive(true);
        if (stored >= maxEuUse) {
            this.energyContainer.removeEnergy(maxEuUse);
            return output * 2;
        }

        this.energyContainer.removeEnergy(stored);
        return EnergyConversionHelper.scaledOutputFromInput(stored, output, efficiency);
    }

    protected void rotateEntitiesAbove(float rotationSpeed) {
        if (!this.isActive || this.frontFacing != EnumFacing.UP || getWorld() == null || !getWorld().isRemote) {
            return;
        }

        BlockPos abovePos = this.getPos().up();
        List<Entity> entities = this.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(abovePos));
        for (Entity entity : entities) {
            float newYaw = entity.rotationYaw + rotationSpeed;
            if (newYaw >= 360.0f) {
                newYaw -= 360.0f;
            }
            entity.rotationYaw = newYaw;
            entity.prevRotationYaw = newYaw;
        }
    }
}
