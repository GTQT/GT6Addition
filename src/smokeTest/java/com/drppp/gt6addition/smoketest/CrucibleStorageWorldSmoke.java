package com.drppp.gt6addition.smoketest;

import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.mojang.authlib.GameProfile;
import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TickableTileEntityBase;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.GameType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;

import java.util.UUID;
import java.lang.reflect.Field;

/** Real registered storage/capability/entity paths, only in the opt-in test mod. */
final class CrucibleStorageWorldSmoke {
    private static int nextPosition;

    static int run(WorldServer world, MinecraftServer server) {
        fifoAndTileReload(world, server);
        fullQueueAndTakeback(world, server);
        invalidItemPreserved(world, server);
        multiMaterialAtomicInput(world, server);
        sharedFractionalCapacity(world, server);
        lavaBoundary(world, server, 1299);
        lavaBoundary(world, server, 1300);
        boilingAndRange(world, server);
        playerSteamProtection(world, server);
        rainExposure(world, server);
        return 10;
    }

    private static void fifoAndTileReload(WorldServer world, MinecraftServer server) {
        FakePlayer player = player(world, server);
        MetaTileEntityCrucible vessel = place(world, player);
        check(fluids(vessel).fill(new FluidStack(FluidRegistry.WATER, 16000), true) == 16000, "FIFO water fill");
        offer(vessel, player, ingot(Materials.Iron, 1));
        offer(vessel, player, ingot(Materials.Gold, 1));
        offer(vessel, player, ingot(Materials.Iron, 1));
        check(items(vessel).insertItem(0, ingot(Materials.Gold, 1), false).isEmpty(), "FIFO slot insert");
        EntityItem dropped = drop(world, vessel, ingot(Materials.Iron, 2));
        vessel.update();
        check(dropped.isDead && items(vessel).getStackInSlot(0).isEmpty(), "FIFO inputs not queued");
        queue(vessel, Materials.Iron, Materials.Gold, Materials.Iron, Materials.Gold, Materials.Iron);
        check(vessel.getPendingItemCount() == 6, "FIFO item count");
        // Serialize/recreate the actual registered CEu holder, not just MTE.readFromNBT.
        vessel = reload(world, vessel);
        queue(vessel, Materials.Iron, Materials.Gold, Materials.Iron, Materials.Gold, Materials.Iron);
        check(vessel.getPendingItemCount() == 6, "Full holder reload lost queue");
        Material[] expected = {Materials.Iron, Materials.Gold, Materials.Iron, Materials.Gold, Materials.Iron, Materials.Iron};
        long iron = 0, gold = 0;
        for (int i = 0; i < expected.length; i++) {
            NBTTagCompound before = save(vessel);
            FluidStack simulated = fluids(vessel).drain(1000, false);
            check(simulated != null && simulated.amount == 1000 && before.equals(save(vessel)), "FIFO simulated drain mutated");
            check(fluids(vessel).drain(1000, true).amount == 1000, "FIFO drain failed");
            vessel.update();
            if (expected[i] == Materials.Iron) iron += GTValues.M; else gold += GTValues.M;
            check(amount(vessel, Materials.Iron) == iron && amount(vessel, Materials.Gold) == gold,
                    "FIFO order or one-item-per-tick limit at " + i);
            check(vessel.getPendingItemCount() == 5 - i, "FIFO count after capacity release");
            check(amount(vessel, Materials.Water) + iron + gold == 16L * GTValues.M, "Shared capacity after queue import");
        }
        pass("FIFO_TILE_RELOAD");
    }

    private static void fullQueueAndTakeback(WorldServer world, MinecraftServer server) {
        FakePlayer player = player(world, server);
        MetaTileEntityCrucible vessel = place(world, player);
        check(fluids(vessel).fill(new FluidStack(FluidRegistry.WATER, 16000), true) == 16000, "Full queue fill");
        EntityItem accepted = drop(world, vessel, ingot(Materials.Iron, 64));
        vessel.update();
        check(accepted.isDead && vessel.getPendingItemCount() == 64, "Queue did not accept exactly 64 items");
        EntityItem refused = drop(world, vessel, ingot(Materials.Gold, 2));
        check(items(vessel).insertItem(0, ingot(Materials.Gold, 3), false).isEmpty(), "Explicit slot buffering");
        vessel.update();
        check(!refused.isDead && refused.getItem().getCount() == 2 && vessel.getPendingItemCount() == 64,
                "Full FIFO consumed world items");
        check(items(vessel).getStackInSlot(0).getCount() == 3, "Full FIFO consumed input slot");
        player.setHeldItem(EnumHand.MAIN_HAND, ingot(Materials.Gold, 1));
        NBTTagCompound before = save(vessel);
        vessel.onRightClick(player, EnumHand.MAIN_HAND, EnumFacing.NORTH, null);
        check(player.getHeldItemMainhand().getCount() == 1 && before.equals(save(vessel)), "Full FIFO consumed held item");
        // Empty-hand top access must take the explicit slot before the queue.
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        check(vessel.onRightClick(player, EnumHand.MAIN_HAND, EnumFacing.UP, null), "Slot takeback unhandled");
        check(inventoryCount(player, ingot(Materials.Gold, 1)) == 1 &&
                items(vessel).getStackInSlot(0).getCount() == 2 && vessel.getPendingItemCount() == 64,
                "Top takeback lost items or skipped explicit slot");
        // The host's external item capability is insert-only for import slots.
        // Keep the returned gold and select a genuinely empty hotbar slot.
        player.inventory.currentItem = 8;
        for (int i = 0; i < 2; i++) {
            check(player.getHeldItemMainhand().isEmpty(), "Takeback fixture hand is occupied");
            check(vessel.onRightClick(player, EnumHand.MAIN_HAND, EnumFacing.UP, null), "Remaining slot takeback");
        }
        check(items(vessel).getStackInSlot(0).isEmpty() && inventoryCount(player, ingot(Materials.Gold, 1)) == 3,
                "Repeated top takeback changed explicit slot quantity");
        check(vessel.onRightClick(player, EnumHand.MAIN_HAND, EnumFacing.UP, null), "Queue takeback unhandled");
        check(inventoryCount(player, ingot(Materials.Iron, 1)) == 1 && vessel.getPendingItemCount() == 63,
                "Queue takeback count");
        pass("FULL_QUEUE_TAKEBACK");
    }

    private static void invalidItemPreserved(WorldServer world, MinecraftServer server) {
        FakePlayer player = player(world, server);
        MetaTileEntityCrucible vessel = place(world, player);
        ItemStack invalid = ingot(Materials.Iron, 2);
        NBTTagCompound extra = new NBTTagCompound();
        extra.setBoolean("SmokeUnknownInventory", true);
        invalid.setTagCompound(extra);
        EntityItem entity = drop(world, vessel, invalid.copy());
        check(items(vessel).insertItem(0, invalid.copy(), false).isEmpty(), "Unsupported buffer insert");
        vessel.update();
        check(!entity.isDead && ItemStack.areItemStacksEqual(invalid, entity.getItem()), "Unsupported world input lost");
        check(ItemStack.areItemStacksEqual(invalid, items(vessel).getStackInSlot(0)) &&
                vessel.getPendingItemCount() == 0 && save(vessel).getTagList("Contents", 10).isEmpty(),
                "Unsupported input partially processed");
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        vessel.onRightClick(player, EnumHand.MAIN_HAND, EnumFacing.UP, null);
        check(items(vessel).getStackInSlot(0).getCount() == 1 && inventoryCount(player, invalid) == 1,
                "Unsupported slot cannot be recovered intact");
        pass("INVALID_INPUT_PRESERVED");
    }

    private static void multiMaterialAtomicInput(WorldServer world, MinecraftServer server) {
        FakePlayer player = player(world, server);
        MetaTileEntityCrucible vessel = place(world, player);
        check(fluids(vessel).fill(new FluidStack(FluidRegistry.WATER, 15000), true) == 15000, "Atomic batch fill");
        // GT6's explicit recycling override format replaces plain-item data.
        ItemStack multi = new ItemStack(Items.STICK);
        NBTTagCompound root = new NBTTagCompound(), entries = new NBTTagCompound();
        entries.setInteger("size", 2);
        for (int i = 0; i < 2; i++) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("m", i == 0 ? "Copper" : "Tin");
            entry.setLong("a", 648648000L); // GT6 CS.U, one unit of each component.
            entries.setTag(Integer.toString(i), entry);
        }
        root.setTag("gt.recycling.mats", entries);
        multi.setTagCompound(root);
        offer(vessel, player, multi);
        check(vessel.getPendingItemCount() == 1 && amount(vessel, Materials.Copper) == 0 && amount(vessel, Materials.Tin) == 0,
                "Multi-material input was partially imported");
        vessel = reload(world, vessel);
        vessel.update();
        check(vessel.getPendingItemCount() == 1 && amount(vessel, Materials.Copper) == 0 && amount(vessel, Materials.Tin) == 0,
                "Blocked multi-material queue partially imported");
        check(fluids(vessel).drain(1000, true).amount == 1000, "Multi-material capacity release");
        vessel.update();
        check(vessel.getPendingItemCount() == 0 && amount(vessel, Materials.Copper) == GTValues.M &&
                amount(vessel, Materials.Tin) == GTValues.M && amount(vessel, Materials.Water) == 14L * GTValues.M,
                "Multi-material batch identity, amount or shared capacity");
        pass("MULTI_MATERIAL_ATOMIC_INPUT");
    }

    private static void sharedFractionalCapacity(WorldServer world, MinecraftServer server) {
        MetaTileEntityCrucible vessel = place(world, player(world, server));
        check(items(vessel).insertItem(0, ingot(Materials.Iron, 15), false).isEmpty(), "Shared capacity iron buffer");
        for (int i = 0; i < 15; i++) vessel.update();
        check(amount(vessel, Materials.Iron) == 15L * GTValues.M, "Shared capacity solid quantity");
        NBTTagCompound before = save(vessel);
        check(fluids(vessel).fill(new FluidStack(FluidRegistry.WATER, 1234), false) == 1000 && before.equals(save(vessel)),
                "Shared capacity simulated fill or limit");
        check(fluids(vessel).fill(new FluidStack(FluidRegistry.WATER, 1234), true) == 1000, "Shared capacity actual fill");
        before = save(vessel);
        check(fluids(vessel).fill(new FluidStack(FluidRegistry.LAVA, 1), true) == 0 && before.equals(save(vessel)),
                "Lava used independent capacity or changed full vessel temperature");
        check(fluids(vessel).drain(1000, true).amount == 1000, "Shared water drain");
        check(fluids(vessel).fill(new FluidStack(FluidRegistry.WATER, 987), true) == 987, "Fractional water fill");
        vessel = reload(world, vessel);
        check(amount(vessel, Materials.Iron) == 15L * GTValues.M && fluids(vessel).drain(2000, false).amount == 987,
                "Fractional water changed during full tile reload");
        pass("SHARED_FRACTIONAL_CAPACITY");
    }

    private static void lavaBoundary(WorldServer world, MinecraftServer server, long temperature) {
        MetaTileEntityCrucible vessel = place(world, player(world, server));
        IFluidHandler fluid = fluids(vessel);
        NBTTagCompound empty = save(vessel);
        check(fluid.fill(new FluidStack(FluidRegistry.LAVA, 1145), false) == 1145 && empty.equals(save(vessel)),
                "Simulated lava fill has thermal/world side effects");
        check(fluid.fill(new FluidStack(FluidRegistry.LAVA, 1145), true) == 1145, "Lava fill refused");
        check(vessel.getCurrentTemperature() > empty.getLong("Temperature"), "Actual lava failed to heat vessel");
        temperature(vessel, temperature);
        vessel.update();
        if (temperature == 1299) {
            check(amount(vessel, Materials.Obsidian) == GTValues.M &&
                    !content(vessel, Materials.Obsidian).getBoolean("Molten"), "One bucket did not make one solid obsidian");
            check(vessel.getTopContents().stream().anyMatch(info ->
                    Materials.Lava.getLocalizedName().equals(info.getMaterialName()) && info.getFluidAmount() == 145),
                    "Lava fractional remainder lost");
            check(fluid.drain(2000, false) == null, "Cold lava bypassed GT6 extraction temperature");
            temperature(vessel, 1300);
            vessel.update();
            check(amount(vessel, Materials.Obsidian) == 0 && fluid.drain(2000, false).amount == 1145,
                    "Obsidian reheating did not restore exactly one bucket: " + save(vessel) + ", drain=" + fluid.drain(2000, false));
        } else {
            check(amount(vessel, Materials.Obsidian) == 0 && fluid.drain(2000, false).amount == 1145 &&
                    content(vessel, Materials.Lava).getBoolean("Molten"), "Lava condensed at 1300 K");
        }
        pass("LAVA_BOUNDARY_" + temperature);
    }

    private static void boilingAndRange(WorldServer world, MinecraftServer server) {
        MetaTileEntityCrucible vessel = place(world, player(world, server));
        check(fluids(vessel).fill(new FluidStack(FluidRegistry.WATER, 1000), true) == 1000, "Steam fill");
        BlockPos pos = vessel.getPos();
        EntityCow near = cow(world, pos, 2), far = cow(world, pos, 6), protectedCow = cow(world, pos, -2);
        protectedCow.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 100));
        try {
            temperature(vessel, 372);
            vessel.update();
            check(near.getHealth() == near.getMaxHealth() && amount(vessel, Materials.Water) == GTValues.M,
                    "372 K prematurely boiled or damaged nearby entity");
            temperature(vessel, 373);
            vessel.update();
            check(amount(vessel, Materials.Water) == 0 && near.getHealth() < near.getMaxHealth(), "373 K steam did not burn nearby entity");
            check(Math.abs((near.getMaxHealth() - near.getHealth()) - 2.92F) < 0.001F, "Steam used crucible temperature instead of boiling point");
            check(far.getHealth() == far.getMaxHealth() && protectedCow.getHealth() == protectedCow.getMaxHealth(),
                    "Steam ignored range/fire resistance");
            check(GTUtility.getMetaTileEntity(world, pos) == vessel, "Steam removed crucible");
        } finally { world.removeEntity(near); world.removeEntity(far); world.removeEntity(protectedCow); }
        pass("STEAM_BOUNDARY_RANGE");
    }

    private static void playerSteamProtection(WorldServer world, MinecraftServer server) {
        MetaTileEntityCrucible vessel = place(world, player(world, server));
        check(fluids(vessel).fill(new FluidStack(FluidRegistry.WATER, 1000), true) == 1000, "Player steam fill");
        EntityPlayerMP[] players = new EntityPlayerMP[4];
        try {
            for (int i = 0; i < players.length; i++) {
                EntityPlayerMP player = new EntityPlayerMP(server, world,
                        new GameProfile(UUID.randomUUID(), "[SteamPlayer" + i + "]"), new PlayerInteractionManager(world));
                players[i] = player;
                new NetHandlerPlayServer(server, new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
                    @Override public void sendPacket(Packet<?> packet) {}
                };
                player.interactionManager.setGameType(i == 1 ? GameType.CREATIVE : GameType.SURVIVAL);
                BlockPos pos = vessel.getPos();
                int dx = i < 2 ? (i == 0 ? 2 : -2) : 0;
                int dz = i >= 2 ? (i == 2 ? 2 : -2) : 0;
                player.setPosition(pos.getX() + .5 + dx, pos.getY() + .5, pos.getZ() + .5 + dz);
                // Actual MP, not Forge FakePlayer (which is always invulnerable).
                // Expire vanilla's initial 60-tick respawn shield through its
                // own update method; do not override damage or the armor logic.
                for (int tick = 0; tick < 61; tick++) player.onUpdate();
                if (i == 2) player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 100));
                if (i == 3) player.setItemStackToSlot(EntityEquipmentSlot.CHEST, MetaItems.NOMEX_CHESTPLATE.getStackForm());
                check(world.spawnEntity(player), "Actual server player could not enter isolated world");
            }
            temperature(vessel, 372);
            vessel.update();
            for (EntityPlayerMP player : players) check(player.getHealth() == player.getMaxHealth(), "Player hurt below boiling");
            temperature(vessel, 373);
            vessel.update();
            check(Math.abs((players[0].getMaxHealth() - players[0].getHealth()) - 2.92F) < 0.001F,
                    "Unarmored survival player steam damage");
            for (int i = 1; i < players.length; i++) {
                check(players[i].getHealth() == players[i].getMaxHealth(), "Steam ignored player protection " + i);
            }
            check(amount(vessel, Materials.Water) == 0, "Player steam left water");
        } finally {
            for (EntityPlayerMP player : players) if (player != null) world.removeEntity(player);
        }
        pass("PLAYER_STEAM_PROTECTION");
    }

    private static void rainExposure(WorldServer world, MinecraftServer server) {
        boolean oldRain = world.getWorldInfo().isRaining(), oldThunder = world.getWorldInfo().isThundering();
        float rainStrength = world.rainingStrength, oldPrevRain = world.prevRainingStrength;
        float thunderStrength = world.thunderingStrength, oldPrevThunder = world.prevThunderingStrength;
        try {
            world.getWorldInfo().setRaining(true);
            // Vanilla setters are CLIENT-only and stripped on a dedicated
            // server. Use the actual Forge-accessible weather fields instead.
            world.rainingStrength = world.prevRainingStrength = 1;
            for (boolean thunder : new boolean[]{false, true}) {
                world.getWorldInfo().setThundering(thunder);
                world.thunderingStrength = world.prevThunderingStrength = thunder ? 1 : 0;
                MetaTileEntityCrucible open = place(world, player(world, server));
                MetaTileEntityCrucible covered = place(world, player(world, server));
                for (MetaTileEntityCrucible vessel : new MetaTileEntityCrucible[]{open, covered}) {
                    BlockPos pos = vessel.getPos();
                    world.getChunk(pos).getBiomeArray()[((pos.getZ() & 15) << 4) | (pos.getX() & 15)] =
                            (byte) Biome.getIdForBiome(Biomes.PLAINS);
                }
                world.setBlockState(covered.getPos().up(), Blocks.STONE.getDefaultState());
                check(world.isRainingAt(open.getPos().up()) && !world.isRainingAt(covered.getPos().up()), "Rain fixture exposure");
                for (MetaTileEntityCrucible vessel : new MetaTileEntityCrucible[]{open, covered}) {
                    MetaTileEntityHolder holder = (MetaTileEntityHolder) world.getTileEntity(vessel.getPos());
                    rainClock(holder);
                    holder.update(); // Offset time 609: before the rain opportunity.
                    check(save(vessel).getTagList("Contents", 10).isEmpty(), "Rain collected at wrong tick");
                    holder.update(); // 610: exactly the rain opportunity.
                    NBTTagCompound afterRain = save(vessel);
                    holder.update(); // 611: cannot collect a second time.
                    check(afterRain.getTagList("Contents", 10).equals(save(vessel).getTagList("Contents", 10)),
                            "Rain collected outside the interval");
                }
                int expected = Math.max(1, (int) (world.getBiome(open.getPos()).getRainfall() * 100)) * (thunder ? 2 : 1);
                check(fluids(open).drain(1000, false).amount == expected, "Rain quantity or thunder factor");
                check(save(covered).getTagList("Contents", 10).isEmpty(), "Covered crucible collected rain");
            }
        } finally {
            world.getWorldInfo().setRaining(oldRain);
            world.getWorldInfo().setThundering(oldThunder);
            world.rainingStrength = rainStrength;
            world.prevRainingStrength = oldPrevRain;
            world.thunderingStrength = thunderStrength;
            world.prevThunderingStrength = oldPrevThunder;
        }
        pass("RAIN_EXPOSURE_THUNDER");
    }

    private static void rainClock(MetaTileEntityHolder holder) {
        try {
            // Exact private field confirmed in the actual dependency. Only the
            // isolated fixture's clock is fast-forwarded, not WorldServer or
            // production code. Avoid 600 updates in a single startup event.
            Field timer = TickableTileEntityBase.class.getDeclaredField("timer");
            timer.setAccessible(true);
            long offset = holder.getOffsetTimer() - timer.getLong(holder);
            timer.setLong(holder, 609 - offset);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Actual CEu timer contract changed", failure);
        }
    }

    private static MetaTileEntityCrucible place(WorldServer world, FakePlayer player) {
        BlockPos pos = new BlockPos(1400 + 16 * nextPosition++, 70, 600);
        ItemStack machine = MetaTileEntityHandler.CRUCIBLE_HU[15].getStackForm();
        MachineItemBlock block = (MachineItemBlock) machine.getItem();
        check(world.isAirBlock(pos), "Storage fixture occupied");
        check(block.placeBlockAt(machine, player, world, pos, EnumFacing.UP, .5F, .5F, .5F,
                block.getBlock().getDefaultState()), "Storage fixture placement failed");
        return (MetaTileEntityCrucible) GTUtility.getMetaTileEntity(world, pos);
    }

    private static MetaTileEntityCrucible reload(WorldServer world, MetaTileEntityCrucible vessel) {
        BlockPos pos = vessel.getPos();
        TileEntity original = world.getTileEntity(pos);
        NBTTagCompound tileData = original.writeToNBT(new NBTTagCompound());
        TileEntity recreated = TileEntity.create(world, tileData);
        check(recreated instanceof MetaTileEntityHolder && recreated != original, "CEu holder recreation");
        world.removeTileEntity(pos);
        world.setTileEntity(pos, recreated);
        MetaTileEntityCrucible loaded = (MetaTileEntityCrucible) GTUtility.getMetaTileEntity(world, pos);
        check(loaded != null && loaded != vessel && save(vessel).equals(save(loaded)), "Full tile NBT changed state");
        return loaded;
    }

    private static FakePlayer player(WorldServer world, MinecraftServer server) {
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[StorageSmoke]"));
        new NetHandlerPlayServer(server, new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
            @Override public void sendPacket(Packet<?> packet) {}
        };
        return player;
    }

    private static EntityCow cow(WorldServer world, BlockPos pos, int offset) {
        EntityCow cow = new EntityCow(world);
        cow.setPosition(pos.getX() + .5 + offset, pos.getY() + .5, pos.getZ() + .5);
        check(world.spawnEntity(cow), "Real hazard entity spawn failed");
        return cow;
    }

    private static EntityItem drop(WorldServer world, MetaTileEntityCrucible vessel, ItemStack stack) {
        BlockPos pos = vessel.getPos();
        EntityItem entity = new EntityItem(world, pos.getX() + .5, pos.getY() + .3, pos.getZ() + .5, stack);
        check(world.spawnEntity(entity), "Real input entity spawn failed");
        return entity;
    }

    private static void offer(MetaTileEntityCrucible vessel, FakePlayer player, ItemStack input) {
        player.setHeldItem(EnumHand.MAIN_HAND, input);
        check(vessel.onRightClick(player, EnumHand.MAIN_HAND, EnumFacing.NORTH, null) && input.isEmpty(), "Held input not queued");
    }

    private static ItemStack ingot(Material material, int count) {
        ItemStack stack = OreDictUnifier.get(OrePrefix.ingot, material, count);
        check(!stack.isEmpty(), "Missing registered ingot " + material);
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

    private static void temperature(MetaTileEntityCrucible vessel, long temperature) {
        NBTTagCompound state = save(vessel);
        state.setLong("Temperature", temperature);
        state.setLong("OldTemperature", temperature);
        vessel.readFromNBT(state);
    }

    private static NBTTagCompound save(MetaTileEntityCrucible vessel) { return vessel.writeToNBT(new NBTTagCompound()); }

    private static NBTTagCompound content(MetaTileEntityCrucible vessel, Material material) {
        NBTTagList contents = save(vessel).getTagList("Contents", 10);
        for (int i = 0; i < contents.tagCount(); i++) {
            NBTTagCompound tag = contents.getCompoundTagAt(i);
            if (material.getRegistryName().equals(tag.getString("Material"))) return tag;
        }
        return new NBTTagCompound();
    }

    private static long amount(MetaTileEntityCrucible vessel, Material material) { return content(vessel, material).getLong("Amount"); }

    private static void queue(MetaTileEntityCrucible vessel, Material... materials) {
        NBTTagList queue = save(vessel).getTagList("PendingItems", 10);
        check(queue.tagCount() == materials.length, "FIFO merged non-adjacent entries");
        for (int i = 0; i < materials.length; i++) {
            check(ItemStack.areItemsEqual(ingot(materials[i], 1), new ItemStack(queue.getCompoundTagAt(i))), "FIFO material at " + i);
        }
    }

    private static int inventoryCount(FakePlayer player, ItemStack expected) {
        int result = 0;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (ItemStack.areItemsEqual(expected, stack) && ItemStack.areItemStackTagsEqual(expected, stack)) result += stack.getCount();
        }
        return result;
    }

    private static void pass(String name) { LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_STORAGE_{} passed", name); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
