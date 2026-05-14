package com.drppp.gt6addition.common.recipes;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class GT6AdditionMachineRecipes {

    private static final Material[] MACHINE_TIER_MATERIALS = {
            Materials.Bronze, Materials.Steel, Materials.StainlessSteel, Materials.Titanium, Materials.TungstenSteel
    };
    private static final String[] TIER_CIRCUITS = {
            "circuitLv", "circuitMv", "circuitHv", "circuitEv", "circuitIv"
    };
    private static final Material[] STEAM_TURBINE_MATERIALS = {
            Materials.Lead, Materials.Bronze, Materials.Steel, Materials.Invar,
            Materials.Chrome, Materials.Titanium, Materials.Tungsten, Materials.TungstenSteel
    };
    private static final Material[] COMBUSTION_MATERIALS = {
            Materials.Lead, Materials.Bronze, Materials.Steel, Materials.Invar,
            Materials.Chrome, Materials.Titanium, Materials.Tungsten, Materials.TungstenSteel
    };
    private static final Material[] HU_MACHINE_MATERIALS = {
            Materials.Steel, Materials.Invar, Materials.Titanium, Materials.TungstenCarbide
    };
    private static final Material[] CRUCIBLE_MATERIALS = {
            Materials.Stone, Materials.Basalt, Materials.GraniteBlack, Materials.GraniteRed, Materials.NetherQuartz,
            Materials.Carbon, Materials.Bronze, Materials.Invar, Materials.Steel, Materials.StainlessSteel,
            Materials.Titanium, Materials.Chrome, Materials.Molybdenum, Materials.Niobium, Materials.Tantalum,
            Materials.Osmium, Materials.Iridium, Materials.NiobiumTitanium, Materials.Vanadium, Materials.Tungsten,
            Materials.TungstenSteel, Materials.TungstenCarbide
    };
    private static boolean initialized;

    private GT6AdditionMachineRecipes() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        registerEnergyMachines();
        registerRuSeries();
        registerKuSeries();
        registerHuSeries();
        registerCrucibleSeries();
        registerItemMachines();
        registerMuSeries();
    }

    private static void registerEnergyMachines() {
        for (int i = 0; i < MetaTileEntityHandler.ELECTRIC_MOTOR.length; i++) {
            Material material = MACHINE_TIER_MATERIALS[i];
            ItemStack core = mechanicalCore(material, 1);
            registerShaped("electric_motor_" + tierName(i), MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    "SPS", "RGR", "SPS",
                    'S', longStick(material, 1),
                    'P', plate(material, 1),
                    'R', new ItemStack(Items.REDSTONE),
                    'G', core);
        }

        for (int i = 0; i < MetaTileEntityHandler.STEAM_TURBINES.length; i++) {
            Material material = STEAM_TURBINE_MATERIALS[i];
            registerShaped("steam_turbine_" + i, MetaTileEntityHandler.STEAM_TURBINES[i].getStackForm(),
                    "PRP", "FCF", "PSP",
                    'P', plate(material, 1),
                    'R', rotor(material, 1),
                    'F', new ItemStack(Blocks.FURNACE),
                    'C', pipe(material, 1),
                    'S', screw(material, 1));
        }

        for (int i = 0; i < MetaTileEntityHandler.DIESEL_ENGINE.length; i++) {
            Material material = MACHINE_TIER_MATERIALS[i];
            ItemStack core = mechanicalCore(material, 1);
            registerShaped("diesel_engine_" + tierName(i), MetaTileEntityHandler.DIESEL_ENGINE[i].getStackForm(),
                    "PGP", "MCE", "PRP",
                    'P', plate(material, 1),
                    'G', gear(material, 1),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'C', core,
                    'E', new ItemStack(Items.FLINT_AND_STEEL),
                    'R', new ItemStack(Items.REDSTONE));
        }

        for (int i = 0; i < MetaTileEntityHandler.ELECTRIC_DYNAMO.length; i++) {
            Material material = MACHINE_TIER_MATERIALS[i];
            ItemStack core = mechanicalCore(material, 1);
            registerShaped("electric_dynamo_" + tierName(i), MetaTileEntityHandler.ELECTRIC_DYNAMO[i].getStackForm(),
                    "PGP", "MCR", "PSP",
                    'P', plate(material, 1),
                    'G', gear(material, 1),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'C', core,
                    'R', new ItemStack(Items.REDSTONE),
                    'S', screw(material, 1));
        }

        for (int i = 0; i < MetaTileEntityHandler.ELECTRIC_CO2_LASER.length; i++) {
            Material material = MACHINE_TIER_MATERIALS[i];
            registerShaped("electric_co2_laser_" + tierName(i),
                    MetaTileEntityHandler.ELECTRIC_CO2_LASER[i].getStackForm(),
                    "GQG", "PLP", "RPR",
                    'G', new ItemStack(Blocks.GLASS_PANE),
                    'Q', new ItemStack(Items.QUARTZ),
                    'P', plate(material, 1),
                    'L', new ItemStack(Blocks.GLASS),
                    'R', new ItemStack(Items.REDSTONE));
        }

        registerShaped("automatic_igniter_lv", MetaTileEntityHandler.AUTOMATIC_IGNITER_LV.getStackForm(),
                " F ", "PLP", " R ",
                'F', new ItemStack(Items.FLINT_AND_STEEL),
                'P', plate(Materials.Steel, 1),
                'L', new ItemStack(Blocks.LEVER),
                'R', new ItemStack(Items.REDSTONE));

        for (int i = 0; i < MetaTileEntityHandler.LASER_WELDER.length; i++) {
            Material material = MACHINE_TIER_MATERIALS[i];
            registerShaped("laser_welder_" + tierName(i), MetaTileEntityHandler.LASER_WELDER[i].getStackForm(),
                    "PAP", "LCP", "P P",
                    'P', plate(material, 1),
                    'A', new ItemStack(Blocks.ANVIL),
                    'L', MetaTileEntityHandler.ELECTRIC_CO2_LASER[i].getStackForm(),
                    'C', TIER_CIRCUITS[i]);
        }

        for (int i = 0; i < MetaTileEntityHandler.LASER_ENGRAVER.length; i++) {
            Material material = MACHINE_TIER_MATERIALS[i];
            registerShaped("laser_engraver_" + tierName(i), MetaTileEntityHandler.LASER_ENGRAVER[i].getStackForm(),
                    "PGP", "LCP", "P P",
                    'P', plate(material, 1),
                    'G', new ItemStack(Blocks.GLASS),
                    'L', MetaTileEntityHandler.ELECTRIC_CO2_LASER[i].getStackForm(),
                    'C', TIER_CIRCUITS[i]);
        }
    }

    private static void registerRuSeries() {
        for (int i = 0; i < MACHINE_TIER_MATERIALS.length; i++) {
            Material material = MACHINE_TIER_MATERIALS[i];
            String tier = tierName(i);
            ItemStack core = mechanicalCore(material, 1);

            registerShaped("ru_ku_engine_" + tier, MetaTileEntityHandler.RU_KU_ENGINE[i].getStackForm(),
                    "SPS", "GMG", "SRS",
                    'S', longStick(material, 1),
                    'P', plate(material, 1),
                    'G', gear(material, 1),
                    'M', core,
                    'R', rotor(material, 1));

            registerShaped("kinetic_steam_engine_" + tier, MetaTileEntityHandler.KINETIC_STEAM_ENGINES[i].getStackForm(),
                    "PRP", "FCF", "PSP",
                    'P', plate(material, 1),
                    'R', rotor(material, 1),
                    'F', new ItemStack(Blocks.FURNACE),
                    'C', pipe(material, 1),
                    'S', screw(material, 1));

            registerShapeless("kinetic_steam_engine_strong_" + tier,
                    MetaTileEntityHandler.KINETIC_STEAM_ENGINES_STRONG[i].getStackForm(),
                    MetaTileEntityHandler.KINETIC_STEAM_ENGINES[i].getStackForm(),
                    plate(material, 2),
                    rotor(material, 1));

            registerShaped("kinetic_gearbox_" + tier, MetaTileEntityHandler.KINETIC_GEARBOXES[i].getStackForm(),
                    "PGP", "S S", "PGP",
                    'P', plate(material, 1),
                    'G', gear(material, 1),
                    'S', longStick(material, 1));

            registerShapeless("adjustable_kinetic_gearbox_" + tier,
                    MetaTileEntityHandler.ADJUSTABLE_KINETIC_GEARBOXES[i].getStackForm(),
                    MetaTileEntityHandler.KINETIC_GEARBOXES[i].getStackForm(),
                    new ItemStack(Blocks.LEVER),
                    new ItemStack(Items.REDSTONE));

            registerShaped("gt6_hopper_" + tier, MetaTileEntityHandler.GT6_HOPPERS[i].getStackForm(),
                    "P P", "PHP", " P ",
                    'P', plate(material, 1),
                    'H', new ItemStack(Blocks.HOPPER));

            registerShapeless("gt6_queue_hopper_" + tier, MetaTileEntityHandler.GT6_QUEUE_HOPPERS[i].getStackForm(),
                    MetaTileEntityHandler.GT6_HOPPERS[i].getStackForm(),
                    new ItemStack(Blocks.CHEST),
                    new ItemStack(Items.REDSTONE));

            registerShaped("rotation_pump_" + tier, MetaTileEntityHandler.ROTATION_PUMPS[i].getStackForm(),
                    "PBP", "RCR", "PSP",
                    'P', plate(material, 1),
                    'B', new ItemStack(Items.BUCKET),
                    'R', rotor(material, 1),
                    'C', pipe(material, 1),
                    'S', screw(material, 1));

            registerShaped("ru_bender_" + tier, MetaTileEntityHandler.METAL_BENDER_RU[i].getStackForm(),
                    "P P", "MGP", "PRP",
                    'P', plate(material, 1),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'G', core,
                    'R', new ItemStack(Blocks.PISTON));

            registerShaped("ru_wiremill_" + tier, MetaTileEntityHandler.WIREMILLS_RU[i].getStackForm(),
                    "P P", "MHP", "RGR",
                    'P', plate(material, 1),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'H', core,
                    'R', new ItemStack(Items.STRING),
                    'G', gear(material, 1));

            registerShaped("ru_lathe_" + tier, MetaTileEntityHandler.LATHE_RU[i].getStackForm(),
                    "PSP", "MHP", "PGR",
                    'P', plate(material, 1),
                    'S', longStick(material, 1),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'H', core,
                    'G', gear(material, 1),
                    'R', new ItemStack(Items.REDSTONE));

            registerShaped("ru_cutter_" + tier, MetaTileEntityHandler.CUTTING_SAW_RU[i].getStackForm(),
                    "PSP", "MHP", "P P",
                    'P', plate(material, 1),
                    'S', new ItemStack(Items.SHEARS),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'H', core);

            registerShaped("ru_centrifuge_" + tier, MetaTileEntityHandler.CENTRIFUGE_RU[i].getStackForm(),
                    "PRP", "MGP", "PBP",
                    'P', plate(material, 1),
                    'R', rotor(material, 1),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'G', core,
                    'B', new ItemStack(Items.BUCKET));

            registerShaped("ru_loom_" + tier, MetaTileEntityHandler.LOOM_RU[i].getStackForm(),
                    "SPS", "MHP", "P P",
                    'S', new ItemStack(Items.STRING),
                    'P', plate(material, 1),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'H', core);

            registerShaped("ru_orewasher_" + tier, MetaTileEntityHandler.ORE_WASHER_RU[i].getStackForm(),
                    "PWP", "MGP", "PBP",
                    'P', plate(material, 1),
                    'W', new ItemStack(Blocks.CAULDRON),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'G', core,
                    'B', new ItemStack(Items.WATER_BUCKET));

            registerShaped("ru_mixer_" + tier, MetaTileEntityHandler.MIXER_RU[i].getStackForm(),
                    "PRP", "MGP", "PBP",
                    'P', plate(material, 1),
                    'R', rotor(material, 1),
                    'M', MetaTileEntityHandler.ELECTRIC_MOTOR[i].getStackForm(),
                    'G', core,
                    'B', new ItemStack(Items.WATER_BUCKET));
        }

        for (int i = 0; i < MetaTileEntityHandler.KINETIC_AXLES.length; i++) {
            ItemStack materialPart = baseMaterial(CRUCIBLE_MATERIALS[i], 1);
            registerShaped("kinetic_axle_" + i, MetaTileEntityHandler.KINETIC_AXLES[i].getStackForm(),
                    " M ", "MSM", " M ",
                    'M', materialPart,
                    'S', longStick(CRUCIBLE_MATERIALS[i], 1));
        }
    }

    private static void registerKuSeries() {
        for (int i = 0; i < MACHINE_TIER_MATERIALS.length; i++) {
            Material material = MACHINE_TIER_MATERIALS[i];
            String tier = tierName(i);
            ItemStack core = mechanicalCore(material, 1);

            registerShaped("ku_compressor_" + tier, MetaTileEntityHandler.COMPRESSOR_KU[i].getStackForm(),
                    "PPP", "RGP", "PPP",
                    'P', plate(material, 1),
                    'R', new ItemStack(Blocks.PISTON),
                    'G', core);

            registerShaped("ku_forming_press_" + tier, MetaTileEntityHandler.FORMING_PRESS_KU[i].getStackForm(),
                    "PAP", "RGP", "PPP",
                    'P', plate(material, 1),
                    'A', new ItemStack(Blocks.ANVIL),
                    'R', new ItemStack(Blocks.PISTON),
                    'G', core);

            registerShaped("ku_hammer_" + tier, MetaTileEntityHandler.HAMMER_KU[i].getStackForm(),
                    "PIP", "RGP", "PSP",
                    'P', plate(material, 1),
                    'I', new ItemStack(Blocks.IRON_BLOCK),
                    'R', new ItemStack(Blocks.ANVIL),
                    'G', core,
                    'S', longStick(material, 1));

            registerShaped("ku_sifter_" + tier, MetaTileEntityHandler.SIFTER_KU[i].getStackForm(),
                    "SMS", "PGP", "PHP",
                    'S', new ItemStack(Items.STRING),
                    'M', MetaTileEntityHandler.RU_KU_ENGINE[i].getStackForm(),
                    'P', plate(material, 1),
                    'G', core,
                    'H', new ItemStack(Blocks.HOPPER));
        }
    }

    private static void registerHuSeries() {
        for (int i = 0; i < COMBUSTION_MATERIALS.length; i++) {
            Material material = COMBUSTION_MATERIALS[i];
            registerShaped("combustion_chamber_" + i, MetaTileEntityHandler.HU_BURRING_BOXS[i].getStackForm(),
                    "PBP", "FCF", "PGP",
                    'P', plate(material, 1),
                    'B', new ItemStack(Blocks.BRICK_BLOCK),
                    'F', new ItemStack(Blocks.FURNACE),
                    'C', baseMaterial(material, 1),
                    'G', gear(material, 1));

            registerShapeless("dense_combustion_chamber_" + i, MetaTileEntityHandler.HU_DENSE_BURRING_BOXS[i].getStackForm(),
                    MetaTileEntityHandler.HU_BURRING_BOXS[i].getStackForm(),
                    plate(material, 2),
                    gear(material, 1));

            registerShaped("combustion_chamber_liquid_" + i, MetaTileEntityHandler.HU_BURRING_BOXS_LIQUID[i].getStackForm(),
                    " P ", "CBC", " B ",
                    'P', pipe(material, 1),
                    'C', MetaTileEntityHandler.HU_BURRING_BOXS[i].getStackForm(),
                    'B', new ItemStack(Items.BUCKET));

            registerShapeless("dense_combustion_chamber_liquid_" + i,
                    MetaTileEntityHandler.HU_DENSE_BURRING_BOXS_LIQUID[i].getStackForm(),
                    MetaTileEntityHandler.HU_BURRING_BOXS_LIQUID[i].getStackForm(),
                    plate(material, 2),
                    gear(material, 1));
        }

        for (int i = 0; i < HU_MACHINE_MATERIALS.length; i++) {
            Material material = HU_MACHINE_MATERIALS[i];
            String tier = "hu_" + (i + 1);
            ItemStack core = mechanicalCore(material, 1);

            registerShaped("hu_oven_" + tier, MetaTileEntityHandler.OVEN_HU[i].getStackForm(),
                    "PFP", "BGB", "P P",
                    'P', plate(material, 1),
                    'F', new ItemStack(Blocks.FURNACE),
                    'B', new ItemStack(Blocks.BRICK_BLOCK),
                    'G', core);

            registerShaped("hu_distillery_" + tier, MetaTileEntityHandler.DISTILLERY_HU[i].getStackForm(),
                    "PGP", "BHB", "P P",
                    'P', plate(material, 1),
                    'G', new ItemStack(Items.GLASS_BOTTLE),
                    'B', new ItemStack(Items.BUCKET),
                    'H', core);

            registerShaped("hu_extruder_" + tier, MetaTileEntityHandler.EXTRUDER_HU[i].getStackForm(),
                    "PIP", "BGB", "P P",
                    'P', plate(material, 1),
                    'I', new ItemStack(Blocks.PISTON),
                    'B', new ItemStack(Blocks.IRON_BLOCK),
                    'G', core);

            registerShaped("hu_laminator_" + tier, MetaTileEntityHandler.LAMINATOR_HU[i].getStackForm(),
                    "PPP", "BGB", "P P",
                    'P', plate(material, 1),
                    'B', new ItemStack(Items.PAPER),
                    'G', core);

            registerShaped("hu_roaster_" + tier, MetaTileEntityHandler.ROASTER_HU[i].getStackForm(),
                    "PXP", "BGB", "P P",
                    'P', plate(material, 1),
                    'X', new ItemStack(Items.COAL),
                    'B', new ItemStack(Blocks.BRICK_BLOCK),
                    'G', core);

            registerShaped("hu_fermenter_" + tier, MetaTileEntityHandler.FERMENTER_HU[i].getStackForm(),
                    "PWP", "BGB", "P P",
                    'P', plate(material, 1),
                    'W', new ItemStack(Items.WHEAT),
                    'B', new ItemStack(Items.WATER_BUCKET),
                    'G', core);
        }

        registerShaped("temperature_sensor", MetaTileEntityHandler.TEMPERATURE_SENSOR.getStackForm(),
                "PRP", "PCP", "P P",
                'P', plate(Materials.Steel, 1),
                'R', new ItemStack(Items.REDSTONE),
                'C', new ItemStack(Items.COMPARATOR));
    }

    private static void registerCrucibleSeries() {
        for (int i = 0; i < CRUCIBLE_MATERIALS.length; i++) {
            ItemStack core = baseMaterial(CRUCIBLE_MATERIALS[i], 1);

            registerShaped("crucible_" + i, MetaTileEntityHandler.CRUCIBLE_HU[i].getStackForm(),
                    "MMM", "MFM", "MMM",
                    'M', core,
                    'F', new ItemStack(Blocks.FURNACE));

            registerShaped("crucible_faucet_" + i, MetaTileEntityHandler.CRUCIBLE_FAUCETS[i].getStackForm(),
                    "  M", " PL", "M  ",
                    'M', core,
                    'P', pipe(CRUCIBLE_MATERIALS[i], 1),
                    'L', new ItemStack(Blocks.LEVER));

            registerShaped("casting_basin_" + i, MetaTileEntityHandler.CASTING_BASINS[i].getStackForm(),
                    "M M", "M M", "MMM",
                    'M', core);

            registerShaped("cooling_mold_" + i, MetaTileEntityHandler.COOLING_MOLDS[i].getStackForm(),
                    " I ", "MBM", " M ",
                    'I', new ItemStack(Blocks.ICE),
                    'M', core,
                    'B', MetaTileEntityHandler.CASTING_BASINS[i].getStackForm());
        }
    }

    private static void registerItemMachines() {
        registerShaped("mortar", MetaTileEntityHandler.MORTAR.getStackForm(),
                " C ", "S S", "SSS",
                'C', new ItemStack(Blocks.COBBLESTONE),
                'S', new ItemStack(Blocks.STONE));

        registerShaped("mini_portal_nether", MetaTileEntityHandler.MINI_PORTAL_NETHER.getStackForm(),
                "OFO", "OPO", "OOO",
                'O', new ItemStack(Blocks.OBSIDIAN),
                'F', new ItemStack(Items.FLINT_AND_STEEL),
                'P', new ItemStack(Items.ENDER_PEARL));

        registerShaped("mini_portal_end", MetaTileEntityHandler.MINI_PORTAL_END.getStackForm(),
                "OEO", "OPO", "OOO",
                'O', new ItemStack(Blocks.END_STONE),
                'E', new ItemStack(Items.ENDER_EYE),
                'P', new ItemStack(Items.ENDER_PEARL));
    }

    private static void registerMuSeries() {
        for (int i = 0; i < MACHINE_TIER_MATERIALS.length; i++) {
            Material material = MACHINE_TIER_MATERIALS[i];
            String tier = tierName(i);
            ItemStack core = mechanicalCore(material, 1);

            registerShaped("electromagnet_" + tier, MetaTileEntityHandler.ELECTROMAGNET[i].getStackForm(),
                    "PIP", "RGR", "PIP",
                    'P', plate(material, 1),
                    'I', new ItemStack(Blocks.IRON_BARS),
                    'R', new ItemStack(Items.REDSTONE),
                    'G', core);

            registerShaped("mu_polarizer_" + tier, MetaTileEntityHandler.POLARIZER[i].getStackForm(),
                    "PEP", "PGP", "P P",
                    'P', plate(material, 1),
                    'E', MetaTileEntityHandler.ELECTROMAGNET[i].getStackForm(),
                    'G', core);

            registerShaped("mu_separator_" + tier, MetaTileEntityHandler.SEPARATOR[i].getStackForm(),
                    "PHP", "PGP", "PMP",
                    'P', plate(material, 1),
                    'H', new ItemStack(Blocks.HOPPER),
                    'G', core,
                    'M', MetaTileEntityHandler.ELECTROMAGNET[i].getStackForm());

            registerShaped("thermoelectric_cooler_" + tier, MetaTileEntityHandler.THERMOELECTRIC_COOLER[i].getStackForm(),
                    "PIP", "WGW", "PRP",
                    'P', plate(material, 1),
                    'I', new ItemStack(Blocks.ICE),
                    'W', new ItemStack(Items.WATER_BUCKET),
                    'G', core,
                    'R', new ItemStack(Items.REDSTONE));
        }
    }

    private static void registerShaped(String name, ItemStack output, Object... inputs) {
        if (!isValid(output, inputs)) {
            return;
        }
        ResourceLocation registryName = new ResourceLocation(Tags.MOD_ID, name);
        ShapedOreRecipe recipe = new ShapedOreRecipe(null, output.copy(), inputs);
        recipe.setRegistryName(registryName);
        ForgeRegistries.RECIPES.register(recipe);
    }

    private static void registerShapeless(String name, ItemStack output, Object... inputs) {
        if (!isValid(output, inputs)) {
            return;
        }
        ResourceLocation registryName = new ResourceLocation(Tags.MOD_ID, name);
        ShapelessOreRecipe recipe = new ShapelessOreRecipe(null, output.copy(), inputs);
        recipe.setRegistryName(registryName);
        ForgeRegistries.RECIPES.register(recipe);
    }

    private static boolean isValid(ItemStack output, Object... inputs) {
        if (output == null || output.isEmpty()) {
            return false;
        }
        for (Object input : inputs) {
            if (input instanceof ItemStack && ((ItemStack) input).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String tierName(int index) {
        return GTValues.VN[index + 1].toLowerCase();
    }

    private static ItemStack baseMaterial(Material material, int amount) {
        return component(material, amount, OrePrefix.plate, OrePrefix.ingot, OrePrefix.gem, OrePrefix.dust, OrePrefix.block);
    }

    private static ItemStack plate(Material material, int amount) {
        return component(material, amount, OrePrefix.plate, OrePrefix.ingot, OrePrefix.gem, OrePrefix.dust, OrePrefix.block);
    }

    private static ItemStack gear(Material material, int amount) {
        return component(material, amount, OrePrefix.gear, OrePrefix.plate, OrePrefix.ingot, OrePrefix.block);
    }

    private static ItemStack rotor(Material material, int amount) {
        return component(material, amount, OrePrefix.rotor, OrePrefix.gear, OrePrefix.plate, OrePrefix.ingot);
    }

    private static ItemStack pipe(Material material, int amount) {
        return component(material, amount, OrePrefix.pipeNormalFluid, OrePrefix.stickLong, OrePrefix.plate, OrePrefix.ingot);
    }

    private static ItemStack longStick(Material material, int amount) {
        return component(material, amount, OrePrefix.stickLong, OrePrefix.stick, OrePrefix.plate, OrePrefix.ingot);
    }

    private static ItemStack screw(Material material, int amount) {
        return component(material, amount, OrePrefix.screw, OrePrefix.stick, OrePrefix.plate, OrePrefix.ingot);
    }

    private static ItemStack mechanicalCore(Material material, int amount) {
        return component(material, amount, OrePrefix.gear, OrePrefix.stickLong, OrePrefix.stick,
                OrePrefix.plate, OrePrefix.ingot);
    }

    private static ItemStack component(Material material, int amount, OrePrefix... prefixes) {
        for (OrePrefix prefix : prefixes) {
            ItemStack stack = OreDictUnifier.get(prefix, material, amount);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
