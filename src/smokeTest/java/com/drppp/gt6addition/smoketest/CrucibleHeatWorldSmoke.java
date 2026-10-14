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
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.heat.BlockHeatConductor;
import gregtech.common.pipelike.heat.HeatConductorType;
import gregtech.common.pipelike.heat.ItemBlockHeatConductor;
import gregtech.common.pipelike.heat.tile.TileEntityHeatConductor;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.common.util.FakePlayer;
import com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.UUID;

/** Real registered pipe blocks and routes; test-only emitter owner, never shipped. */
final class CrucibleHeatWorldSmoke {
    private static final Logger LOG = LogManager.getLogger("CrucibleParitySmoke");

    static int run(WorldServer world) {
        directSource(world);
        routedSource(world, false);
        routedSource(world, true);
        calibratedHeatingAndCooling(world);
        registeredPolymerCycle(world, Materials.Polytetrafluoroethylene, new BlockPos(728, 70, 400));
        registeredPolymerCycle(world, Materials.PolyvinylChloride, new BlockPos(760, 70, 400));
        nativeAliasHeating(world);
        routedSourceAcrossMultipleSegments(world);
        multipleSourcesAcrossVesselSides(world);
        sixSidedSources(world);
        nonInsulatedPipeLoss(world);
        externalHeatablePull(world);
        cyclicPipeNetwork(world);
        return 13;
    }

    private static void registeredPolymerCycle(WorldServer world, Material material, BlockPos pos) {
        MetaTileEntityCrucible vessel = vessel(world, pos);
        seedMaterial(vessel, material, 3 * GTValues.M, 422, 421, false, 0);
        advanceWorldTime(world);
        vessel.update();
        check(content(vessel).getLong("Amount") == 3 * GTValues.M && !content(vessel).getBoolean("Molten"),
                material + " melted before 423 K");
        seedMaterial(vessel, material, 3 * GTValues.M, 423, 422, false, 0);
        advanceWorldTime(world);
        vessel.update();
        NBTTagCompound molten = content(vessel);
        check(molten.getLong("Amount") == 2 * GTValues.M && molten.getBoolean("Molten") &&
                        material.getRegistryName().equals(molten.getString("Material")),
                material + " did not keep its self target and two-thirds GT6 yield");
        IFluidHandler fluids = vessel.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.NORTH);
        check(fluids != null, "Registered polymer vessel has no fluid interface");
        FluidStack registeredFluid = material.getFluid(GTValues.L);
        check(registeredFluid != null, "Registered polymer has no liquid");
        int fluidTemperature = registeredFluid.getFluid().getTemperature(registeredFluid);
        long extractionTemperature = Math.max(423, fluidTemperature < 320 ? 423 : fluidTemperature);
        check(extractionTemperature < 846, "Polymer liquid is registered above GT6's boiling point");
        NBTTagCompound before = vessel.writeToNBT(new NBTTagCompound());
        FluidStack simulated = fluids.drain(GTValues.L, false);
        // GT6's container extraction has a second bound (Smeltery:454-455).
        // A molten material need not yet be hot enough for its registered
        // Forge liquid: CEu PTFE uses 600 K, whereas GT6's melt point is 423 K.
        if (extractionTemperature > 423) {
            check(simulated == null && fluids.getTankProperties()[0].getContents() == null,
                    material + " ignored the liquid-temperature extraction bound");
        } else {
            check(simulated != null && simulated.amount == GTValues.L && simulated.isFluidEqual(registeredFluid),
                    material + " molten projection used the wrong registered fluid");
        }
        check(before.equals(vessel.writeToNBT(new NBTTagCompound())), "Polymer drain simulation changed inventory");
        // Re-read the actual installed machine's serialized data, not a
        // replacement fake Material. Disk/restart coverage is a separate gate.
        vessel.readFromNBT(before);
        advanceWorldTime(world);
        vessel.update();
        check(molten.equals(content(vessel)), "Polymer NBT reload repeated the two-thirds yield");
        if (extractionTemperature > 423) {
            setTemperature(vessel, extractionTemperature - 1, 423);
            advanceWorldTime(world);
            vessel.update();
            NBTTagCompound below = vessel.writeToNBT(new NBTTagCompound());
            check(fluids.drain(GTValues.L, false) == null && fluids.drain(GTValues.L, true) == null &&
                            fluids.getTankProperties()[0].getContents() == null &&
                            below.equals(vessel.writeToNBT(new NBTTagCompound())),
                    material + " extracted below its liquid-temperature boundary");
        }
        // Preserve the preceding real temperature. PVC can already drain at
        // 423 K; fabricating OldTemperature=422 would falsely create a second
        // melting-boundary crossing and legitimately apply the yield again.
        setTemperature(vessel, extractionTemperature, vessel.getCurrentTemperature());
        advanceWorldTime(world);
        vessel.update();
        check(molten.equals(content(vessel)), "Heating a molten polymer repeated its two-thirds yield");
        NBTTagCompound drainable = vessel.writeToNBT(new NBTTagCompound());
        simulated = fluids.drain(GTValues.L, false);
        FluidStack projection = fluids.getTankProperties()[0].getContents();
        check(simulated != null && simulated.amount == GTValues.L && simulated.isFluidEqual(registeredFluid) &&
                        projection != null && projection.amount == 2 * GTValues.L && projection.isFluidEqual(registeredFluid),
                material + " did not expose its actual liquid at the extraction boundary");
        check(fluids.drain(Materials.Water.getFluid(GTValues.L), false) == null &&
                        drainable.equals(vessel.writeToNBT(new NBTTagCompound())),
                "Polymer simulated/typed drain changed inventory");
        FluidStack extracted = fluids.drain(registeredFluid, true);
        check(extracted != null && extracted.amount == GTValues.L && extracted.isFluidEqual(registeredFluid) &&
                        content(vessel).getLong("Amount") == GTValues.M && content(vessel).getBoolean("Molten"),
                material + " actual drain lost or duplicated material");
        NBTTagCompound remainder = vessel.writeToNBT(new NBTTagCompound());
        vessel.readFromNBT(remainder);
        advanceWorldTime(world);
        vessel.update();
        check(content(vessel).getLong("Amount") == GTValues.M && content(vessel).getBoolean("Molten"),
                "Drained polymer NBT reload changed the remaining quantity");
        setTemperature(vessel, 422, extractionTemperature);
        advanceWorldTime(world);
        vessel.update();
        NBTTagCompound solid = content(vessel);
        check(solid.getLong("Amount") == GTValues.M && !solid.getBoolean("Molten") &&
                        material.getRegistryName().equals(solid.getString("Material")),
                material + " cooled to a host fallback or restored the lost third");
        check(fluids.drain(GTValues.L, false) == null, "Solid polymer was exposed as drainable fluid");
        LOG.info("CRUCIBLE_WORLD_CASE registered_polymer_cycle {} fluid={} liquidTemperature={} extractionTemperature={} passed",
                material.getRegistryName(), registeredFluid.getFluid().getName(), fluidTemperature, extractionTemperature);
    }

    private static void nativeAliasHeating(WorldServer world) {
        Material[] materials = {Materials.Chrome, Materials.Polytetrafluoroethylene, Materials.PolyvinylChloride};
        for (int index = 0; index < materials.length; index++) {
            Material material = materials[index];
            double density = index == 0 ? 7150 : (2267D + 2 * .08988D) / 3;
            MetaTileEntityCrucible vessel = vessel(world, new BlockPos(792 + 32 * index, 70, 400));
            seedMaterial(vessel, material, 16 * GTValues.M, 300, 300, false, 2560);
            advanceWorldTime(world);
            vessel.update();
            long cost = 1 + (long) (((7 * 22610 + 16 * density) / 9) / 100);
            check(vessel.getCurrentTemperature() == 300 + 2560 / cost,
                    material + " used host components/fluid density instead of GT6 density");
            check(vessel.writeToNBT(new NBTTagCompound()).getLong("StoredHeat") == 2560 % cost,
                    material + " did not retain unused heat");
            check(content(vessel).getLong("Amount") == 16 * GTValues.M &&
                            material.getRegistryName().equals(content(vessel).getString("Material")),
                    material + " was lost to conflicting host targets or hazard flags");
        }
        LOG.info("CRUCIBLE_WORLD_CASE registered_alias_heat_density passed");
    }

    private static NBTTagCompound content(MetaTileEntityCrucible vessel) {
        NBTTagList contents = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
        check(contents.tagCount() == 1, "Expected exactly one material in native alias fixture");
        return contents.getCompoundTagAt(0);
    }

    private static void setTemperature(MetaTileEntityCrucible vessel, long temperature, long oldTemperature) {
        NBTTagCompound data = vessel.writeToNBT(new NBTTagCompound());
        data.setLong("Temperature", temperature);
        data.setLong("OldTemperature", oldTemperature);
        vessel.readFromNBT(data);
    }

    private static void seedMaterial(MetaTileEntityCrucible vessel, Material material, long amount,
                                     long temperature, long oldTemperature, boolean molten, long storedHeat) {
        NBTTagCompound data = vessel.writeToNBT(new NBTTagCompound());
        data.setLong("Temperature", temperature);
        data.setLong("OldTemperature", oldTemperature);
        data.setLong("StoredHeat", storedHeat);
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("Material", material.getRegistryName());
        entry.setLong("Amount", amount);
        entry.setBoolean("Molten", molten);
        NBTTagList contents = new NBTTagList();
        contents.appendTag(entry);
        data.setTag("Contents", contents);
        vessel.readFromNBT(data);
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

    private static void routedSourceAcrossMultipleSegments(WorldServer world) {
        BlockPos sourcePos = new BlockPos(888, 70, 400);
        int segmentCount = 3;
        MetaTileEntityCrucible vessel = vessel(world, sourcePos.add(segmentCount + 1, 0, 0));
        TileEntityHeatConductor[] pipes = new TileEntityHeatConductor[segmentCount];
        BlockHeatConductor block = null;
        for (int i = 0; i < segmentCount; i++) {
            BlockPos pipePos = sourcePos.add(i + 1, 0, 0);
            pipes[i] = pipe(world, pipePos);
            pipes[i].setConnection(EnumFacing.WEST, true, false);
            pipes[i].setConnection(EnumFacing.EAST, true, false);
            block = (BlockHeatConductor) world.getBlockState(pipePos).getBlock();
            block.updateTick(world, pipePos, world.getBlockState(pipePos), world.rand);
        }

        check(pipes[0].isConnected(EnumFacing.WEST) && pipes[0].isConnected(EnumFacing.EAST) &&
                        pipes[1].isConnected(EnumFacing.WEST) && pipes[1].isConnected(EnumFacing.EAST) &&
                        pipes[2].isConnected(EnumFacing.WEST) && pipes[2].isConnected(EnumFacing.EAST),
                "Three-segment real heat pipe route is not fully connected");
        IHeatable inlet = pipes[0].getCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER,
                EnumFacing.WEST);
        check(inlet != null, "First segment did not expose its connected heat-network input");

        HeatContainerHandler source = emitter(world, sourcePos);
        source.setHeatStored(1000);
        source.update();
        long delivered = heat(vessel, EnumFacing.WEST).getHeatStored();
        long debited = 1000 - source.getHeatStored();
        LOG.info("CRUCIBLE_WORLD_HEAT segments={} delivered={} debited={} lossPerBlock={}", segmentCount,
                delivered, debited, pipes[0].getNodeData().getHeatLossPerBlock());
        check(delivered == 128 && debited == delivered,
                "Three-segment real heat route duplicated, lost, or created insulated HU: delivered=" + delivered +
                        " debited=" + debited);
        check(vessel.getCurrentTemperature() == 293,
                "Three-segment heat transfer overwrote the vessel's temperature");
        LOG.info("CRUCIBLE_WORLD_CASE native_pipe_three_segment passed");
    }

    private static void multipleSourcesAcrossVesselSides(WorldServer world) {
        BlockPos vesselPos = new BlockPos(920, 70, 400);
        BlockPos westPipePos = vesselPos.west();
        BlockPos southPipePos = vesselPos.south();
        TileEntityHeatConductor westPipe = pipe(world, westPipePos);
        TileEntityHeatConductor southPipe = pipe(world, southPipePos);
        westPipe.setConnection(EnumFacing.WEST, true, false);
        westPipe.setConnection(EnumFacing.EAST, true, false);
        southPipe.setConnection(EnumFacing.NORTH, true, false);
        southPipe.setConnection(EnumFacing.SOUTH, true, false);
        BlockHeatConductor block = (BlockHeatConductor) world.getBlockState(westPipePos).getBlock();
        block.updateTick(world, westPipePos, world.getBlockState(westPipePos), world.rand);
        block.updateTick(world, southPipePos, world.getBlockState(southPipePos), world.rand);
        MetaTileEntityCrucible vessel = vessel(world, vesselPos);

        HeatContainerHandler westSource = emitter(world, westPipePos.west(), EnumFacing.EAST);
        HeatContainerHandler southSource = emitter(world, southPipePos.south(), EnumFacing.NORTH);
        westSource.setHeatStored(1000);
        southSource.setHeatStored(1000);
        check(westPipe.getCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, EnumFacing.WEST) != null &&
                        southPipe.getCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER,
                                EnumFacing.SOUTH) != null,
                "Separate connected heat sources did not expose their pipe inlets");

        westSource.update();
        check(storedHeat(vessel) == 128 && 1000 - westSource.getHeatStored() == 128 &&
                        southSource.getHeatStored() == 1000,
                "West-side heat source did not debit exactly its accepted HU");
        southSource.update();
        check(storedHeat(vessel) == 256 && 1000 - southSource.getHeatStored() == 128 &&
                        2000 - westSource.getHeatStored() - southSource.getHeatStored() == storedHeat(vessel),
                "Independent side sources duplicated, lost, or created HU in the shared vessel buffer");
        check(vessel.getCurrentTemperature() == 293,
                "Multiple heat-source transfers changed temperature before thermal processing");
        LOG.info("CRUCIBLE_WORLD_CASE native_pipe_multiple_sources passed sources=2 perSource=128 stored=256");
    }

    private static void sixSidedSources(WorldServer world) {
        // Keep this radial fixture outside the input smoke test's x=800+16*i
        // row, which deliberately places later vessels at x=960.
        BlockPos vesselPos = new BlockPos(960, 70, 2000);
        MetaTileEntityCrucible vessel = vessel(world, vesselPos);
        EnumFacing[] sides = EnumFacing.values();
        TileEntityHeatConductor[] pipes = new TileEntityHeatConductor[sides.length];
        HeatContainerHandler[] sources = new HeatContainerHandler[sides.length];
        for (int i = 0; i < sides.length; i++) {
            EnumFacing side = sides[i];
            BlockPos pipePos = vesselPos.offset(side);
            pipes[i] = pipe(world, pipePos);
            pipes[i].setConnection(side, true, false);
            pipes[i].setConnection(side.getOpposite(), true, false);
            sources[i] = emitter(world, pipePos.offset(side), side.getOpposite());
            sources[i].setHeatStored(1000);
            BlockHeatConductor block = (BlockHeatConductor) world.getBlockState(pipePos).getBlock();
            block.updateTick(world, pipePos, world.getBlockState(pipePos), world.rand);
        }

        for (int i = 0; i < sides.length; i++) {
            EnumFacing side = sides[i];
            check(pipes[i].isConnected(side) && pipes[i].isConnected(side.getOpposite()) &&
                            pipes[i].getCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, side) != null,
                    "Real heat inlet was not connected on crucible side " + side);
        }
        for (int i = 0; i < sides.length; i++) {
            sources[i].update();
            long expected = 128L * (i + 1);
            long debited = 6_000L - totalStoredHeat(sources);
            check(storedHeat(vessel) == expected && debited == expected,
                    "Six-sided heat transfer did not conserve accepted HU after side " + sides[i] +
                            ": vessel=" + storedHeat(vessel) + " sources=" + debited);
        }
        check(vessel.getCurrentTemperature() == 293,
                "Six-sided heat transfer changed temperature before thermal processing");
        advanceWorldTime(world);
        vessel.update();
        check(vessel.getCurrentTemperature() == 295 && storedHeat(vessel) == 136,
                "Six independent heat sources did not convert conserved 768 HU at the 316 HU/K melt capacity");
        LOG.info("CRUCIBLE_WORLD_CASE native_pipe_all_six_sides passed sides=6 perSource=128 stored=768 thermalized=632 temperatureDelta=2 remainder=136");
    }

    private static void nonInsulatedPipeLoss(WorldServer world) {
        BlockPos sourcePos = new BlockPos(1040, 70, 2000);
        HeatConductorType type = HeatConductorType.HEAT_CONDUCTOR_SINGLE;
        BlockHeatConductor block = MetaBlocks.HEAT_CONDUCTOR.get("gregtech")[type.ordinal()];
        Material material = block.getEnabledMaterials().stream()
                .filter(candidate -> candidate.hasProperty(PropertyKey.HEAT_CONDUCTOR))
                .filter(candidate -> candidate.getProperty(PropertyKey.HEAT_CONDUCTOR).getMaxTemperature() >= 293)
                .filter(candidate -> {
                    float loss = candidate.getProperty(PropertyKey.HEAT_CONDUCTOR).getHeatLossPerBlock() * type.lossFactor;
                    long lossPerPipe = (long) (4096L * loss);
                    return lossPerPipe * 3 < 4096;
                })
                .max(Comparator.comparingDouble(candidate ->
                        candidate.getProperty(PropertyKey.HEAT_CONDUCTOR).getHeatLossPerBlock()))
                .orElseThrow(() -> new AssertionError("No registered single heat pipe material supports the test route"));

        TileEntityHeatConductor[] pipes = new TileEntityHeatConductor[3];
        for (int i = 0; i < pipes.length; i++) {
            BlockPos pipePos = sourcePos.add(i + 1, 0, 0);
            pipes[i] = pipe(world, pipePos, type, material);
            pipes[i].setConnection(EnumFacing.WEST, true, false);
            pipes[i].setConnection(EnumFacing.EAST, true, false);
            block.updateTick(world, pipePos, world.getBlockState(pipePos), world.rand);
        }
        MetaTileEntityCrucible vessel = vessel(world, sourcePos.add(pipes.length + 1, 0, 0));
        HeatContainerHandler source = HeatContainerHandler.emitterContainer(
                new EmitterOwner(world, sourcePos), 10000, 10000, 4096);
        source.setSideOutputCondition(side -> side == EnumFacing.EAST);
        source.setTemperature(293);
        source.setHeatStored(10000);

        long expectedLoss = 0L;
        for (TileEntityHeatConductor pipe : pipes) {
            expectedLoss += (long) (4096L * pipe.getNodeData().getHeatLossPerBlock());
        }
        check(expectedLoss >= 0 && expectedLoss < 4096,
                "Fixture route loss exceeds its transfer budget: " + expectedLoss);
        check(pipes[0].getCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, EnumFacing.WEST) != null,
                "Non-insulated real heat pipe did not expose its source inlet");

        source.update();
        long delivered = heat(vessel, EnumFacing.WEST).getHeatStored();
        long debited = 10000 - source.getHeatStored();
        check(delivered == 4096 - expectedLoss && debited == delivered,
                "CEu non-insulated route loss differs from its registered per-block properties: expected=" +
                        (4096 - expectedLoss) + " delivered=" + delivered + " debited=" + debited);
        check(delivered <= 4096 && delivered <= debited,
                "Non-insulated heat route created HU or exceeded its source budget");
        LOG.info("CRUCIBLE_WORLD_CASE native_pipe_non_insulated_loss passed material={} segments={} lossPerBlock={} expectedLoss={} delivered={} debited={}",
                material.getRegistryName(), pipes.length, pipes[0].getNodeData().getHeatLossPerBlock(),
                expectedLoss, delivered, debited);
    }

    private static void externalHeatablePull(WorldServer world) {
        BlockPos sourcePos = new BlockPos(1100, 70, 2010);
        MetaTileEntityCrucible vessel = vessel(world, sourcePos.east());
        PullHeatTile sourceTile = new PullHeatTile();
        check(world.isAirBlock(sourcePos), "Generic heat source fixture position occupied");
        world.setBlockState(sourcePos, Blocks.FURNACE.getDefaultState());
        world.setTileEntity(sourcePos, sourceTile);
        check(world.getTileEntity(sourcePos) == sourceTile,
                "Generic heat source was not attached to its tile-entity block");
        check(sourcePos.east().equals(vessel.getPos()), "Generic heat source is not adjacent to its crucible");
        check(sourceTile.hasCapability(GregtechCapabilities.CAPABILITY_HEAT_CONTAINER, EnumFacing.EAST),
                "Generic test heat source did not expose the registered heat capability");

        sourceTile.source.stored = 128;
        vessel.update();
        check(sourceTile.source.stored == 0 && storedHeat(vessel) == 128 &&
                        vessel.getCurrentTemperature() == 293,
                "Crucible did not simulate, debit, and commit one external 128 HU offer exactly once");

        sourceTile.source.stored = 128;
        advanceWorldTime(world);
        vessel.update();
        check(sourceTile.source.stored == 0 && storedHeat(vessel) == 256 &&
                        vessel.getCurrentTemperature() == 293,
                "Repeated external IHeatable pulls duplicated or lost heat");
        world.setBlockToAir(sourcePos);
        check(world.getTileEntity(sourcePos) == null, "Generic heat-source fixture was not removed after use");
        LOG.info("CRUCIBLE_WORLD_CASE external_heatable_pull passed first=128 second=128 stored=256 simulationAndDebit=true");
    }

    private static void cyclicPipeNetwork(WorldServer world) {
        BlockPos sourcePos = new BlockPos(1200, 70, 3000);
        BlockPos vesselPos = sourcePos.add(4, 0, 0);
        MetaTileEntityCrucible vessel = vessel(world, vesselPos);
        BlockPos[] positions = {
                sourcePos.east(), sourcePos.add(2, 0, 0), sourcePos.add(3, 0, 0),
                sourcePos.add(3, 0, 1), sourcePos.add(3, 0, 2), sourcePos.add(2, 0, 2),
                sourcePos.add(1, 0, 2), sourcePos.add(1, 0, 1)
        };
        EnumFacing[][] connections = {
                {EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH},
                {EnumFacing.WEST, EnumFacing.EAST},
                {EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH},
                {EnumFacing.NORTH, EnumFacing.SOUTH},
                {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST},
                {EnumFacing.EAST, EnumFacing.WEST},
                {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH},
                {EnumFacing.SOUTH, EnumFacing.NORTH}
        };
        TileEntityHeatConductor[] pipes = new TileEntityHeatConductor[positions.length];
        for (int i = 0; i < positions.length; i++) {
            pipes[i] = pipe(world, positions[i]);
            for (EnumFacing side : connections[i]) {
                pipes[i].setConnection(side, true, false);
            }
        }
        for (int i = 0; i < positions.length; i++) {
            BlockHeatConductor block = (BlockHeatConductor) world.getBlockState(positions[i]).getBlock();
            block.updateTick(world, positions[i], world.getBlockState(positions[i]), world.rand);
        }
        for (int i = 0; i < positions.length; i++) {
            for (EnumFacing side : connections[i]) {
                check(pipes[i].isConnected(side), "Cycle route lost a configured pipe edge at " + positions[i] + " " + side);
            }
        }

        HeatContainerHandler source = emitter(world, sourcePos);
        source.setHeatStored(1000);
        source.update();
        long firstDelivered = storedHeat(vessel);
        long firstDebited = 1000 - source.getHeatStored();
        check(firstDelivered == 128 && firstDebited == 128,
                "Cyclic real CEu heat network duplicated or lost its first source transfer: received=" +
                        firstDelivered + " debited=" + firstDebited);
        source.update();
        long secondDelivered = storedHeat(vessel);
        long totalDebited = 1000 - source.getHeatStored();
        check(secondDelivered == 256 && totalDebited == 256,
                "Cyclic real CEu heat network duplicated or lost its repeated transfer: received=" +
                        secondDelivered + " debited=" + totalDebited);
        LOG.info("CRUCIBLE_WORLD_CASE native_pipe_cycle passed segments={} first=128 total=256 debited={}",
                pipes.length, totalDebited);
    }

    private static HeatContainerHandler emitter(WorldServer world, BlockPos pos) {
        return emitter(world, pos, EnumFacing.EAST);
    }

    private static HeatContainerHandler emitter(WorldServer world, BlockPos pos, EnumFacing outputSide) {
        HeatContainerHandler source = HeatContainerHandler.emitterContainer(new EmitterOwner(world, pos), 10000, 10000, 128);
        source.setSideOutputCondition(side -> side == outputSide);
        source.setTemperature(1000);
        return source;
    }

    private static long storedHeat(MetaTileEntityCrucible vessel) {
        return vessel.writeToNBT(new NBTTagCompound()).getLong("StoredHeat");
    }

    private static long totalStoredHeat(HeatContainerHandler[] sources) {
        long total = 0L;
        for (HeatContainerHandler source : sources) total += source.getHeatStored();
        return total;
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
        HeatConductorType type = HeatConductorType.INSULATED_HEAT_CONDUCTOR_SINGLE;
        BlockHeatConductor block = MetaBlocks.HEAT_CONDUCTOR.get("gregtech")[type.ordinal()];
        Material material = block.getEnabledMaterials().stream().findFirst().orElseThrow(() -> new AssertionError("No heat pipe materials"));
        return pipe(world, pos, type, material);
    }

    private static TileEntityHeatConductor pipe(WorldServer world, BlockPos pos, HeatConductorType type, Material material) {
        BlockHeatConductor block = MetaBlocks.HEAT_CONDUCTOR.get("gregtech")[type.ordinal()];
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

    private static final class PullHeatTile extends TileEntity {
        private final PullHeatSource source = new PullHeatSource();

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == GregtechCapabilities.CAPABILITY_HEAT_CONTAINER || super.hasCapability(capability, facing);
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (capability == GregtechCapabilities.CAPABILITY_HEAT_CONTAINER) {
                return GregtechCapabilities.CAPABILITY_HEAT_CONTAINER.cast(source);
            }
            return super.getCapability(capability, facing);
        }
    }

    /** Test-only non-HeatContainerHandler implementation for the MTE pull fallback. */
    private static final class PullHeatSource implements IHeatable {
        private long stored;

        @Override public long changeHeat(long delta) {
            long previous = stored;
            if (delta < 0L) stored = Math.max(0L, stored + delta);
            else stored = Math.min(Long.MAX_VALUE, stored + delta);
            return stored - previous;
        }
        @Override public boolean canOutputHeat() { return stored > 0L; }
        @Override public long getHeatStored() { return stored; }
        @Override public long transferHeat(long amount, int temperature) { return 0L; }
        @Override public int getTemperature() { return 1000; }
        @Override public void setTemperature(int temperature) {}
        @Override public int getMaxTemperature() { return 1000; }
        @Override public void setMaxTemperature(int temperature) {}
        @Override public long getHeatCapacity() { return Long.MAX_VALUE; }
        @Override public boolean canAcceptHeat() { return false; }
    }
}
