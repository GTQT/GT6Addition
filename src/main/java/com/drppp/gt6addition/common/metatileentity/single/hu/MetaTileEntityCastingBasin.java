package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import com.drppp.gt6addition.api.temperature.ITemperatureProvider;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityCastingBasin extends MetaTileEntity implements ITemperatureProvider, ICrucibleMold {

    private static final String NBT_MATERIAL = "Material";
    private static final String NBT_AMOUNT = "Amount";
    private static final String NBT_TEMPERATURE = "Temperature";
    private static final int DATA_DISPLAY_STATE = 201;
    private static final long CAPACITY = 9L * GTValues.M;
    private static final long ENVIRONMENT_TEMPERATURE = 295L;
    private static final double WALL_SIZE = 0.125D;
    private static final double BASIN_HEIGHT = 0.5D;
    private static final Cuboid6 WALL_X_NEG = new Cuboid6(0.0D, 0.0D, 0.0D, WALL_SIZE, BASIN_HEIGHT, 1.0D);
    private static final Cuboid6 WALL_X_POS = new Cuboid6(1.0D - WALL_SIZE, 0.0D, 0.0D, 1.0D, BASIN_HEIGHT, 1.0D);
    private static final Cuboid6 WALL_Z_NEG = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, BASIN_HEIGHT, WALL_SIZE);
    private static final Cuboid6 WALL_Z_POS = new Cuboid6(0.0D, 0.0D, 1.0D - WALL_SIZE, 1.0D, BASIN_HEIGHT, 1.0D);
    private static final Cuboid6 BOTTOM = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, WALL_SIZE, 1.0D);

    private final int tier;
    private final int casingColor;
    private final boolean acidProof;
    private final float hardness;
    private final float resistance;
    private final long maxTemperature;
    private final BasinFluidHandler fluidHandler = new BasinFluidHandler();

    private Material material;
    private long materialAmount;
    private long temperature = ENVIRONMENT_TEMPERATURE;
    private int displayHeight;
    private int lastDisplayHeight = -1;
    private String displayMaterialName = "";
    private String lastDisplayMaterialName = "";
    private boolean displayMolten;
    private boolean lastDisplayMolten;

    public MetaTileEntityCastingBasin(ResourceLocation metaTileEntityId, int tier, int casingColor,
                                      boolean acidProof, float hardness, float resistance) {
        this(metaTileEntityId, tier, casingColor, acidProof, hardness, resistance,
                getDefaultMaxTemperature(tier));
    }

    public MetaTileEntityCastingBasin(ResourceLocation metaTileEntityId, int tier, int casingColor,
                                      boolean acidProof, float hardness, float resistance, long maxTemperature) {
        super(metaTileEntityId);
        this.tier = tier;
        this.casingColor = casingColor;
        this.acidProof = acidProof;
        this.hardness = hardness;
        this.resistance = resistance;
        this.maxTemperature = Math.max(ENVIRONMENT_TEMPERATURE, maxTemperature);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCastingBasin(metaTileEntityId, tier, casingColor,
                acidProof, hardness, resistance, maxTemperature);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 20 == 0) {
            coolDown();
            refreshDisplayState();
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            return true;
        }
        ItemStack heldStack = player.getHeldItem(hand);
        if (!heldStack.isEmpty()) {
            FluidActionResult emptyResult = FluidUtil.tryEmptyContainer(heldStack, fluidHandler, Integer.MAX_VALUE, player, true);
            if (emptyResult.isSuccess()) {
                player.setHeldItem(hand, emptyResult.getResult());
                return true;
            }
            FluidActionResult fillResult = FluidUtil.tryFillContainer(heldStack, fluidHandler, Integer.MAX_VALUE, player, true);
            if (fillResult.isSuccess()) {
                player.setHeldItem(hand, fillResult.getResult());
                return true;
            }
            return true;
        }
        if (materialAmount <= 0 || material == null) {
            player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.casting_basin.status.empty"), true);
            return true;
        }
        if (isMolten()) {
            player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.casting_basin.status.hot"), true);
            return true;
        }
        CastResult result = createCastResult();
        if (result == null) {
            player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.casting_basin.status.no_output"), true);
            return true;
        }
        ItemHandlerHelper.giveItemToPlayer(player, result.output);
        materialAmount -= Math.min(materialAmount, result.materialAmount);
        if (materialAmount <= 0) {
            material = null;
            temperature = ENVIRONMENT_TEMPERATURE;
        }
        markDirty();
        refreshDisplayState();
        return true;
    }

    private void coolDown() {
        if (materialAmount <= 0) {
            temperature = ENVIRONMENT_TEMPERATURE;
            return;
        }
        if (temperature > ENVIRONMENT_TEMPERATURE) {
            temperature -= Math.max(1L, (temperature - ENVIRONMENT_TEMPERATURE) / 80L);
        } else if (temperature < ENVIRONMENT_TEMPERATURE) {
            temperature++;
        }
    }

    private boolean isMolten() {
        return material != null && materialAmount > 0 && temperature >= getMeltingTemperature(material);
    }

    @Override
    public boolean isMoldInputSide(@Nullable EnumFacing side) {
        return side != EnumFacing.UP;
    }

    @Override
    public long getMoldMaxTemperature() {
        return maxTemperature;
    }

    @Override
    public long getMoldRequiredMaterialUnits(@Nullable Material requestedMaterial) {
        if (material != null && requestedMaterial != null && material != requestedMaterial) {
            return 0L;
        }
        return Math.max(0L, CAPACITY - materialAmount);
    }

    @Override
    public long fillMold(Material incomingMaterial, long incomingAmount, long incomingTemperature,
                         @Nullable EnumFacing side, boolean simulate) {
        if (incomingMaterial == null || incomingMaterial == Materials.NULL || incomingAmount <= 0L ||
                !isMoldInputSide(side) || incomingTemperature > maxTemperature) {
            return 0L;
        }
        if (material != null && material != incomingMaterial) {
            return 0L;
        }
        long acceptedAmount = Math.min(incomingAmount, Math.max(0L, CAPACITY - materialAmount));
        if (acceptedAmount <= 0L) {
            return 0L;
        }
        if (!simulate) {
            if (material == null || materialAmount <= 0L) {
                material = incomingMaterial;
                temperature = incomingTemperature;
                materialAmount = acceptedAmount;
            } else {
                long totalAmount = materialAmount + acceptedAmount;
                temperature = (temperature * materialAmount + incomingTemperature * acceptedAmount) / totalAmount;
                materialAmount = totalAmount;
            }
            markDirty();
            refreshDisplayState();
        }
        return acceptedAmount;
    }

    @Nullable
    private CastResult createCastResult() {
        if (material == null || materialAmount <= 0) {
            return null;
        }
        OrePrefix[] prefixes = {OrePrefix.block, OrePrefix.ingot, OrePrefix.dust, OrePrefix.nugget,
                OrePrefix.dustSmall, OrePrefix.dustTiny};
        for (OrePrefix prefix : prefixes) {
            long unitAmount = prefix.getMaterialAmount(material);
            if (unitAmount <= 0L || materialAmount < unitAmount) {
                continue;
            }
            int count = (int) Math.min(64L, materialAmount / unitAmount);
            ItemStack output = OreDictUnifier.get(prefix, material, count);
            if (!output.isEmpty()) {
                return new CastResult(output, unitAmount * count);
            }
        }
        return null;
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

    private int getMeltingTemperature(Material material) {
        if (material.hasFluid()) {
            return material.getFluid().getTemperature();
        }
        if (material.hasProperty(PropertyKey.INGOT)) {
            return material.getBlastTemperature() > 0 ? material.getBlastTemperature() : 1811;
        }
        return 1811;
    }

    private static long getDefaultMaxTemperature(int tier) {
        return 1800L + Math.max(0, tier) * 300L;
    }

    private int toFluidAmount(long amount) {
        return (int) Math.min(Integer.MAX_VALUE, amount * GTValues.L / GTValues.M);
    }

    private long toMaterialAmount(int amount) {
        return Math.max(1L, (amount * GTValues.M + GTValues.L - 1L) / GTValues.L);
    }

    public int getStoredFluidAmount() {
        return toFluidAmount(materialAmount);
    }

    public int getCapacityFluidAmount() {
        return toFluidAmount(CAPACITY);
    }

    public long getTemperature() {
        return temperature;
    }

    @Override
    public long getTemperatureValue(@Nullable EnumFacing side) {
        return getTemperature();
    }

    @Override
    public long getTemperatureMax(@Nullable EnumFacing side) {
        return maxTemperature;
    }

    public String getContentsDisplayName() {
        return material == null || materialAmount <= 0 ? "" : material.getLocalizedName();
    }

    public boolean isContentsMolten() {
        return isMolten();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return side != EnumFacing.UP;
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
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
        }
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return null;
        }
        return super.getCapability(capability, side);
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
        if (material == null || materialAmount <= 0) {
            displayHeight = 0;
            displayMaterialName = "";
            displayMolten = false;
            return;
        }
        displayHeight = (int) Math.min(255L, materialAmount * 255L / CAPACITY);
        displayMaterialName = material.getName();
        displayMolten = isMolten();
    }

    private boolean isDisplayStateChanged() {
        return displayHeight != lastDisplayHeight ||
                displayMolten != lastDisplayMolten ||
                !displayMaterialName.equals(lastDisplayMaterialName);
    }

    private void rememberDisplayState() {
        lastDisplayHeight = displayHeight;
        lastDisplayMaterialName = displayMaterialName;
        lastDisplayMolten = displayMolten;
    }

    @Nullable
    private Material getDisplayedClientMaterial() {
        if (displayMaterialName.isEmpty()) {
            return null;
        }
        Material displayMaterial = GregTechAPI.materialManager.getMaterial(displayMaterialName);
        return displayMaterial == null || displayMaterial == Materials.NULL ? null : displayMaterial;
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
        renderDisplayedContent(renderState, translation, pipeline);
    }

    @SideOnly(Side.CLIENT)
    private void renderDisplayedContent(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (displayHeight <= 0) {
            return;
        }
        Material displayMaterial = getDisplayedClientMaterial();
        if (displayMaterial == null) {
            return;
        }
        double top = WALL_SIZE + (BASIN_HEIGHT - WALL_SIZE) * displayHeight / 255.0D;
        Cuboid6 contentBounds = new Cuboid6(WALL_SIZE, WALL_SIZE, WALL_SIZE, 1.0D - WALL_SIZE, top, 1.0D - WALL_SIZE);
        IVertexOperation[] contentPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getContentRenderColor(displayMaterial))));
        Textures.renderFace(renderState, translation, contentPipeline, EnumFacing.UP, contentBounds,
                getContentSprite(displayMaterial), BlockRenderLayer.CUTOUT_MIPPED);
    }

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite getContentSprite(Material displayMaterial) {
        if (displayMolten && displayMaterial.hasFluid()) {
            FluidStack fluidStack = displayMaterial.getFluid(1);
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

    private int getContentRenderColor(Material displayMaterial) {
        if (displayMolten && displayMaterial.hasFluid()) {
            FluidStack fluidStack = displayMaterial.getFluid(1);
            if (fluidStack != null) {
                int fluidColor = fluidStack.getFluid().getColor(fluidStack) & 0xFFFFFF;
                if (fluidColor != 0xFFFFFF) {
                    return fluidColor;
                }
            }
        }
        return displayMaterial.getMaterialRGB() & 0xFFFFFF;
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
        tooltip.add(I18n.format("gt6addition.machine.casting_basin.tooltip.capacity", getCapacityFluidAmount()));
        tooltip.add(I18n.format("gt6addition.machine.casting_basin.tooltip.faucet"));
        tooltip.add(I18n.format("gt6addition.machine.casting_basin.tooltip.output"));
        if (acidProof) {
            tooltip.add(I18n.format("gt6addition.machine.casting_basin.tooltip.acid_proof"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (material != null && material != Materials.NULL && materialAmount > 0) {
            data.setString(NBT_MATERIAL, material.getName());
            data.setLong(NBT_AMOUNT, materialAmount);
            data.setLong(NBT_TEMPERATURE, temperature);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        material = GregTechAPI.materialManager.getMaterial(data.getString(NBT_MATERIAL));
        if (material == Materials.NULL) {
            material = null;
        }
        materialAmount = data.getLong(NBT_AMOUNT);
        temperature = data.hasKey(NBT_TEMPERATURE) ? data.getLong(NBT_TEMPERATURE) : ENVIRONMENT_TEMPERATURE;
        if (material == null || materialAmount <= 0) {
            material = null;
            materialAmount = 0;
        }
        calculateDisplayState();
        rememberDisplayState();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        calculateDisplayState();
        rememberDisplayState();
        writeDisplayState(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        readDisplayState(buf);
        rememberDisplayState();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == DATA_DISPLAY_STATE) {
            readDisplayState(buf);
            rememberDisplayState();
            scheduleRenderUpdate();
        }
    }

    private void writeDisplayState(PacketBuffer buf) {
        buf.writeVarInt(displayHeight);
        buf.writeString(displayMaterialName);
        buf.writeBoolean(displayMolten);
    }

    private void readDisplayState(PacketBuffer buf) {
        displayHeight = buf.readVarInt();
        displayMaterialName = buf.readString(Short.MAX_VALUE);
        displayMolten = buf.readBoolean();
    }

    private static class CastResult {
        private final ItemStack output;
        private final long materialAmount;

        private CastResult(ItemStack output, long materialAmount) {
            this.output = output;
            this.materialAmount = materialAmount;
        }
    }

    private class BasinFluidHandler implements IFluidHandler {

        @Override
        public IFluidTankProperties[] getTankProperties() {
            FluidStack content = material == null || materialAmount <= 0 || !material.hasFluid() ?
                    null : material.getFluid(toFluidAmount(materialAmount));
            return new IFluidTankProperties[]{new FluidTankProperties(content, toFluidAmount(CAPACITY), true, true)};
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            Material filledMaterial = getMaterialFromFluid(resource);
            if (filledMaterial == null || resource == null || resource.amount <= 0) {
                return 0;
            }
            if (material != null && material != filledMaterial) {
                return 0;
            }
            long space = CAPACITY - materialAmount;
            if (space <= 0) {
                return 0;
            }
            int acceptedFluid = Math.min(resource.amount, toFluidAmount(space));
            if (acceptedFluid <= 0) {
                return 0;
            }
            long acceptedMaterialAmount = Math.min(space, toMaterialAmount(acceptedFluid));
            if (acceptedMaterialAmount <= 0) {
                return 0;
            }
            if (doFill) {
                long incomingTemperature = resource.getFluid().getTemperature(resource);
                long oldAmount = materialAmount;
                material = filledMaterial;
                materialAmount += acceptedMaterialAmount;
                temperature = (temperature * Math.max(1L, oldAmount) + incomingTemperature * acceptedMaterialAmount) /
                        Math.max(1L, oldAmount + acceptedMaterialAmount);
                markDirty();
                refreshDisplayState();
            }
            return acceptedFluid;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0 || material == null || !material.hasFluid() || !isMolten()) {
                return null;
            }
            FluidStack content = material.getFluid(1);
            if (content == null || !content.isFluidEqual(resource)) {
                return null;
            }
            return drain(resource.amount, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0 || material == null || materialAmount <= 0 || !material.hasFluid() || !isMolten()) {
                return null;
            }
            int drainedFluid = Math.min(maxDrain, toFluidAmount(materialAmount));
            if (drainedFluid <= 0) {
                return null;
            }
            FluidStack result = material.getFluid(drainedFluid);
            if (doDrain) {
                materialAmount -= Math.min(materialAmount, toMaterialAmount(drainedFluid));
                if (materialAmount <= 0) {
                    material = null;
                    temperature = ENVIRONMENT_TEMPERATURE;
                }
                markDirty();
                refreshDisplayState();
            }
            return result;
        }
    }
}
