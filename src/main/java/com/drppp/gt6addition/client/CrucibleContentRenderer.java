package com.drppp.gt6addition.client;

import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleContentVisual;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashSet;
import java.util.Set;

/** Material-based molten fallback, also usable when no Forge fluid exists. */
@SideOnly(Side.CLIENT)
public final class CrucibleContentRenderer {
    private static final ResourceLocation MOLTEN_FALLBACK =
            new ResourceLocation("gregtech", "blocks/material_sets/dull/molten");

    private CrucibleContentRenderer() {}

    public static void registerSprites(TextureMap map) {
        Set<ResourceLocation> textures = new LinkedHashSet<>();
        textures.add(MOLTEN_FALLBACK);
        if (GregTechAPI.materialManager != null) {
            for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
                ResourceLocation molten = MaterialIconType.molten.getBlockTexturePath(material.getMaterialIconSet());
                if (hasTexture(molten)) textures.add(molten);
                FluidStack liquid = CrucibleContentVisual.liquidAppearance(material);
                if (liquid != null) {
                    ResourceLocation still = liquid.getFluid().getStill(liquid);
                    if (still != null && hasTexture(still)) textures.add(still);
                }
            }
        }
        for (ResourceLocation texture : textures) map.registerSprite(texture);
    }

    private static boolean hasTexture(ResourceLocation sprite) {
        return ResourceHelper.doResourcepacksHaveResource(
                new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath() + ".png"));
    }

    public static TextureAtlasSprite sprite(Material material, boolean molten) {
        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        if (!molten) {
            return map.getAtlasSprite(material == Materials.Obsidian ?
                    "minecraft:blocks/obsidian" : "minecraft:blocks/gravel");
        }
        FluidStack liquid = CrucibleContentVisual.liquidAppearance(material);
        if (liquid != null) {
            ResourceLocation still = liquid.getFluid().getStill(liquid);
            if (still != null) {
                TextureAtlasSprite sprite = map.getAtlasSprite(still.toString());
                if (sprite != map.getMissingSprite()) return sprite;
            }
        }
        ResourceLocation moltenTexture = MaterialIconType.molten.getBlockTexturePath(material.getMaterialIconSet());
        TextureAtlasSprite sprite = map.getAtlasSprite(moltenTexture.toString());
        return sprite != map.getMissingSprite() ? sprite : map.getAtlasSprite(MOLTEN_FALLBACK.toString());
    }
}
