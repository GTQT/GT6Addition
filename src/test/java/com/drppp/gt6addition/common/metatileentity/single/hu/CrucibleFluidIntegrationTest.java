package com.drppp.gt6addition.common.metatileentity.single.hu;

import com.drppp.gt6addition.api.capability.interfaces.ICrucibleEnergyReceiver;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import com.mojang.authlib.GameProfile;
import gregtech.api.GregTechAPI;
import gregtech.api.GTValues;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.material.registry.MarkerMaterialRegistry;
import gregtech.core.unification.material.internal.MaterialRegistryManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.MobEffects;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/** Actual MTE fill/drain, tick and NBT paths, with no chunks or save files. */
class CrucibleFluidIntegrationTest {
    private static IMaterialRegistryManager previousMaterials;
    private static MTEManager previousMtes;
    private static MarkerMaterialRegistry previousMarkers;
    private static Capability<IFluidHandler> previousCapability;
    private static Material previousWater, previousLava, previousObsidian, previousIce;
    private static Material previousBandedIron;
    private static Material coreIronOxide;
    private static Material osmium;
    private static Material sulfuricAcid, nitroglycerin;
    private static Material multiPhase, liquidAlias, conflictingPhase, sharedCandidate;
    private static Material equalUnitAlias, equalUnitPlasmaConflict;
    private static Material primaryGas, primaryPlasma;
    private static Fluid multiPhaseGas, multiPhasePlasma;
    private static Fluid lavaGas;
    private static Material coloredMelt;
    private static Material fluidRefinedIron;
    private static Material attributeAcid;
    private static Material zeroDensity;
    private static Material glowingShell;
    private static Material enderAmethyst, amethyst, unknownCombustibleGem;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void bootstrap() throws Exception {
        Bootstrap.register();
        previousMaterials = GregTechAPI.materialManager;
        previousMtes = GregTechAPI.mteManager;
        previousCapability = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        previousMarkers = GregTechAPI.markerMaterialRegistry;
        if (previousMarkers == null) GregTechAPI.markerMaterialRegistry = MarkerMaterialRegistry.getInstance();
        previousWater = Materials.Water;
        previousLava = Materials.Lava;
        previousObsidian = Materials.Obsidian;
        previousIce = Materials.Ice;
        previousBandedIron = Materials.BandedIron;

        // Use the dependency's real registry and Material.Builder, isolated from
        // its singleton and the game's registration lifecycle. Restore globals.
        Constructor<MaterialRegistryManager> registryConstructor = MaterialRegistryManager.class.getDeclaredConstructor();
        registryConstructor.setAccessible(true);
        MaterialRegistryManager registry = registryConstructor.newInstance();
        GregTechAPI.materialManager = registry;
        registry.createRegistry("gtqtcore");
        registry.unfreezeRegistries();
        ResourceLocation texture = new ResourceLocation("gt6addition", "fluid_integration_test");
        Materials.Water = Material.builder(269, new ResourceLocation("gregtech", "water"))
                .fluid(FluidRegistry.WATER, FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        Materials.Lava = Material.builder(1600, new ResourceLocation("gregtech", "lava"))
                .fluid(FluidRegistry.LAVA, FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        Materials.Obsidian = Material.builder(297, new ResourceLocation("gregtech", "obsidian"))
                .dust(3).fluid(new Fluid("parity_obsidian_gas", texture, texture)
                                .setTemperature(1000).setGaseous(true),
                        FluidStorageKeys.GAS, FluidState.GAS).build();
        Materials.Ice = Material.builder(1530, new ResourceLocation("gregtech", "ice")).dust().build();
        osmium = Material.builder(77, new ResourceLocation("gregtech", "osmium"))
                .dust().color(0x445566).build();
        Material iron = Material.builder(26, new ResourceLocation("gregtech", "iron")).ingot().build();
        Material wrought = Material.builder(27, new ResourceLocation("gregtech", "wrought_iron")).ingot().build();
        // The real host sets this target. GT6 refines via an alloy recipe at
        // 2011 K instead, so its ordinary phase change must ignore this field.
        iron.getProperty(gregtech.api.unification.material.properties.PropertyKey.INGOT).setSmeltingInto(wrought);
        Material tin = Material.builder(50, new ResourceLocation("gregtech", "tin")).ingot().build();
        tin.getProperty(gregtech.api.unification.material.properties.PropertyKey.INGOT).setSmeltingInto(wrought);
        Material hostRefinable = Material.builder(51, new ResourceLocation("gregtech", "host_refinable")).ingot().build();
        hostRefinable.getProperty(gregtech.api.unification.material.properties.PropertyKey.INGOT).setSmeltingInto(wrought);
        Material oxygen = Material.builder(8, new ResourceLocation("gregtech", "oxygen")).dust().build();
        Materials.BandedIron = Material.builder(255, new ResourceLocation("gregtech", "banded_iron"))
                .dust().components(new gregtech.api.unification.stack.MaterialStack(iron, 2),
                        new gregtech.api.unification.stack.MaterialStack(oxygen, 3)).build();
        coreIronOxide = Material.builder(1, new ResourceLocation("gtqtcore", "iron_iii_oxide"))
                .dust().components(new gregtech.api.unification.stack.MaterialStack(iron, 2),
                        new gregtech.api.unification.stack.MaterialStack(oxygen, 3)).build();
        Material.builder(1615, new ResourceLocation("gregtech", "wheat")).dust().build();
        // Deliberately unrelated fluid names: resolution must use the real
        // registered material/phase binding, never a fluid-name heuristic.
        lavaGas = new Fluid("parity_lava_gas", texture, texture).setTemperature(3000).setGaseous(true);
        Materials.Lava.getProperty(PropertyKey.FLUID).store(FluidStorageKeys.GAS, lavaGas);
        sulfuricAcid = Material.builder(1180, new ResourceLocation("gregtech", "sulfuric_acid"))
                .fluid(new Fluid("parity_bound_liquid_a", texture, texture).setTemperature(300),
                        FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        nitroglycerin = Material.builder(1009, new ResourceLocation("gregtech", "glyceryl_trinitrate"))
                .fluid(new Fluid("parity_bound_liquid_b", texture, texture).setTemperature(300),
                        FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        multiPhase = Material.builder(1900, new ResourceLocation("gregtech", "parity_multiphase"))
                .color(0x345678)
                .fluid(new Fluid("parity_molten", texture, texture).setTemperature(3000),
                        FluidStorageKeys.MOLTEN, FluidState.LIQUID).build();
        FluidProperty phases = multiPhase.getProperty(PropertyKey.FLUID);
        multiPhaseGas = new Fluid("parity_gas", texture, texture).setTemperature(3000).setGaseous(true);
        multiPhasePlasma = new Fluid("parity_plasma", texture, texture).setTemperature(3000).setGaseous(true);
        phases.store(FluidStorageKeys.GAS, multiPhaseGas);
        phases.store(FluidStorageKeys.PLASMA, multiPhasePlasma);
        Fluid alias = new Fluid("parity_liquid_alias", texture, texture).setTemperature(3000);
        liquidAlias = Material.builder(1901, new ResourceLocation("gregtech", "parity_liquid_alias"))
                .fluid(alias, FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        liquidAlias.getProperty(PropertyKey.FLUID).store(FluidStorageKeys.MOLTEN, alias);
        Fluid conflict = new Fluid("parity_conflicting_phases", texture, texture).setTemperature(3000);
        conflictingPhase = Material.builder(1902, new ResourceLocation("gregtech", "parity_conflicting_phases"))
                .fluid(conflict, FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        conflictingPhase.getProperty(PropertyKey.FLUID).store(FluidStorageKeys.GAS, conflict);
        // A different otherwise valid material must not hide the ambiguous
        // entry, regardless of the registry iteration order.
        sharedCandidate = Material.builder(1903, new ResourceLocation("gregtech", "parity_shared_candidate"))
                .fluid(conflict, FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        Fluid consistent = new Fluid("parity_equal_units", texture, texture).setTemperature(300);
        equalUnitAlias = Material.builder(1904, new ResourceLocation("gregtech", "nitric_acid"))
                .fluid(consistent, FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        equalUnitAlias.getProperty(PropertyKey.FLUID).store(FluidStorageKeys.GAS, consistent);
        Fluid plasmaAlias = new Fluid("parity_equal_plasma_units", texture, texture)
                .setTemperature(3000).setGaseous(true);
        equalUnitPlasmaConflict = Material.builder(1905, new ResourceLocation("gregtech", "helium"))
                .fluid(plasmaAlias, FluidStorageKeys.GAS, FluidState.GAS).build();
        equalUnitPlasmaConflict.getProperty(PropertyKey.FLUID).store(FluidStorageKeys.PLASMA, plasmaAlias);
        primaryGas = Material.builder(1906, new ResourceLocation("gregtech", "parity_primary_gas"))
                .fluid(new Fluid("parity_primary_gas", texture, texture).setTemperature(3000).setGaseous(true),
                        FluidStorageKeys.GAS, FluidState.GAS).build();
        primaryPlasma = Material.builder(1907, new ResourceLocation("gregtech", "parity_primary_plasma"))
                .fluid(new Fluid("parity_primary_plasma", texture, texture).setTemperature(3000).setGaseous(true),
                        FluidStorageKeys.PLASMA, FluidState.PLASMA).build();
        coloredMelt = Material.builder(1908, new ResourceLocation("gregtech", "parity_colored_melt"))
                .color(0x123456)
                .fluid(new Fluid("parity_colored_melt", texture, texture).setColor(0xFFABCDEF),
                        FluidStorageKeys.MOLTEN, FluidState.LIQUID).build();
        coloredMelt.getProperty(PropertyKey.FLUID).store(FluidStorageKeys.LIQUID,
                new Fluid("parity_colored_liquid", texture, texture).setColor(0xFF010203));
        fluidRefinedIron = Material.builder(1909, new ResourceLocation("gregtech", "refined_iron"))
                .fluid(new Fluid("parity_refined_iron_gas", texture, texture)
                                .setTemperature(1000).setGaseous(true),
                        FluidStorageKeys.GAS, FluidState.GAS).build();
        attributeAcid = Material.builder(1910, new ResourceLocation("gregtech", "parity_attribute_acid"))
                .fluid(new HazardProbeFluid("parity_acid_molten", texture, FluidState.LIQUID, false),
                        FluidStorageKeys.MOLTEN, FluidState.LIQUID).build();
        FluidProperty acidPhases = attributeAcid.getProperty(PropertyKey.FLUID);
        acidPhases.store(FluidStorageKeys.LIQUID,
                new HazardProbeFluid("parity_acid_liquid", texture, FluidState.LIQUID, false));
        acidPhases.store(FluidStorageKeys.GAS,
                new HazardProbeFluid("parity_acid_gas", texture, FluidState.GAS, true));
        acidPhases.store(FluidStorageKeys.PLASMA,
                new HazardProbeFluid("parity_acid_plasma", texture, FluidState.PLASMA, false));
        zeroDensity = Material.builder(1911, new ResourceLocation("gregtech", "fermium")).dust().build();
        glowingShell = Material.builder(1912, new ResourceLocation("gregtech", "parity_glowing_shell"))
                .dust().flags(MaterialFlags.GLOWING).build();
        // Deliberately conflicting host flags in this isolated registry: the
        // source-verified GT6 zero tags must win, without rewriting host flags.
        enderAmethyst = Material.builder(1913, new ResourceLocation("gregtech", "amethyst_ender"))
                .dust().flags(MaterialFlags.FLAMMABLE, MaterialFlags.EXPLOSIVE).build();
        amethyst = Material.builder(1914, new ResourceLocation("gregtech", "amethyst"))
                .dust().flags(MaterialFlags.FLAMMABLE, MaterialFlags.EXPLOSIVE).build();
        unknownCombustibleGem = Material.builder(1915, new ResourceLocation("gregtech", "parity_unknown_gem"))
                .dust().flags(MaterialFlags.FLAMMABLE, MaterialFlags.EXPLOSIVE).build();
        registry.closeRegistries();
        if (previousMtes == null) GregTechAPI.mteManager = MTEManager.getInstance();
        if (previousCapability == null) {
            Constructor<?> constructor = Capability.class.getDeclaredConstructor(
                    String.class, Capability.IStorage.class, Callable.class);
            constructor.setAccessible(true);
            Capability.IStorage<IFluidHandler> storage = new Capability.IStorage<IFluidHandler>() {
                @Override public NBTBase writeNBT(Capability<IFluidHandler> cap, IFluidHandler value, EnumFacing side) {
                    return new NBTTagCompound();
                }
                @Override public void readNBT(Capability<IFluidHandler> cap, IFluidHandler value,
                                              EnumFacing side, NBTBase data) {}
            };
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY = (Capability<IFluidHandler>) constructor.newInstance(
                    IFluidHandler.class.getName(), storage, (Callable<IFluidHandler>) () -> null);
        }
    }

    @AfterAll
    static void restoreGlobals() {
        GregTechAPI.materialManager = previousMaterials;
        GregTechAPI.mteManager = previousMtes;
        GregTechAPI.markerMaterialRegistry = previousMarkers;
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY = previousCapability;
        Materials.Water = previousWater;
        Materials.Lava = previousLava;
        Materials.Obsidian = previousObsidian;
        Materials.Ice = previousIce;
        Materials.BandedIron = previousBandedIron;
    }

    @Test
    void knownZeroDensityIsPreservedAsKnownDataAndClearedOnlyByActualTick() {
        assertTrue(CrucibleTransferLogic.hasKnownGt6MaterialDensity(zeroDensity.getRegistryName()));
        assertEquals(0.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(
                zeroDensity.getRegistryName(), 1200));
        assertEquals(1200.0D, CrucibleTransferLogic.gt6MaterialDensityKgPerCubicMeter(
                "parity_unknown_density", 0));
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(300, zeroDensity.getRegistryName(), GTValues.L, GTValues.L));
        assertEquals(GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
        NBTTagCompound stored = vessel.save();
        Vessel loaded = new Vessel();
        loaded.readFromNBT(stored);
        assertEquals(GTValues.M, onlyContent(loaded.save()).getLong("Amount"));
        assertEquals(0, loaded.world.blockChanges);
        loaded.update();
        assertEquals(0, loaded.save().getTagList("Contents", 10).tagCount());
        assertEquals(0, loaded.world.blockChanges);
        assertEquals(GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
    }

    @Test
    void condensedMaterialDoesNotVaporizeWhenItsDefaultChangesToAttributedGasOrPlasma() {
        FluidProperty property = multiPhase.getProperty(PropertyKey.FLUID);
        FluidStorageKey primary = property.getPrimaryKey();
        Fluid gas = property.get(FluidStorageKeys.GAS);
        Fluid plasma = property.get(FluidStorageKeys.PLASMA);
        ResourceLocation texture = new ResourceLocation("gt6addition", "fluid_integration_test");
        try {
            property.store(FluidStorageKeys.GAS,
                    new HazardProbeFluid("parity_hazard_gas", texture, FluidState.GAS, false));
            property.store(FluidStorageKeys.PLASMA,
                    new HazardProbeFluid("parity_hazard_plasma", texture, FluidState.PLASMA, false));
            for (FluidStorageKey key : new FluidStorageKey[]{FluidStorageKeys.MOLTEN,
                    FluidStorageKeys.GAS, FluidStorageKeys.PLASMA}) {
                property.setPrimaryKey(key);
                assertFalse(GT6MaterialHazardData.isGasOnlyMaterial(multiPhase));
                Vessel vessel = new Vessel();
                vessel.readFromNBT(state(3000, multiPhase.getRegistryName(), GTValues.L, GTValues.L));
                vessel.update();
                NBTTagCompound retained = onlyContent(vessel.save());
                assertEquals(multiPhase.getRegistryName(), retained.getString("Material"));
                assertEquals(GTValues.M, retained.getLong("Amount"));
                assertEquals(0, retained.getInteger("FluidRemainder"));
                assertEquals(0, vessel.world.blockChanges);
                Vessel loaded = new Vessel();
                loaded.readFromNBT(vessel.save());
                loaded.update();
                assertEquals(GTValues.M, onlyContent(loaded.save()).getLong("Amount"));
                assertEquals(0, loaded.world.blockChanges);
            }
        } finally {
            property.setPrimaryKey(primary);
            property.store(FluidStorageKeys.GAS, gas);
            property.store(FluidStorageKeys.PLASMA, plasma);
        }
    }

    @Test
    void plainForgeGasAndPlasmaBindingsUseUnknownGasFallbackOnlyOnActualTick() {
        assertFalse(GT6MaterialHazardData.isGasOnlyMaterial(null));
        assertFalse(GT6MaterialHazardData.isGasOnlyMaterial(osmium));
        assertFalse(GT6MaterialHazardData.isGasOnlyMaterial(liquidAlias));
        for (Material material : new Material[]{primaryGas, primaryPlasma}) {
            assertFalse(material.getFluid() instanceof AttributedFluid);
            assertTrue(GT6MaterialHazardData.isGasOnlyMaterial(material));
            assertEquals(Long.MAX_VALUE, CrucibleMaterialPhaseData.boilingPoint(material.getName()));
            Vessel vessel = new Vessel();
            vessel.readFromNBT(emptyAt(300));
            // Keep the gas cold so this check does not stand in for heat damage.
            Fluid fluid = material.getFluid();
            int temperature = fluid.getTemperature();
            try {
                fluid.setTemperature(300);
                FluidStack offered = new FluidStack(fluid, 1000);
                NBTTagCompound before = vessel.save();
                assertEquals(1000, vessel.fluids().fill(offered, false));
                assertEquals(before, vessel.save());
                assertEquals(0, vessel.world.blockChanges);
                assertEquals(1000, vessel.fluids().fill(offered, true));
                assertEquals(1, vessel.save().getTagList("Contents", 10).tagCount());
                vessel.update();
                assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
                assertEquals(0, vessel.world.blockChanges);
            } finally {
                fluid.setTemperature(temperature);
            }
        }
    }

    @Test
    void knownGt6BoilingPointOverridesGasOnlyBindingFallback() {
        assertTrue(GT6MaterialHazardData.isGasOnlyMaterial(fluidRefinedIron));
        assertEquals(3134, CrucibleMaterialPhaseData.boilingPoint(fluidRefinedIron.getName()));
        Vessel below = new Vessel();
        below.readFromNBT(state(3133, fluidRefinedIron.getRegistryName(), 1000, 1000));
        below.update();
        assertEquals(GTValues.M, onlyContent(below.save()).getLong("Amount"));
        Vessel boiling = new Vessel();
        boiling.readFromNBT(state(3134, fluidRefinedIron.getRegistryName(), 1000, 1000));
        boiling.update();
        assertEquals(0, boiling.save().getTagList("Contents", 10).tagCount());
    }

    @Test
    void acidAttributesAreReadFromEveryStandardPhaseWithoutChangingBindings() {
        FluidProperty property = attributeAcid.getProperty(PropertyKey.FLUID);
        FluidStorageKey primary = property.getPrimaryKey();
        Fluid gas = property.get(FluidStorageKeys.GAS);
        ResourceLocation texture = new ResourceLocation("gt6addition", "fluid_integration_test");
        assertFalse(GT6MaterialHazardData.isAcid(attributeAcid.getName()));
        try {
            property.store(FluidStorageKeys.GAS,
                    new HazardProbeFluid("parity_neutral_gas", texture, FluidState.GAS, false));
            assertFalse(GT6MaterialHazardData.isAcidMaterial(attributeAcid));
            for (FluidStorageKey key : new FluidStorageKey[]{FluidStorageKeys.MOLTEN, FluidStorageKeys.LIQUID,
                    FluidStorageKeys.GAS, FluidStorageKeys.PLASMA}) {
                Fluid previous = property.get(key);
                FluidState state = key == FluidStorageKeys.GAS ? FluidState.GAS :
                        key == FluidStorageKeys.PLASMA ? FluidState.PLASMA : FluidState.LIQUID;
                try {
                    Fluid acid = new HazardProbeFluid("parity_phase_acid_" + state.name(), texture, state, true);
                    property.store(key, acid);
                    for (FluidStorageKey defaultKey : new FluidStorageKey[]{FluidStorageKeys.MOLTEN,
                            FluidStorageKeys.GAS, FluidStorageKeys.PLASMA}) {
                        property.setPrimaryKey(defaultKey);
                        assertTrue(GT6MaterialHazardData.isAcidMaterial(attributeAcid));
                        assertSame(defaultKey, property.getPrimaryKey());
                        assertSame(acid, property.get(key));
                        assertEquals(1, ((AttributedFluid) acid).getAttributes().size());
                    }
                } finally {
                    property.store(key, previous);
                }
            }
        } finally {
            property.store(FluidStorageKeys.GAS, gas);
            property.setPrimaryKey(primary);
        }
    }

    @Test
    void nonPrimaryAcidAttributeCorrodesOnlyOnTickAndRespectsAcidProofVessel() {
        FluidProperty property = attributeAcid.getProperty(PropertyKey.FLUID);
        FluidStorageKey primary = property.getPrimaryKey();
        try {
            for (FluidStorageKey key : new FluidStorageKey[]{FluidStorageKeys.MOLTEN,
                    FluidStorageKeys.GAS, FluidStorageKeys.PLASMA}) {
                property.setPrimaryKey(key);
                for (boolean proof : new boolean[]{false, true}) {
                    Vessel vessel = new Vessel(null, 10_000, proof);
                    vessel.readFromNBT(emptyAt(300));
                    FluidStack offered = new FluidStack(property.get(FluidStorageKeys.MOLTEN), GTValues.L);
                    NBTTagCompound before = vessel.save();
                    assertEquals(GTValues.L, vessel.fluids().fill(offered, false));
                    assertEquals(before, vessel.save());
                    assertEquals(0, vessel.world.blockChanges);
                    assertEquals(GTValues.L, vessel.fluids().fill(offered, true));
                    assertEquals(0, vessel.world.blockChanges);
                    vessel.update();
                    assertEquals(proof ? 0 : 1, vessel.world.blockChanges);
                    if (proof) {
                        assertEquals(GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
                    } else {
                        assertEquals(Blocks.AIR, vessel.world.lastBlockState.getBlock());
                        assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
                    }
                }
            }
        } finally {
            property.setPrimaryKey(primary);
        }
    }

    @Test
    void fluidArrivalExpansionIsRejectedBeforeTemperatureOrInventoryCommit() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(2000, "gregtech:tin", 13 * GTValues.L, GTValues.L));
        FluidStack offered = Materials.Obsidian.getFluid(1000);
        NBTTagCompound before = vessel.save();
        int dirty = vessel.dirtyCalls;
        int renders = vessel.renderCalls;
        // An artificial registered cold gas exercises arrival melting without
        // the ordinary liquid-temperature clamp. 1M obsidian fits as raw input,
        // but the project lava expansion (1000/144 M) exceeds the 3M free space.
        assertEquals(0, vessel.fluids().fill(offered, false));
        assertEquals(before, vessel.save());
        assertEquals(0, vessel.fluids().fill(offered, true));
        assertEquals(before, vessel.save());
        assertEquals(dirty, vessel.dirtyCalls);
        assertEquals(renders, vessel.renderCalls);
        assertEquals(0, vessel.world.blockChanges);
        assertEquals(0, vessel.world.entityQueries);
        assertEquals(1000, offered.amount);
    }

    @Test
    void fluidArrivalExpansionThatFitsCommitsTheSamePreparedQuantityAfterSimulation() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(2000, "gregtech:tin", 13 * GTValues.L, GTValues.L));
        FluidStack offered = Materials.Obsidian.getFluid(137);
        NBTTagCompound before = vessel.save();
        assertEquals(137, vessel.fluids().fill(offered, false));
        assertEquals(before, vessel.save());
        assertEquals(137, vessel.fluids().fill(offered, true));
        assertEquals(137, offered.amount);
        NBTTagCompound lava = content(vessel.save(), "gregtech:lava");
        assertEquals(137, volume(lava, GTValues.L));
        assertTrue(lava.getBoolean("Molten"));
        assertTrue(vessel.getCurrentTemperature() < 2000 && vessel.getCurrentTemperature() >= 1300);
        assertEquals(13 * GTValues.M, content(vessel.save(), "gregtech:tin").getLong("Amount"));
        Vessel loaded = new Vessel();
        loaded.readFromNBT(vessel.save());
        assertEquals(lava, content(loaded.save(), "gregtech:lava"));
    }

    @Test
    void fluidArrivalSmeltingPreservesTheFractionalSourceQuantity() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(emptyAt(2500));
        FluidStack offered = fluidRefinedIron.getFluid(1);
        NBTTagCompound before = vessel.save();
        CrucibleFluidUnits.Quantity exact = CrucibleFluidUnits.storedFluidAmount(1, 1000);
        assertNotNull(exact);
        assertTrue(exact.remainder > 0);
        assertEquals(1, vessel.fluids().fill(offered, false));
        assertEquals(before, vessel.save());
        assertEquals(1, vessel.fluids().fill(offered, true));
        NBTTagCompound iron = onlyContent(vessel.save());
        assertEquals("gregtech:iron", iron.getString("Material"));
        assertEquals(exact.amount, iron.getLong("Amount"));
        assertEquals(exact.remainder, iron.getInteger("FluidRemainder"));
        assertTrue(iron.getBoolean("Molten"));
        Vessel loaded = new Vessel();
        loaded.readFromNBT(vessel.save());
        assertEquals(iron, onlyContent(loaded.save()));
        assertEquals(1, offered.amount);
    }

    @Test
    void fluidFreezePreparationIsReadOnlyUntilActualFill() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(emptyAt(250));
        FluidStack offered = new FluidStack(FluidRegistry.WATER, 1);
        NBTTagCompound before = vessel.save();
        int dirty = vessel.dirtyCalls;
        assertEquals(1, vessel.fluids().fill(offered, false));
        assertEquals(before, vessel.save());
        assertEquals(dirty, vessel.dirtyCalls);
        assertEquals(0, vessel.world.blockChanges);
        assertEquals(0, vessel.world.entityQueries);
        assertEquals(1, vessel.fluids().fill(offered, true));
        NBTTagCompound ice = onlyContent(vessel.save());
        CrucibleFluidUnits.Quantity exact = CrucibleFluidUnits.storedFluidAmount(1, 1000);
        assertNotNull(exact);
        assertEquals("gregtech:ice", ice.getString("Material"));
        assertEquals(exact.amount, ice.getLong("Amount"));
        assertEquals(exact.remainder, ice.getInteger("FluidRemainder"));
        assertFalse(ice.getBoolean("Molten"));
        assertTrue(vessel.getCurrentTemperature() < 273);
    }

    @Test
    void registeredDensityPrefersLiquidOverGasAndPlasmaWithoutChangingBindings() {
        FluidProperty property = multiPhase.getProperty(PropertyKey.FLUID);
        Fluid molten = property.get(FluidStorageKeys.MOLTEN);
        int previousMoltenDensity = molten.getDensity();
        int previousGasDensity = multiPhaseGas.getDensity();
        int previousPlasmaDensity = multiPhasePlasma.getDensity();
        FluidStorageKey previous = property.getPrimaryKey();
        try {
            molten.setDensity(7000);
            multiPhaseGas.setDensity(50);
            multiPhasePlasma.setDensity(800_000);
            for (FluidStorageKey key : new FluidStorageKey[]{FluidStorageKeys.MOLTEN,
                    FluidStorageKeys.GAS, FluidStorageKeys.PLASMA}) {
                property.setPrimaryKey(key);
                assertEquals(7000.0D, CrucibleRegisteredDensity.get(multiPhase));
                assertSame(key, property.getPrimaryKey());
                assertSame(molten, property.get(FluidStorageKeys.MOLTEN));
            }
        } finally {
            molten.setDensity(previousMoltenDensity);
            multiPhaseGas.setDensity(previousGasDensity);
            multiPhasePlasma.setDensity(previousPlasmaDensity);
            property.setPrimaryKey(previous);
        }
    }

    @Test
    void registeredDensityRejectsBuoyancyCodesZeroAndPlasmaOnlyValues() {
        Fluid gas = primaryGas.getFluid();
        Fluid plasma = primaryPlasma.getFluid();
        int previousGasDensity = gas.getDensity();
        int previousPlasmaDensity = plasma.getDensity();
        try {
            for (int value : new int[]{0, -100, -100000, Integer.MIN_VALUE}) {
                gas.setDensity(value);
                assertEquals(1200.0D, CrucibleRegisteredDensity.get(primaryGas));
            }
            gas.setDensity(50);
            assertEquals(50.0D, CrucibleRegisteredDensity.get(primaryGas));
            plasma.setDensity(800_000);
            assertEquals(1200.0D, CrucibleRegisteredDensity.get(primaryPlasma));
            assertEquals(1200.0D, CrucibleRegisteredDensity.get(osmium));
            assertEquals(1200.0D, CrucibleRegisteredDensity.get(null));
        } finally {
            gas.setDensity(previousGasDensity);
            plasma.setDensity(previousPlasmaDensity);
        }
    }

    @Test
    void registeredDensityFallsThroughUnusableMoltenToUsableLiquid() {
        FluidProperty property = coloredMelt.getProperty(PropertyKey.FLUID);
        Fluid molten = property.get(FluidStorageKeys.MOLTEN);
        Fluid liquid = property.get(FluidStorageKeys.LIQUID);
        int previousMoltenDensity = molten.getDensity();
        int previousLiquidDensity = liquid.getDensity();
        try {
            molten.setDensity(-100);
            liquid.setDensity(4500);
            assertEquals(4500.0D, CrucibleRegisteredDensity.get(coloredMelt));
            liquid.setDensity(0);
            assertEquals(1200.0D, CrucibleRegisteredDensity.get(coloredMelt));
        } finally {
            molten.setDensity(previousMoltenDensity);
            liquid.setDensity(previousLiquidDensity);
        }
    }

    @Test
    void actualHuConversionForUnknownMaterialIsIndependentOfHostPrimaryPhase() {
        FluidProperty property = multiPhase.getProperty(PropertyKey.FLUID);
        Fluid molten = property.get(FluidStorageKeys.MOLTEN);
        int previousDensity = molten.getDensity();
        FluidStorageKey previous = property.getPrimaryKey();
        try {
            molten.setDensity(7000);
            for (FluidStorageKey key : new FluidStorageKey[]{FluidStorageKeys.MOLTEN,
                    FluidStorageKeys.GAS, FluidStorageKeys.PLASMA}) {
                property.setPrimaryKey(key);
                Vessel vessel = new Vessel();
                vessel.readFromNBT(state(293, multiPhase.getRegistryName(), GTValues.L, GTValues.L));
                NBTTagCompound before = vessel.save();
                assertEquals(128, vessel.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, 128, true));
                assertEquals(before, vessel.save());
                assertEquals(128, vessel.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, 128, false));
                vessel.update();
                // Missing shell: 7*1000/9 kg. Contents: 7000/9 kg.
                // 1 + floor((777.777... + 777.777...)/100) = 16 HU/K.
                assertEquals(301, vessel.getCurrentTemperature());
                assertEquals(0, vessel.save().getLong("StoredHeat"));
                assertEquals(GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
            }
        } finally {
            molten.setDensity(previousDensity);
            property.setPrimaryKey(previous);
        }
    }

    @Test
    void internalMeltAppearanceDoesNotFollowGasOrPlasmaDefault() {
        FluidProperty property = multiPhase.getProperty(PropertyKey.FLUID);
        FluidStorageKey previous = property.getPrimaryKey();
        Fluid molten = property.get(FluidStorageKeys.MOLTEN);
        try {
            for (FluidStorageKey key : new FluidStorageKey[]{FluidStorageKeys.MOLTEN,
                    FluidStorageKeys.GAS, FluidStorageKeys.PLASMA}) {
                property.setPrimaryKey(key);
                assertSame(molten, CrucibleContentVisual.liquidAppearance(multiPhase).getFluid());
                assertEquals(0x345678, CrucibleContentVisual.color(multiPhase, true));
            }
        } finally {
            property.setPrimaryKey(previous);
        }
    }

    @Test
    void meltAppearancePrefersMoltenBindingAndPreservesSolidMaterialColor() {
        FluidProperty property = coloredMelt.getProperty(PropertyKey.FLUID);
        FluidStorageKey previous = property.getPrimaryKey();
        try {
            property.setPrimaryKey(FluidStorageKeys.LIQUID);
            assertSame(property.get(FluidStorageKeys.MOLTEN),
                    CrucibleContentVisual.liquidAppearance(coloredMelt).getFluid());
            assertEquals(0xABCDEF, CrucibleContentVisual.color(coloredMelt, true));
            assertEquals(0x123456, CrucibleContentVisual.color(coloredMelt, false));
        } finally {
            property.setPrimaryKey(previous);
        }
    }

    @Test
    void missingLiquidAndGasOnlyMaterialsUseMaterialAppearanceFallback() {
        assertNull(CrucibleContentVisual.liquidAppearance(null));
        assertNull(CrucibleContentVisual.liquidAppearance(osmium));
        assertNull(CrucibleContentVisual.liquidAppearance(primaryGas));
        assertNull(CrucibleContentVisual.liquidAppearance(primaryPlasma));
        assertEquals(0x445566, CrucibleContentVisual.color(osmium, true));
        assertEquals(primaryGas.getMaterialRGB() & 0xFFFFFF, CrucibleContentVisual.color(primaryGas, true));
        assertSame(liquidAlias.getFluid(), CrucibleContentVisual.liquidAppearance(liquidAlias).getFluid());
    }

    @Test
    void vanillaLiquidAppearanceAndStackCopiesDoNotDependOnHostPrimaryPhase() {
        FluidProperty property = Materials.Lava.getProperty(PropertyKey.FLUID);
        FluidStorageKey previous = property.getPrimaryKey();
        try {
            property.setPrimaryKey(FluidStorageKeys.GAS);
            FluidStack first = CrucibleContentVisual.liquidAppearance(Materials.Lava);
            FluidStack second = CrucibleContentVisual.liquidAppearance(Materials.Lava);
            assertSame(FluidRegistry.LAVA, first.getFluid());
            assertNotSame(first, second);
            first.amount = 20;
            assertEquals(1, second.amount);
            assertSame(FluidRegistry.WATER, CrucibleContentVisual.liquidAppearance(Materials.Water).getFluid());
            assertSame(lavaGas, property.get(FluidStorageKeys.GAS));
            assertSame(FluidStorageKeys.GAS, property.getPrimaryKey());
        } finally {
            property.setPrimaryKey(previous);
        }
    }

    @Test
    void gaseousMoltenBindingFallsBackToRegisteredLiquidWithoutMutatingBindings() {
        FluidProperty property = coloredMelt.getProperty(PropertyKey.FLUID);
        Fluid molten = property.get(FluidStorageKeys.MOLTEN);
        boolean gaseous = molten.isGaseous();
        try {
            molten.setGaseous(true);
            assertSame(property.get(FluidStorageKeys.LIQUID),
                    CrucibleContentVisual.liquidAppearance(coloredMelt).getFluid());
            assertEquals(0x010203, CrucibleContentVisual.color(coloredMelt, true));
            assertSame(molten, property.get(FluidStorageKeys.MOLTEN));
        } finally {
            molten.setGaseous(gaseous);
        }
    }

    @Test
    void hematiteBindingLoadsLegacyContentsWithoutDiscardingNamespaceOrQuantity() {
        assertSame(Materials.BandedIron, MetaTileEntityCrucible.resolveMaterial("Hematite"));
        assertSame(coreIronOxide, MetaTileEntityCrucible.resolveMaterial("gtqtcore:iron_iii_oxide"));
        assertNull(MetaTileEntityCrucible.resolveMaterial("thirdparty:hematite"));
        assertNull(MetaTileEntityCrucible.resolveMaterial("gt6addition:hematite"));
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(1206, "Hematite", GTValues.L, GTValues.L));
        NBTTagCompound saved = onlyContent(vessel.save());
        assertEquals("gregtech:banded_iron", saved.getString("Material"));
        assertEquals(GTValues.M, saved.getLong("Amount"));
        Vessel loaded = new Vessel();
        loaded.readFromNBT(vessel.save());
        assertEquals(saved, onlyContent(loaded.save()));
    }

    @Test
    void generatedLiquidFillSimulationNbtAndDrainUseTheRegisteredBucketUnit() {
        Vessel vessel = new Vessel();
        NBTTagCompound empty = new NBTTagCompound();
        empty.setLong("Temperature", 300);
        empty.setLong("OldTemperature", 300);
        vessel.readFromNBT(empty);
        FluidStack input = sulfuricAcid.getFluid(500);
        assertSame(sulfuricAcid, CrucibleFluidInput.resolve(input).material);
        assertEquals(1000, CrucibleFluidUnits.defaultFluidUnit(sulfuricAcid));
        NBTTagCompound before = vessel.save();
        assertEquals(500, vessel.fluids().fill(input, false));
        assertEquals(before, vessel.save());
        assertEquals(500, input.amount);
        assertEquals(500, vessel.fluids().fill(input, true));
        assertEquals(1, vessel.fluids().fill(sulfuricAcid.getFluid(1), true));
        NBTTagCompound saved = vessel.save();
        assertEquals(501, volume(onlyContent(saved), 1000));
        assertEquals(300, vessel.getCurrentTemperature());
        Vessel loaded = new Vessel();
        loaded.readFromNBT(saved);
        assertEquals(onlyContent(saved), onlyContent(loaded.save()));
        // Internal occupancy rounds a fractional material unit up for safe
        // admission; the one-fluid mB projection rounds free space down.
        assertEquals(15_999, loaded.fluids().getTankProperties()[0].getCapacity());
        assertEquals(501, loaded.fluids().getTankProperties()[0].getContents().amount);
        NBTTagCompound beforeDrain = loaded.save();
        assertEquals(501, loaded.fluids().drain(1000, false).amount);
        assertEquals(beforeDrain, loaded.save());
        assertEquals(500, loaded.fluids().drain(sulfuricAcid.getFluid(500), true).amount);
        assertEquals(1, volume(onlyContent(loaded.save()), 1000));
        assertEquals(1, loaded.fluids().drain(1000, true).amount);
        assertEquals(0, loaded.save().getTagList("Contents", 10).tagCount());
    }

    @Test
    void oldGeneratedLiquidSaveKeepsMaterialQuantityInsteadOfReinterpretingRemainder() {
        Vessel vessel = new Vessel();
        NBTTagCompound old = state(300, "gregtech:sulfuric_acid", 1000, 1000);
        old.removeTag("FluidQuantityVersion");
        onlyContent(old).setInteger("FluidRemainder", 1);
        vessel.readFromNBT(old);
        NBTTagCompound loaded = onlyContent(vessel.save());
        assertEquals(GTValues.M, loaded.getLong("Amount"));
        assertEquals(CrucibleFluidUnits.STORAGE_UNIT / GTValues.L, loaded.getInteger("FluidRemainder"));
        assertEquals(1000, volume(loaded, 1000));
        Vessel reloaded = new Vessel();
        reloaded.readFromNBT(vessel.save());
        assertEquals(loaded, onlyContent(reloaded.save()));
    }

    @Test
    void oldGlycerylNameBindsOnlyToVerifiedNitroglycerinWithoutLosingItsQuantity() {
        assertSame(nitroglycerin, MetaTileEntityCrucible.resolveMaterial("Glyceryl"));
        assertSame(nitroglycerin, MetaTileEntityCrucible.resolveMaterial("gregtech:glyceryl"));
        assertNull(MetaTileEntityCrucible.resolveMaterial("thirdparty:glyceryl"));
        assertEquals(1000, CrucibleFluidInput.resolve(nitroglycerin.getFluid(1)).unit);
        assertSame(nitroglycerin, MetaTileEntityCrucible.getSmeltingTarget(nitroglycerin));
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(300, "Glyceryl", 987, 1000));
        NBTTagCompound entry = onlyContent(vessel.save());
        assertEquals("gregtech:glyceryl_trinitrate", entry.getString("Material"));
        assertEquals(987, volume(entry, 1000));
        assertEquals(987, vessel.fluids().drain(1000, false).amount);
        Vessel loaded = new Vessel();
        loaded.readFromNBT(vessel.save());
        assertEquals(entry, onlyContent(loaded.save()));
    }

    @Test
    void registeredPhasesUseTheirOwnUnitsButEquivalentLiquidAliasesStayValid() {
        assertEquals(GTValues.L, CrucibleFluidInput.resolve(multiPhase.getFluid(1)).unit);
        assertEquals(1000, CrucibleFluidInput.resolve(new FluidStack(multiPhaseGas, 1)).unit);
        CrucibleFluidInput plasma = CrucibleFluidInput.resolve(new FluidStack(multiPhasePlasma, 1));
        assertEquals(20736, plasma.unit);
        assertTrue(plasma.plasma);
        assertEquals(GTValues.L, CrucibleFluidUnits.defaultFluidUnit(multiPhase));
        assertEquals(GTValues.L, CrucibleFluidUnits.defaultFluidUnit(liquidAlias));
        assertSame(liquidAlias, CrucibleFluidInput.resolve(liquidAlias.getFluid(1)).material);
        assertEquals(1000, CrucibleFluidUnits.defaultFluidUnit(equalUnitAlias));
        assertSame(equalUnitAlias, CrucibleFluidInput.resolve(equalUnitAlias.getFluid(1)).material);
        assertEquals(GTValues.L, CrucibleFluidUnits.defaultFluidUnit(sharedCandidate));
        assertEquals(1000, CrucibleFluidUnits.defaultFluidUnit(primaryGas));
        assertEquals(20736, CrucibleFluidUnits.defaultFluidUnit(primaryPlasma));
        assertNull(CrucibleFluidInput.resolve(new FluidStack(
                new Fluid("unbound_phase", new ResourceLocation("test", "a"),
                        new ResourceLocation("test", "b")), 1)));
    }

    @Test
    void oneMaterialDifferentPhaseInputsCoalesceToMaterialQuantityAcrossNbtAndDrain() {
        Vessel vessel = new Vessel();
        NBTTagCompound empty = new NBTTagCompound();
        empty.setLong("Temperature", 3000);
        empty.setLong("OldTemperature", 3000);
        vessel.readFromNBT(empty);
        for (FluidStack input : new FluidStack[]{multiPhase.getFluid(GTValues.L),
                new FluidStack(multiPhaseGas, 1000), new FluidStack(multiPhasePlasma, 20736)}) {
            NBTTagCompound before = vessel.save();
            assertEquals(input.amount, vessel.fluids().fill(input, false));
            assertEquals(before, vessel.save());
            assertEquals(input.amount, vessel.fluids().fill(input, true));
        }
        NBTTagCompound saved = vessel.save();
        NBTTagCompound entry = onlyContent(saved);
        assertEquals(3L * GTValues.M, entry.getLong("Amount"));
        assertEquals(0, entry.getInteger("FluidRemainder"));
        assertEquals(3000, vessel.getCurrentTemperature());
        Vessel loaded = new Vessel();
        loaded.readFromNBT(saved);
        assertEquals(entry, onlyContent(loaded.save()));
        IFluidTankProperties tank = loaded.fluids().getTankProperties()[0];
        assertSame(multiPhase.getFluid(), tank.getContents().getFluid());
        assertEquals(3 * GTValues.L, tank.getContents().amount);
        assertFalse(tank.canDrainFluidType(new FluidStack(multiPhaseGas, 1)));
        assertNull(loaded.fluids().drain(new FluidStack(multiPhasePlasma, 1), true));
        assertEquals(entry, onlyContent(loaded.save()));
        NBTTagCompound beforeDrain = loaded.save();
        assertEquals(3 * GTValues.L, loaded.fluids().drain(Integer.MAX_VALUE, false).amount);
        assertEquals(beforeDrain, loaded.save());
        assertEquals(3 * GTValues.L, loaded.fluids().drain(Integer.MAX_VALUE, true).amount);
        assertEquals(0, loaded.save().getTagList("Contents", 10).tagCount());
    }

    @Test
    void eachRegisteredPhaseUsesSharedMaterialCapacityWithoutSimulationMutation() {
        for (FluidStack input : new FluidStack[]{multiPhase.getFluid(2 * GTValues.L),
                new FluidStack(multiPhaseGas, 2000), new FluidStack(multiPhasePlasma, 41472)}) {
            Vessel vessel = new Vessel();
            vessel.readFromNBT(state(3000, multiPhase.getRegistryName(), 15 * GTValues.L, GTValues.L));
            NBTTagCompound before = vessel.save();
            int accepted = input.amount / 2;
            assertEquals(accepted, vessel.fluids().fill(input, false));
            assertEquals(before, vessel.save());
            assertEquals(accepted, vessel.fluids().fill(input, true));
            assertEquals(16L * GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
            assertEquals(0, onlyContent(vessel.save()).getInteger("FluidRemainder"));
            NBTTagCompound full = vessel.save();
            assertEquals(0, vessel.fluids().fill(input, true));
            assertEquals(full, vessel.save());
        }
    }

    @Test
    void conflictingUnitsAndPlasmaSemanticsRejectFillAndDoNotInventOutputUnits() {
        for (Material material : new Material[]{conflictingPhase, equalUnitPlasmaConflict}) {
            FluidStack input = material.getFluid(1);
            assertNull(CrucibleFluidInput.forMaterial(material, input.getFluid()));
            assertNull(CrucibleFluidInput.resolve(input));
            assertEquals(0, CrucibleFluidUnits.defaultFluidUnit(material));
            Vessel vessel = new Vessel();
            vessel.readFromNBT(state(3000, material.getRegistryName(), GTValues.L, GTValues.L));
            NBTTagCompound before = vessel.save();
            assertEquals(0, vessel.fluids().fill(input, false));
            assertEquals(0, vessel.fluids().fill(input, true));
            IFluidTankProperties tank = vessel.fluids().getTankProperties()[0];
            assertNull(tank.getContents());
            assertFalse(tank.canFillFluidType(input));
            assertFalse(tank.canDrain());
            assertNull(vessel.fluids().drain(Integer.MAX_VALUE, false));
            assertNull(vessel.fluids().drain(Integer.MAX_VALUE, true));
            assertEquals(before, vessel.save());
            assertEquals(0, vessel.world.blockChanges);
        }
    }

    @Test
    void mixedPhaseMicroAmountsSurviveDefaultFluidDrainAndReloadWithoutRoundingAway() {
        Vessel vessel = new Vessel();
        NBTTagCompound empty = new NBTTagCompound();
        empty.setLong("Temperature", 3000);
        empty.setLong("OldTemperature", 3000);
        vessel.readFromNBT(empty);
        CrucibleFluidUnits.Quantity expected = CrucibleFluidUnits.storedFluidAmount(1, GTValues.L);
        for (FluidStack input : new FluidStack[]{multiPhase.getFluid(1),
                new FluidStack(multiPhaseGas, 1), new FluidStack(multiPhasePlasma, 1)}) {
            assertEquals(1, vessel.fluids().fill(input, true));
        }
        for (int unit : new int[]{1000, 20736}) {
            CrucibleFluidUnits.Quantity additional = CrucibleFluidUnits.storedFluidAmount(1, unit);
            expected = CrucibleFluidUnits.merge(expected.amount, expected.remainder,
                    additional.amount, additional.remainder, CrucibleFluidUnits.STORAGE_UNIT);
            assertNotNull(expected);
        }
        NBTTagCompound initial = onlyContent(vessel.save());
        assertEquals(expected.amount, initial.getLong("Amount"));
        assertEquals(expected.remainder, initial.getInteger("FluidRemainder"));
        assertEquals(1, vessel.fluids().drain(1000, true).amount);
        expected = CrucibleFluidUnits.drainStored(expected.amount, expected.remainder, 1, GTValues.L);
        assertNotNull(expected);
        assertTrue(expected.amount > 0 || expected.remainder > 0);
        NBTTagCompound residual = onlyContent(vessel.save());
        assertEquals(expected.amount, residual.getLong("Amount"));
        assertEquals(expected.remainder, residual.getInteger("FluidRemainder"));
        assertNull(vessel.fluids().drain(1, true));
        Vessel loaded = new Vessel();
        loaded.readFromNBT(vessel.save());
        assertEquals(residual, onlyContent(loaded.save()));
        assertEquals(1, loaded.fluids().fill(multiPhase.getFluid(1), true));
        assertEquals(1, loaded.fluids().drain(1, true).amount);
        assertEquals(residual, onlyContent(loaded.save()));
    }

    @Test
    void basinAlsoRejectsAmbiguousDefaultBindingsWithoutInventoryOrWorldMutation() {
        Basin vessel = new Basin();
        NBTTagCompound before = vessel.save();
        for (Material material : new Material[]{conflictingPhase, equalUnitPlasmaConflict}) {
            FluidStack input = material.getFluid(1000);
            assertEquals(0, vessel.fluids().fill(input, false));
            assertEquals(0, vessel.fluids().fill(input, true));
            assertFalse(vessel.fluids().getTankProperties()[0].canFillFluidType(input));
            assertEquals(before, vessel.save());
            assertEquals(0, vessel.world.blockChanges);
        }
    }

    @Test
    void cruciblePourReceiverAcceptsOnlyTopAndSimulationNeverWritesOrMarksDirty() {
        Vessel receiver = new Vessel();
        receiver.readFromNBT(emptyAt(3000));
        ICrucibleMold mold = receiver;
        assertEquals(1, mold.getMoldRequiredMaterialUnits(multiPhase));
        assertEquals(receiver.getMaxTemperature(), mold.getMoldMaxTemperature());
        NBTTagCompound before = receiver.save();
        int dirty = receiver.dirtyCalls;
        for (EnumFacing side : EnumFacing.VALUES) {
            assertEquals(side == EnumFacing.UP, mold.isMoldInputSide(side));
            assertEquals(side == EnumFacing.UP ? GTValues.M : 0,
                    mold.fillMold(multiPhase, GTValues.M, 3000, side, true));
            if (side != EnumFacing.UP) {
                assertEquals(0, mold.fillMold(multiPhase, GTValues.M, 3000, side, false));
            }
        }
        assertFalse(mold.isMoldInputSide(null));
        assertEquals(0, mold.fillMold(multiPhase, GTValues.M, 3000, null, false));
        assertEquals(0, mold.fillMold(null, GTValues.M, 3000, EnumFacing.UP, false));
        assertEquals(0, mold.fillMold(Materials.NULL, GTValues.M, 3000, EnumFacing.UP, false));
        assertEquals(0, mold.fillMold(multiPhase, 0, 3000, EnumFacing.UP, false));
        assertEquals(0, mold.fillMold(multiPhase, -1, 3000, EnumFacing.UP, false));
        assertEquals(before, receiver.save());
        assertEquals(dirty, receiver.dirtyCalls);
        assertEquals(0, receiver.world.blockChanges);
    }

    @Test
    void crucibleToCruciblePourTransfersWholeOfferRatherThanItsOneUnitDemandHint() {
        Vessel source = new Vessel();
        source.readFromNBT(state(3000, multiPhase.getRegistryName(), 3 * GTValues.L, GTValues.L));
        Vessel receiver = new Vessel();
        receiver.readFromNBT(emptyAt(3000));
        NBTTagCompound before = receiver.save();
        assertEquals(3L * GTValues.M, receiver.fillMold(multiPhase, 3L * GTValues.M,
                3000, EnumFacing.UP, true));
        assertEquals(before, receiver.save());
        assertEquals(3L * GTValues.M, source.fillMoldAtSide(receiver, EnumFacing.EAST, EnumFacing.UP));
        assertEquals(0, source.save().getTagList("Contents", 10).tagCount());
        assertEquals(3L * GTValues.M, onlyContent(receiver.save()).getLong("Amount"));
        assertEquals(multiPhase.getRegistryName(), onlyContent(receiver.save()).getString("Material"));
        assertEquals(3000, receiver.getCurrentTemperature());
        Vessel loaded = new Vessel();
        loaded.readFromNBT(receiver.save());
        assertEquals(onlyContent(receiver.save()), onlyContent(loaded.save()));
    }

    @Test
    void cruciblePourFallbackTakesExactlyOneMaterialUnitAndKeepsSourceFraction() {
        Vessel source = new Vessel();
        // Three M plus one mB of the same material's GAS phase, represented
        // by the shared denominator rather than a separate gas inventory.
        source.readFromNBT(state(3000, multiPhase.getRegistryName(), 3001, 1000));
        NBTTagCompound sourceEntry = onlyContent(source.save());
        long sourceAmount = sourceEntry.getLong("Amount");
        int sourceRemainder = sourceEntry.getInteger("FluidRemainder");
        Vessel receiver = new Vessel();
        receiver.readFromNBT(state(2500, "gregtech:tin", 15 * GTValues.L, GTValues.L));
        NBTTagCompound before = receiver.save();
        assertEquals(GTValues.M, receiver.fillMold(multiPhase, sourceAmount, 3000, EnumFacing.UP, true));
        assertEquals(before, receiver.save());
        assertEquals(GTValues.M, source.fillMoldAtSide(receiver, EnumFacing.WEST, EnumFacing.UP));
        NBTTagCompound remaining = onlyContent(source.save());
        assertEquals(sourceAmount - GTValues.M, remaining.getLong("Amount"));
        assertEquals(sourceRemainder, remaining.getInteger("FluidRemainder"));
        assertEquals(15L * GTValues.M, content(receiver.save(), "gregtech:tin").getLong("Amount"));
        assertEquals(GTValues.M, content(receiver.save(), multiPhase.getRegistryName()).getLong("Amount"));
        assertTrue(receiver.getCurrentTemperature() > 2500 && receiver.getCurrentTemperature() < 3000);
        assertEquals(3000, source.getCurrentTemperature());
    }

    @Test
    void subUnitFreeSpaceDoesNotMakeCrucibleAcceptAnArbitrarySliceOfALargerOffer() {
        Vessel source = new Vessel();
        source.readFromNBT(state(3000, multiPhase.getRegistryName(), 2 * GTValues.L, GTValues.L));
        Vessel receiver = new Vessel();
        receiver.readFromNBT(state(2500, "gregtech:tin", 15 * GTValues.L + GTValues.L / 2, GTValues.L));
        NBTTagCompound sourceBefore = source.save();
        NBTTagCompound receiverBefore = receiver.save();
        assertEquals(0, source.fillMoldAtSide(receiver, EnumFacing.SOUTH, EnumFacing.UP));
        assertEquals(sourceBefore, source.save());
        assertEquals(receiverBefore, receiver.save());
        assertEquals(GTValues.M / 2, receiver.fillMold(multiPhase, GTValues.M / 2,
                3000, EnumFacing.UP, true));
        assertEquals(receiverBefore, receiver.save());
        assertEquals(GTValues.M / 2, receiver.fillMold(multiPhase, GTValues.M / 2,
                3000, EnumFacing.UP, false));
        assertEquals(GTValues.M / 2, content(receiver.save(), multiPhase.getRegistryName()).getLong("Amount"));
    }

    @Test
    void coldCrucibleSolidifiesIncomingWaterOnlyOnActualPourAndKeepsItsAmount() {
        Vessel source = new Vessel();
        source.readFromNBT(state(300, "gregtech:water", 1000, 1000));
        Vessel receiver = new Vessel(osmium);
        receiver.readFromNBT(emptyAt(250));
        NBTTagCompound before = receiver.save();
        assertEquals(GTValues.M, receiver.fillMold(Materials.Water, GTValues.M,
                300, EnumFacing.UP, true));
        assertEquals(before, receiver.save());
        assertEquals(0, receiver.world.blockChanges);
        assertEquals(GTValues.M, source.fillMoldAtSide(receiver, EnumFacing.NORTH, EnumFacing.UP));
        assertEquals(0, source.save().getTagList("Contents", 10).tagCount());
        NBTTagCompound ice = onlyContent(receiver.save());
        assertEquals("gregtech:ice", ice.getString("Material"));
        assertEquals(GTValues.M, ice.getLong("Amount"));
        assertFalse(ice.getBoolean("Molten"));
        assertTrue(receiver.getCurrentTemperature() > 250 && receiver.getCurrentTemperature() < 273);
    }

    @Test
    void cruciblePourRejectsArrivalPhaseExpansionBeforeChangingTemperatureOrContent() {
        Vessel receiver = new Vessel();
        receiver.readFromNBT(state(2000, "gregtech:tin", 13 * GTValues.L, GTValues.L));
        NBTTagCompound before = receiver.save();
        int dirty = receiver.dirtyCalls;
        // Raw 1M fits the remaining 3M, but arrival melting expands it into
        // this project's 1000mB lava quantity (greater than 6M), which does not.
        assertEquals(0, receiver.fillMold(Materials.Obsidian, GTValues.M, 1000, EnumFacing.UP, true));
        assertEquals(0, receiver.fillMold(Materials.Obsidian, GTValues.M, 1000, EnumFacing.UP, false));
        assertEquals(before, receiver.save());
        assertEquals(dirty, receiver.dirtyCalls);
        assertEquals(0, receiver.world.blockChanges);
    }

    @Test
    void hotterThanShellLimitIncomingMaterialIsMixedBeforeAnyShellHazard() {
        Vessel receiver = new Vessel(osmium, 1000);
        receiver.readFromNBT(emptyAt(293));
        Material tin = MetaTileEntityCrucible.resolveMaterial("tin");
        NBTTagCompound before = receiver.save();
        assertEquals(GTValues.M, receiver.fillMold(tin, GTValues.M, 10_000, EnumFacing.UP, true));
        assertEquals(before, receiver.save());
        assertEquals(0, receiver.world.blockChanges);
        assertEquals(GTValues.M, receiver.fillMold(tin, GTValues.M, 10_000, EnumFacing.UP, false));
        assertTrue(receiver.getCurrentTemperature() > 293 && receiver.getCurrentTemperature() < 1000);
        assertEquals(GTValues.M, onlyContent(receiver.save()).getLong("Amount"));
        assertEquals(0, receiver.world.blockChanges);
    }

    @Test
    void pouringBackIntoTheSameCrucibleDoesNotMutateItsLiveContentIteration() {
        Vessel source = new Vessel();
        source.readFromNBT(state(3000, multiPhase.getRegistryName(), GTValues.L, GTValues.L));
        NBTTagCompound before = source.save();
        assertEquals(0, source.fillMoldAtSide(source, EnumFacing.NORTH, EnumFacing.UP));
        assertEquals(before, source.save());
    }

    @Test
    void coreOxideActuallyMeltsToSharedHematiteThenSolidifiesWithoutChangingAmount() {
        Vessel vessel = new Vessel();
        NBTTagCompound initial = state(1207, "gtqtcore:iron_iii_oxide", GTValues.L, GTValues.L);
        onlyContent(initial).setBoolean("Molten", false);
        vessel.readFromNBT(initial);
        vessel.update();
        NBTTagCompound melted = onlyContent(vessel.save());
        assertEquals("gregtech:banded_iron", melted.getString("Material"));
        assertEquals(GTValues.M, melted.getLong("Amount"));
        assertTrue(melted.getBoolean("Molten"));
        NBTTagCompound cooler = vessel.save();
        cooler.setLong("Temperature", 1206);
        cooler.setLong("OldTemperature", 1207);
        vessel.readFromNBT(cooler);
        vessel.update();
        NBTTagCompound solid = onlyContent(vessel.save());
        assertEquals("gregtech:banded_iron", solid.getString("Material"));
        assertEquals(GTValues.M, solid.getLong("Amount"));
        assertFalse(solid.getBoolean("Molten"));
    }

    @Test
    void knownDefaultTargetOverridesHostPropertyButUnknownMaterialStillUsesIt() {
        Material tin = MetaTileEntityCrucible.resolveMaterial("tin");
        assertSame(tin, MetaTileEntityCrucible.getSmeltingTarget(tin));
        assertSame(MetaTileEntityCrucible.resolveMaterial("wrought_iron"),
                MetaTileEntityCrucible.getSmeltingTarget(MetaTileEntityCrucible.resolveMaterial("host_refinable")));
        Vessel vessel = new Vessel();
        NBTTagCompound state = state(505, "gregtech:tin", GTValues.L, GTValues.L);
        state.setLong("OldTemperature", 504);
        onlyContent(state).setBoolean("Molten", false);
        vessel.readFromNBT(state);
        vessel.update();
        NBTTagCompound molten = onlyContent(vessel.save());
        assertEquals("gregtech:tin", molten.getString("Material"));
        assertEquals(GTValues.M, molten.getLong("Amount"));
        assertTrue(molten.getBoolean("Molten"));
    }

    @Test
    void ironMeltingDoesNotPrematurelyApplyHostWroughtIronTarget() {
        Material iron = MetaTileEntityCrucible.resolveMaterial("iron");
        assertSame(iron, MetaTileEntityCrucible.getSmeltingTarget(iron));
        Vessel vessel = new Vessel();
        NBTTagCompound state = state(1811, "gregtech:iron", GTValues.L, GTValues.L);
        state.setLong("OldTemperature", 1810);
        onlyContent(state).setBoolean("Molten", false);
        vessel.readFromNBT(state);
        vessel.update();
        NBTTagCompound molten = onlyContent(vessel.save());
        assertEquals("gregtech:iron", molten.getString("Material"));
        assertEquals(GTValues.M, molten.getLong("Amount"));
        assertTrue(molten.getBoolean("Molten"));
    }

    @Test
    void retiredAddonWheatLoadsAsHostWheatWithoutLosingItsQuantity() {
        Vessel vessel = new Vessel();
        NBTTagCompound legacy = state(300, "gt6addition:wheat", GTValues.L, GTValues.L);
        vessel.readFromNBT(legacy);
        NBTTagCompound saved = onlyContent(vessel.save());
        assertEquals("gregtech:wheat", saved.getString("Material"));
        assertEquals(GTValues.M, saved.getLong("Amount"));
        assertEquals(300L, vessel.getCurrentTemperature());
        assertSame(MetaTileEntityCrucible.resolveMaterial("gregtech:wheat"),
                MetaTileEntityCrucible.resolveMaterial("gt6addition:wheat"));
        assertNull(MetaTileEntityCrucible.resolveMaterial("thirdparty:wheat"));
        assertNull(MetaTileEntityCrucible.resolveMaterial("gt6addition:iron"));
        Vessel reloaded = new Vessel();
        reloaded.readFromNBT(vessel.save());
        assertEquals(saved, onlyContent(reloaded.save()));
    }

    @Test
    void actualFillSimulationLeavesTemperatureInventoryAndWorldUntouched() {
        Vessel vessel = new Vessel();
        NBTTagCompound before = vessel.save();
        FluidStack input = new FluidStack(FluidRegistry.LAVA, 1000);
        assertEquals(1000, vessel.fluids().fill(input, false));
        assertEquals(before, vessel.save());
        assertEquals(1000, input.amount);
        assertEquals(0, vessel.world.entityQueries);
        assertEquals(0, vessel.world.blockChanges);
        assertEquals(0, vessel.dirtyCalls);
        assertEquals(0, vessel.renderCalls);
    }

    @Test
    void actualWaterFillUsesSharedCapacityAndDrainSimulationDoesNotConsume() {
        Vessel vessel = new Vessel();
        assertEquals(16_000, vessel.fluids().fill(new FluidStack(FluidRegistry.WATER, 20_000), true));
        NBTTagCompound full = vessel.save();
        assertEquals(16L * GTValues.M, onlyContent(full).getLong("Amount"));
        assertEquals(0, vessel.fluids().fill(new FluidStack(FluidRegistry.LAVA, 1), true));
        assertEquals(full, vessel.save());
        IFluidTankProperties tank = vessel.fluids().getTankProperties()[0];
        assertEquals(16_000, tank.getCapacity());
        assertEquals(16_000, tank.getContents().amount);
        assertFalse(tank.canFill());
        assertFalse(tank.canFillFluidType(new FluidStack(FluidRegistry.LAVA, 1)));
        assertEquals(1000, vessel.fluids().drain(1000, false).amount);
        assertEquals(full, vessel.save());
        assertEquals(1000, vessel.fluids().drain(1000, true).amount);
        assertEquals(15_000, vessel.fluids().getTankProperties()[0].getContents().amount);
        assertTrue(vessel.fluids().getTankProperties()[0].canFill());
    }

    @Test
    void fractionalMixedFluidQuantitiesSurviveTheActualNbtRoundTrip() {
        Vessel vessel = new Vessel();
        assertEquals(1234, vessel.fluids().fill(new FluidStack(FluidRegistry.WATER, 1234), true));
        assertEquals(145, vessel.fluids().fill(new FluidStack(FluidRegistry.LAVA, 145), true));
        NBTTagCompound saved = vessel.save();
        assertTrue(content(saved, "gregtech:water").getInteger("FluidRemainder") > 0);
        Vessel loaded = new Vessel();
        loaded.readFromNBT(saved);
        // Historical SolidifyTarget is deliberately re-resolved from current
        // material rules; compare actual quantities/state, not obsolete hints.
        assertEquals(2, loaded.save().getTagList("Contents", 10).tagCount());
        for (String name : new String[]{"gregtech:water", "gregtech:lava"}) {
            NBTTagCompound before = content(saved, name);
            NBTTagCompound after = content(loaded.save(), name);
            assertEquals(before.getLong("Amount"), after.getLong("Amount"));
            assertEquals(before.getInteger("FluidRemainder"), after.getInteger("FluidRemainder"));
            assertEquals(before.getBoolean("Molten"), after.getBoolean("Molten"));
        }
        assertEquals(saved.getLong("OldTemperature"), loaded.save().getLong("OldTemperature"));
        assertEquals(saved.getLong("StoredHeat"), loaded.save().getLong("StoredHeat"));
        assertEquals(vessel.getCurrentTemperature(), loaded.getCurrentTemperature());
        assertEquals(1234, volume(content(loaded.save(), "gregtech:water"), 1000));
        assertEquals(145, volume(content(loaded.save(), "gregtech:lava"), GTValues.L));
    }

    @Test
    void actualTickCondensesLavaOnlyBelow1300AndOnlyInWholeBuckets() {
        for (int temperature : new int[]{1299, 1300}) {
            Vessel vessel = new Vessel();
            vessel.readFromNBT(state(temperature, "gregtech:lava", 1145, GTValues.L));
            vessel.update();
            NBTTagCompound saved = vessel.save();
            if (temperature == 1299) {
                assertEquals(GTValues.M, content(saved, "gregtech:obsidian").getLong("Amount"));
                assertFalse(content(saved, "gregtech:obsidian").getBoolean("Molten"));
                assertEquals(145, volume(content(saved, "gregtech:lava"), GTValues.L));
            } else {
                assertEquals(1, saved.getTagList("Contents", 10).tagCount());
                assertEquals(1145, volume(onlyContent(saved), GTValues.L));
                assertTrue(onlyContent(saved).getBoolean("Molten"));
            }
        }
    }

    @Test
    void lavaCondensationAndRemeltingDoNotUseTheHostPrimaryGasUnit() {
        FluidProperty property = Materials.Lava.getProperty(PropertyKey.FLUID);
        FluidStorageKey primary = property.getPrimaryKey();
        try {
            property.setPrimaryKey(FluidStorageKeys.GAS);
            assertEquals(1000, CrucibleFluidUnits.defaultFluidUnit(Materials.Lava));
            Vessel cold = new Vessel();
            cold.readFromNBT(state(1299, "gregtech:lava", 1145, GTValues.L));
            cold.update();
            assertEquals(GTValues.M, content(cold.save(), "gregtech:obsidian").getLong("Amount"));
            assertEquals(145, volume(content(cold.save(), "gregtech:lava"), GTValues.L));
            Vessel loaded = new Vessel();
            loaded.readFromNBT(cold.save());
            assertEquals(145, volume(content(loaded.save(), "gregtech:lava"), GTValues.L));

            Vessel hot = new Vessel();
            hot.readFromNBT(state(1300, "gregtech:lava", 1145, GTValues.L));
            hot.update();
            assertEquals(1145, volume(onlyContent(hot.save()), GTValues.L));
            assertEquals("gregtech:lava", onlyContent(hot.save()).getString("Material"));

            // Restoring a liquid projection must not require an inventory
            // migration, nor change the quantity produced by reheating.
            property.setPrimaryKey(primary);
            NBTTagCompound warmer = loaded.save();
            warmer.setLong("Temperature", 1300);
            warmer.setLong("OldTemperature", 1299);
            loaded.readFromNBT(warmer);
            loaded.update();
            assertEquals(1145, volume(onlyContent(loaded.save()), GTValues.L));
            assertEquals(1145, loaded.fluids().drain(2000, false).amount);
        } finally {
            property.setPrimaryKey(primary);
        }
    }

    @Test
    void ambiguousHostLavaBindingBlocksProjectionButNotInternalBucketConversion() {
        FluidProperty property = Materials.Lava.getProperty(PropertyKey.FLUID);
        Fluid gas = property.get(FluidStorageKeys.GAS);
        try {
            property.store(FluidStorageKeys.GAS, FluidRegistry.LAVA);
            assertEquals(0, CrucibleFluidUnits.defaultFluidUnit(Materials.Lava));
            Vessel vessel = new Vessel();
            vessel.readFromNBT(state(1299, "gregtech:lava", 1145, GTValues.L));
            vessel.update();
            assertEquals(GTValues.M, content(vessel.save(), "gregtech:obsidian").getLong("Amount"));
            assertEquals(145, volume(content(vessel.save(), "gregtech:lava"), GTValues.L));
            NBTTagCompound before = vessel.save();
            assertNull(vessel.fluids().drain(2000, false));
            assertEquals(before, vessel.save());
            Vessel loaded = new Vessel();
            loaded.readFromNBT(before);
            assertEquals(145, volume(content(loaded.save(), "gregtech:lava"), GTValues.L));
            assertEquals(GTValues.M, content(loaded.save(), "gregtech:obsidian").getLong("Amount"));
        } finally {
            property.store(FluidStorageKeys.GAS, gas);
        }
    }

    @Test
    void boundLavaGasKeepsPhaseInputUnitsWithoutCreatingOneObsidianPerGasBucket() {
        FluidProperty property = Materials.Lava.getProperty(PropertyKey.FLUID);
        FluidStorageKey primary = property.getPrimaryKey();
        try {
            property.setPrimaryKey(FluidStorageKeys.GAS);
            Vessel vessel = new Vessel();
            vessel.readFromNBT(emptyAt(293));
            NBTTagCompound before = vessel.save();
            FluidStack offered = new FluidStack(lavaGas, 1000);
            assertEquals(1000, vessel.fluids().fill(offered, false));
            assertEquals(before, vessel.save());
            assertEquals(1000, vessel.fluids().fill(offered, true));
            assertTrue(vessel.getCurrentTemperature() < 1300);
            assertEquals(GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
            assertEquals(144, volume(onlyContent(vessel.save()), GTValues.L));
            vessel.update();
            assertEquals("gregtech:lava", onlyContent(vessel.save()).getString("Material"));
            assertEquals(GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
            assertEquals(144, volume(onlyContent(vessel.save()), GTValues.L));
        } finally {
            property.setPrimaryKey(primary);
        }
    }

    @Test
    void coldSubBucketLavaDisplayUsesItsInternalLiquidQuantity() throws ReflectiveOperationException {
        FluidProperty property = Materials.Lava.getProperty(PropertyKey.FLUID);
        FluidStorageKey primary = property.getPrimaryKey();
        Fluid gas = property.get(FluidStorageKeys.GAS);
        java.lang.reflect.Field display = MetaTileEntityCrucible.class.getDeclaredField("displayMolten");
        display.setAccessible(true);
        try {
            property.setPrimaryKey(FluidStorageKeys.GAS);
            Vessel gasPrimary = new Vessel();
            gasPrimary.readFromNBT(state(1299, "gregtech:lava", 450, GTValues.L));
            assertTrue(display.getBoolean(gasPrimary));
            gasPrimary.update();
            assertTrue(display.getBoolean(gasPrimary));
            assertEquals(450, volume(onlyContent(gasPrimary.save()), GTValues.L));

            property.setPrimaryKey(primary);
            property.store(FluidStorageKeys.GAS, FluidRegistry.LAVA);
            assertEquals(0, CrucibleFluidUnits.defaultFluidUnit(Materials.Lava));
            Vessel ambiguous = new Vessel();
            ambiguous.readFromNBT(state(1299, "gregtech:lava", 450, GTValues.L));
            assertTrue(display.getBoolean(ambiguous));
            ambiguous.update();
            assertTrue(display.getBoolean(ambiguous));
            assertEquals(450, volume(onlyContent(ambiguous.save()), GTValues.L));
        } finally {
            property.store(FluidStorageKeys.GAS, gas);
            property.setPrimaryKey(primary);
        }
    }

    @Test
    void lavaRemainderAndRemeltedObsidianCoalesceForTheFluidProjection() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(1299, "gregtech:lava", 1145, GTValues.L));
        vessel.update();
        assertEquals(GTValues.M, content(vessel.save(), "gregtech:obsidian").getLong("Amount"));
        assertEquals(145, volume(content(vessel.save(), "gregtech:lava"), GTValues.L));
        assertNull(vessel.fluids().drain(2000, false), "Cold remainder is not extractable");
        for (int cycle = 0; cycle < 3; cycle++) {
            NBTTagCompound hotter = vessel.save();
            hotter.setLong("Temperature", 1300);
            hotter.setLong("OldTemperature", 1299);
            vessel.readFromNBT(hotter);
            vessel.update();
            NBTTagCompound combined = onlyContent(vessel.save());
            assertEquals("gregtech:lava", combined.getString("Material"));
            assertEquals(1145, volume(combined, GTValues.L));
            assertEquals(1145, vessel.fluids().getTankProperties()[0].getContents().amount);
            NBTTagCompound beforeDrain = vessel.save();
            assertEquals(1145, vessel.fluids().drain(2000, false).amount);
            assertEquals(beforeDrain, vessel.save());
            NBTTagCompound colder = vessel.save();
            colder.setLong("Temperature", 1299);
            colder.setLong("OldTemperature", 1300);
            vessel.readFromNBT(colder);
            vessel.update();
            assertEquals(GTValues.M, content(vessel.save(), "gregtech:obsidian").getLong("Amount"));
            assertEquals(145, volume(content(vessel.save(), "gregtech:lava"), GTValues.L));
        }
    }

    @Test
    void duplicateFractionalFluidEntriesMergeWithoutRoundingOrSimulatedMutation() {
        Vessel vessel = new Vessel();
        NBTTagCompound first = state(300, "gregtech:water", 123, 1000);
        NBTTagList entries = first.getTagList("Contents", 10);
        entries.appendTag(onlyContent(state(300, "gregtech:water", 234, 1000)));
        vessel.readFromNBT(first);
        NBTTagCompound before = vessel.save();
        assertEquals(1, vessel.fluids().fill(new FluidStack(FluidRegistry.WATER, 1), false));
        assertEquals(before, vessel.save(), "Simulation must not normalize old saves");
        vessel.update();
        assertEquals(357, volume(onlyContent(vessel.save()), 1000));
        assertEquals(357, vessel.fluids().drain(1000, true).amount);
        assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
    }

    @Test
    void sameMaterialMergePreservesUnmergeableLegacyOverflow() {
        Vessel vessel = new Vessel();
        NBTTagCompound data = state(300, "gregtech:water", 1000, 1000);
        NBTTagList entries = data.getTagList("Contents", 10);
        entries.getCompoundTagAt(0).setLong("Amount", Long.MAX_VALUE);
        entries.appendTag(onlyContent(state(300, "gregtech:water", 1000, 1000)));
        entries.appendTag(onlyContent(state(300, "gregtech:water", 1000, 1000)));
        vessel.readFromNBT(data);
        vessel.update();
        NBTTagList saved = vessel.save().getTagList("Contents", 10);
        assertEquals(2, saved.tagCount(), "Overflow stays separate; the two small entries still merge");
        assertEquals(Long.MAX_VALUE, saved.getCompoundTagAt(0).getLong("Amount"));
        assertEquals(2L * GTValues.M, saved.getCompoundTagAt(1).getLong("Amount"));
        assertEquals(0, vessel.fluids().fill(new FluidStack(FluidRegistry.WATER, 1), true));
    }

    @Test
    void actualTickBoilsWaterAt373ButNot372AndQueriesNearbyEntities() {
        Vessel cool = new Vessel();
        cool.readFromNBT(state(372, "gregtech:water", 1000, 1000));
        cool.update();
        assertEquals(1000, volume(onlyContent(cool.save()), 1000));
        int contactQueries = cool.world.entityQueries;
        Vessel hot = new Vessel();
        hot.readFromNBT(state(373, "gregtech:water", 1000, 1000));
        hot.update();
        assertEquals(0, hot.save().getTagList("Contents", 10).tagCount());
        assertTrue(hot.world.entityQueries > contactQueries, "Steam must run the nearby-entity hazard path");
        assertEquals(0, hot.world.blockChanges, "373 K steam must not start fire");
    }

    @Test
    void boilingUsesWaterBoilingPointForDamageAndRespectsRangeAndFireResistance() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(1500, "gregtech:water", 1000, 1000));
        ProbeCow nearby = new ProbeCow(vessel.world, -2, 0.5, 0.5);
        ProbeCow outside = new ProbeCow(vessel.world, -10, 0.5, 0.5);
        ProbeCow resistant = new ProbeCow(vessel.world, 2, 0.5, 0.5);
        resistant.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 100));
        vessel.world.entities.add(nearby);
        vessel.world.entities.add(outside);
        vessel.world.entities.add(resistant);
        vessel.update();
        assertEquals(1, nearby.damageCalls);
        assertEquals(2.92F, nearby.receivedDamage, 0.0001F); // 2 * (373 - 300) / 50, not 1500 K.
        assertEquals(0, outside.damageCalls);
        assertEquals(0, resistant.damageCalls);
        assertEquals(0, vessel.world.blockChanges);
    }

    @Test
    void actualRainTickTruncatesRainfallBeforeDoublingThunderInput() {
        for (boolean thunder : new boolean[]{false, true}) {
            Vessel vessel = new Vessel();
            vessel.offsetTick = 10;
            vessel.world.rain = true;
            vessel.world.thunder = thunder;
            vessel.world.biome = new Biome(new Biome.BiomeProperties("crucible_rain_test")
                    .setTemperature(0.6F).setRainfall(0.655F)) {};
            vessel.update();
            assertEquals(thunder ? 130 : 65, volume(onlyContent(vessel.save()), 1000));
            assertTrue(vessel.getCurrentTemperature() < 293, "Rain uses the biome's ambient temperature");
            assertEquals(0, vessel.world.blockChanges);
        }
    }

    @Test
    void rainRespectsTimingExposureBiomeAndUnifiedCapacity() {
        Vessel wrongTick = new Vessel();
        wrongTick.world.rain = true;
        wrongTick.update();
        assertEquals(0, wrongTick.save().getTagList("Contents", 10).tagCount());
        Vessel covered = new Vessel();
        covered.offsetTick = 10;
        covered.update();
        assertEquals(0, covered.save().getTagList("Contents", 10).tagCount());
        for (Biome biome : new Biome[]{
                new Biome(new Biome.BiomeProperties("cold_rain_test").setTemperature(0.1F).setRainfall(0.5F)) {},
                new Biome(new Biome.BiomeProperties("dry_rain_test").setTemperature(0.6F).setRainfall(0)) {}}) {
            Vessel vessel = new Vessel();
            vessel.offsetTick = 10;
            vessel.world.rain = true;
            vessel.world.biome = biome;
            vessel.update();
            assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
        }
        Vessel full = new Vessel();
        full.readFromNBT(state(372, "gregtech:water", 16_000, 1000));
        full.offsetTick = 10;
        full.world.rain = true;
        full.update();
        assertEquals(16_000, volume(onlyContent(full.save()), 1000));
        assertEquals(372, full.getCurrentTemperature(), "Rejected rain must not change temperature");
    }

    @Test
    void waterFreezesBelow273AndDoesNotRemainDrainable() {
        Vessel frozen = new Vessel();
        frozen.readFromNBT(state(272, "gregtech:water", 1000, 1000));
        frozen.update();
        assertEquals("gregtech:ice", onlyContent(frozen.save()).getString("Material"));
        assertEquals(GTValues.M, onlyContent(frozen.save()).getLong("Amount"));
        assertNull(frozen.fluids().drain(1000, false));
        Vessel liquid = new Vessel();
        liquid.readFromNBT(state(273, "gregtech:water", 1000, 1000));
        liquid.update();
        assertEquals("gregtech:water", onlyContent(liquid.save()).getString("Material"));
        assertEquals(1000, liquid.fluids().drain(1000, false).amount);
    }

    @Test
    void reheatingOneObsidianRestores1000MbRatherThanMetal144Mb() {
        Vessel vessel = new Vessel();
        NBTTagCompound state = state(1299, "gregtech:obsidian", GTValues.L, GTValues.L);
        onlyContent(state).setBoolean("Molten", false);
        vessel.readFromNBT(state);
        // Simulate a real temperature crossing, preserving oldTemperature.
        NBTTagCompound heated = vessel.save();
        heated.setLong("Temperature", 1300);
        vessel.readFromNBT(heated);
        vessel.update();
        assertEquals("gregtech:lava", onlyContent(vessel.save()).getString("Material"));
        assertEquals(1000, vessel.fluids().drain(2000, false).amount);
        assertEquals(1000, vessel.fluids().drain(2000, true).amount);
        assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
    }

    @Test
    void fullQueuePreservesInputSlotAndDroppedRemaindersAcrossSaveAndResume() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(293, "gregtech:water", 16_000, 1000));
        vessel.getImportItems().setStackInSlot(0, recycledObsidian("A", 2, 1));
        EntityItem dropped = new EntityItem(vessel.world, 0.5, 0.5, 0.5, recycledObsidian("B", 64, 1));
        vessel.world.entities.add(dropped);
        vessel.update();
        assertEquals(64, vessel.getPendingItemCount());
        assertEquals(1, vessel.getImportItems().getStackInSlot(0).getCount());
        assertEquals(1, dropped.getItem().getCount());
        assertFalse(dropped.isDead);
        NBTTagList queue = vessel.save().getTagList("PendingItems", 10);
        assertEquals(2, queue.tagCount());
        assertEquals(1, new ItemStack(queue.getCompoundTagAt(0)).getCount());
        assertEquals("A", new ItemStack(queue.getCompoundTagAt(0)).getDisplayName());
        assertEquals(63, new ItemStack(queue.getCompoundTagAt(1)).getCount());
        assertEquals("B", new ItemStack(queue.getCompoundTagAt(1)).getDisplayName());

        Vessel loaded = new Vessel();
        loaded.readFromNBT(vessel.save());
        assertEquals(64, loaded.getPendingItemCount());
        assertEquals(queue, loaded.save().getTagList("PendingItems", 10));
        assertEquals(1000, loaded.fluids().drain(1000, true).amount);
        loaded.update();
        assertEquals(GTValues.M, content(loaded.save(), "gregtech:obsidian").getLong("Amount"));
        assertEquals(15_000, volume(content(loaded.save(), "gregtech:water"), 1000));
        assertEquals(64, loaded.getPendingItemCount()); // Queue A imported; slot A appended at the tail.
        assertTrue(loaded.getImportItems().getStackInSlot(0).isEmpty());
        NBTTagList resumed = loaded.save().getTagList("PendingItems", 10);
        assertEquals("B", new ItemStack(resumed.getCompoundTagAt(0)).getDisplayName());
        assertEquals(63, new ItemStack(resumed.getCompoundTagAt(0)).getCount());
        assertEquals("A", new ItemStack(resumed.getCompoundTagAt(1)).getDisplayName());
        List<ItemStack> drops = new ArrayList<>();
        loaded.clearMachineInventory(drops);
        assertEquals(64, drops.stream().mapToInt(ItemStack::getCount).sum());
        assertEquals(0, loaded.getPendingItemCount());
        loaded.clearMachineInventory(drops);
        assertEquals(64, drops.stream().mapToInt(ItemStack::getCount).sum());
    }

    @Test
    void invalidOrOverCapacityItemsRemainInTheirActualSources() {
        ItemStack malformed = recycledObsidian("malformed", 2, 1);
        malformed.getTagCompound().getCompoundTag(CrucibleRecyclingOverride.TAG).setInteger("size", 0);
        for (ItemStack rejected : new ItemStack[]{malformed, recycledObsidian("oversized", 2, 17)}) {
            Vessel vessel = new Vessel();
            vessel.getImportItems().setStackInSlot(0, rejected.copy());
            EntityItem dropped = new EntityItem(vessel.world, 0.5, 0.5, 0.5, rejected.copy());
            vessel.world.entities.add(dropped);
            vessel.update();
            assertEquals(2, vessel.getImportItems().getStackInSlot(0).getCount());
            assertEquals(2, dropped.getItem().getCount());
            assertFalse(dropped.isDead);
            assertEquals(0, vessel.getPendingItemCount());
            assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
            assertEquals(293, vessel.getCurrentTemperature());
        }
    }

    @Test
    void legacySpecialFluidOverflowIsPreservedDrainableAndNotReimportedTwice() {
        Vessel vessel = new Vessel();
        NBTTagCompound legacy = state(293, "gregtech:water", 16_000, 1000);
        legacy.setString("SpecialFluid", "water");
        legacy.setInteger("SpecialFluidAmount", 2000);
        vessel.readFromNBT(legacy);
        assertEquals(18_000, vessel.fluids().drain(Integer.MAX_VALUE, false).amount);
        assertEquals(0, vessel.fluids().fill(new FluidStack(FluidRegistry.WATER, 1), true));
        NBTTagCompound migrated = vessel.save();
        assertFalse(migrated.hasKey("SpecialFluid"));
        assertFalse(migrated.hasKey("SpecialFluidAmount"));
        Vessel loaded = new Vessel();
        loaded.readFromNBT(migrated);
        assertEquals(18_000, loaded.fluids().drain(Integer.MAX_VALUE, false).amount);
        assertEquals(3000, loaded.fluids().drain(3000, true).amount);
        assertEquals(1000, loaded.fluids().fill(new FluidStack(FluidRegistry.WATER, 2000), false));
    }

    @Test
    void legacyWaterUnitsMigrateWithoutChangingTheirFluidVolume() {
        NBTTagCompound legacy = state(293, "gregtech:water", GTValues.L, GTValues.L);
        legacy.removeTag("WaterUnitVersion");
        legacy.removeTag("FluidQuantityVersion");
        Vessel vessel = new Vessel();
        vessel.readFromNBT(legacy);
        assertEquals(GTValues.L, vessel.fluids().drain(1000, false).amount);
        assertEquals(1, vessel.save().getInteger("WaterUnitVersion"));
        assertEquals(1, vessel.save().getInteger("FluidQuantityVersion"));
        Vessel loaded = new Vessel();
        loaded.readFromNBT(vessel.save());
        assertEquals(GTValues.L, loaded.fluids().drain(1000, false).amount);
    }

    @Test
    void actualOsmiumCrucibleWith16IronUnitsGains8KPerSecondAt128HuPerTick() {
        Vessel vessel = new Vessel(osmium);
        vessel.readFromNBT(state(293, "gregtech:iron", 16 * GTValues.L, GTValues.L));
        for (int i = 0; i < 20; i++) {
            assertEquals(128, vessel.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, 128, false));
            vessel.update();
        }
        assertEquals(301, vessel.getCurrentTemperature());
        assertEquals(16L * GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
        // After the final effective heating tick, GT6 waits 100 ticks before
        // cooling by 1 K, then adjusts every 10 ticks.
        for (int i = 0; i < 99; i++) vessel.update();
        assertEquals(301, vessel.getCurrentTemperature());
        vessel.update();
        assertEquals(300, vessel.getCurrentTemperature());
        for (int i = 0; i < 9; i++) vessel.update();
        assertEquals(300, vessel.getCurrentTemperature());
        vessel.update();
        assertEquals(299, vessel.getCurrentTemperature());
    }

    @Test
    void actualCuUsesTheSameThermalMassAndSimulationDoesNotSpendEnergy() {
        Vessel vessel = new Vessel(osmium);
        vessel.readFromNBT(state(293, "gregtech:iron", 16 * GTValues.L, GTValues.L));
        NBTTagCompound before = vessel.save();
        assertEquals(128, vessel.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.CU, 128, true));
        assertEquals(before, vessel.save());
        for (int i = 0; i < 20; i++) {
            assertEquals(128, vessel.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.CU, 128, false));
            vessel.update();
        }
        assertEquals(285, vessel.getCurrentTemperature());
        assertEquals(16L * GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
    }

    @Test
    void creativeHotPlayerRemovalDoesNotProduceLavaDamageOrFire() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
        ProbePlayer player = new ProbePlayer(vessel.world, true);
        ProbeCow nearby = new ProbeCow(vessel.world, -2, 0.5, 0.5);
        vessel.world.entities.add(nearby);
        assertTrue(vessel.performPlayerRemoval(player, () -> {
            vessel.onRemoval();
            return true;
        }));
        assertEquals(0, vessel.world.blockChanges);
        assertEquals(0, nearby.damageCalls);
        assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
    }

    @Test
    void handRecoveryTakesOneObsidianThenStacksInTheSameHandWithoutReimporting() {
        Vessel vessel = new Vessel();
        loadSolidObsidian(vessel, 293, 3);
        ProbePlayer player = new ProbePlayer(vessel.world, false);
        for (int count = 1; count <= 3; count++) {
            assertTrue(vessel.onRightClick(player, net.minecraft.util.EnumHand.MAIN_HAND, EnumFacing.UP, null));
            assertEquals(net.minecraft.item.Item.getItemFromBlock(Blocks.OBSIDIAN),
                    player.getHeldItemMainhand().getItem());
            assertEquals(count, player.getHeldItemMainhand().getCount());
            assertEquals(0, vessel.getPendingItemCount());
            if (count < 3) assertEquals((3L - count) * GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
            else assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
        }
    }

    @Test
    void offhandRecoveryDoesNotReplaceTheMainHand() {
        Vessel vessel = new Vessel();
        loadSolidObsidian(vessel, 293, 3);
        ProbePlayer player = new ProbePlayer(vessel.world, false);
        player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND, 2));
        assertTrue(vessel.onRightClick(player, net.minecraft.util.EnumHand.OFF_HAND, EnumFacing.UP, null));
        assertEquals(2, player.getHeldItemMainhand().getCount());
        assertEquals(Items.DIAMOND, player.getHeldItemMainhand().getItem());
        assertEquals(1, player.getHeldItemOffhand().getCount());
        assertEquals(2L * GTValues.M, onlyContent(vessel.save()).getLong("Amount"));
    }

    @Test
    void fullHandRecoveryStackDoesNotConsumeContentsOrQueueTheHeldItem() {
        Vessel vessel = new Vessel();
        loadSolidObsidian(vessel, 293, 3);
        ProbePlayer player = new ProbePlayer(vessel.world, false);
        player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, new ItemStack(Blocks.OBSIDIAN, 64));
        NBTTagCompound before = vessel.save();
        assertTrue(vessel.onRightClick(player, net.minecraft.util.EnumHand.MAIN_HAND, EnumFacing.UP, null));
        assertEquals(before, vessel.save());
        assertEquals(64, player.getHeldItemMainhand().getCount());
        assertEquals(0, vessel.getPendingItemCount());
    }

    @Test
    void shovelRecoveryRequiresWholeStackSpaceAndChargesOnlySuccessfulRecovery() {
        Vessel vessel = new Vessel();
        loadSolidObsidian(vessel, 321, 11);
        ProbePlayer player = new ProbePlayer(vessel.world, false);
        for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
            player.inventory.mainInventory.set(slot, new ItemStack(Items.GOLD_INGOT, 64));
        }
        ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
        player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, shovel);
        NBTTagCompound before = vessel.save();
        assertTrue(vessel.onRightClick(player, net.minecraft.util.EnumHand.MAIN_HAND, EnumFacing.UP, null));
        assertEquals(before, vessel.save());
        assertEquals(0, shovel.getItemDamage());
        player.inventory.mainInventory.set(1, ItemStack.EMPTY);
        assertTrue(vessel.onRightClick(player, net.minecraft.util.EnumHand.MAIN_HAND, EnumFacing.UP, null));
        assertEquals(11, player.inventory.mainInventory.get(1).getCount());
        assertEquals(net.minecraft.item.Item.getItemFromBlock(Blocks.OBSIDIAN),
                player.inventory.mainInventory.get(1).getItem());
        assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
        assertEquals(2, shovel.getItemDamage()); // ceil(11/10) host durability units.
        assertEquals(20F, player.getHealth()); // Shovels do not inflict hand-contact damage.
    }

    private static void loadSolidObsidian(Vessel vessel, int temperature, int blocks) {
        NBTTagCompound data = state(temperature, "gregtech:obsidian", blocks * GTValues.L, GTValues.L);
        data.getTagList("Contents", 10).getCompoundTagAt(0).setBoolean("Molten", false);
        vessel.readFromNBT(data);
    }

    @Test
    void survivalPlayerRemovalUses1300BoundaryAndDoesNotDropTheHotMachine() {
        for (int temperature : new int[]{1299, 1300}) {
            Vessel vessel = new Vessel();
            vessel.readFromNBT(state(temperature, "gregtech:obsidian", GTValues.L, GTValues.L));
            ProbeCow nearby = new ProbeCow(vessel.world, -2, 0.5, 0.5);
            vessel.world.entities.add(nearby);
            assertTrue(vessel.performPlayerRemoval(new ProbePlayer(vessel.world, false), () -> {
                vessel.onRemoval();
                return true;
            }));
            if (temperature == 1299) {
                assertEquals(0, vessel.world.blockChanges);
                assertEquals(0, nearby.damageCalls);
                assertTrue(vessel.shouldDropWhenDestroyed());
            } else {
                assertHotReplacement(vessel.world);
                assertEquals(Blocks.FLOWING_LAVA, vessel.world.lastBlockState.getBlock());
                assertEquals(1, vessel.world.lastBlockState.getValue(BlockLiquid.LEVEL));
                assertEquals(20.0F, nearby.receivedDamage, 0.0001F); // GT6 removal multiplier 1.
                assertTrue(vessel.getCurrentTemperature() < 1300);
                assertFalse(vessel.shouldDropWhenDestroyed()); // The reset temperature must not restore drops.
            }
            assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
        }
    }

    @Test
    void refusedOrThrowingRemovalDoesNotLeaveAStoredCreativeExemption() {
        for (boolean throwsException : new boolean[]{false, true}) {
            Vessel vessel = new Vessel();
            vessel.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
            NBTTagCompound before = vessel.save();
            ProbePlayer creative = new ProbePlayer(vessel.world, true);
            if (throwsException) {
                assertThrows(IllegalStateException.class, () -> vessel.performPlayerRemoval(creative, () -> {
                    throw new IllegalStateException("Removal failed");
                }));
            } else {
                assertFalse(vessel.performPlayerRemoval(creative, () -> false));
            }
            assertEquals(before, vessel.save());
            assertEquals(0, vessel.world.blockChanges);
            // A later ordinary removal must still run the hot-removal branch.
            vessel.onRemoval();
            assertHotReplacement(vessel.world);
            assertFalse(vessel.shouldDropWhenDestroyed());
        }
    }

    @Test
    void nestedRemovalRestoresItsOuterPlayerContext() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
        assertTrue(vessel.performPlayerRemoval(new ProbePlayer(vessel.world, true), () -> {
            assertFalse(vessel.performPlayerRemoval(new ProbePlayer(vessel.world, false), () -> false));
            vessel.onRemoval();
            return true;
        }));
        assertEquals(0, vessel.world.blockChanges);
    }

    @Test
    void playerRemovalContextDoesNotLeakToAnotherCrucible() {
        Vessel creative = new Vessel();
        Vessel ordinary = new Vessel();
        creative.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
        ordinary.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
        assertTrue(creative.performPlayerRemoval(new ProbePlayer(creative.world, true), () -> {
            ordinary.onRemoval();
            creative.onRemoval();
            return true;
        }));
        assertEquals(0, creative.world.blockChanges);
        assertHotReplacement(ordinary.world);
    }

    @Test
    void absentPlayerDoesNotBypassTheHotRemovalHazard() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
        assertTrue(vessel.performPlayerRemoval(null, () -> {
            vessel.onRemoval();
            return true;
        }));
        assertHotReplacement(vessel.world);
    }

    @Test
    void explodedRemovalDoesNotProduceAnAdditionalHotLavaHazard() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
        vessel.markExploded();
        vessel.onRemoval();
        assertEquals(0, vessel.world.blockChanges);
        assertEquals(0, vessel.world.entityQueries);
        assertFalse(vessel.shouldDropWhenDestroyed());
        assertEquals(0, vessel.save().getTagList("Contents", 10).tagCount());
    }

    @Test
    void completedLavaReplacementReportsSuccessWhenTheOuterAirWriteFails() {
        Vessel vessel = new Vessel();
        vessel.world.exposeWrittenBlock = true;
        vessel.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
        assertTrue(vessel.performPlayerRemoval(null, () -> {
            vessel.onRemoval();
            return false; // Real Chunk rejects the outer air write after replacement.
        }));
        assertFalse(vessel.performPlayerRemoval(null, () -> false)); // Old destruction is not a new removal.
    }

    @Test
    void failedRemovalWithoutTheActualLavaReplacementStillReportsFailure() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
        assertFalse(vessel.performPlayerRemoval(null, () -> {
            vessel.onRemoval();
            return false;
        })); // This fixture's world still reports air: no verified lava write.
        assertFalse(vessel.shouldDropWhenDestroyed());
    }

    @Test
    void meltingDownKeepsItsDoubleDamageRatherThanPlayerRemovalMultiplier() {
        Vessel vessel = new Vessel(null, 1299);
        NBTTagCompound hot = new NBTTagCompound();
        hot.setLong("Temperature", 1300);
        hot.setLong("OldTemperature", 1300);
        vessel.readFromNBT(hot);
        ProbeCow nearby = new ProbeCow(vessel.world, -2, 0.5, 0.5);
        vessel.world.entities.add(nearby);
        vessel.update();
        assertEquals(40.0F, nearby.receivedDamage, 0.0001F);
        assertEquals(Blocks.FLOWING_LAVA, vessel.world.lastBlockState.getBlock());
        assertEquals(1, vessel.world.lastBlockState.getValue(BlockLiquid.LEVEL));
        assertFalse(vessel.shouldDropWhenDestroyed());
    }

    private static ItemStack recycledObsidian(String name, int count, int units) {
        ItemStack stack = new ItemStack(Items.PAPER, count);
        stack.setStackDisplayName(name);
        NBTTagCompound materials = new NBTTagCompound();
        materials.setInteger("size", 1);
        NBTTagCompound component = new NBTTagCompound();
        component.setString("m", "Obsidian");
        component.setLong("a", units * 648648000L);
        materials.setTag("0", component);
        stack.getTagCompound().setTag(CrucibleRecyclingOverride.TAG, materials);
        return stack;
    }

    private static NBTTagCompound emptyAt(long temperature) {
        NBTTagCompound state = new NBTTagCompound();
        state.setLong("Temperature", temperature);
        state.setLong("OldTemperature", temperature);
        return state;
    }

    private static NBTTagCompound state(long temperature, String material, int volume, int unit) {
        NBTTagCompound state = new NBTTagCompound();
        state.setLong("Temperature", temperature);
        state.setLong("OldTemperature", temperature);
        state.setInteger("WaterUnitVersion", 1);
        state.setInteger("FluidQuantityVersion", 1);
        CrucibleFluidUnits.Quantity quantity = CrucibleFluidUnits.storedFluidAmount(volume, unit);
        assertNotNull(quantity);
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("Material", material);
        entry.setLong("Amount", quantity.amount);
        entry.setInteger("FluidRemainder", quantity.remainder);
        entry.setBoolean("Molten", true);
        NBTTagList contents = new NBTTagList();
        contents.appendTag(entry);
        state.setTag("Contents", contents);
        return state;
    }

    @Test
    void shellWarningFollowsRealHeatAndCoolingAndRebuildsFromSavedTemperature()
            throws ReflectiveOperationException {
        java.lang.reflect.Field warning = MetaTileEntityCrucible.class.getDeclaredField("meltDownWarning");
        warning.setAccessible(true);
        Vessel vessel = new Vessel(null, 4133);
        vessel.readFromNBT(emptyAt(4033));
        assertFalse(warning.getBoolean(vessel));
        // Missing vessel material uses 7M at 1000 kg/m3: R = 1 + floor(777.777/100) = 8.
        NBTTagCompound before = vessel.save();
        assertEquals(8, vessel.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, 8, true));
        assertEquals(before, vessel.save());
        assertFalse(warning.getBoolean(vessel));
        assertEquals(8, vessel.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, 8, false));
        int renderCalls = vessel.renderCalls;
        vessel.update();
        assertEquals(4034, vessel.getCurrentTemperature());
        assertTrue(warning.getBoolean(vessel));
        assertTrue(vessel.renderCalls > renderCalls);

        Vessel loaded = new Vessel(null, 4133);
        loaded.readFromNBT(vessel.save());
        assertTrue(warning.getBoolean(loaded));
        renderCalls = loaded.renderCalls;
        assertEquals(8, loaded.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.CU, 8, false));
        loaded.update();
        assertEquals(4033, loaded.getCurrentTemperature());
        assertFalse(warning.getBoolean(loaded));
        assertTrue(loaded.renderCalls > renderCalls);
        assertEquals(0, vessel.world.blockChanges);
        assertEquals(0, loaded.world.blockChanges);
    }

    @Test
    void registeredGlowingShellIsEmissiveWithoutChangingMaterialFlagsOrInventory() {
        Vessel vessel = new Vessel(glowingShell);
        vessel.readFromNBT(emptyAt(293));
        NBTTagCompound before = vessel.save();
        assertTrue(CrucibleShellVisual.isEmissive(false, glowingShell));
        assertFalse(CrucibleShellVisual.isEmissive(false, osmium));
        assertTrue(CrucibleShellVisual.isEmissive(true, osmium));
        assertEquals(before, vessel.save());
        assertTrue(glowingShell.hasFlag(MaterialFlags.GLOWING));
        assertFalse(osmium.hasFlag(MaterialFlags.GLOWING));
        assertEquals(0, vessel.world.blockChanges);
    }

    @Test
    void registeredAmethystKeepsUnmeltableContentsUntilItsOwnBoilingBoundary() {
        assertSame(enderAmethyst, MetaTileEntityCrucible.resolveMaterial(GT6MaterialIdentity.name(8329)));
        for (Material material : new Material[]{enderAmethyst, amethyst}) {
            assertTrue(material.hasFlags(MaterialFlags.FLAMMABLE, MaterialFlags.EXPLOSIVE));
            assertFalse(GT6MaterialHazardData.shouldBurn(material, 2668));
            assertFalse(GT6MaterialHazardData.isExplosiveMaterial(material));
            assertNull(MetaTileEntityCrucible.getSmeltingTarget(material));
            assertFalse(MetaTileEntityCrucible.hasMaterialMeltingTarget(material));
            assertEquals(0, MetaTileEntityCrucible.getSmeltingOutputAmount(material, GTValues.M));
            assertSame(material, MetaTileEntityCrucible.getSolidifyingTarget(material));

            Vessel vessel = new Vessel();
            NBTTagCompound initial = state(2668, material.getRegistryName(), GTValues.L, GTValues.L);
            onlyContent(initial).setBoolean("Molten", false);
            vessel.readFromNBT(initial);
            vessel.update();
            NBTTagCompound content = onlyContent(vessel.save());
            assertEquals(material.getRegistryName(), content.getString("Material"));
            assertEquals(GTValues.M, content.getLong("Amount"));
            assertEquals(0, content.getInteger("FluidRemainder"));
            assertFalse(content.getBoolean("Molten"));
            assertEquals(0, vessel.world.blockChanges);
            Vessel restored = new Vessel();
            restored.readFromNBT(vessel.save());
            restored.update();
            assertEquals(content, onlyContent(restored.save()));
            assertTrue(material.hasFlags(MaterialFlags.FLAMMABLE, MaterialFlags.EXPLOSIVE));
        }

        Vessel boiling = new Vessel();
        NBTTagCompound initial = state(2669, enderAmethyst.getRegistryName(), GTValues.L, GTValues.L);
        onlyContent(initial).setBoolean("Molten", false);
        boiling.readFromNBT(initial);
        boiling.update();
        assertEquals(0, boiling.save().getTagList("Contents", 10).tagCount());
        assertEquals(0, boiling.world.lavaChanges);
        // High-temperature evaporation may ignite fire, but must not destroy
        // the vessel via the deliberately conflicting host EXPLOSIVE flag.
        assertEquals(boiling.world.fireChanges, boiling.world.blockChanges);
        Vessel ordinary = new Vessel();
        initial = state(2669, amethyst.getRegistryName(), GTValues.L, GTValues.L);
        onlyContent(initial).setBoolean("Molten", false);
        ordinary.readFromNBT(initial);
        ordinary.update();
        assertEquals(GTValues.M, onlyContent(ordinary.save()).getLong("Amount"));
        assertEquals(0, ordinary.world.blockChanges);
        assertTrue(GT6MaterialHazardData.shouldBurn(unknownCombustibleGem, 314));
        assertTrue(GT6MaterialHazardData.isExplosiveMaterial(unknownCombustibleGem));
    }

    private static NBTTagCompound onlyContent(NBTTagCompound data) {
        NBTTagList contents = data.getTagList("Contents", 10);
        assertEquals(1, contents.tagCount());
        return contents.getCompoundTagAt(0);
    }

    private static NBTTagCompound content(NBTTagCompound data, String name) {
        NBTTagList contents = data.getTagList("Contents", 10);
        for (int i = 0; i < contents.tagCount(); i++) {
            NBTTagCompound entry = contents.getCompoundTagAt(i);
            if (name.equals(entry.getString("Material"))) return entry;
        }
        fail("Missing content " + name + ": " + data);
        return null;
    }

    private static int volume(NBTTagCompound entry, int unit) {
        return CrucibleFluidUnits.storedFluidVolume(entry.getLong("Amount"),
                entry.getInteger("FluidRemainder"), unit);
    }

    private static final class Basin extends MetaTileEntityCastingBasin {
        final TestWorld world = new TestWorld();
        Basin() { super(new ResourceLocation("gt6addition", "basin_integration_test"),
                2, 0xFFFFFF, false, 6, 6, 10_000); }
        @Override public World getWorld() { return world; }
        @Override public BlockPos getPos() { return BlockPos.ORIGIN; }
        @Override public void markDirty() {}
        @Override public void scheduleRenderUpdate() {}
        IFluidHandler fluids() {
            IFluidHandler handler = getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);
            assertNotNull(handler);
            return handler;
        }
        NBTTagCompound save() { return writeToNBT(new NBTTagCompound()); }
    }

    private static final class Vessel extends MetaTileEntityCrucible {
        final TestWorld world = new TestWorld();
        int dirtyCalls, renderCalls;
        long offsetTick = 1;
        Vessel() { this(null); }
        Vessel(Material shell) { this(shell, 10_000); }
        Vessel(Material shell, int maximumTemperature) {
            this(shell, maximumTemperature, false);
        }
        Vessel(Material shell, int maximumTemperature, boolean acidProof) {
            super(new ResourceLocation("gt6addition", "fluid_integration_test"), 2, 0xFFFFFF,
                    maximumTemperature, shell, acidProof, 6, 6);
        }
        @Override public World getWorld() { return world; }
        @Override public BlockPos getPos() { return BlockPos.ORIGIN; }
        @Override public long getOffsetTimer() { return offsetTick; }
        @Override public void markDirty() { dirtyCalls++; }
        @Override public void scheduleRenderUpdate() { renderCalls++; }
        IFluidHandler fluids() {
            IFluidHandler handler = getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.NORTH);
            assertNotNull(handler);
            return handler;
        }
        NBTTagCompound save() { return writeToNBT(new NBTTagCompound()); }
        void markExploded() { setExploded(); }
    }

    private static void assertHotReplacement(TestWorld world) {
        assertEquals(1, world.lavaChanges, "Hot removal must write flowing lava exactly once");
        assertEquals(world.fireChanges + 1, world.blockChanges, "Only fire and the final lava replacement are allowed");
        assertEquals(Blocks.FLOWING_LAVA, world.lastBlockState.getBlock());
        assertEquals(1, world.lastBlockState.getValue(BlockLiquid.LEVEL));
    }

    /** Isolated CEu attribute/state metadata without registering any game fluids. */
    private static final class HazardProbeFluid extends Fluid implements AttributedFluid {
        private final FluidState state;
        private final List<FluidAttribute> attributes = new ArrayList<>();
        HazardProbeFluid(String name, ResourceLocation texture, FluidState state, boolean acid) {
            super(name, texture, texture);
            this.state = state;
            setTemperature(300);
            setGaseous(state != FluidState.LIQUID);
            if (acid) addAttribute(FluidAttributes.ACID);
        }
        @Override public Collection<FluidAttribute> getAttributes() { return Collections.unmodifiableList(attributes); }
        @Override public void addAttribute(FluidAttribute attribute) { attributes.add(attribute); }
        @Override public FluidState getState() { return state; }
    }

    private static final class TestWorld extends World {
        int entityQueries, blockChanges, lavaChanges, fireChanges;
        IBlockState lastBlockState;
        boolean rain, thunder, exposeWrittenBlock;
        Biome biome = Biomes.PLAINS;
        final List<Entity> entities = new ArrayList<>();
        TestWorld() {
            super(null, new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false);
        }
        @Override protected IChunkProvider createChunkProvider() { return null; }
        @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return false; }
        @Override public BlockPos getSpawnPoint() { return BlockPos.ORIGIN; }
        @Override public Biome getBiome(BlockPos pos) { return biome; }
        @Override public boolean isRainingAt(BlockPos pos) { return rain; }
        @Override public boolean isThundering() { return thunder; }
        @Override public IBlockState getBlockState(BlockPos pos) {
            return exposeWrittenBlock && BlockPos.ORIGIN.equals(pos) && lastBlockState != null ?
                    lastBlockState : Blocks.AIR.getDefaultState();
        }
        @Override public TileEntity getTileEntity(BlockPos pos) { return null; }
        @Override public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> type, AxisAlignedBB bounds) {
            entityQueries++;
            List<T> result = new ArrayList<>();
            for (Entity entity : entities) {
                if (type.isInstance(entity) && bounds.intersects(entity.getEntityBoundingBox())) result.add(type.cast(entity));
            }
            return result;
        }
        @Override public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> type, AxisAlignedBB bounds,
                com.google.common.base.Predicate<? super T> predicate) {
            return getEntitiesWithinAABB(type, bounds);
        }
        @Override public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
            blockChanges++;
            if (state.getBlock() == Blocks.FLOWING_LAVA) lavaChanges++;
            if (state.getBlock() == Blocks.FIRE) fireChanges++;
            lastBlockState = state;
            return true;
        }
    }

    private static final class ProbePlayer extends EntityPlayer {
        ProbePlayer(World world, boolean creative) {
            super(world, new GameProfile(new UUID(0, creative ? 1 : 2), "crucible_test"));
            capabilities.isCreativeMode = creative;
        }
        @Override public boolean isCreative() { return capabilities.isCreativeMode; }
        @Override public boolean isSpectator() { return false; }
    }

    private static final class ProbeCow extends EntityCow {
        int damageCalls;
        float receivedDamage;
        ProbeCow(World world, double x, double y, double z) {
            super(world);
            setPosition(x, y, z);
        }
        @Override public boolean attackEntityFrom(DamageSource source, float damage) {
            damageCalls++;
            receivedDamage += damage;
            return true;
        }
    }
}
