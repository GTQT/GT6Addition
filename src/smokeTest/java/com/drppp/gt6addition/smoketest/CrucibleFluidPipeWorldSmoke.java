package com.drppp.gt6addition.smoketest;

import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleTransferLogic;
import com.mojang.authlib.GameProfile;
import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.ItemBlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/** Real registered CEu fluid pipes routing into a placed crucible; test-only. */
final class CrucibleFluidPipeWorldSmoke {
    private static final Logger LOG = LogManager.getLogger("CrucibleParitySmoke");

    private CrucibleFluidPipeWorldSmoke() {}

    static int run(WorldServer world) {
        BlockPos firstPipePos = new BlockPos(2400, 70, 400);
        BlockPos secondPipePos = firstPipePos.east();
        BlockPos vesselPos = secondPipePos.east();
        MetaTileEntityCrucible vessel = placeVessel(world, vesselPos);
        TileEntityFluidPipeTickable firstPipe = placePipe(world, firstPipePos);
        TileEntityFluidPipeTickable secondPipe = placePipe(world, secondPipePos);
        BlockFluidPipe block = (BlockFluidPipe) world.getBlockState(firstPipePos).getBlock();

        // Build the same CEu connections used by GT heat-pipe smoke fixtures,
        // then let the actual block callbacks register both nodes in the net.
        firstPipe.setConnection(EnumFacing.EAST, true, false);
        secondPipe.setConnection(EnumFacing.WEST, true, false);
        secondPipe.setConnection(EnumFacing.EAST, true, false);
        block.updateTick(world, firstPipePos, world.getBlockState(firstPipePos), world.rand);
        block.updateTick(world, secondPipePos, world.getBlockState(secondPipePos), world.rand);
        check(firstPipe.isConnected(EnumFacing.EAST), "Placed fluid pipe segments are not connected");
        check(secondPipe.isConnected(EnumFacing.EAST), "Placed fluid pipe is not connected to the crucible endpoint");
        IFluidHandler inlet = firstPipe.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                EnumFacing.EAST);
        check(inlet != null, "Real fluid pipe did not expose its Forge fluid capability");
        int accepted = inlet.fill(new FluidStack(FluidRegistry.WATER, 1000), true);
        check(accepted == 1000, "Real pipe inlet simulated the wrong water amount: " + accepted);
        accepted = inlet.fill(new FluidStack(FluidRegistry.WATER, 1000), false);
        check(accepted == 1000, "Real pipe inlet accepted the wrong water amount: " + accepted);

        for (int tick = 0; tick < 40 && amount(vessel) < GTValues.M; tick++) {
            world.getWorldInfo().setWorldTotalTime(world.getTotalWorldTime() + 1);
            firstPipe.update();
            secondPipe.update();
        }
        long received = amount(vessel);
        long remainingInNetwork = contained(firstPipe) + contained(secondPipe);
        check(received == GTValues.M && remainingInNetwork == 0,
                "CEu fluid pipe route lost/stranded water: crucible=" + received +
                        " pipe1=" + contained(firstPipe) + " pipe2=" + contained(secondPipe));
        check("gregtech:water".equals(materialName(vessel)),
                "Fluid pipe route changed the crucible material identity: " + materialName(vessel));
        long inputTemperature = CrucibleTransferLogic.fluidInputTemperature(
                FluidRegistry.WATER.getTemperature(new FluidStack(FluidRegistry.WATER, 1000)), 273, 373);
        long resultTemperature = vessel.getCurrentTemperature();
        check(resultTemperature >= 293 && resultTemperature <= inputTemperature,
                "Pipe-delivered water produced an out-of-range mixed temperature: " + resultTemperature +
                        " (ambient=293, GT6 water input=" + inputTemperature + ")");
        LOG.info("CRUCIBLE_WORLD_CASE FLUID_PIPE_TWO_SEGMENT_CRUCIBLE_INPUT passed water={} temperature={}K",
                received, resultTemperature);
        return 1;
    }

    private static MetaTileEntityCrucible placeVessel(WorldServer world, BlockPos pos) {
        ItemStack stack = MetaTileEntityHandler.CRUCIBLE_HU[15].getStackForm();
        MachineItemBlock block = (MachineItemBlock) stack.getItem();
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[FluidPipeSmoke]"));
        check(world.isAirBlock(pos), "Fluid pipe vessel fixture position occupied");
        check(block.placeBlockAt(stack, player, world, pos, EnumFacing.UP, .5F, .5F, .5F,
                block.getBlock().getDefaultState()), "Fluid pipe vessel placement failed");
        MetaTileEntity installed = GTUtility.getMetaTileEntity(world, pos);
        check(installed instanceof MetaTileEntityCrucible, "Fluid pipe endpoint is not a placed crucible");
        MetaTileEntityCrucible vessel = (MetaTileEntityCrucible) installed;
        NBTTagCompound data = new NBTTagCompound();
        data.setLong("Temperature", 293);
        data.setLong("OldTemperature", 293);
        data.setInteger("WaterUnitVersion", 1);
        data.setInteger("FluidQuantityVersion", 3);
        vessel.readFromNBT(data);
        return vessel;
    }

    private static TileEntityFluidPipeTickable placePipe(WorldServer world, BlockPos pos) {
        BlockFluidPipe block = findPipeBlock();
        Material material = block.getEnabledMaterials().stream().findFirst()
                .orElseThrow(() -> new AssertionError("Registered fluid pipe has no enabled material"));
        ItemStack stack = block.getItem(material);
        check(!stack.isEmpty() && stack.getItem() instanceof ItemBlockFluidPipe,
                "Registered fluid pipe item missing for " + material);
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[FluidPipeSmoke]"));
        check(world.isAirBlock(pos), "Fluid pipe fixture position occupied");
        check(((ItemBlockFluidPipe) stack.getItem()).placeBlockAt(stack, player, world, pos,
                        EnumFacing.UP, .5F, .5F, .5F, block.getDefaultState()),
                "Actual fluid pipe item placement failed");
        TileEntity tile = world.getTileEntity(pos);
        check(tile instanceof TileEntityFluidPipeTickable,
                "Placed fluid pipe has unexpected ticking tile: " + (tile == null ? "null" : tile.getClass()));
        return (TileEntityFluidPipeTickable) tile;
    }

    private static BlockFluidPipe findPipeBlock() {
        for (BlockFluidPipe[] blocks : MetaBlocks.FLUID_PIPES.values()) {
            if (blocks == null) continue;
            for (BlockFluidPipe block : blocks) {
                if (block != null && !block.getEnabledMaterials().isEmpty()) return block;
            }
        }
        throw new AssertionError("No registered CEu fluid pipe block/material");
    }

    private static long contained(TileEntityFluidPipeTickable pipe) {
        long total = 0;
        for (FluidStack fluid : pipe.getContainedFluids()) if (fluid != null) total += fluid.amount;
        return total;
    }

    private static long amount(MetaTileEntityCrucible vessel) {
        NBTTagList contents = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
        for (int i = 0; i < contents.tagCount(); i++) {
            NBTTagCompound entry = contents.getCompoundTagAt(i);
            if ("gregtech:water".equals(entry.getString("Material"))) return entry.getLong("Amount");
        }
        return 0;
    }

    private static String materialName(MetaTileEntityCrucible vessel) {
        NBTTagList contents = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
        return contents.tagCount() == 1 ? contents.getCompoundTagAt(0).getString("Material") : "contents=" + contents;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

}
