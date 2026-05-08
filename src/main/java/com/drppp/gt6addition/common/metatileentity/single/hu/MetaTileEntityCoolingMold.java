package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import gregtech.api.GregTechAPI;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityCoolingMold extends MetaTileEntity implements ICrucibleMold {

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
    private final long maxTemperature;
    private final ItemStackHandler inventory = new MoldItemHandler();
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            Material material = getMaterialFromFluid(resource);
            if (material == null || !hasSolidOutput(material)) {
                return 0;
            }
            if (resource.getFluid().getTemperature(resource) > maxTemperature) {
                if (doFill) {
                    meltDown();
                }
                return 0;
            }
            return super.fill(resource, doFill);
        }

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
    private ItemStack displayOutputStack = ItemStack.EMPTY;
    private ItemStack lastDisplayOutputStack = ItemStack.EMPTY;

    public MetaTileEntityCoolingMold(ResourceLocation metaTileEntityId, int tier, int casingColor,
                                     boolean acidProof, float hardness, float resistance) {
        this(metaTileEntityId, tier, casingColor, acidProof, hardness, resistance,
                getDefaultMaxTemperature(tier));
    }

    public MetaTileEntityCoolingMold(ResourceLocation metaTileEntityId, int tier, int casingColor,
                                     boolean acidProof, float hardness, float resistance, long maxTemperature) {
        super(metaTileEntityId);
        this.tier = tier;
        this.casingColor = casingColor;
        this.acidProof = acidProof;
        this.hardness = hardness;
        this.resistance = resistance;
        this.maxTemperature = Math.max(295L, maxTemperature);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCoolingMold(metaTileEntityId, tier, casingColor,
                acidProof, hardness, resistance, maxTemperature);
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
        return findSolidifierRecipe(moldStack, fluidStack);
    }

    @Nullable
    private Recipe findSolidifierRecipe(ItemStack moldStack, FluidStack fluidStack) {
        if (moldStack.isEmpty() || fluidStack == null || fluidStack.amount <= 0) {
            return null;
        }
        Recipe bestRecipe = null;
        int bestRequiredFluid = 0;
        for (Recipe recipe : RecipeMaps.FLUID_SOLIDFICATION_RECIPES.getRecipeList()) {
            int requiredFluid = getRequiredFluidAmount(recipe, moldStack, fluidStack);
            if (requiredFluid <= 0 || fluidStack.amount < requiredFluid || !canFitOutput(recipe)) {
                continue;
            }
            if (bestRecipe == null || requiredFluid > bestRequiredFluid) {
                bestRecipe = recipe;
                bestRequiredFluid = requiredFluid;
            }
        }
        return bestRecipe;
    }

    private int getRequiredFluidAmount(Recipe recipe, ItemStack moldStack, FluidStack fluidStack) {
        if (recipe == null || moldStack.isEmpty() || fluidStack == null) {
            return 0;
        }
        boolean acceptsMold = recipe.getInputs().isEmpty();
        for (GTRecipeInput input : recipe.getInputs()) {
            if (input.acceptsStack(moldStack)) {
                acceptsMold = true;
                break;
            }
        }
        if (!acceptsMold) {
            return 0;
        }
        int requiredFluid = 0;
        for (GTRecipeInput input : recipe.getFluidInputs()) {
            FluidStack required = input.getInputFluidStack();
            if (required == null || input.isNonConsumable()) {
                continue;
            }
            if (!required.isFluidEqual(fluidStack) && !input.acceptsFluid(fluidStack)) {
                return 0;
            }
            requiredFluid += required.amount;
        }
        return requiredFluid;
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

    @Override
    public boolean isMoldInputSide(@Nullable EnumFacing side) {
        return true;
    }

    @Override
    public long getMoldMaxTemperature() {
        return maxTemperature;
    }

    @Override
    public long getMoldRequiredMaterialUnits(@Nullable Material material) {
        if (material == null || !material.hasFluid() || !hasSolidOutput(material) || tank.getFluid() != null) {
            return 0L;
        }
        ItemStack moldStack = inventory.getStackInSlot(0);
        if (moldStack.isEmpty()) {
            return 0L;
        }
        FluidStack fluidStack = material.getFluid(CAPACITY);
        Recipe recipe = findSolidifierRecipe(moldStack, fluidStack);
        if (recipe == null || !canFitOutput(recipe)) {
            return 0L;
        }
        FluidStack requiredFluid = getRequiredRecipeFluidInput(recipe, fluidStack);
        return requiredFluid == null ? 0L : toMaterialAmount(requiredFluid.amount);
    }

    @Override
    public long fillMold(Material material, long materialAmount, long temperature,
                         @Nullable EnumFacing side, boolean simulate) {
        if (material == null || !material.hasFluid() || materialAmount <= 0L ||
                !isMoldInputSide(side) || !hasSolidOutput(material) || tank.getFluid() != null) {
            return 0L;
        }
        if (temperature > maxTemperature) {
            if (!simulate) {
                meltDown();
            }
            return 0L;
        }
        ItemStack moldStack = inventory.getStackInSlot(0);
        if (moldStack.isEmpty()) {
            return 0L;
        }
        FluidStack fluidStack = material.getFluid(CAPACITY);
        Recipe recipe = findSolidifierRecipe(moldStack, fluidStack);
        if (recipe == null || !canFitOutput(recipe)) {
            return 0L;
        }
        FluidStack requiredFluid = getRequiredRecipeFluidInput(recipe, fluidStack);
        if (requiredFluid == null || requiredFluid.amount <= 0) {
            return 0L;
        }
        long requiredMaterial = toMaterialAmount(requiredFluid.amount);
        if (materialAmount < requiredMaterial || tank.fill(requiredFluid, false) < requiredFluid.amount) {
            return 0L;
        }
        if (!simulate) {
            tank.fill(requiredFluid.copy(), true);
            resetProgress();
            markDirty();
            refreshRenderState();
        }
        return requiredMaterial;
    }

    @Nullable
    private FluidStack getRequiredRecipeFluidInput(Recipe recipe, FluidStack fluidStack) {
        if (recipe == null || fluidStack == null) {
            return null;
        }
        for (GTRecipeInput input : recipe.getFluidInputs()) {
            FluidStack required = input.getInputFluidStack();
            if (required != null && !input.isNonConsumable() && required.isFluidEqual(fluidStack)) {
                return required.copy();
            }
        }
        return null;
    }

    private static long toMaterialAmount(int fluidAmount) {
        return Math.max(1L, (fluidAmount * GTValues.M + GTValues.L - 1L) / GTValues.L);
    }

    @Nullable
    private Material getMaterialFromFluid(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) {
            return null;
        }
        for (Material registeredMaterial : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (registeredMaterial == null || !registeredMaterial.hasFluid()) {
                continue;
            }
            FluidStack materialFluid = registeredMaterial.getFluid(1);
            if (materialFluid != null && materialFluid.isFluidEqual(stack)) {
                return registeredMaterial;
            }
        }
        return null;
    }

    private boolean hasSolidOutput(Material material) {
        if (material == null || material == Materials.NULL) {
            return false;
        }
        OrePrefix[] prefixes = {OrePrefix.block, OrePrefix.ingot, OrePrefix.dust, OrePrefix.nugget,
                OrePrefix.dustSmall, OrePrefix.dustTiny};
        for (OrePrefix prefix : prefixes) {
            if (!OreDictUnifier.get(prefix, material).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void meltDown() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        inventory.setStackInSlot(1, ItemStack.EMPTY);
        tank.setFluid(null);
        resetProgress();
        markDirty();
        refreshRenderState();
        if (getWorld() != null && !getWorld().isRemote) {
            getWorld().setBlockState(getPos(), Blocks.FLOWING_LAVA.getDefaultState(), 3);
        }
    }

    private static long getDefaultMaxTemperature(int tier) {
        return 1800L + Math.max(0, tier) * 300L;
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
            return true;
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
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
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
        } else {
            displayFluidAmount = Math.min(CAPACITY, fluidStack.amount);
            displayFluidName = fluidStack.getFluid().getName();
        }
        displayOutputStack = inventory.getStackInSlot(1).copy();
    }

    private boolean isRenderStateChanged() {
        return displayFluidAmount != lastDisplayFluidAmount || !displayFluidName.equals(lastDisplayFluidName) ||
                !ItemStack.areItemStacksEqual(displayOutputStack, lastDisplayOutputStack);
    }

    private void rememberRenderState() {
        lastDisplayFluidAmount = displayFluidAmount;
        lastDisplayFluidName = displayFluidName;
        lastDisplayOutputStack = displayOutputStack.copy();
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
        renderOutputItem(renderState, translation, pipeline);
    }

    @SideOnly(Side.CLIENT)
    private void renderOutputItem(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (displayOutputStack.isEmpty()) {
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getRenderItem()
                .getItemModelWithOverrides(displayOutputStack, null, null)
                .getParticleTexture();
        Cuboid6 outputBounds = new Cuboid6(0.25D, WALL_SIZE + 0.01D, 0.25D,
                0.75D, WALL_SIZE + 0.015D, 0.75D);
        Textures.renderFace(renderState, translation, pipeline, EnumFacing.UP, outputBounds,
                sprite, BlockRenderLayer.CUTOUT_MIPPED);
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
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.SOLID_STEEL_CASING.getParticleSprite(), casingColor);
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
        ByteBufUtils.writeItemStack(buf, displayOutputStack);
    }

    private void readRenderState(PacketBuffer buf) {
        displayFluidAmount = buf.readVarInt();
        displayFluidName = buf.readString(Short.MAX_VALUE);
        displayOutputStack = ByteBufUtils.readItemStack(buf);
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
            refreshRenderState();
        }
    }
}
