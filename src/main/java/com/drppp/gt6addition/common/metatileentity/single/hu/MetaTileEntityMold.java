package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UVScale;
import codechicken.lib.vec.uv.UVTransformationList;
import codechicken.lib.vec.uv.UVTranslation;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import com.drppp.gt6addition.api.temperature.ITemperatureProvider;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IngotProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.io.IOException;
import java.util.List;

/**
 * GT6 MultiTileEntityMold's 5 x 5 sculpted shell.
 *
 * <p>The silhouette is deliberately not a low cuboid: GT6 builds it from a
 * 1 px outer rim, four 4 px side pieces, twelve clamps and 25 individually
 * masked carving cells. Keeping those render passes is what gives this block
 * its shallow, open mold appearance.</p>
 */
public class MetaTileEntityMold extends MetaTileEntity implements ICrucibleMold, ITemperatureProvider {

    private static final double PX = 1.0D / 16.0D;
    private static final String NBT_SHAPE = "gt.mold";
    private static final String NBT_TEMPERATURE = "gt.mold.temperature";
    private static final String NBT_COOLING_START_TEMPERATURE = "gt.mold.cooling_start_temperature";
    private static final String NBT_CONTENT_MATERIAL = "gt.mold.material";
    private static final String NBT_CONTENT_AMOUNT = "gt.mold.amount";
    private static final String NBT_OUTPUT = "gt.mold.output";
    private static final String NBT_AUTO_PULL_DIRECTIONS = "gt.mold.auto_pull_directions";
    private static final String NBT_REDSTONE_MODE = "gt.mold.redstone_mode";
    private static final String CHISEL_TOOL_CLASS = "chisel";
    private static final int DATA_SHAPE = 0x4701;
    private static final int DATA_STATE = 0x4702;
    private static final int SHAPE_CELL_COUNT = 25;
    private static final double INNER_START = 2.0D * PX;
    private static final double INNER_SIZE = 12.0D * PX;
    private static final long ENVIRONMENT_TEMPERATURE = 300L;
    private static final long COOLING_STEP = 5L;

    /* GT6 passes 1-17 from MOLD_BOUNDS. Molten contents and the finished
       casting are rendered separately over the carved cells. */
    private static final Cuboid6[] SHELL_PASSES = {
            null,
            box(0, 0, 0, 16, 1, 16),
            new Cuboid6(14.0D * PX, 0.0D, 0.0D, 1.0D, 4.0D * PX + 0.001D, 1.0D),
            box(0, 0, 14, 16, 4, 16),
            new Cuboid6(0.0D, 0.0D, 0.0D, 2.0D * PX, 4.0D * PX + 0.001D, 1.0D),
            box(0, 0, 0, 16, 4, 2),
            box(6, 4, 0, 7, 6, 2),
            box(9, 4, 0, 10, 6, 2),
            box(6, 6, 0, 10, 7, 2),
            box(6, 4, 14, 7, 6, 16),
            box(9, 4, 14, 10, 6, 16),
            box(6, 6, 14, 10, 7, 16),
            box(0, 4, 6, 2, 6, 7),
            box(0, 4, 9, 2, 6, 10),
            box(0, 6, 6, 2, 7, 10),
            box(14, 4, 6, 16, 6, 7),
            box(14, 4, 9, 16, 6, 10),
            box(14, 6, 6, 16, 7, 10)
    };
    private static final Cuboid6[] CELLS = createCells();

    private final int tier;
    private final int color;
    private final boolean acidProof;
    private final float hardness;
    private final float resistance;
    private final long maxTemperature;

    /** The GT6 5 x 5 carving bit-field. A set bit means that cell was carved away. */
    private int shape;
    private int autoPullDirections;
    private boolean redstoneMode;
    private long temperature = ENVIRONMENT_TEMPERATURE;
    private long coolingStartTemperature = ENVIRONMENT_TEMPERATURE;
    private Material contentMaterial;
    private long contentAmount;
    private ItemStack output = ItemStack.EMPTY;

    public MetaTileEntityMold(ResourceLocation metaTileEntityId, int tier, int color, boolean acidProof,
                              float hardness, float resistance, long maxTemperature) {
        super(metaTileEntityId);
        this.tier = tier;
        this.color = color;
        this.acidProof = acidProof;
        this.hardness = hardness;
        this.resistance = resistance;
        this.maxTemperature = maxTemperature;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMold(metaTileEntityId, tier, color, acidProof, hardness, resistance, maxTemperature);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld() == null || getWorld().isRemote) {
            return;
        }

        if (contentMaterial == null && contentAmount <= 0L && output.isEmpty()) {
            processAutoInput();
        }
        if (contentMaterial == null || contentAmount <= 0L) return;

        boolean changed = false;
        boolean solidified = false;
        if (temperature > ENVIRONMENT_TEMPERATURE) {
            temperature -= Math.min(COOLING_STEP, temperature - ENVIRONMENT_TEMPERATURE);
            changed = true;
        } else if (temperature < ENVIRONMENT_TEMPERATURE) {
            temperature += Math.min(COOLING_STEP, ENVIRONMENT_TEMPERATURE - temperature);
            changed = true;
        }

        if (temperature > maxTemperature) {
            getWorld().setBlockState(getPos(), Blocks.FLOWING_LAVA.getDefaultState(), 3);
            return;
        } else if (temperature < getMaterialMeltingTemperature(contentMaterial)) {
            OrePrefix recipe = getMoldRecipe(shape);
            Material solidifyingMaterial = getSolidifyingMaterial(contentMaterial);
            ItemStack result = createOutput(recipe, solidifyingMaterial, contentAmount);
            contentMaterial = null;
            contentAmount = 0L;
            if (!result.isEmpty()) {
                output = result;
            }
            changed = true;
            solidified = true;
        }

        if (changed) {
            markDirty();
            if (solidified || getOffsetTimer() % 5 == 0) {
                syncState();
            }
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (facing == EnumFacing.DOWN) {
            return super.onRightClick(player, hand, facing, hitResult);
        }
        ItemStack tool = player.getHeldItem(hand);
        if (tool.isEmpty() || !tool.getItem().getToolClasses(tool).contains(CHISEL_TOOL_CLASS)) {
            if (tryPickUpOutput(player)) {
                return true;
            }
            if (getWorld() != null && !getWorld().isRemote) {
                if (tryPourFromAdjacent()) {
                    return true;
                }
                player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.mold.status.no_pour"), true);
            }
            return super.onRightClick(player, hand, facing, hitResult);
        }

        // Keep the interaction client-side passive. The server owns the bit-field and
        // sends the resulting shape to tracking clients below.
        if (getWorld() == null || getWorld().isRemote) {
            return true;
        }

        // A mold cannot be reshaped while it contains molten material or a
        // finished casting, matching GT6's mContent/slot(0) guard.
        int cell = contentMaterial == null && contentAmount <= 0L && output.isEmpty()
                ? getCarvedCell(hitResult) : -1;
        if (cell < 0 || (shape & (1 << cell)) != 0) {
            return true;
        }

        shape |= 1 << cell;
        if (!player.isCreative()) {
            tool.damageItem(1, player);
        }
        getWorld().playSound(null, getPos(), SoundEvents.BLOCK_STONE_HIT,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        markDirty();
        writeCustomData(DATA_SHAPE, buffer -> buffer.writeVarInt(shape));
        scheduleRenderUpdate();
        return true;
    }

    private boolean tryPickUpOutput(EntityPlayer player) {
        if (output.isEmpty() || getWorld() == null || getWorld().isRemote) {
            return false;
        }
        ItemHandlerHelper.giveItemToPlayer(player, output.copy());
        output = ItemStack.EMPTY;
        markDirty();
        syncState();
        return true;
    }

    private void processAutoInput() {
        if (autoPullDirections == 0 || redstoneMode && !getWorld().isBlockPowered(getPos()) ||
                !redstoneMode && getOffsetTimer() % 20 != 5) {
            return;
        }
        for (EnumFacing direction : EnumFacing.VALUES) {
            if ((autoPullDirections & (1 << direction.getIndex())) == 0 || !isMoldInputSide(direction)) continue;
            Object source = GTUtility.getMetaTileEntity(getWorld(), getPos().offset(direction));
            if (source instanceof MetaTileEntityCrucible &&
                    ((MetaTileEntityCrucible) source).fillMoldAtSide(this, direction.getOpposite(), direction) > 0L) return;
            if (source instanceof MetaTileEntityCrucibleCrossing &&
                    ((MetaTileEntityCrucibleCrossing) source).fillMoldAtSide(this, direction.getOpposite(), direction)) return;
        }
    }

    /**
     * GT6 lets a mold be clicked to request a pour from an adjacent crucible.
     * This is deliberately limited to horizontal neighbours; a vertical
     * faucet path is triggered through the faucet itself below the crucible.
     */
    private boolean tryPourFromAdjacent() {
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            Object source = GTUtility.getMetaTileEntity(getWorld(), getPos().offset(direction));
            if (source instanceof MetaTileEntityCrucible
                    && ((MetaTileEntityCrucible) source).fillMoldAtSide(this, direction.getOpposite(), direction) > 0L) {
                return true;
            }
            if (source instanceof MetaTileEntityCrucibleCrossing
                    && ((MetaTileEntityCrucibleCrossing) source).fillMoldAtSide(this, direction.getOpposite(), direction)) {
                return true;
            }
        }

        // A mold below a dedicated pouring spout can request the same transfer
        // without requiring the player to click the spout first.
        Object above = GTUtility.getMetaTileEntity(getWorld(), getPos().up());
        return above instanceof MetaTileEntityCruciblePouringSpout
                && ((MetaTileEntityCruciblePouringSpout) above).triggerPour();
    }

    @Override
    public boolean onWrenchClick(EntityPlayer player, EnumHand hand, @Nullable EnumFacing side,
                                 CuboidRayTraceResult hitResult) {
        if (getWorld() == null || getWorld().isRemote) return true;
        if (side == EnumFacing.DOWN) return true;
        EnumFacing direction = getWrenchSelectedDirection(side, hitResult);
        if (direction == null) {
            redstoneMode = !redstoneMode;
        } else {
            autoPullDirections ^= 1 << direction.getIndex();
        }
        markDirty();
        player.sendStatusMessage(new TextComponentTranslation(redstoneMode
                ? "gt6addition.machine.mold.status.redstone_on"
                : "gt6addition.machine.mold.status.auto_input",
                Integer.bitCount(autoPullDirections)), true);
        return true;
    }

    @Override
    public boolean onSoftMalletClick(EntityPlayer player, EnumHand hand, @Nullable EnumFacing side,
                                     CuboidRayTraceResult hitResult) {
        if (getWorld() != null && !getWorld().isRemote) {
            redstoneMode = false;
            autoPullDirections = 0;
            markDirty();
            player.sendStatusMessage(new TextComponentTranslation(
                    "gt6addition.machine.mold.status.auto_input", 0), true);
        }
        return true;
    }

    @Nullable
    private EnumFacing getWrenchSelectedDirection(@Nullable EnumFacing side, @Nullable CuboidRayTraceResult hitResult) {
        if (side != EnumFacing.UP || hitResult == null || hitResult.hitVec == null) return side != null && side.getAxis().isHorizontal() ? side : null;
        double localX = hitResult.hitVec.x - getPos().getX();
        double localZ = hitResult.hitVec.z - getPos().getZ();
        double offsetX = localX - 0.5D;
        double offsetZ = localZ - 0.5D;
        if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) < 0.18D) return null;
        if (Math.abs(offsetX) > Math.abs(offsetZ)) return offsetX < 0.0D ? EnumFacing.WEST : EnumFacing.EAST;
        return offsetZ < 0.0D ? EnumFacing.NORTH : EnumFacing.SOUTH;
    }

    /**
     * Maps GT6's inner 12 x 12 pixel area to one of the 25 carving cells.
     * The strict bounds intentionally match MultiTileEntityMold's original
     * PX_P[2] / PX_N[2] check.
     */
    private int getCarvedCell(@Nullable CuboidRayTraceResult hitResult) {
        if (hitResult == null || hitResult.hitVec == null || getPos() == null) {
            return -1;
        }

        double localX = hitResult.hitVec.x - getPos().getX();
        double localZ = hitResult.hitVec.z - getPos().getZ();
        if (localX <= INNER_START || localX >= INNER_START + INNER_SIZE
                || localZ <= INNER_START || localZ >= INNER_START + INNER_SIZE) {
            return -1;
        }

        double cellSize = INNER_SIZE / 5.0D;
        int row = (int) ((localX - INNER_START) / cellSize);
        int column = (int) ((localZ - INNER_START) / cellSize);
        if (row < 0 || row >= 5 || column < 0 || column >= 5) {
            return -1;
        }
        return row * 5 + column;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buffer) {
        super.writeInitialSyncData(buffer);
        buffer.writeVarInt(shape);
        writeState(buffer);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buffer) {
        super.receiveInitialSyncData(buffer);
        shape = buffer.readVarInt() & getShapeMask();
        readState(buffer);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buffer) {
        super.receiveCustomData(dataId, buffer);
        if (dataId == DATA_SHAPE) {
            shape = buffer.readVarInt() & getShapeMask();
            scheduleRenderUpdate();
        } else if (dataId == DATA_STATE) {
            readState(buffer);
            scheduleRenderUpdate();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger(NBT_SHAPE, shape);
        data.setInteger(NBT_AUTO_PULL_DIRECTIONS, autoPullDirections);
        data.setBoolean(NBT_REDSTONE_MODE, redstoneMode);
        data.setLong(NBT_TEMPERATURE, temperature);
        data.setLong(NBT_COOLING_START_TEMPERATURE, coolingStartTemperature);
        data.setLong(NBT_CONTENT_AMOUNT, contentAmount);
        if (contentMaterial != null) {
            data.setString(NBT_CONTENT_MATERIAL, getMaterialRegistryName(contentMaterial));
        }
        if (!output.isEmpty()) {
            data.setTag(NBT_OUTPUT, output.writeToNBT(new NBTTagCompound()));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        shape = data.hasKey(NBT_SHAPE) ? data.getInteger(NBT_SHAPE) & getShapeMask() : 0;
        autoPullDirections = data.getInteger(NBT_AUTO_PULL_DIRECTIONS) & 0x3F;
        redstoneMode = data.getBoolean(NBT_REDSTONE_MODE);
        temperature = data.hasKey(NBT_TEMPERATURE) ? data.getLong(NBT_TEMPERATURE) : ENVIRONMENT_TEMPERATURE;
        coolingStartTemperature = data.hasKey(NBT_COOLING_START_TEMPERATURE)
                ? data.getLong(NBT_COOLING_START_TEMPERATURE) : temperature;
        contentAmount = data.hasKey(NBT_CONTENT_AMOUNT) ? data.getLong(NBT_CONTENT_AMOUNT) : 0L;
        contentMaterial = null;
        if (data.hasKey(NBT_CONTENT_MATERIAL)) {
            contentMaterial = resolveMaterial(data.getString(NBT_CONTENT_MATERIAL));
        }
        if (contentMaterial == null || contentAmount <= 0L) {
            contentMaterial = null;
            contentAmount = 0L;
        }
        output = data.hasKey(NBT_OUTPUT) ? new ItemStack(data.getCompoundTag(NBT_OUTPUT)) : ItemStack.EMPTY;
    }

    private static int getShapeMask() {
        return (1 << SHAPE_CELL_COUNT) - 1;
    }

    private void syncState() {
        if (getWorld() != null && !getWorld().isRemote) {
            writeCustomData(DATA_STATE, this::writeState);
            scheduleRenderUpdate();
        }
    }

    private void writeState(PacketBuffer buffer) {
        buffer.writeLong(temperature);
        buffer.writeLong(coolingStartTemperature);
        buffer.writeString(getMaterialRegistryName(contentMaterial));
        buffer.writeLong(contentAmount);
        buffer.writeItemStack(output);
    }

    private void readState(PacketBuffer buffer) {
        temperature = buffer.readLong();
        coolingStartTemperature = buffer.readLong();
        String materialName = buffer.readString(Short.MAX_VALUE);
        contentMaterial = resolveMaterial(materialName);
        contentAmount = buffer.readLong();
        try {
            output = buffer.readItemStack();
        } catch (IOException ignored) {
            output = ItemStack.EMPTY;
        }
        if (contentMaterial == null || contentAmount <= 0L) {
            contentMaterial = null;
            contentAmount = 0L;
        }
        if (output == null) {
            output = ItemStack.EMPTY;
        }
    }

    private static String getMaterialRegistryName(@Nullable Material material) {
        return material == null ? "" : material.getRegistryName();
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
        // Keep molds from older saves working; they stored Material#getName()
        // without the namespace used by this mod's registered materials.
        for (Material registeredMaterial : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (registeredMaterial != null && materialName.equals(registeredMaterial.getName())) {
                return registeredMaterial;
            }
        }
        return null;
    }

    private OrePrefix getMoldRecipe(int moldShape) {
        return CrucibleMoldRecipes.get(moldShape);
    }

    @Nullable
    private Material getSolidifyingMaterial(@Nullable Material material) {
        if (material == null) {
            return null;
        }
        if (material.hasProperty(PropertyKey.INGOT)) {
            IngotProperty property = material.getProperty(PropertyKey.INGOT);
            if (property.getSmeltingInto() != null) {
                return property.getSmeltingInto();
            }
        }
        return material;
    }

    private long getMaterialMeltingTemperature(Material material) {
        if (material.hasFluid()) {
            return material.getFluid().getTemperature();
        }
        return material.getBlastTemperature() > 0 ? material.getBlastTemperature() : 1811L;
    }

    private boolean isAcidMaterial(Material material) {
        String materialName = material.getName();
        if (materialName != null && materialName.toLowerCase(java.util.Locale.ROOT).contains("acid")) {
            return true;
        }
        return material.hasFluid() && material.getFluid().getName().toLowerCase(java.util.Locale.ROOT).contains("acid");
    }

    private ItemStack createOutput(@Nullable OrePrefix prefix, @Nullable Material material, long materialAmount) {
        if (prefix == null || material == null || materialAmount <= 0L) {
            return ItemStack.EMPTY;
        }
        long unitAmount = prefix.getMaterialAmount(material);
        if (unitAmount <= 0L || materialAmount < unitAmount) {
            return ItemStack.EMPTY;
        }
        int count = (int) Math.min(64L, materialAmount / unitAmount);
        ItemStack result = OreDictUnifier.get(prefix, material, count);
        return result.isEmpty() ? ItemStack.EMPTY : result;
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public int getLightOpacity() { return 0; }
    @Override public float getBlockHardness() { return hardness; }
    @Override public float getBlockResistance() { return resistance; }
    @Override public BlockFaceShape getFaceShape(EnumFacing side) { return side == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED; }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getMaterialRenderer().getParticleSprite(), color);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] coloured = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color)));
        SimpleSidedCubeRenderer materialRenderer = getMaterialRenderer();

        for (int pass = 1; pass < SHELL_PASSES.length; pass++) {
            renderShellPass(state, translation, coloured, materialRenderer, pass, SHELL_PASSES[pass]);
        }
        // The carved cells are only the upper cavity.  GT6 keeps the one-pixel
        // base slab, so carving must never expose the world below the mold.
        renderFloor(state, translation, coloured, materialRenderer);
        for (int cell = 0; cell < CELLS.length; cell++) {
            renderCell(state, translation, coloured, materialRenderer, cell, CELLS[cell]);
        }
        renderContent(state, translation, pipeline);
        renderSolidOutput(state, translation, pipeline);
    }

    @SideOnly(Side.CLIENT)
    private void renderFloor(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline,
                             SimpleSidedCubeRenderer materialRenderer) {
        Cuboid6 floor = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, PX, 1.0D);
        Textures.renderFace(state, translation, pipeline, EnumFacing.UP, floor,
                materialRenderer.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.TOP),
                BlockRenderLayer.CUTOUT_MIPPED);
    }

    @SideOnly(Side.CLIENT)
    private void renderContent(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        if (contentMaterial == null || contentAmount <= 0L) {
            return;
        }
        TextureAtlasSprite sprite = getContentSprite(contentMaterial);
        if (sprite == null) {
            return;
        }
        int contentColor = getContentRenderColor(contentMaterial);
        IVertexOperation[] contentPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(contentColor)));
        // A carved bit is a 12/5 px wide, 3 px deep cavity cell. Render liquid
        // only on those cells, at the cavity lip (3 px), instead of one broad
        // slab whose incorrect 13 px height made the contents appear airborne.
        for (int cell = 0; cell < CELLS.length; cell++) {
            if ((shape & (1 << cell)) == 0) {
                continue;
            }
            Textures.renderFace(state, translation, contentPipeline, EnumFacing.UP,
                    CELLS[cell], sprite, BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderSolidOutput(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        if (output.isEmpty()) {
            return;
        }
        TextureAtlasSprite sprite = getOutputSprite();
        if (sprite == null) {
            return;
        }

        // Face UVs are mold-local X/Z coordinates. Remap the complete 12x12 px
        // carved field to one item sprite, so every carved cell draws its own
        // matching slice instead of repeating a tiny full icon in each cell.
        IVertexOperation[] outputPipeline = ArrayUtils.add(pipeline,
                new UVTransformationList(
                        new UVScale(1.0D / INNER_SIZE),
                        new UVTranslation(-INNER_START / INNER_SIZE, -INNER_START / INNER_SIZE),
                        new IconTransformation(sprite)));
        for (int cell = 0; cell < CELLS.length; cell++) {
            if ((shape & (1 << cell)) == 0) {
                continue;
            }
            Textures.renderFace(state, translation, outputPipeline, EnumFacing.UP,
                    CELLS[cell], sprite, BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    private TextureAtlasSprite getOutputSprite() {
        IBakedModel itemModel = Minecraft.getMinecraft().getRenderItem()
                .getItemModelWithOverrides(output, getWorld(), null);
        return itemModel == null ? null : itemModel.getParticleTexture();
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    private TextureAtlasSprite getContentSprite(Material material) {
        if (material.hasFluid()) {
            FluidStack fluidStack = material.getFluid(1);
            if (fluidStack != null) {
                Fluid fluid = fluidStack.getFluid();
                ResourceLocation still = fluid.getStill(fluidStack);
                if (still != null) {
                    TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
                    TextureAtlasSprite sprite = textureMap.getAtlasSprite(still.toString());
                    if (sprite != textureMap.getMissingSprite()) {
                        return sprite;
                    }
                }
            }
        }
        // Match the crucible's material fallback: some registered materials
        // (for example solidifying compounds without a FluidProperty) are
        // meltable/castable but have no Forge fluid still texture. Returning
        // null here hid the liquid during cooling, even though the output item
        // appeared correctly once solidified.
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/gravel");
    }

    private int getContentRenderColor(Material material) {
        if (material.hasFluid()) {
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

    @SideOnly(Side.CLIENT)
    private SimpleSidedCubeRenderer getMaterialRenderer() {
        SimpleSidedCubeRenderer renderer = tier >= 0 && tier < Gt6AdditionTextures.MACHINE_BASES.length
                ? Gt6AdditionTextures.MACHINE_BASES[tier] : null;
        return renderer == null ? Gt6AdditionTextures.BASE_NULL_TEXTURE : renderer;
    }

    private void renderShellPass(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline,
                                 SimpleSidedCubeRenderer materialRenderer, int pass, Cuboid6 bounds) {
        for (EnumFacing side : EnumFacing.VALUES) {
            if (isShellFaceVisible(pass, side)) {
                Textures.renderFace(state, translation, pipeline, side, bounds,
                        materialRenderer.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.bySide(side)),
                        BlockRenderLayer.CUTOUT_MIPPED);
            }
        }
    }

    /** Side masks copied from GT6 MultiTileEntityMold#getTexture2 for passes 1-17. */
    private boolean isShellFaceVisible(int pass, EnumFacing side) {
        if (pass == 1) return side.getAxis().isHorizontal();
        if (pass == 2 || pass == 4) return side != EnumFacing.DOWN && side.getAxis() != EnumFacing.Axis.Z;
        if (pass == 3 || pass == 5) return side != EnumFacing.DOWN && side.getAxis() != EnumFacing.Axis.X;
        if (pass == 8 || pass == 11 || pass == 14 || pass == 17) return true;
        return side.getAxis().isHorizontal();
    }

    /** Side masks copied from GT6's 25 shape-controlled render passes. */
    private void renderCell(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline,
                            SimpleSidedCubeRenderer materialRenderer, int cell, Cuboid6 bounds) {
        if ((shape & (1 << cell)) != 0) return;

        // A non-carved cell has the visible top surface. Bottom is never rendered.
        Textures.renderFace(state, translation, pipeline, EnumFacing.UP, bounds,
                materialRenderer.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.TOP),
                BlockRenderLayer.CUTOUT_MIPPED);

        int row = cell / 5;
        int column = cell % 5;
        renderCellWall(state, translation, pipeline, materialRenderer, bounds, row < 4 ? cell + 5 : -1, EnumFacing.EAST);
        renderCellWall(state, translation, pipeline, materialRenderer, bounds, row > 0 ? cell - 5 : -1, EnumFacing.WEST);
        renderCellWall(state, translation, pipeline, materialRenderer, bounds, column < 4 ? cell + 1 : -1, EnumFacing.SOUTH);
        renderCellWall(state, translation, pipeline, materialRenderer, bounds, column > 0 ? cell - 1 : -1, EnumFacing.NORTH);
    }

    private void renderCellWall(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline,
                                SimpleSidedCubeRenderer materialRenderer, Cuboid6 bounds, int neighbour, EnumFacing side) {
        if (neighbour >= 0 && (shape & (1 << neighbour)) != 0) {
            Textures.renderFace(state, translation, pipeline, side, bounds,
                    materialRenderer.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.bySide(side)),
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    private static Cuboid6[] createCells() {
        Cuboid6[] cells = new Cuboid6[25];
        double start = 2.0D * PX;
        double cellSize = 12.0D * PX / 5.0D;
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                double minX = start + row * cellSize;
                double minZ = start + column * cellSize;
                cells[row * 5 + column] = new Cuboid6(minX, 0.0D, minZ,
                        minX + cellSize, 3.0D * PX, minZ + cellSize);
            }
        }
        return cells;
    }

    private static Cuboid6 box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new Cuboid6(minX * PX, minY * PX, minZ * PX, maxX * PX, maxY * PX, maxZ * PX);
    }

    /**
     * Reuse the exact sculpted render pieces for collision and interaction.
     * BlockMachine ray-traces this same list when dispatching clicks.
     */
    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        for (int pass = 1; pass < SHELL_PASSES.length; pass++) {
            collisionList.add(new IndexedCuboid6(null, SHELL_PASSES[pass]));
        }
        for (int cell = 0; cell < CELLS.length; cell++) {
            if ((shape & (1 << cell)) == 0) {
                collisionList.add(new IndexedCuboid6(null, CELLS[cell]));
            }
        }
    }

    @Override public boolean isMoldInputSide(@Nullable EnumFacing side) { return side != null && side != EnumFacing.DOWN; }
    @Override public long getMoldMaxTemperature() { return maxTemperature; }

    public boolean isCooling() {
        return contentMaterial != null && contentAmount > 0L;
    }

    public int getCoolingProgressPercent() {
        if (!isCooling()) {
            return 0;
        }
        double solidificationTemperature = getMaterialMeltingTemperature(contentMaterial);
        double startTemperature = Math.max((double) coolingStartTemperature, solidificationTemperature);
        double coolingRange = Math.max(1.0D, startTemperature - solidificationTemperature);
        double cooledAmount = Math.max(0.0D, startTemperature - temperature);
        return (int) Math.max(0.0D, Math.min(100.0D, cooledAmount * 100.0D / coolingRange));
    }

    public long getCoolingTargetTemperature() {
        return isCooling() ? getMaterialMeltingTemperature(contentMaterial) : 0L;
    }

    public String getCoolingMaterialName() {
        return isCooling() ? contentMaterial.getLocalizedName() : "";
    }

    public ItemStack getOutputStack() {
        return output.copy();
    }

    @Override
    public long getTemperatureValue(@Nullable EnumFacing side) {
        return temperature;
    }

    @Override
    public long getTemperatureMax(@Nullable EnumFacing side) {
        return maxTemperature;
    }

    @Override
    public long getMoldRequiredMaterialUnits(@Nullable Material material) {
        Material solidifyingMaterial = getSolidifyingMaterial(material);
        OrePrefix prefix = getMoldRecipe(shape);
        if (solidifyingMaterial == null || prefix == null) {
            return 0L;
        }

        long unitAmount = prefix.getMaterialAmount(solidifyingMaterial);
        if (unitAmount <= 0L) {
            return 0L;
        }
        return prefix == OrePrefix.nugget ? unitAmount * Integer.bitCount(shape) : unitAmount;
    }

    @Override
    public long fillMold(Material material, long materialAmount, long temperature, @Nullable EnumFacing side,
                         boolean simulate) {
        if (material == null || materialAmount <= 0L || !isMoldInputSide(side)
                || contentMaterial != null || contentAmount > 0L || !output.isEmpty()) {
            return 0L;
        }
        if (!acidProof && isAcidMaterial(material)) {
            return 0L;
        }
        Material solidifyingMaterial = getSolidifyingMaterial(material);
        OrePrefix prefix = getMoldRecipe(shape);
        long requiredAmount = getMoldRequiredMaterialUnits(solidifyingMaterial);
        if (solidifyingMaterial == null || prefix == null || requiredAmount <= 0L
                || materialAmount < requiredAmount
                || createOutput(prefix, solidifyingMaterial, requiredAmount).isEmpty()) {
            return 0L;
        }

        if (!simulate) {
            // Keep the poured/molten material while cooling. GT6 stores this
            // material in the mold and only converts it to its solidifying
            // target at the phase transition; resolving the output material
            // here can discard the molten texture (e.g. lava -> obsidian).
            contentMaterial = material;
            contentAmount = requiredAmount;
            this.temperature = temperature;
            this.coolingStartTemperature = temperature;
            markDirty();
            syncState();
        }
        return requiredAmount;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.capacity", "5×5"));
        tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.operation"));
        tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.auto_input"));
        if (acidProof) tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.acid_proof"));
    }
}
