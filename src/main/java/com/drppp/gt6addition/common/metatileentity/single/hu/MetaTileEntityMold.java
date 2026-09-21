package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
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

/** Visual and placement shell for GT6's sculpted 5×5 casting mold. */
public class MetaTileEntityMold extends MetaTileEntity implements ICrucibleMold {

    private static final double PIXEL = 1.0D / 16.0D;
    private static final Cuboid6 BODY = new Cuboid6(PIXEL, PIXEL, PIXEL, 15.0D * PIXEL, 13.0D * PIXEL, 15.0D * PIXEL);
    private static final Cuboid6[] RIM_AND_CLAMPS = {
            new Cuboid6(0.0D, 0.0D, 0.0D, PIXEL, 12.0D * PIXEL, 1.0D),
            new Cuboid6(15.0D * PIXEL, 0.0D, 0.0D, 1.0D, 12.0D * PIXEL, 1.0D),
            new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, 12.0D * PIXEL, PIXEL),
            new Cuboid6(0.0D, 0.0D, 15.0D * PIXEL, 1.0D, 12.0D * PIXEL, 1.0D),
            new Cuboid6(6.0D * PIXEL, 4.0D * PIXEL, 0.0D, 7.0D * PIXEL, 6.0D * PIXEL, 2.0D * PIXEL),
            new Cuboid6(9.0D * PIXEL, 4.0D * PIXEL, 0.0D, 10.0D * PIXEL, 6.0D * PIXEL, 2.0D * PIXEL),
            new Cuboid6(6.0D * PIXEL, 6.0D * PIXEL, 0.0D, 10.0D * PIXEL, 7.0D * PIXEL, 2.0D * PIXEL),
            new Cuboid6(6.0D * PIXEL, 4.0D * PIXEL, 14.0D * PIXEL, 7.0D * PIXEL, 6.0D * PIXEL, 1.0D),
            new Cuboid6(9.0D * PIXEL, 4.0D * PIXEL, 14.0D * PIXEL, 10.0D * PIXEL, 6.0D * PIXEL, 1.0D),
            new Cuboid6(6.0D * PIXEL, 6.0D * PIXEL, 14.0D * PIXEL, 10.0D * PIXEL, 7.0D * PIXEL, 1.0D)
    };
    private final int color;
    private final boolean acidProof;
    private final float hardness;
    private final float resistance;
    private final long maxTemperature;

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
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] casing = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color)));
        // GT6's central mold body is a top surface.  Rendering it as a full
        // cuboid was the cause of the oversized solid-block appearance.
        Textures.renderFace(renderState, translation, casing, EnumFacing.UP, BODY,
                Textures.SOLID_STEEL_CASING.getParticleSprite(), BlockRenderLayer.CUTOUT_MIPPED);
        for (Cuboid6 part : RIM_AND_CLAMPS) Textures.SOLID_STEEL_CASING.render(renderState, translation, casing, part);
    }

    @Override
    public boolean isMoldInputSide(@Nullable EnumFacing side) {
        return side != null && side != EnumFacing.DOWN;
    }

    @Override public long getMoldMaxTemperature() { return maxTemperature; }

    @Override
    public long getMoldRequiredMaterialUnits(@Nullable Material material) {
        return 0L;
    }

    @Override
    public long fillMold(Material material, long materialAmount, long temperature, @Nullable EnumFacing side,
                         boolean simulate) {
        // Shape storage and the GT6 shape-to-prefix table land with the new
        // material core.  The visual block intentionally accepts no material yet.
        return 0L;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.capacity", "5×5"));
        tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.operation"));
        if (acidProof) tooltip.add(I18n.format("gt6addition.machine.mold.tooltip.acid_proof"));
    }
}
