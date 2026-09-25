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

    // GT6 texture indices are ordered bottom, top, left, front, right, back.
    private static final String[] GT6_FACE_TEXTURES = {"bottom", "top", "left", "front", "right", "back"};
    private static final int[][] GT6_FACING_ROTATIONS = {
            {0, 1, 2, 3, 4, 5, 6, 6},
            {0, 1, 2, 3, 4, 5, 6, 6},
            {0, 1, 3, 5, 4, 2, 6, 6},
            {0, 1, 5, 3, 2, 4, 6, 6},
            {0, 1, 2, 4, 3, 5, 6, 6},
            {0, 1, 4, 2, 5, 3, 6, 6},
            {0, 1, 2, 3, 4, 5, 6, 6},
            {0, 1, 2, 3, 4, 5, 6, 6}
    };

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

    /** Renders a six-texture GT6 cube, preserving its separate left/right faces and facing rotations. */
    public static void renderGt6SixSidedCube(CCRenderState renderState, Matrix4 translation,
                                              IVertexOperation[] pipeline, EnumFacing frontFacing,
                                              Cuboid6 bounds, String textureRoot) {
        int facingIndex = frontFacing.getIndex();
        for (EnumFacing side : EnumFacing.VALUES) {
            int textureIndex = GT6_FACING_ROTATIONS[facingIndex][side.getIndex()];
            if (textureIndex < 0 || textureIndex >= GT6_FACE_TEXTURES.length) continue;
            Textures.renderFace(renderState, translation, pipeline, side, bounds,
                    getSprite(textureRoot + GT6_FACE_TEXTURES[textureIndex]), BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    public static TextureAtlasSprite getSprite(String spritePath) {
        String atlasPath = spritePath.contains(":") ? spritePath : "gregtech:blocks/" + spritePath;
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(atlasPath);
    }
}
