package com.drppp.gt6addition.common.metatileentity.single.hu;

import com.drppp.gt6addition.common.material.GT6MaterialCompatibility;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.interfaces.IColdEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IHeatEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IKineticEnergy;
import com.drppp.gt6addition.api.capability.interfaces.ICrucibleEnergyReceiver;
import com.drppp.gt6addition.api.baseMTile.TieredMutiEnergyMetaTileEntity;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import com.drppp.gt6addition.client.CrucibleContentRenderer;
import com.drppp.gt6addition.common.material.GT6AdditionOrePrefixes;
import com.drppp.gt6addition.api.temperature.ITemperatureProvider;
import com.drppp.gt6addition.api.utils.EnergyTypeList;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IHeatable;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.HeatContainerHandler;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.material.properties.IngotProperty;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.RecyclingData;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Hazard;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;
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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class MetaTileEntityCrucible extends TieredMutiEnergyMetaTileEntity
        implements ITemperatureProvider, ICrucibleEnergyReceiver, ICrucibleMold {

    private static final String NBT_TEMPERATURE = "Temperature";
    private static final String NBT_OLD_TEMPERATURE = "OldTemperature";
    private static final String NBT_CONTENTS = "Contents";
    private static final String NBT_MATERIAL = "Material";
    private static final String NBT_AMOUNT = "Amount";
    private static final String NBT_MOLTEN = "Molten";
    private static final String NBT_SOLIDIFY_TARGET = "SolidifyTarget";
    private static final String NBT_PENDING_ITEMS = "PendingItems";
    private static final String NBT_WATER_UNIT_VERSION = "WaterUnitVersion";
    private static final String NBT_FLUID_REMAINDER = "FluidRemainder";
    private static final String NBT_FLUID_QUANTITY_VERSION = "FluidQuantityVersion";
    // Legacy keys retained only so existing worlds can migrate the old independent fluid slot.
    private static final String NBT_SPECIAL_FLUID = "SpecialFluid";
    private static final String NBT_SPECIAL_FLUID_AMOUNT = "SpecialFluidAmount";
    private static final String NBT_STORED_HEAT = "StoredHeat";
    private static final int PENDING_ITEM_CAPACITY = 64;
    private static final int LAVA_OBSIDIAN_MILLIBUCKETS = 1_000;
    private static final int DATA_DISPLAY_STATE = 200;
    private static final long CAPACITY = 16L * GTValues.M;

    public static long getMaterialCapacity() { return CAPACITY; }
    private static final long DEFAULT_ENVIRONMENT_TEMPERATURE = 293L;
    private static final int HEAT_COOLDOWN_TICKS = 100;
    private static final int PASSIVE_COOLDOWN_TICKS = 10;
    // This is the single-block GT6 Smeltery, not the 3x3x3 Crucible.
    private static final long GT6_VESSEL_MATERIAL_AMOUNT = 7L * GTValues.M;
    // Preserve GT6's 1 g/cm^3 baseline for a missing vessel material. Unknown registered
    // materials use a slightly heavier fallback until they receive explicit density data.
    private static final double GT6_DEFAULT_MATERIAL_DENSITY_KG_PER_CUBIC_METER = 1_000.0D;
    private static final long SCRAP_MATERIAL_AMOUNT = GTValues.M / 9L;
    private static final int RAIN_FILL_INTERVAL = 600;
    private static final int RAIN_FILL_OFFSET = 10;
    private static final int FLAME_RANGE = 3;
    private static final int HOT_BREAK_TEMPERATURE = 1300;
    private static final int FIRE_TEMPERATURE = 2000;
    private static final double WALL_SIZE = 0.125D;
    private static final double CONTENT_HEIGHT_SCALE = 292.571428D;
    private static final Cuboid6 WALL_X_NEG = new Cuboid6(0.0D, 0.0D, 0.0D, WALL_SIZE, 1.0D, 1.0D);
    private static final Cuboid6 WALL_Z_NEG = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, WALL_SIZE);
    private static final Cuboid6 WALL_X_POS = new Cuboid6(1.0D - WALL_SIZE, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final Cuboid6 WALL_Z_POS = new Cuboid6(0.0D, 0.0D, 1.0D - WALL_SIZE, 1.0D, 1.0D, 1.0D);
    private static final Cuboid6 BOTTOM = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, WALL_SIZE, 1.0D);
    private static final Cuboid6[] SHELL_PARTS = {WALL_X_NEG, WALL_Z_NEG, WALL_X_POS, WALL_Z_POS, BOTTOM};
    private static final String EMPTY_DISPLAY_MATERIAL = "";

    private final int color;
    private final int maxTemperature;
    @Nullable
    private final Material vesselMaterial;
    private final boolean acidProof;
    private final float blockHardness;
    private final float blockResistance;
    private final CrucibleFluidHandler fluidHandler = new CrucibleFluidHandler();
    private final CrucibleNativeHeatReceiver[] nativeHeatReceivers = new CrucibleNativeHeatReceiver[7];
    private final List<StoredMaterial> contents = new ArrayList<>();
    private final List<ItemStack> pendingItems = new ArrayList<>();
    private long temperature = DEFAULT_ENVIRONMENT_TEMPERATURE;
    private long oldTemperature = DEFAULT_ENVIRONMENT_TEMPERATURE;
    private long storedHeat;
    private int thermalCooldown = HEAT_COOLDOWN_TICKS;
    private boolean heatedThisTick;
    private boolean energyReceivedThisTick;
    // CEu can query machine drops after onRemoval has reset the temperature.
    // Keep destruction state on this discarded instance, independently of heat.
    private boolean destroyedByHeat;
    // Only valid during the external player's actual block-removal call. This
    // is instance-local, never saved, and restored even if removal throws.
    private boolean creativePlayerRemoval;
    // Intake can occur between update calls (player/fluid capability). Consume
    // this event in the phase pass, rather than discarding it at tick start.
    private boolean newContentForPhaseTargets;
    private boolean active;
    private int displayHeight;
    private int oldDisplayHeight = -1;
    private String displayMaterialName = EMPTY_DISPLAY_MATERIAL;
    private String oldDisplayMaterialName = null;
    private boolean displayMolten;
    private boolean oldDisplayMolten;
    private boolean meltDownWarning;
    private boolean oldMeltDownWarning;
    private int displayPendingItems;
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
        super(metaTileEntityId, tier, EnergyTypeList.HU, MachineEnergyAcceptFacing.values());
        this.color = color;
        this.maxTemperature = Math.max((int) DEFAULT_ENVIRONMENT_TEMPERATURE, maxTemperature);
        this.vesselMaterial = vesselMaterial;
        this.acidProof = acidProof;
        this.blockHardness = blockHardness;
        this.blockResistance = blockResistance;
        for (int i = 0; i < nativeHeatReceivers.length; i++) {
            nativeHeatReceivers[i] = new CrucibleNativeHeatReceiver(
                    () -> getWorld() == null ? 0L : getWorld().getTotalWorldTime(),
                    () -> Math.min(Integer.MAX_VALUE, GTValues.V[getTier()] * 2L),
                    () -> storedHeat, () -> temperature, () -> this.maxTemperature,
                    heat -> receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, heat, true),
                    heat -> receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, heat, false));
        }
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

        pullEnergyFromNeighbors();
        processRainFill();
        CrucibleItemIntake.Tick intake = new CrucibleItemIntake.Tick();
        processPendingItemQueue(intake);
        processInputSlot(intake);
        processDroppedItems(intake);
        processEntityContacts();
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
        refreshDisplayState();
        setActive(energyReceivedThisTick || temperature != oldTemperature);
        energyReceivedThisTick = false;
    }

    private void pullEnergyFromNeighbors() {
        heatedThisTick = false;
        int maxInputPerSide = (int) Math.min(Integer.MAX_VALUE, GTValues.V[getTier()] * 2L);
        for (EnumFacing side : EnumFacing.VALUES) {
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));
            if (tileEntity == null) {
                continue;
            }
            EnumFacing sourceSide = side.getOpposite();
            if (tileEntity.hasCapability(CapabilityHandler.CAPABILITY_HEAT_ENERGY, sourceSide)) {
                IHeatEnergy heatSource = tileEntity.getCapability(
                        CapabilityHandler.CAPABILITY_HEAT_ENERGY, sourceSide);
                if (heatSource != null) {
                    int heat = Math.min(Math.max(0, heatSource.getHeat()), maxInputPerSide);
                    if (heat > 0) {
                        int accepted = (int) receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, heat, false);
                        heatSource.changeHuEnergy(-accepted);
                    }
                }
            } else if (tileEntity.hasCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, sourceSide)) {
                IHeatable nativeSource = tileEntity.getCapability(
                        GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, sourceSide);
                // Native HeatContainerHandler pushes through our IHeatable view
                // and accounts its own total output flow. Pulling it as well
                // would bypass that budget and charge twice in the same tick.
                if (!(nativeSource instanceof HeatContainerHandler)) {
                    CrucibleNativeHeatTransfer.pull(nativeSource, sourceSide, maxInputPerSide,
                            heat -> receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, heat, true),
                            heat -> receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, heat, false));
                }
            }

            if (tileEntity.hasCapability(CapabilityHandler.CAPABILITY_COLD_ENERGY, sourceSide)) {
                IColdEnergy coldSource = tileEntity.getCapability(
                        CapabilityHandler.CAPABILITY_COLD_ENERGY, sourceSide);
                if (coldSource != null) {
                    int cold = Math.min(Math.max(0, coldSource.getCold()), maxInputPerSide);
                    if (cold > 0) {
                        int accepted = (int) receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.CU, cold, false);
                        coldSource.changeCuEnergy(-accepted);
                    }
                }
            }
            if (tileEntity.hasCapability(CapabilityHandler.CAPABILITY_KINETIC_ENERGY, sourceSide)) {
                IKineticEnergy kineticSource = tileEntity.getCapability(
                        CapabilityHandler.CAPABILITY_KINETIC_ENERGY, sourceSide);
                if (kineticSource != null) {
                    int kinetic = Math.min(Math.max(0, kineticSource.getKinetic()), maxInputPerSide);
                    if (kinetic > 0) {
                        int accepted = (int) receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.KU, kinetic, false);
                        kineticSource.changeKineticEnergy(-accepted);
                    }
                }
            }
        }
    }

    private void processStoredHeat() {
        if (storedHeat == 0L) {
            return;
        }
        double thermalMassKg = getThermalMassKg();
        long requiredEnergyPerKelvin = CrucibleTransferLogic.requiredEnergyPerKelvin(thermalMassKg);
        long temperatureDelta = CrucibleTransferLogic.temperatureDeltaForEnergy(storedHeat, thermalMassKg);
        if (temperatureDelta == 0L) {
            return;
        }
        long consumedEnergy = temperatureDelta * requiredEnergyPerKelvin;
        storedHeat -= consumedEnergy;
        if (temperatureDelta > 0L && temperature > Long.MAX_VALUE - temperatureDelta) {
            temperature = Long.MAX_VALUE;
        } else if (temperatureDelta < 0L && temperature < Long.MIN_VALUE - temperatureDelta) {
            temperature = Long.MIN_VALUE;
        } else {
            temperature += temperatureDelta;
        }
        markDirty();
        thermalCooldown = HEAT_COOLDOWN_TICKS;
        heatedThisTick = true;
    }

    @Override
    public long receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type type, long amount, boolean simulate) {
        if (type == null || amount <= 0L || getWorld() == null || getWorld().isRemote) return 0L;
        long accepted = amount;
        if (type == ICrucibleEnergyReceiver.Type.CU) {
            if (storedHeat < 0L) accepted = Math.min(amount, Long.MAX_VALUE + storedHeat);
        } else if (type != ICrucibleEnergyReceiver.Type.KU && storedHeat > 0L) {
            accepted = Math.min(amount, Long.MAX_VALUE - storedHeat);
        }
        if (accepted <= 0L) return 0L;
        if (simulate) return accepted;

        if (type == ICrucibleEnergyReceiver.Type.KU) {
            long space = Math.max(0L, CAPACITY - getTotalAmount());
            // Saturate before multiplication. Excess blowing is accepted like
            // GT6, but never creates air beyond the shared content capacity.
            long unitsPerThousand = GTValues.M;
            long air = accepted > Long.MAX_VALUE / unitsPerThousand ? space :
                    Math.min(space, accepted * unitsPerThousand / 1000L);
            if (air > 0L && CrucibleOxygenCompatibility.hasOxygen(getWorld(), getPos().up())) {
                addStoredMaterial(new StoredMaterial(Materials.Air, air, true, Materials.Air));
            }
        } else {
            storedHeat += type == ICrucibleEnergyReceiver.Type.CU ? -accepted : accepted;
        }
        energyReceivedThisTick = true;
        markDirty();
        return accepted;
    }

    private void processRainFill() {
        // GT6 uses its shared server tick cadence (SERVER_TIME % 600 == 10),
        // rather than a per-machine offset timer, for passive rain collection.
        if (getWorld().getTotalWorldTime() % RAIN_FILL_INTERVAL != RAIN_FILL_OFFSET ||
                !getWorld().isRainingAt(getPos().up())) {
            return;
        }
        Biome biome = getWorld().getBiome(getPos());
        float rainfall = biome.getRainfall();
        if (rainfall <= 0.0F || biome.getTemperature(getPos()) < 0.2F) {
            return;
        }

        // GT6 truncates the rainfall-derived amount before the thunder factor.
        int fluidAmount = Math.max(1, (int) (rainfall * 100.0F));
        if (getWorld().isThundering()) {
            fluidAmount *= 2;
        }
        FluidStack rainWater = Materials.Water.getFluid(fluidAmount);
        // GT6 addMaterialStacks is atomic: a rain pulse is either accepted in
        // full or not at all. The normal external fluid handler intentionally
        // supports partial fills, so probe it before committing this pulse.
        long ambientTemperature = getAmbientTemperature();
        if (addMaterialFromFluid(rainWater, false, ambientTemperature) == fluidAmount) {
            addMaterialFromFluid(rainWater, true, ambientTemperature);
        }
    }

    private void processDroppedItems(CrucibleItemIntake.Tick intake) {
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
                if (!pendingItems.isEmpty() || !intake.tryImport(() -> addMaterialFromItem(oneItem))) {
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

    private void processInputSlot(CrucibleItemIntake.Tick intake) {
        ItemStack stack = importItems.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }

        ItemStack oneItem = stack.copy();
        oneItem.setCount(1);
        if (pendingItems.isEmpty() && intake.tryImport(() -> addMaterialFromItem(oneItem))) {
            importItems.extractItem(0, 1, false);
        } else if (enqueuePendingItem(oneItem)) {
            importItems.extractItem(0, 1, false);
        }
    }

    private void processPendingItemQueue(CrucibleItemIntake.Tick intake) {
        if (!pendingItems.isEmpty()) {
            ItemStack first = pendingItems.get(0);
            ItemStack oneItem = first.copy();
            oneItem.setCount(1);
            if (!intake.tryImport(() -> addMaterialFromItem(oneItem))) {
                return;
            }
            first.shrink(1);
            if (first.isEmpty()) {
                pendingItems.remove(0);
            }
        }
    }

    private boolean enqueuePendingItem(ItemStack stack) {
        long inputAmount = getInputAmount(getInputMaterials(stack));
        if (stack.isEmpty() || CrucibleTransferLogic.acceptedQueueItems(pendingItemCount(), 1,
                PENDING_ITEM_CAPACITY) <= 0 || inputAmount <= 0 || inputAmount > CAPACITY) {
            return false;
        }
        // Only coalesce the tail: merging A, B, A into A+A, B breaks FIFO.
        if (!pendingItems.isEmpty()) {
            ItemStack queued = pendingItems.get(pendingItems.size() - 1);
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
        return CrucibleItemIntake.pendingCount(pendingItems);
    }

    private boolean addMaterialFromItem(ItemStack stack) {
        return addMaterialBatch(getInputMaterials(stack), getAmbientTemperature());
    }

    private boolean addMaterialBatch(List<MaterialStack> materials, long incomingTemperature) {
        return addMaterialBatch(materials, incomingTemperature, false);
    }

    private boolean addMaterialBatch(List<MaterialStack> materials, long incomingTemperature, boolean simulate) {
        long inputAmount = getInputAmount(materials);
        if (inputAmount <= 0 || inputAmount > CAPACITY - getTotalAmount()) {
            return false;
        }
        double incomingMass = 0.0D;
        for (MaterialStack materialStack : materials) {
            incomingMass += getMaterialWeightKg(materialStack.material, materialStack.amount);
        }
        // One mixed temperature for the entire physical item, before any
        // component changes identity or weight. Intermediate rounding must
        // not make recycling-list order affect melting or alloy availability.
        long mixedTemperature = CrucibleTransferLogic.smelteryIntakeTemperature(temperature, getThermalMassKg(),
                incomingTemperature, incomingMass);
        List<StoredMaterial> prepared = new ArrayList<>();
        long[] amounts = new long[materials.size()];
        int[] remainders = new int[materials.size()];
        for (int i = 0; i < materials.size(); i++) {
            MaterialStack component = materials.get(i);
            StoredMaterial received = prepareMaterialIntake(component.material, component.amount,
                    incomingTemperature, mixedTemperature);
            prepared.add(received);
            amounts[i] = received.amount;
            remainders[i] = received.fluidRemainder;
        }
        if (!CrucibleFluidUnits.incomingBatchFits(getTotalAmount(), CAPACITY, amounts, remainders)) return false;
        if (simulate) return true;
        temperature = mixedTemperature;
        for (StoredMaterial received : prepared) {
            addStoredMaterial(received);
        }
        markDirty();
        refreshDisplayState();
        return true;
    }

    private StoredMaterial prepareMaterialIntake(Material source, long amount,
                                                  long incomingTemperature, long mixedTemperature) {
        return prepareMaterialIntake(source, amount, 0, incomingTemperature, mixedTemperature);
    }

    private StoredMaterial prepareMaterialIntake(Material source, long amount, int remainder,
                                                  long incomingTemperature, long mixedTemperature) {
        int meltingPoint = getMeltingTemperature(source);
        boolean hasMeltingTarget = hasMaterialMeltingTarget(source);
        StoredMaterial received = new StoredMaterial(source, amount,
                mixedTemperature >= meltingPoint && hasMeltingTarget,
                getSolidifyingTarget(source));
        received.fluidRemainder = remainder;
        if (isLowDensityMaterial(source) || shouldVaporize(received, mixedTemperature) ||
                (!acidProof && isAcidMaterial(source))) return received;
        // The generic host fallback temperature is only a compatibility hint;
        // it must not make an unknown, non-fluid material phase-change on
        // intake. GT6 keeps that item solid unless a real hot target exists.
        CrucibleTransferLogic.IntakePhase intakePhase = hasMeltingTarget ?
                CrucibleTransferLogic.intakePhase(incomingTemperature, mixedTemperature, meltingPoint) :
                CrucibleTransferLogic.IntakePhase.NONE;
        switch (intakePhase) {
            case MELT:
                Material target = getSmeltingTarget(source);
                CrucibleFluidUnits.Quantity converted = getSmeltingStoredQuantity(source, target, amount, remainder);
                if (converted == null || (converted.amount == 0 && converted.remainder == 0)) {
                    // Disabled, missing or overflowing targets retain the
                    // source as recoverable solid content; never erase it.
                    received.molten = false;
                    break;
                }
                received.material = target;
                received.amount = converted.amount;
                received.fluidRemainder = converted.remainder;
                received.solidifyingMaterial = getSolidifyingTarget(target);
                received.molten = true;
                break;
            case SOLIDIFY:
                solidify(received);
                break;
            default:
                break;
        }
        return received;
    }

    private List<MaterialStack> getInputMaterials(ItemStack stack) {
        // A filled CEu battery block's static recycling data may describe only
        // its empty shell. Without manufacturing provenance, refuse it instead
        // of consuming it as a shell and silently losing the contained core.
        if (CrucibleComponentProvenance.isUntrackedFilledBatteryPart(stack)) {
            return Collections.emptyList();
        }
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(CrucibleComponentProvenance.TAG)) {
            return CrucibleComponentProvenance.resolve(stack);
        }
        if (!CrucibleToolRecycling.isTool(stack) && stack.copy().hasCapability(
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
            return CrucibleElectricRecycling.resolve(stack, MetaTileEntityCrucible::resolveMaterial);
        }
        if (CrucibleToolRecycling.isTool(stack)) {
            if (stack.copy().hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
                return CrucibleElectricProvenance.resolveTool(stack, MetaTileEntityCrucible::resolveMaterial);
            }
            if (!CrucibleToolRecycling.isSafeTool(stack)) return Collections.emptyList();
            if (stack.getTagCompound().hasKey(CrucibleToolProvenance.TAG)) {
                List<MaterialStack> account = CrucibleToolProvenance.resolve(stack);
                if (account.isEmpty()) return Collections.emptyList();
                if (stack.getTagCompound().hasKey(CrucibleRecyclingOverride.TAG)) {
                    List<MaterialStack> extra = CrucibleRecyclingOverride.parse(stack.getTagCompound(),
                            MetaTileEntityCrucible::resolveMaterial);
                    return extra.isEmpty() ? Collections.emptyList() : CrucibleRecyclingOverride.combine(account, extra);
                }
                return account;
            }
            List<MaterialStack> toolData = CrucibleToolRecycling.resolve(stack);
            if (stack.getTagCompound().hasKey(CrucibleRecyclingOverride.TAG)) {
                List<MaterialStack> overrideData = CrucibleRecyclingOverride.parse(stack.getTagCompound(),
                        MetaTileEntityCrucible::resolveMaterial);
                // A malformed explicit override cannot fall back to base data.
                if (overrideData.isEmpty()) return Collections.emptyList();
                return CrucibleRecyclingOverride.combine(toolData, overrideData);
            }
            return toolData;
        }
        // Containers and unknown custom NBT need their own material overrides.
        // Vanilla presentation/enchantment tags do not store extra materials.
        if (stack.isEmpty() || !hasStaticRecyclingTags(stack) ||
                stack.hasCapability(net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ||
                stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return Collections.emptyList();
        }
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(CrucibleRecyclingOverride.TAG)) {
            // An explicit override takes priority, including when invalid:
            // never fall back to unrelated static data and consume the item.
            return CrucibleRecyclingOverride.parse(stack.getTagCompound(), MetaTileEntityCrucible::resolveMaterial);
        }
        CrucibleFoodInput.Rule food = CrucibleFoodInput.find(stack);
        if (food != null) return food.resolve(MetaTileEntityCrucible::resolveMaterial);
        boolean gt6OreForm = false;
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (CrucibleOreInputForm.find(OreDictionary.getOreName(id)) != null) {
                gt6OreForm = true;
                break;
            }
        }
        if (gt6OreForm) {
            MaterialStack material = getGt6OreFormInput(stack);
            return material == null ? Collections.emptyList() : Collections.singletonList(material);
        }
        // Ore processing has its own explicit quantity/target mapping.
        if (!isOreInputPrefix(OreDictUnifier.getPrefix(stack))) {
            RecyclingData recycling = GregTechAPI.RECYCLING_MANAGER.getRecyclingData(stack);
            boolean scaleDurability = false;
            if (recycling == null && stack.getItem().isDamageable() && stack.isItemDamaged()) {
                ItemStack pristine = stack.copy();
                pristine.setItemDamage(0);
                recycling = GregTechAPI.RECYCLING_MANAGER.getRecyclingData(pristine);
                scaleDurability = recycling != null;
            }
            if (recycling != null && !recycling.getMaterials().isEmpty()) {
                List<MaterialStack> result = new ArrayList<>();
                for (MaterialStack component : recycling.getMaterials()) {
                    if (component == null || component.material == null || component.material == Materials.NULL ||
                            component.amount <= 0) {
                        return Collections.emptyList();
                    }
                    long amount = scaleDurability ? CrucibleTransferLogic.remainingDurabilityMaterial(
                            component.amount, stack.getItemDamage(), stack.getMaxDamage()) : component.amount;
                    if (amount > 0) result.add(new MaterialStack(component.material, amount));
                }
                return result;
            }
        }
        MaterialStack single = getInputMaterial(stack);
        return single == null ? Collections.emptyList() : Collections.singletonList(single);
    }

    private boolean hasStaticRecyclingTags(ItemStack stack) {
        if (!stack.hasTagCompound()) return true;
        // CEu tools encode material/durability in custom NBT and require a dynamic resolver.
        if (stack.getItem().getRegistryName() == null ||
                (!"minecraft".equals(stack.getItem().getRegistryName().getNamespace()) &&
                        !stack.getTagCompound().hasKey(CrucibleRecyclingOverride.TAG))) return false;
        for (String key : stack.getTagCompound().getKeySet()) {
            if (!"display".equals(key) && !"ench".equals(key) && !"RepairCost".equals(key) &&
                    !"Unbreakable".equals(key) && !CrucibleRecyclingOverride.TAG.equals(key)) return false;
        }
        return true;
    }

    private long getInputAmount(List<MaterialStack> materials) {
        long total = 0;
        for (MaterialStack material : materials) {
            if (material.material == null || material.material == Materials.NULL || material.amount <= 0 ||
                    material.amount > CAPACITY - total) {
                return -1;
            }
            total += material.amount;
        }
        return total;
    }

    @Nullable
    private MaterialStack getGt6OreFormInput(ItemStack stack) {
        MaterialStack selected = null;
        for (int id : OreDictionary.getOreIDs(stack)) {
            CrucibleOreInputForm form = CrucibleOreInputForm.find(OreDictionary.getOreName(id));
            if (form == null) continue;
            Material source = resolveMaterial(form.materialName);
            if (source == null || source == Materials.NULL) return null;
            String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(source);
            CrucibleOreInputRule crushing = CrucibleOreInputRule.find(mechanicsName);
            Material target = crushing == null ? source : resolveMaterial(crushing.target);
            if (target == null || target == Materials.NULL) return null;
            OreProperty property = source.hasProperty(PropertyKey.ORE) ? source.getProperty(PropertyKey.ORE) : null;
            long multiplier = GT6OreMultiplierData.multiplier(mechanicsName,
                    property == null ? 1 : property.getOreMultiplier());
            long base = GTValues.M * form.multiplier;
            if (multiplier > Long.MAX_VALUE / base) return null;
            long amount = base * multiplier;
            // Multiple aliases must agree; never choose an arbitrary material/quantity.
            if (selected != null && (selected.material != target || selected.amount != amount)) return null;
            selected = new MaterialStack(target, amount);
        }
        return selected;
    }

    @Nullable
    private MaterialStack getInputMaterial(ItemStack stack) {
        OrePrefix prefix = OreDictUnifier.getPrefix(stack);
        if (isOreInputPrefix(prefix)) {
            // CEu assigns -1 as the material amount for ore prefixes, so
            // getMaterial(stack) is intentionally not a usable quantity here.
            // Resolve the material from the unification entry and apply GT6's
            // one-U ore input amount plus its explicit crushing multiplier.
            UnificationEntry entry = OreDictUnifier.getUnificationEntry(stack);
            if (entry == null) return null;
            Material oreMaterial = entry.material != null ? entry.material : entry.orePrefix.materialType;
            if (oreMaterial == null || oreMaterial == Materials.NULL) return null;
            String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(oreMaterial);
            // GT6 distinguishes legacy dense dimension prefixes from ordinary
            // netherrack/endstone ores. CEu's resolved prefix avoids confusing
            // the material NetherQuartz with the legacy oreNether prefix.
            long baseAmount = GTValues.M * CrucibleOreInputForm.registeredOreMultiplier(prefix.name);
            CrucibleOreInputRule crushing = CrucibleOreInputRule.find(mechanicsName);
            if (crushing != null) {
                Material target = resolveMaterial(crushing.target);
                if (target == null || target == Materials.NULL ||
                        baseAmount > Long.MAX_VALUE / crushing.multiplier) {
                    return null;
                }
                return new MaterialStack(target, baseAmount * crushing.multiplier);
            }
            OreProperty oreProperty = oreMaterial.hasProperty(PropertyKey.ORE) ?
                    oreMaterial.getProperty(PropertyKey.ORE) : null;
            long multiplier = GT6OreMultiplierData.multiplier(mechanicsName,
                    oreProperty == null ? 1L : oreProperty.getOreMultiplier());
            if (baseAmount > Long.MAX_VALUE / multiplier) return null;
            // Retain the crushed material itself; furnace direct-smelt outputs
            // are not a substitute for GT6 targetCrushing/targetSmelting.
            return new MaterialStack(oreMaterial, baseAmount * multiplier);
        }

        MaterialStack materialStack = OreDictUnifier.getMaterial(stack);
        if (materialStack == null || materialStack.material == null || materialStack.amount <= 0L) return null;
        // GT6 accepts valid material stacks even when the current vessel cannot
        // liquefy them. Keep such materials as solids so they can be recovered;
        // only non-material/invalid inputs are refused without consumption.
        return materialStack;
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
        CrucibleFluidInput input = CrucibleFluidInput.resolve(resource);
        if (input == null) {
            return 0;
        }
        Material material = input.material;
        long space = CAPACITY - getTotalAmount();
        if (space <= 0) {
            return 0;
        }

        int acceptedFluid = CrucibleTransferLogic.acceptedSharedFluidAmount(
                resource.amount, CrucibleTransferLogic.materialFluidAmount(space, GTValues.M, input.unit));
        if (acceptedFluid <= 0) {
            return 0;
        }
        CrucibleFluidUnits.Quantity exact = CrucibleFluidUnits.storedFluidAmount(acceptedFluid, input.unit);
        if (exact == null) return 0;

        FluidStack acceptedStack = resource.copy();
        acceptedStack.amount = acceptedFluid;
        long incomingTemperature = temperatureOverride == null ?
                acceptedStack.getFluid().getTemperature(acceptedStack) : temperatureOverride;
        if (temperatureOverride == null && !input.plasma && !acceptedStack.getFluid().isGaseous(acceptedStack) &&
                !isAcidMaterial(material)) {
            incomingTemperature = CrucibleTransferLogic.fluidInputTemperature(incomingTemperature,
                    CrucibleMaterialPhaseData.meltingPoint(material),
                    CrucibleMaterialPhaseData.boilingPoint(material));
        }
        long mixedTemperature = CrucibleTransferLogic.smelteryIntakeTemperature(temperature, getThermalMassKg(),
                incomingTemperature, getMaterialWeightKg(material, exact.amount, exact.remainder));
        StoredMaterial received;
        if (isWaterMaterial(material) || isLavaMaterial(material)) {
            received = new StoredMaterial(material, exact.amount, false,
                    getSolidifyingTarget(material));
            received.fluidRemainder = exact.remainder;
            if (isWaterMaterial(material)) {
                if (CrucibleTransferLogic.shouldFreezeWater(mixedTemperature)) {
                    solidify(received);
                } else {
                    received.molten = true;
                }
            } else {
                // Lava remains a fluid entry until complete 1000 mB portions condense below 1300 K.
                received.molten = mixedTemperature >= getMeltingTemperature(material);
            }
        } else {
            received = prepareMaterialIntake(material, exact.amount, exact.remainder,
                    incomingTemperature, mixedTemperature);
            // A registered liquid is already the material's fluid form; its
            // identity must not depend on an optional INGOT melting target.
            // Only preserve the incoming liquid phase when the exact fluid
            // binding and post-mix temperature both support a molten entry.
            if (!input.plasma && !hasMaterialMeltingTarget(material) &&
                    CrucibleFluidInput.liquidFluid(material) == acceptedStack.getFluid() &&
                    mixedTemperature >= getMeltingTemperature(material)) {
                received.molten = true;
            }
        }
        // Same atomic arrival contract as items and top-side pouring. The
        // capacity check uses the prepared identity and its exact fraction;
        // failed expansion must not spend the incoming fluid or change heat.
        if (!CrucibleFluidUnits.incomingBatchFits(getTotalAmount(), CAPACITY,
                new long[]{received.amount}, new int[]{received.fluidRemainder})) return 0;
        if (!doFill) return acceptedFluid;
        temperature = mixedTemperature;
        addStoredMaterial(received);
        markDirty();
        refreshDisplayState();
        return acceptedFluid;
    }

    private void processSpecialFluids() {
        boolean changed = false;
        List<StoredMaterial> condensedObsidian = new ArrayList<>();
        Iterator<StoredMaterial> iterator = contents.iterator();
        while (iterator.hasNext()) {
            StoredMaterial material = iterator.next();
            if (isWaterMaterial(material.material) && CrucibleTransferLogic.shouldBoilWater(temperature)) {
                long evaporatedAmount = material.amount;
                iterator.remove();
                playHazardFizz();
                releaseHotGasEffects(CrucibleMaterialPhaseData.boilingPoint(Materials.Water),
                        evaporatedAmount);
                changed = true;
                continue;
            }
            if (isWaterMaterial(material.material)) {
                if (CrucibleTransferLogic.shouldFreezeWater(temperature)) {
                    changed |= solidify(material);
                } else if (!material.molten) {
                    material.molten = true;
                    changed = true;
                }
                continue;
            }
            if (!isLavaMaterial(material.material)) {
                continue;
            }

            boolean molten = temperature >= getMeltingTemperature(material.material);
            if (material.molten != molten) {
                material.molten = molten;
                changed = true;
            }
            if (!CrucibleTransferLogic.shouldCondenseLava(temperature)) {
                continue;
            }

            Material obsidianTarget = getSolidifyingTarget(material.material);
            if (obsidianTarget == null || obsidianTarget == Materials.NULL || obsidianTarget == material.material) {
                continue; // Never consume a bucket when the required target is absent.
            }
            CrucibleFluidUnits.LavaCondensation condensed = CrucibleFluidUnits.condenseLava(
                    material.amount, material.fluidRemainder,
                    // This is an internal material conversion, not a projection
                    // of whichever host phase happens to be the default fluid.
                    // Obsidian melting and vanilla lava input also use L mB/M.
                    GTValues.L);
            if (condensed == null) {
                continue;
            }
            material.amount = condensed.remaining.amount;
            material.fluidRemainder = condensed.remaining.remainder;
            long obsidianAmount = condensed.obsidianCount * (long) GTValues.M;
            condensedObsidian.add(new StoredMaterial(obsidianTarget, obsidianAmount, false, obsidianTarget));
            changed = true;
            if (material.amount == 0L && material.fluidRemainder == 0) {
                iterator.remove();
            }
        }
        for (StoredMaterial obsidian : condensedObsidian) {
            addStoredMaterial(obsidian);
        }
        if (changed) {
            removeEmptyContents();
            markDirty();
            refreshDisplayState();
        }
    }

    private double getThermalMassKg() {
        // GT6's single-block Smeltery includes its wall material at U * 7;
        // the multiblock Crucible's U * 100 wall basis does not apply here.
        // GT6's default material density is 1 g/cm^3 (1000 kg/m^3).
        double mass = getMaterialWeightKg(vesselMaterial, GT6_VESSEL_MATERIAL_AMOUNT);
        for (StoredMaterial material : contents) {
            mass += getMaterialWeightKg(material.material, material.amount, material.fluidRemainder);
        }
        return Math.max(1.0D, mass);
    }

    private double getMaterialWeightKg(Material material, long amount) {
        return getMaterialWeightKg(material, amount, 0);
    }

    private double getMaterialWeightKg(Material material, long amount, int remainder) {
        double densityKgPerCubicMeter = getGt6MaterialDensityKgPerCubicMeter(material,
                Collections.newSetFromMap(new IdentityHashMap<Material, Boolean>()));
        return CrucibleTransferLogic.materialWeightKg(amount, remainder, CrucibleFluidUnits.STORAGE_UNIT,
                densityKgPerCubicMeter, GTValues.M);
    }

    private double getGt6MaterialDensityKgPerCubicMeter(Material material, Set<Material> visiting) {
        if (material == null) {
            return GT6_DEFAULT_MATERIAL_DENSITY_KG_PER_CUBIC_METER;
        }

        if (CrucibleTransferLogic.hasKnownGt6MaterialDensity(material)) {
            return CrucibleTransferLogic.knownGt6MaterialDensityKgPerCubicMeter(material);
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
        return CrucibleRegisteredDensity.get(material);
    }

    private long getAmbientTemperature() {
        Biome biome = getWorld().getBiome(getPos());
        if (biome == null) {
            return DEFAULT_ENVIRONMENT_TEMPERATURE;
        }
        return Math.max(1L, 270L + (long) (biome.getTemperature(getPos()) * 20.0F));
    }

    private boolean processMaterialTemperatureState() {
        boolean newContent = newContentForPhaseTargets;
        newContentForPhaseTargets = false;
        // GT6 checks hazards on the source material before phase conversion:
        // an acid must not escape corrosion by turning into a neutral oxide.
        if (processHazards()) {
            return true;
        }
        boolean changed = false;
        for (StoredMaterial material : contents) {
            // Lava's sub-1300 K conversion is quantized to full 1000 mB units in
            // processSpecialFluids(); generic solidification would lose the remainder.
            if (isLavaMaterial(material.material) || isWaterMaterial(material.material)) {
                continue;
            }
            if (CrucibleTransferLogic.shouldApplyHotTarget(temperature, oldTemperature,
                    getMeltingTemperature(material.material), material.molten, newContent) &&
                    hasMaterialMeltingTarget(material.material)) {
                changed |= melt(material);
            } else if (CrucibleTransferLogic.shouldApplyColdTarget(temperature,
                    getMeltingTemperature(material.material), material.molten, newContent,
                    getSolidifyingTarget(material.material) != material.material)) {
                changed |= solidify(material);
            }
        }
        // GT6 adds converted stacks back through addToList, coalescing their
        // identity. In-place conversion must not split the fluid projection.
        changed |= mergeCompatibleContents();
        if (changed) {
            markDirty();
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
        destroyedByHeat = true;
        playHazardFizz();
        releaseOverheatEffects(temperature, 2.0F);
        contents.clear();
        temperature = getAmbientTemperature();
        refreshDisplayState();
        markDirty();
        getWorld().setBlockState(getPos(), Blocks.FLOWING_LAVA.getDefaultState()
                .withProperty(BlockLiquid.LEVEL, 1), 3);
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
                playHazardFizz();
                changed = true;
                continue;
            }

            if (shouldVaporize(material)) {
                long amount = material.amount;
                playHazardFizz();
                long boilingPoint = CrucibleMaterialPhaseData.boilingPoint(material.material);
                releaseHotGasEffects(boilingPoint == Long.MAX_VALUE ? temperature : boilingPoint, amount);
                if (GT6MaterialHazardData.isExplosiveMaterial(material.material)) {
                    explodeFromContent(amount);
                    return true;
                }
                iterator.remove();
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
        playHazardFizz();
        contents.clear();
        temperature = getAmbientTemperature();
        refreshDisplayState();
        markDirty();
        getWorld().setBlockToAir(getPos());
    }

    private void playHazardFizz() {
        getWorld().playSound(null, getPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                1.0F, 0.9F + getWorld().rand.nextFloat() * 0.2F);
    }

    private boolean shouldVaporize(StoredMaterial material) {
        return shouldVaporize(material, temperature);
    }

    private boolean shouldVaporize(StoredMaterial material, long atTemperature) {
        long boilingPoint = CrucibleMaterialPhaseData.boilingPoint(material.material);
        // FluidProperty only tells us which external fluid forms are registered;
        // a GAS/PLASMA key is not an authoritative boiling point. GT6 evaporates
        // contents at its numeric mBoilingPoint, so unknown host data must not
        // cause an accepted gas input to vanish on the next server tick.
        return (boilingPoint != Long.MAX_VALUE && atTemperature >= boilingPoint) ||
                GT6MaterialHazardData.shouldBurn(material.material, atTemperature);
    }

    private boolean isLowDensityMaterial(Material material) {
        return material == Materials.Air ||
                CrucibleTransferLogic.isAirDensity(getMaterialDensityKgPerCubicMeter(material));
    }

    private boolean isAcidMaterial(Material material) {
        return GT6MaterialHazardData.isAcidMaterial(material);
    }

    private void explodeFromContent(long amount) {
        World world = getWorld();
        BlockPos pos = getPos();
        // GT6's root explosion removes the vessel before producing its blast.
        // CEu's explosion flag also prevents a machine item drop after removal.
        setExploded();
        contents.clear();
        refreshDisplayState();
        markDirty();
        float strength = CrucibleTransferLogic.contentExplosionStrength(amount, CAPACITY);
        world.setBlockToAir(pos);
        world.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                strength, true);
    }

    private void releaseHotGasEffects(long heat, long amount) {
        float damage = CrucibleTransferLogic.vaporHeatDamage(heat);
        if (damage > 0.0F) {
            AxisAlignedBB bounds = new AxisAlignedBB(
                    getPos().getX() - FLAME_RANGE, getPos().getY() - 1.0D, getPos().getZ() - FLAME_RANGE,
                    getPos().getX() + FLAME_RANGE + 1.0D, getPos().getY() + FLAME_RANGE + 1.0D,
                    getPos().getZ() + FLAME_RANGE + 1.0D);
            for (EntityLivingBase entity : getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
                applyHeatHazard(entity, damage);
            }
        }
        if (heat >= FIRE_TEMPERATURE) {
            placeHazardFire(amount);
        }
    }

    private void releaseOverheatEffects(long heat, float damageMultiplier) {
        // GT6 uses multiplier 2 for melting down, but its player's hot removal
        // uses the default multiplier 1. Steam's shared helper has multiplier 2.
        float damage = CrucibleTransferLogic.vaporHeatDamage(heat) * damageMultiplier / 2.0F;
        if (damage > 0.0F) {
            AxisAlignedBB bounds = new AxisAlignedBB(
                    getPos().getX() - FLAME_RANGE, getPos().getY() - 1.0D, getPos().getZ() - FLAME_RANGE,
                    getPos().getX() + FLAME_RANGE + 1.0D, getPos().getY() + FLAME_RANGE + 1.0D,
                    getPos().getZ() + FLAME_RANGE + 1.0D);
            for (EntityLivingBase entity : getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
                applyHeatHazard(entity, damage);
            }
        }
        int fireCount = CrucibleTransferLogic.overheatFireAttempts(heat);
        for (int i = 0; i < fireCount; i++) {
            BlockPos firePos = getPos().add(
                    getWorld().rand.nextInt(FLAME_RANGE * 2 + 1) - FLAME_RANGE,
                    getWorld().rand.nextInt(FLAME_RANGE + 2) - 1,
                    getWorld().rand.nextInt(FLAME_RANGE * 2 + 1) - FLAME_RANGE);
            CrucibleHazardFire.tryIgnite(getWorld(), firePos, getWorld().rand.nextInt(3) != 0);
        }
    }

    private void placeHazardFire(long amount) {
        int fires = CrucibleTransferLogic.materialHazardFireAttempts(amount, GTValues.M, CAPACITY);
        for (int i = 0; i < fires; i++) {
            BlockPos firePos = getPos().add(
                    getWorld().rand.nextInt(FLAME_RANGE * 2 + 1) - FLAME_RANGE,
                    getWorld().rand.nextInt(FLAME_RANGE + 2) - 1,
                    getWorld().rand.nextInt(FLAME_RANGE * 2 + 1) - FLAME_RANGE);
            CrucibleHazardFire.tryIgnite(getWorld(), firePos, getWorld().rand.nextInt(3) != 0);
        }
    }

    private boolean melt(StoredMaterial material) {
        Material target = getSmeltingTarget(material.material);
        CrucibleFluidUnits.Quantity converted = getSmeltingStoredQuantity(material.material, target,
                material.amount, material.fluidRemainder);
        if (converted == null || (converted.amount == 0 && converted.remainder == 0)) return false;
        if (!CrucibleFluidUnits.replacementFits(getTotalAmount(), CAPACITY,
                material.amount, material.fluidRemainder, converted.amount, converted.remainder,
                contents.contains(material))) return false;
        // Commit only after every conversion succeeds. A failed expansion
        // must not leave the source amount partially converted for a retry.
        material.amount = converted.amount;
        material.fluidRemainder = converted.remainder;
        material.material = target;
        material.solidifyingMaterial = getSolidifyingTarget(target);
        material.molten = true;
        return true;
    }

    private static CrucibleFluidUnits.Quantity getSmeltingStoredQuantity(Material source, Material target,
                                                                         long amount, int remainder) {
        if (source == null || target == null || target == Materials.NULL) return null;
        String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(source);
        if (GT6LiteralTargetData.isAmbiguous(mechanicsName)) return null;
        GT6LiteralTargetData.Profile literal = GT6LiteralTargetData.find(mechanicsName);
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(mechanicsName);
        CrucibleFluidUnits.Quantity converted;
        if (literal != null) {
            if (literal.hotAmount <= 0) return null;
            converted = CrucibleFluidUnits.scaleStored(amount, remainder,
                    literal.hotAmount, GT6LiteralTargetData.GT6_U);
        } else {
            converted = rule == null ? CrucibleFluidUnits.scaleStored(amount, remainder, 1, 1) :
                    rule.convertStored(amount, remainder);
        }
        if (converted != null && isLavaMaterial(target) && (source == Materials.Obsidian ||
                "obsidian".equals(normalizeMaterialName(mechanicsName)) ||
                (literal != null && "lava".equals(normalizeMaterialName(literal.hotTargetName))) ||
                (rule != null && "lava".equals(rule.target)))) {
            return CrucibleFluidUnits.scaleStored(converted.amount, converted.remainder,
                    LAVA_OBSIDIAN_MILLIBUCKETS, GTValues.L);
        }
        return converted;
    }

    private boolean solidify(StoredMaterial material) {
        // Lava is handled in complete bucket portions by processSpecialFluids.
        // Never restore a historical input material (e.g. magnetic iron) just
        // because an older save recorded it as the solidification target.
        if (isLavaMaterial(material.material)) {
            return false;
        }
        Material target = getSolidifyingTarget(material.material);
        if (target == null || target == Materials.NULL) return false;
        String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(material.material);
        if (GT6LiteralTargetData.isAmbiguous(mechanicsName)) return false;
        GT6LiteralTargetData.Profile literal = GT6LiteralTargetData.find(mechanicsName);
        CrucibleFluidUnits.Quantity converted = literal == null ?
                CrucibleFluidUnits.scaleStored(material.amount, material.fluidRemainder, 1, 1) :
                CrucibleFluidUnits.scaleStored(material.amount, material.fluidRemainder,
                        literal.coldAmount, GT6LiteralTargetData.GT6_U);
        if (converted == null || (converted.amount == 0 && converted.remainder == 0)) return false;
        if (contents.contains(material) && !CrucibleFluidUnits.replacementFits(getTotalAmount(), CAPACITY,
                material.amount, material.fluidRemainder, converted.amount, converted.remainder, true)) {
            return false;
        }
        boolean changed = material.molten || material.material != target || material.solidifyingMaterial != null ||
                material.amount != converted.amount || material.fluidRemainder != converted.remainder;
        material.material = target;
        material.amount = converted.amount;
        material.fluidRemainder = converted.remainder;
        material.solidifyingMaterial = null;
        material.molten = false;
        return changed;
    }

    static Material getSolidifyingTarget(Material moltenMaterial) {
        if (moltenMaterial == null || moltenMaterial == Materials.NULL) {
            return moltenMaterial;
        }
        String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(moltenMaterial);
        if (GT6LiteralTargetData.isAmbiguous(mechanicsName)) return null;
        GT6LiteralTargetData.Profile literal = GT6LiteralTargetData.find(mechanicsName);
        if (literal != null) {
            if (literal.coldAmount <= 0) return null;
            if (literal.coldTargetId == literal.id) return moltenMaterial;
            Material target = resolveLiteralTarget(literal.coldTargetName);
            // An unresolved optional identity is not permission to substitute
            // the source material as the cooling product.
            return target == null || target == Materials.NULL ? null : target;
        }
        String gt6Target = CrucibleSolidifyingRule.target(mechanicsName);
        if (gt6Target != null) {
            Material target = resolveMaterial(gt6Target);
            // An explicit target is part of the conversion contract. If an
            // optional external material is absent, refuse the conversion
            // rather than solidifying back into the molten identity.
            return target == null || target == Materials.NULL ? null : target;
        }
        // GT6's constructor initializes solidification to one U of self. For
        // verified snapshot materials this is an authoritative relation, not
        // absence of data permitting a CEu reverse-fluid target to override it.
        if (CrucibleSolidifyingRule.hasAuthoritativeSelfTarget(mechanicsName)) {
            return moltenMaterial;
        }

        Fluid moltenFluid = moltenMaterial.hasFluid() ? moltenMaterial.getFluid() : null;
        Material explicitCandidate = null;
        for (Material candidate : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (candidate == null || candidate == moltenMaterial || !candidate.hasProperty(PropertyKey.FLUID)) {
                continue;
            }
            FluidProperty fluidProperty = candidate.getProperty(PropertyKey.FLUID);
            if (moltenFluid != null && fluidProperty.solidifiesFrom() == moltenFluid) {
                if (explicitCandidate != null && explicitCandidate != candidate) {
                    return moltenMaterial;
                }
                explicitCandidate = candidate;
            }
        }
        if (explicitCandidate != null) {
            return explicitCandidate;
        }

        // GT6 initializes targetSolidifying to self. Even a unique reverse
        // smelting candidate is not evidence of an explicit cooling target.
        return moltenMaterial;
    }

    /** Handle living entities inside this block directly from the server tick. */
    private void processEntityContacts() {
        if (CrucibleTransferLogic.contactTemperatureDamage(temperature, 10.0F) <= 0) return;
        // Vanilla's entity/block overlap check excludes exact boundary contact.
        // Include the hollow interior: touching the hot contents is dangerous too.
        AxisAlignedBB bounds = new AxisAlignedBB(getPos()).grow(-0.001D);
        for (EntityLivingBase entity : getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
            onEntityContact(entity);
        }
    }

    /** Applies GT6 temperature contact and recycling after a fatal contact. */
    public void onEntityContact(EntityLivingBase entity) {
        if (getWorld() == null || getWorld().isRemote || entity == null || !entity.isEntityAlive()) return;
        long contactTemperature = temperature;
        float damage = CrucibleTransferLogic.contactTemperatureDamage(contactTemperature, 10.0F);
        if (damage <= 0) return;
        applyContactTemperatureHazard(entity, damage);
        if (CrucibleEntityRecycling.shouldRecycleDeath(true, entity.isEntityAlive(), contactTemperature)) {
            recycleContactDeath(entity);
        }
    }

    private void recycleContactDeath(EntityLivingBase entity) {
        CrucibleEntityRecycling.Rule rule = CrucibleEntityRecycling.rule(entity.getClass(), entity.getName());
        if (rule == null) return;
        List<MaterialStack> materials = new ArrayList<>();
        for (int i = 0; i < rule.materials.length; i++) {
            Material material = resolveMaterial(rule.materials[i]);
            // Do not substitute unrelated meat/bone materials when the exact
            // GT6 product is not registered by the host or an integration.
            if (material == null || material == Materials.NULL) return;
            materials.add(new MaterialStack(material, (long) rule.units[i] * GTValues.M));
        }
        long incomingTemperature = rule.temperature < 0 ? getAmbientTemperature() : rule.temperature;
        for (int repeat = 0; repeat < rule.repetitions; repeat++) {
            if (!addMaterialBatch(materials, incomingTemperature)) break;
        }
    }

    private void processAlloys() {
        // No explicit GT6 or CEu alloy recipe can react without any stored
        // material. Avoid walking both the complete GT6 recipe table and the
        // host material registry for empty crucibles on every server tick.
        if (contents.isEmpty()) {
            return;
        }
        // GT6 commits one preferred alloy per tick, not a same-tick reaction chain.
        AlloyMatch match = findBestAlloyMatch();
        if (match != null) {
            applyAlloy(match);
        }
    }

    @Nullable
    private AlloyMatch findBestAlloyMatch() {
        // Explicit GT6 recipes carry an authoritative commonDivider. Do not
        // let a generic CEu component decomposition shadow their yields.
        Set<Material> matchedGt6Outputs = Collections.newSetFromMap(new IdentityHashMap<>());
        AlloyMatch gt6Match = findBestGt6RecipeMatch(matchedGt6Outputs);
        Collection<Material> materials = GregTechAPI.materialManager.getRegisteredMaterials();
        AlloyMatch bestMatch = gt6Match;
        for (Material alloy : materials) {
            // A known GT6 output must not silently use a different CEu recipe
            // when its GT6 ingredients are absent or insufficient. These
            // audited alloy outputs are handled by the explicit path without
            // requiring a Forge fluid. Audited composition-only materials
            // intentionally have no recipe and must not be synthesized here.
            String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(alloy);
            if (mechanicsName != null && GT6AlloyRecipes.hasCompleteRecipeSet(mechanicsName)) continue;
            if (alloy == null || !alloy.hasFluid() || temperature < getMeltingTemperature(alloy)) {
                continue;
            }
            // When GT6 has an explicit recipe for this output, use its declared
            // commonDivider rather than CEu's composition-derived quantity.
            if (matchedGt6Outputs.contains(alloy)) continue;

            List<MaterialStack> components = normalizeAlloyComponents(alloy.getMaterialComponents());
            if (components.size() < 2) {
                continue;
            }

            long conversions = Long.MAX_VALUE;
            long outputUnits = 0;
            int nonMoltenComponents = 0;
            boolean valid = true;
            for (MaterialStack component : components) {
                Material componentMaterial = component.material;
                long componentAmount = component.amount;
                long available = getStoredAmount(componentMaterial);
                if (available < componentAmount) {
                    valid = false;
                    break;
                }
                if (temperature < getMeltingTemperature(componentMaterial)) {
                    nonMoltenComponents++;
                }
                conversions = Math.min(conversions, available / componentAmount);
                if (componentAmount > Long.MAX_VALUE - outputUnits) {
                    valid = false;
                    break;
                }
                outputUnits += componentAmount;
            }

            if (!valid || conversions <= 0 || outputUnits <= 0 || nonMoltenComponents > 1) {
                continue;
            }
            // Material-volume-conserving CEu component recipes cannot produce
            // more units than are present; also bound the multiplication.
            conversions = Math.min(conversions, getTotalAmount() / outputUnits);
            if (conversions <= 0) continue;
            if (bestMatch == null || conversions * outputUnits > bestMatch.conversions * bestMatch.outputUnits) {
                bestMatch = new AlloyMatch(alloy, components, conversions, outputUnits);
            }
        }
        return bestMatch;
    }

    @Nullable
    private AlloyMatch findBestGt6RecipeMatch(Set<Material> matchedOutputs) {
        AlloyMatch best = null;
        // Many optional GT6 materials are deliberately absent. Repeated
        // same-name queries must not rescan the entire registry per recipe.
        // Lifetime is this invocation only: no stale missing bindings survive
        // a registry change, world restart or a different test registry.
        Map<String, Material> bindings = new HashMap<>();
        for (GT6AlloyRecipes recipe : GT6AlloyRecipes.ALL) {
            // GT6 matches integer coefficients against its internal material
            // units, not whole-ingot batches. Keeping both sides unscaled
            // allows dust/nugget portions and recipes larger than the vessel
            // (e.g. 14:3 -> 6) to react in fractional-ingot batches.
            long outputAmount = recipe.outputUnits;
            List<MaterialStack> components = new ArrayList<>();
            long conversions = Long.MAX_VALUE / outputAmount;
            int nonMolten = 0;
            for (int i = 0; i < recipe.inputs.length; i++) {
                Material input = resolveRecipeMaterial(recipe.inputs[i], bindings);
                if (input == null) {
                    conversions = 0;
                    break;
                }
                long required = recipe.inputUnits[i];
                long available = getStoredAmount(input);
                if (available < required) {
                    conversions = 0;
                    break;
                }
                if (temperature < getMeltingTemperature(input)) nonMolten++;
                conversions = Math.min(conversions, available / required);
                components.add(new MaterialStack(input, required));
            }
            // GT6 reaches alloy references only from a molten component.
            // A single-input recipe therefore cannot start with a solid input.
            if (conversions <= 0 || nonMolten > 1 || nonMolten == components.size()) continue;
            // Resolve an optional output only after its ingredients actually
            // match. No input is removed until applyAlloy commits a candidate.
            Material output = resolveRecipeMaterial(recipe.output, bindings);
            if (output == null || temperature < getMeltingTemperature(output)) continue;
            matchedOutputs.add(output);
            // GT6 chooses the candidate with the largest conversions × commonDivider.
            if (best == null || conversions * outputAmount > best.conversions * best.outputUnits) {
                best = new AlloyMatch(output, components, conversions, outputAmount);
            }
        }
        return best;
    }

    @Nullable
    private static Material resolveRecipeMaterial(String name, Map<String, Material> bindings) {
        if (!bindings.containsKey(name)) bindings.put(name, resolveMaterial(name));
        return bindings.get(name);
    }

    private void applyAlloy(AlloyMatch match) {
        for (MaterialStack component : match.components) {
            Material material = component.material;
            long amount = component.amount * match.conversions;
            removeMaterial(material, amount);
        }
        boolean molten = temperature >= getMeltingTemperature(match.alloy);
        addStoredMaterial(new StoredMaterial(match.alloy, match.outputUnits * match.conversions, molten,
                molten ? getSolidifyingTarget(match.alloy) : match.alloy));
        removeEmptyContents();
        markDirty();
    }

    public static List<MaterialStack> normalizeAlloyComponents(@Nullable List<MaterialStack> source) {
        if (source == null) return Collections.emptyList();
        List<MaterialStack> result = new ArrayList<>();
        for (MaterialStack component : source) {
            if (component == null || component.material == null || component.material == Materials.NULL ||
                    component.amount <= 0L) {
                return Collections.emptyList();
            }
            MaterialStack existing = null;
            for (MaterialStack candidate : result) {
                if (candidate.material == component.material) {
                    existing = candidate;
                    break;
                }
            }
            if (existing == null) {
                result.add(new MaterialStack(component.material, component.amount));
            } else {
                if (component.amount > Long.MAX_VALUE - existing.amount) return Collections.emptyList();
                result.set(result.indexOf(existing),
                        new MaterialStack(existing.material, existing.amount + component.amount));
            }
        }
        return result;
    }

    private long getStoredAmount(Material material) {
        long amount = 0;
        for (StoredMaterial storedMaterial : contents) {
            if (storedMaterial.material == material) {
                amount = CrucibleTransferLogic.saturatingMaterialSum(amount, storedMaterial.amount);
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
            if (storedMaterial.material == material.material && storedMaterial.molten == material.molten) {
                CrucibleFluidUnits.Quantity merged = CrucibleFluidUnits.merge(storedMaterial.amount,
                        storedMaterial.fluidRemainder, material.amount, material.fluidRemainder,
                        CrucibleFluidUnits.STORAGE_UNIT);
                if (merged != null) {
                    storedMaterial.amount = merged.amount;
                    storedMaterial.fluidRemainder = merged.remainder;
                    newContentForPhaseTargets = true;
                    return;
                }
            }
        }
        // If a matching old-save entry cannot be merged without overflowing,
        // retain the incoming amount as a separate entry instead of clipping it.
        contents.add(material);
        newContentForPhaseTargets = true;
    }

    private boolean mergeCompatibleContents() {
        boolean changed = false;
        IdentityHashMap<Material, StoredMaterial> solids = new IdentityHashMap<>();
        IdentityHashMap<Material, StoredMaterial> liquids = new IdentityHashMap<>();
        for (Iterator<StoredMaterial> iterator = contents.iterator(); iterator.hasNext();) {
            StoredMaterial candidate = iterator.next();
            IdentityHashMap<Material, StoredMaterial> samePhase = candidate.molten ? liquids : solids;
            StoredMaterial retained = samePhase.get(candidate.material);
            if (retained == null) {
                samePhase.put(candidate.material, candidate);
                continue;
            }
            // SolidifyTarget is a historical display/save hint, not an
            // authoritative identity. It must not split the current melt.
            CrucibleFluidUnits.Quantity merged = CrucibleFluidUnits.merge(retained.amount,
                    retained.fluidRemainder, candidate.amount, candidate.fluidRemainder,
                    CrucibleFluidUnits.STORAGE_UNIT);
            if (merged == null) {
                // Preserve unmergeable old-save overflow; later small entries
                // can still coalesce with this candidate without losing either.
                samePhase.put(candidate.material, candidate);
                continue;
            }
            retained.amount = merged.amount;
            retained.fluidRemainder = merged.remainder;
            retained.solidifyingMaterial = retained.molten ? getSolidifyingTarget(retained.material) : null;
            iterator.remove();
            changed = true;
        }
        return changed;
    }

    private void removeEmptyContents() {
        Iterator<StoredMaterial> iterator = contents.iterator();
        while (iterator.hasNext()) {
            StoredMaterial material = iterator.next();
            if (material.amount < 0 || (material.amount == 0 && material.fluidRemainder <= 0) ||
                    material.material == null || material.material == gregtech.api.unification.material.Materials.NULL) {
                iterator.remove();
            }
        }
    }

    private long getTotalAmount() {
        long total = 0;
        for (StoredMaterial material : contents) {
            total = CrucibleTransferLogic.saturatingMaterialSum(total, material.amount);
            if (material.fluidRemainder > 0) total = CrucibleTransferLogic.saturatingMaterialSum(total, 1);
        }
        return total;
    }

    public static Material getSmeltingTarget(Material material) {
        if (material == null || material == Materials.NULL) return null;
        String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(material);
        if (GT6LiteralTargetData.isAmbiguous(mechanicsName)) return null;
        GT6LiteralTargetData.Profile literal = GT6LiteralTargetData.find(mechanicsName);
        if (literal != null) {
            if (literal.hotAmount <= 0) return null;
            if (literal.hotTargetId == literal.id) return material;
            Material target = resolveLiteralTarget(literal.hotTargetName);
            return target == null || target == Materials.NULL ? null : target;
        }
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(mechanicsName);
        if (rule != null) {
            return rule.target == null ? material : resolveMaterial(rule.target);
        }
        if (material == Materials.Obsidian || "obsidian".equals(normalizeMaterialName(mechanicsName))) {
            return Materials.Lava;
        }
        if (CrucibleSmeltingRule.hasDefaultSelfTarget(mechanicsName)) {
            return material;
        }
        if (material.hasProperty(PropertyKey.INGOT)) {
            IngotProperty property = material.getProperty(PropertyKey.INGOT);
            if (property.getSmeltingInto() != null) {
                return property.getSmeltingInto();
            }
        }
        // Only actual GT6 source identities inherit OreDictMaterial's default
        // self/U target. A CEu Material is not an OreDictMaterial: if the
        // host did not declare an ingot conversion, do not invent one.
        return mechanicsName != null && CrucibleSolidifyingRule.isSnapshotMaterial(mechanicsName) ?
                material : null;
    }

    /** Same integer conversion for phase changes and recipe display; negative means overflow. */
    public static long getSmeltingOutputAmount(Material source, long amount) {
        if (source == null || source == Materials.NULL || amount <= 0) return 0;
        String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(source);
        if (GT6LiteralTargetData.isAmbiguous(mechanicsName)) return 0;
        Material target = getSmeltingTarget(source);
        if (target == null || target == Materials.NULL) return 0;
        GT6LiteralTargetData.Profile literal = GT6LiteralTargetData.find(mechanicsName);
        CrucibleSmeltingRule rule = CrucibleSmeltingRule.find(mechanicsName);
        long converted;
        if (literal != null) {
            java.math.BigInteger result = java.math.BigInteger.valueOf(amount)
                    .multiply(java.math.BigInteger.valueOf(literal.hotAmount))
                    .divide(java.math.BigInteger.valueOf(GT6LiteralTargetData.GT6_U));
            converted = result.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? -1 : result.longValue();
        } else {
            converted = rule == null ? amount : rule.convert(amount);
        }
        if (converted <= 0) return converted;
        if (isLavaMaterial(target) && (source == Materials.Obsidian ||
                (literal != null && "lava".equals(normalizeMaterialName(literal.hotTargetName))) ||
                (rule != null && "lava".equals(rule.target)))) {
            long lavaPerUnit = CrucibleFluidUnits.materialAmount(LAVA_OBSIDIAN_MILLIBUCKETS, GTValues.L);
            long whole = converted / GTValues.M;
            long partial = converted % GTValues.M * lavaPerUnit / GTValues.M;
            if (whole > (Long.MAX_VALUE - partial) / lavaPerUnit) return -1;
            converted = whole * lavaPerUnit + partial;
        }
        return converted;
    }

    /**
     * Lossless fluid projection of a phase conversion for displays such as JEI.
     * Unlike {@link #getSmeltingOutputAmount(Material, long)}, this keeps the
     * shared material remainder until the final millibucket conversion, so a
     * fractional GT6 target ratio is not rounded to zero prematurely.
     */
    public static int getSmeltingOutputFluidAmount(Material source, Material target, long amount) {
        FluidStack output = getSmeltingOutputFluidStack(source, target, amount);
        return output == null ? 0 : output.amount;
    }

    /** Millibuckets per internal material unit for the registered liquid phase. */
    public static int getLiquidFluidUnit(Material material) {
        return CrucibleFluidInput.liquidUnit(material);
    }

    /** Builds a stack from the exact liquid phase used by the crucible fluid handler. */
    @Nullable
    public static FluidStack getLiquidFluidStack(Material material, int amount) {
        return CrucibleFluidInput.liquidStack(material, amount);
    }

    /** Lossless and identity-preserving liquid projection of a phase conversion. */
    @Nullable
    public static FluidStack getSmeltingOutputFluidStack(Material source, Material target, long amount) {
        if (source == null || target == null || target == Materials.NULL || amount <= 0L ||
                !hasMaterialMeltingTarget(source) ||
                getSmeltingTarget(source) != target) return null;
        int fluidUnit = CrucibleFluidInput.liquidUnit(target);
        if (fluidUnit <= 0) return null;
        CrucibleFluidUnits.Quantity converted = getSmeltingStoredQuantity(source, target, amount, 0);
        if (converted == null || (converted.amount == 0L && converted.remainder == 0)) return null;
        int fluidAmount = CrucibleFluidUnits.storedFluidVolume(converted.amount, converted.remainder, fluidUnit);
        return CrucibleFluidInput.liquidStack(target, fluidAmount);
    }

    public static boolean hasMaterialMeltingTarget(Material material) {
        if (material == null || material == Materials.NULL) return false;
        String name = GT6MaterialIdentity.canonicalMechanicsName(material);
        if (GT6LiteralTargetData.isAmbiguous(name)) return false;
        Material target = getSmeltingTarget(material);
        if (target == null || target == Materials.NULL) return false;

        // A valid GT6 target is the source of truth, including its default
        // one-U self target. Do not infer that relation from a fluid binding.
        GT6LiteralTargetData.Profile literal = GT6LiteralTargetData.find(name);
        if (literal != null) return literal.hotAmount > 0;
        if (CrucibleSmeltingRule.find(name) != null) {
            return CrucibleSmeltingRule.hasMeltingFlag(name);
        }
        if (CrucibleSmeltingRule.hasDefaultSelfTarget(name)) return target == material;
        // GT6's constructor default is self/U even when the material does not
        // carry the processing MELTING flag. Restrict this to known GT6 source
        // identities; host materials must declare their own conversion below.
        if (target == material && CrucibleSolidifyingRule.isSnapshotMaterial(name)) return true;

        // For non-GT6 host materials, use CEu's actual ingot smelting target.
        // IngotProperty is the host's explicit smelting contract; merely
        // having an unrelated Forge fluid is not evidence of a melt target.
        if (!material.hasProperty(PropertyKey.INGOT)) return false;
        IngotProperty property = material.getProperty(PropertyKey.INGOT);
        Material hostTarget = property.getSmeltingInto();
        return hostTarget != null && hostTarget != Materials.NULL;
    }

    private int getMeltingTemperature(Material material) {
        return CrucibleMaterialPhaseData.meltingPoint(material);
    }

    private static int getDefaultMaxTemperature(int tier) {
        return 1800 + Math.max(0, tier) * 300;
    }

    private StoredMaterial getDrainableMaterial(@Nullable FluidStack requestedFluid) {
        // GT6 container extraction selects the surface from ALL contents,
        // then checks liquid eligibility. A floating solid/internal melt must
        // not disappear from this selection just because Forge cannot drain it.
        StoredMaterial lightest = getSurfaceMaterial(true);
        if (lightest == null || !lightest.molten ||
                CrucibleFluidInput.liquidUnit(lightest.material) <= 0 || toFluidAmount(lightest) <= 0) return null;
        FluidStack liquid = CrucibleFluidInput.liquidStack(lightest.material, 1);
        if (liquid == null) return null;
        if (!CrucibleTransferLogic.canExtractFluidAtTemperature(temperature,
                getMeltingTemperature(lightest.material), liquid.getFluid().getTemperature(liquid))) return null;
        // Select BEFORE the requested type filter, so typed drain cannot
        // reach a heavier unreported layer either.
        if (requestedFluid != null &&
                !requestedFluid.isFluidEqual(liquid)) {
            return null;
        }
        return lightest;
    }

    @Override
    public boolean isMoldInputSide(@Nullable EnumFacing side) {
        return side == EnumFacing.UP;
    }

    @Override
    public long getMoldMaxTemperature() {
        return getMaxTemperature();
    }

    @Override
    public long getMoldRequiredMaterialUnits(@Nullable Material material) {
        // GT6 Smeltery returns one internal unit here, not one ingot. This
        // hint must not truncate the full offer or its one-M fallback below.
        return 1L;
    }

    @Override
    public long fillMold(Material material, long amount, long incomingTemperature,
                         @Nullable EnumFacing side, boolean simulate) {
        if (!isMoldInputSide(side) || material == null || material == Materials.NULL || amount <= 0L) return 0L;
        // GT6 Smeltery accepts the complete offered stack first, then tries
        // exactly one U (one host M), never an arbitrary partial free space.
        // Reuse atomic intake, including shell-weight mixing and arrival
        // phase conversion. Simulation only prepares local content objects.
        if (addMaterialBatch(Collections.singletonList(new MaterialStack(material, amount)),
                incomingTemperature, simulate)) return amount;
        if (amount > GTValues.M && addMaterialBatch(
                Collections.singletonList(new MaterialStack(material, GTValues.M)),
                incomingTemperature, simulate)) return GTValues.M;
        return 0L;
    }

    public long fillMoldAtSide(ICrucibleMold mold, @Nullable EnumFacing sideOfCrucible,
                               @Nullable EnumFacing sideOfMold) {
        if (mold == null || mold == this || !mold.isMoldInputSide(sideOfMold)) {
            return 0L;
        }
        for (StoredMaterial material : contents) {
            if (!isPourableMaterial(material)) {
                continue;
            }
            long filled = CrucibleTransferLogic.transferToMold(material.amount,
                    amount -> mold.fillMold(material.material, amount, temperature, sideOfMold, true),
                    amount -> mold.fillMold(material.material, amount, temperature, sideOfMold, false));
            if (filled <= 0L) {
                continue;
            }
            material.amount -= filled;
            removeEmptyContents();
            markDirty();
            refreshDisplayState();
            return filled;
        }
        return 0L;
    }

    private boolean isPourableMaterial(StoredMaterial material) {
        return material != null && material.material != null &&
                CrucibleTransferLogic.canPourIntoMold(material.amount, temperature,
                        getMeltingTemperature(material.material),
                        getSmeltingTarget(material.material) == material.material);
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
        displayHeight = CrucibleTransferLogic.displayHeight(total, CAPACITY);

        StoredMaterial displayMaterial = getDisplayedMaterial();
        if (displayMaterial == null) {
            displayMaterialName = EMPTY_DISPLAY_MATERIAL;
            displayMolten = false;
        } else {
            displayMaterialName = getMaterialRegistryName(displayMaterial.material);
            boolean retainedLavaFluid = isLavaMaterial(displayMaterial.material) &&
                    temperature < getMeltingTemperature(displayMaterial.material) &&
                    CrucibleTransferLogic.obsidianUnitsForLava(CrucibleFluidUnits.storedFluidVolume(
                            displayMaterial.amount, displayMaterial.fluidRemainder, GTValues.L)) == 0;
            displayMolten = retainedLavaFluid ||
                    (displayMaterial.molten && temperature >= getMeltingTemperature(displayMaterial.material));
        }
        meltDownWarning = CrucibleShellVisual.isWarning(temperature, maxTemperature);
        displayPendingItems = pendingItemCount();
    }

    private boolean isDisplayStateChanged() {
        return displayHeight != oldDisplayHeight ||
                displayMolten != oldDisplayMolten ||
                meltDownWarning != oldMeltDownWarning ||
                displayPendingItems != oldDisplayPendingItems ||
                !displayMaterialName.equals(oldDisplayMaterialName);
    }

    private void rememberDisplayState() {
        oldDisplayHeight = displayHeight;
        oldDisplayMaterialName = displayMaterialName;
        oldDisplayMolten = displayMolten;
        oldMeltDownWarning = meltDownWarning;
        oldDisplayPendingItems = displayPendingItems;
    }

    @Nullable
    private StoredMaterial getDisplayedMaterial() {
        return getSurfaceMaterial(false);
    }

    private StoredMaterial getSurfaceMaterial(boolean skipSubMillibucketFluid) {
        return CrucibleSurfaceSelection.lightest(contents, material -> {
            if (material.material == null || material.material == Materials.NULL ||
                    (material.amount <= 0 && material.fluidRemainder <= 0)) return false;
            // Preserve the documented Forge extension: a liquid remainder
            // smaller than 1 mB is retained, but cannot lock the fluid API.
            // Do not use this exception to skip solid or unprocessed contents.
            return !(skipSubMillibucketFluid && material.molten &&
                    CrucibleFluidInput.liquidUnit(material.material) > 0 &&
                    temperature >= getMeltingTemperature(material.material) &&
                    toFluidAmount(material) <= 0);
        }, material -> getMaterialDensityKgPerCubicMeter(material.material));
    }

    @Nullable
    private Material getDisplayedClientMaterial() {
        return resolveMaterial(displayMaterialName);
    }

    private static String getMaterialRegistryName(Material material) {
        return material == null ? EMPTY_DISPLAY_MATERIAL : material.getRegistryName();
    }

    @Nullable
    public static Material resolveMaterial(@Nullable String materialName) {
        materialName = GT6MaterialIdentity.migrateOwnRegistryName(materialName);
        String retiredPath = GT6MaterialCompatibility.retiredPath(materialName);
        if (retiredPath != null) {
            // Known retired addon identities may reuse an existing external material, never create one.
            return GT6MaterialCompatibility.findExternal(retiredPath);
        }
        if (!GT6MaterialIdentity.allowsRegistryNamespace(materialName, materialName)) {
            return null;
        }
        int namespaceSeparator = materialName.indexOf(':');
        if (namespaceSeparator >= 0) {
            // Explicit identities stay exact. In particular, never let CEu's
            // fallback from an absent namespace resolve a different mod's material.
            Material material = GregTechAPI.materialManager.getMaterial(materialName);
            return material != null && materialName.equals(material.getRegistryName()) ? material : null;
        }

        // Old saves and native GT6 data often store only Material#getName(),
        // losing the registry namespace. Route those through the shared resolver
        // so gtqtcore -> gregtech -> unique external priority and ambiguity
        // rejection are consistent with target conversion and retired identities.
        Material material = GT6MaterialCompatibility.findExternal(materialName);
        if (material != null && material != Materials.NULL) return material;

        // GT6 spelling aliases (for example Aluminum/Aluminium) are normalized
        // only after the exact host lookup. Do not scan registries in iteration
        // order: duplicate same-name third-party identities must stay unresolved.
        String normalizedName = normalizeMaterialName(materialName);
        if (!normalizedName.isEmpty() && !normalizedName.equals(normalizeRegistryPath(materialName))) {
            material = GT6MaterialCompatibility.findExternal(normalizedName);
            if (material != null && material != Materials.NULL) return material;
        }
        // MT.java:1094 names BandedIron as an alias of Hematite, not Magnetite.
        // Only an unqualified GT6 name may use this host binding. Explicit
        // foreign namespaces must not fall through to a different registry.
        if (materialName.indexOf(':') < 0 && "hematite".equals(normalizedName)) {
            return Materials.BandedIron;
        }
        return null;
    }

    /** GT6 targets must bind to an existing external identity without an
     * arbitrary registry-order match. Only the explicitly verified Hematite
     * alias falls back to CEu's BandedIron identity.
     */
    @Nullable
    private static Material resolveLiteralTarget(String materialName) {
        Material target = GT6MaterialCompatibility.findExternal(materialName);
        if (target != null && target != Materials.NULL) return target;
        String canonicalName = normalizeMaterialName(materialName);
        if (!canonicalName.isEmpty() && !canonicalName.equals(normalizeRegistryPath(materialName))) {
            target = GT6MaterialCompatibility.findExternal(canonicalName);
            if (target != null && target != Materials.NULL) return target;
        }
        if ("hematite".equals(normalizeMaterialName(materialName)) &&
                Materials.BandedIron != null && Materials.BandedIron != Materials.NULL) {
            return Materials.BandedIron;
        }
        return null;
    }

    private static String normalizeRegistryPath(String materialName) {
        if (materialName == null) return "";
        int separator = materialName.lastIndexOf(':');
        String path = separator < 0 ? materialName : materialName.substring(separator + 1);
        return path.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String normalizeMaterialName(String name) {
        if (name == null) return "";
        int namespaceSeparator = name.lastIndexOf(':');
        String path = namespaceSeparator >= 0 ? name.substring(namespaceSeparator + 1) : name;
        String normalized = path.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        // GT6 uses British spelling in several material identifiers; CEu
        // integrations may register the same canonical material as aluminum.
        return GT6MaterialIdentity.canonicalOreName(GT6MaterialIdentity.canonicalAlloyName(
                GT6MaterialIdentity.canonicalElementName(GT6MaterialIdentity.canonicalCompoundName(
                        normalized.replace("aluminum", "aluminium")))));
    }

    // The read-only GT6 rules already operate on these exact source names.
    // Keep an external registered identity; singleton-only tests would bypass
    // water boiling and the project's bucket-quantized lava conversion.
    private static boolean isWaterMaterial(Material material) {
        String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(material);
        return material == Materials.Water || "water".equals(normalizeMaterialName(mechanicsName));
    }

    private static boolean isLavaMaterial(Material material) {
        String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(material);
        return material == Materials.Lava || "lava".equals(normalizeMaterialName(mechanicsName));
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

    public int getPendingItemCount() {
        return getWorld() != null && getWorld().isRemote ? displayPendingItems : pendingItemCount();
    }

    public int getPendingItemCapacity() {
        return PENDING_ITEM_CAPACITY;
    }

    @NotNull
    public List<CrucibleContentInfo> getTopContents() {
        List<CrucibleContentInfo> result = new ArrayList<>();
        for (StoredMaterial material : contents) {
            result.add(new CrucibleContentInfo(
                    getDisplayStack(material),
                    material.material.getLocalizedName(),
                    toFluidAmount(material),
                    material.molten));
        }
        return result;
    }

    private ItemStack getDisplayStack(StoredMaterial material) {
        ItemStack stack = OreDictUnifier.getIngotOrDust(material.material, Math.max(GTValues.M, material.amount));
        if (!stack.isEmpty()) {
            stack = stack.copy();
            long units = material.amount / GTValues.M + (material.amount % GTValues.M > 0 ? 1 : 0);
            stack.setCount((int) Math.min(64L, Math.max(1L, units)));
        }
        return stack;
    }

    private int toFluidAmount(long materialAmount) {
        return CrucibleTransferLogic.materialFluidAmount(materialAmount, GTValues.M, GTValues.L);
    }

    private int toFluidAmount(Material material, long materialAmount) {
        return CrucibleTransferLogic.materialFluidAmount(materialAmount, GTValues.M,
                CrucibleFluidInput.liquidUnit(material));
    }

    private int toFluidAmount(StoredMaterial material) {
        return CrucibleFluidUnits.storedFluidVolume(material.amount, material.fluidRemainder,
                CrucibleFluidInput.liquidUnit(material.material));
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            return true;
        }

        ItemStack heldItem = player.getHeldItem(hand);
        if (facing == EnumFacing.UP && heldItem.isEmpty() && tryTakeInputItem(player)) {
            return true;
        }
        if (facing == EnumFacing.UP && tryScrapeSolidContent(player, hand, heldItem)) {
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
            if (pendingItems.isEmpty() && addMaterialFromItem(oneItem)) {
                heldItem.shrink(1);
                return true;
            }
            if (enqueuePendingItem(oneItem)) {
                heldItem.shrink(1);
                return true;
            }
        } else {
            player.sendStatusMessage(new TextComponentTranslation(
                    "gt6addition.machine.hu_crucible.status.temperature", temperature, maxTemperature), true);
            return true;
        }
        return super.onRightClick(player, hand, facing, hitResult);
    }

    private boolean tryTakeInputItem(EntityPlayer player) {
        ItemStack extracted = importItems.extractItem(0, 1, false);
        if (extracted.isEmpty() && !pendingItems.isEmpty()) {
            ItemStack first = pendingItems.get(0);
            extracted = first.splitStack(1);
            if (first.isEmpty()) {
                pendingItems.remove(0);
            }
        }
        if (extracted.isEmpty()) {
            return false;
        }
        ItemHandlerHelper.giveItemToPlayer(player, extracted);
        applyContactTemperature(player);
        markDirty();
        refreshDisplayState();
        return true;
    }

    private boolean tryScrapeSolidContent(EntityPlayer player, EnumHand hand, ItemStack heldItem) {
        boolean emptyHandScrape = heldItem.isEmpty();
        boolean shovelScrape = !heldItem.isEmpty() && heldItem.getItem().getToolClasses(heldItem).contains("shovel");

        StoredMaterial material = getScrapableMaterial();
        if (material == null) {
            if (!emptyHandScrape && !shovelScrape) return false;
            player.sendStatusMessage(new TextComponentTranslation(
                    "gt6addition.machine.hu_crucible.status.no_solid_content"), true);
            return true;
        }

        ScrapeResult result = createScrapeResult(material, shovelScrape);
        if (result == null) {
            if (!emptyHandScrape && !shovelScrape) return false;
            player.sendStatusMessage(new TextComponentTranslation(
                    "gt6addition.machine.hu_crucible.status.residue_too_small"), true);
            if (!shovelScrape) applyContactTemperature(player);
            return true;
        }

        int recoveredCount = result.stack.getCount();
        if (shovelScrape) {
            // Match GT6 ST.add: no partial insertion and no overflow dropped
            // into the crucible (where it could be immediately reimported).
            if (!CrucibleSolidRecovery.insertWholeStack(player.inventory.mainInventory,
                    player.inventory.currentItem, player.inventory.getInventoryStackLimit(), result.stack)) {
                player.sendStatusMessage(new TextComponentTranslation(
                        "gt6addition.machine.hu_crucible.status.recovery_full"), true);
                return true;
            }
        } else if (emptyHandScrape) {
            player.setHeldItem(hand, result.stack);
        } else {
            if (!ItemHandlerHelper.canItemStacksStack(heldItem, result.stack)) return false;
            // Consume the interaction even at the stack limit, so a full
            // recovery stack is not accidentally submitted as new input.
            if (heldItem.getCount() >= heldItem.getMaxStackSize()) {
                player.sendStatusMessage(new TextComponentTranslation(
                        "gt6addition.machine.hu_crucible.status.recovery_full"), true);
                return true;
            }
            heldItem.grow(1);
        }
        material.amount -= result.materialAmount;
        removeEmptyContents();
        markDirty();
        refreshDisplayState();
        player.inventory.markDirty();
        // UT.Entities.exhaust applies mining fatigue to its 0.1/item cost.
        int fatigue = player.isPotionActive(MobEffects.MINING_FATIGUE) ?
                2 + Math.min(63, Math.max(0, player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier())) : 1;
        player.addExhaustion(0.1F * recoveredCount * fatigue);
        if (shovelScrape && !player.capabilities.isCreativeMode) {
            ToolHelper.damageItem(heldItem, player, CrucibleSolidRecovery.shovelDamage(recoveredCount));
        }
        // GT6 only applies direct contact damage to hand recovery, not shovels.
        if (!shovelScrape) applyContactTemperature(player);
        return true;
    }

    @Nullable
    private StoredMaterial getScrapableMaterial() {
        StoredMaterial lightest = getSurfaceMaterial(false);
        // A liquid surface covers solids below it. An unmeltable solid remains
        // recoverable even above its nominal melting temperature.
        return lightest == null || lightest.molten ? null : lightest;
    }

    @Nullable
    private ScrapeResult createScrapeResult(StoredMaterial material, boolean shovel) {
        String mechanicsName = GT6MaterialIdentity.canonicalMechanicsName(material.material);
        if (material.material == Materials.Obsidian ||
                "obsidian".equals(normalizeMaterialName(mechanicsName))) {
            int blocks = CrucibleSolidRecovery.outputCount(material.amount, GTValues.M, 64, shovel);
            return blocks <= 0 ? null : new ScrapeResult(new ItemStack(Blocks.OBSIDIAN, blocks),
                    blocks * (long) GTValues.M);
        }
        if (material.amount < SCRAP_MATERIAL_AMOUNT) {
            return null;
        }
        int count = CrucibleSolidRecovery.outputCount(material.amount, SCRAP_MATERIAL_AMOUNT,
                GT6AdditionOrePrefixes.SCRAP_GT.maxStackSize, shovel);
        ItemStack output = GT6AdditionOrePrefixes.SCRAP_GT.getItemForm(material.material, count);
        return output.isEmpty() ? null : new ScrapeResult(output, SCRAP_MATERIAL_AMOUNT * count);
    }

    private double getMaterialDensityKgPerCubicMeter(Material material) {
        return getGt6MaterialDensityKgPerCubicMeter(material,
                Collections.newSetFromMap(new IdentityHashMap<Material, Boolean>()));
    }

    private void applyContactTemperature(EntityPlayer player) {
        applyContactTemperatureHazard(player,
                CrucibleTransferLogic.contactTemperatureDamage(temperature, 5.0F));
    }

    private void applyContactTemperatureHazard(EntityLivingBase entity, float damage) {
        if (temperature < 260L) {
            if (damage <= 0 || !entity.isEntityAlive()) return;
            // GT6 frost damage has no Blaze/fire-resistance exemption. Use the
            // host's frost armor resistance, not its integer 273 K pipe formula.
            Hazard.FROST.applyTo(entity, damage);
        } else {
            applyHeatHazard(entity, damage);
        }
    }

    private void applyHeatHazard(EntityLivingBase entity, float damage) {
        if (damage <= 0 || !entity.isEntityAlive() || entity instanceof EntityBlaze ||
                entity.isPotionActive(MobEffects.FIRE_RESISTANCE)) return;
        // Use the host's float-valued hazard path: its temperature/pipe helper
        // truncates damage and uses a different formula from GT6 contact/steam.
        Hazard.HEAT.applyTo(entity, damage);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityHandler.CAPABILITY_CRUCIBLE_ENERGY) return true;
        if (capability == GregtechCapabilities.CAPABILITY_HEAT_CONTAINER) return true;
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
        if (capability == CapabilityHandler.CAPABILITY_CRUCIBLE_ENERGY) {
            return CapabilityHandler.CAPABILITY_CRUCIBLE_ENERGY.cast(this);
        }
        if (capability == GregtechCapabilities.CAPABILITY_HEAT_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_HEAT_CONTAINER.cast(
                    nativeHeatReceivers[facing == null ? 6 : facing.getIndex()]);
        }
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
        return !destroyedByHeat && temperature < HOT_BREAK_TEMPERATURE && super.shouldDropWhenDestroyed();
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        // CEu clears only import/export handlers. Pending items are a separate
        // inventory and must be released here before the tile is removed.
        itemBuffer.addAll(CrucibleItemIntake.takePendingDrops(pendingItems));
        markDirty();
    }

    /** Supply the player context CEu's parameterless onRemoval callback lacks. */
    public boolean performPlayerRemoval(@Nullable EntityPlayer player, @NotNull BooleanSupplier removal) {
        boolean previous = creativePlayerRemoval;
        boolean previouslyDestroyedByHeat = destroyedByHeat;
        creativePlayerRemoval = player != null && player.capabilities.isCreativeMode;
        try {
            boolean removed = removal.getAsBoolean();
            if (removed) return true;
            // Chunk.setBlockState first writes air and invokes breakBlock.
            // Our onRemoval then replaces that air with lava. Chunk correctly
            // refuses to report its original air write as successful, although
            // the player removal has completed. GT6 returns the lava write's
            // success directly. Do not mistake a cancelled/failed call for it.
            if (!previouslyDestroyedByHeat && destroyedByHeat && getWorld() != null && !getWorld().isRemote &&
                    getWorld().getTileEntity(getPos()) == null) {
                IBlockState replacement = getWorld().getBlockState(getPos());
                return replacement.getBlock() == Blocks.FLOWING_LAVA && replacement.getValue(BlockLiquid.LEVEL) == 1;
            }
            return false;
        } finally {
            creativePlayerRemoval = previous;
        }
    }

    @Override
    public void onRemoval() {
        if (getWorld() != null && !getWorld().isRemote && !wasExploded() &&
                !creativePlayerRemoval && temperature >= HOT_BREAK_TEMPERATURE) {
            destroyedByHeat = true;
            playHazardFizz();
            releaseOverheatEffects(temperature, 1.0F);
            contents.clear();
            temperature = getAmbientTemperature();
            getWorld().setBlockState(getPos(), Blocks.FLOWING_LAVA.getDefaultState()
                    .withProperty(BlockLiquid.LEVEL, 1), 3);
        }
        // GT6 breakBlock discards processed materials in every game mode;
        // unprocessed input/cache drops are handled by clearMachineInventory.
        contents.clear();
        super.onRemoval();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] shellPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getShellRenderColor())));
        boolean emissive = CrucibleShellVisual.isEmissive(meltDownWarning, vesselMaterial);
        boolean previousLighting = renderState.computeLighting;
        int previousBrightness = renderState.brightness;
        if (emissive) {
            // GT6's glow is max brightness without AO, not a world light source
            // or a bloom-only layer. Both CCL lighting operations skip load when
            // computeLighting is false; the explicit lightmap remains active.
            renderState.computeLighting = false;
            shellPipeline = ArrayUtils.add(shellPipeline, new LightMapOperation(240, 240));
        }
        try {
            for (int pass = 0; pass < SHELL_PARTS.length; pass++) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    if (CrucibleShellVisual.shouldRenderFace(pass, face)) {
                        getBaseRenderer().renderSided(face, SHELL_PARTS[pass], renderState, shellPipeline, translation);
                    }
                }
            }
        } finally {
            // The shared render state is also used by contents and other blocks.
            renderState.computeLighting = previousLighting;
            renderState.brightness = previousBrightness;
        }
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
        collisionList.add(new IndexedCuboid6(null, BOTTOM));
        collisionList.add(new IndexedCuboid6(null, WALL_X_NEG));
        collisionList.add(new IndexedCuboid6(null, WALL_X_POS));
        collisionList.add(new IndexedCuboid6(null, WALL_Z_NEG));
        collisionList.add(new IndexedCuboid6(null, WALL_Z_POS));
    }

    @SideOnly(Side.CLIENT)
    private void renderDisplayedContent(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (displayHeight > 0) {
            Material material = getDisplayedClientMaterial();
            if (material != null) {
                double top = WALL_SIZE + displayHeight / CONTENT_HEIGHT_SCALE;
                Cuboid6 contentBounds = new Cuboid6(WALL_SIZE, WALL_SIZE, WALL_SIZE,
                        1.0D - WALL_SIZE, top, 1.0D - WALL_SIZE);
                IVertexOperation[] contentPipeline = ArrayUtils.add(pipeline,
                        new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getContentRenderColor(material))));
                Textures.renderFace(renderState, translation, contentPipeline, EnumFacing.UP, contentBounds,
                        getContentSprite(material), BlockRenderLayer.CUTOUT_MIPPED);
            }
        }

    }

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite getContentSprite(Material material) {
        return CrucibleContentRenderer.sprite(material, displayMolten);
    }

    private int getContentRenderColor(Material material) {
        return CrucibleContentVisual.color(material, displayMolten);
    }

    private int getShellRenderColor() {
        return CrucibleShellVisual.color(color, meltDownWarning);
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
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.safe_input"));
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.automatic_rate"));
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.pouring_input"));
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.hazard"));
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible.tooltip.entity_recycling"));
        tooltip.add(I18n.format("gt6addition.accept_facing", I18n.format("gt6addition.all_sides")));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong(NBT_TEMPERATURE, temperature);
        data.setLong(NBT_OLD_TEMPERATURE, oldTemperature);
        data.setLong(NBT_STORED_HEAT, storedHeat);
        data.setInteger(NBT_WATER_UNIT_VERSION, 1);
        data.setInteger(NBT_FLUID_QUANTITY_VERSION, 2);
        data.removeTag(NBT_SPECIAL_FLUID_AMOUNT);
        data.removeTag(NBT_SPECIAL_FLUID);
        NBTTagList contentList = new NBTTagList();
        for (StoredMaterial material : contents) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(NBT_MATERIAL, getMaterialRegistryName(material.material));
            tag.setLong(NBT_AMOUNT, material.amount);
            tag.setInteger(NBT_FLUID_REMAINDER, material.fluidRemainder);
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
        storedHeat = data.getLong(NBT_STORED_HEAT);
        thermalCooldown = HEAT_COOLDOWN_TICKS;
        Fluid legacySpecialFluid = data.hasKey(NBT_SPECIAL_FLUID) ?
                FluidRegistry.getFluid(data.getString(NBT_SPECIAL_FLUID)) : null;
        int legacySpecialFluidAmount = Math.max(0, data.getInteger(NBT_SPECIAL_FLUID_AMOUNT));
        contents.clear();
        pendingItems.clear();
        NBTTagList pendingList = data.getTagList(NBT_PENDING_ITEMS, Constants.NBT.TAG_COMPOUND);
        // Admission is capped at 64, but loading must not delete old overflow.
        // It drains in its original order and blocks new queued input meanwhile.
        for (int i = 0; i < pendingList.tagCount(); i++) {
            ItemStack stack = new ItemStack(pendingList.getCompoundTagAt(i));
            if (stack.isEmpty()) continue;
            pendingItems.add(stack);
        }
        NBTTagList contentList = data.getTagList(NBT_CONTENTS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < contentList.tagCount(); i++) {
            NBTTagCompound tag = contentList.getCompoundTagAt(i);
            Material material = resolveMaterial(tag.getString(NBT_MATERIAL));
            if (material != null && material != gregtech.api.unification.material.Materials.NULL) {
                boolean molten = tag.getBoolean(NBT_MOLTEN);
                Material solidifyingMaterial = molten ? getSolidifyingTarget(material) : material;
                long amount = tag.getLong(NBT_AMOUNT);
                int remainder = tag.getInteger(NBT_FLUID_REMAINDER);
                int quantityVersion = data.getInteger(NBT_FLUID_QUANTITY_VERSION);
                int unit = quantityVersion >= 2 ? CrucibleFluidUnits.STORAGE_UNIT :
                        quantityVersion >= 1 ? CrucibleFluidUnits.LEGACY_STORAGE_UNIT :
                                CrucibleFluidUnits.legacyFluidUnit(material.getName());
                if (remainder < 0 || remainder >= unit) remainder = 0;
                if (isWaterMaterial(material) && data.getInteger(NBT_WATER_UNIT_VERSION) < 1) {
                    CrucibleFluidUnits.Quantity migrated = CrucibleFluidUnits.exactLegacyWaterAmount(amount);
                    amount = migrated.amount;
                    remainder = migrated.remainder;
                    unit = 1000;
                }
                if (unit != CrucibleFluidUnits.STORAGE_UNIT) {
                    CrucibleFluidUnits.Quantity migrated = CrucibleFluidUnits.migrateStoredQuantity(amount, remainder, unit);
                    if (migrated == null) continue;
                    amount = migrated.amount;
                    remainder = migrated.remainder;
                }
                StoredMaterial loaded = new StoredMaterial(material, amount, molten, solidifyingMaterial);
                loaded.fluidRemainder = remainder;
                contents.add(loaded);
            }
        }
        migrateLegacySpecialFluid(legacySpecialFluid, legacySpecialFluidAmount);
        removeEmptyContents();
        calculateDisplayState();
        rememberDisplayState();
    }

    private void migrateLegacySpecialFluid(@Nullable Fluid fluid, int fluidAmount) {
        if (fluid == null || fluidAmount <= 0) {
            return;
        }
        Material material;
        if (fluid == FluidRegistry.WATER) {
            material = Materials.Water;
        } else if (fluid == FluidRegistry.LAVA) {
            material = Materials.Lava;
        } else {
            return;
        }
        // Historical special slots have known water/lava units, independent
        // of today's host phase bindings. Even an ambiguous host registration
        // must not turn loading existing inventory into a null dereference.
        CrucibleFluidUnits.Quantity exact = CrucibleFluidUnits.storedFluidAmount(fluidAmount,
                CrucibleFluidUnits.fluidUnit(material.getName()));
        if (exact == null) return;
        StoredMaterial migrated = new StoredMaterial(material, exact.amount, false,
                getSolidifyingTarget(material));
        migrated.fluidRemainder = exact.remainder;
        if (isWaterMaterial(material)) {
            if (CrucibleTransferLogic.shouldFreezeWater(temperature)) {
                solidify(migrated);
            } else {
                migrated.molten = true;
            }
        } else if (isLavaMaterial(material)) {
            migrated.molten = temperature >= getMeltingTemperature(material);
        } else if (temperature >= getMeltingTemperature(material)) {
            melt(migrated);
        } else {
            solidify(migrated);
        }
        // Keep legacy contents even if their sum exceeds today's shared capacity;
        // the overflow remains drainable and blocks further input until reduced.
        addStoredMaterial(migrated);
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
        buf.writeVarInt(displayPendingItems);
    }

    private void readDisplayState(PacketBuffer buf) {
        displayHeight = buf.readVarInt();
        displayMaterialName = buf.readString(Short.MAX_VALUE);
        displayMolten = buf.readBoolean();
        meltDownWarning = buf.readBoolean();
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
        private int fluidRemainder;
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
            FluidStack content = material == null ? null :
                    CrucibleFluidInput.liquidStack(material.material, toFluidAmount(material));
            long sharedSpace = Math.max(0L, CAPACITY - getTotalAmount());
            int availableSpace = material == null || CrucibleFluidInput.liquidUnit(material.material) <= 0 ?
                    toFluidAmount(sharedSpace) : toFluidAmount(material.material, sharedSpace);
            long reportedCapacity = (content == null ? 0L : content.amount) + availableSpace;
            return new IFluidTankProperties[]{
                    new FluidTankProperties(content,
                            (int) Math.min(Integer.MAX_VALUE, reportedCapacity),
                            // This is a projection in one fluid's mB units.
                            // A zero rounded projection can still hold another
                            // fluid (e.g. 1 mB water, but less than 1 mB metal).
                            // Type-specific acceptance below is authoritative.
                            sharedSpace > 0L, content != null && content.amount > 0) {
                        @Override
                        public boolean canFillFluidType(FluidStack fluidStack) {
                            if (fluidStack == null) return false;
                            FluidStack probe = fluidStack.copy();
                            probe.amount = 1;
                            return addMaterialFromFluid(probe, false) > 0;
                        }

                        @Override
                        public boolean canDrainFluidType(FluidStack fluidStack) {
                            if (fluidStack == null) return false;
                            StoredMaterial candidate = getDrainableMaterial(fluidStack);
                            return candidate != null && toFluidAmount(candidate) > 0;
                        }
                    }
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
            return material == null ? null : drainMaterial(material, maxDrain, doDrain);
        }

        private FluidStack drainMaterial(StoredMaterial material, int maxDrain, boolean doDrain) {
            int availableFluid = toFluidAmount(material);
            int drainedFluid = Math.min(maxDrain, availableFluid);
            if (drainedFluid <= 0) {
                return null;
            }
            FluidStack result = CrucibleFluidInput.liquidStack(material.material, drainedFluid);
            if (result == null) return null;
            if (doDrain) {
                CrucibleFluidUnits.Quantity remaining = CrucibleFluidUnits.drainStored(material.amount,
                        material.fluidRemainder, drainedFluid,
                        CrucibleFluidInput.liquidUnit(material.material));
                if (remaining == null) return null;
                material.amount = remaining.amount;
                material.fluidRemainder = remaining.remainder;
                removeEmptyContents();
                markDirty();
                refreshDisplayState();
            }
            return result;
        }
    }
}
