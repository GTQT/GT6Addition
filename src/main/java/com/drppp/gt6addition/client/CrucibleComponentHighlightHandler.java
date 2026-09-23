package com.drppp.gt6addition.client;

import codechicken.lib.raytracer.IndexedCuboid6;
import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucibleCrossing;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCruciblePouringSpout;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityMold;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

/** Draws the real segmented interaction outline for GT6 crucible tools. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class CrucibleComponentHighlightHandler {

    private CrucibleComponentHighlightHandler() {
    }

    @SubscribeEvent
    public static void drawActualBounds(DrawBlockHighlightEvent event) {
        RayTraceResult target = event.getTarget();
        if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        BlockPos blockPos = target.getBlockPos();
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(event.getPlayer().world, blockPos);
        if (!(metaTileEntity instanceof MetaTileEntityMold)
                && !(metaTileEntity instanceof MetaTileEntityCruciblePouringSpout)
                && !(metaTileEntity instanceof MetaTileEntityCrucibleCrossing)) {
            return;
        }

        List<IndexedCuboid6> outlines = new ArrayList<>();
        metaTileEntity.addCollisionBoundingBox(outlines);
        if (outlines.isEmpty()) {
            return;
        }

        event.setCanceled(true);
        EntityPlayer player = event.getPlayer();
        float partialTicks = event.getPartialTicks();
        double viewX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double viewY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double viewZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        try {
            for (IndexedCuboid6 outline : outlines) {
                AxisAlignedBB bounds = outline.aabb().offset(blockPos).grow(0.002D)
                        .offset(-viewX, -viewY, -viewZ);
                RenderGlobal.drawSelectionBoundingBox(bounds, 0.0F, 0.0F, 0.0F, 0.4F);
            }
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }
}
