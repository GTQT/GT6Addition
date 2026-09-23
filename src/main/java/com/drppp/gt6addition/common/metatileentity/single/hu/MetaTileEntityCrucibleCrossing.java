package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** GT6 MultiTileEntityCrossing: four-way crucible-material routing, never a fluid pipe. */
public class MetaTileEntityCrucibleCrossing extends MetaTileEntity {

    private static long nextLockId;
    private final int tier;
    private final int color;
    private final float hardness;
    private final float resistance;
    private long lockId;
    private boolean redstoneActive;

    public MetaTileEntityCrucibleCrossing(ResourceLocation id, int tier, int color, float hardness, float resistance) {
        super(id);
        this.tier = tier;
        this.color = color;
        this.hardness = hardness;
        this.resistance = resistance;
    }

    @Override public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tile) {
        return new MetaTileEntityCrucibleCrossing(metaTileEntityId, tier, color, hardness, resistance);
    }

    public boolean fillMoldAtSide(ICrucibleMold mold, @Nullable EnumFacing inputSide, @Nullable EnumFacing sideOfMold) {
        long request = ++nextLockId;
        return forward(mold, inputSide, sideOfMold, request);
    }

    private boolean forward(ICrucibleMold mold, @Nullable EnumFacing inputSide, @Nullable EnumFacing sideOfMold, long request) {
        if (lockId == request) return false;
        lockId = request;
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            if (direction == inputSide) continue;
            Object source = GTUtility.getMetaTileEntity(getWorld(), getPos().offset(direction));
            if (source instanceof MetaTileEntityCrucible &&
                    ((MetaTileEntityCrucible) source).fillMoldAtSide(mold, direction.getOpposite(), sideOfMold) > 0L) return true;
            if (source instanceof MetaTileEntityCrucibleCrossing &&
                    ((MetaTileEntityCrucibleCrossing) source).forward(mold, direction.getOpposite(), sideOfMold, request)) return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) return;
        lockId = 0L;
        boolean powered = getInputRedstoneSignal(EnumFacing.UP, false) > 0 ||
                getInputRedstoneSignal(EnumFacing.DOWN, false) > 0;
        if (powered != redstoneActive) {
            redstoneActive = powered;
            int output = redstoneActive ? 1 : 0;
            for (EnumFacing direction : EnumFacing.HORIZONTALS) {
                setOutputRedstoneSignal(direction, output);
            }
            markDirty();
        }
    }

    @Override
    protected boolean canMachineConnectRedstone(@Nullable EnumFacing side) {
        return true;
    }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public int getLightOpacity() { return 0; }
    @Override public float getBlockHardness() { return hardness; }
    @Override public float getBlockResistance() { return resistance; }
    @Override @SideOnly(Side.CLIENT) public Pair<TextureAtlasSprite, Integer> getParticleTexture() { return Pair.of(getMaterialRenderer().getParticleSprite(), color); }

    @Override public void renderMetaTileEntity(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] coloured = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color)));
        SimpleSidedCubeRenderer materialRenderer = getMaterialRenderer();
        // GT6's crossing draws every face of each of its eleven render passes.
        // Draw those faces directly, as with the now-correct mold renderer, rather
        // than passing the shape through the full-machine casing renderer.
        for (Cuboid6 box : GEOMETRY) {
            for (EnumFacing side : EnumFacing.VALUES) {
                Textures.renderFace(state, translation, coloured, side, box,
                        materialRenderer.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.bySide(side)),
                        BlockRenderLayer.CUTOUT_MIPPED);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private SimpleSidedCubeRenderer getMaterialRenderer() {
        SimpleSidedCubeRenderer renderer = tier >= 0 && tier < Gt6AdditionTextures.MACHINE_BASES.length
                ? Gt6AdditionTextures.MACHINE_BASES[tier] : null;
        return renderer == null ? Gt6AdditionTextures.BASE_NULL_TEXTURE : renderer;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    // GT6 PX_N[n] means (16 - n) / 16: three floor pieces at y=1..2,
    // then eight wall pieces at y=2..6, leaving the crossing open from above.
    private static final Cuboid6[] GEOMETRY = boxes(
            6,1,0,10,2,16, 0,1,6,6,2,10, 10,1,6,16,2,10,
            5,2,0,6,6,5, 5,2,11,6,6,16, 0,2,5,6,6,6, 10,2,5,16,6,6,
            10,2,0,11,6,5, 10,2,11,11,6,16, 0,2,10,6,6,11, 10,2,10,16,6,11);
    private static Cuboid6[] boxes(int... c) {
        Cuboid6[] result = new Cuboid6[c.length / 6];
        for (int i = 0; i < result.length; i++) { int p = i * 6; result[i] = new Cuboid6(c[p]/16D,c[p+1]/16D,c[p+2]/16D,c[p+3]/16D,c[p+4]/16D,c[p+5]/16D); }
        return result;
    }

    /** Exact eleven-piece crossing geometry, shared by collision and click ray tracing. */
    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        for (Cuboid6 bounds : GEOMETRY) {
            collisionList.add(new IndexedCuboid6(null, bounds));
        }
    }

    @Override public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gt6addition.machine.crucible_pouring_channel.tooltip.1"));
        tooltip.add(I18n.format("gt6addition.machine.crucible_pouring_channel.tooltip.2"));
    }
}
