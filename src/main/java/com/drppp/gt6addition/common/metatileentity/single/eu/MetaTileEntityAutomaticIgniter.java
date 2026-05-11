package com.drppp.gt6addition.common.metatileentity.single.eu;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.machine.IAutomaticIgnitable;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityAutomaticIgniter extends TieredMetaTileEntity {

    private static final String NBT_ACTIVE = "isActive";
    private static final int IGNITION_INTERVAL = 10;
    private static final SimpleSidedCubeRenderer IGNITER_RENDERER =
            new SimpleSidedCubeRenderer("gt6addition:machines/autotools/igniter/colored");

    private final int color;
    private final long euPerIgnite;
    private boolean isActive;

    public MetaTileEntityAutomaticIgniter(ResourceLocation metaTileEntityId, int tier, int color) {
        super(metaTileEntityId, tier);
        this.color = color;
        this.euPerIgnite = gregtech.api.GTValues.V[tier];
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityAutomaticIgniter(this.metaTileEntityId, this.getTier(), this.color);
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    private void setActive(boolean active) {
        if (this.isActive == active) {
            return;
        }
        this.isActive = active;
        markDirty();
        if (getWorld() != null && !getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public void update() {
        super.update();
        if (getWorld() == null || getWorld().isRemote || getOffsetTimer() % IGNITION_INTERVAL != 0) {
            return;
        }
        if (energyContainer.getEnergyStored() < euPerIgnite) {
            setActive(false);
            return;
        }

        BlockPos targetPos = getPos().offset(getFrontFacing());
        Object target = GTUtility.getMetaTileEntity(getWorld(), targetPos);
        if (!(target instanceof IAutomaticIgnitable) ||
                !((IAutomaticIgnitable) target).igniteFromAutomaticIgniter()) {
            setActive(false);
            return;
        }

        energyContainer.removeEnergy(euPerIgnite);
        setActive(true);
        getWorld().playSound(null, targetPos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return !hasFrontFacing() || getFrontFacing() != facing;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IGNITER_RENDERER.render(renderState, translation, pipeline);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(IGNITER_RENDERER.getParticleSprite(), 0xFFFFFF);
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
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.automatic_igniter.tooltip.1"));
        tooltip.add(I18n.format("gt6addition.machine.automatic_igniter.tooltip.2"));
        tooltip.add(I18n.format("gt6addition.machine.automatic_igniter.tooltip.3", euPerIgnite));
    }
}
