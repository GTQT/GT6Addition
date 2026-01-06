package com.drppp.gt6addition.intergations.top.provider;


import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.api.top.IEnergyOutShow;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamberLiquid;
import gregtech.api.util.GTUtility;
import lombok.var;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
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
        if (GTUtility.getMetaTileEntity(world, iProbeHitData.getPos()) instanceof MetaTileEntityCombustionchamber ) {
            MetaTileEntityCombustionchamber s = (MetaTileEntityCombustionchamber)GTUtility.getMetaTileEntity(world, iProbeHitData.getPos());
            ItemStack item = s.getImportItems().getStackInSlot(0).copy();
            ItemStack itemout = s.getExportItems().getStackInSlot(0).copy();
            iProbeInfo.text(TextFormatting.BOLD + I18n.format("gt6addition.top.work.status") + TextFormatting.GREEN + s.isActive);//"工作状态:"
            iProbeInfo.text(TextFormatting.BOLD + "燃烧速度:" + TextFormatting.GREEN + s.burnSpeed);
            iProbeInfo.text(TextFormatting.BOLD + "燃烧热量:" + TextFormatting.GREEN + s.currentItemHasBurnedTime + "/" + s.currentItemBurnTime);
            iProbeInfo.text(TextFormatting.BOLD + "HU输出:" + TextFormatting.GREEN + s.outPutHu);
            iProbeInfo.text(TextFormatting.BOLD + "缓存物品:" + TextFormatting.GREEN + (item.isEmpty() ? "null" : item.getDisplayName() + "*" + item.getCount()));
            iProbeInfo.text(TextFormatting.BOLD + "灰烬栏状态:" + TextFormatting.GREEN + (itemout.isEmpty() ? "null" : itemout.getCount() + "/64"));
        }
        else if (GTUtility.getMetaTileEntity(world, iProbeHitData.getPos()) instanceof MetaTileEntityCombustionchamberLiquid ) {
            MetaTileEntityCombustionchamberLiquid s = (MetaTileEntityCombustionchamberLiquid)GTUtility.getMetaTileEntity(world, iProbeHitData.getPos());
            var fludi = s.getImportFluids().getTankAt(0).getFluid();
            var itemout = s.getExportFluids().getTankAt(0).getFluid();

            iProbeInfo.text(TextFormatting.BOLD + "工作状态:" + TextFormatting.GREEN + s.isActive);
            iProbeInfo.text(TextFormatting.BOLD + "燃烧速度:" + TextFormatting.GREEN + s.burnSpeed);
            iProbeInfo.text(TextFormatting.BOLD + "燃烧热量:" + TextFormatting.GREEN + s.currentItemHasBurnedTime + "/" + s.currentItemBurnTime);
            iProbeInfo.text(TextFormatting.BOLD + "HU输出:" + TextFormatting.GREEN + s.outPutHu);
            iProbeInfo.text(TextFormatting.BOLD + "缓存流体:" + TextFormatting.GREEN + (fludi==null ? "null" : fludi.getLocalizedName() + "*" + fludi.amount +"/1000"));
            iProbeInfo.text(TextFormatting.BOLD + "输出流体:" + TextFormatting.GREEN + (itemout==null ? "null" : itemout.getLocalizedName() + "*" + itemout.amount+"/1000"));
        }
        if(GTUtility.getMetaTileEntity(world, iProbeHitData.getPos()) instanceof IEnergyOutShow)
        {
            IEnergyOutShow ens =  (IEnergyOutShow)GTUtility.getMetaTileEntity(world, iProbeHitData.getPos());
            iProbeInfo.text(TextFormatting.BOLD + "能量类型:" + TextFormatting.GREEN + ens.getEnergyName());
            iProbeInfo.text(TextFormatting.BOLD + "能量输出:" + TextFormatting.GREEN + ens.getEnergyOut()+ens.getEnergyName()+"/t");
        }
    }

}
