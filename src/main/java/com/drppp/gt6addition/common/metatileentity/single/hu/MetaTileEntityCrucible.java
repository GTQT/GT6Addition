package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.baseMTile.TieredMutiEnergyMetaTileEntity;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import com.drppp.gt6addition.common.material.GT6AdditionOrePrefixes;
import com.drppp.gt6addition.api.temperature.ITemperatureProvider;
import com.drppp.gt6addition.api.utils.EnergyTypeList;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.material.properties.IngotProperty;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MetaTileEntityCrucible extends TieredMutiEnergyMetaTileEntity implements ITemperatureProvider {

    private static final String NBT_TEMPERATURE = "Temperature";
    private static final String NBT_OLD_TEMPERATURE = "OldTemperature";
    private static final String NBT_CONTENTS = "Contents";
    private static final String NBT_MATERIAL = "Material";
    private static final String NBT_AMOUNT = "Amount";
    private static final String NBT_MOLTEN = "Molten";
    private static final String NBT_SOLIDIFY_TARGET = "SolidifyTarget";
    private static final String NBT_PENDING_ITEMS = "PendingItems";
    private static final String NBT_SPECIAL_FLUID = "SpecialFluid";
    private static final String NBT_SPECIAL_FLUID_AMOUNT = "SpecialFluidAmount";
    private static final String NBT_LAVA_CONDENSATION_PENDING = "LavaCondensationPending";
    private static final String NBT_STORED_HEAT = "StoredHeat";
    private static final int SPECIAL_FLUID_CAPACITY = 16_000;
    private static final int PENDING_ITEM_CAPACITY = 64;
    private static final int LAVA_OBSIDIAN_MILLIBUCKETS = 1_000;
    private static final int DATA_DISPLAY_STATE = 200;
    private static final long CAPACITY = 16L * GTValues.M;
    private static final long DEFAULT_ENVIRONMENT_TEMPERATURE = 293L;
    private static final int HEAT_COOLDOWN_TICKS = 100;
    private static final int PASSIVE_COOLDOWN_TICKS = 10;
    // This is the single-block GT6 Smeltery, not the 3x3x3 Crucible.
    private static final long GT6_VESSEL_MATERIAL_AMOUNT = 7L * GTValues.M;
    private static final double DEFAULT_MATERIAL_DENSITY_KG_PER_CUBIC_METER = 1_000.0D;
    private static final int AIR_DENSITY_LIMIT = 200;
    private static final long SCRAP_MATERIAL_AMOUNT = GTValues.M / 9L;
    private static final int RAIN_FILL_INTERVAL = 600;
    private static final int RAIN_FILL_OFFSET = 10;
    private static final int FLAME_RANGE = 3;
    private static final int HOT_BREAK_TEMPERATURE = 1300;
    private static final int GAS_DAMAGE_TEMPERATURE = 320;
    private static final int FIRE_TEMPERATURE = 2000;
    private static final int FLAMMABLE_TEMPERATURE = 313;
    private static final int CONTACT_DAMAGE_INTERVAL = 10;
    private static final double WALL_SIZE = 0.125D;
    private static final double CONTENT_HEIGHT_SCALE = 292.571428D;
    private static final Cuboid6 WALL_X_NEG = new Cuboid6(0.0D, 0.0D, 0.0D, WALL_SIZE, 1.0D, 1.0D);
    private static final Cuboid6 WALL_Z_NEG = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, WALL_SIZE);
    private static final Cuboid6 WALL_X_POS = new Cuboid6(1.0D - WALL_SIZE, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final Cuboid6 WALL_Z_POS = new Cuboid6(0.0D, 0.0D, 1.0D - WALL_SIZE, 1.0D, 1.0D, 1.0D);
    private static final Cuboid6 BOTTOM = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, WALL_SIZE, 1.0D);
    private static final String EMPTY_DISPLAY_MATERIAL = "";

    private final int color;
    private final int maxTemperature;
    @Nullable
    private final Material vesselMaterial;
    private final boolean acidProof;
    private final float blockHardness;
    private final float blockResistance;
    private final CrucibleFluidHandler fluidHandler = new CrucibleFluidHandler();
    private final List<StoredMaterial> contents = new ArrayList<>();
    private final List<ItemStack> pendingItems = new ArrayList<>();
    @Nullable
    private Fluid specialFluid;
    private int specialFluidAmount;
    private boolean lavaCondensationPending;
    private long temperature = DEFAULT_ENVIRONMENT_TEMPERATURE;
    private long oldTemperature = DEFAULT_ENVIRONMENT_TEMPERATURE;
    private long storedHeat;
    private int thermalCooldown = HEAT_COOLDOWN_TICKS;
    private boolean heatedThisTick;
    private boolean active;
    private int displayHeight;
    private int oldDisplayHeight = -1;
    private String displayMaterialName = EMPTY_DISPLAY_MATERIAL;
    private String oldDisplayMaterialName = null;
    private boolean displayMolten;
    private boolean oldDisplayMolten;
    private boolean meltDownWarning;
    private boolean oldMeltDownWarning;
    private String displaySpecialFluidName = "";
    private int displaySpecialFluidAmount;
    private int displayPendingItems;
    private String oldDisplaySpecialFluidName;
    private int oldDisplaySpecialFluidAmount = -1;
    private int oldDisplayPendingItems = -1;

    public MetaTileEntityCrucible(ResourceLocation metaTileEntityId, int tier, int color) {
        this(metaTileEntityId, tier, color, getDefaultMaxTemperature(tier));
    }

    public MetaTileEntityCrucible(ResourceLocation metaTileEntityId, int tier, int color, int maxTemperature) {
        this(metaTileEntityId, tier, color, maxTemperature, null, false, 6.0F, 6.0F);
    }

    public MetaTileEntityCrucible(ResourceLocation metaTileEntityId, int tier, int color, int maxTemperature,
                                  boolean acidProof, float blockHardness, float blockResistance) {
        this(metaTileEntityId, tier, color, maxTemperature, null, acidProof, blockHardness, blockResistance);
    }

    public MetaTileEntityCrucible(ResourceLocation metaTileEntityId, int tier, int color, int maxTemperature,
                                  @Nullable Material vesselMaterial, boolean acidProof,
                                  float blockHardness, float blockResistance) {
        super(metaTileEntityId, tier, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN});
        this.color = color;
        this.maxTemperature = Math.max((int) DEFAULT_ENVIRONMENT_TEMPERATURE, maxTemperature);
        this.vesselMaterial = vesselMaterial;
        this.acidProof = acidProof;
        this.blockHardness = blockHardness;
        this.blockResistance = blockResistance;
    }

    @Override
    protected void reinitializeEnergyContainer() {
        this.energyContainer = new EnergyContainerHandler(this, 0L, 0L, 0L, 0L, 0L) {
            @Override
            public boolean isOneProbeHidden() {
                return true;
            }

            @Override
            public boolean inputsEnergy(EnumFacing side) {
                return false;
            }

            @Override
            public boolean outputsEnergy(EnumFacing side) {
                return false;
            }
        };
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCrucible(metaTileEntityId, getTier(), color, maxTemperature, vesselMaterial,
                acidProof, blockHardness, blockResistance);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 0);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false);
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

        pullHeatFromBottom();
        processRainFill();
        processPendingItemQueue();
        processDroppedItems();
        processInputSlot();
        processSpecialFluids();
        // GT6 calculates the crucible's thermal mass after the current tick's
        // inputs, alloy conversion, and phase changes have updated the melt.
        processAlloys();
        removeEmptyContents();
        if (processMaterialTemperatureState()) {
            return;
        }
        oldTemperature = temperature;
        processStoredHeat();
        if (processTemperature()) {
            return;
        }
        processContactDamage();
        refreshDisplayState();
        setActive(mutiEnergyProxy != null && mutiEnergyProxy.getEnergy() > 0);
    }

    private void pullHeatFromBottom() {
        heatedThisTick = false;
        if (mutiEnergyProxy != null) {
            TileEntity tileEntity = getWorld().getTileEntity(getPos().down());
            if (tileEntity == null || !mutiEnergyProxy.getNearEnergyToMyself(tileEntity, EnumFacing.DOWN)) {
                mutiEnergyProxy.setEnergy(0);
            } else {
                int heat = mutiEnergyProxy.getEnergy();
                if (heat > 0) {
                    long updatedStoredHeat = CrucibleTransferLogic.accumulateHeat(storedHeat, heat);
                    int acceptedHeat = (int) Math.min(Integer.MAX_VALUE, updatedStoredHeat - storedHeat);
                    storedHeat = updatedStoredHeat;
                    // Buffer incoming HU locally. Heat containers are drained
                    // once on transfer; continuous-output HU sources are
                    // accumulated over ticks just like GT6's mEnergy buffer.
                    if (acceptedHeat > 0) {
                        markDirty();
                        mutiEnergyProxy.changeEnergy(acceptedHeat);
                    }
                }
            }
        }

    }

    private void processStoredHeat() {
        if (storedHeat <= 0L) {
            return;
        }
        double thermalMassKg = getThermalMassKg();
        long requiredEnergyPerKelvin = CrucibleTransferLogic.requiredEnergyPerKelvin(thermalMassKg);
        long temperatureGain = CrucibleTransferLogic.temperatureGainForHeat(storedHeat, thermalMassKg);
        if (temperatureGain <= 0L) {
            return;
        }
        long consumedEnergy = temperatureGain * requiredEnergyPerKelvin;
        storedHeat -= consumedEnergy;
        temperature += temperatureGain;
        markDirty();
        thermalCooldown = HEAT_COOLDOWN_TICKS;
        heatedThisTick = true;
    }

    private void processRainFill() {
        if (getOffsetTimer() % RAIN_FILL_INTERVAL != RAIN_FILL_OFFSET || !getWorld().isRainingAt(getPos().up())) {
            return;
        }
        Biome biome = getWorld().getBiome(getPos());
        float rainfall = biome.getRainfall();
        if (rainfall <= 0.0F || biome.getTemperature(getPos()) < 0.2F) {
            return;
        }

        int fluidAmount = Math.max(1, Math.round(rainfall * 100.0F));
        if (getWorld().isThundering()) {
            fluidAmount *= 2;
        }
        FluidStack rainWater = Materials.Water.getFluid(fluidAmount);
        fillSpecialFluid(rainWater, true, getAmbientTemperature());
    }

    private void processDroppedItems() {
        AxisAlignedBB bounds = new AxisAlignedBB(
                getPos().getX() + WALL_SIZE, getPos().getY() + WALL_SIZE, getPos().getZ() + WALL_SIZE,
                getPos().getX() + 1.0D - WALL_SIZE, getPos().getY() + 1.25D, getPos().getZ() + 1.0D - WALL_SIZE);
        for (EntityItem entityItem : getWorld().getEntitiesWithinAABB(EntityItem.class, bounds)) {
            if (entityItem.isDead) {
                continue;
            }
            ItemStack stack = entityItem.getItem();
            if (stack.isEmpty()) {
                entityItem.setDead();
                continue;
            }
            boolean acceptedAny = false;
            while (!stack.isEmpty()) {
                ItemStack oneItem = stack.copy();
                oneItem.setCount(1);
                if (!addMaterialFromItem(oneItem)) {
                    if (enqueuePendingItem(oneItem)) {
                        stack.shrink(1);
                        acceptedAny = true;
                        continue;
                    }
                    break;
                }
                stack.shrink(1);
                acceptedAny = true;
            }
            if (acceptedAny) {
                if (stack.isEmpty()) {
                    entityItem.setDead();
                } else {
                    entityItem.setItem(stack);
                }
            }
        }
    }

    private void processInputSlot() {
        ItemStack stack = importItems.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }

        ItemStack oneItem = stack.copy();
        oneItem.setCount(1);
        if (addMaterialFromItem(oneItem)) {
            importItems.extractItem(0, 1, false);
        } else if (enqueuePendingItem(oneItem)) {
            importItems.extractItem(0, 1, false);
        }
    }

    private void processPendingItemQueue() {
        while (!pendingItems.isEmpty()) {
            ItemStack first = pendingItems.get(0);
            ItemStack oneItem = first.copy();
            oneItem.setCount(1);
            if (!addMaterialFromItem(oneItem)) {
                break;
            }
            first.shrink(1);
            if (first.isEmpty()) {
                pendingItems.remove(0);
            }
        }
    }

    private boolean enqueuePendingItem(ItemStack stack) {
        if (stack.isEmpty() || CrucibleTransferLogic.acceptedQueueItems(pendingItemCount(), 1,
                PENDING_ITEM_CAPACITY) <= 0 || getInputMaterial(stack) == null) {
            return false;
        }
        for (ItemStack queued : pendingItems) {
            if (ItemStack.areItemsEqual(queued, stack) && ItemStack.areItemStackTagsEqual(queued, stack) &&
                    queued.getCount() < queued.getMaxStackSize()) {
                queued.grow(1);
                markDirty();
                refreshDisplayState();
                return true;
            }
        }
        ItemStack queued = stack.copy();
        queued.setCount(1);
        pendingItems.add(queued);
        markDirty();
        refreshDisplayState();
        return true;
    }

    private int pendingItemCount() {
        int count = 0;
        for (ItemStack stack : pendingItems) {
            count += stack.getCount();
        }
        return count;
    }

    private boolean addMaterialFromItem(ItemStack stack) {
        MaterialStack materialStack = getInputMaterial(stack);
        if (materialStack == null || materialStack.material == null || materialStack.amount <= 0) {
            return false;
        }
        if (getTotalAmount() + materialStack.amount > CAPACITY) {
            return false;
        }

        mixTemperature(getAmbientTemperature(), materialStack.material, materialStack.amount);
        StoredMaterial storedMaterial = new StoredMaterial(materialStack.material, materialStack.amount, false,
                materialStack.material);
        if (temperature >= getMeltingTemperature(storedMaterial.material)) {
            melt(storedMaterial);
        }
        addStoredMaterial(storedMaterial);
        markDirty();
        refreshDisplayState();
        return true;
    }

    @Nullable
    private MaterialStack getInputMaterial(ItemStack stack) {
        MaterialStack materialStack = OreDictUnifier.getMaterial(stack);
        if (materialStack == null || materialStack.material == null || materialStack.amount <= 0L) {
            return null;
        }

        OrePrefix prefix = OreDictUnifier.getPrefix(stack);
        if (isOreInputPrefix(prefix)) {
            Material oreMaterial = materialStack.material;
            OreProperty oreProperty = oreMaterial.hasProperty(PropertyKey.ORE) ?
                    oreMaterial.getProperty(PropertyKey.ORE) : null;
            if (oreProperty != null) {
                Material targetMaterial = oreProperty.getDirectSmeltResult() == null ? oreMaterial :
                        oreProperty.getDirectSmeltResult();
                long multiplier = Math.max(1, oreProperty.getOreMultiplier());
                materialStack = new MaterialStack(targetMaterial, materialStack.amount * multiplier);
            }
        }

        Material smeltingTarget = getSmeltingTarget(materialStack.material);
        boolean hasFluidOutput = smeltingTarget != null && smeltingTarget != Materials.NULL &&
                smeltingTarget.hasFluid() && smeltingTarget.getFluid(1) != null;
        return CrucibleTransferLogic.canMeltWithinLimit(maxTemperature,
                getMeltingTemperature(materialStack.material), hasFluidOutput) ? materialStack : null;
    }

    private boolean isOreInputPrefix(@Nullable OrePrefix prefix) {
        return prefix == OrePrefix.rawOre || prefix == OrePrefix.ore || prefix == OrePrefix.oreLean ||
                prefix == OrePrefix.oreGranite || prefix == OrePrefix.oreDiorite ||
                prefix == OrePrefix.oreAndesite || prefix == OrePrefix.oreBlackgranite ||
                prefix == OrePrefix.oreRedgranite || prefix == OrePrefix.oreMarble ||
                prefix == OrePrefix.oreBasalt || prefix == OrePrefix.oreGraniteLean ||
                prefix == OrePrefix.oreDioriteLean || prefix == OrePrefix.oreAndesiteLean ||
                prefix == OrePrefix.oreBlackgraniteLean || prefix == OrePrefix.oreRedgraniteLean ||
                prefix == OrePrefix.oreMarbleLean || prefix == OrePrefix.oreBasaltLean ||
                prefix == OrePrefix.oreSand || prefix == OrePrefix.oreRedSand ||
                prefix == OrePrefix.oreSandLean || prefix == OrePrefix.oreRedSandLean ||
                prefix == OrePrefix.oreNetherrack || prefix == OrePrefix.oreEndstone ||
                prefix == OrePrefix.oreNether || prefix == OrePrefix.oreEnd ||
                prefix == OrePrefix.oreNetherrackLean || prefix == OrePrefix.oreNetherLean ||
                prefix == OrePrefix.oreEndstoneLean || prefix == OrePrefix.oreEndLean;
    }

    private int addMaterialFromFluid(@Nullable FluidStack resource, boolean doFill) {
        return addMaterialFromFluid(resource, doFill, null);
    }

    private int addMaterialFromFluid(@Nullable FluidStack resource, boolean doFill,
                                     @Nullable Long temperatureOverride) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        if (getSpecialFluidKind(resource.getFluid()) != null) {
            return fillSpecialFluid(resource, doFill, temperatureOverride);
        }
        Material material = getMaterialFromFluid(resource);
        if (material == null || isAcidMaterial(material) || isGasLikeMaterial(material)) {
            return 0;
        }
        long space = CAPACITY - getTotalAmount();
        if (space <= 0) {
            return 0;
        }

        int acceptedFluid = Math.min(resource.amount, toFluidAmount(space));
        if (acceptedFluid <= 0) {
            return 0;
        }
        long materialAmount = Math.min(space, toMaterialAmount(acceptedFluid));
        if (materialAmount <= 0) {
            return 0;
        }

        if (doFill) {
            FluidStack acceptedStack = resource.copy();
            acceptedStack.amount = acceptedFluid;
            long incomingTemperature = temperatureOverride == null ?
                    acceptedStack.getFluid().getTemperature(acceptedStack) : temperatureOverride;
            mixTemperature(incomingTemperature, material, materialAmount);
            StoredMaterial received = new StoredMaterial(material, materialAmount, false,
                    getSolidifyingTarget(material));
            if (temperature >= getMeltingTemperature(material)) {
                melt(received);
            } else {
                solidify(received);
            }
            addStoredMaterial(received);
            markDirty();
            refreshDisplayState();
        }
        return acceptedFluid;
    }

    private int fillSpecialFluid(FluidStack resource, boolean doFill, @Nullable Long temperatureOverride) {
        Fluid fluid = resource.getFluid();
        String kind = getSpecialFluidKind(fluid);
        if (kind == null || (specialFluidAmount > 0 && specialFluid != fluid)) {
            return 0;
        }
        int accepted = CrucibleTransferLogic.acceptedFluidAmount(
                specialFluid == null ? null : specialFluid.getName(), specialFluidAmount,
                fluid.getName(), resource.amount, SPECIAL_FLUID_CAPACITY);
        if (accepted <= 0) {
            return 0;
        }
        if (doFill) {
            if ("lava".equals(kind) && CrucibleTransferLogic.shouldCondenseLava(temperature)) {
                lavaCondensationPending = true;
            }
            long incomingTemperature = temperatureOverride == null ?
                    fluid.getTemperature(resource) : temperatureOverride;
            Material thermalMaterial = "water".equals(kind) ? Materials.Water : Materials.Lava;
            mixTemperature(incomingTemperature, thermalMaterial, toMaterialAmount(accepted));
            specialFluid = fluid;
            specialFluidAmount += accepted;
            markDirty();
            refreshDisplayState();
        }
        return accepted;
    }

    @Nullable
    private String getSpecialFluidKind(@Nullable Fluid fluid) {
        if (fluid == null) return null;
        String name = fluid.getName();
        if ("water".equals(name)) return "water";
        if ("lava".equals(name)) return "lava";
        return null;
    }

    private void processSpecialFluids() {
        if (specialFluid == null || specialFluidAmount <= 0) {
            specialFluid = null;
            specialFluidAmount = 0;
            lavaCondensationPending = false;
            return;
        }
        String kind = getSpecialFluidKind(specialFluid);
        if ("water".equals(kind) && CrucibleTransferLogic.shouldBoilWater(temperature)) {
            int evaporatedAmount = specialFluidAmount;
            specialFluidAmount = 0;
            specialFluid = null;
            releaseHotGasEffects(temperature, toMaterialAmount(evaporatedAmount));
            markDirty();
            refreshDisplayState();
        } else if ("lava".equals(kind) && (lavaCondensationPending ||
                CrucibleTransferLogic.shouldCondenseLava(temperature))) {
            lavaCondensationPending = true;
            int blocksAvailable = CrucibleTransferLogic.obsidianUnitsForLava(specialFluidAmount);
            long blockCapacity = Math.max(0L, (CAPACITY - getTotalAmount()) / GTValues.M);
            int blocksToCondense = (int) Math.min(blocksAvailable, blockCapacity);
            if (blocksToCondense > 0) {
                addStoredMaterial(new StoredMaterial(Materials.Obsidian,
                        blocksToCondense * (long) GTValues.M, false, Materials.Obsidian));
                specialFluidAmount -= blocksToCondense * LAVA_OBSIDIAN_MILLIBUCKETS;
                if (specialFluidAmount <= 0) {
                    specialFluidAmount = 0;
                    specialFluid = null;
                    lavaCondensationPending = false;
                }
                markDirty();
                refreshDisplayState();
            }
        }
    }

    private void mixTemperature(long incomingTemperature, Material incomingMaterial, long incomingAmount) {
        double currentMass = getThermalMassKg();
        double incomingMass = getMaterialWeightKg(incomingMaterial, incomingAmount);
        temperature = CrucibleTransferLogic.mixTemperature(temperature, currentMass,
                incomingTemperature, incomingMass);
    }

    private double getThermalMassKg() {
        // GT6's single-block Smeltery includes its wall material at U * 7;
        // the multiblock Crucible's U * 100 wall basis does not apply here.
        // GT6's default material density is 1 g/cm^3 (1000 kg/m^3).
        double mass = getMaterialWeightKg(vesselMaterial, GT6_VESSEL_MATERIAL_AMOUNT);
        for (StoredMaterial material : contents) {
            mass += getMaterialWeightKg(material.material, material.amount);
        }
        if (specialFluid != null && specialFluidAmount > 0) {
            mass += specialFluidAmount * (double) Math.max(1, specialFluid.getDensity()) / 1_000_000.0D;
        }
        return Math.max(1.0D, mass);
    }

    private double getMaterialWeightKg(Material material, long amount) {
        double densityKgPerCubicMeter = getGt6MaterialDensityKgPerCubicMeter(material,
                Collections.newSetFromMap(new IdentityHashMap<Material, Boolean>()));
        double ceuMolecularMass = material == null ? 0.0D : material.getMass();
        return CrucibleTransferLogic.materialWeightKgFromMolecularMass(
                amount, GTValues.M, ceuMolecularMass, densityKgPerCubicMeter);
    }

    private double getGt6MaterialDensityKgPerCubicMeter(Material material, Set<Material> visiting) {
        if (material == null) {
            return DEFAULT_MATERIAL_DENSITY_KG_PER_CUBIC_METER;
        }

        if (CrucibleTransferLogic.hasKnownGt6MaterialDensity(material.getRegistryName())) {
            return CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(material.getRegistryName());
        }

        if (!visiting.add(material)) {
            return getRegisteredOrDefaultDensity(material);
        }
        try {
            MaterialStack[] components = material.getMaterialComponentsCt();
            if (components != null && components.length > 0) {
                double[] componentAmounts = new double[components.length];
                double[] componentDensities = new double[components.length];
                boolean hasValidComponent = false;
                for (int i = 0; i < components.length; i++) {
                    MaterialStack component = components[i];
                    if (component == null || component.amount <= 0L || component.material == null) {
                        continue;
                    }
                    hasValidComponent = true;
                    componentAmounts[i] = component.amount;
                    componentDensities[i] = getGt6MaterialDensityKgPerCubicMeter(component.material, visiting);
                }
                double compositionDensity = CrucibleTransferLogic.gt6MoleculeDensityKgPerCubicMeter(
                        componentAmounts, componentDensities);
                if (hasValidComponent) {
                    return compositionDensity;
                }
            }
            return getRegisteredOrDefaultDensity(material);
        } finally {
            visiting.remove(material);
        }
    }

    private double getRegisteredOrDefaultDensity(Material material) {
        if (material != null && material.hasFluid()) {
            Fluid fluid = material.getFluid();
            if (fluid != null && fluid.getDensity() > 0) {
                return fluid.getDensity();
            }
        }
        return DEFAULT_MATERIAL_DENSITY_KG_PER_CUBIC_METER;
    }

    private long getAmbientTemperature() {
        Biome biome = getWorld().getBiome(getPos());
        if (biome == null) {
            return DEFAULT_ENVIRONMENT_TEMPERATURE;
        }
        return Math.max(1L, 270L + (long) (biome.getTemperature(getPos()) * 20.0F));
    }

    @Nullable
    private Material getMaterialFromFluid(FluidStack stack) {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material == null || !material.hasFluid()) {
                continue;
            }
            FluidStack materialFluid = material.getFluid(1);
            if (materialFluid != null && materialFluid.isFluidEqual(stack)) {
                return material;
            }
        }
        return null;
    }

    private boolean processMaterialTemperatureState() {
        boolean changed = false;
        for (StoredMaterial material : contents) {
            if (!material.molten && temperature >= getMeltingTemperature(material.material)) {
                melt(material);
                changed = true;
            } else if (material.molten && temperature < getMeltingTemperature(material.material)) {
                solidify(material);
                changed = true;
            }
        }
        if (changed) {
            markDirty();
        }
        if (processHazards()) {
            return true;
        }
        return false;
    }

    private boolean processTemperature() {
        long ambientTemperature = getAmbientTemperature();
        boolean wasHeatedThisTick = heatedThisTick;
        heatedThisTick = false;
        boolean adjustTowardAmbient = CrucibleTransferLogic.shouldPassivelyAdjustTemperature(
                thermalCooldown, wasHeatedThisTick);
        thermalCooldown = CrucibleTransferLogic.nextThermalCooldown(thermalCooldown, wasHeatedThisTick,
                HEAT_COOLDOWN_TICKS, PASSIVE_COOLDOWN_TICKS);
        if (adjustTowardAmbient) {
            temperature = CrucibleTransferLogic.moveTemperatureTowardAmbient(temperature, ambientTemperature);
        }
        temperature = Math.max(temperature, Math.min(200L, ambientTemperature));
        if (temperature > maxTemperature) {
            meltDown();
            return true;
        }
        return false;
    }

    private void meltDown() {
        releaseOverheatEffects(temperature);
        contents.clear();
        specialFluid = null;
        specialFluidAmount = 0;
        lavaCondensationPending = false;
        temperature = getAmbientTemperature();
        refreshDisplayState();
        markDirty();
        getWorld().setBlockState(getPos(), Blocks.FLOWING_LAVA.getDefaultState(), 3);
    }

    private boolean processHazards() {
        boolean changed = false;
        Iterator<StoredMaterial> iterator = contents.iterator();
        while (iterator.hasNext()) {
            StoredMaterial material = iterator.next();
            if (material.material == null) {
                continue;
            }

            if (isLowDensityMaterial(material.material)) {
                iterator.remove();
                changed = true;
                continue;
            }

            if (shouldVaporize(material)) {
                long amount = material.amount;
                if (material.material.hasFlags(MaterialFlags.EXPLOSIVE)) {
                    explodeFromContent(amount);
                    return true;
                }
                iterator.remove();
                releaseHotGasEffects(temperature, amount);
                changed = true;
                continue;
            }

            if (!acidProof && isAcidMaterial(material.material)) {
                destroyByAcid();
                return true;
            }

        }
        if (changed) {
            markDirty();
            refreshDisplayState();
        }
        return false;
    }

    private void destroyByAcid() {
        contents.clear();
        temperature = getAmbientTemperature();
        refreshDisplayState();
        markDirty();
        getWorld().setBlockToAir(getPos());
    }

    private boolean shouldVaporize(StoredMaterial material) {
        return isGasLikeMaterial(material.material) ||
                (material.material.hasFlags(MaterialFlags.FLAMMABLE) && temperature > FLAMMABLE_TEMPERATURE);
    }

    private boolean isLowDensityMaterial(Material material) {
        return material == Materials.Air ||
                (material.hasFluid() && material.getFluid().getDensity() <= AIR_DENSITY_LIMIT);
    }

    private boolean isGasLikeMaterial(Material material) {
        if (!material.hasFluid()) {
            return false;
        }
        Fluid fluid = material.getFluid();
        if (fluid instanceof AttributedFluid) {
            FluidState state = ((AttributedFluid) fluid).getState();
            return state == FluidState.GAS || state == FluidState.PLASMA;
        }
        return false;
    }

    private boolean isAcidMaterial(Material material) {
        if (!material.hasFluid()) {
            return isAcidName(material.getName());
        }
        Fluid fluid = material.getFluid();
        if (fluid instanceof AttributedFluid &&
                ((AttributedFluid) fluid).getAttributes().contains(FluidAttributes.ACID)) {
            return true;
        }
        return isAcidName(material.getName()) || isAcidName(fluid.getName());
    }

    private boolean isAcidName(String name) {
        return name != null && name.toLowerCase(Locale.ROOT).contains("acid");
    }

    private void explodeFromContent(long amount) {
        contents.clear();
        refreshDisplayState();
        markDirty();
        float strength = Math.max(1.0F, Math.min(6.0F, amount * 6.0F / CAPACITY));
        getWorld().createExplosion(null, getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D,
                strength, true);
    }

    private void releaseHotGasEffects(long heat, long amount) {
        if (heat >= GAS_DAMAGE_TEMPERATURE) {
            AxisAlignedBB bounds = new AxisAlignedBB(getPos()).grow(FLAME_RANGE, 1.0D, FLAME_RANGE);
            float damage = Math.max(1.0F, Math.min(10.0F, heat / 400.0F));
            int fireSeconds = Math.max(1, Math.min(8, (int) (heat / 400L)));
            for (EntityLivingBase entity : getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
                entity.attackEntityFrom(DamageSource.ON_FIRE, damage);
                entity.setFire(fireSeconds);
            }
        }
        if (heat >= FIRE_TEMPERATURE) {
            placeHazardFire(amount);
        }
    }

    private void releaseOverheatEffects(long heat) {
        if (heat >= GAS_DAMAGE_TEMPERATURE) {
            AxisAlignedBB bounds = new AxisAlignedBB(
                    getPos().getX() - FLAME_RANGE, getPos().getY() - 1.0D, getPos().getZ() - FLAME_RANGE,
                    getPos().getX() + FLAME_RANGE + 1.0D, getPos().getY() + FLAME_RANGE + 1.0D,
                    getPos().getZ() + FLAME_RANGE + 1.0D);
            float damage = Math.max(1.0F, Math.min(10.0F, heat / 400.0F));
            int fireSeconds = Math.max(1, Math.min(8, (int) (heat / 400L)));
            for (EntityLivingBase entity : getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
                entity.attackEntityFrom(DamageSource.ON_FIRE, damage);
                entity.setFire(fireSeconds);
            }
        }
        int fireCount = (int) Math.min(256L, Math.max(1L, heat / 25L));
        for (int i = 0; i < fireCount; i++) {
            BlockPos firePos = getPos().add(
                    getWorld().rand.nextInt(FLAME_RANGE * 2 + 1) - FLAME_RANGE,
                    getWorld().rand.nextInt(FLAME_RANGE + 2) - 1,
                    getWorld().rand.nextInt(FLAME_RANGE * 2 + 1) - FLAME_RANGE);
            if (getWorld().isAirBlock(firePos) && Blocks.FIRE.canPlaceBlockAt(getWorld(), firePos)) {
                getWorld().setBlockState(firePos, Blocks.FIRE.getDefaultState(), 3);
            }
        }
    }

    private void placeHazardFire(long amount) {
        int fires = Math.max(1, Math.min(64, (int) Math.max(1L, amount * 9L / GTValues.M)));
        for (int i = 0; i < fires; i++) {
            BlockPos firePos = getPos().add(
                    getWorld().rand.nextInt(FLAME_RANGE * 2 + 1) - FLAME_RANGE,
                    getWorld().rand.nextInt(FLAME_RANGE + 2) - 1,
                    getWorld().rand.nextInt(FLAME_RANGE * 2 + 1) - FLAME_RANGE);
            if (getWorld().isAirBlock(firePos) && Blocks.FIRE.canPlaceBlockAt(getWorld(), firePos)) {
                getWorld().setBlockState(firePos, Blocks.FIRE.getDefaultState(), 3);
            }
        }
    }

    private void melt(StoredMaterial material) {
        if (material.solidifyingMaterial == null) {
            material.solidifyingMaterial = getSolidifyingTarget(material.material);
        }
        material.material = getSmeltingTarget(material.material);
        material.molten = true;
    }

    private void solidify(StoredMaterial material) {
        Material target = material.solidifyingMaterial;
        if (target == null || target == Materials.NULL) {
            target = getSolidifyingTarget(material.material);
        }
        if (target != null && target != Materials.NULL) {
            material.material = target;
        }
        material.solidifyingMaterial = null;
        material.molten = false;
    }

    private Material getSolidifyingTarget(Material moltenMaterial) {
        if (moltenMaterial == null || moltenMaterial == Materials.NULL) {
            return moltenMaterial;
        }
        if (moltenMaterial == Materials.Obsidian) {
            return Materials.Obsidian;
        }
        if (moltenMaterial == Materials.Lava) {
            return Materials.Obsidian;
        }

        Fluid moltenFluid = moltenMaterial.hasFluid() ? moltenMaterial.getFluid() : null;
        for (Material candidate : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (candidate == null || candidate == moltenMaterial || !candidate.hasProperty(PropertyKey.FLUID)) {
                continue;
            }
            FluidProperty fluidProperty = candidate.getProperty(PropertyKey.FLUID);
            if (moltenFluid != null && fluidProperty.solidifiesFrom() == moltenFluid) {
                return candidate;
            }
        }

        if (moltenMaterial.hasProperty(PropertyKey.INGOT) && getSmeltingTarget(moltenMaterial) == moltenMaterial) {
            return moltenMaterial;
        }

        for (Material candidate : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (candidate != null && candidate != moltenMaterial && candidate.hasProperty(PropertyKey.INGOT) &&
                    getSmeltingTarget(candidate) == moltenMaterial) {
                return candidate;
            }
        }
        return moltenMaterial;
    }

    private void processContactDamage() {
        if (temperature < GAS_DAMAGE_TEMPERATURE || getOffsetTimer() % CONTACT_DAMAGE_INTERVAL != 0) {
            return;
        }
        AxisAlignedBB bounds = new AxisAlignedBB(getPos()).grow(0.1D, 0.1D, 0.1D);
        float damage = Math.max(1.0F, Math.min(5.0F, temperature / 650.0F));
        int fireSeconds = Math.max(1, Math.min(5, (int) (temperature / 500L)));
        for (EntityLivingBase entity : getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
            entity.attackEntityFrom(DamageSource.ON_FIRE, damage);
            entity.setFire(fireSeconds);
        }
    }

    private void processAlloys() {
        boolean changed;
        int guard = 0;
        do {
            changed = false;
            AlloyMatch match = findBestAlloyMatch();
            if (match != null) {
                applyAlloy(match);
                changed = true;
            }
        } while (changed && ++guard < 64);
    }

    @Nullable
    private AlloyMatch findBestAlloyMatch() {
        Collection<Material> materials = GregTechAPI.materialManager.getRegisteredMaterials();
        AlloyMatch bestMatch = null;
        for (Material alloy : materials) {
            if (alloy == null || !alloy.hasFluid() || temperature < getMeltingTemperature(alloy)) {
                continue;
            }

            List<MaterialStack> components = alloy.getMaterialComponents();
            if (components == null || components.size() < 2) {
                continue;
            }

            long conversions = Long.MAX_VALUE;
            long outputUnits = 0;
            int nonMoltenComponents = 0;
            boolean valid = true;
            for (MaterialStack component : components) {
                Material componentMaterial = getSmeltingTarget(component.material);
                long componentAmount = Math.max(1L, component.amount);
                long available = getStoredAmount(componentMaterial);
                if (available < componentAmount) {
                    valid = false;
                    break;
                }
                if (getSolidAmount(componentMaterial) > 0) {
                    nonMoltenComponents++;
                }
                conversions = Math.min(conversions, available / componentAmount);
                outputUnits += componentAmount;
            }

            if (!valid || conversions <= 0 || outputUnits <= 0 || nonMoltenComponents > 1) {
                continue;
            }
            if (bestMatch == null || conversions * outputUnits > bestMatch.conversions * bestMatch.outputUnits) {
                bestMatch = new AlloyMatch(alloy, components, conversions, outputUnits);
            }
        }
        return bestMatch;
    }

    private void applyAlloy(AlloyMatch match) {
        for (MaterialStack component : match.components) {
            Material material = getSmeltingTarget(component.material);
            long amount = Math.max(1L, component.amount) * match.conversions;
            removeMaterial(material, amount);
        }
        boolean molten = temperature >= getMeltingTemperature(match.alloy);
        addStoredMaterial(new StoredMaterial(match.alloy, match.outputUnits * match.conversions, molten,
                molten ? getSolidifyingTarget(match.alloy) : match.alloy));
        removeEmptyContents();
        markDirty();
    }

    private long getStoredAmount(Material material) {
        long amount = 0;
        for (StoredMaterial storedMaterial : contents) {
            if (storedMaterial.material == material) {
                amount += storedMaterial.amount;
            }
        }
        return amount;
    }

    private long getSolidAmount(Material material) {
        long amount = 0;
        for (StoredMaterial storedMaterial : contents) {
            if (!storedMaterial.molten && storedMaterial.material == material) {
                amount += storedMaterial.amount;
            }
        }
        return amount;
    }

    private void removeMaterial(Material material, long amount) {
        for (StoredMaterial storedMaterial : contents) {
            if (storedMaterial.material != material) {
                continue;
            }
            long removed = Math.min(amount, storedMaterial.amount);
            storedMaterial.amount -= removed;
            amount -= removed;
            if (amount <= 0) {
                return;
            }
        }
    }

    private void addStoredMaterial(StoredMaterial material) {
        for (StoredMaterial storedMaterial : contents) {
            if (storedMaterial.material == material.material && storedMaterial.molten == material.molten &&
                    storedMaterial.solidifyingMaterial == material.solidifyingMaterial) {
                storedMaterial.amount += material.amount;
                return;
            }
        }
        contents.add(material);
    }

    private void removeEmptyContents() {
        Iterator<StoredMaterial> iterator = contents.iterator();
        while (iterator.hasNext()) {
            StoredMaterial material = iterator.next();
            if (material.amount <= 0 || material.material == null || material.material == gregtech.api.unification.material.Materials.NULL) {
                iterator.remove();
            }
        }
    }

    private long getTotalAmount() {
        long total = 0;
        for (StoredMaterial material : contents) {
            total += material.amount;
        }
        return total;
    }

    private Material getSmeltingTarget(Material material) {
        if (material == Materials.Obsidian) {
            return Materials.Lava;
        }
        if (material.hasProperty(PropertyKey.INGOT)) {
            IngotProperty property = material.getProperty(PropertyKey.INGOT);
            if (property.getSmeltingInto() != null) {
                return property.getSmeltingInto();
            }
        }
        return material;
    }

    private int getMeltingTemperature(Material material) {
        if (material == Materials.Obsidian) {
            return CrucibleTransferLogic.OBSIDIAN_MELTING_TEMPERATURE;
        }
        if (material.hasFluid()) {
            return material.getFluid().getTemperature();
        }
        int blastTemperature = material.getBlastTemperature();
        return blastTemperature > 0 ? blastTemperature : 1811;
    }

    private static int getDefaultMaxTemperature(int tier) {
        return 1800 + Math.max(0, tier) * 300;
    }

    private StoredMaterial getDrainableMaterial(@Nullable FluidStack requestedFluid) {
        for (StoredMaterial material : contents) {
            if (!material.molten || material.amount <= 0 || !material.material.hasFluid()) {
                continue;
            }
            if (temperature < getMeltingTemperature(material.material)) {
                continue;
            }
            FluidStack fluidStack = material.material.getFluid(1);
            if (fluidStack == null) {
                continue;
            }
            if (requestedFluid == null || requestedFluid.isFluidEqual(fluidStack)) {
                return material;
            }
        }
        return null;
    }

    public long fillMoldAtSide(ICrucibleMold mold, @Nullable EnumFacing sideOfCrucible,
                               @Nullable EnumFacing sideOfMold) {
        if (mold == null || !mold.isMoldInputSide(sideOfMold)) {
            return 0L;
        }
        for (StoredMaterial material : contents) {
            if (!isPourableMaterial(material)) {
                continue;
            }
            long amountToTry = material.amount;
            long requiredAmount = mold.getMoldRequiredMaterialUnits(material.material);
            if (requiredAmount > 0L) {
                amountToTry = Math.min(amountToTry, requiredAmount);
            }
            if (amountToTry <= 0L) {
                continue;
            }
            long accepted = mold.fillMold(material.material, amountToTry, temperature, sideOfMold, true);
            if (accepted <= 0L) {
                continue;
            }
            long filled = mold.fillMold(material.material, Math.min(material.amount, accepted),
                    temperature, sideOfMold, false);
            if (filled <= 0L) {
                continue;
            }
            material.amount -= Math.min(material.amount, filled);
            removeEmptyContents();
            markDirty();
            refreshDisplayState();
            return filled;
        }
        return 0L;
    }

    private boolean isPourableMaterial(StoredMaterial material) {
        return material != null && material.material != null && material.amount > 0L &&
                material.molten && temperature >= getMeltingTemperature(material.material);
    }

    private void refreshDisplayState() {
        calculateDisplayState();
        if (getWorld() != null && !getWorld().isRemote && isDisplayStateChanged()) {
            writeCustomData(DATA_DISPLAY_STATE, this::writeDisplayState);
            rememberDisplayState();
            scheduleRenderUpdate();
        }
    }

    private void calculateDisplayState() {
        long total = getTotalAmount();
        displayHeight = total <= 0 ? 0 : (int) Math.min(255L, total * 255L / CAPACITY);

        StoredMaterial displayMaterial = getDisplayedMaterial();
        if (displayMaterial == null) {
            displayMaterialName = EMPTY_DISPLAY_MATERIAL;
            displayMolten = false;
        } else {
            displayMaterialName = getMaterialRegistryName(displayMaterial.material);
            displayMolten = displayMaterial.molten && temperature >= getMeltingTemperature(displayMaterial.material);
        }
        meltDownWarning = temperature + 100L > maxTemperature;
        displaySpecialFluidName = specialFluid == null ? "" : specialFluid.getName();
        displaySpecialFluidAmount = specialFluidAmount;
        displayPendingItems = pendingItemCount();
    }

    private boolean isDisplayStateChanged() {
        return displayHeight != oldDisplayHeight ||
                displayMolten != oldDisplayMolten ||
                meltDownWarning != oldMeltDownWarning ||
                displaySpecialFluidAmount != oldDisplaySpecialFluidAmount ||
                displayPendingItems != oldDisplayPendingItems ||
                !displaySpecialFluidName.equals(oldDisplaySpecialFluidName) ||
                !displayMaterialName.equals(oldDisplayMaterialName);
    }

    private void rememberDisplayState() {
        oldDisplayHeight = displayHeight;
        oldDisplayMaterialName = displayMaterialName;
        oldDisplayMolten = displayMolten;
        oldMeltDownWarning = meltDownWarning;
        oldDisplaySpecialFluidName = displaySpecialFluidName;
        oldDisplaySpecialFluidAmount = displaySpecialFluidAmount;
        oldDisplayPendingItems = displayPendingItems;
    }

    @Nullable
    private StoredMaterial getDisplayedMaterial() {
        StoredMaterial lightest = null;
        for (StoredMaterial material : contents) {
            if (material.amount <= 0) {
                continue;
            }
            if (lightest == null || getMaterialDensity(material.material) < getMaterialDensity(lightest.material)) {
                lightest = material;
            }
        }
        return lightest;
    }

    @Nullable
    private Material getDisplayedClientMaterial() {
        return resolveMaterial(displayMaterialName);
    }

    private static String getMaterialRegistryName(Material material) {
        return material == null ? EMPTY_DISPLAY_MATERIAL : material.getRegistryName();
    }

    @Nullable
    private static Material resolveMaterial(@Nullable String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return null;
        }

        Material material = GregTechAPI.materialManager.getMaterial(materialName);
        if (material != null) {
            return material;
        }

        // Older saves stored only Material#getName(), which loses the registry
        // namespace for materials supplied by other mods.
        for (Material registeredMaterial : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (registeredMaterial != null && materialName.equals(registeredMaterial.getName())) {
                return registeredMaterial;
            }
        }
        return null;
    }

    public long getCurrentTemperature() {
        return temperature;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    @Override
    public long getTemperatureValue(@Nullable EnumFacing side) {
        return getCurrentTemperature();
    }

    @Override
    public long getTemperatureMax(@Nullable EnumFacing side) {
        return getMaxTemperature();
    }

    @Override
    public float getBlockHardness() {
        return blockHardness;
    }

    @Override
    public float getBlockResistance() {
        return blockResistance;
    }

    public int getStoredFluidAmount() {
        return toFluidAmount(getTotalAmount());
    }

    public int getCapacityFluidAmount() {
        return toFluidAmount(CAPACITY);
    }

    @NotNull
    public List<CrucibleContentInfo> getTopContents() {
        List<CrucibleContentInfo> result = new ArrayList<>();
        for (StoredMaterial material : contents) {
            result.add(new CrucibleContentInfo(
                    getDisplayStack(material),
                    material.material.getLocalizedName(),
                    toFluidAmount(material.amount),
                    material.molten));
        }
        return result;
    }

    private ItemStack getDisplayStack(StoredMaterial material) {
        ItemStack stack = OreDictUnifier.getIngotOrDust(material.material, Math.max(GTValues.M, material.amount));
        if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount((int) Math.min(64L, Math.max(1L, (material.amount + GTValues.M - 1L) / GTValues.M)));
        }
        return stack;
    }

    private int toFluidAmount(long materialAmount) {
        return (int) Math.min(Integer.MAX_VALUE, materialAmount * GTValues.L / GTValues.M);
    }

    private long toMaterialAmount(int fluidAmount) {
        return Math.max(1L, (fluidAmount * GTValues.M + GTValues.L - 1L) / GTValues.L);
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            return true;
        }

        ItemStack heldItem = player.getHeldItem(hand);
        if (facing == EnumFacing.UP && tryScrapeSolidContent(player, heldItem)) {
            return true;
        }
        if (!heldItem.isEmpty()) {
            FluidActionResult emptyResult = FluidUtil.tryEmptyContainer(heldItem, fluidHandler,
                    Integer.MAX_VALUE, player, true);
            if (emptyResult.isSuccess()) {
                player.setHeldItem(hand, emptyResult.getResult());
                return true;
            }
            FluidActionResult fillResult = FluidUtil.tryFillContainer(heldItem, fluidHandler,
                    Integer.MAX_VALUE, player, true);
            if (fillResult.isSuccess()) {
                player.setHeldItem(hand, fillResult.getResult());
                return true;
            }

            ItemStack oneItem = heldItem.copy();
            oneItem.setCount(1);
            if (addMaterialFromItem(oneItem)) {
                heldItem.shrink(1);
                return true;
            }
            if (enqueuePendingItem(oneItem)) {
                heldItem.shrink(1);
                return true;
            }
        } else {
            player.sendStatusMessage(new TextComponentString("Temperature: " + temperature + "/" + maxTemperature + "K, Content: " + getDisplayContent()), true);
            return true;
        }
        return super.onRightClick(player, hand, facing, hitResult);
    }

    private boolean tryScrapeSolidContent(EntityPlayer player, ItemStack heldItem) {
        boolean emptyHandScrape = heldItem.isEmpty();
        boolean shovelScrape = !heldItem.isEmpty() && heldItem.getItem().getToolClasses(heldItem).contains("shovel");
        if (!emptyHandScrape && !shovelScrape) {
            return false;
        }

        StoredMaterial material = getScrapableMaterial();
        if (material == null) {
            player.sendStatusMessage(new TextComponentString("No solid content to scrape."), true);
            return true;
        }

        ScrapeResult result = createScrapeResult(material);
        if (result == null) {
            material.amount = 0;
            removeEmptyContents();
            markDirty();
            refreshDisplayState();
            player.sendStatusMessage(new TextComponentString("Solid residue is too small to recover."), true);
            applyContactHeat(player);
            return true;
        }

        material.amount -= result.materialAmount;
        removeEmptyContents();
        markDirty();
        refreshDisplayState();
        ItemHandlerHelper.giveItemToPlayer(player, result.stack);
        if (shovelScrape && !player.capabilities.isCreativeMode) {
            heldItem.damageItem(1, player);
        }
        applyContactHeat(player);
        return true;
    }

    @Nullable
    private StoredMaterial getScrapableMaterial() {
        StoredMaterial lightest = null;
        for (StoredMaterial material : contents) {
            if (material.amount <= 0 || material.molten || temperature >= getMeltingTemperature(material.material)) {
                continue;
            }
            if (lightest == null || getMaterialDensity(material.material) < getMaterialDensity(lightest.material)) {
                lightest = material;
            }
        }
        return lightest;
    }

    @Nullable
    private ScrapeResult createScrapeResult(StoredMaterial material) {
        if (material.material == Materials.Obsidian) {
            int blocks = (int) Math.min(64L, material.amount / GTValues.M);
            return blocks <= 0 ? null : new ScrapeResult(new ItemStack(Blocks.OBSIDIAN, blocks),
                    blocks * (long) GTValues.M);
        }
        if (material.amount < SCRAP_MATERIAL_AMOUNT) {
            return null;
        }
        int count = (int) Math.min(GT6AdditionOrePrefixes.SCRAP_GT.maxStackSize,
                material.amount / SCRAP_MATERIAL_AMOUNT);
        ItemStack output = GT6AdditionOrePrefixes.SCRAP_GT.getItemForm(material.material, count);
        return output.isEmpty() ? null : new ScrapeResult(output, SCRAP_MATERIAL_AMOUNT * count);
    }

    private double getMaterialDensity(Material material) {
        if (material.hasFluid()) {
            return Math.abs((double) material.getFluid().getDensity());
        }
        return Math.max(1.0D, material.getMass());
    }

    private void applyContactHeat(EntityPlayer player) {
        if (temperature <= GAS_DAMAGE_TEMPERATURE) {
            return;
        }
        float damage = Math.max(1.0F, Math.min(5.0F, temperature / 650.0F));
        player.attackEntityFrom(DamageSource.ON_FIRE, damage);
        player.setFire(Math.max(1, Math.min(5, (int) (temperature / 500L))));
    }

    private String getDisplayContent() {
        StringBuilder builder = new StringBuilder();
        for (StoredMaterial material : contents) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(material.material.getLocalizedName())
                    .append(material.molten ? "(molten)" : "(solid)")
                    .append(' ')
                    .append(toFluidAmount(material.amount))
                    .append("L");
        }
        if (specialFluid != null && specialFluidAmount > 0) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(specialFluid.getLocalizedName(new FluidStack(specialFluid, specialFluidAmount)))
                    .append(' ').append(specialFluidAmount).append("mB");
        }
        if (pendingItemCount() > 0) {
            if (builder.length() > 0) builder.append(", ");
            builder.append("queued ").append(pendingItemCount()).append(" items");
        }
        return builder.length() == 0 ? "empty" : builder.toString();
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return false;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != EnumFacing.UP) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return null;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != EnumFacing.UP) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean acceptsCovers() {
        return false;
    }

    @Override
    public boolean canPlaceCoverOnSide(EnumFacing side) {
        return false;
    }

    @Override
    public boolean shouldDropWhenDestroyed() {
        return temperature < HOT_BREAK_TEMPERATURE && super.shouldDropWhenDestroyed();
    }

    @Override
    public void onRemoval() {
        if (getWorld() != null && !getWorld().isRemote && temperature >= HOT_BREAK_TEMPERATURE) {
            releaseOverheatEffects(temperature);
            contents.clear();
            temperature = getAmbientTemperature();
            getWorld().setBlockState(getPos(), Blocks.FLOWING_LAVA.getDefaultState(), 3);
        }
        super.onRemoval();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] shellPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getShellRenderColor())));
        getBaseRenderer().render(renderState, translation, shellPipeline, WALL_X_NEG);
        getBaseRenderer().render(renderState, translation, shellPipeline, WALL_Z_NEG);
        getBaseRenderer().render(renderState, translation, shellPipeline, WALL_X_POS);
        getBaseRenderer().render(renderState, translation, shellPipeline, WALL_Z_POS);
        getBaseRenderer().render(renderState, translation, shellPipeline, BOTTOM);
        renderDisplayedContent(renderState, translation, pipeline);
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
        // Keep the open crucible model, while allowing interaction anywhere in its block space.
        super.addCollisionBoundingBox(collisionList);
    }

    @SideOnly(Side.CLIENT)
    private void renderDisplayedContent(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (displayHeight <= 0) {
            return;
        }
        Material material = getDisplayedClientMaterial();
        if (material == null) {
            return;
        }
        double top = WALL_SIZE + displayHeight / CONTENT_HEIGHT_SCALE;
        Cuboid6 contentBounds = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, top, 1.0D);
        IVertexOperation[] contentPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getContentRenderColor(material))));
        Textures.renderFace(renderState, translation, contentPipeline, EnumFacing.UP, contentBounds,
                getContentSprite(material), BlockRenderLayer.CUTOUT_MIPPED);
    }

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite getContentSprite(Material material) {
        if (displayMolten && material.hasFluid()) {
            FluidStack fluidStack = material.getFluid(1);
            if (fluidStack != null) {
                Fluid fluid = fluidStack.getFluid();
                ResourceLocation still = fluid.getStill(fluidStack);
                if (still != null) {
                    return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(still.toString());
                }
            }
        }
        String texture = material == Materials.Obsidian ? "minecraft:blocks/obsidian" : "minecraft:blocks/gravel";
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture);
    }

    private int getContentRenderColor(Material material) {
        if (displayMolten && material.hasFluid()) {
            FluidStack fluidStack = material.getFluid(1);
            if (fluidStack != null) {
                int fluidColor = fluidStack.getFluid().getColor(fluidStack) & 0xFFFFFF;
                if (fluidColor != 0xFFFFFF) {
                    return fluidColor;
                }
            }
        }
        return material.getMaterialRGB() & 0xFFFFFF;
    }

    private int getShellRenderColor() {
        if (!meltDownWarning) {
            return color;
        }
        int red = clampColor(((color >> 16) & 0xFF) * 2 + 50);
        int green = clampColor(((color >> 8) & 0xFF) + 35);
        int blue = clampColor((color & 0xFF) / 2);
        return (red << 16) | (green << 8) | blue;
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.1"));
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.2", CAPACITY / GTValues.M));
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.3", maxTemperature));
        if (acidProof) {
            tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.acid_proof"));
        }
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.scrape"));
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.hazard"));
        tooltip.add(I18n.format("gt6addition.accept_facing", I18n.format("gt6addition.down")));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong(NBT_TEMPERATURE, temperature);
        data.setLong(NBT_OLD_TEMPERATURE, oldTemperature);
        data.setLong(NBT_STORED_HEAT, storedHeat);
        data.setInteger(NBT_SPECIAL_FLUID_AMOUNT, specialFluidAmount);
        data.setBoolean(NBT_LAVA_CONDENSATION_PENDING, lavaCondensationPending);
        if (specialFluid != null && specialFluidAmount > 0) {
            data.setString(NBT_SPECIAL_FLUID, specialFluid.getName());
        } else {
            data.removeTag(NBT_SPECIAL_FLUID);
        }
        NBTTagList contentList = new NBTTagList();
        for (StoredMaterial material : contents) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(NBT_MATERIAL, getMaterialRegistryName(material.material));
            tag.setLong(NBT_AMOUNT, material.amount);
            tag.setBoolean(NBT_MOLTEN, material.molten);
            if (material.solidifyingMaterial != null && material.solidifyingMaterial != Materials.NULL) {
                tag.setString(NBT_SOLIDIFY_TARGET, getMaterialRegistryName(material.solidifyingMaterial));
            }
            contentList.appendTag(tag);
        }
        data.setTag(NBT_CONTENTS, contentList);
        NBTTagList pendingList = new NBTTagList();
        for (ItemStack stack : pendingItems) {
            if (!stack.isEmpty()) pendingList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        data.setTag(NBT_PENDING_ITEMS, pendingList);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        temperature = data.hasKey(NBT_TEMPERATURE) ? data.getLong(NBT_TEMPERATURE) : DEFAULT_ENVIRONMENT_TEMPERATURE;
        oldTemperature = data.hasKey(NBT_OLD_TEMPERATURE) ? data.getLong(NBT_OLD_TEMPERATURE) : temperature;
        storedHeat = Math.max(0L, data.getLong(NBT_STORED_HEAT));
        thermalCooldown = HEAT_COOLDOWN_TICKS;
        contents.clear();
        pendingItems.clear();
        int pendingCount = 0;
        NBTTagList pendingList = data.getTagList(NBT_PENDING_ITEMS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < pendingList.tagCount() && pendingCount < PENDING_ITEM_CAPACITY; i++) {
            ItemStack stack = new ItemStack(pendingList.getCompoundTagAt(i));
            if (stack.isEmpty()) continue;
            int acceptedCount = Math.min(stack.getCount(), PENDING_ITEM_CAPACITY - pendingCount);
            stack.setCount(acceptedCount);
            pendingItems.add(stack);
            pendingCount += acceptedCount;
        }
        specialFluidAmount = Math.max(0, Math.min(SPECIAL_FLUID_CAPACITY,
                data.getInteger(NBT_SPECIAL_FLUID_AMOUNT)));
        specialFluid = data.hasKey(NBT_SPECIAL_FLUID) ? FluidRegistry.getFluid(data.getString(NBT_SPECIAL_FLUID)) : null;
        lavaCondensationPending = data.getBoolean(NBT_LAVA_CONDENSATION_PENDING);
        if (specialFluid == null) {
            specialFluidAmount = 0;
            lavaCondensationPending = false;
        }
        NBTTagList contentList = data.getTagList(NBT_CONTENTS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < contentList.tagCount(); i++) {
            NBTTagCompound tag = contentList.getCompoundTagAt(i);
            Material material = resolveMaterial(tag.getString(NBT_MATERIAL));
            if (material != null && material != gregtech.api.unification.material.Materials.NULL) {
                boolean molten = tag.getBoolean(NBT_MOLTEN);
                Material solidifyingMaterial = tag.hasKey(NBT_SOLIDIFY_TARGET) ?
                        resolveMaterial(tag.getString(NBT_SOLIDIFY_TARGET)) :
                        (molten ? getSolidifyingTarget(material) : material);
                if (solidifyingMaterial == Materials.NULL) {
                    solidifyingMaterial = material;
                }
                contents.add(new StoredMaterial(material, tag.getLong(NBT_AMOUNT), molten, solidifyingMaterial));
            }
        }
        removeEmptyContents();
        calculateDisplayState();
        rememberDisplayState();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        calculateDisplayState();
        rememberDisplayState();
        buf.writeBoolean(active);
        writeDisplayState(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        active = buf.readBoolean();
        readDisplayState(buf);
        rememberDisplayState();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            active = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == DATA_DISPLAY_STATE) {
            readDisplayState(buf);
            rememberDisplayState();
            scheduleRenderUpdate();
        }
    }

    private void writeDisplayState(PacketBuffer buf) {
        buf.writeVarInt(displayHeight);
        buf.writeString(displayMaterialName);
        buf.writeBoolean(displayMolten);
        buf.writeBoolean(meltDownWarning);
        buf.writeString(displaySpecialFluidName);
        buf.writeVarInt(displaySpecialFluidAmount);
        buf.writeVarInt(displayPendingItems);
    }

    private void readDisplayState(PacketBuffer buf) {
        displayHeight = buf.readVarInt();
        displayMaterialName = buf.readString(Short.MAX_VALUE);
        displayMolten = buf.readBoolean();
        meltDownWarning = buf.readBoolean();
        displaySpecialFluidName = buf.readString(Short.MAX_VALUE);
        displaySpecialFluidAmount = buf.readVarInt();
        displayPendingItems = buf.readVarInt();
    }

    public static class CrucibleContentInfo {
        private final ItemStack displayStack;
        private final String materialName;
        private final int fluidAmount;
        private final boolean molten;

        private CrucibleContentInfo(ItemStack displayStack, String materialName, int fluidAmount, boolean molten) {
            this.displayStack = displayStack.copy();
            this.materialName = materialName;
            this.fluidAmount = fluidAmount;
            this.molten = molten;
        }

        public ItemStack getDisplayStack() {
            return displayStack.copy();
        }

        public String getMaterialName() {
            return materialName;
        }

        public int getFluidAmount() {
            return fluidAmount;
        }

        public boolean isMolten() {
            return molten;
        }
    }

    private static class StoredMaterial {
        private Material material;
        private long amount;
        private boolean molten;
        @Nullable
        private Material solidifyingMaterial;

        private StoredMaterial(Material material, long amount, boolean molten) {
            this(material, amount, molten, molten ? null : material);
        }

        private StoredMaterial(Material material, long amount, boolean molten,
                               @Nullable Material solidifyingMaterial) {
            this.material = material;
            this.amount = amount;
            this.molten = molten;
            this.solidifyingMaterial = solidifyingMaterial;
        }
    }

    private static class AlloyMatch {
        private final Material alloy;
        private final List<MaterialStack> components;
        private final long conversions;
        private final long outputUnits;

        private AlloyMatch(Material alloy, List<MaterialStack> components, long conversions, long outputUnits) {
            this.alloy = alloy;
            this.components = components;
            this.conversions = conversions;
            this.outputUnits = outputUnits;
        }
    }

    private static class ScrapeResult {
        private final ItemStack stack;
        private final long materialAmount;

        private ScrapeResult(ItemStack stack, long materialAmount) {
            this.stack = stack;
            this.materialAmount = materialAmount;
        }
    }

    private class CrucibleFluidHandler implements IFluidHandler {

        @Override
        public IFluidTankProperties[] getTankProperties() {
            StoredMaterial material = getDrainableMaterial(null);
            FluidStack content = material == null ? null : material.material.getFluid(toFluidAmount(material.amount));
            FluidStack specialContent = specialFluid == null || specialFluidAmount <= 0 ? null :
                    new FluidStack(specialFluid, specialFluidAmount);
            return new IFluidTankProperties[]{
                    new FluidTankProperties(content, toFluidAmount(CAPACITY), true, true),
                    new FluidTankProperties(specialContent, SPECIAL_FLUID_CAPACITY, true, true)
            };
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return addMaterialFromFluid(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) {
                return null;
            }
            if (getSpecialFluidKind(resource.getFluid()) != null && resource.getFluid() == specialFluid) {
                return drainSpecial(resource.amount, doDrain);
            }
            StoredMaterial material = getDrainableMaterial(resource);
            if (material == null) {
                return null;
            }
            return drainMaterial(material, resource.amount, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0) {
                return null;
            }
            StoredMaterial material = getDrainableMaterial(null);
            return material == null ? drainSpecial(maxDrain, doDrain) : drainMaterial(material, maxDrain, doDrain);
        }

        @Nullable
        private FluidStack drainSpecial(int maxDrain, boolean doDrain) {
            if (specialFluid == null || specialFluidAmount <= 0 || maxDrain <= 0) return null;
            int drained = Math.min(maxDrain, specialFluidAmount);
            FluidStack result = new FluidStack(specialFluid, drained);
            if (doDrain) {
                specialFluidAmount -= drained;
                if (specialFluidAmount <= 0) {
                    specialFluidAmount = 0;
                    specialFluid = null;
                    lavaCondensationPending = false;
                }
                markDirty();
                refreshDisplayState();
            }
            return result;
        }

        private FluidStack drainMaterial(StoredMaterial material, int maxDrain, boolean doDrain) {
            int availableFluid = toFluidAmount(material.amount);
            int drainedFluid = Math.min(maxDrain, availableFluid);
            if (drainedFluid <= 0) {
                return null;
            }
            FluidStack result = material.material.getFluid(drainedFluid);
            if (doDrain) {
                material.amount -= Math.min(material.amount, toMaterialAmount(drainedFluid));
                removeEmptyContents();
                markDirty();
                refreshDisplayState();
            }
            return result;
        }
    }
}
