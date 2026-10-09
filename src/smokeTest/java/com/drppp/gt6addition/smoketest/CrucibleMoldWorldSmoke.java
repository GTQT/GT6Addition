package com.drppp.gt6addition.smoketest;

import com.drppp.gt6addition.common.item.GT6CastingIngotItem;
import com.drppp.gt6addition.common.material.GT6MaterialCompatibility;
import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityMold;
import com.mojang.authlib.GameProfile;
import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;

import java.util.UUID;

/** Complete registered Fluorite casting/recovery path in an isolated real world. */
final class CrucibleMoldWorldSmoke {
    static int run(WorldServer world, MinecraftServer server) {
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[MoldSmoke]"));
        new NetHandlerPlayServer(server, new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
            @Override public void sendPacket(Packet<?> packet) {}
        };
        verifyAmbientCooling(world, player);
        verifyBoilingMoldMeltdown(world, player);

        Material fluorite = GT6MaterialCompatibility.findExternal("fluorite");
        if (fluorite == null) {
            check(net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValue(
                    new net.minecraft.util.ResourceLocation("gt6addition", "fluorite_ingot")) == null,
                    "Absent external Fluorite generated an addon casting form");
            LogManager.getLogger("CrucibleParitySmoke")
                    .info("CRUCIBLE_WORLD_OPTIONAL_FLUORITE unavailable; casting checks not executed");
            return 2;
        }
        ItemStack ingot = OreDictUnifier.get(OrePrefix.ingot, fluorite);
        check(!ingot.isEmpty() && !OreDictUnifier.get(OrePrefix.gem, fluorite).isEmpty(), "Missing Fluorite form");
        check(!fluorite.hasProperty(PropertyKey.INGOT), "Gem material acquired conflicting IngotProperty");
        check(OreDictUnifier.getMaterial(ingot).material == fluorite &&
                OreDictUnifier.getMaterial(ingot).amount == GTValues.M, "Wrong casting ingot material quantity");
        check(ingot.getItem() instanceof GT6CastingIngotItem && ingot.getMetadata() == 0,
                "Casting item identity depends on optional Core's numeric material ID");
        check(((GT6CastingIngotItem) ingot.getItem()).getMaterial(new ItemStack(ingot.getItem(), 1, 1)) == null,
                "Unregistered item metadata acquired a material");

        // Chrome is acid-proof in the actual registered GT6 machine family.
        MetaTileEntityCrucible vessel = (MetaTileEntityCrucible) place(world, 0, player,
                MetaTileEntityHandler.CRUCIBLE_HU[11]);
        MetaTileEntityMold mold = mold(world, 1, player, 11);
        vessel.readFromNBT(contents(fluorite, 1633));
        NBTTagCompound beforeVessel = vessel.writeToNBT(new NBTTagCompound());
        NBTTagCompound beforeMold = mold.writeToNBT(new NBTTagCompound());
        check(mold.fillMold(fluorite, GTValues.M, 1633, EnumFacing.UP, true) == GTValues.M,
                "Valid simulated Fluorite casting refused");
        check(beforeVessel.equals(vessel.writeToNBT(new NBTTagCompound())) &&
                beforeMold.equals(mold.writeToNBT(new NBTTagCompound())), "Simulated casting mutated inventory");
        check(vessel.fillMoldAtSide(mold, EnumFacing.EAST, EnumFacing.UP) == GTValues.M,
                "Actual Fluorite casting refused");
        check(vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10).tagCount() == 0,
                "Pour left duplicated source material");
        check(mold.isCooling() && mold.getOutputStack().isEmpty(), "Casting skipped molten cooling state");
        NBTTagCompound cooling = mold.writeToNBT(new NBTTagCompound());
        mold.readFromNBT(cooling);
        check(cooling.equals(mold.writeToNBT(new NBTTagCompound())) && mold.isCooling(),
                "Molten casting reload changed quantity or state");
        mold.update();
        check(!mold.isCooling() && ItemStack.areItemStacksEqual(ingot, mold.getOutputStack()),
                "Cooling did not produce an actual Fluorite ingot");
        NBTTagCompound cooled = mold.writeToNBT(new NBTTagCompound());
        mold.readFromNBT(cooled);
        check(ItemStack.areItemStacksEqual(ingot, mold.getOutputStack()), "Finished casting lost on reload");
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        check(mold.onRightClick(player, EnumHand.MAIN_HAND, EnumFacing.UP, null), "Casting pickup not handled");
        int count = 0;
        ItemStack recovered = ItemStack.EMPTY;
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack item = player.inventory.getStackInSlot(slot);
            if (ItemStack.areItemsEqual(ingot, item)) {
                count += item.getCount();
                recovered = item.copy();
                player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            }
        }
        check(count == 1 && mold.getOutputStack().isEmpty(), "Casting pickup lost or duplicated output");
        vessel.readFromNBT(contents(null, 300));
        IItemHandler items = vessel.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        check(items != null && items.insertItem(0, recovered, false).isEmpty(), "Casting cannot be recycled");
        vessel.update();
        NBTTagList recycled = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
        check(recycled.tagCount() == 1 && fluorite.getRegistryName().equals(
                recycled.getCompoundTagAt(0).getString("Material")) &&
                recycled.getCompoundTagAt(0).getLong("Amount") == GTValues.M && items.getStackInSlot(0).isEmpty(),
                "Casting recycling changed material or quantity");
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_FLUORITE_CAST_RECOVER passed");

        MetaTileEntityMold nonAcidProof = mold(world, 2, player, 8);
        NBTTagCompound acidBefore = nonAcidProof.writeToNBT(new NBTTagCompound());
        check(nonAcidProof.fillMold(fluorite, GTValues.M, 1633, EnumFacing.UP, true) == 0 &&
                nonAcidProof.fillMold(fluorite, GTValues.M, 1633, EnumFacing.UP, false) == 0 &&
                acidBefore.equals(nonAcidProof.writeToNBT(new NBTTagCompound())), "Acid refusal mutated mold");
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_FLUORITE_ACID_REFUSAL passed");

        MetaTileEntityMold emptyMold = mold(world, 3, player, 11);
        NBTTagCompound coldBefore = emptyMold.writeToNBT(new NBTTagCompound());
        check(emptyMold.fillMold(fluorite, GTValues.M, 1632, EnumFacing.UP, false) == 0 &&
                emptyMold.fillMold(fluorite, GTValues.M - 1, 1633, EnumFacing.UP, false) == 0 &&
                coldBefore.equals(emptyMold.writeToNBT(new NBTTagCompound())), "Cold/short casting consumed material");
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_FLUORITE_CAST_REFUSAL passed");
        return 5;
    }

    private static void verifyAmbientCooling(WorldServer world, FakePlayer player) {
        ItemStack ingot = OreDictUnifier.get(OrePrefix.ingot, Materials.Iron);
        check(!ingot.isEmpty(), "Missing native iron ingot for mold cooling check");

        MetaTileEntityMold withOutput = mold(world, 4, player, 11);
        NBTTagCompound coolingState = withOutput.writeToNBT(new NBTTagCompound());
        coolingState.setLong("gt.mold.temperature", 400L);
        coolingState.setLong("gt.mold.cooling_start_temperature", 400L);
        coolingState.setTag("gt.mold.output", ingot.writeToNBT(new NBTTagCompound()));
        withOutput.readFromNBT(coolingState);
        long ambient = environmentTemperature(world, withOutput.getPos());
        check(ambient < 400L, "Flat-world ambient temperature must be below mold test temperature");
        withOutput.update();
        check(withOutput.getTemperatureValue(null) == Math.max(ambient, 395L),
                "Mold with finished casting did not cool 5 K toward biome ambient");
        check(ItemStack.areItemStacksEqual(ingot, withOutput.getOutputStack()),
                "Passive cooling changed the finished casting");

        MetaTileEntityMold empty = mold(world, 5, player, 11);
        NBTTagCompound emptyState = empty.writeToNBT(new NBTTagCompound());
        emptyState.setLong("gt.mold.temperature", 400L);
        empty.readFromNBT(emptyState);
        empty.update();
        check(empty.getTemperatureValue(null) == ambient,
                "Empty mold failed to settle to the biome environment temperature");
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_MOLD_AMBIENT_COOLING passed");
    }

    private static void verifyBoilingMoldMeltdown(WorldServer world, FakePlayer player) {
        MetaTileEntityMold mold = mold(world, 6, player, 22);
        NBTTagCompound state = mold.writeToNBT(new NBTTagCompound());
        state.setLong("gt.mold.temperature", 1186L);
        state.setLong("gt.mold.cooling_start_temperature", 1186L);
        state.setString("gt.mold.material", Materials.Zinc.getRegistryName());
        state.setLong("gt.mold.amount", 1L);
        mold.readFromNBT(state);
        check(mold.getTemperatureMax(null) > 1181L,
                "Ceramic mold max temperature must isolate the Zinc boiling-point hazard");
        mold.update(); // 1186 K cools by 5 K, remaining above GT6 Zinc boiling point (1180 K).
        check(world.getBlockState(mold.getPos()).getBlock() == net.minecraft.init.Blocks.FLOWING_LAVA,
                "Mold contents above the GT6 Zinc boiling point did not melt down");
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_MOLD_BOILING_MELTDOWN passed");
    }

    private static long environmentTemperature(WorldServer world, BlockPos pos) {
        net.minecraft.world.biome.Biome biome = world.getBiome(pos);
        return biome == null ? 293L : Math.max(1L, 270L +
                (long) (biome.getTemperature(pos) * 20.0F));
    }

    private static MetaTileEntityMold mold(WorldServer world, int index, FakePlayer player, int materialIndex) {
        MetaTileEntityMold mold = (MetaTileEntityMold) place(world, index, player, MetaTileEntityHandler.MOLDS[materialIndex]);
        NBTTagCompound state = new NBTTagCompound();
        int shape = 0;
        for (int row = 0; row < 5; row++) for (int column = 0; column < 3; column++) shape |= 1 << (5 * row + column);
        state.setInteger("gt.mold", shape); // GT6 ingot's 3 x 5 carved cells.
        mold.readFromNBT(state);
        return mold;
    }

    private static MetaTileEntity place(WorldServer world, int index, FakePlayer player, MetaTileEntity prototype) {
        BlockPos pos = new BlockPos(1200 + 16 * index, 70, 400);
        ItemStack machine = prototype.getStackForm();
        MachineItemBlock block = (MachineItemBlock) machine.getItem();
        check(world.isAirBlock(pos), "Casting fixture position occupied");
        check(block.placeBlockAt(machine, player, world, pos, EnumFacing.UP, .5F, .5F, .5F,
                block.getBlock().getDefaultState()), "Casting fixture placement failed");
        return GTUtility.getMetaTileEntity(world, pos);
    }

    private static NBTTagCompound contents(Material material, long temperature) {
        NBTTagCompound state = new NBTTagCompound();
        state.setLong("Temperature", temperature);
        state.setLong("OldTemperature", temperature);
        state.setInteger("WaterUnitVersion", 1);
        state.setInteger("FluidQuantityVersion", 1);
        NBTTagList contents = new NBTTagList();
        if (material != null) {
            NBTTagCompound content = new NBTTagCompound();
            content.setString("Material", material.getRegistryName());
            content.setLong("Amount", GTValues.M);
            content.setBoolean("Molten", true);
            contents.appendTag(content);
        }
        state.setTag("Contents", contents);
        return state;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
