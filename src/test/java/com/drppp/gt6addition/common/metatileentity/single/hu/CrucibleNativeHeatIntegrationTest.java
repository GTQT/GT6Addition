package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.IHeatable;
import gregtech.api.capability.impl.HeatContainerHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.registry.MTEManager;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.capabilities.Capability;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/** Runs the dependency's real emitter update against the actual crucible capability. */
class CrucibleNativeHeatIntegrationTest {
    private static Capability<IHeatable> previousHeatCapability;
    private static MTEManager previousMteManager;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void bootstrap() throws Exception {
        Bootstrap.register();
        previousMteManager = GregTechAPI.mteManager;
        if (previousMteManager == null) GregTechAPI.mteManager = MTEManager.getInstance();
        previousHeatCapability = GregtechCapabilities.CAPABILITY_HEAT_CONTAINER;
        if (previousHeatCapability == null) {
            // JUnit does not run Forge's ASM @CapabilityInject lifecycle. Create
            // only this capability token; production never uses reflection.
            Constructor<?> constructor = Capability.class.getDeclaredConstructor(
                    String.class, Capability.IStorage.class, Callable.class);
            constructor.setAccessible(true);
            Capability.IStorage<IHeatable> storage = new Capability.IStorage<IHeatable>() {
                @Override public NBTBase writeNBT(Capability<IHeatable> cap, IHeatable value, EnumFacing side) {
                    return new NBTTagCompound();
                }
                @Override public void readNBT(Capability<IHeatable> cap, IHeatable value,
                                              EnumFacing side, NBTBase data) {}
            };
            GregtechCapabilities.CAPABILITY_HEAT_CONTAINER = (Capability<IHeatable>) constructor.newInstance(
                    IHeatable.class.getName(), storage, (Callable<IHeatable>) () -> IHeatable.DEFAULT);
        }
    }

    @AfterAll
    static void restoreCapability() {
        GregtechCapabilities.CAPABILITY_HEAT_CONTAINER = previousHeatCapability;
        GregTechAPI.mteManager = previousMteManager;
    }

    @Test
    void realNativeEmitterPushesHuIntoTheActualCrucibleBuffer() {
        TestWorld world = new TestWorld(false);
        Vessel vessel = new Vessel(world);
        Source source = new Source(world);
        source.neighbors.put(EnumFacing.NORTH, new VesselTile(vessel));
        HeatContainerHandler heat = HeatContainerHandler.emitterContainer(source, 10_000, 10_000, 128);
        heat.setHeatStored(1000);
        long temperature = vessel.getCurrentTemperature();
        heat.update();
        assertEquals(872, heat.getHeatStored());
        assertEquals(128, vessel.input(EnumFacing.SOUTH).getHeatStored());
        assertEquals(temperature, vessel.getCurrentTemperature());
        assertFalse(vessel.input(EnumFacing.SOUTH).canOutputHeat());
    }

    @Test
    void realNativeSourceSharesItsOutputBudgetAcrossTwoCrucibles() {
        TestWorld world = new TestWorld(false);
        Vessel north = new Vessel(world);
        Vessel south = new Vessel(world);
        Source source = new Source(world);
        source.neighbors.put(EnumFacing.NORTH, new VesselTile(north));
        source.neighbors.put(EnumFacing.SOUTH, new VesselTile(south));
        HeatContainerHandler heat = HeatContainerHandler.emitterContainer(source, 10_000, 10_000, 128);
        heat.setHeatStored(1000);
        heat.update();
        long first = north.input(EnumFacing.SOUTH).getHeatStored();
        long second = south.input(EnumFacing.NORTH).getHeatStored();
        assertEquals(128, first + second);
        assertEquals(872, heat.getHeatStored());
        assertTrue(first == 0 || second == 0);
    }

    @Test
    void realNativeEmitterHonorsItsOutputFaceRestriction() {
        TestWorld world = new TestWorld(false);
        Vessel vessel = new Vessel(world);
        Source source = new Source(world);
        source.neighbors.put(EnumFacing.NORTH, new VesselTile(vessel));
        HeatContainerHandler heat = HeatContainerHandler.emitterContainer(source, 10_000, 10_000, 128);
        heat.setSideOutputCondition(side -> side == EnumFacing.SOUTH);
        heat.setHeatStored(1000);
        heat.update();
        assertEquals(1000, heat.getHeatStored());
        assertEquals(0, vessel.input(EnumFacing.SOUTH).getHeatStored());
    }

    @Test
    void actualCrucibleCapabilityReusesEachSideBudgetAndSharesBufferAcrossSides() {
        TestWorld world = new TestWorld(false);
        Vessel vessel = new Vessel(world);
        IHeatable south = vessel.input(EnumFacing.SOUTH);
        assertSame(south, vessel.input(EnumFacing.SOUTH));
        assertEquals(256, south.transferHeat(1000, 10_000));
        assertEquals(0, vessel.input(EnumFacing.SOUTH).transferHeat(1000, 10_000));
        assertEquals(256, vessel.input(EnumFacing.NORTH).transferHeat(1000, 10_000));
        assertEquals(512, south.getHeatStored());
        world.setTotalWorldTime(1);
        assertEquals(256, south.transferHeat(1000, 10_000));
        assertEquals(768, south.getHeatStored());
        assertEquals(256, vessel.input(null).transferHeat(1000, 10_000));
        assertEquals(1024, south.getHeatStored());
    }

    @Test
    void actualClientAndUnattachedCruciblesRejectNativeEnergy() {
        Vessel client = new Vessel(new TestWorld(true));
        Vessel unattached = new Vessel(null);
        for (Vessel vessel : new Vessel[]{client, unattached}) {
            IHeatable input = vessel.input(EnumFacing.UP);
            assertFalse(input.canAcceptHeat());
            assertEquals(0, input.transferHeat(128, 2000));
            assertEquals(0, input.getHeatStored());
        }
    }

    private static final class Vessel extends MetaTileEntityCrucible {
        private final World testWorld;
        Vessel(World world) {
            super(new ResourceLocation("gt6addition", "native_heat_test"), 2, 0xFFFFFF);
            testWorld = world;
        }
        @Override public World getWorld() { return testWorld; }
        IHeatable input(EnumFacing side) {
            assertTrue(hasCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, side));
            IHeatable input = getCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, side);
            assertNotNull(input);
            return input;
        }
    }

    private static final class VesselTile extends TileEntity {
        private final Vessel vessel;
        VesselTile(Vessel vessel) { this.vessel = vessel; }
        @Override public boolean hasCapability(Capability<?> capability, EnumFacing side) {
            return vessel.hasCapability(capability, side);
        }
        @Override public <T> T getCapability(Capability<T> capability, EnumFacing side) {
            return vessel.getCapability(capability, side);
        }
    }

    private static final class Source extends MetaTileEntity {
        private final World testWorld;
        final EnumMap<EnumFacing, TileEntity> neighbors = new EnumMap<>(EnumFacing.class);
        Source(World world) {
            super(new ResourceLocation("gregtech", "native_heat_test_source"));
            testWorld = world;
        }
        @Override public World getWorld() { return testWorld; }
        @Override public TileEntity getNeighbor(EnumFacing side) { return neighbors.get(side); }
        @Override public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tile) { return new Source(testWorld); }
    }

    /** No save files, chunks, or live client: just the World object needed by the emitter. */
    private static final class TestWorld extends World {
        TestWorld(boolean client) {
            super(null, new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), client);
        }
        @Override protected IChunkProvider createChunkProvider() { return null; }
        @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return false; }
    }
}
