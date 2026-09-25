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
import com.drppp.gt6addition.common.metatileentity.single.eu.MetaTileEntityAutomaticIgniter;
import com.drppp.gt6addition.common.metatileentity.single.eu.MetaTileEntityElectricCo2Laser;
import com.drppp.gt6addition.common.metatileentity.single.eu.MetaTileEntityElectricDynamo;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamberLiquid;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCastingBasin;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucibleCrossing;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCruciblePouringSpout;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityFluidizedBed;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityMold;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityTemperatureSensor;
import com.drppp.gt6addition.common.metatileentity.single.item.MetaTileEntityGt6Hopper;
import com.drppp.gt6addition.common.metatileentity.single.item.MetaTileEntityMiniPortalEnd;
import com.drppp.gt6addition.common.metatileentity.single.item.MetaTileEntityMiniPortalNether;
import com.drppp.gt6addition.common.metatileentity.single.item.MetaTileEntityMiniPortalTwilight;
import com.drppp.gt6addition.common.metatileentity.single.item.MetaTileEntityMortar;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticAxle;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticGearbox;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticSteamEngine;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityRotationEngine;
import com.drppp.gt6addition.common.metatileentity.single.lu.MetaTileEntityLaserEngraver;
import com.drppp.gt6addition.common.metatileentity.single.lu.MetaTileEntityLaserWelder;
import com.drppp.gt6addition.common.metatileentity.single.mu.MetaTileEntityElectromagnet;
import com.drppp.gt6addition.common.metatileentity.multiblock.MetaTileEntityCokeOven;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntityDieselEngine;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntityElectricMotor;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntityRotationPump;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntitySteamTurbine;
import gregtech.api.GTValues;
import com.drppp.gt6addition.common.recipes.GT6AdditionRecipeMaps;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import com.drppp.gt6addition.common.material.GT6MachineMaterials;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class MetaTileEntityHandler {

    public static MetaTileEntityCombustionchamber[] HU_BURRING_BOXS = new MetaTileEntityCombustionchamber[13];
    public static MetaTileEntityCombustionchamber HU_BRICK_BURNING_BOX;
    public static MetaTileEntityCokeOven COKE_OVEN;
    public static MetaTileEntityCombustionchamber[] HU_DENSE_BURRING_BOXS = new MetaTileEntityCombustionchamber[13];
    public static MetaTileEntityCombustionchamberLiquid[] HU_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[13];
    public static MetaTileEntityCombustionchamberLiquid[] HU_DENSE_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[13];
    public static MetaTileEntityFluidizedBed[] HU_FLUIDIZED_BEDS = new MetaTileEntityFluidizedBed[13];
    public static MetaTileEntityFluidizedBed[] HU_DENSE_FLUIDIZED_BEDS = new MetaTileEntityFluidizedBed[13];

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
    public static MetaTileEntityKineticSteamEngine[] KINETIC_STEAM_ENGINES_STRONG = new MetaTileEntityKineticSteamEngine[5];
    public static MetaTileEntityKineticGearbox[] KINETIC_GEARBOXES = new MetaTileEntityKineticGearbox[5];
    public static MetaTileEntityKineticGearbox[] ADJUSTABLE_KINETIC_GEARBOXES = new MetaTileEntityKineticGearbox[5];
    public static MetaTileEntityKineticAxle[] KINETIC_AXLES = new MetaTileEntityKineticAxle[22];
    public static MetaTileEntityGt6Hopper[] GT6_HOPPERS = new MetaTileEntityGt6Hopper[5];
    public static MetaTileEntityGt6Hopper[] GT6_QUEUE_HOPPERS = new MetaTileEntityGt6Hopper[5];
    public static MetaTileEntityRotationPump[] ROTATION_PUMPS = new MetaTileEntityRotationPump[5];
    public static MetaTileEntityMortar MORTAR;
    public static MetaTileEntityMiniPortalNether MINI_PORTAL_NETHER;
    public static MetaTileEntityMiniPortalEnd MINI_PORTAL_END;
    public static MetaTileEntityMiniPortalTwilight MINI_PORTAL_TWILIGHT;

    public static MetaTileEntityColorOvenMachine[] OVEN_HU = new MetaTileEntityColorOvenMachine[4];
    public static MetaTileEntityColorMachine[] DISTILLERY_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityColorMachine[] EXTRUDER_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityColorMachine[] LAMINATOR_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityColorMachine[] ROASTER_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityColorMachine[] FERMENTER_HU = new MetaTileEntityColorMachine[4];
    public static MetaTileEntityCrucible[] CRUCIBLE_HU = new MetaTileEntityCrucible[23];
    public static MetaTileEntityCruciblePouringSpout[] CRUCIBLE_POURING_SPOUTS = new MetaTileEntityCruciblePouringSpout[23];
    public static MetaTileEntityCrucibleCrossing[] CRUCIBLE_POURING_CHANNELS = new MetaTileEntityCrucibleCrossing[23];
    public static MetaTileEntityCastingBasin[] CASTING_BASINS = new MetaTileEntityCastingBasin[23];
    public static MetaTileEntityMold[] MOLDS = new MetaTileEntityMold[23];
    public static MetaTileEntityTemperatureSensor TEMPERATURE_SENSOR;

    public static MetaTileEntitySteamTurbine[] STEAM_TURBINES = new MetaTileEntitySteamTurbine[8];
    public static MetaTileEntityElectricMotor[] ELECTRIC_MOTOR = new MetaTileEntityElectricMotor[5];
    public static MetaTileEntityElectricDynamo[] ELECTRIC_DYNAMO = new MetaTileEntityElectricDynamo[5];
    public static MetaTileEntityElectricCo2Laser[] ELECTRIC_CO2_LASER = new MetaTileEntityElectricCo2Laser[5];
    public static MetaTileEntityDieselEngine[] DIESEL_ENGINE = new MetaTileEntityDieselEngine[5];

    public static MetaTileEntityElectromagnet[] ELECTROMAGNET = new MetaTileEntityElectromagnet[5];
    public static MetaTileEntityColorMachine[] POLARIZER = new MetaTileEntityColorMachine[5];
    public static MetaTileEntityColorMachine[] SEPARATOR = new MetaTileEntityColorMachine[5];
    public static MetaTileEntityThermoelectricCooler[] THERMOELECTRIC_COOLER = new MetaTileEntityThermoelectricCooler[5];
    public static MetaTileEntityAutomaticIgniter AUTOMATIC_IGNITER_LV;
    public static MetaTileEntityLaserWelder[] LASER_WELDER = new MetaTileEntityLaserWelder[5];
    public static MetaTileEntityLaserEngraver[] LASER_ENGRAVER = new MetaTileEntityLaserEngraver[5];

    static int startID = 0;

    public static int getID() {
        startID++;
        return startID;
    }

    public static void InitMte() {
        String[] names = {"lead", "bismuth", "bronze", "arsenic_copper", "arsenic_bronze", "invar", "steel",
                "chrome", "titanium", "netherite", "tungsten", "tungstensteel", "tantalum_hafnium_carbide"};
        Material[] burningMaterials = {Materials.Lead, Materials.Bismuth, Materials.Bronze,
                GT6MachineMaterials.ARSENIC_COPPER, GT6MachineMaterials.ARSENIC_BRONZE, Materials.Invar,
                Materials.Steel, Materials.Chrome, Materials.Titanium, GT6MachineMaterials.NETHERITE,
                Materials.Tungsten, Materials.TungstenSteel, GT6MachineMaterials.TANTALUM_HAFNIUM_CARBIDE};
        double[] burningEfficiency = {0.50D, 0.45D, 0.75D, 0.80D, 0.90D, 1.00D, 0.70D,
                0.85D, 0.85D, 0.90D, 1.00D, 0.90D, 1.00D};
        int[] burningOutput = {16, 20, 24, 24, 28, 16, 32, 112, 96, 96, 128, 128, 256};
        int[] burningTier = {1, 1, 1, 1, 1, 1, 2, 3, 3, 3, 4, 5, 5};

        String[] crucibleNames = {
                "stone", "basalt", "graniteblack", "granitered", "quartz", "carbon",
                "bronze", "invar", "steel", "stainlesssteel", "titanium", "chrome",
                "molybdenum", "niobium", "tantalum", "osmium", "iridium", "niobium_titanium",
                "vanadium", "tungsten", "tungstensteel", "tungstencarbide", "ceramic"
        };
        Material[] crucibleMaterials = {
                Materials.Stone, Materials.Basalt, Materials.GraniteBlack, Materials.GraniteRed, Materials.NetherQuartz, Materials.Carbon,
                Materials.Bronze, Materials.Invar, Materials.Steel, Materials.StainlessSteel, Materials.Titanium, Materials.Chrome,
                Materials.Molybdenum, Materials.Niobium, Materials.Tantalum, Materials.Osmium, Materials.Iridium, Materials.NiobiumTitanium,
                Materials.Vanadium, Materials.Tungsten, Materials.TungstenSteel, Materials.TungstenCarbide,
                Materials.Clay
        };
        int[] crucibleTiers = {1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 1};
        boolean[] crucibleAcidProof = {
                false, false, false, false, false, false,
                false, false, false, true, false, true,
                false, false, false, false, true, false,
                false, true, false, true, false
        };
        float[] crucibleHardness = {
                5.0F, 15.0F, 15.0F, 15.0F, 5.0F, 10.0F,
                7.0F, 4.0F, 6.0F, 6.0F, 9.0F, 9.0F,
                9.0F, 9.0F, 9.0F, 9.0F, 9.0F, 9.0F,
                9.0F, 10.0F, 10.0F, 10.0F, 5.0F
        };
        float[] crucibleResistance = crucibleHardness;
        for (int i = 0; i < STEAM_TURBINES.length; i++) {
            Material[] materials = {Materials.Lead, Materials.Bronze, Materials.Steel, Materials.Invar,
                    Materials.Chrome, Materials.Titanium, Materials.Tungsten, Materials.TungstenSteel};
            int[] output = {8, 16, 64, 64, 96, 256, 384, 512};
            int[] outInventory = {8000, 8000, 8000, (int) (8000 * 1.5), 8000 * 2, 8000 * 2, 8000 * 2, 8000 * 2};
            String[] turbineNames = {"lead", "bronze", "steel", "invar", "chrome", "titanium", "tungsten", "tungstensteel"};
            STEAM_TURBINES[i] = registerMetaTileEntity(getID(), new MetaTileEntitySteamTurbine(
                    getMyId(turbineNames[i] + "_steam_turbine"), getColor(materials[i]), 0.66, output[i], outInventory[i]));
        }
        // Keep each combustion-chamber family contiguous in the registry.
        for (int i = 0; i < HU_BURRING_BOXS.length; i++) {
            HU_BURRING_BOXS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamber(
                    getMyId(names[i] + "_burring_box"), getColor(burningMaterials[i]), burningEfficiency[i],
                    burningOutput[i], false, burningTier[i]));
        }
        for (int i = 0; i < HU_DENSE_BURRING_BOXS.length; i++) {
            HU_DENSE_BURRING_BOXS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamber(
                    getMyId("dense_" + names[i] + "_burring_box"), getColor(burningMaterials[i]), burningEfficiency[i],
                    burningOutput[i] * 4, true, burningTier[i]));
        }
        for (int i = 0; i < HU_BURRING_BOXS_LIQUID.length; i++) {
            HU_BURRING_BOXS_LIQUID[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamberLiquid(
                    getMyId(names[i] + "_burring_box_liquid"), getColor(burningMaterials[i]), burningEfficiency[i],
                    burningOutput[i], false, burningTier[i]));
        }
        for (int i = 0; i < HU_DENSE_BURRING_BOXS_LIQUID.length; i++) {
            HU_DENSE_BURRING_BOXS_LIQUID[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamberLiquid(
                    getMyId("dense_" + names[i] + "_burring_box_liquid"), getColor(burningMaterials[i]), burningEfficiency[i],
                    burningOutput[i] * 4, true, burningTier[i]));
        }
        for (int i = 0; i < HU_FLUIDIZED_BEDS.length; i++) {
            HU_FLUIDIZED_BEDS[i] = registerMetaTileEntity(getID(), new MetaTileEntityFluidizedBed(
                    getMyId(names[i] + "_fluidized_bed_burning_box"), getColor(burningMaterials[i]), burningEfficiency[i],
                    burningOutput[i] * 4, false, burningTier[i]));
        }
        for (int i = 0; i < HU_DENSE_FLUIDIZED_BEDS.length; i++) {
            HU_DENSE_FLUIDIZED_BEDS[i] = registerMetaTileEntity(getID(), new MetaTileEntityFluidizedBed(
                    getMyId("dense_" + names[i] + "_fluidized_bed_burning_box"), getColor(burningMaterials[i]),
                    burningEfficiency[i], burningOutput[i] * 16, true, burningTier[i]));
        }
        HU_BRICK_BURNING_BOX = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamber(
                getMyId("brick_burning_box_solid"), getColor(MaterialColorUtil.MaterialName.BRICK),
                0.25D, 16, false, 0, true));
        COKE_OVEN = registerMetaTileEntity(getID(), new MetaTileEntityCokeOven(getMyId("coke_oven")));
        Material[] electricMaterials = {Materials.Steel, Materials.Aluminium, Materials.StainlessSteel,
                Materials.Titanium, Materials.TungstenSteel};
        for (int i = 1; i <= 5; i++) {
            ELECTRIC_MOTOR[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityElectricMotor(getMyId("electric_motor." + GTValues.VN[i]), i, getColor(electricMaterials[i - 1]), 0.8, (int) GTValues.V[i]));
        }
        for (int i = 1; i <= 5; i++) {
            ELECTRIC_DYNAMO[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityElectricDynamo(getMyId("electric_dynamo." + GTValues.VN[i]), i));
        }
        for (int i = 1; i <= 5; i++) {
            ELECTRIC_CO2_LASER[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityElectricCo2Laser(getMyId("electric_co2_laser." + GTValues.VN[i]), i));
        }
        for (int i = 1; i <= 5; i++) {
            DIESEL_ENGINE[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityDieselEngine(getMyId("diesel_engine." + GTValues.VN[i]), getColor(electricMaterials[i - 1]), GTValues.VH[i] * 3));
        }

        String[] levelNames = {"bronze", "steel", "stainlesssteel", "titanium", "tungstensteel"};
        Material[] tierMaterials = {Materials.Bronze, Materials.Steel, Materials.StainlessSteel,
                Materials.Titanium, Materials.TungstenSteel};
        for (int i = 1; i <= 5; i++) {
            RU_KU_ENGINE[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityRotationEngine(getMyId("ru_ku_engine." + GTValues.VN[i]), getColor(tierMaterials[i - 1]), (int) GTValues.V[i]));
        }
        for (int i = 1; i <= 5; i++) {
            int throughput = (int) GTValues.V[i];
            KINETIC_STEAM_ENGINES[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityKineticSteamEngine(getMyId("kinetic_steam_engine." + GTValues.VN[i]), getColor(tierMaterials[i - 1]), throughput, 80));
        }
        for (int i = 1; i <= 5; i++) {
            int throughput = (int) GTValues.V[i];
            KINETIC_STEAM_ENGINES_STRONG[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityKineticSteamEngine(getMyId("kinetic_steam_engine_strong." + GTValues.VN[i]), getColor(tierMaterials[i - 1]), throughput*4, 80));
        }
        for (int i = 1; i <= 5; i++) {
            int throughput = (int) GTValues.V[i];
            KINETIC_GEARBOXES[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityKineticGearbox(getMyId("kinetic_gearbox." + GTValues.VN[i]), getColor(tierMaterials[i - 1]), throughput * 2, false));
        }
        for (int i = 1; i <= 5; i++) {
            int throughput = (int) GTValues.V[i];
            ADJUSTABLE_KINETIC_GEARBOXES[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityKineticGearbox(getMyId("adjustable_kinetic_gearbox." + GTValues.VN[i]), getColor(tierMaterials[i - 1]), throughput * 2, true));
        }
        int[] hopperSlots = {3, 5, 9, 12, 27};
        for (int i = 0; i < 5; i++) {
            GT6_HOPPERS[i] = registerMetaTileEntity(getID(), new MetaTileEntityGt6Hopper(
                    getMyId("gt6_hopper_" + levelNames[i]),
                    getColor(tierMaterials[i]),
                    hopperSlots[i],
                    false));
        }
        for (int i = 0; i < 5; i++) {
            GT6_QUEUE_HOPPERS[i] = registerMetaTileEntity(getID(), new MetaTileEntityGt6Hopper(
                    getMyId("gt6_queue_hopper_" + levelNames[i]),
                    getColor(tierMaterials[i]),
                    Math.max(2, hopperSlots[i]),
                    true));
        }
        for (int i = 0; i < 5; i++) {
            ROTATION_PUMPS[i] = registerMetaTileEntity(getID(), new MetaTileEntityRotationPump(
                    getMyId("rotation_pump." + GTValues.VN[i + 1]),
                    i + 1,
                    getColor(electricMaterials[i])));
        }
        MORTAR = registerMetaTileEntity(getID(), new MetaTileEntityMortar(
                getMyId("mortar"),
                getColor(MaterialColorUtil.MaterialName.STONE),
                getColor(MaterialColorUtil.MaterialName.STEEL)));
        MINI_PORTAL_NETHER = registerMetaTileEntity(getID(), new MetaTileEntityMiniPortalNether(
                getMyId("mini_portal_nether")));
        MINI_PORTAL_END = registerMetaTileEntity(getID(), new MetaTileEntityMiniPortalEnd(
                getMyId("mini_portal_end")));
        if (Loader.isModLoaded("twilightforest")) {
            MINI_PORTAL_TWILIGHT = registerMetaTileEntity(getID(), new MetaTileEntityMiniPortalTwilight(
                    getMyId("mini_portal_twilight")));
        }
        for (int i = 0; i < KINETIC_AXLES.length; i++) {
            Material material = crucibleMaterials[i];
            KINETIC_AXLES[i] = registerMetaTileEntity(getID(), new MetaTileEntityKineticAxle(
                    getMyId("kinetic_axle_" + crucibleNames[i]),
                    getColor(material),
                    getKineticTransferLimit(material, crucibleTiers[i])));
        }

        MaterialColorUtil.MaterialName[] muMaterials = {MaterialColorUtil.MaterialName.GALVANIZED_STEEL,
                MaterialColorUtil.MaterialName.ALUMINIUM, MaterialColorUtil.MaterialName.STAINLESS_STEEL,
                MaterialColorUtil.MaterialName.TITANIUM, MaterialColorUtil.MaterialName.TUNGSTEN_STEEL};
        for (int i = 0; i < 5; i++) {
            ELECTROMAGNET[i] = registerMetaTileEntity(getID(), new MetaTileEntityElectromagnet(getMyId("electromagnet." + GTValues.VN[i + 1]), i + 1, getColor(muMaterials[i]), 0.9, (int) GTValues.V[i + 1]));
        }
        for (int i = 0; i < 5; i++) {
            THERMOELECTRIC_COOLER[i] = registerMetaTileEntity(getID(), new MetaTileEntityThermoelectricCooler(getMyId("thermoelectric_cooler." + GTValues.VN[i + 1]), i + 1, getColor(muMaterials[i]), 0.5, (int) GTValues.VH[i + 1]));
        }
        AUTOMATIC_IGNITER_LV = registerMetaTileEntity(getID(), new MetaTileEntityAutomaticIgniter(
                getMyId("automatic_igniter.lv"), GTValues.LV, getColor(MaterialColorUtil.MaterialName.STEEL)));
        OrientedOverlayRenderer laserWelderRenderer = new OrientedOverlayRenderer("gt6addition:machines/lu_machines/laser_welder");
        OrientedOverlayRenderer laserEngraverRenderer = new OrientedOverlayRenderer("gt6addition:machines/lu_machines/laser_engraver");
        for (int i = 1; i <= 5; i++) {
            LASER_WELDER[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityLaserWelder(
                    getMyId("laser_welder." + GTValues.VN[i]),
                    GT6AdditionRecipeMaps.LASER_WELDER_RECIPES,
                    laserWelderRenderer,
                    i,
                    new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.BACK}));
        }
        for (int i = 1; i <= 5; i++) {
            LASER_ENGRAVER[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityLaserEngraver(
                    getMyId("laser_engraver." + GTValues.VN[i]),
                    RecipeMaps.LASER_ENGRAVER_RECIPES,
                    laserEngraverRenderer,
                    i,
                    new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.BACK}));
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
        Material[] huMaterials = {Materials.Steel, Materials.Invar, Materials.Titanium, Materials.TungstenCarbide};
        for (int i = 1; i <= 4; i++) {
            OVEN_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorOvenMachine(getMyId("hu_oven_" + huName[i - 1]), RecipeMaps.FURNACE_RECIPES, Gt6AdditionTextures.HU_OVEN, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, getColor(huMaterials[i - 1])));
        }
        for (int i = 1; i <= 4; i++) {
            DISTILLERY_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_distillery_" + huName[i - 1]), RecipeMaps.DISTILLERY_RECIPES, Gt6AdditionTextures.HU_DISTILLERY, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, getColor(huMaterials[i - 1]), 8));
        }
        for (int i = 1; i <= 4; i++) {
            EXTRUDER_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_extruder_" + huName[i - 1]), RecipeMaps.EXTRUDER_RECIPES, Gt6AdditionTextures.HU_EXTRUDER, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, getColor(huMaterials[i - 1])));
        }
        for (int i = 1; i <= 4; i++) {
            LAMINATOR_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_laminator_" + huName[i - 1]), RecipeMaps.LAMINATOR_RECIPES, Gt6AdditionTextures.HU_LAMINATOR, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, getColor(huMaterials[i - 1])));
        }
        for (int i = 1; i <= 4; i++) {
            ROASTER_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_roaster_" + huName[i - 1]), RecipeMaps.ROASTER_RECIPES, Gt6AdditionTextures.HU_ROASTER, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, getColor(huMaterials[i - 1])));
        }
        for (int i = 1; i <= 4; i++) {
            FERMENTER_HU[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("hu_fermenter_" + huName[i - 1]), RecipeMaps.FERMENTING_RECIPES, Gt6AdditionTextures.HU_FERMENTER, i, false, EnergyTypeList.HU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.DOWN}, getColor(huMaterials[i - 1])));
        }


        for (int i = 0; i < crucibleMaterials.length; i++) {
            Material material = crucibleMaterials[i];
            boolean ceramic = i == crucibleMaterials.length - 1;
            CRUCIBLE_HU[i] = registerMetaTileEntity(getID(), new MetaTileEntityCrucible(
                    getMyId("hu_crucible_" + crucibleNames[i]),
                    crucibleTiers[i],
                    getCrucibleColor(material, ceramic),
                    getCrucibleMaxTemperature(material, ceramic),
                    material,
                    crucibleAcidProof[i],
                    crucibleHardness[i],
                    crucibleResistance[i]));
        }
        for (int i = 0; i < crucibleMaterials.length; i++) {
            Material material = crucibleMaterials[i];
            boolean ceramic = i == crucibleMaterials.length - 1;
            CRUCIBLE_POURING_SPOUTS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCruciblePouringSpout(
                    getMyId("crucible_pouring_spout_" + crucibleNames[i]),
                    crucibleTiers[i],
                    getCrucibleColor(material, ceramic),
                    crucibleAcidProof[i],
                    crucibleHardness[i],
                    crucibleResistance[i],
                    getCrucibleMaxTemperature(material, ceramic)));
        }
        for (int i = 0; i < crucibleMaterials.length; i++) {
            Material material = crucibleMaterials[i];
            boolean ceramic = i == crucibleMaterials.length - 1;
            CRUCIBLE_POURING_CHANNELS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCrucibleCrossing(
                    getMyId("crucible_pouring_channel_" + crucibleNames[i]),
                    crucibleTiers[i],
                    getCrucibleColor(material, ceramic),
                    crucibleHardness[i],
                    crucibleResistance[i]));
        }
        for (int i = 0; i < crucibleMaterials.length; i++) {
            Material material = crucibleMaterials[i];
            boolean ceramic = i == crucibleMaterials.length - 1;
            CASTING_BASINS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCastingBasin(
                    getMyId("casting_basin_" + crucibleNames[i]),
                    crucibleTiers[i],
                    getCrucibleColor(material, ceramic),
                    crucibleAcidProof[i],
                    crucibleHardness[i],
                    crucibleResistance[i],
                    getCrucibleMaxTemperature(material, ceramic)));
        }
        for (int i = 0; i < crucibleMaterials.length; i++) {
            Material material = crucibleMaterials[i];
            boolean ceramic = i == crucibleMaterials.length - 1;
            MOLDS[i] = registerMetaTileEntity(getID(), new MetaTileEntityMold(
                    getMyId("mold_" + crucibleNames[i]),
                    crucibleTiers[i],
                    getCrucibleColor(material, ceramic),
                    crucibleAcidProof[i],
                    crucibleHardness[i],
                    crucibleResistance[i],
                    getCrucibleMaxTemperature(material, ceramic)));
        }
        TEMPERATURE_SENSOR = registerMetaTileEntity(getID(), new MetaTileEntityTemperatureSensor(
                getMyId("temperature_sensor"),
                getColor(Materials.Steel),
                2.0F,
                6.0F));

        for (int i = 1; i <= 5; i++) {
            POLARIZER[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("mu_polarizer." + GTValues.VN[i]), RecipeMaps.POLARIZER_RECIPES, Gt6AdditionTextures.MU_POLARIZER, i, false, EnergyTypeList.MU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.UP, MachineEnergyAcceptFacing.DOWN}, getColor(muMaterials[i - 1]), i * 2));
        }
        for (int i = 1; i <= 5; i++) {
            SEPARATOR[i - 1] = registerMetaTileEntity(getID(), new MetaTileEntityColorMachine(getMyId("mu_separator." + GTValues.VN[i]), RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES, Gt6AdditionTextures.MU_SEPARATOR, i, false, EnergyTypeList.MU, new MachineEnergyAcceptFacing[]{MachineEnergyAcceptFacing.UP, MachineEnergyAcceptFacing.DOWN}, getColor(muMaterials[i - 1]), i * 2));
        }
    }

    public static @NotNull ResourceLocation getMyId(@NotNull String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }

    private static int getColor(MaterialColorUtil.MaterialName name) {
        return MaterialColorUtil.get(name);
    }

    private static int getColor(Material material) {
        return MaterialColorUtil.get(material);
    }

    private static int getCrucibleMaxTemperature(Material material) {
        int baseTemperature = material.hasFluid() ? material.getFluid().getTemperature() : material.getBlastTemperature();
        if (baseTemperature <= 0) {
            baseTemperature = 1811;
        }
        return (int) Math.ceil(baseTemperature * 1.25D);
    }

    private static int getCrucibleMaxTemperature(Material material, boolean ceramic) {
        return ceramic ? 2500 : getCrucibleMaxTemperature(material);
    }

    private static int getKineticTransferLimit(Material material, int tier) {
        int safeTier = Math.min(5, Math.max(1, tier));
        long tierLimit = GTValues.V[safeTier];
        long materialLimit = getCrucibleMaxTemperature(material);
        return (int) Math.min(Integer.MAX_VALUE, Math.max(tierLimit, materialLimit));
    }

    private static int getCrucibleColor(Material material, boolean ceramic) {
        return ceramic ? getColor(MaterialColorUtil.MaterialName.CERAMIC) : getColor(material);
    }
}



