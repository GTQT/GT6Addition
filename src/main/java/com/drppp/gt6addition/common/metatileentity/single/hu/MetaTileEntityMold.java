package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.BlockFaceShape;
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

/**
 * GT6 MultiTileEntityMold's 5 x 5 sculpted shell.
 *
 * <p>The silhouette is deliberately not a low cuboid: GT6 builds it from a
 * 1 px outer rim, four 4 px side pieces, twelve clamps and 25 individually
 * masked carving cells. Keeping those render passes is what gives this block
 * its shallow, open mold appearance.</p>
 */
public class MetaTileEntityMold extends MetaTileEntity implements ICrucibleMold {

    private static final double PX = 1.0D / 16.0D;

    /* GT6 passes 1-17 from MOLD_BOUNDS. Its pass 0 is the molten/solid
       material surface and remains absent until mold contents are implemented. */
    private static final Cuboid6[] SHELL_PASSES = {
            null,
            box(0, 0, 0, 16, 1, 16),
            new Cuboid6(14.0D * PX, 0.0D, 0.0D, 1.0D, 4.0D * PX + 0.001D, 1.0D),
            box(0, 0, 14, 16, 4, 16),
            new Cuboid6(0.0D, 0.0D, 0.0D, 14.0D * PX, 4.0D * PX + 0.001D, 1.0D),
            box(0, 0, 0, 16, 4, 14),
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

    private final int color;
    private final boolean acidProof;
    private final float hardness;
    private final float resistance;
    private final long maxTemperature;

    /** The GT6 5 x 5 carving bit-field. It stays empty until chisel logic is added. */
    private int shape;

    public MetaTileEntityMold(ResourceLocation metaTileEntityId, int color, boolean acidProof,
                              float hardness, float resistance, long maxTemperature) {
        super(metaTileEntityId);
        this.color = color;
        this.acidProof = acidProof;
        this.hardness = hardness;
        this.resistance = resistance;
        this.maxTemperature = maxTemperature;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMold(metaTileEntityId, color, acidProof, hardness, resistance, maxTemperature);
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
        return Pair.of(Textures.SOLID_STEEL_CASING.getParticleSprite(), color);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] coloured = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color)));
        TextureAtlasSprite sprite = Textures.SOLID_STEEL_CASING.getParticleSprite();

        for (int pass = 1; pass < SHELL_PASSES.length; pass++) {
            renderShellPass(state, translation, coloured, sprite, pass, SHELL_PASSES[pass]);
        }
        for (int cell = 0; cell < CELLS.length; cell++) {
            renderCell(state, translation, coloured, sprite, cell, CELLS[cell]);
        }
    }

    private void renderShellPass(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline,
                                 TextureAtlasSprite sprite, int pass, Cuboid6 bounds) {
        for (EnumFacing side : EnumFacing.VALUES) {
            if (isShellFaceVisible(pass, side)) {
                Textures.renderFace(state, translation, pipeline, side, bounds, sprite, BlockRenderLayer.CUTOUT_MIPPED);
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
                            TextureAtlasSprite sprite, int cell, Cuboid6 bounds) {
        if ((shape & (1 << cell)) != 0) return;

        // A non-carved cell has the visible top surface. Bottom is never rendered.
        Textures.renderFace(state, translation, pipeline, EnumFacing.UP, bounds, sprite, BlockRenderLayer.CUTOUT_MIPPED);

        int row = cell / 5;
        int column = cell % 5;
        renderCellWall(state, translation, pipeline, sprite, bounds, row < 4 ? cell + 5 : -1, EnumFacing.EAST);
        renderCellWall(state, translation, pipeline, sprite, bounds, row > 0 ? cell - 5 : -1, EnumFacing.WEST);
        renderCellWall(state, translation, pipeline, sprite, bounds, column < 4 ? cell + 1 : -1, EnumFacing.SOUTH);
        renderCellWall(state, translation, pipeline, sprite, bounds, column > 0 ? cell - 1 : -1, EnumFacing.NORTH);
    }

    private void renderCellWall(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline,
                                TextureAtlasSprite sprite, Cuboid6 bounds, int neighbour, EnumFacing side) {
        if (neighbour >= 0 && (shape & (1 << neighbour)) != 0) {
            Textures.renderFace(state, translation, pipeline, side, bounds, sprite, BlockRenderLayer.CUTOUT_MIPPED);
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

    /** The four clamp pieces reach 7 px above the block base. */
    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(new IndexedCuboid6(null, new Cuboid6(0.0D, 0.0D, 0.0D,
                1.0D, 7.0D * PX, 1.0D)));
    }

    @Override public boolean isMoldInputSide(@Nullable EnumFacing side) { return side != null && side != EnumFacing.DOWN; }
    @Override public long getMoldMaxTemperature() { return maxTemperature; }
    @Override public long getMoldRequiredMaterialUnits(@Nullable Material material) { return 0L; }

    @Override
    public long fillMold(Material material, long materialAmount, long temperature, @Nullable EnumFacing side,
                         boolean simulate) {
        // The sculpted-carving and casting state is added with the GT6 material core.
        return 0L;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.capacity", "5×5"));
        tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.operation"));
        if (acidProof) tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.acid_proof"));
    }
}
