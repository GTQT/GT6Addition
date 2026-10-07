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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.common.util.FakePlayer;
import com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        return 7;
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
