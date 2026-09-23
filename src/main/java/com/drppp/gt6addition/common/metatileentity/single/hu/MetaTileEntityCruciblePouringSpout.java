package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/** GT6 MultiTileEntityFaucet's dedicated Crucible Pouring Spout. */
public class MetaTileEntityCruciblePouringSpout extends MetaTileEntity implements ICrucibleMold {

    private static final String NBT_AUTO_PULL = "AutoPull";
    private final int tier;
    private final int color;
    private final boolean acidProof;
    private final float hardness;
    private final float resistance;
    private final long maxTemperature;
    private boolean autoPull;

    public MetaTileEntityCruciblePouringSpout(ResourceLocation id, int tier, int color, boolean acidProof,
                                               float hardness, float resistance, long maxTemperature) {
        super(id);
        this.tier = tier;
        this.color = color;
        this.acidProof = acidProof;
        this.hardness = hardness;
        this.resistance = resistance;
        this.maxTemperature = maxTemperature;
    }

    @Override public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tile) {
        return new MetaTileEntityCruciblePouringSpout(metaTileEntityId, tier, color, acidProof, hardness, resistance, maxTemperature);
    }

    @Override public void onPlacement(EntityLivingBase placer) {
        super.onPlacement(placer);
        if (getFrontFacing().getAxis().isHorizontal()) setFrontFacing(getFrontFacing().getOpposite());
    }
    @Override public boolean isValidFrontFacing(EnumFacing side) { return side.getAxis().isHorizontal(); }

    @Override public void update() {
        super.update();
        if (!getWorld().isRemote && (autoPull ? getOffsetTimer() % 20 == 5 : getWorld().isBlockPowered(getPos()))) triggerPour();
    }

    @Override public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing side, CuboidRayTraceResult hit) {
        if (!getWorld().isRemote && !triggerPour()) player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.crucible_pouring_spout.status.empty"), true);
        return true;
    }

    /** Manual/automatic trigger shared by the spout and a mold below it. */
    public boolean triggerPour() {
        if (getWorld() == null || getWorld().isRemote) {
            return false;
        }
        return pullFrom(getFrontFacing());
    }

    private boolean pullFrom(EnumFacing sourceDirection) {
        Object source = GTUtility.getMetaTileEntity(getWorld(), getPos().offset(sourceDirection));
        if (source instanceof MetaTileEntityCrucible) {
            return ((MetaTileEntityCrucible) source).fillMoldAtSide(this, sourceDirection.getOpposite(), getFrontFacing()) > 0L;
        }
        return source instanceof MetaTileEntityCrucibleCrossing &&
                ((MetaTileEntityCrucibleCrossing) source).fillMoldAtSide(this, sourceDirection.getOpposite(), getFrontFacing());
    }

    @Override public boolean isMoldInputSide(@Nullable EnumFacing side) { return side == getFrontFacing(); }
    @Override public long getMoldMaxTemperature() { return maxTemperature; }
    @Override public long getMoldRequiredMaterialUnits(@Nullable Material material) { return getDownstreamDemand(material); }

    private long getDownstreamDemand(@Nullable Material material) {
        BlockPos cursor = getPos().down();
        while (cursor.getY() > 0) {
            Object target = GTUtility.getMetaTileEntity(getWorld(), cursor);
            if (target instanceof MetaTileEntityCruciblePouringSpout) { cursor = cursor.down(); continue; }
            if (target instanceof ICrucibleMold) return ((ICrucibleMold) target).getMoldRequiredMaterialUnits(material);
            if (!canFlowThrough(cursor)) return 0L;
            cursor = cursor.down();
        }
        return 0L;
    }

    private boolean canFlowThrough(BlockPos pos) {
        if (getWorld().isAirBlock(pos)) return true;
        return getWorld().getBlockState(pos).getCollisionBoundingBox(getWorld(), pos) == null;
    }

    @Override public long fillMold(Material material, long amount, long temperature, @Nullable EnumFacing side, boolean simulate) {
        if (side != getFrontFacing() || material == null || amount <= 0L ||
                (!acidProof && material.getName().toLowerCase(Locale.ROOT).contains("acid"))) return 0L;
        if (temperature > maxTemperature) {
            if (!simulate) getWorld().setBlockState(getPos(), Blocks.FLOWING_LAVA.getDefaultState(), 3);
        }
        BlockPos cursor = getPos().down();
        while (cursor.getY() > 0) {
            Object target = GTUtility.getMetaTileEntity(getWorld(), cursor);
            if (target instanceof MetaTileEntityCruciblePouringSpout) { cursor = cursor.down(); continue; }
            if (target instanceof ICrucibleMold) return ((ICrucibleMold) target).fillMold(material, amount, temperature, EnumFacing.UP, simulate);
            if (!canFlowThrough(cursor)) return 0L;
            cursor = cursor.down();
        }
        return 0L;
    }

    @Override
    public boolean onWrenchClick(EntityPlayer player, EnumHand hand, @Nullable EnumFacing side,
                                 CuboidRayTraceResult hitResult) {
        if (getWorld() != null && !getWorld().isRemote) {
            autoPull = !autoPull;
            markDirty();
            player.sendStatusMessage(new TextComponentTranslation(autoPull
                    ? "gt6addition.machine.crucible_pouring_spout.status.automatic"
                    : "gt6addition.machine.crucible_pouring_spout.status.redstone"), true);
        }
        return true;
    }

    @Override
    public boolean onSoftMalletClick(EntityPlayer player, EnumHand hand, @Nullable EnumFacing side,
                                     CuboidRayTraceResult hitResult) {
        if (getWorld() != null && !getWorld().isRemote) {
            autoPull = false;
            markDirty();
            player.sendStatusMessage(new TextComponentTranslation(
                    "gt6addition.machine.crucible_pouring_spout.status.redstone"), true);
        }
        return true;
    }

    @Override
    protected boolean canMachineConnectRedstone(@Nullable EnumFacing side) {
        return true;
    }

    @Override public void renderMetaTileEntity(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] coloured = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color)));
        SimpleSidedCubeRenderer materialRenderer = getMaterialRenderer();
        for (Cuboid6 passBounds : gt6RenderPasses()) {
            for (EnumFacing side : EnumFacing.VALUES) {
                // GT6 getTexture2: only suppress the crucible-facing side when
                // that side is occluded by its neighbour.
                if (side != getFrontFacing() || shouldRenderFrontFace()) {
                    Textures.renderFace(state, translation, coloured, side, passBounds,
                            materialRenderer.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.bySide(side)),
                            BlockRenderLayer.CUTOUT_MIPPED);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private SimpleSidedCubeRenderer getMaterialRenderer() {
        SimpleSidedCubeRenderer renderer = tier >= 0 && tier < Gt6AdditionTextures.MACHINE_BASES.length
                ? Gt6AdditionTextures.MACHINE_BASES[tier] : null;
        return renderer == null ? Gt6AdditionTextures.BASE_NULL_TEXTURE : renderer;
    }

    private boolean shouldRenderFrontFace() {
        return renderContextStack != null || getHolder() == null || getWorld().getBlockState(getPos())
                .shouldSideBeRendered(getWorld(), getPos(), getFrontFacing());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    /** Exact bounds from GT6 MultiTileEntityFaucet#setBlockBounds2, passes 0-2. */
    private Cuboid6[] gt6RenderPasses() {
        switch (getFrontFacing()) {
            // GT6 PX_N[n] means (16 - n) / 16: floor ends at y=2, walls at y=6.
            case NORTH: return boxes(6, 1, 0, 10, 2, 4, 5, 2, 0, 6, 6, 4, 10, 2, 0, 11, 6, 4);
            case SOUTH: return boxes(6, 1, 12, 10, 2, 16, 5, 2, 12, 6, 6, 16, 10, 2, 12, 11, 6, 16);
            case WEST:  return boxes(0, 1, 6, 4, 2, 10, 0, 2, 5, 4, 6, 6, 0, 2, 10, 4, 6, 11);
            default:    return boxes(12, 1, 6, 16, 2, 10, 12, 2, 5, 16, 6, 6, 12, 2, 10, 16, 6, 11);
        }
    }

    private static Cuboid6[] boxes(int... coordinates) {
        Cuboid6[] result = new Cuboid6[coordinates.length / 6];
        for (int i = 0; i < result.length; i++) {
            int offset = i * 6;
            result[i] = new Cuboid6(coordinates[offset] / 16.0D, coordinates[offset + 1] / 16.0D,
                    coordinates[offset + 2] / 16.0D, coordinates[offset + 3] / 16.0D,
                    coordinates[offset + 4] / 16.0D, coordinates[offset + 5] / 16.0D);
        }
        return result;
    }

    /** Exact three-piece model geometry, shared by collision and click ray tracing. */
    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        for (Cuboid6 bounds : gt6RenderPasses()) {
            collisionList.add(new IndexedCuboid6(null, bounds));
        }
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public int getLightOpacity() { return 0; }
    @Override public float getBlockHardness() { return hardness; }
    @Override public float getBlockResistance() { return resistance; }
    @Override @SideOnly(Side.CLIENT) public Pair<TextureAtlasSprite, Integer> getParticleTexture() { return Pair.of(getMaterialRenderer().getParticleSprite(), color); }
    @Override public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gt6addition.machine.crucible_pouring_spout.tooltip.transfer"));
        tooltip.add(I18n.format("gt6addition.machine.crucible_pouring_spout.tooltip.redstone"));
        if (acidProof) tooltip.add(I18n.format("gt6addition.machine.crucible_pouring_spout.tooltip.acid_proof"));
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound data) { super.writeToNBT(data); data.setBoolean(NBT_AUTO_PULL, autoPull); return data; }
    @Override public void readFromNBT(NBTTagCompound data) { super.readFromNBT(data); autoPull = data.getBoolean(NBT_AUTO_PULL); }
}
