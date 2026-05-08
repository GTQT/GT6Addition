package com.drppp.gt6addition.intergations.top.provider;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.api.top.IEnergyOutShow;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCastingBasin;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamberLiquid;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCoolingMold;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityTemperatureSensor;
import com.drppp.gt6addition.common.metatileentity.single.item.MetaTileEntityGt6Hopper;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticGearbox;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticSteamEngine;
import gregtech.api.util.GTUtility;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class TopCommonProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return Tags.MOD_ID + ":top_common_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer,
                             World world, IBlockState iBlockState, IProbeHitData iProbeHitData) {
        Object metaTileEntity = GTUtility.getMetaTileEntity(world, iProbeHitData.getPos());
        if (metaTileEntity instanceof MetaTileEntityCrucible) {
            MetaTileEntityCrucible s = (MetaTileEntityCrucible) metaTileEntity;
            ItemStack input = s.getImportItems().getStackInSlot(0).copy();
            long heat = Math.max(0L, s.getCurrentTemperature());
            long heatMax = Math.max(1L, s.getMaxTemperature());
            iProbeInfo.progress(Math.min(heat, heatMax), heatMax, iProbeInfo.defaultProgressStyle()
                    .prefix("\u70ed\u91cf: ")
                    .suffix(" / " + heatMax + " K")
                    .filledColor(0xFFFF6600)
                    .alternateFilledColor(0xFFFFC040)
                    .borderColor(0xFF555555)
                    .backgroundColor(0xFF111111)
                    .numberFormat(NumberFormat.FULL));
            iProbeInfo.text(TextFormatting.BOLD + "\u5bb9\u91cf:" + TextFormatting.GREEN +
                    s.getStoredFluidAmount() + "/" + s.getCapacityFluidAmount() + " L");
            iProbeInfo.text(TextFormatting.BOLD + "\u8f93\u5165\u69fd:" + TextFormatting.GREEN +
                    (input.isEmpty() ? "\u7a7a" : input.getDisplayName() + "*" + input.getCount()));
            if (s.getTopContents().isEmpty()) {
                iProbeInfo.text(TextFormatting.BOLD + "\u5185\u5bb9\u7269:" + TextFormatting.GREEN + "\u7a7a");
            } else {
                iProbeInfo.text(TextFormatting.BOLD + "\u5185\u5bb9\u7269:");
                for (MetaTileEntityCrucible.CrucibleContentInfo content : s.getTopContents()) {
                    ItemStack displayStack = content.getDisplayStack();
                    IProbeInfo line = iProbeInfo.horizontal();
                    if (!displayStack.isEmpty()) {
                        line.item(displayStack);
                    }
                    line.text(TextFormatting.GREEN + content.getMaterialName() + TextFormatting.GRAY + " " +
                            content.getFluidAmount() + "L " +
                            (content.isMolten() ? "\u7194\u878d" : "\u56fa\u6001"));
                }
            }
        } else if (metaTileEntity instanceof MetaTileEntityCastingBasin) {
            MetaTileEntityCastingBasin s = (MetaTileEntityCastingBasin) metaTileEntity;
            iProbeInfo.text(TextFormatting.BOLD + "\u5bb9\u91cf:" + TextFormatting.GREEN +
                    s.getStoredFluidAmount() + "/" + s.getCapacityFluidAmount() + " L");
            iProbeInfo.text(TextFormatting.BOLD + "\u6e29\u5ea6:" + TextFormatting.GREEN + s.getTemperature() + " K");
            String contents = s.getContentsDisplayName();
            iProbeInfo.text(TextFormatting.BOLD + "\u5185\u5bb9\u7269:" + TextFormatting.GREEN +
                    (contents.isEmpty() ? "\u7a7a" : contents + " " +
                            (s.isContentsMolten() ? "\u7194\u878d" : "\u56fa\u6001")));
        } else if (metaTileEntity instanceof MetaTileEntityCoolingMold) {
            MetaTileEntityCoolingMold s = (MetaTileEntityCoolingMold) metaTileEntity;
            int duration = Math.max(1, s.getCoolingDuration());
            iProbeInfo.progress(Math.min(s.getCoolingProgress(), duration), duration, iProbeInfo.defaultProgressStyle()
                    .prefix("\u51b7\u5374: ")
                    .suffix(" / " + duration + " t")
                    .filledColor(0xFF44AAFF)
                    .alternateFilledColor(0xFF88DDFF)
                    .borderColor(0xFF555555)
                    .backgroundColor(0xFF111111)
                    .numberFormat(NumberFormat.FULL));
            iProbeInfo.text(TextFormatting.BOLD + "\u6d41\u4f53:" + TextFormatting.GREEN +
                    (s.getFluidDisplayName().isEmpty() ? "\u7a7a" :
                            s.getFluidDisplayName() + " " + s.getFluidAmount() + "/" + s.getCapacity() + " L"));
            ItemStack mold = s.getMoldStack();
            ItemStack output = s.getOutputStack();
            iProbeInfo.text(TextFormatting.BOLD + "\u6a21\u5177:" + TextFormatting.GREEN +
                    (mold.isEmpty() ? "\u7a7a" : mold.getDisplayName()));
            iProbeInfo.text(TextFormatting.BOLD + "\u8f93\u51fa:" + TextFormatting.GREEN +
                    (output.isEmpty() ? "\u7a7a" : output.getDisplayName() + "*" + output.getCount()));
        } else if (metaTileEntity instanceof MetaTileEntityTemperatureSensor) {
            MetaTileEntityTemperatureSensor s = (MetaTileEntityTemperatureSensor) metaTileEntity;
            long max = Math.max(1L, s.getMaxTemperature());
            iProbeInfo.progress(Math.min(s.getCurrentTemperature(), max), max, iProbeInfo.defaultProgressStyle()
                    .prefix("\u6e29\u5ea6: ")
                    .suffix(" / " + max + " K")
                    .filledColor(0xFFFF5533)
                    .alternateFilledColor(0xFFFFAA44)
                    .borderColor(0xFF555555)
                    .backgroundColor(0xFF111111)
                    .numberFormat(NumberFormat.FULL));
            iProbeInfo.text(TextFormatting.BOLD + "\u6a21\u5f0f:" + TextFormatting.GREEN + s.getModeDisplayName());
            iProbeInfo.text(TextFormatting.BOLD + "\u8bbe\u5b9a\u6e29\u5ea6:" + TextFormatting.GREEN +
                    s.getSetTemperature() + " K");
            iProbeInfo.text(TextFormatting.BOLD + "\u8bfb\u53d6\u9762:" + TextFormatting.GREEN +
                    s.getTargetFacing().getName());
            iProbeInfo.text(TextFormatting.BOLD + "\u7ea2\u77f3\u8f93\u51fa:" + TextFormatting.GREEN +
                    s.getRedstoneOutput());
        } else if (metaTileEntity instanceof MetaTileEntityKineticSteamEngine) {
            MetaTileEntityKineticSteamEngine s = (MetaTileEntityKineticSteamEngine) metaTileEntity;
            iProbeInfo.progress(Math.min(s.getStoredEnergy(), s.getEnergyCapacity()), s.getEnergyCapacity(),
                    iProbeInfo.defaultProgressStyle()
                            .prefix("KU\u7f13\u5b58: ")
                            .suffix(" / " + s.getEnergyCapacity())
                            .filledColor(0xFF66CCFF)
                            .alternateFilledColor(0xFF88EEFF)
                            .borderColor(0xFF555555)
                            .backgroundColor(0xFF111111)
                            .numberFormat(NumberFormat.FULL));
            iProbeInfo.text(TextFormatting.BOLD + "\u72b6\u6001:" + TextFormatting.GREEN +
                    (s.isStopped() ? "\u505c\u6b62" : s.isActive() ? "\u8fd0\u884c" : "\u5f85\u673a"));
            iProbeInfo.text(TextFormatting.BOLD + "\u84b8\u6c7d:" + TextFormatting.GREEN +
                    s.getSteamAmount() + "/" + s.getTankCapacity() + " L");
            iProbeInfo.text(TextFormatting.BOLD + "\u84b8\u998f\u6c34:" + TextFormatting.GREEN +
                    s.getWaterAmount() + "/" + s.getTankCapacity() + " L");
        } else if (metaTileEntity instanceof MetaTileEntityKineticGearbox) {
            MetaTileEntityKineticGearbox s = (MetaTileEntityKineticGearbox) metaTileEntity;
            iProbeInfo.text(TextFormatting.BOLD + "\u72b6\u6001:" + TextFormatting.GREEN +
                    (s.isJammed() ? "\u5361\u6b7b" : s.isActive() ? "\u4f20\u52a8" : "\u7a7a\u8f6c"));
            iProbeInfo.text(TextFormatting.BOLD + "\u500d\u7387:" + TextFormatting.GREEN + s.getRatioName());
        } else if (metaTileEntity instanceof MetaTileEntityGt6Hopper) {
            MetaTileEntityGt6Hopper s = (MetaTileEntityGt6Hopper) metaTileEntity;
            iProbeInfo.progress(s.getUsedSlots(), Math.max(1, s.getSlotCount()), iProbeInfo.defaultProgressStyle()
                    .prefix("\u69fd\u4f4d: ")
                    .suffix(" / " + s.getSlotCount())
                    .filledColor(0xFF77AA66)
                    .alternateFilledColor(0xFF99CC88)
                    .borderColor(0xFF555555)
                    .backgroundColor(0xFF111111)
                    .numberFormat(NumberFormat.FULL));
            iProbeInfo.text(TextFormatting.BOLD + "\u6a21\u5f0f:" + TextFormatting.GREEN +
                    (s.isQueueMode() ? "\u961f\u5217/FIFO" : "\u666e\u901a\u5408\u5e76"));
            iProbeInfo.text(TextFormatting.BOLD + "\u5355\u6b21\u6570\u91cf:" + TextFormatting.GREEN +
                    s.getStackSizeLimit() + (s.isExactMode() ? " exact" : ""));
            iProbeInfo.text(TextFormatting.BOLD + "\u7269\u54c1\u603b\u6570:" + TextFormatting.GREEN +
                    s.getTotalItemCount());
            ItemStack preview = s.getOutputStackPreview();
            if (!preview.isEmpty()) {
                IProbeInfo line = iProbeInfo.horizontal();
                line.item(preview);
                line.text(TextFormatting.BOLD + "\u5f85\u8f93\u51fa:" + TextFormatting.GREEN +
                        preview.getDisplayName() + "*" + preview.getCount());
            }
        } else if (metaTileEntity instanceof MetaTileEntityCombustionchamber) {
            MetaTileEntityCombustionchamber s = (MetaTileEntityCombustionchamber) metaTileEntity;
            ItemStack item = s.getImportItems().getStackInSlot(0).copy();
            ItemStack itemOut = s.getExportItems().getStackInSlot(0).copy();
            iProbeInfo.text(TextFormatting.BOLD + I18n.format("gt6addition.top.work.status") + TextFormatting.GREEN + s.isActive);
            iProbeInfo.text(TextFormatting.BOLD + "\u71c3\u70e7\u901f\u5ea6:" + TextFormatting.GREEN + s.burnSpeed);
            iProbeInfo.text(TextFormatting.BOLD + "\u71c3\u70e7\u70ed\u91cf:" + TextFormatting.GREEN +
                    s.currentItemHasBurnedTime + "/" + s.currentItemBurnTime);
            iProbeInfo.text(TextFormatting.BOLD + "HU\u8f93\u51fa:" + TextFormatting.GREEN + s.outPutHu);
            iProbeInfo.text(TextFormatting.BOLD + "\u7f13\u5b58\u7269\u54c1:" + TextFormatting.GREEN +
                    (item.isEmpty() ? "null" : item.getDisplayName() + "*" + item.getCount()));
            iProbeInfo.text(TextFormatting.BOLD + "\u7070\u70ec\u680f\u72b6\u6001:" + TextFormatting.GREEN +
                    (itemOut.isEmpty() ? "null" : itemOut.getCount() + "/64"));
        } else if (metaTileEntity instanceof MetaTileEntityCombustionchamberLiquid) {
            MetaTileEntityCombustionchamberLiquid s = (MetaTileEntityCombustionchamberLiquid) metaTileEntity;
            FluidStack fluidIn = s.getImportFluids().getTankAt(0).getFluid();
            FluidStack fluidOut = s.getExportFluids().getTankAt(0).getFluid();
            iProbeInfo.text(TextFormatting.BOLD + "\u5de5\u4f5c\u72b6\u6001:" + TextFormatting.GREEN + s.isActive);
            iProbeInfo.text(TextFormatting.BOLD + "\u71c3\u70e7\u901f\u5ea6:" + TextFormatting.GREEN + s.burnSpeed);
            iProbeInfo.text(TextFormatting.BOLD + "\u71c3\u70e7\u70ed\u91cf:" + TextFormatting.GREEN +
                    s.currentItemHasBurnedTime + "/" + s.currentItemBurnTime);
            iProbeInfo.text(TextFormatting.BOLD + "HU\u8f93\u51fa:" + TextFormatting.GREEN + s.outPutHu);
            iProbeInfo.text(TextFormatting.BOLD + "\u7f13\u5b58\u6d41\u4f53:" + TextFormatting.GREEN +
                    (fluidIn == null ? "null" : fluidIn.getLocalizedName() + "*" + fluidIn.amount + "/1000"));
            iProbeInfo.text(TextFormatting.BOLD + "\u8f93\u51fa\u6d41\u4f53:" + TextFormatting.GREEN +
                    (fluidOut == null ? "null" : fluidOut.getLocalizedName() + "*" + fluidOut.amount + "/1000"));
        }
        if (metaTileEntity instanceof IEnergyOutShow) {
            IEnergyOutShow energyOutShow = (IEnergyOutShow) metaTileEntity;
            iProbeInfo.text(TextFormatting.BOLD + "\u80fd\u91cf\u7c7b\u578b:" + TextFormatting.GREEN +
                    energyOutShow.getEnergyName());
            iProbeInfo.text(TextFormatting.BOLD + "\u80fd\u91cf\u8f93\u51fa:" + TextFormatting.GREEN +
                    energyOutShow.getEnergyOut() + energyOutShow.getEnergyName() + "/t");
        }
    }
}