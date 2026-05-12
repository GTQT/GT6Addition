package com.drppp.gt6addition.common.metatileentity.single.item;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.common.metatileentity.single.ku.KineticRenderHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class MetaTileEntityMiniPortal extends MetaTileEntity {

    private static final String NBT_ACTIVE = "Active";
    private static final double PX = 1.0D / 16.0D;
    private static final int REDSTONE_HOLD_TICKS = 20;
    private static final Cuboid6[] PORTAL_CORE_BOXES = {
            new Cuboid6(7 * PX, 2 * PX, 2 * PX, 9 * PX, 14 * PX, 14 * PX),
            new Cuboid6(2 * PX, 7 * PX, 2 * PX, 14 * PX, 9 * PX, 14 * PX),
            new Cuboid6(2 * PX, 2 * PX, 7 * PX, 14 * PX, 14 * PX, 9 * PX)
    };
    private static final Cuboid6[] FRAME_BOXES = {
            new Cuboid6(0.0D, 0.0D, 0.0D, 16 * PX, 2 * PX, 2 * PX),
            new Cuboid6(0.0D, 0.0D, 14 * PX, 16 * PX, 2 * PX, 16 * PX),
            new Cuboid6(0.0D, 14 * PX, 0.0D, 16 * PX, 16 * PX, 2 * PX),
            new Cuboid6(0.0D, 14 * PX, 14 * PX, 16 * PX, 16 * PX, 16 * PX),
            new Cuboid6(0.0D, 0.0D, 2 * PX, 2 * PX, 2 * PX, 14 * PX),
            new Cuboid6(14 * PX, 0.0D, 2 * PX, 16 * PX, 2 * PX, 14 * PX),
            new Cuboid6(0.0D, 14 * PX, 2 * PX, 2 * PX, 16 * PX, 14 * PX),
            new Cuboid6(14 * PX, 14 * PX, 2 * PX, 16 * PX, 16 * PX, 14 * PX),
            new Cuboid6(0.0D, 2 * PX, 0.0D, 2 * PX, 14 * PX, 2 * PX),
            new Cuboid6(14 * PX, 2 * PX, 0.0D, 16 * PX, 14 * PX, 2 * PX),
            new Cuboid6(0.0D, 2 * PX, 14 * PX, 2 * PX, 14 * PX, 16 * PX),
            new Cuboid6(14 * PX, 2 * PX, 14 * PX, 16 * PX, 14 * PX, 16 * PX)
    };

    private final SideItemHandler[] sideItemHandlers = new SideItemHandler[EnumFacing.VALUES.length];
    private final SideFluidHandler[] sideFluidHandlers = new SideFluidHandler[EnumFacing.VALUES.length];
    private final int[] remoteRedstoneSignals = new int[EnumFacing.VALUES.length];
    private final int[] remoteSignalAges = new int[EnumFacing.VALUES.length];
    private final boolean[] remoteSignalUpdated = new boolean[EnumFacing.VALUES.length];

    protected MetaTileEntityMiniPortal targetPortal;
    private boolean active;

    protected MetaTileEntityMiniPortal(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        for (EnumFacing side : EnumFacing.VALUES) {
            sideItemHandlers[side.ordinal()] = new SideItemHandler(side);
            sideFluidHandlers[side.ordinal()] = new SideFluidHandler(side);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!getWorld().isRemote && active) {
            addToDimensionList();
            findTargetPortal();
        }
    }

    @Override
    public void onUnload() {
        if (!getWorld().isRemote) {
            deactivatePortal(false);
        }
        super.onUnload();
    }

    @Override
    public void onRemoval() {
        if (!getWorld().isRemote) {
            deactivatePortal(false);
        }
        super.onRemoval();
    }

    @Override
    public void invalidate() {
        if (getWorld() != null && !getWorld().isRemote) {
            deactivatePortal(false);
        }
        super.invalidate();
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        if (!isSupportedDimension()) {
            deactivatePortal(false);
            updateRedstoneOutputs();
            return;
        }

        if (active && (targetPortal == null || !isUsableTarget(targetPortal) || getOffsetTimer() % 100L == 5L)) {
            findTargetPortal();
        }
        if (active && targetPortal != null) {
            relayRedstoneToTarget();
        }
        updateRedstoneOutputs();
    }

    protected boolean activatePortal() {
        if (active || !isSupportedDimension()) {
            return false;
        }
        active = true;
        addToDimensionList();
        refreshPortalLinks();
        syncActiveState();
        return true;
    }

    protected void deactivatePortal(boolean sync) {
        if (!active) {
            clearRemoteSignals();
            targetPortal = null;
            return;
        }
        active = false;
        clearRemoteSignals();
        removeFromDimensionLists();
        targetPortal = null;
        refreshPortalLinks();
        if (sync) {
            syncActiveState();
        } else {
            markDirty();
        }
    }

    private void syncActiveState() {
        markDirty();
        writeCustomData(gregtech.api.capability.GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
        notifyBlockUpdate();
        scheduleRenderUpdate();
    }

    private void refreshPortalLinks() {
        findTargetPortal();
        List<MetaTileEntityMiniPortal> local = getCurrentDimensionList();
        List<MetaTileEntityMiniPortal> remote = getOtherDimensionList();
        refreshTargets(local);
        refreshTargets(remote);
    }

    private void refreshTargets(@Nullable List<MetaTileEntityMiniPortal> portals) {
        if (portals == null) {
            return;
        }
        prunePortals(portals);
        for (MetaTileEntityMiniPortal portal : portals) {
            if (portal != this) {
                portal.findTargetPortal();
            }
        }
    }

    protected void findTargetPortal() {
        targetPortal = null;
        List<MetaTileEntityMiniPortal> remoteList = getOtherDimensionList();
        if (!active || remoteList == null) {
            return;
        }
        prunePortals(remoteList);
        long shortestDistance = (long) getDistanceMargin() * (long) getDistanceMargin();
        for (MetaTileEntityMiniPortal candidate : remoteList) {
            if (!isUsableTarget(candidate)) {
                continue;
            }
            long dx;
            long dz;
            if (isInOverworld()) {
                dx = getPos().getX() - (long) candidate.getPos().getX() * getDistanceFactor();
                dz = getPos().getZ() - (long) candidate.getPos().getZ() * getDistanceFactor();
            } else {
                dx = candidate.getPos().getX() - (long) getPos().getX() * getDistanceFactor();
                dz = candidate.getPos().getZ() - (long) getPos().getZ() * getDistanceFactor();
            }
            long distance = dx * dx + dz * dz;
            if (distance < shortestDistance) {
                shortestDistance = distance;
                targetPortal = candidate;
            } else if (distance == shortestDistance && targetPortal != null &&
                    Math.abs(candidate.getPos().getY() - getPos().getY()) <
                            Math.abs(targetPortal.getPos().getY() - getPos().getY())) {
                targetPortal = candidate;
            }
        }
    }

    private boolean isUsableTarget(@Nullable MetaTileEntityMiniPortal portal) {
        return portal != null && portal != this && portal.active && portal.isValid() &&
                portal.getWorld() != null && !portal.getWorld().isRemote && portal.isSupportedDimension();
    }

    private void relayRedstoneToTarget() {
        for (EnumFacing side : EnumFacing.VALUES) {
            int signal = getInputRedstoneSignal(side, false);
            targetPortal.acceptRemoteRedstone(side.getOpposite(), signal);
        }
    }

    private void acceptRemoteRedstone(EnumFacing side, int signal) {
        int index = side.ordinal();
        remoteRedstoneSignals[index] = Math.max(remoteRedstoneSignals[index], signal);
        remoteSignalAges[index] = 0;
        remoteSignalUpdated[index] = true;
    }

    private void updateRedstoneOutputs() {
        for (EnumFacing side : EnumFacing.VALUES) {
            int index = side.ordinal();
            if (!active) {
                remoteRedstoneSignals[index] = 0;
                remoteSignalAges[index] = 0;
            } else if (!remoteSignalUpdated[index]) {
                if (remoteSignalAges[index] >= REDSTONE_HOLD_TICKS) {
                    remoteRedstoneSignals[index] = 0;
                } else {
                    remoteSignalAges[index]++;
                }
            }
            setOutputRedstoneSignal(side, remoteRedstoneSignals[index]);
            remoteSignalUpdated[index] = false;
        }
    }

    private void clearRemoteSignals() {
        for (EnumFacing side : EnumFacing.VALUES) {
            int index = side.ordinal();
            remoteRedstoneSignals[index] = 0;
            remoteSignalAges[index] = 0;
            remoteSignalUpdated[index] = false;
            setOutputRedstoneSignal(side, 0);
        }
    }

    private void addToDimensionList() {
        List<MetaTileEntityMiniPortal> list = getCurrentDimensionList();
        if (list == null) {
            return;
        }
        prunePortals(list);
        if (!list.contains(this)) {
            list.add(this);
        }
    }

    private void removeFromDimensionLists() {
        removeFromList(getPrimaryPortalList());
        removeFromList(getSecondaryPortalList());
    }

    private void removeFromList(List<MetaTileEntityMiniPortal> portals) {
        Iterator<MetaTileEntityMiniPortal> iterator = portals.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == this) {
                iterator.remove();
            }
        }
    }

    private void prunePortals(List<MetaTileEntityMiniPortal> portals) {
        Iterator<MetaTileEntityMiniPortal> iterator = portals.iterator();
        while (iterator.hasNext()) {
            MetaTileEntityMiniPortal portal = iterator.next();
            if (portal == null || !portal.isValid() || portal.getWorld() == null || !portal.active) {
                iterator.remove();
            }
        }
    }

    @Nullable
    private List<MetaTileEntityMiniPortal> getCurrentDimensionList() {
        if (!isSupportedDimension()) {
            return null;
        }
        return isInOverworld() ? getPrimaryPortalList() : getSecondaryPortalList();
    }

    @Nullable
    private List<MetaTileEntityMiniPortal> getOtherDimensionList() {
        if (!isSupportedDimension()) {
            return null;
        }
        return isInOverworld() ? getSecondaryPortalList() : getPrimaryPortalList();
    }

    protected boolean isSupportedDimension() {
        int dimension = getWorld().provider.getDimension();
        return dimension == 0 || dimension == getRemoteDimensionId();
    }

    protected boolean isInOverworld() {
        return getWorld().provider.getDimension() == 0;
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            return true;
        }
        ItemStack heldStack = player.getHeldItem(hand);
        if (player.isSneaking() && heldStack.isEmpty()) {
            deactivatePortal(true);
            return true;
        }
        if (canActivateWithItem(heldStack) && activatePortal()) {
            onActivationItemUsed(player, hand, heldStack);
            return true;
        }
        return true;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing side) {
        if (side != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        if (side != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (side != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(sideItemHandlers[side.ordinal()]);
        }
        if (side != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(sideFluidHandlers[side.ordinal()]);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        for (Cuboid6 frameBox : FRAME_BOXES) {
            renderTexturedCube(renderState, translation, pipeline, frameBox, getFrameTextureId());
        }
        if (active) {
            for (Cuboid6 portalCoreBox : PORTAL_CORE_BOXES) {
                renderTexturedCube(renderState, translation, pipeline, portalCoreBox, getPortalTextureId());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderTexturedCube(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                    Cuboid6 cuboid, String textureId) {
        for (EnumFacing side : EnumFacing.VALUES) {
            KineticRenderHelper.renderOverlayFace(renderState, translation, pipeline, side, cuboid, textureId);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(KineticRenderHelper.getSprite(getFrameTextureId()), 0xFFFFFF);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 1;
    }

    @Override
    public BlockFaceShape getFaceShape(EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        for (Cuboid6 frameBox : FRAME_BOXES) {
            collisionList.add(new IndexedCuboid6(null, frameBox));
        }
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
        if (dataId == gregtech.api.capability.GregtechDataCodes.WORKABLE_ACTIVE) {
            active = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(NBT_ACTIVE, active);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        active = data.getBoolean(NBT_ACTIVE);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public SoundType getSoundType() {
        return SoundType.STONE;
    }

    @Nullable
    private IItemHandler getRemoteItemHandler(EnumFacing localSide) {
        if (targetPortal == null) {
            return null;
        }
        TileEntity tileEntity = targetPortal.getWorld().getTileEntity(targetPortal.getPos().offset(localSide.getOpposite()));
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, localSide)) {
            return null;
        }
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, localSide);
    }

    @Nullable
    private IFluidHandler getRemoteFluidHandler(EnumFacing localSide) {
        if (targetPortal == null) {
            return null;
        }
        TileEntity tileEntity = targetPortal.getWorld().getTileEntity(targetPortal.getPos().offset(localSide.getOpposite()));
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, localSide)) {
            return null;
        }
        return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, localSide);
    }

    protected abstract int getRemoteDimensionId();

    protected abstract int getDistanceFactor();

    protected abstract int getDistanceMargin();

    protected abstract List<MetaTileEntityMiniPortal> getPrimaryPortalList();

    protected abstract List<MetaTileEntityMiniPortal> getSecondaryPortalList();

    protected abstract boolean canActivateWithItem(ItemStack heldStack);

    protected abstract void onActivationItemUsed(EntityPlayer player, EnumHand hand, ItemStack heldStack);

    protected abstract String getPortalTextureId();

    protected abstract String getFrameTextureId();

    private final class SideItemHandler implements IItemHandler {

        private final EnumFacing side;

        private SideItemHandler(EnumFacing side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            IItemHandler handler = getRemoteItemHandler(side);
            return handler == null ? 0 : handler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            IItemHandler handler = getRemoteItemHandler(side);
            return handler == null ? ItemStack.EMPTY : handler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            IItemHandler handler = getRemoteItemHandler(side);
            return handler == null ? stack : handler.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandler handler = getRemoteItemHandler(side);
            return handler == null ? ItemStack.EMPTY : handler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            IItemHandler handler = getRemoteItemHandler(side);
            return handler == null ? 0 : handler.getSlotLimit(slot);
        }
    }

    private final class SideFluidHandler implements IFluidHandler {

        private final EnumFacing side;

        private SideFluidHandler(EnumFacing side) {
            this.side = side;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            IFluidHandler handler = getRemoteFluidHandler(side);
            return handler == null ? new IFluidTankProperties[0] : handler.getTankProperties();
        }

        @Override
        public int fill(net.minecraftforge.fluids.FluidStack resource, boolean doFill) {
            IFluidHandler handler = getRemoteFluidHandler(side);
            return handler == null ? 0 : handler.fill(resource, doFill);
        }

        @Nullable
        @Override
        public net.minecraftforge.fluids.FluidStack drain(net.minecraftforge.fluids.FluidStack resource,
                                                          boolean doDrain) {
            IFluidHandler handler = getRemoteFluidHandler(side);
            return handler == null ? null : handler.drain(resource, doDrain);
        }

        @Nullable
        @Override
        public net.minecraftforge.fluids.FluidStack drain(int maxDrain, boolean doDrain) {
            IFluidHandler handler = getRemoteFluidHandler(side);
            return handler == null ? null : handler.drain(maxDrain, doDrain);
        }
    }
}
