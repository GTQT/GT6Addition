package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.metatileentity.single.ku.KineticRenderHelper;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import net.minecraft.world.World;
import java.util.List;

/**
 * GT6 fluidized-bed burning box. The 1.12 material system has no Calcite fluid,
 * so one Calcite dust is used as the fluidizing additive for each solid-fuel item.
 */
public class MetaTileEntityFluidizedBed extends MetaTileEntityCombustionchamber {
    public MetaTileEntityFluidizedBed(ResourceLocation id, int color, double efficiency,
                                      int outputHu, boolean dense, int baseTier) {
        super(id, color, efficiency, outputHu, dense, baseTier);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, 2);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFluidizedBed(metaTileEntityId, color, efficiency, outPutHu, isDense, baseTier);
    }

    @Override
    public void update() {
        int previousFuelCount = importItems.getStackInSlot(0).getCount();
        super.update();
        if (!getWorld().isRemote) {
            int consumedFuelCount = previousFuelCount - importItems.getStackInSlot(0).getCount();
            if (consumedFuelCount > 0) {
                importItems.extractItem(1, consumedFuelCount, false);
            }
        }
    }

    @Override
    protected boolean canStartFuelCycle() {
        return hasCalcite();
    }

    @Override
    public boolean igniteFromAutomaticIgniter() {
        return hasCalcite() && super.igniteFromAutomaticIgniter();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.hu.fluidized_bed.info"));
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hit) {
        ItemStack held = player.getHeldItem(hand);
        if (!getWorld().isRemote && !held.isEmpty() && isCalcite(held)) {
            ItemStack remainder = importItems.insertItem(1, held.copy(), false);
            held.setCount(remainder.getCount());
            return true;
        }
        return super.onRightClick(player, hand, facing, hit);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] coloured = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color)));
        String root = "gt6addition:blocks/machines/generators/burning_fluidbed/";
        KineticRenderHelper.renderGt6SixSidedCube(state, translation, coloured, getFrontFacing(),
                new codechicken.lib.vec.Cuboid6(0, 0, 0, 1, 1, 1), root + "colored/");
        KineticRenderHelper.renderGt6SixSidedCube(state, translation, pipeline, getFrontFacing(),
                new codechicken.lib.vec.Cuboid6(0, 0, 0, 1, 1, 1),
                root + (isActive() ? "overlay_active/" : "overlay/"));
    }

    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(KineticRenderHelper.getSprite(
                "gt6addition:blocks/machines/generators/burning_fluidbed/colored/top"), color);
    }

    private boolean hasCalcite() {
        return isCalcite(importItems.getStackInSlot(1));
    }

    private static boolean isCalcite(ItemStack stack) {
        MaterialStack material = OreDictUnifier.getMaterial(stack);
        return material != null && material.material == Materials.Calcite;
    }
}
