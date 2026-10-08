package com.drppp.gt6addition.smoketest;

import com.drppp.gt6addition.api.capability.interfaces.ICrucibleEnergyReceiver;
import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.mojang.authlib.GameProfile;
import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.util.UUID;

/** Cross-process Anvil/CEu holder recovery. Test-only, never in the release JAR. */
final class CrucibleRestartWorldSmoke {
    private static final String PHASE = System.getProperty("gt6addition.parity.restartPhase", "");
    private static final String CHECKPOINT = "crucible-restart-checkpoint.dat";
    private static final BlockPos[] POSITIONS = {
            new BlockPos(2400, 70, 2400), new BlockPos(2416, 70, 2400), new BlockPos(2432, 70, 2400)};
    private static boolean prepared;

    static boolean started(WorldServer world, MinecraftServer server) throws Exception {
        if (PHASE.isEmpty()) return false;
        check(PHASE.equals("write") || PHASE.equals("verify"), "Invalid restart phase");
        String targets = System.getProperty("gt6addition.parity.mixinTargets", "");
        check(!targets.isEmpty(), "Missing audited target inventory");
        for (String target : targets.split(",")) {
            Class.forName(target, false, CrucibleRestartWorldSmoke.class.getClassLoader());
        }
        File directory = world.getSaveHandler().getWorldDirectory();
        check(directory.getName().matches("parity-smoke-restart-[0-9a-f-]{36}"), "Not an isolated restart world");
        if (PHASE.equals("write")) {
            check(!new File(directory, CHECKPOINT).exists(), "Checkpoint already exists");
            prepare(world, server);
            prepared = true;
            LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_RESTART_PREPARED fixtures=3 world={}", directory.getName());
        } else {
            verify(world, server, directory);
        }
        return true;
    }

    static void stopping(WorldServer world) throws Exception {
        if (!PHASE.equals("write")) return;
        check(prepared, "Cannot checkpoint a failed setup");
        NBTTagCompound checkpoint = new NBTTagCompound();
        checkpoint.setInteger("Version", 1);
        checkpoint.setString("World", world.getSaveHandler().getWorldDirectory().getName());
        checkpoint.setString("WriterJvm", ManagementFactory.getRuntimeMXBean().getName());
        NBTTagList fixtures = new NBTTagList();
        for (int i = 0; i < POSITIONS.length; i++) {
            MetaTileEntityCrucible vessel = loaded(world, i);
            assertFixture(vessel, i);
            NBTTagCompound expected = new NBTTagCompound();
            expected.setLong("Position", POSITIONS[i].toLong());
            expected.setTag("State", persistentState(vessel));
            expected.setTag("Input", items(vessel).getStackInSlot(0).writeToNBT(new NBTTagCompound()));
            fixtures.appendTag(expected);
            vessel.markDirty();
        }
        checkpoint.setTag("Fixtures", fixtures);
        // Save through the world/chunk pipeline. The checkpoint only supplies
        // assertions; verification never feeds it to readFromNBT/TileEntity.create.
        world.saveAllChunks(true, null);
        try (FileOutputStream output = new FileOutputStream(new File(world.getSaveHandler().getWorldDirectory(), CHECKPOINT))) {
            CompressedStreamTools.writeCompressed(checkpoint, output);
        }
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_RESTART_SAVED fixtures=3 jvm={}", checkpoint.getString("WriterJvm"));
    }

    private static void prepare(WorldServer world, MinecraftServer server) {
        FakePlayer player = player(world, server);
        MetaTileEntityCrucible queued = place(world, player, 0);
        check(fluids(queued).fill(new FluidStack(FluidRegistry.WATER, 16000), true) == 16000, "Full shared capacity");
        offer(queued, player, ingot(Materials.Iron, 1));
        offer(queued, player, ingot(Materials.Gold, 1));
        offer(queued, player, ingot(Materials.Iron, 62));
        check(items(queued).insertItem(0, ingot(Materials.Gold, 3), false).isEmpty(), "Explicit input fixture");
        queued.update();
        assertFixture(queued, 0);

        MetaTileEntityCrucible fraction = place(world, player, 1);
        check(items(fraction).insertItem(0, ingot(Materials.Iron, 15), false).isEmpty(), "Solid fixture input");
        for (int i = 0; i < 15; i++) fraction.update();
        check(fluids(fraction).fill(new FluidStack(FluidRegistry.WATER, 987), true) == 987, "Fractional water fixture");
        check(fraction.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.CU, 1, false) == 1, "Signed heat fixture");
        fraction.update();
        assertFixture(fraction, 1);

        MetaTileEntityCrucible lava = place(world, player, 2);
        check(fluids(lava).fill(new FluidStack(FluidRegistry.LAVA, 1145), true) == 1145, "Lava fixture input");
        check(lava.getCurrentTemperature() < 1300, "Lava intake unexpectedly above condensation boundary");
        lava.update();
        assertFixture(lava, 2);
    }

    private static void verify(WorldServer world, MinecraftServer server, File directory) throws Exception {
        NBTTagCompound checkpoint;
        try (FileInputStream input = new FileInputStream(new File(directory, CHECKPOINT))) {
            checkpoint = CompressedStreamTools.readCompressed(input);
        }
        check(checkpoint.getInteger("Version") == 1 && directory.getName().equals(checkpoint.getString("World")),
                "Checkpoint identity/version mismatch");
        String readerJvm = ManagementFactory.getRuntimeMXBean().getName();
        check(!checkpoint.getString("WriterJvm").isEmpty() && !readerJvm.equals(checkpoint.getString("WriterJvm")),
                "Recovery must use a different server process");
        NBTTagList fixtures = checkpoint.getTagList("Fixtures", 10);
        check(fixtures.tagCount() == POSITIONS.length, "Incomplete checkpoint");
        for (int i = 0; i < POSITIONS.length; i++) {
            NBTTagCompound expected = fixtures.getCompoundTagAt(i);
            check(expected.getLong("Position") == POSITIONS[i].toLong(), "Checkpoint position mismatch");
            MetaTileEntityCrucible vessel = loaded(world, i);
            check(expected.getCompoundTag("State").equals(persistentState(vessel)),
                    "Disk-restored persistent state changed for fixture " + i + ": " + save(vessel));
            ItemStack expectedInput = new ItemStack(expected.getCompoundTag("Input"));
            check(ItemStack.areItemStacksEqual(expectedInput, items(vessel).getStackInSlot(0)), "Explicit input lost on restart");
            assertFixture(vessel, i);
            LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_RESTART_RESTORED fixture={} temperature={} storedHeat={}",
                    i, vessel.getCurrentTemperature(), save(vessel).getLong("StoredHeat"));
        }

        MetaTileEntityCrucible queued = loaded(world, 0);
        FakePlayer player = player(world, server);
        player.inventory.currentItem = 8;
        for (int i = 0; i < 3; i++) {
            check(player.getHeldItemMainhand().isEmpty(), "Takeback hand unexpectedly occupied");
            check(queued.onRightClick(player, EnumHand.MAIN_HAND, EnumFacing.UP, null), "Restored input not recoverable");
        }
        check(items(queued).getStackInSlot(0).isEmpty() && queued.getPendingItemCount() == 64, "Takeback changed FIFO");
        for (int i = 0; i < 3; i++) {
            NBTTagCompound before = save(queued);
            check(fluids(queued).drain(1000, false).amount == 1000 && before.equals(save(queued)), "Restart simulation changed state");
            check(fluids(queued).drain(1000, true).amount == 1000, "Restart FIFO space release");
            queued.update();
            check(amount(queued, Materials.Iron) == (i == 0 ? 1 : i == 1 ? 1 : 2) * (long) GTValues.M &&
                    amount(queued, Materials.Gold) == (i == 0 ? 0 : 1) * (long) GTValues.M &&
                    queued.getPendingItemCount() == 63 - i, "Restart FIFO order/rate changed at " + i);
        }
        MetaTileEntityCrucible fraction = loaded(world, 1);
        NBTTagCompound before = save(fraction);
        check(fluids(fraction).drain(7, false).amount == 7 && before.equals(save(fraction)), "Restart fractional simulation");
        check(fluids(fraction).drain(7, true).amount == 7 && fluids(fraction).drain(2000, false).amount == 980,
                "Restart fractional drain changed quantity");
        check(save(fraction).getLong("StoredHeat") == -1, "Cold energy remainder changed");

        MetaTileEntityCrucible lava = loaded(world, 2);
        // Apply heat through the real receiver/tick, not checkpoint hydration.
        check(lava.receiveCrucibleEnergy(ICrucibleEnergyReceiver.Type.HU, 500000, false) == 500000, "Reheating receiver");
        lava.update();
        check(lava.getCurrentTemperature() >= 1300 && lava.getCurrentTemperature() < 4000, "Reheating boundary fixture");
        lava.update();
        check(amount(lava, Materials.Obsidian) == 0 && fluids(lava).drain(2000, false).amount == 1145,
                "Restart/remelting lost or duplicated lava");
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_RESTART_PASS checks=6 writer={} reader={}",
                checkpoint.getString("WriterJvm"), readerJvm);
    }

    private static void assertFixture(MetaTileEntityCrucible vessel, int index) {
        if (index == 0) {
            check(fluids(vessel).drain(20000, false).amount == 16000 && vessel.getPendingItemCount() == 64, "Full queue fixture changed");
            NBTTagList pending = save(vessel).getTagList("PendingItems", 10);
            check(pending.tagCount() == 3, "FIFO boundaries changed");
            Material[] materials = {Materials.Iron, Materials.Gold, Materials.Iron};
            int[] counts = {1, 1, 62};
            for (int i = 0; i < 3; i++) check(ItemStack.areItemStacksEqual(ingot(materials[i], counts[i]),
                    new ItemStack(pending.getCompoundTagAt(i))), "FIFO entry changed " + i);
            check(ItemStack.areItemStacksEqual(ingot(Materials.Gold, 3), items(vessel).getStackInSlot(0)), "Explicit input fixture changed");
        } else if (index == 1) {
            check(amount(vessel, Materials.Iron) == 15L * GTValues.M && fluids(vessel).drain(2000, false).amount == 987,
                    "Fractional fixture changed");
            check(save(vessel).getLong("StoredHeat") == -1, "Signed thermal remainder changed");
        } else {
            check(amount(vessel, Materials.Obsidian) == GTValues.M, "Condensed bucket changed");
            NBTTagCompound lava = content(vessel, Materials.Lava);
            check(lava.getLong("Amount") * 144L / GTValues.M == 145 && !lava.getBoolean("Molten"), "Lava remainder changed");
        }
    }

    private static NBTTagCompound persistentState(MetaTileEntityCrucible vessel) {
        NBTTagCompound full = save(vessel), selected = new NBTTagCompound();
        for (String key : new String[]{"Temperature", "OldTemperature", "StoredHeat", "WaterUnitVersion",
                "FluidQuantityVersion", "Contents", "PendingItems"}) {
            check(full.hasKey(key), "Missing persisted key " + key);
            selected.setTag(key, full.getTag(key).copy());
        }
        // The current cold target is derived from the current material rules
        // on load, not a historical input identity to be restored from a save.
        NBTTagList contents = selected.getTagList("Contents", 10);
        for (int i = 0; i < contents.tagCount(); i++) contents.getCompoundTagAt(i).removeTag("SolidifyTarget");
        return selected;
    }

    private static MetaTileEntityCrucible place(WorldServer world, FakePlayer player, int index) {
        BlockPos pos = POSITIONS[index];
        check(world.isAirBlock(pos), "Restart fixture already occupied");
        ItemStack stack = MetaTileEntityHandler.CRUCIBLE_HU[15].getStackForm();
        MachineItemBlock item = (MachineItemBlock) stack.getItem();
        check(item.placeBlockAt(stack, player, world, pos, EnumFacing.UP, .5F, .5F, .5F,
                item.getBlock().getDefaultState()), "Restart fixture placement failed");
        return loaded(world, index);
    }

    private static MetaTileEntityCrucible loaded(WorldServer world, int index) {
        world.getChunk(POSITIONS[index]);
        Object loaded = GTUtility.getMetaTileEntity(world, POSITIONS[index]);
        check(loaded instanceof MetaTileEntityCrucible, "Disk-restored registered holder missing at " + POSITIONS[index]);
        return (MetaTileEntityCrucible) loaded;
    }

    private static FakePlayer player(WorldServer world, MinecraftServer server) {
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[RestartSmoke]"));
        new NetHandlerPlayServer(server, new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
            @Override public void sendPacket(Packet<?> packet) {}
        };
        return player;
    }

    private static void offer(MetaTileEntityCrucible vessel, FakePlayer player, ItemStack stack) {
        player.setHeldItem(EnumHand.MAIN_HAND, stack);
        while (!stack.isEmpty()) {
            int before = stack.getCount();
            check(vessel.onRightClick(player, EnumHand.MAIN_HAND, EnumFacing.NORTH, null) &&
                    stack.getCount() == before - 1, "FIFO setup rejected");
        }
    }

    private static ItemStack ingot(Material material, int count) {
        ItemStack stack = OreDictUnifier.get(OrePrefix.ingot, material, count);
        check(!stack.isEmpty(), "Missing existing host ingot " + material);
        return stack;
    }

    private static IFluidHandler fluids(MetaTileEntityCrucible vessel) {
        IFluidHandler handler = vessel.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.NORTH);
        check(handler != null, "Missing real fluid capability");
        return handler;
    }

    private static IItemHandler items(MetaTileEntityCrucible vessel) {
        IItemHandler handler = vessel.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        check(handler != null, "Missing real item capability");
        return handler;
    }

    private static NBTTagCompound save(MetaTileEntityCrucible vessel) { return vessel.writeToNBT(new NBTTagCompound()); }

    private static NBTTagCompound content(MetaTileEntityCrucible vessel, Material material) {
        NBTTagList contents = save(vessel).getTagList("Contents", 10);
        for (int i = 0; i < contents.tagCount(); i++) {
            NBTTagCompound entry = contents.getCompoundTagAt(i);
            if (material.getRegistryName().equals(entry.getString("Material"))) return entry;
        }
        return new NBTTagCompound();
    }

    private static long amount(MetaTileEntityCrucible vessel, Material material) { return content(vessel, material).getLong("Amount"); }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
