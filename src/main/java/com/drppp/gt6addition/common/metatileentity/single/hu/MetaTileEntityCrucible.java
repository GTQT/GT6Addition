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
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.material.properties.IngotProperty;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MetaTileEntityCrucible extends TieredMutiEnergyMetaTileEntity implements ITemperatureProvider {

    private static final String NBT_TEMPERATURE = "Temperature";
    private static final String NBT_OLD_TEMPERATURE = "OldTemperature";
    private static final String NBT_CONTENTS = "Contents";
    private static final String NBT_MATERIAL = "Material";
    private static final String NBT_AMOUNT = "Amount";
    private static final String NBT_MOLTEN = "Molten";
    private static final int DATA_DISPLAY_STATE = 200;
    private static final long CAPACITY = 16L * GTValues.M;
    private static final long BASE_THERMAL_MASS = 20L;
    private static final int ENVIRONMENT_TEMPERATURE = 300;
    private static final int RAIN_FILL_INTERVAL = 600;
    private static final int RAIN_FILL_OFFSET = 10;
    private static final int FLAME_RANGE = 3;
    private static final int HOT_BREAK_TEMPERATURE = 1300;
    private static final int GAS_DAMAGE_TEMPERATURE = 320;
    private static final int FIRE_TEMPERATURE = 2000;
    private static final int FLAMMABLE_TEMPERATURE = 313;
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
    private final boolean acidProof;
    private final float blockHardness;
    private final float blockResistance;
    private final CrucibleFluidHandler fluidHandler = new CrucibleFluidHandler();
    private final List<StoredMaterial> contents = new ArrayList<>();
    private long temperature = ENVIRONMENT_TEMPERATURE;
    private long oldTemperature = ENVIRONMENT_TEMPERATURE;
    private boolean active;
    private int displayHeight;
    private int oldDisplayHeight = -1;
    private String displayMaterialName = EMPTY_DISPLAY_MATERIAL;
    private String oldDisplayMaterialName = null;
    private boolean displayMolten;
    private boolean oldDisplayMolten;
    private boolean meltDownWarning;
    private boolean oldMeltDownWarning;

    public MetaTileEntityCrucible(ResourceLocation metaTileEntityId, int tier, int color) {
        this(metaTileEntityId, tier, color, getDefaultMaxTemperature(tier));
    }

    public MetaTileEntityCrucible(ResourceLocation metaTileEntityId, int tier, int color, int maxTemperature) {
        this(metaTileEntityId, tier, color, maxTemperature, false, 6.0F, 6.0F);
    }

    public MetaTileEntityCrucible(ResourceLocation metaTileEntityId, int tier, int color, int maxTemperature,
                                  boolean acidProof, float blockHardness, float blockResistance) {
        super(metaTileEntityId, tier, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN});
        this.color = color;
        this.maxTemperature = Math.max(ENVIRONMENT_TEMPERATURE, maxTemperature);
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
        return new MetaTileEntityCrucible(metaTileEntityId, getTier(), color, maxTemperature,
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
        processDroppedItems();
        processInputSlot();
        if (processTemperature()) {
            return;
        }
        processAlloys();
        removeEmptyContents();
        refreshDisplayState();
        setActive(mutiEnergyProxy != null && mutiEnergyProxy.getEnergy() > 0);
    }

    private void pullHeatFromBottom() {
        if (mutiEnergyProxy == null) {
            return;
        }
        TileEntity tileEntity = getWorld().getTileEntity(getPos().down());
        if (tileEntity == null || !mutiEnergyProxy.getNearEnergyToMyself(tileEntity, EnumFacing.DOWN)) {
            mutiEnergyProxy.setEnergy(0);
            return;
        }

        int heat = mutiEnergyProxy.getEnergy();
        if (heat <= 0) {
            return;
        }
        long thermalMass = BASE_THERMAL_MASS + Math.max(1L, getTotalAmount() / GTValues.M) * 10L;
        long temperatureGain = Math.max(1L, heat / thermalMass);
        temperature += temperatureGain;
        mutiEnergyProxy.changeEnergy((int) Math.min(Integer.MAX_VALUE, temperatureGain * thermalMass));
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
        addMaterialFromFluid(rainWater, true);
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
        }
    }

    private boolean addMaterialFromItem(ItemStack stack) {
        MaterialStack materialStack = OreDictUnifier.getMaterial(stack);
        if (materialStack == null || materialStack.material == null || materialStack.amount <= 0) {
            return false;
        }
        if (getTotalAmount() + materialStack.amount > CAPACITY) {
            return false;
        }

        mixTemperature(ENVIRONMENT_TEMPERATURE, materialStack.amount);
        StoredMaterial storedMaterial = new StoredMaterial(materialStack.material, materialStack.amount, false);
        if (temperature >= getMeltingTemperature(storedMaterial.material)) {
            melt(storedMaterial);
        }
        addStoredMaterial(storedMaterial);
        markDirty();
        refreshDisplayState();
        return true;
    }

    private int addMaterialFromFluid(@Nullable FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        Material material = getMaterialFromFluid(resource);
        if (material == null) {
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
            mixTemperature(resource.getFluid().getTemperature(acceptedStack), materialAmount);
            addStoredMaterial(new StoredMaterial(material, materialAmount, true));
            markDirty();
            refreshDisplayState();
        }
        return acceptedFluid;
    }

    private void mixTemperature(long incomingTemperature, long incomingAmount) {
        long currentMass = BASE_THERMAL_MASS * GTValues.M + getTotalAmount();
        long incomingMass = Math.max(1L, incomingAmount);
        temperature = Math.max(0L,
                (temperature * currentMass + incomingTemperature * incomingMass) / (currentMass + incomingMass));
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

    private boolean processTemperature() {
        oldTemperature = temperature;
        if (mutiEnergyProxy == null || mutiEnergyProxy.getEnergy() <= 0) {
            if (getOffsetTimer() % 20 == 0) {
                if (temperature > ENVIRONMENT_TEMPERATURE) {
                    temperature--;
                } else if (temperature < ENVIRONMENT_TEMPERATURE) {
                    temperature++;
                }
            }
        }

        boolean changed = false;
        for (StoredMaterial material : contents) {
            if (!material.molten && temperature >= getMeltingTemperature(material.material)) {
                melt(material);
                changed = true;
            } else if (material.molten && temperature < getMeltingTemperature(material.material)) {
                material.molten = false;
                changed = true;
            }
        }
        if (changed) {
            markDirty();
        }
        if (processHazards()) {
            return true;
        }
        if (temperature > maxTemperature) {
            meltDown();
            return true;
        }
        return false;
    }

    private void meltDown() {
        releaseHotGasEffects(temperature, Math.max(GTValues.M, getTotalAmount()));
        contents.clear();
        temperature = ENVIRONMENT_TEMPERATURE;
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

            if (!acidProof && isAcidMaterial(material.material)) {
                destroyByAcid();
                return true;
            }

            if (shouldExplode(material)) {
                explodeFromContent(material.amount);
                return true;
            }

            if (shouldBurnOff(material)) {
                long amount = material.amount;
                long hazardTemperature = Math.max(temperature, getMeltingTemperature(material.material));
                iterator.remove();
                releaseHotGasEffects(hazardTemperature, amount);
                changed = true;
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
        temperature = ENVIRONMENT_TEMPERATURE;
        refreshDisplayState();
        markDirty();
        getWorld().setBlockToAir(getPos());
    }

    private boolean shouldExplode(StoredMaterial material) {
        return material.material.hasFlags(MaterialFlags.EXPLOSIVE) &&
                temperature >= Math.min(FLAMMABLE_TEMPERATURE, getMeltingTemperature(material.material));
    }

    private boolean shouldBurnOff(StoredMaterial material) {
        return isGasLikeMaterial(material.material) ||
                (material.material.hasFlags(MaterialFlags.FLAMMABLE) && temperature > FLAMMABLE_TEMPERATURE);
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
        material.material = getSmeltingTarget(material.material);
        material.molten = true;
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
        addStoredMaterial(new StoredMaterial(match.alloy, match.outputUnits * match.conversions,
                temperature >= getMeltingTemperature(match.alloy)));
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
            if (storedMaterial.material == material.material && storedMaterial.molten == material.molten) {
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
        if (material.hasProperty(PropertyKey.INGOT)) {
            IngotProperty property = material.getProperty(PropertyKey.INGOT);
            if (property.getSmeltingInto() != null) {
                return property.getSmeltingInto();
            }
        }
        return material;
    }

    private int getMeltingTemperature(Material material) {
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
        long moldMaxTemperature = mold.getMoldMaxTemperature();
        if (moldMaxTemperature > 0L && temperature > moldMaxTemperature) {
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
            displayMaterialName = displayMaterial.material.getName();
            displayMolten = displayMaterial.molten && temperature >= getMeltingTemperature(displayMaterial.material);
        }
        meltDownWarning = temperature + 100L > maxTemperature;
    }

    private boolean isDisplayStateChanged() {
        return displayHeight != oldDisplayHeight ||
                displayMolten != oldDisplayMolten ||
                meltDownWarning != oldMeltDownWarning ||
                !displayMaterialName.equals(oldDisplayMaterialName);
    }

    private void rememberDisplayState() {
        oldDisplayHeight = displayHeight;
        oldDisplayMaterialName = displayMaterialName;
        oldDisplayMolten = displayMolten;
        oldMeltDownWarning = meltDownWarning;
    }

    @Nullable
    private StoredMaterial getDisplayedMaterial() {
        StoredMaterial lightest = null;
        for (StoredMaterial material : contents) {
            if (material.amount <= 0) {
                continue;
            }
            if (lightest == null || material.material.getMass() < lightest.material.getMass()) {
                lightest = material;
            }
        }
        return lightest;
    }

    @Nullable
    private Material getDisplayedClientMaterial() {
        if (displayMaterialName.isEmpty()) {
            return null;
        }
        return GregTechAPI.materialManager.getMaterial(displayMaterialName);
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
        } else {
            player.sendStatusMessage(new TextComponentString("Temperature: " + temperature + "/" + maxTemperature + "K, Content: " + getDisplayContent()), true);
            return true;
        }
        return super.onRightClick(player, hand, facing, hitResult);
    }

    private boolean tryScrapeSolidContent(EntityPlayer player, ItemStack heldItem) {
        boolean emptyHandScrape = heldItem.isEmpty() && player.isSneaking();
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
            if (lightest == null || material.material.getMass() < lightest.material.getMass()) {
                lightest = material;
            }
        }
        return lightest;
    }

    @Nullable
    private ScrapeResult createScrapeResult(StoredMaterial material) {
        OrePrefix[] prefixes = {OrePrefix.dustTiny, OrePrefix.nugget, OrePrefix.dustSmall, OrePrefix.dust};
        for (OrePrefix prefix : prefixes) {
            long unitAmount = prefix.getMaterialAmount(material.material);
            if (unitAmount <= 0 || material.amount < unitAmount) {
                continue;
            }
            int count = (int) Math.min(64L, material.amount / unitAmount);
            ItemStack output = OreDictUnifier.get(prefix, material.material, count);
            if (!output.isEmpty()) {
                return new ScrapeResult(output, unitAmount * count);
            }
        }
        return null;
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
        if (contents.isEmpty()) {
            return "empty";
        }
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
        return builder.toString();
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
            releaseHotGasEffects(temperature, Math.max(GTValues.M, getTotalAmount()));
            contents.clear();
            temperature = ENVIRONMENT_TEMPERATURE;
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
        collisionList.add(new IndexedCuboid6(null, WALL_X_NEG));
        collisionList.add(new IndexedCuboid6(null, WALL_Z_NEG));
        collisionList.add(new IndexedCuboid6(null, WALL_X_POS));
        collisionList.add(new IndexedCuboid6(null, WALL_Z_POS));
        collisionList.add(new IndexedCuboid6(null, BOTTOM));
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
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/gravel");
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
        NBTTagList contentList = new NBTTagList();
        for (StoredMaterial material : contents) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(NBT_MATERIAL, material.material.getName());
            tag.setLong(NBT_AMOUNT, material.amount);
            tag.setBoolean(NBT_MOLTEN, material.molten);
            contentList.appendTag(tag);
        }
        data.setTag(NBT_CONTENTS, contentList);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        temperature = data.hasKey(NBT_TEMPERATURE) ? data.getLong(NBT_TEMPERATURE) : ENVIRONMENT_TEMPERATURE;
        oldTemperature = data.hasKey(NBT_OLD_TEMPERATURE) ? data.getLong(NBT_OLD_TEMPERATURE) : temperature;
        contents.clear();
        NBTTagList contentList = data.getTagList(NBT_CONTENTS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < contentList.tagCount(); i++) {
            NBTTagCompound tag = contentList.getCompoundTagAt(i);
            Material material = GregTechAPI.materialManager.getMaterial(tag.getString(NBT_MATERIAL));
            if (material != null && material != gregtech.api.unification.material.Materials.NULL) {
                contents.add(new StoredMaterial(material, tag.getLong(NBT_AMOUNT), tag.getBoolean(NBT_MOLTEN)));
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
    }

    private void readDisplayState(PacketBuffer buf) {
        displayHeight = buf.readVarInt();
        displayMaterialName = buf.readString(Short.MAX_VALUE);
        displayMolten = buf.readBoolean();
        meltDownWarning = buf.readBoolean();
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

        private StoredMaterial(Material material, long amount, boolean molten) {
            this.material = material;
            this.amount = amount;
            this.molten = molten;
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
            return new IFluidTankProperties[]{new FluidTankProperties(content, toFluidAmount(CAPACITY), true, true)};
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
            if (material == null) {
                return null;
            }
            return drainMaterial(material, maxDrain, doDrain);
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
