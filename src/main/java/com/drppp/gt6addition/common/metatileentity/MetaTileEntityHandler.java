package com.drppp.gt6addition.common.metatileentity;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.api.baseMTile.MetaTileEntityHUOvenMachine;
import com.drppp.gt6addition.api.baseMTile.MetaTileEntityMutiEnergyMachine;
import com.drppp.gt6addition.api.utils.EnergyTypeList;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamberLiquid;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntityDieselEngine;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntityElectricMotor;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntitySteamTurbine;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class MetaTileEntityHandler {

    public static MetaTileEntityCombustionchamber[] HU_BURRING_BOXS = new MetaTileEntityCombustionchamber[8];
    public static MetaTileEntityCombustionchamber[] HU_DENSE_BURRING_BOXS = new MetaTileEntityCombustionchamber[8];
    public static MetaTileEntityCombustionchamberLiquid[] HU_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[8];
    public static MetaTileEntityCombustionchamberLiquid[] HU_DENSE_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[8];
    //RU系列机器 青铜、钢、不锈钢、钛、钨钢
    //卷板机 线材轧机 车床 切割机 离心 织布 洗矿 搅拌
    public static MetaTileEntityMutiEnergyMachine[] METAL_BENDER_RU= new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] WIREMILLS_RU= new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] LATHE_RU= new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] CUTTING_SAW_RU= new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] CENTRIFUGE_RU= new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] LOOM_RU= new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] ORE_WASHER_RU= new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] MIXER_RU= new MetaTileEntityMutiEnergyMachine[5];
    //KU机器 青铜、钢、不锈钢、钛、钨钢
    //压缩机 冲压机床 筛选 破碎机(新配方)

    //KU 蒸汽动力引擎、电力动力引擎、旋转动能转换器 2:1

    //HU机器 钢、因瓦、钛、碳化钨
    //熔炉  蒸馏室  压模 过胶机 高压釜(结晶)  焙烧  发酵
    public static MetaTileEntityHUOvenMachine[] OVEN_HU = new MetaTileEntityHUOvenMachine[4];

    //蒸汽涡轮机 SU->RU    电动机EU->RU   燃油引擎 燃料->RU
    public static MetaTileEntitySteamTurbine[] STEAM_TURBINES = new MetaTileEntitySteamTurbine[8];
    public static MetaTileEntityElectricMotor[] ELECTRIC_MOTOR = new MetaTileEntityElectricMotor[5];
    public static MetaTileEntityDieselEngine[] DIESEL_ENGINE = new MetaTileEntityDieselEngine[5];
    static int startID = 0;

    public static int getID() {
        startID++;
        return startID;
    }
    public static void InitMte()
    {
        String[] names = {"lead", "bronze", "steel", "invar", "chrome", "titanium", "tungsten", "tungstensteel"};
        //燃烧室
        for (int i = 0; i < HU_BURRING_BOXS.length; i++) {
            int[] color = {0x251945, 0x815024, 0x4F4F4E, 0x87875C, 0xA39393, 0x896495, 0x1D1D1D, 0x3C3C61};
            double[] efficiency = {0.5, 0.75, 0.7, 1, 0.85, 0.85, 1, 0.9};
            int[] output = {16, 24, 32, 16, 112, 96, 128, 128};
            HU_BURRING_BOXS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamber(getmyId(names[i] + "_burring_box"), color[i], efficiency[i], output[i], false));
            HU_DENSE_BURRING_BOXS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamber(getmyId("dense_" + names[i] + "_burring_box"), color[i], efficiency[i], output[i] * 4, true));
            HU_BURRING_BOXS_LIQUID[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamberLiquid(getmyId(names[i] + "_burring_box_liquid"), color[i], efficiency[i], (int) (output[i] * 1.5), false));
            HU_DENSE_BURRING_BOXS_LIQUID[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamberLiquid(getmyId("dense_" + names[i] + "_burring_box_liquid"), color[i], efficiency[i], (int) (output[i] * 4 * 1.5), true));
        }
        for (int i = 0; i < STEAM_TURBINES.length; i++) {
            int[] color = {0x251945, 0x815024, 0x4F4F4E, 0x87875C, 0xA39393, 0x896495, 0x1D1D1D, 0x3C3C61};
            int[] output = {16, 24, 32, 16, 112, 96, 128, 128};
            int[] out_inventry = {8000, 8000, 8000, (int)(8000*1.5), 8000*2, 8000*2, 8000*2, 8000*2};
            STEAM_TURBINES[i] = registerMetaTileEntity(getID(), new MetaTileEntitySteamTurbine(getmyId(names[i] + "_steam_turbine"), color[i], 0.66, output[i],out_inventry[i]));
        }

        //电动机  钢 铝 不锈钢 钛 钨钢
        int[] electric_motor_color = {0x000000, 0x4F4F4E, 0x8bd4d2, 0x90a5b6, 0x896495,0x3C3C61};
        for (int i = 1; i <= 5; i++) {
            ELECTRIC_MOTOR[i-1] = registerMetaTileEntity(getID(),new MetaTileEntityElectricMotor(getmyId("electric_motor."+ GTValues.VN[i]),i,electric_motor_color[i],0.8,(int)GTValues.V[i]));
        }//燃油引擎  钢 铝 不锈钢 钛 钨钢
        for (int i = 1; i <= 5; i++) {
            DIESEL_ENGINE[i-1] = registerMetaTileEntity(getID(),new MetaTileEntityDieselEngine(getmyId("diesel_engine."+ GTValues.VN[i]),electric_motor_color[i],GTValues.VH[i]*3));
        }

        //卷板
        String[] level_names = { "bronze", "steel", "stainlesssteel",  "titanium", "tungstensteel"};
        for (int i = 0; i < 5; i++) {
            METAL_BENDER_RU[i] =  registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getmyId("ru_bender_"+level_names[i]), RecipeMaps.BENDER_RECIPES, Gt6AdditionTextures.RU_BENDER, 1+i, true, EnergyTypeList.RU,new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.LEFT, MachineEnergyAcceptFacing.RIGHT}));
        }
        //线材
        for (int i = 0; i < 5; i++) {
            WIREMILLS_RU[i] =  registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getmyId("ru_wiremill_"+level_names[i]), RecipeMaps.WIREMILL_RECIPES, Gt6AdditionTextures.RU_WIREMILL, 1+i, true, EnergyTypeList.RU,new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.LEFT, MachineEnergyAcceptFacing.RIGHT}));
        }
        //车床
        for (int i = 0; i < 5; i++) {
            LATHE_RU[i] =  registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getmyId("ru_lathe_"+level_names[i]), RecipeMaps.LATHE_RECIPES, Gt6AdditionTextures.RU_LATHE, 1+i, true, EnergyTypeList.RU,new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}));
        }
        //切割机
        for (int i = 0; i < 5; i++) {
            CUTTING_SAW_RU[i] =  registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getmyId("ru_cutter_"+level_names[i]), RecipeMaps.CUTTER_RECIPES, Gt6AdditionTextures.RU_CUTTING_SAW, 1+i, true, EnergyTypeList.RU,new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.BACK}));
        }
        //离心
        for (int i = 0; i < 5; i++) {
            CENTRIFUGE_RU[i] =  registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getmyId("ru_centrifuge_"+level_names[i]), RecipeMaps.CENTRIFUGE_RECIPES, Gt6AdditionTextures.RU_CENTRIFUGE, 1+i, true, EnergyTypeList.RU,new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}));
        }
        //织布
        for (int i = 0; i < 5; i++) {
            LOOM_RU[i] =  registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getmyId("ru_loom_"+level_names[i]), RecipeMaps.LOOM_RECIPES, Gt6AdditionTextures.RU_LOOM, 1+i, true, EnergyTypeList.RU,new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.LEFT, MachineEnergyAcceptFacing.RIGHT}));
        }
        //洗矿
        for (int i = 0; i < 5; i++) {
            ORE_WASHER_RU[i] =  registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getmyId("ru_orewasher_"+level_names[i]), RecipeMaps.ORE_WASHER_RECIPES, Gt6AdditionTextures.RU_ORE_WASHER, 1+i, true, EnergyTypeList.RU,new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.BACK}));
        }
        //搅拌
        for (int i = 0; i < 5; i++) {
            MIXER_RU[i] =  registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getmyId("ru_mixer_"+level_names[i]), RecipeMaps.MIXER_RECIPES, Gt6AdditionTextures.RU_MIXER, 1+i, true, EnergyTypeList.RU,new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}));
        }
        String[] huName = { "steel", "invar",  "titanium", "tungstencarbide"};
        int[] huColor = { 0x4F4F4E, 0x87875C, 0x896495,0x4F4F4F};
        for (int i = 1; i <=4 ; i++) {
            OVEN_HU[i-1] = registerMetaTileEntity(getID(),new MetaTileEntityHUOvenMachine(getmyId("hu_oven_"+huName[i-1]),RecipeMaps.FURNACE_RECIPES,Gt6AdditionTextures.HU_OVEN,i,false,EnergyTypeList.HU,new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN},huColor[i-1]));
        }
    }
    public static @NotNull ResourceLocation getmyId(@NotNull String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }
}
