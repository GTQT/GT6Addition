package com.drppp.gt6addition.smoketest;

import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.mojang.authlib.GameProfile;
import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.items.ToolItems;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;

/** Opt-in development mod. This source set is never included in the release JAR. */
@Mod(modid = "gt6addition_parity_smoke", name = "Crucible parity smoke checks", version = "1",
        dependencies = "required-after:gt6addition")
public final class CrucibleWorldSmoke {
    private static final Logger LOG = LogManager.getLogger("CrucibleParitySmoke");
    private int nextPosition;

    @Mod.EventHandler
    public void started(FMLServerStartedEvent event) {
        try {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            WorldServer world = server.getWorld(0);
            check(world != null && !world.isRemote, "Missing real server world");
            // Full host material registration must produce a usable Wheat dust.
            check(!OreDictUnifier.get(OrePrefix.dust, Materials.Wheat).isEmpty(), "Host Wheat dust is missing");
            removed(world, server, false, 1299);
            removed(world, server, false, 1300);
            removed(world, server, true, 1300);
            cancelledCreativeRemoval(world, server);
            unprocessedDrops(world, server);
            creativeToolCancellation(world, server);
            CrucibleHeatWorldSmoke.run(world);
            int inputCases = CrucibleInputWorldSmoke.run(world, server);
            int moldCases = CrucibleMoldWorldSmoke.run(world, server);
            int storageCases = CrucibleStorageWorldSmoke.run(world, server);
            LOG.info("CRUCIBLE_WORLD_PASS {}", 10 + inputCases + moldCases + storageCases);
        } catch (Throwable failure) {
            // A server can exit zero after a startup event failure. The Gradle
            // verifier requires the success marker and rejects this marker.
            LOG.error("CRUCIBLE_WORLD_FAIL", failure);
        }
    }

    private void removed(WorldServer world, MinecraftServer server, boolean creative, long temperature) {
        BlockPos pos = position();
        FakePlayer player = player(world, server, creative);
        MetaTileEntityCrucible vessel = place(world, pos, player);
        vessel.readFromNBT(contents(temperature));
        boolean removed = player.interactionManager.tryHarvestBlock(pos);
        check(removed, "Player removal returned false at " + temperature + ", creative=" +
                player.capabilities.isCreativeMode + ", remaining=" + world.getBlockState(pos) +
                ", machine=" + GTUtility.getMetaTileEntity(world, pos));
        IBlockState state = world.getBlockState(pos);
        if (!creative && temperature >= 1300) {
            check(state.getBlock() == Blocks.FLOWING_LAVA, "Hot survival removal did not leave flowing lava");
            check(state.getValue(BlockLiquid.LEVEL) == 1, "Hot removal produced a lava source");
        } else {
            check(state.getBlock() == Blocks.AIR, "Cool/creative removal did not leave air");
        }
        check(world.getTileEntity(pos) == null, "Removed machine still has a tile entity");
        check(vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10).tagCount() == 0,
                "Processed materials survived removal");
        ItemStack machine = MetaTileEntityHandler.CRUCIBLE_HU[0].getStackForm();
        int machineDrops = countDrops(world, pos, machine);
        check(machineDrops == (!creative && temperature < 1300 ? 1 : 0),
                "Unexpected machine drop count: " + machineDrops);
        LOG.info("CRUCIBLE_WORLD_CASE removal creative={} temperature={} passed", creative, temperature);
    }

    private void creativeToolCancellation(WorldServer world, MinecraftServer server) {
        BlockPos pos = position();
        FakePlayer player = player(world, server, true);
        player.setHeldItem(EnumHand.MAIN_HAND, ToolItems.WRENCH.get(Materials.Iron));
        MetaTileEntityCrucible vessel = place(world, pos, player);
        vessel.readFromNBT(contents(1300));
        NBTTagCompound before = vessel.writeToNBT(new NBTTagCompound());
        check(player.getHeldItemMainhand().getItem() instanceof team.chisel.api.IChiselItem,
                "Loaded CEu wrench no longer implements Chisel's interface");
        CreativeBreakObservation observation = new CreativeBreakObservation(pos);
        MinecraftForge.EVENT_BUS.register(observation);
        boolean removed;
        try { removed = player.interactionManager.tryHarvestBlock(pos); }
        finally { MinecraftForge.EVENT_BUS.unregister(observation); }
        LOG.info("CRUCIBLE_WORLD_CREATIVE_TOOL removed={} state={} installed={} creative={} tool={}",
                removed, world.getBlockState(pos), GTUtility.getMetaTileEntity(world, pos),
                player.capabilities.isCreativeMode, player.getHeldItemMainhand());
        LOG.info("CRUCIBLE_WORLD_CREATIVE_TOOL eventCancelled={} canDestroy={}", observation.cancelled,
                player.getHeldItemMainhand().getItem().canDestroyBlockInCreative(world, pos, player.getHeldItemMainhand(), player));
        // ChiselController.onBlockBreak cancels creative IChiselItem breaking;
        // CEu's tool item implements that interface even for the wrench.
        // This hook must respect the cancellation instead of forcing removal.
        check(observation.cancelled && !removed, "Host creative wrench cancellation changed");
        check(GTUtility.getMetaTileEntity(world, pos) == vessel && before.equals(vessel.writeToNBT(new NBTTagCompound())),
                "Cancelled creative tool break changed the crucible");
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        check(player.interactionManager.tryHarvestBlock(pos), "Creative empty-hand removal after tool cancellation failed");
        check(world.isAirBlock(pos), "Creative removal after tool cancellation left lava/machine");
        check(countDrops(world, pos, MetaTileEntityHandler.CRUCIBLE_HU[0].getStackForm()) == 0,
                "Creative removal after tool cancellation dropped the machine");
        LOG.info("CRUCIBLE_WORLD_CASE creative_tool_cancellation passed");
    }

    private void cancelledCreativeRemoval(WorldServer world, MinecraftServer server) {
        BlockPos pos = position();
        FakePlayer player = player(world, server, true);
        MetaTileEntityCrucible vessel = place(world, pos, player);
        vessel.readFromNBT(contents(1300));
        NBTTagCompound before = vessel.writeToNBT(new NBTTagCompound());
        BreakCancellation cancellation = new BreakCancellation(pos);
        MinecraftForge.EVENT_BUS.register(cancellation);
        try {
            check(!player.interactionManager.tryHarvestBlock(pos), "Cancelled break succeeded");
            check(GTUtility.getMetaTileEntity(world, pos) == vessel, "Cancelled break removed the machine");
            check(before.equals(vessel.writeToNBT(new NBTTagCompound())), "Cancelled break modified contents");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancellation);
        }
        player.interactionManager.setGameType(GameType.SURVIVAL);
        player.setHeldItem(EnumHand.MAIN_HAND, ToolItems.WRENCH.get(Materials.Iron));
        check(player.interactionManager.tryHarvestBlock(pos), "Survival removal after cancellation failed");
        check(world.getBlockState(pos).getBlock() == Blocks.FLOWING_LAVA, "Creative exemption survived cancellation");
        LOG.info("CRUCIBLE_WORLD_CASE cancellation passed");
    }

    private void unprocessedDrops(WorldServer world, MinecraftServer server) {
        BlockPos pos = position();
        FakePlayer player = player(world, server, false);
        MetaTileEntityCrucible vessel = place(world, pos, player);
        NBTTagCompound data = contents(1300);
        ItemStack pending = OreDictUnifier.get(OrePrefix.ingot, Materials.Iron, 64);
        NBTTagList queue = new NBTTagList();
        queue.appendTag(pending.writeToNBT(new NBTTagCompound()));
        data.setTag("PendingItems", queue);
        vessel.readFromNBT(data);
        IItemHandler items = vessel.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        check(items != null, "Missing actual item capability");
        ItemStack buffered = OreDictUnifier.get(OrePrefix.ingot, Materials.Gold, 3);
        check(items.insertItem(0, buffered.copy(), false).isEmpty(), "Input slot refused its buffer");
        check(player.interactionManager.tryHarvestBlock(pos), "Buffered hot machine removal failed");
        check(countDrops(world, pos, pending) == 64, "Pending inventory was lost or duplicated");
        check(countDrops(world, pos, buffered) == 3, "Input inventory was lost or duplicated");
        check(vessel.getPendingItemCount() == 0, "Removed machine retained its pending queue");
        LOG.info("CRUCIBLE_WORLD_CASE unprocessed_drops passed");
    }

    private MetaTileEntityCrucible place(WorldServer world, BlockPos pos, FakePlayer player) {
        ItemStack stack = MetaTileEntityHandler.CRUCIBLE_HU[0].getStackForm();
        check(stack.getItem() instanceof MachineItemBlock, "Registered crucible has no machine item block");
        MachineItemBlock item = (MachineItemBlock) stack.getItem();
        check(world.isAirBlock(pos), "Isolated test position is not empty: " + pos);
        player.setPosition(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5);
        check(item.placeBlockAt(stack, player, world, pos, EnumFacing.UP, 0.5F, 0.5F, 0.5F,
                item.getBlock().getDefaultState()), "Actual machine placement failed");
        MetaTileEntity installed = GTUtility.getMetaTileEntity(world, pos);
        check(installed instanceof MetaTileEntityCrucible, "Actual placement did not install a crucible");
        return (MetaTileEntityCrucible) installed;
    }

    private static FakePlayer player(WorldServer world, MinecraftServer server, boolean creative) {
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[CrucibleSmoke]"));
        new NetHandlerPlayServer(server, new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
            @Override public void sendPacket(Packet<?> packet) {} // No real connection or remote client.
        };
        player.setHeldItem(EnumHand.MAIN_HAND, creative ? ItemStack.EMPTY : ToolItems.WRENCH.get(Materials.Iron));
        player.interactionManager.setGameType(creative ? GameType.CREATIVE : GameType.SURVIVAL);
        return player;
    }

    private static NBTTagCompound contents(long temperature) {
        NBTTagCompound data = new NBTTagCompound();
        data.setLong("Temperature", temperature);
        data.setLong("OldTemperature", temperature);
        data.setInteger("WaterUnitVersion", 1);
        data.setInteger("FluidQuantityVersion", 1);
        NBTTagCompound material = new NBTTagCompound();
        material.setString("Material", "gregtech:iron");
        material.setLong("Amount", GTValues.M);
        NBTTagList contents = new NBTTagList();
        contents.appendTag(material);
        data.setTag("Contents", contents);
        return data;
    }

    private BlockPos position() { return new BlockPos(400 + 16 * nextPosition++, 70, 400); }

    private static int countDrops(WorldServer world, BlockPos pos, ItemStack expected) {
        List<EntityItem> drops = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).grow(4));
        int amount = 0;
        for (EntityItem drop : drops) if (ItemStack.areItemsEqual(expected, drop.getItem())) amount += drop.getItem().getCount();
        return amount;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static final class BreakCancellation {
        private final BlockPos pos;
        BreakCancellation(BlockPos pos) { this.pos = pos; }
        @SubscribeEvent public void cancel(BlockEvent.BreakEvent event) {
            if (pos.equals(event.getPos())) event.setCanceled(true);
        }
    }

    public static final class CreativeBreakObservation {
        private final BlockPos pos;
        boolean cancelled;
        CreativeBreakObservation(BlockPos pos) { this.pos = pos; }
        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public void observe(BlockEvent.BreakEvent event) {
            if (pos.equals(event.getPos())) {
                cancelled = event.isCanceled();
            }
        }
    }
}
