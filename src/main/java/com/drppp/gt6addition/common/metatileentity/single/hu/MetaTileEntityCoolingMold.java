package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MetaTileEntityCoolingMold extends MetaTileEntity {

    private static final String NBT_INVENTORY = "Inventory";
    private static final String NBT_TANK = "Tank";
    private static final String NBT_PROGRESS = "Progress";
    private static final String NBT_DURATION = "Duration";
    private static final int DATA_RENDER_STATE = 202;
    private static final int CAPACITY = (int) (9L * GTValues.L);
    private static final int MIN_COOLING_TIME = 20;
    private static final double WALL_SIZE = 0.125D;
    private static final double MOLD_HEIGHT = 0.375D;
    private static final Cuboid6 WALL_X_NEG = new Cuboid6(0.0D, 0.0D, 0.0D, WALL_SIZE, MOLD_HEIGHT, 1.0D);
    private static final Cuboid6 WALL_X_POS = new Cuboid6(1.0D - WALL_SIZE, 0.0D, 0.0D, 1.0D, MOLD_HEIGHT, 1.0D);
    private static final Cuboid6 WALL_Z_NEG = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, MOLD_HEIGHT, WALL_SIZE);
    private static final Cuboid6 WALL_Z_POS = new Cuboid6(0.0D, 0.0D, 1.0D - WALL_SIZE, 1.0D, MOLD_HEIGHT, 1.0D);
    private static final Cuboid6 BOTTOM = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, WALL_SIZE, 1.0D);

    private final int tier;
    private final int casingColor;
    private final boolean acidProof;
    private final float hardness;
    private final float resistance;
    private final ItemStackHandler inventory = new MoldItemHandler();
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            markDirty();
            refreshRenderState();
        }
    };

    private int coolingProgress;
    private int coolingDuration;
    private int lastRecipeHash;
    private int displayFluidAmount;
    private int lastDisplayFluidAmount = -1;
    private String displayFluidName = "";
    private String lastDisplayFluidName = "";

    public MetaTileEntityCoolingMold(ResourceLocation metaTileEntityId, int tier, int casingColor,
                                     boolean acidProof, float hardness, float resistance) {
        super(metaTileEntityId);
        this.tier = tier;
        this.casingColor = casingColor;
        this.acidProof = acidProof;
        this.hardness = hardness;
        this.resistance = resistance;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCoolingMold(metaTileEntityId, tier, casingColor, acidProof, hardness, resistance);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            updateCooling();
        }
    }

    private void updateCooling() {
        Recipe recipe = findSolidifierRecipe();
        if (recipe == null || !canFitOutput(recipe)) {
            resetProgress();
            return;
        }
        int recipeHash = recipe.hashCode();
        if (lastRecipeHash != recipeHash) {
            coolingProgress = 0;
            coolingDuration = Math.max(MIN_COOLING_TIME, recipe.getDuration());
            lastRecipeHash = recipeHash;
        }
        coolingProgress++;
        if (coolingProgress >= coolingDuration) {
            completeRecipe(recipe);
            resetProgress();
        }
        markDirty();
    }

    @Nullable
    private Recipe findSolidifierRecipe() {
        ItemStack moldStack = inventory.getStackInSlot(0);
        FluidStack fluidStack = tank.getFluid();
        if (moldStack.isEmpty() || fluidStack == null || fluidStack.amount <= 0) {
            return null;
        }
        return RecipeMaps.FLUID_SOLIDFICATION_RECIPES.findRecipe(Long.MAX_VALUE,
                Collections.singletonList(moldStack.copy()),
                Collections.singletonList(fluidStack.copy()),
                true);
    }

    private boolean canFitOutput(Recipe recipe) {
        List<ItemStack> outputs = recipe.getOutputs();
        if (outputs.isEmpty() || outputs.get(0).isEmpty()) {
            return false;
        }
        ItemStack current = inventory.getStackInSlot(1);
        ItemStack output = outputs.get(0);
        if (current.isEmpty()) {
            return output.getCount() <= output.getMaxStackSize();
        }
        return ItemHandlerHelper.canItemStacksStack(current, output) &&
                current.getCount() + output.getCount() <= current.getMaxStackSize();
    }

    private void completeRecipe(Recipe recipe) {
        if (!consumeFluidInputs(recipe)) {
            return;
        }
        ItemStack output = recipe.getOutputs().get(0).copy();
        ItemStack current = inventory.getStackInSlot(1);
        if (current.isEmpty()) {
            inventory.setStackInSlot(1, output);
        } else if (ItemHandlerHelper.canItemStacksStack(current, output)) {
            current.grow(output.getCount());
            inventory.setStackInSlot(1, current);
        }
        markDirty();
        refreshRenderState();
    }

    private boolean consumeFluidInputs(Recipe recipe) {
        for (GTRecipeInput input : recipe.getFluidInputs()) {
            FluidStack required = input.getInputFluidStack();
            if (required == null || input.isNonConsumable()) {
                continue;
            }
            FluidStack drained = tank.drain(required.amount, true);
            if (drained == null || drained.amount < required.amount) {
                return false;
            }
        }
        return true;
    }

    private void resetProgress() {
        if (coolingProgress != 0 || coolingDuration != 0 || lastRecipeHash != 0) {
            coolingProgress = 0;
            coolingDuration = 0;
            lastRecipeHash = 0;
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
        if (player.isSneaking() && heldStack.isEmpty()) {
            ItemStack moldStack = inventory.extractItem(0, 1, false);
            if (!moldStack.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, moldStack);
            }
            return true;
        }
        if (!heldStack.isEmpty()) {
            FluidActionResult emptyResult = FluidUtil.tryEmptyContainer(heldStack, tank, CAPACITY, player, true);
            if (emptyResult.isSuccess()) {
                player.setHeldItem(hand, emptyResult.getResult());
                return true;
            }
            if (isGtceuShapeMold(heldStack) && inventory.getStackInSlot(0).isEmpty()) {
                ItemStack moldStack = heldStack.splitStack(1);
                inventory.setStackInSlot(0, moldStack);
                player.setHeldItem(hand, heldStack);
                markDirty();
                return true;
            }
            return true;
        }
        ItemStack outputStack = inventory.extractItem(1, 64, false);
        if (!outputStack.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, outputStack);
        } else {
            player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.cooling_mold.status.empty"), true);
        }
        return true;
    }

    private boolean isGtceuShapeMold(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (MetaItem<?>.MetaValueItem mold : MetaItems.SHAPE_MOLDS) {
            if (mold != null && mold.isItemEqual(stack)) {
                return true;
            }
        }
        return false;
    }

    public int getCoolingProgress() {
        return coolingProgress;
    }

    public int getCoolingDuration() {
        return coolingDuration;
    }

    public int getFluidAmount() {
        FluidStack fluidStack = tank.getFluid();
        return fluidStack == null ? 0 : fluidStack.amount;
    }

    public int getCapacity() {
        return CAPACITY;
    }

    public ItemStack getMoldStack() {
        return inventory.getStackInSlot(0).copy();
    }

    public ItemStack getOutputStack() {
        return inventory.getStackInSlot(1).copy();
    }

    public String getFluidDisplayName() {
        FluidStack fluidStack = tank.getFluid();
        return fluidStack == null ? "" : fluidStack.getLocalizedName();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return side != EnumFacing.UP;
        }
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
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != EnumFacing.UP) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return null;
        }
        return super.getCapability(capability, side);
    }

    private void refreshRenderState() {
        calculateRenderState();
        if (getWorld() != null && !getWorld().isRemote && isRenderStateChanged()) {
            writeCustomData(DATA_RENDER_STATE, this::writeRenderState);
            rememberRenderState();
            scheduleRenderUpdate();
        }
    }

    private void calculateRenderState() {
        FluidStack fluidStack = tank.getFluid();
        if (fluidStack == null || fluidStack.amount <= 0) {
            displayFluidAmount = 0;
            displayFluidName = "";
            return;
        }
        displayFluidAmount = Math.min(CAPACITY, fluidStack.amount);
        displayFluidName = fluidStack.getFluid().getName();
    }

    private boolean isRenderStateChanged() {
        return displayFluidAmount != lastDisplayFluidAmount || !displayFluidName.equals(lastDisplayFluidName);
    }

    private void rememberRenderState() {
        lastDisplayFluidAmount = displayFluidAmount;
        lastDisplayFluidName = displayFluidName;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] shellPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(casingColor)));
        Textures.SOLID_STEEL_CASING.render(renderState, translation, shellPipeline, WALL_X_NEG);
        Textures.SOLID_STEEL_CASING.render(renderState, translation, shellPipeline, WALL_Z_NEG);
        Textures.SOLID_STEEL_CASING.render(renderState, translation, shellPipeline, WALL_X_POS);
        Textures.SOLID_STEEL_CASING.render(renderState, translation, shellPipeline, WALL_Z_POS);
        Textures.SOLID_STEEL_CASING.render(renderState, translation, shellPipeline, BOTTOM);
        renderFluid(renderState, translation, pipeline);
    }

    @SideOnly(Side.CLIENT)
    private void renderFluid(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Fluid fluid = FluidRegistry.getFluid(displayFluidName);
        if (fluid == null || displayFluidAmount <= 0) {
            return;
        }
        FluidStack fluidStack = new FluidStack(fluid, displayFluidAmount);
        double top = WALL_SIZE + (MOLD_HEIGHT - WALL_SIZE) * displayFluidAmount / (double) CAPACITY;
        Cuboid6 contentBounds = new Cuboid6(WALL_SIZE, WALL_SIZE, WALL_SIZE, 1.0D - WALL_SIZE, top, 1.0D - WALL_SIZE);
        IVertexOperation[] contentPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(fluid.getColor(fluidStack) & 0xFFFFFF)));
        Textures.renderFace(renderState, translation, contentPipeline, EnumFacing.UP, contentBounds,
                getFluidSprite(fluidStack), BlockRenderLayer.CUTOUT_MIPPED);
    }

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite getFluidSprite(FluidStack fluidStack) {
        ResourceLocation still = fluidStack.getFluid().getStill(fluidStack);
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(still == null ? "minecraft:blocks/water_still" : still.toString());
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
    public BlockFaceShape getFaceShape(EnumFacing side) {
        return side == EnumFacing.UP ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(new IndexedCuboid6(null, WALL_X_NEG));
        collisionList.add(new IndexedCuboid6(null, WALL_Z_NEG));
        collisionList.add(new IndexedCuboid6(null, WALL_X_POS));
        collisionList.add(new IndexedCuboid6(null, WALL_Z_POS));
        collisionList.add(new IndexedCuboid6(null, BOTTOM));
    }

    @Override
    public float getBlockHardness() {
        return hardness;
    }

    @Override
    public float getBlockResistance() {
        return resistance;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.cooling_mold.tooltip.capacity", CAPACITY));
        tooltip.add(I18n.format("gt6addition.machine.cooling_mold.tooltip.recipe_map"));
        tooltip.add(I18n.format("gt6addition.machine.cooling_mold.tooltip.operation"));
        if (acidProof) {
            tooltip.add(I18n.format("gt6addition.machine.cooling_mold.tooltip.acid_proof"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag(NBT_INVENTORY, inventory.serializeNBT());
        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        data.setTag(NBT_TANK, tankTag);
        data.setInteger(NBT_PROGRESS, coolingProgress);
        data.setInteger(NBT_DURATION, coolingDuration);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        inventory.deserializeNBT(data.getCompoundTag(NBT_INVENTORY));
        tank.readFromNBT(data.getCompoundTag(NBT_TANK));
        coolingProgress = data.getInteger(NBT_PROGRESS);
        coolingDuration = data.getInteger(NBT_DURATION);
        calculateRenderState();
        rememberRenderState();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        calculateRenderState();
        rememberRenderState();
        writeRenderState(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        readRenderState(buf);
        rememberRenderState();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == DATA_RENDER_STATE) {
            readRenderState(buf);
            rememberRenderState();
            scheduleRenderUpdate();
        }
    }

    private void writeRenderState(PacketBuffer buf) {
        buf.writeVarInt(displayFluidAmount);
        buf.writeString(displayFluidName);
    }

    private void readRenderState(PacketBuffer buf) {
        displayFluidAmount = buf.readVarInt();
        displayFluidName = buf.readString(Short.MAX_VALUE);
    }

    private class MoldItemHandler extends ItemStackHandler {

        private MoldItemHandler() {
            super(2);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 1 : 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && isGtceuShapeMold(stack);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != 0 || !isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    }
}
