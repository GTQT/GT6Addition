package com.drppp.gt6addition.common.metatileentity.single.ku;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;

public final class KineticRenderHelper {

    private KineticRenderHelper() {
    }

    public static Cuboid6 axisBox(EnumFacing.Axis axis, double minAxis, double maxAxis,
                                  double minFirst, double maxFirst, double minSecond, double maxSecond) {
        switch (axis) {
            case X:
                return new Cuboid6(minAxis, minFirst, minSecond, maxAxis, maxFirst, maxSecond);
            case Y:
                return new Cuboid6(minFirst, minAxis, minSecond, maxFirst, maxAxis, maxSecond);
            case Z:
            default:
                return new Cuboid6(minFirst, minSecond, minAxis, maxFirst, maxSecond, maxAxis);
        }
    }

    public static void renderAllFaces(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                      Cuboid6 bounds, String spritePath, int color) {
        IVertexOperation[] coloredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color & 0xFFFFFF)));
        TextureAtlasSprite sprite = getSprite(spritePath);
        for (EnumFacing facing : EnumFacing.VALUES) {
            Textures.renderFace(renderState, translation, coloredPipeline, facing, bounds, sprite,
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    public static void renderFace(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                  EnumFacing face, Cuboid6 bounds, String spritePath, int color) {
        IVertexOperation[] coloredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color & 0xFFFFFF)));
        Textures.renderFace(renderState, translation, coloredPipeline, face, bounds, getSprite(spritePath),
                BlockRenderLayer.CUTOUT_MIPPED);
    }

    public static void renderOverlayFace(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                         EnumFacing face, Cuboid6 bounds, String spritePath) {
        Textures.renderFace(renderState, translation, pipeline, face, bounds, getSprite(spritePath),
                BlockRenderLayer.CUTOUT_MIPPED);
    }

    public static TextureAtlasSprite getSprite(String spritePath) {
        String atlasPath = spritePath.contains(":") ? spritePath : "gregtech:blocks/" + spritePath;
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(atlasPath);
    }
}
