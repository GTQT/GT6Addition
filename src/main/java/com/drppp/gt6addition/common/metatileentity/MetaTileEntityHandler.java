package com.drppp.gt6addition.common.metatileentity;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.api.baseMTile.MetaTileEntityColorMachine;
import com.drppp.gt6addition.api.baseMTile.MetaTileEntityColorOvenMachine;
import com.drppp.gt6addition.api.baseMTile.MetaTileEntityMutiEnergyMachine;
import com.drppp.gt6addition.api.utils.EnergyTypeList;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import com.drppp.gt6addition.api.utils.MaterialColorUtil;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.metatileentity.single.cu.MetaTileEntityThermoelectricCooler;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamberLiquid;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCastingBasin;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCoolingMold;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucibleFaucet;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityTemperatureSensor;
import com.drppp.gt6addition.common.metatileentity.single.item.MetaTileEntityGt6Hopper;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticAxle;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticGearbox;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticSteamEngine;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityRotationEngine;
import com.drppp.gt6addition.common.metatileentity.single.mu.MetaTileEntityElectromagnet;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntityDieselEngine;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntityElectricMotor;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntitySteamTurbine;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class MetaTileEntityHandler {

    public static MetaTileEntityCombustionchamber[] HU_BURRING_BOXS = new MetaTileEntityCombustionchamber[8];
    public static MetaTileEntityCombustionchamber[] HU_DENSE_BURRING_BOXS = new MetaTileEntityCombustionchamber[8];
    public static MetaTileEntityCombustionchamberLiquid[] HU_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[8];
    public static MetaTileEntityCombustionchamberLiquid[] HU_DENSE_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[8];

    public static MetaTileEntityMutiEnergyMachine[] METAL_BENDER_RU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] WIREMILLS_RU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] LATHE_RU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] CUTTING_SAW_RU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] CENTRIFUGE_RU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] LOOM_RU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] ORE_WASHER_RU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] MIXER_RU = new MetaTileEntityMutiEnergyMachine[5];

    public static MetaTileEntityMutiEnergyMachine[] COMPRESSOR_KU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] FORMING_PRESS_KU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] HAMMER_KU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityMutiEnergyMachine[] SIFTER_KU = new MetaTileEntityMutiEnergyMachine[5];
    public static MetaTileEntityRotationEngine[] RU_KU_ENGINE = new MetaTileEntityRotationEngine[5];
    public static MetaTileEntityKineticSteamEngine[] KINETIC_STEAM_ENGINES = new MetaTileEntityKineticSteamEngine[5];
    public static MetaTileEntityKineticGearbox[] KINETIC_GEARBOXES = new MetaTileEntityKineticGearbox[5];
    public static MetaTileEntityKineticGearbox[] ADJUSTABLE_KINETIC_GEARBOXES = new MetaTileEntityKineticGearbox[5];
    public static MetaTileEntityKineticAxle[] KINETIC_AXLES = new MetaTileEntityKineticAxle[22];
    public static MetaTileEntityGt6Hopper[] GT6_HOPPERS = new MetaTileEntityGt6Hopper[5];
    public static MetaTileEntityGt6Hopper[] GT6_QUEUE_HOPPERS = new MetaTileEntityGt6Hopper[5];

    public static MetaTileEntityColorOvenMachine[] OVEN_HU = new MetaTileEntityColorOvenMachine[4];
    public static MetaTileEntityColorMachine[] DISTILLERY_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityColorMachine[] EXTRUDER_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityColorMachine[] LAMINATOR_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityColorMachine[] ROASTER_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityColorMachine[] FERMENTER_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityCrucible[] CRUCIBLE_HU = new MetaTileEntityCrucible[22];
    public static MetaTileEntityCrucibleFaucet[] CRUCIBLE_FAUCETS = new MetaTileEntityCrucibleFaucet[22];
    public static MetaTileEntityCastingBasin[] CASTING_BASINS = new MetaTileEntityCastingBasin[22];
    public static MetaTileEntityCoolingMold[] COOLING_MOLDS = new MetaTileEntityCoolingMold[22];
    public static MetaTileEntityTemperatureSensor TEMPERATURE_SENSOR;

    public static MetaTileEntitySteamTurbine[] STEAM_TURBINES = new MetaTileEntitySteamTurbine[8];
    public static MetaTileEntityElectricMotor[] ELECTRIC_MOTOR = new MetaTileEntityElectricMotor[5];
    public static MetaTileEntityDieselEngine[] DIESEL_ENGINE = new MetaTileEntityDieselEngine[5];

    public static MetaTileEntityElectromagnet[] ELECTROMAGNET = new MetaTileEntityElectromagnet[5];
    public static MetaTileEntityColorMachine[] POLARIZER = new MetaTileEntityColorMachine[5];
    public static MetaTileEntityColorMachine[] SEPARATOR = new MetaTileEntityColorMachine[5];
    public static MetaTileEntityThermoelectricCooler[] THERMOELECTRIC_COOLER = new MetaTileEntityThermoelectricCooler[5];

    static int startID = 0;

    public static int getID() {
        startID++;
        return startID;
    }

    public static void InitMte() {
        String[] names = {"lead", "bronze", "steel", "invar", "chrome", "titanium", "tungsten", "tungstensteel"};

        for (int i = 0; i < STEAM_TURBINES.length; i++) {
            int[] color = {0x251945, 0x815024, 0x4F4F4E, 0x87875C, 0xA39393, 0x896495, 0x1D1D1D, 0x3C3C61};
            int[] output = {8, 16, 64, 64, 96, 256, 384, 512};
            int[] outInventory = {8000, 8000, 8000, (int) (8000 * 1.5), 8000 * 2, 8000 * 2, 8000 * 2, 8000 * 2};
            STEAM_TURBINES[i] = registerMetaTileEntity(getID(), new MetaTileEntitySteamTurbine(getMyId(names[i] + "_steam_turbine"), color[i], 0.66, output[i], outInventory[i]));
        }

        int[] electricMotorColor = {0x000000, MaterialColorUtil.MaterialColor.get(MaterialColorUtil.MaterialName.steel), 0x8bd4d2, 0x90a5b6, 0x896495, 0x3C3C61};
        for (int i = 1; i <= 5; i++) {
            ELECTRIC_MOTOR[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityElectricMotor(getMyId("electric_motor." + GTValues.VN[i]), i, electricMotorColor[i], 0.8, (int) GTValues.V[i]));
        }
        for (int i = 1; i <= 5; i++) {
            DIESEL_ENGINE[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityDieselEngine(getMyId("diesel_engine." + GTValues.VN[i]), electricMotorColor[i], GTValues.VH[i] * 3));
        }

        String[] levelNames = {"bronze", "steel", "stainlesssteel", "titanium", "tungstensteel"};
        int[] ruKuEngineColor = {0x000000, 0x815024, 0x4F4F4E, 0x90a5b6, 0x896495, 0x3C3C61};
        for (int i = 1; i <= 5; i++) {
            RU_KU_ENGINE[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityRotationEngine(getMyId("ru_ku_engine." + GTValues.VN[i]), ruKuEngineColor[i], (int) GTValues.V[i]));
        }
        for (int i = 1; i <= 5; i++) {
            int throughput = (int) GTValues.V[i];
            KINETIC_STEAM_ENGINES[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityKineticSteamEngine(getMyId("kinetic_steam_engine." + GTValues.VN[i]), ruKuEngineColor[i], throughput, 80));
            KINETIC_GEARBOXES[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityKineticGearbox(getMyId("kinetic_gearbox." + GTValues.VN[i]), ruKuEngineColor[i], throughput * 2, false));
            ADJUSTABLE_KINETIC_GEARBOXES[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityKineticGearbox(getMyId("adjustable_kinetic_gearbox." + GTValues.VN[i]), ruKuEngineColor[i], throughput * 2, true));
        }
        int[] hopperSlots = {3, 5, 9, 12, 27};
        for (int i = 0; i < 5; i++) {
            GT6_HOPPERS[i] = registerMetaTileEntity(getID(), new MetaTileEntityGt6Hopper(
                    getMyId("gt6_hopper_" + levelNames[i]),
                    ruKuEngineColor[i + 1],
                    hopperSlots[i],
                    false));
            GT6_QUEUE_HOPPERS[i] = registerMetaTileEntity(getID(), new MetaTileEntityGt6Hopper(
                    getMyId("gt6_queue_hopper_" + levelNames[i]),
                    ruKuEngineColor[i + 1],
                    Math.max(2, hopperSlots[i]),
                    true));
        }

        int[] muColor = {getColor(MaterialColorUtil.MaterialName.galvanized_steel), getColor(MaterialColorUtil.MaterialName.aluminum), getColor(MaterialColorUtil.MaterialName.stain_steel), getColor(MaterialColorUtil.MaterialName.titanium), getColor(MaterialColorUtil.MaterialName.tungsten_steel)};
        for (int i = 0; i < 5; i++) {
            ELECTROMAGNET[i] = registerMetaTileEntity(getID(), new MetaTileEntityElectromagnet(getMyId("electromagnet." + GTValues.VN[i + 1]), i + 1, muColor[i], 0.9, (int) GTValues.V[i + 1]));
        }
        for (int i = 0; i < 5; i++) {
            THERMOELECTRIC_COOLER[i] = registerMetaTileEntity(getID(), new MetaTileEntityThermoelectricCooler(getMyId("thermoelectric_cooler." + GTValues.VN[i + 1]), i + 1, muColor[i], 0.5, (int) GTValues.VH[i + 1]));
        }

        for (int i = 0; i < 5; i++) {
            METAL_BENDER_RU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ru_bender_" + levelNames[i]), RecipeMaps.BENDER_RECIPES, Gt6AdditionTextures.RU_BENDER, 1 + i, true, EnergyTypeList.RU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.LEFT, MachineEnergyAcceptFacing.RIGHT}));
        }
        for (int i = 0; i < 5; i++) {
            WIREMILLS_RU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ru_wiremill_" + levelNames[i]), RecipeMaps.WIREMILL_RECIPES, Gt6AdditionTextures.RU_WIREMILL, 1 + i, true, EnergyTypeList.RU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.LEFT, MachineEnergyAcceptFacing.RIGHT}));
        }
        for (int i = 0; i < 5; i++) {
            LATHE_RU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ru_lathe_" + levelNames[i]), RecipeMaps.LATHE_RECIPES, Gt6AdditionTextures.RU_LATHE, 1 + i, true, EnergyTypeList.RU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}));
        }
        for (int i = 0; i < 5; i++) {
            CUTTING_SAW_RU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ru_cutter_" + levelNames[i]), RecipeMaps.CUTTER_RECIPES, Gt6AdditionTextures.RU_CUTTING_SAW, 1 + i, true, EnergyTypeList.RU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.BACK}));
        }
        for (int i = 0; i < 5; i++) {
            CENTRIFUGE_RU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ru_centrifuge_" + levelNames[i]), RecipeMaps.CENTRIFUGE_RECIPES, Gt6AdditionTextures.RU_CENTRIFUGE, 1 + i, true, EnergyTypeList.RU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}));
        }
        for (int i = 0; i < 5; i++) {
            LOOM_RU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ru_loom_" + levelNames[i]), RecipeMaps.LOOM_RECIPES, Gt6AdditionTextures.RU_LOOM, 1 + i, true, EnergyTypeList.RU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.LEFT, MachineEnergyAcceptFacing.RIGHT}));
        }
        for (int i = 0; i < 5; i++) {
            ORE_WASHER_RU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ru_orewasher_" + levelNames[i]), RecipeMaps.ORE_WASHER_RECIPES, Gt6AdditionTextures.RU_ORE_WASHER, 1 + i, true, EnergyTypeList.RU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.BACK}));
        }
        for (int i = 0; i < 5; i++) {
            MIXER_RU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ru_mixer_" + levelNames[i]), RecipeMaps.MIXER_RECIPES, Gt6AdditionTextures.RU_MIXER, 1 + i, true, EnergyTypeList.RU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}));
        }
        for (int i = 0; i < 5; i++) {
            COMPRESSOR_KU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ku_compressor_" + levelNames[i]), RecipeMaps.COMPRESSOR_RECIPES, Gt6AdditionTextures.KU_COMPRESSOR, 1 + i, true, EnergyTypeList.KU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.LEFT, MachineEnergyAcceptFacing.RIGHT}, (i + 1) * 4));
        }
        for (int i = 0; i < 5; i++) {
            FORMING_PRESS_KU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ku_forming_press_" + levelNames[i]), RecipeMaps.FORMING_PRESS_RECIPES, Gt6AdditionTextures.KU_FORMING_PRESS, 1 + i, true, EnergyTypeList.KU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.UP}, (i + 1) * 4));
        }
        for (int i = 0; i < 5; i++) {
            HAMMER_KU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ku_hammer_" + levelNames[i]), RecipeMaps.FORGE_HAMMER_RECIPES, Gt6AdditionTextures.KU_HAMMER, 1 + i, true, EnergyTypeList.KU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.LEFT, MachineEnergyAcceptFacing.RIGHT}, (i + 1) * 4));
        }
        for (int i = 0; i < 5; i++) {
            SIFTER_KU[i] = registerMetaTileEntity(getID(), new MetaTileEntityMutiEnergyMachine(getMyId("ku_sifter_" + levelNames[i]), RecipeMaps.SIFTER_RECIPES, Gt6AdditionTextures.KU_SIFTER, 1 + i, true, EnergyTypeList.KU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.BACK}, (i + 1) * 4));
        }

        String[] huName = {"steel", "invar", "titanium", "tungstencarbide"};
        int[] huColor = {0x4F4F4E, 0x87875C, 0x896495, 0x4F4F4F};
        for (int i = 1; i <= 4; i++) {
            OVEN_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorOvenMachine(getMyId("hu_oven_" + huName[i - 1]), RecipeMaps.FURNACE_RECIPES, Gt6AdditionTextures.HU_OVEN, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, huColor[i - 1]));
        }
        for (int i = 1; i <= 4; i++) {
            DISTILLERY_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_distillery_" + huName[i - 1]), RecipeMaps.DISTILLERY_RECIPES, Gt6AdditionTextures.HU_DISTILLERY, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, huColor[i - 1], 8));
        }
        for (int i = 1; i <= 4; i++) {
            EXTRUDER_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_extruder_" + huName[i - 1]), RecipeMaps.EXTRUDER_RECIPES, Gt6AdditionTextures.HU_EXTRUDER, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, huColor[i - 1]));
        }
        for (int i = 1; i <= 4; i++) {
            LAMINATOR_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_laminator_" + huName[i - 1]), RecipeMaps.LAMINATOR_RECIPES, Gt6AdditionTextures.HU_LAMINATOR, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, huColor[i - 1]));
        }
        for (int i = 1; i <= 4; i++) {
            ROASTER_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_roaster_" + huName[i - 1]), RecipeMaps.ROASTER_RECIPES, Gt6AdditionTextures.HU_ROASTER, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, huColor[i - 1]));
        }
        for (int i = 1; i <= 4; i++) {
            FERMENTER_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_fermenter_" + huName[i - 1]), RecipeMaps.FERMENTING_RECIPES, Gt6AdditionTextures.HU_FERMENTER, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, huColor[i - 1]));
        }

        String[] crucibleNames = {
                "stone", "basalt", "graniteblack", "granitered", "quartz", "carbon",
                "bronze", "invar", "steel", "stainlesssteel", "titanium", "chrome",
                "molybdenum", "niobium", "tantalum", "osmium", "iridium", "niobium_titanium",
                "vanadium", "tungsten", "tungstensteel", "tungstencarbide"
        };
        Material[] crucibleMaterials = {
                Materials.Stone, Materials.Basalt, Materials.GraniteBlack, Materials.GraniteRed, Materials.NetherQuartz, Materials.Carbon,
                Materials.Bronze, Materials.Invar, Materials.Steel, Materials.StainlessSteel, Materials.Titanium, Materials.Chrome,
                Materials.Molybdenum, Materials.Niobium, Materials.Tantalum, Materials.Osmium, Materials.Iridium, Materials.NiobiumTitanium,
                Materials.Vanadium, Materials.Tungsten, Materials.TungstenSteel, Materials.TungstenCarbide
        };
        int[] crucibleTiers = {1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5};
        int[] crucibleFallbackColors = {
                0x7A7A7A, 0x2F2F35, 0x1E1E1E, 0x9F4A3A, 0xE5E5D8, 0x141414,
                0x815024, 0x87875C, 0x4F4F4E, 0x90A5B6, 0x896495, 0xA39393,
                0xAAA7B8, 0x928FBC, 0x7D7181, 0x6E86A2, 0xDAD3CF, 0x464C5F,
                0x506A56, 0x1D1D1D, 0x3C3C61, 0x4F4F4F
        };
        boolean[] crucibleAcidProof = {
                false, false, false, false, false, false,
                false, false, false, true, false, true,
                false, false, false, false, true, false,
                false, true, false, true
        };
        float[] crucibleHardness = {
                5.0F, 15.0F, 15.0F, 15.0F, 5.0F, 10.0F,
                7.0F, 4.0F, 6.0F, 6.0F, 9.0F, 9.0F,
                9.0F, 9.0F, 9.0F, 9.0F, 9.0F, 9.0F,
                9.0F, 10.0F, 10.0F, 10.0F
        };
        float[] crucibleResistance = crucibleHardness;
        for (int i = 0; i < CRUCIBLE_HU.length; i++) {
            Material material = crucibleMaterials[i];
            CRUCIBLE_HU[i] = registerMetaTileEntity(getID(), new MetaTileEntityCrucible(
                    getMyId("hu_crucible_" + crucibleNames[i]),
                    crucibleTiers[i],
                    getCrucibleColor(material, crucibleFallbackColors[i]),
                    getCrucibleMaxTemperature(material),
                    crucibleAcidProof[i],
                    crucibleHardness[i],
                    crucibleResistance[i]));
            CRUCIBLE_FAUCETS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCrucibleFaucet(
                    getMyId("hu_crucible_faucet_" + crucibleNames[i]),
                    crucibleTiers[i],
                    getCrucibleColor(material, crucibleFallbackColors[i]),
                    crucibleAcidProof[i],
                    crucibleHardness[i],
                    crucibleResistance[i]));
            CASTING_BASINS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCastingBasin(
                    getMyId("casting_basin_" + crucibleNames[i]),
                    crucibleTiers[i],
                    getCrucibleColor(material, crucibleFallbackColors[i]),
                    crucibleAcidProof[i],
                    crucibleHardness[i],
                    crucibleResistance[i],
                    getCrucibleMaxTemperature(material)));
            COOLING_MOLDS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCoolingMold(
                    getMyId("cooling_mold_" + crucibleNames[i]),
                    crucibleTiers[i],
                    getCrucibleColor(material, crucibleFallbackColors[i]),
                    crucibleAcidProof[i],
                    crucibleHardness[i],
                    crucibleResistance[i],
                    getCrucibleMaxTemperature(material)));
            KINETIC_AXLES[i] = registerMetaTileEntity(getID(), new MetaTileEntityKineticAxle(
                    getMyId("kinetic_axle_" + crucibleNames[i]),
                    getCrucibleColor(material, crucibleFallbackColors[i]),
                    getKineticTransferLimit(material, crucibleTiers[i])));
        }

        TEMPERATURE_SENSOR = registerMetaTileEntity(getID(), new MetaTileEntityTemperatureSensor(
                getMyId("temperature_sensor"),
                getCrucibleColor(Materials.Steel, 0x4F4F4E),
                2.0F,
                6.0F));

        for (int i = 1; i <= 5; i++) {
            POLARIZER[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("mu_polarizer." + GTValues.VN[i]), RecipeMaps.POLARIZER_RECIPES, Gt6AdditionTextures.MU_POLARIZER, i, false, EnergyTypeList.MU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.UP, MachineEnergyAcceptFacing.DOWN}, muColor[i - 1], i * 2));
        }
        for (int i = 1; i <= 5; i++) {
            SEPARATOR[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("mu_separator." + GTValues.VN[i]), RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES, Gt6AdditionTextures.MU_SEPARATOR, i, false, EnergyTypeList.MU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.UP, MachineEnergyAcceptFacing.DOWN}, muColor[i - 1], i * 2));
        }
    }

    public static @NotNull ResourceLocation getMyId(@NotNull String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }

    private static int getColor(MaterialColorUtil.MaterialName name) {
        return MaterialColorUtil.MaterialColor.get(name);
    }

    private static int getCrucibleMaxTemperature(Material material) {
        int baseTemperature = material.hasFluid() ? material.getFluid().getTemperature() : material.getBlastTemperature();
        if (baseTemperature <= 0) {
            baseTemperature = 1811;
        }
        return (int) Math.ceil(baseTemperature * 1.25D);
    }

    private static int getKineticTransferLimit(Material material, int tier) {
        int safeTier = Math.min(5, Math.max(1, tier));
        long tierLimit = GTValues.V[safeTier];
        long materialLimit = getCrucibleMaxTemperature(material);
        return (int) Math.min(Integer.MAX_VALUE, Math.max(tierLimit, materialLimit));
    }

    private static int getCrucibleColor(Material material, int fallback) {
        int materialColor = material.getMaterialRGB() & 0xFFFFFF;
        return materialColor == 0 ? fallback : materialColor;
    }
}