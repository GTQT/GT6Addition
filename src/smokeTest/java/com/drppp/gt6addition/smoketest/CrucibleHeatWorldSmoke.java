package com.drppp.gt6addition.smoketest;

import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IHeatable;
import gregtech.api.capability.impl.HeatContainerHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.heat.BlockHeatConductor;
import gregtech.common.pipelike.heat.HeatConductorType;
import gregtech.common.pipelike.heat.ItemBlockHeatConductor;
import gregtech.common.pipelike.heat.tile.TileEntityHeatConductor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/** Real registered pipe blocks and routes; test-only emitter owner, never shipped. */
final class CrucibleHeatWorldSmoke {
    private static final Logger LOG = LogManager.getLogger("CrucibleParitySmoke");

    static void run(WorldServer world) {
        directSource(world);
        routedSource(world, false);
        routedSource(world, true);
        calibratedHeatingAndCooling(world);
    }

    private static void directSource(WorldServer world) {
        BlockPos sourcePos = new BlockPos(600, 70, 400);
        MetaTileEntityCrucible vessel = vessel(world, sourcePos.east());
        HeatContainerHandler source = emitter(world, sourcePos);
        source.setHeatStored(1000);
        source.update();
        check(source.getHeatStored() == 872, "Direct source did not debit exactly 128 HU");
        check(heat(vessel, EnumFacing.WEST).getHeatStored() == 128, "Direct vessel did not receive exactly 128 HU");
        check(vessel.getCurrentTemperature() == 293, "Source temperature replaced vessel temperature");
        LOG.info("CRUCIBLE_WORLD_CASE native_direct_heat passed");
    }

    private static void routedSource(WorldServer world, boolean branched) {
        BlockPos pipePos = new BlockPos(branched ? 664 : 632, 70, 400);
        MetaTileEntityCrucible first = vessel(world, pipePos.east());
        MetaTileEntityCrucible second = branched ? vessel(world, pipePos.south()) : null;
        TileEntityHeatConductor pipe = pipe(world, pipePos);
        pipe.setConnection(EnumFacing.WEST, true, false);
        pipe.setConnection(EnumFacing.EAST, true, false);
        if (branched) pipe.setConnection(EnumFacing.SOUTH, true, false);
        // Execute the dependency's scheduled registration callback, not a
        // fabricated route list or a substitute network implementation.
        BlockHeatConductor block = (BlockHeatConductor) world.getBlockState(pipePos).getBlock();
        block.updateTick(world, pipePos, world.getBlockState(pipePos), world.rand);
        IHeatable inlet = pipe.getCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, EnumFacing.WEST);
        check(inlet != null, "Registered real pipe did not expose an inlet");
        HeatContainerHandler source = emitter(world, pipePos.west());
        source.setHeatStored(1000);
        source.update();
        long delivered = heat(first, EnumFacing.WEST).getHeatStored() +
                (second == null ? 0 : heat(second, EnumFacing.NORTH).getHeatStored());
        long debited = 1000 - source.getHeatStored();
        LOG.info("CRUCIBLE_WORLD_HEAT branched={} delivered={} debited={} loss={}",
                branched, delivered, debited, pipe.getNodeData().getHeatLossPerBlock());
        check(delivered > 0, "Real pipe did not route heat to a crucible");
        check(delivered <= 128 && debited <= 128 && delivered <= debited,
                "Pipe network exceeded source budget or created HU: " + delivered + "/" + debited);
        check(first.getCurrentTemperature() == 293 && (second == null || second.getCurrentTemperature() == 293),
                "Pipe temperature replaced vessel temperature");
        LOG.info("CRUCIBLE_WORLD_CASE native_pipe_{} passed", branched ? "branch" : "single");
    }

    private static HeatContainerHandler emitter(WorldServer world, BlockPos pos) {
        HeatContainerHandler source = HeatContainerHandler.emitterContainer(new EmitterOwner(world, pos), 10000, 10000, 128);
        source.setSideOutputCondition(side -> side == EnumFacing.EAST);
        source.setTemperature(1000);
        return source;
    }

    private static void calibratedHeatingAndCooling(WorldServer world) {
        BlockPos sourcePos = new BlockPos(696, 70, 400);
        MetaTileEntityCrucible vessel = vessel(world, sourcePos.east());
        HeatContainerHandler source = emitter(world, sourcePos);
        source.setHeatStored(10000);
        long start = vessel.getCurrentTemperature();
        for (int tick = 0; tick < 20; tick++) {
            advanceWorldTime(world);
            source.update();
            vessel.update();
        }
        long hot = vessel.getCurrentTemperature();
        LOG.info("CRUCIBLE_WORLD_THERMAL start={} heated={} debited={}", start, hot, 10000 - source.getHeatStored());
        check(10000 - source.getHeatStored() == 2560, "20-tick heat source debit is not 20*128 HU");
        check(hot == start + 8, "Registered osmium + 16 iron thermal calibration is not 8 K/20 ticks");
        for (int tick = 0; tick < 99; tick++) {
            advanceWorldTime(world);
            vessel.update();
        }
        check(vessel.getCurrentTemperature() == hot, "Vessel cooled before GT6's initial 100-tick delay");
        advanceWorldTime(world);
        vessel.update();
        check(vessel.getCurrentTemperature() == hot - 1, "Vessel did not cool at the 100-tick boundary");
        for (int tick = 0; tick < 10; tick++) {
            advanceWorldTime(world);
            vessel.update();
        }
        check(vessel.getCurrentTemperature() == hot - 2, "Vessel did not cool another K after 10 ticks");
        LOG.info("CRUCIBLE_WORLD_CASE registered_thermal_calibration passed");
    }

    private static void advanceWorldTime(World world) {
        world.getWorldInfo().setWorldTotalTime(world.getTotalWorldTime() + 1);
    }

    private static MetaTileEntityCrucible vessel(WorldServer world, BlockPos pos) {
        ItemStack stack = MetaTileEntityHandler.CRUCIBLE_HU[15].getStackForm(); // Registered osmium vessel.
        MachineItemBlock block = (MachineItemBlock) stack.getItem();
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[HeatSmoke]"));
        check(world.isAirBlock(pos), "Heat fixture position occupied");
        check(block.placeBlockAt(stack, player, world, pos, EnumFacing.UP, .5F, .5F, .5F,
                block.getBlock().getDefaultState()), "Heat vessel placement failed");
        MetaTileEntityCrucible vessel = (MetaTileEntityCrucible) GTUtility.getMetaTileEntity(world, pos);
        NBTTagCompound data = new NBTTagCompound();
        data.setLong("Temperature", 293);
        data.setLong("OldTemperature", 293);
        data.setInteger("WaterUnitVersion", 1);
        data.setInteger("FluidQuantityVersion", 1);
        NBTTagCompound iron = new NBTTagCompound();
        iron.setString("Material", "gregtech:iron");
        iron.setLong("Amount", 16 * GTValues.M);
        NBTTagList contents = new NBTTagList();
        contents.appendTag(iron);
        data.setTag("Contents", contents);
        vessel.readFromNBT(data);
        return vessel;
    }

    private static TileEntityHeatConductor pipe(WorldServer world, BlockPos pos) {
        BlockHeatConductor block = MetaBlocks.HEAT_CONDUCTOR.get("gregtech")[HeatConductorType.INSULATED_HEAT_CONDUCTOR_SINGLE.ordinal()];
        Material material = block.getEnabledMaterials().stream().findFirst().orElseThrow(() -> new AssertionError("No heat pipe materials"));
        ItemStack stack = block.getItem(material);
        ItemBlockHeatConductor item = (ItemBlockHeatConductor) stack.getItem();
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[HeatSmoke]"));
        check(world.isAirBlock(pos), "Pipe fixture position occupied");
        check(item.placeBlockAt(stack, player, world, pos, EnumFacing.UP, .5F, .5F, .5F, block.getDefaultState()),
                "Real pipe item placement failed");
        TileEntity tile = world.getTileEntity(pos);
        check(tile instanceof TileEntityHeatConductor, "Placed heat pipe has wrong tile");
        return (TileEntityHeatConductor) tile;
    }

    private static IHeatable heat(MetaTileEntityCrucible vessel, EnumFacing side) {
        IHeatable heat = vessel.getCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, side);
        check(heat != null, "Placed crucible missing native heat capability");
        return heat;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class EmitterOwner extends MetaTileEntity {
        private final World world;
        private final BlockPos pos;
        EmitterOwner(World world, BlockPos pos) { super(new ResourceLocation("gt6addition_parity_smoke", "emitter")); this.world = world; this.pos = pos; }
        @Override public World getWorld() { return world; }
        @Override public BlockPos getPos() { return pos; }
        @Override public TileEntity getNeighbor(EnumFacing side) { return world.getTileEntity(pos.offset(side)); }
        @Override public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tile) { return new EmitterOwner(world, pos); }
    }
}
