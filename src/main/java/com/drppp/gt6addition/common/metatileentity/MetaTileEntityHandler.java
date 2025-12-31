package com.drppp.gt6addition.common.metatileentity;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamberLiquid;
import com.sun.istack.internal.NotNull;
import net.minecraft.util.ResourceLocation;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class MetaTileEntityHandler {

    public static MetaTileEntityCombustionchamber[] HU_BURRING_BOXS = new MetaTileEntityCombustionchamber[8];
    public static MetaTileEntityCombustionchamber[] HU_DENSE_BURRING_BOXS = new MetaTileEntityCombustionchamber[8];
    public static MetaTileEntityCombustionchamberLiquid[] HU_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[8];
    public static MetaTileEntityCombustionchamberLiquid[] HU_DENSE_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[8];

    static int startID = 0;

    public static int getID() {
        startID++;
        return startID;
    }
    public static void InitMte()
    {
        //燃烧室
        for (int i = 0; i < HU_BURRING_BOXS.length; i++) {
            String[] names = {"lead", "bronze", "steel", "invar", "chrome", "titanium", "tungsten", "tungstensteel"};
            int[] color = {0x251945, 0x815024, 0x4F4F4E, 0x87875C, 0xA39393, 0x896495, 0x1D1D1D, 0x3C3C61};
            double[] efficiency = {0.5, 0.75, 0.7, 1, 0.85, 0.85, 1, 0.9};
            int[] output = {16, 24, 32, 16, 112, 96, 128, 128};
            HU_BURRING_BOXS[i] = registerMetaTileEntity(20+i, new MetaTileEntityCombustionchamber(getmyId(names[i] + "_burring_box"), color[i], efficiency[i], output[i], false));
            HU_DENSE_BURRING_BOXS[i] = registerMetaTileEntity(30+i, new MetaTileEntityCombustionchamber(getmyId("dense_" + names[i] + "_burring_box"), color[i], efficiency[i], output[i] * 4, true));
            HU_BURRING_BOXS_LIQUID[i] = registerMetaTileEntity(40+i, new MetaTileEntityCombustionchamberLiquid(getmyId(names[i] + "_burring_box_liquid"), color[i], efficiency[i], (int) (output[i] * 1.5), false));
            HU_DENSE_BURRING_BOXS_LIQUID[i] = registerMetaTileEntity(50+i, new MetaTileEntityCombustionchamberLiquid(getmyId("dense_" + names[i] + "_burring_box_liquid"), color[i], efficiency[i], (int) (output[i] * 4 * 1.5), true));
        }
    }
    public static @NotNull ResourceLocation getmyId(@NotNull String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }
}
