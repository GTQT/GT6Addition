package com.drppp.gt6addition.common.metatileentity.single.item;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.common.metatileentity.single.ku.KineticRenderHelper;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityGt6Hopper extends MetaTileEntity {

    private static final String NBT_INVENTORY = "Inventory";
    private static final String NBT_STACK_SIZE = "StackSize";
    private static final String NBT_EXACT = "Exact";
    private static final String NBT_ACTIVE = "Active";
    private static final int DATA_ACTIVE = 610;
    private static final int DEFAULT_STACK_SIZE = 64;
    private static final double P4 = 4.0D / 16.0D;
    private static final double P6 = 6.0D / 16.0D;
    private static final double P8 = 8.0D / 16.0D;
    private static final double P10 = 10.0D / 16.0D;
    private static final double P12 = 12.0D / 16.0D;
    private static final Cuboid6 TOP_BASIN = new Cuboid6(0.0D, P10, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final Cuboid6 FUNNEL = new Cuboid6(P4, P4, P4, P12, P10, P12);

    private final int color;
    private final int slotCount;
    private final boolean queueMode;
    private final HopperInventory inventory;
    private final IItemHandler inputHandler = new SidedItemHandler(true, false);
    private final IItemHandler outputHandler = new SidedItemHandler(false, true);
    private final IItemHandler allHandler = new SidedItemHandler(true, true);
    private int stackSizeLimit = DEFAULT_STACK_SIZE;
    private boolean exactMode;
    private boolean active;

    public MetaTileEntityGt6Hopper(ResourceLocation metaTileEntityId, int color, int slotCount, boolean queueMode) {
        super(metaTileEntityId);
        this.frontFacing = EnumFacing.DOWN;
        this.color = color;
        this.slotCount = Math.max(queueMode ? 2 : 1, slotCount);
        this.queueMode = queueMode;
        this.inventory = new HopperInventory(this.slotCount);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityGt6Hopper(metaTileEntityId, color, slotCount, queueMode);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote || getOffsetTimer() % 4 != 0) {
            return;
        }
        if (isBlockRedstonePowered()) {
            setActive(false);
            return;
        }
        if (queueMode) {
            compactQueue();
        } else {
            mergeInventory();
        }
        int moved = 0;
        moved += pushToOutput();
        moved += pullFromTopInventory();
        if (moved == 0) {
            moved += suckDroppedItemsFromTop();
        }
        if (queueMode) {
            compactQueue();
        } else {
            mergeInventory();
        }
        setActive(moved > 0);
        if (moved > 0) {
            markDirty();
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            return true;
        }
        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.isEmpty() && player.isSneaking() && !queueMode) {
            exactMode = !exactMode;
            player.sendStatusMessage(new TextComponentTranslation(exactMode ?
                    "gt6addition.machine.gt6_hopper.status.exact_on" :
                    "gt6addition.machine.gt6_hopper.status.exact_off"), true);
            markDirty();
            return true;
        }
        if (!heldStack.isEmpty()) {
            ItemStack remaining = insertIntoInternal(heldStack.copy(), false);
            int inserted = heldStack.getCount() - remaining.getCount();
            if (inserted > 0) {
                heldStack.setCount(remaining.getCount());
                player.setHeldItem(hand, heldStack.isEmpty() ? ItemStack.EMPTY : heldStack);
                player.sendStatusMessage(new TextComponentTranslation(
                        "gt6addition.machine.gt6_hopper.status.inserted", inserted), true);
                return true;
            }
            player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.gt6_hopper.status.full"), true);
            return true;
        }
        ItemStack extracted = extractFromOutput(player.isSneaking() ? DEFAULT_STACK_SIZE : stackSizeLimit, false);
        if (!extracted.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, extracted);
        } else {
            player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.gt6_hopper.status.empty"), true);
        }
        markDirty();
        return true;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            stackSizeLimit += player.isSneaking() ? -1 : 1;
            if (stackSizeLimit < 1) {
                stackSizeLimit = DEFAULT_STACK_SIZE;
            } else if (stackSizeLimit > DEFAULT_STACK_SIZE) {
                stackSizeLimit = 1;
            }
            player.sendStatusMessage(new TextComponentTranslation(
                    "gt6addition.machine.gt6_hopper.status.stack_size", stackSizeLimit), true);
            markDirty();
        }
        return true;
    }

    @Override
    public boolean onSoftMalletClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            stackSizeLimit = DEFAULT_STACK_SIZE;
            exactMode = false;
            player.sendStatusMessage(new TextComponentTranslation(
                    "gt6addition.machine.gt6_hopper.status.reset", stackSizeLimit), true);
            markDirty();
        }
        return true;
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
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
                writeCustomData(DATA_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    private int pushToOutput() {
        if (getFrontFacing() == EnumFacing.UP) {
            return 0;
        }
        IItemHandler target = getAdjacentItemHandler(getFrontFacing(), getFrontFacing().getOpposite());
        if (target == null) {
            return 0;
        }
        int limit = stackSizeLimit;
        int moved = 0;
        while (moved < limit) {
            int request = limit - moved;
            ItemStack simulatedExtract = extractFromOutput(request, true);
            if (simulatedExtract.isEmpty()) {
                break;
            }
            if (exactMode && !queueMode) {
                if (simulatedExtract.getCount() < stackSizeLimit) {
                    break;
                }
                simulatedExtract.setCount(stackSizeLimit);
            }
            ItemStack remainder = insertIntoHandler(target, simulatedExtract.copy(), true);
            int accepted = simulatedExtract.getCount() - remainder.getCount();
            if (accepted <= 0 || (exactMode && accepted < stackSizeLimit)) {
                break;
            }
            ItemStack extracted = extractFromOutput(accepted, false);
            ItemStack returned = insertIntoHandler(target, extracted, false);
            int actuallyMoved = extracted.getCount() - returned.getCount();
            if (!returned.isEmpty()) {
                insertIntoInternal(returned, false);
            }
            if (actuallyMoved <= 0) {
                break;
            }
            moved += actuallyMoved;
            if (queueMode || exactMode) {
                break;
            }
        }
        return moved;
    }

    private int pullFromTopInventory() {
        IItemHandler source = getAdjacentItemHandler(EnumFacing.UP, EnumFacing.DOWN);
        if (source == null) {
            return 0;
        }
        int limit = stackSizeLimit;
        int moved = 0;
        for (int slot = 0; slot < source.getSlots() && moved < limit; slot++) {
            ItemStack simulatedExtract = source.extractItem(slot, limit - moved, true);
            if (simulatedExtract.isEmpty()) {
                continue;
            }
            ItemStack remainder = insertIntoInternal(simulatedExtract.copy(), true);
            int accepted = simulatedExtract.getCount() - remainder.getCount();
            if (accepted <= 0) {
                continue;
            }
            ItemStack extracted = source.extractItem(slot, accepted, false);
            ItemStack returned = insertIntoInternal(extracted, false);
            moved += extracted.getCount() - returned.getCount();
            if (!returned.isEmpty()) {
                insertIntoHandler(source, returned, false);
            }
        }
        return moved;
    }

    private int suckDroppedItemsFromTop() {
        AxisAlignedBB box = new AxisAlignedBB(getPos()).offset(0.0D, 1.0D, 0.0D);
        List<EntityItem> entities = getWorld().getEntitiesWithinAABB(EntityItem.class, box);
        int limit = stackSizeLimit;
        int moved = 0;
        for (EntityItem entityItem : entities) {
            if (entityItem.isDead || moved >= limit) {
                continue;
            }
            ItemStack entityStack = entityItem.getItem();
            if (entityStack.isEmpty()) {
                continue;
            }
            ItemStack insertStack = entityStack.copy();
            insertStack.setCount(Math.min(insertStack.getCount(), limit - moved));
            ItemStack remainder = insertIntoInternal(insertStack, false);
            int accepted = insertStack.getCount() - remainder.getCount();
            if (accepted > 0) {
                entityStack.shrink(accepted);
                moved += accepted;
                if (entityStack.isEmpty()) {
                    entityItem.setDead();
                } else {
                    entityItem.setItem(entityStack);
                }
            }
        }
        return moved;
    }

    @Nullable
    private IItemHandler getAdjacentItemHandler(EnumFacing offsetSide, EnumFacing accessSide) {
        TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(offsetSide));
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide)) {
            return null;
        }
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide);
    }

    private ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
            remaining = handler.insertItem(i, remaining, simulate);
        }
        return remaining;
    }

    private ItemStack insertIntoInternal(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (queueMode) {
            return inventory.insertItem(0, stack, simulate);
        }
        ItemStack remaining = stack.copy();
        for (int i = 0; i < slotCount && !remaining.isEmpty(); i++) {
            ItemStack current = inventory.getStackInSlot(i);
            if (!current.isEmpty() && ItemHandlerHelper.canItemStacksStack(current, remaining)) {
                remaining = inventory.insertItem(i, remaining, simulate);
            }
        }
        for (int i = 0; i < slotCount && !remaining.isEmpty(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                remaining = inventory.insertItem(i, remaining, simulate);
            }
        }
        return remaining;
    }

    private ItemStack extractFromOutput(int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        if (queueMode) {
            return inventory.extractItem(slotCount - 1, amount, simulate);
        }
        for (int i = 0; i < slotCount; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return inventory.extractItem(i, amount, simulate);
            }
        }
        return ItemStack.EMPTY;
    }

    private void mergeInventory() {
        for (int i = 0; i < slotCount; i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                for (int j = i + 1; j < slotCount; j++) {
                    if (moveSlot(j, i)) {
                        break;
                    }
                }
            }
            ItemStack base = inventory.getStackInSlot(i);
            if (base.isEmpty()) {
                continue;
            }
            for (int j = i + 1; j < slotCount && base.getCount() < inventory.getSlotLimit(i); j++) {
                ItemStack other = inventory.getStackInSlot(j);
                if (!other.isEmpty() && ItemHandlerHelper.canItemStacksStack(base, other)) {
                    moveSlot(j, i);
                    base = inventory.getStackInSlot(i);
                }
            }
        }
    }

    private void compactQueue() {
        boolean moved;
        do {
            moved = false;
            for (int i = 1; i < slotCount; i++) {
                moved |= moveSlot(i - 1, i);
            }
        } while (moved);
    }

    private boolean moveSlot(int from, int to) {
        ItemStack source = inventory.getStackInSlot(from);
        if (source.isEmpty()) {
            return false;
        }
        ItemStack remainder = inventory.insertItem(to, source.copy(), true);
        int accepted = source.getCount() - remainder.getCount();
        if (accepted <= 0) {
            return false;
        }
        ItemStack extracted = inventory.extractItem(from, accepted, false);
        ItemStack returned = inventory.insertItem(to, extracted, false);
        if (!returned.isEmpty()) {
            inventory.insertItem(from, returned, false);
        }
        return returned.isEmpty() || returned.getCount() < extracted.getCount();
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return false;
        }
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(allHandler);
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(side == getFrontFacing() ? outputHandler : inputHandler);
        }
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag(NBT_INVENTORY, inventory.serializeNBT());
        data.setInteger(NBT_STACK_SIZE, stackSizeLimit);
        data.setBoolean(NBT_EXACT, exactMode);
        data.setBoolean(NBT_ACTIVE, active);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        inventory.deserializeNBT(data.getCompoundTag(NBT_INVENTORY));
        stackSizeLimit = data.hasKey(NBT_STACK_SIZE) ? clampStackSize(data.getInteger(NBT_STACK_SIZE)) : DEFAULT_STACK_SIZE;
        exactMode = data.getBoolean(NBT_EXACT);
        active = data.getBoolean(NBT_ACTIVE);
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
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE || dataId == DATA_ACTIVE) {
            active = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    private int clampStackSize(int value) {
        return Math.max(1, Math.min(DEFAULT_STACK_SIZE, value));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation,
                                     IVertexOperation[] pipeline) {
        renderTexturedBox(renderState, translation, pipeline, TOP_BASIN);
        renderTexturedBox(renderState, translation, pipeline, FUNNEL);
        Cuboid6 pipe = getOutputPipeBounds();
        if (pipe != null) {
            renderTexturedBox(renderState, translation, pipeline, pipe);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderTexturedBox(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                   Cuboid6 box) {
        for (EnumFacing side : EnumFacing.VALUES) {
            String face = getTextureFace(side);
            KineticRenderHelper.renderFace(renderState, translation, pipeline, side, box,
                    getTexturePrefix() + "/colored/" + face, color);
            KineticRenderHelper.renderOverlayFace(renderState, translation, pipeline, side, box,
                    getTexturePrefix() + "/overlay/" + face);
        }
    }

    private String getTexturePrefix() {
        return queueMode ? "machines/automation/queuehopper" : "machines/automation/hopper";
    }

    private String getTextureFace(EnumFacing side) {
        if (side == EnumFacing.UP) {
            return "top";
        }
        return side == EnumFacing.DOWN ? "bottom" : "side";
    }

    @Nullable
    private Cuboid6 getOutputPipeBounds() {
        switch (getFrontFacing()) {
            case DOWN:
                return new Cuboid6(P6, 0.0D, P6, P10, P4, P10);
            case NORTH:
                return new Cuboid6(P6, P4, 0.0D, P10, P8, P4);
            case SOUTH:
                return new Cuboid6(P6, P4, P12, P10, P8, 1.0D);
            case WEST:
                return new Cuboid6(0.0D, P4, P6, P4, P8, P10);
            case EAST:
                return new Cuboid6(P12, P4, P6, 1.0D, P8, P10);
            case UP:
            default:
                return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(KineticRenderHelper.getSprite(getTexturePrefix() + "/colored/side"), color);
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
    public BlockFaceShape getFaceShape(EnumFacing side) {
        return side == EnumFacing.UP ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(new IndexedCuboid6(null, TOP_BASIN));
        collisionList.add(new IndexedCuboid6(null, FUNNEL));
        Cuboid6 pipe = getOutputPipeBounds();
        if (pipe != null) {
            collisionList.add(new IndexedCuboid6(null, pipe));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.gt6_hopper.tooltip.slots", slotCount));
        tooltip.add(I18n.format(queueMode ?
                "gt6addition.machine.gt6_hopper.tooltip.queue" :
                "gt6addition.machine.gt6_hopper.tooltip.normal"));
        tooltip.add(I18n.format("gt6addition.machine.gt6_hopper.tooltip.transfer"));
        tooltip.add(I18n.format("gt6addition.machine.gt6_hopper.tooltip.tools"));
    }

    public boolean isQueueMode() {
        return queueMode;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public int getStackSizeLimit() {
        return stackSizeLimit;
    }

    public boolean isExactMode() {
        return exactMode;
    }

    public int getUsedSlots() {
        int used = 0;
        for (int i = 0; i < slotCount; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                used++;
            }
        }
        return used;
    }

    public int getTotalItemCount() {
        int total = 0;
        for (int i = 0; i < slotCount; i++) {
            total += inventory.getStackInSlot(i).getCount();
        }
        return total;
    }

    public ItemStack getOutputStackPreview() {
        return extractFromOutput(DEFAULT_STACK_SIZE, true);
    }

    private class HopperInventory extends ItemStackHandler {

        HopperInventory(int size) {
            super(size);
        }

        @Override
        public int getSlotLimit(int slot) {
            return stackSizeLimit;
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    }

    private class SidedItemHandler implements IItemHandler {

        private final boolean allowInsert;
        private final boolean allowExtract;

        SidedItemHandler(boolean allowInsert, boolean allowExtract) {
            this.allowInsert = allowInsert;
            this.allowExtract = allowExtract;
        }

        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!allowInsert) {
                return stack;
            }
            if (queueMode && slot != 0) {
                return stack;
            }
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!allowExtract) {
                return ItemStack.EMPTY;
            }
            if (queueMode && slot != slotCount - 1) {
                return ItemStack.EMPTY;
            }
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }
    }
}
