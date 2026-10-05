package com.drppp.gt6addition.common.metatileentity.single.hu;

import com.drppp.gt6addition.api.capability.interfaces.ICrucibleEnergyReceiver;
import com.mojang.authlib.GameProfile;
import gregtech.api.GregTechAPI;
import gregtech.api.GTValues;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
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
        Materials.Water = Material.builder(269, new ResourceLocation("gregtech", "water"))
                .fluid(FluidRegistry.WATER, FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        Materials.Lava = Material.builder(1600, new ResourceLocation("gregtech", "lava"))
                .fluid(FluidRegistry.LAVA, FluidStorageKeys.LIQUID, FluidState.LIQUID).build();
        Materials.Obsidian = Material.builder(297, new ResourceLocation("gregtech", "obsidian")).dust(3).build();
        Materials.Ice = Material.builder(1530, new ResourceLocation("gregtech", "ice")).dust().build();
        osmium = Material.builder(77, new ResourceLocation("gregtech", "osmium")).dust().build();
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
                assertEquals(1, vessel.world.blockChanges);
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
            assertEquals(1, vessel.world.blockChanges);
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
        assertEquals(1, ordinary.world.blockChanges);
    }

    @Test
    void absentPlayerDoesNotBypassTheHotRemovalHazard() {
        Vessel vessel = new Vessel();
        vessel.readFromNBT(state(1300, "gregtech:obsidian", GTValues.L, GTValues.L));
        assertTrue(vessel.performPlayerRemoval(null, () -> {
            vessel.onRemoval();
            return true;
        }));
        assertEquals(1, vessel.world.blockChanges);
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

    private static final class Vessel extends MetaTileEntityCrucible {
        final TestWorld world = new TestWorld();
        int dirtyCalls, renderCalls;
        long offsetTick = 1;
        Vessel() { this(null); }
        Vessel(Material shell) { this(shell, 10_000); }
        Vessel(Material shell, int maximumTemperature) {
            super(new ResourceLocation("gt6addition", "fluid_integration_test"), 2, 0xFFFFFF,
                    maximumTemperature, shell, false, 6, 6);
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

    private static final class TestWorld extends World {
        int entityQueries, blockChanges;
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
