package com.drppp.gt6addition.common.metatileentity.multiblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.casing.DeclarativePatternBuilder;
import gregtech.api.pattern.element.StructureDefinition;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/** GT6's 3x3x3 hollow, fire-brick Coke Oven backed by GTCE's Coke Oven recipes. */
public class MetaTileEntityCokeOven extends RecipeMapMultiblockController {
    private static final String NBT_IGNITED = "CokeOvenIgnited";
    private static final String NBT_INPUT = "CokeOvenInput";
    private static final String NBT_OUTPUT = "CokeOvenOutput";
    private static final String NBT_FLUID_OUTPUT = "CokeOvenFluidOutput";
    private static final int DATA_IGNITED = 0x4741;
    private static final int PARALLEL = 16;
    private static final int ITEM_SLOTS = 16;
    private static final int OUTPUT_FLUID_CAPACITY = 16_000;
    private static final IEnergyContainer FREE_HEAT = new IEnergyContainer() {
        @Override public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) { return 0; }
        @Override public boolean inputsEnergy(EnumFacing side) { return false; }
        @Override public long changeEnergy(long differenceAmount) { return differenceAmount; }
        @Override public long getEnergyStored() { return Long.MAX_VALUE; }
        @Override public long getEnergyCapacity() { return Long.MAX_VALUE; }
        @Override public long getInputAmperage() { return Long.MAX_VALUE; }
        @Override public long getInputVoltage() { return Long.MAX_VALUE; }
    };

    private IItemHandlerModifiable inputItems;
    private IItemHandlerModifiable outputItems;
    private final FluidTankList inputFluids = new FluidTankList(false);
    private FluidTank outputFluidTank;
    private FluidTankList outputFluids;
    private boolean ignited;

    public MetaTileEntityCokeOven(ResourceLocation id) {
        super(id, RecipeMaps.COKE_OVEN_RECIPES);
        createInventories();
        this.recipeMapWorkable = new CokeOvenRecipeLogic(this);
        this.recipeMapWorkable.setParallelLimit(PARALLEL);
    }

    private void createInventories() {
        inputItems = new GTItemStackHandler(this, ITEM_SLOTS);
        outputItems = new GTItemStackHandler(this, ITEM_SLOTS);
        outputFluidTank = new FluidTank(OUTPUT_FLUID_CAPACITY);
        outputFluids = new FluidTankList(false, outputFluidTank);
        inputInventory = inputItems;
        outputInventory = outputItems;
        inputFluidInventory = inputFluids;
        outputFluidInventory = outputFluids;
        energyContainer = FREE_HEAT;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCokeOven(metaTileEntityId);
    }

    @Override
    protected void initializeAbilities() {
        // GT6's Coke Oven has built-in buffers; its shell is exactly 25 firebricks,
        // rather than requiring GregTech item/fluid/energy hatch substitutions.
        // Formation is not a new machine instance: keep the buffers created by the
        // constructor so items and fluids survive a structure break/re-form cycle.
        inputInventory = inputItems;
        outputInventory = outputItems;
        inputFluidInventory = inputFluids;
        outputFluidInventory = outputFluids;
        energyContainer = FREE_HEAT;
    }

    @Override
    protected StructureDefinition<?> createStructureDefinition() {
        return DeclarativePatternBuilder.start()
                .aisle("BBB", "B~B", "BBB")
                .aisle("BBB", "B B", "BBB")
                .aisle("BBB", "BBB", "BBB")
                .block('B', GT6AdditionBlocks.COKE_OVEN_BRICK.getDefaultState())
                .air(' ')
                .self('~', MetaTileEntityCokeOven.class)
                .buildStructureDefinition();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        return Gt6AdditionTextures.COKE_OVEN_BASE;
    }

    @Override
    protected void updateFormedValid() {
        if (ignited) super.updateFormedValid();
        pushOutputs();
    }

    private void pushOutputs() {
        if (getWorld() == null || getWorld().isRemote) return;
        TileEntity below = getWorld().getTileEntity(getPos().down());
        if (below == null) return;
        IItemHandler itemTarget = below.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        if (itemTarget != null) {
            for (int slot = 0; slot < outputItems.getSlots(); slot++) {
                ItemStack output = outputItems.getStackInSlot(slot);
                if (output.isEmpty()) continue;
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(itemTarget, output, false);
                int moved = output.getCount() - remainder.getCount();
                if (moved > 0) outputItems.extractItem(slot, moved, false);
            }
        }
        if (below.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP)) {
            FluidUtil.tryFluidTransfer(
                    below.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP),
                    outputFluids, 1_000, true);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side == EnumFacing.UP) return true;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side == EnumFacing.DOWN) return true;
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side == EnumFacing.DOWN) return true;
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == EnumFacing.UP) return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inputItems);
            if (side == EnumFacing.DOWN) return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(outputItems);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side == EnumFacing.DOWN) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(outputFluids);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing side,
                                codechicken.lib.raytracer.CuboidRayTraceResult hitResult) {
        ItemStack held = player.getHeldItem(hand);
        if (side == getFrontFacing() && isIgniter(held)) {
            if (getWorld().isRemote) return true;
            if (!ignited) setIgnited(true);
            if (!player.capabilities.isCreativeMode) {
                if (held.getItem() == Items.FLINT_AND_STEEL) held.damageItem(1, player);
                else held.shrink(1);
            }
            return true;
        }
        if (!held.isEmpty() && !player.isSneaking() && side == EnumFacing.UP) {
            if (getWorld().isRemote) return true;
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(inputItems, held, false);
            player.setHeldItem(hand, remainder);
            return true;
        }
        if (held.isEmpty()) {
            if (!getWorld().isRemote) {
                player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(
                        "Coke Oven: " + (isStructureFormed() ? "formed" : "structure incomplete") +
                                (ignited ? ", ignited" : ", needs ignition") +
                                ", progress " + recipeMapWorkable.getProgress() + "/" + recipeMapWorkable.getMaxProgress()), true);
            }
            return true;
        }
        return super.onRightClick(player, hand, side, hitResult);
    }

    private static boolean isIgniter(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE);
    }

    private void setIgnited(boolean ignited) {
        if (this.ignited == ignited) return;
        this.ignited = ignited;
        markDirty();
        writeCustomData(DATA_IGNITED, buf -> buf.writeBoolean(ignited));
        scheduleRenderUpdate();
    }

    public boolean isIgnited() {
        return ignited;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        Gt6AdditionTextures.COKE_OVEN_BASE.renderOrientedState(state, translation, pipeline,
                getFrontFacing(), isActive(), isActive());
        (isActive() ? Gt6AdditionTextures.COKE_OVEN_ACTIVE_OVERLAY : Gt6AdditionTextures.COKE_OVEN_OVERLAY)
                .renderOrientedState(state, translation, pipeline, getFrontFacing(), isActive(), isActive());
    }

    @Override
    public boolean isActive() {
        return super.isActive() && ignited;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(NBT_IGNITED, ignited);
        data.setTag(NBT_INPUT, ((GTItemStackHandler) inputItems).serializeNBT());
        data.setTag(NBT_OUTPUT, ((GTItemStackHandler) outputItems).serializeNBT());
        data.setTag(NBT_FLUID_OUTPUT, outputFluids.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        ignited = data.getBoolean(NBT_IGNITED);
        if (data.hasKey(NBT_INPUT)) ((GTItemStackHandler) inputItems).deserializeNBT(data.getCompoundTag(NBT_INPUT));
        if (data.hasKey(NBT_OUTPUT)) ((GTItemStackHandler) outputItems).deserializeNBT(data.getCompoundTag(NBT_OUTPUT));
        if (data.hasKey(NBT_FLUID_OUTPUT)) outputFluids.deserializeNBT(data.getCompoundTag(NBT_FLUID_OUTPUT));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(ignited);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        ignited = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == DATA_IGNITED) {
            ignited = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable net.minecraft.world.World world,
                               List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add("3x3x3 hollow; 25 GT6 Coke Oven Bricks; center air");
        tooltip.add("Controller centered on the front side; needs flint and steel or fire charge");
        tooltip.add("GT Coke Oven recipes, up to 16 parallel; top input and bottom item/fluid output");
    }

    private static final class CokeOvenRecipeLogic extends MultiblockRecipeLogic {
        private final MetaTileEntityCokeOven oven;

        private CokeOvenRecipeLogic(MetaTileEntityCokeOven oven) {
            super(oven);
            this.oven = oven;
        }

        @Override
        protected boolean canProgressRecipe() {
            return oven.ignited && super.canProgressRecipe();
        }

        @Override
        protected boolean drawEnergy(long recipeEUt, boolean simulate) {
            return oven.ignited;
        }

        @Override
        protected boolean hasEnoughPower(long recipeEUt, int amperage) {
            return oven.ignited;
        }

        @Override
        public long getMaxVoltage() {
            return Long.MAX_VALUE;
        }
    }
}
