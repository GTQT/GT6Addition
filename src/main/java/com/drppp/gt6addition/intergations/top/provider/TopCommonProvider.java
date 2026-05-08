package com.drppp.gt6addition.intergations.top.provider;


import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.api.top.IEnergyOutShow;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamberLiquid;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCastingBasin;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCoolingMold;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import gregtech.api.util.GTUtility;
import lombok.var;
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

public class TopCommonProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Tags.MOD_ID + ":top_common_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData) {
        boolean flag = false;
        Object metaTileEntity = GTUtility.getMetaTileEntity(world, iProbeHitData.getPos());
        if (metaTileEntity instanceof MetaTileEntityCrucible ) {
            MetaTileEntityCrucible s = (MetaTileEntityCrucible)metaTileEntity;
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
            iProbeInfo.text(TextFormatting.BOLD + "\u5bb9\u91cf:" + TextFormatting.GREEN + s.getStoredFluidAmount() + "/" + s.getCapacityFluidAmount() + " L");
            iProbeInfo.text(TextFormatting.BOLD + "\u8f93\u5165\u69fd:" + TextFormatting.GREEN + (input.isEmpty() ? "\u7a7a" : input.getDisplayName() + "*" + input.getCount()));
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
                    line.text(TextFormatting.GREEN + content.getMaterialName() + TextFormatting.GRAY + " " + content.getFluidAmount() + "L " + (content.isMolten() ? "\u7194\u878d" : "\u56fa\u6001"));
                }
            }
        }
        else if (metaTileEntity instanceof MetaTileEntityCastingBasin) {
            MetaTileEntityCastingBasin s = (MetaTileEntityCastingBasin) metaTileEntity;
            iProbeInfo.text(TextFormatting.BOLD + "容量:" + TextFormatting.GREEN + s.getStoredFluidAmount() + "/" + s.getCapacityFluidAmount() + " L");
            iProbeInfo.text(TextFormatting.BOLD + "温度:" + TextFormatting.GREEN + s.getTemperature() + " K");
            String contents = s.getContentsDisplayName();
            iProbeInfo.text(TextFormatting.BOLD + "内容物:" + TextFormatting.GREEN + (contents.isEmpty() ? "空" : contents + " " + (s.isContentsMolten() ? "熔融" : "固态")));
        }
        else if (metaTileEntity instanceof MetaTileEntityCoolingMold) {
            MetaTileEntityCoolingMold s = (MetaTileEntityCoolingMold) metaTileEntity;
            int duration = Math.max(1, s.getCoolingDuration());
            iProbeInfo.progress(Math.min(s.getCoolingProgress(), duration), duration, iProbeInfo.defaultProgressStyle()
                    .prefix("冷却: ")
                    .suffix(" / " + duration + " t")
                    .filledColor(0xFF44AAFF)
                    .alternateFilledColor(0xFF88DDFF)
                    .borderColor(0xFF555555)
                    .backgroundColor(0xFF111111)
                    .numberFormat(NumberFormat.FULL));
            iProbeInfo.text(TextFormatting.BOLD + "流体:" + TextFormatting.GREEN + (s.getFluidDisplayName().isEmpty() ? "空" : s.getFluidDisplayName() + " " + s.getFluidAmount() + "/" + s.getCapacity() + " L"));
            ItemStack mold = s.getMoldStack();
            ItemStack output = s.getOutputStack();
            iProbeInfo.text(TextFormatting.BOLD + "模具:" + TextFormatting.GREEN + (mold.isEmpty() ? "空" : mold.getDisplayName()));
            iProbeInfo.text(TextFormatting.BOLD + "输出:" + TextFormatting.GREEN + (output.isEmpty() ? "空" : output.getDisplayName() + "*" + output.getCount()));
        }
        else if (metaTileEntity instanceof MetaTileEntityCombustionchamber ) {
            MetaTileEntityCombustionchamber s = (MetaTileEntityCombustionchamber)metaTileEntity;
            ItemStack item = s.getImportItems().getStackInSlot(0).copy();
            ItemStack itemout = s.getExportItems().getStackInSlot(0).copy();
            iProbeInfo.text(TextFormatting.BOLD + I18n.format("gt6addition.top.work.status") + TextFormatting.GREEN + s.isActive);//"工作状态:"
            iProbeInfo.text(TextFormatting.BOLD + "燃烧速度:" + TextFormatting.GREEN + s.burnSpeed);
            iProbeInfo.text(TextFormatting.BOLD + "燃烧热量:" + TextFormatting.GREEN + s.currentItemHasBurnedTime + "/" + s.currentItemBurnTime);
            iProbeInfo.text(TextFormatting.BOLD + "HU输出:" + TextFormatting.GREEN + s.outPutHu);
            iProbeInfo.text(TextFormatting.BOLD + "缓存物品:" + TextFormatting.GREEN + (item.isEmpty() ? "null" : item.getDisplayName() + "*" + item.getCount()));
            iProbeInfo.text(TextFormatting.BOLD + "灰烬栏状态:" + TextFormatting.GREEN + (itemout.isEmpty() ? "null" : itemout.getCount() + "/64"));
        }
        else if (metaTileEntity instanceof MetaTileEntityCombustionchamberLiquid ) {
            MetaTileEntityCombustionchamberLiquid s = (MetaTileEntityCombustionchamberLiquid)metaTileEntity;
            var fludi = s.getImportFluids().getTankAt(0).getFluid();
            var itemout = s.getExportFluids().getTankAt(0).getFluid();

            iProbeInfo.text(TextFormatting.BOLD + "工作状态:" + TextFormatting.GREEN + s.isActive);
            iProbeInfo.text(TextFormatting.BOLD + "燃烧速度:" + TextFormatting.GREEN + s.burnSpeed);
            iProbeInfo.text(TextFormatting.BOLD + "燃烧热量:" + TextFormatting.GREEN + s.currentItemHasBurnedTime + "/" + s.currentItemBurnTime);
            iProbeInfo.text(TextFormatting.BOLD + "HU输出:" + TextFormatting.GREEN + s.outPutHu);
            iProbeInfo.text(TextFormatting.BOLD + "缓存流体:" + TextFormatting.GREEN + (fludi==null ? "null" : fludi.getLocalizedName() + "*" + fludi.amount +"/1000"));
            iProbeInfo.text(TextFormatting.BOLD + "输出流体:" + TextFormatting.GREEN + (itemout==null ? "null" : itemout.getLocalizedName() + "*" + itemout.amount+"/1000"));
        }
        if(metaTileEntity instanceof IEnergyOutShow)
        {
            IEnergyOutShow ens =  (IEnergyOutShow)metaTileEntity;
            iProbeInfo.text(TextFormatting.BOLD + "能量类型:" + TextFormatting.GREEN + ens.getEnergyName());
            iProbeInfo.text(TextFormatting.BOLD + "能量输出:" + TextFormatting.GREEN + ens.getEnergyOut()+ens.getEnergyName()+"/t");
        }
    }

}
