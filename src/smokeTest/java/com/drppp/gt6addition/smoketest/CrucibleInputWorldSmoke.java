package com.drppp.gt6addition.smoketest;

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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;

import java.util.UUID;

/** Opt-in full-registry input checks; never packaged in the production mod. */
final class CrucibleInputWorldSmoke {
    static int run(WorldServer world, MinecraftServer server) {
        // Host targets are mandatory; optional targets must never trigger addon registration.
        for (String target : new String[]{"adamantine", "hematite", "alumina", "rutile", "scheelite",
                "uraninite", "fluorite", "tantalite", "columbite", "naquadah", "dolamide"}) {
            LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_CRUSHING_TARGET target={} registered={}",
                    target, MetaTileEntityCrucible.resolveMaterial(target) != null);
            if (!java.util.Arrays.asList("adamantine", "fluorite", "columbite", "dolamide").contains(target)) {
                check(MetaTileEntityCrucible.resolveMaterial(target) != null, "Missing host crushing target: " + target);
            }
        }
        // MT.java:1972-2014 and Smeltery.onServerTickPost, not CEu recipe yields.
        input(world, 0, OrePrefix.ore, Materials.Coal, Materials.Coal, GTValues.M);
        input(world, 1, OrePrefix.ore, Materials.Sodalite, Materials.Sodalite, 5 * GTValues.M);
        input(world, 2, OrePrefix.ore, Materials.Malachite, Materials.Malachite, 5 * GTValues.M);
        input(world, 3, OrePrefix.ore, Materials.Lapis, Materials.Lapis, 5 * GTValues.M);
        input(world, 4, OrePrefix.ore, Materials.Apatite, Materials.Apatite, 4 * GTValues.M);
        input(world, 5, OrePrefix.ore, Materials.Cassiterite, Materials.Cassiterite, 2 * GTValues.M);
        input(world, 6, OrePrefix.ingot, Materials.Iron, Materials.Iron, GTValues.M);
        input(world, 7, OrePrefix.nugget, Materials.Iron, Materials.Iron, GTValues.M / 9);
        input(world, 8, OrePrefix.dustSmall, Materials.Iron, Materials.Iron, GTValues.M / 4);
        input(world, 9, OrePrefix.rawOre, Materials.Coal, Materials.Coal, GTValues.M);
        input(world, 10, OrePrefix.rawOre, Materials.Iron, Materials.BandedIron, 3 * GTValues.M);
        input(world, 11, OrePrefix.ore, Materials.Iron, Materials.BandedIron, 3 * GTValues.M);
        int optional = optionalInput(world, 12, OrePrefix.rawOre, "adamantium", "adamantine", 2 * GTValues.M);
        optional += optionalInput(world, 13, OrePrefix.ore, "columbite", "columbite", GTValues.M);
        optional += optionalInput(world, 14, OrePrefix.dust, "dolamide", "dolamide", GTValues.M);
        input(world, 15, OrePrefix.dust, Materials.BandedIron, Materials.BandedIron, GTValues.M);
        hematitePhase(world);
        hematiteReduction(world);
        refinement(world, 18, Materials.Iron, Materials.WroughtIron, 2011);
        refinement(world, 19, Materials.Copper, Materials.AnnealedCopper, 2800);
        // GT6 OP.blockRaw/crateGtRaw/crateGt64Raw and oreDense aliases do not
        // exist as CEu OrePrefix values. Exercise their actual OreDictionary
        // intake path with isolated vanilla stacks, including over-capacity
        // forms that must remain as dropped items.
        oreForm(world, 20, "blockRawCopper", new ItemStack(Items.STICK), Materials.Copper, 9 * GTValues.M);
        oreForm(world, 21, "blockOreCopper", new ItemStack(Items.PAPER), Materials.Copper, 9 * GTValues.M);
        oreForm(world, 22, "crateGtRawCopper", new ItemStack(Items.BOOK), Materials.Copper, 16 * GTValues.M);
        oreForm(world, 23, "crateGtOreCopper", new ItemStack(Items.SNOWBALL), Materials.Copper, 16 * GTValues.M);
        oreForm(world, 24, "oreDenseCopper", new ItemStack(Items.FEATHER), Materials.Copper, 2 * GTValues.M);
        oreForm(world, 25, "denseoreCopper", new ItemStack(Items.MAGMA_CREAM), Materials.Copper, 2 * GTValues.M);
        oreForm(world, 26, "crateGt64RawCopper", new ItemStack(Items.BLAZE_ROD), Materials.Copper, -1);
        oreForm(world, 27, "crateGt64OreCopper", new ItemStack(Items.LEATHER), Materials.Copper, -1);
        // Iron's explicit GT6 crushing target is hematite at 3 U per ore;
        // a 9-ore raw block exceeds this crucible's 16-U capacity and is refused.
        oreForm(world, 28, "blockRawIron", new ItemStack(Items.GUNPOWDER), Materials.BandedIron, -1);
        return 26 + optional;
    }

    private static int optionalInput(WorldServer world, int index, OrePrefix prefix, String sourceName,
                                     String targetName, long amount) {
        Material source = MetaTileEntityCrucible.resolveMaterial(sourceName);
        Material target = MetaTileEntityCrucible.resolveMaterial(targetName);
        if (source == null || target == null || OreDictUnifier.get(prefix, source).isEmpty()) {
            LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_OPTIONAL_INPUT unavailable source={} target={}",
                    sourceName, targetName);
            return 0;
        }
        input(world, index, prefix, source, target, amount);
        return 1;
    }

    private static void hematitePhase(WorldServer world) {
        MetaTileEntityCrucible vessel = place(world, 16,
                new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[PhaseSmoke]")));
        NBTTagCompound state = state(1206, 1206);
        NBTTagList contents = new NBTTagList();
        contents.appendTag(content(Materials.BandedIron, GTValues.M, false));
        state.setTag("Contents", contents);
        vessel.readFromNBT(state);
        vessel.update();
        check(!onlyContent(vessel).getBoolean("Molten"), "Hematite melted below 1207 K");
        NBTTagCompound hotter = vessel.writeToNBT(new NBTTagCompound());
        hotter.setLong("Temperature", 1207);
        hotter.setLong("OldTemperature", 1206);
        vessel.readFromNBT(hotter);
        vessel.update();
        check(onlyContent(vessel).getBoolean("Molten"), "Hematite did not melt at 1207 K");
        NBTTagCompound colder = vessel.writeToNBT(new NBTTagCompound());
        colder.setLong("Temperature", 1206);
        colder.setLong("OldTemperature", 1207);
        vessel.readFromNBT(colder);
        vessel.update();
        NBTTagCompound solid = onlyContent(vessel);
        check(!solid.getBoolean("Molten") && solid.getLong("Amount") == GTValues.M &&
                Materials.BandedIron.getRegistryName().equals(solid.getString("Material")),
                "Hematite cooling changed identity or quantity");
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_HEMATITE_PHASE passed");
    }

    private static void hematiteReduction(WorldServer world) {
        MetaTileEntityCrucible vessel = place(world, 17,
                new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[AlloySmoke]")));
        NBTTagCompound state = state(1811, 1810);
        NBTTagList contents = new NBTTagList();
        contents.appendTag(content(Materials.BandedIron, 5 * GTValues.M, true));
        contents.appendTag(content(Materials.Carbon, GTValues.M, false));
        contents.appendTag(content(Materials.Calcite, GTValues.M, true));
        state.setTag("Contents", contents);
        vessel.readFromNBT(state);
        vessel.update();
        NBTTagCompound iron = onlyContent(vessel);
        check(Materials.Iron.getRegistryName().equals(iron.getString("Material")) &&
                iron.getLong("Amount") == 2 * GTValues.M && iron.getBoolean("Molten"),
                "GT6 hematite/carbon/calcite 5:1:1 -> iron 2 failed: " + iron);
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_HEMATITE_REDUCTION passed");
    }

    private static NBTTagCompound state(long temperature, long previous) {
        NBTTagCompound state = new NBTTagCompound();
        state.setLong("Temperature", temperature);
        state.setLong("OldTemperature", previous);
        state.setInteger("WaterUnitVersion", 1);
        state.setInteger("FluidQuantityVersion", 1);
        return state;
    }

    private static void refinement(WorldServer world, int index, Material source, Material target, long threshold) {
        MetaTileEntityCrucible vessel = place(world, index,
                new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[RefineSmoke]")));
        NBTTagCompound state = state(threshold - 1, threshold - 1);
        NBTTagList contents = new NBTTagList();
        contents.appendTag(content(source, GTValues.M, true));
        state.setTag("Contents", contents);
        vessel.readFromNBT(state);
        vessel.update();
        check(source.getRegistryName().equals(onlyContent(vessel).getString("Material")),
                "Refinement occurred below GT6 threshold: " + source);
        NBTTagCompound hotter = vessel.writeToNBT(new NBTTagCompound());
        hotter.setLong("Temperature", threshold);
        hotter.setLong("OldTemperature", threshold - 1);
        vessel.readFromNBT(hotter);
        vessel.update();
        NBTTagCompound refined = onlyContent(vessel);
        check(target.getRegistryName().equals(refined.getString("Material")) &&
                refined.getLong("Amount") == GTValues.M && refined.getBoolean("Molten"),
                "GT6 refinement threshold or quantity is wrong: " + refined);
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_REFINEMENT source={} threshold={} passed",
                source, threshold);
    }

    private static NBTTagCompound content(Material material, long amount, boolean molten) {
        NBTTagCompound content = new NBTTagCompound();
        content.setString("Material", material.getRegistryName());
        content.setLong("Amount", amount);
        content.setBoolean("Molten", molten);
        return content;
    }

    private static NBTTagCompound onlyContent(MetaTileEntityCrucible vessel) {
        NBTTagList contents = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
        check(contents.tagCount() == 1, "Expected one component, found " + contents);
        return contents.getCompoundTagAt(0);
    }

    private static void input(WorldServer world, int index, OrePrefix prefix, Material source,
                              Material target, long expectedAmount) {
        ItemStack input = OreDictUnifier.get(prefix, source);
        check(!input.isEmpty(), "Missing registered input: " + prefix + "/" + source);
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[InputSmoke]"));
        MetaTileEntityCrucible vessel = place(world, index, player);
        IItemHandler items = vessel.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        check(items != null, "Input capability missing");
        NBTTagCompound before = vessel.writeToNBT(new NBTTagCompound());
        check(items.insertItem(0, input.copy(), true).isEmpty(), "Valid simulated input refused");
        check(before.equals(vessel.writeToNBT(new NBTTagCompound())), "Simulated item input mutated vessel");
        check(items.insertItem(0, input.copy(), false).isEmpty(), "Valid actual input refused");
        vessel.update();
        check(items.getStackInSlot(0).isEmpty(), "Input was not processed");
        NBTTagList contents = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
        check(contents.tagCount() == 1, "Input produced wrong component count: " + contents);
        NBTTagCompound content = contents.getCompoundTagAt(0);
        check(target.getRegistryName().toString().equals(content.getString("Material")),
                "Wrong input target for " + prefix + "/" + source + ": " + content);
        check(content.getLong("Amount") == expectedAmount,
                "Wrong GT6 quantity for " + prefix + "/" + source + ": " + content.getLong("Amount") +
                        ", expected " + expectedAmount);
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_INPUT prefix={} source={} amount={}",
                prefix, source, content.getLong("Amount"));
    }

    private static void oreForm(WorldServer world, int index, String oreName, ItemStack fixture,
                                Material expectedMaterial, long expectedAmount) {
        OreDictionary.registerOre(oreName, fixture.copy());
        BlockPos pos = new BlockPos(800 + 16 * index, 70, 400);
        MetaTileEntityCrucible vessel = place(world, index,
                new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[OreFormSmoke]")));
        EntityItem dropped = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, fixture.copy());
        check(world.spawnEntity(dropped), "Could not spawn OreDictionary fixture " + oreName);
        vessel.update();

        NBTTagList contents = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
        if (expectedAmount < 0) {
            check(!dropped.isDead && dropped.getItem().getCount() == 1,
                    "Over-capacity GT6 ore form was consumed: " + oreName);
            check(contents.tagCount() == 0, "Over-capacity GT6 ore form added partial contents: " + oreName);
            LogManager.getLogger("CrucibleParitySmoke").info(
                    "CRUCIBLE_WORLD_ORE_FORM oreName={} accepted=false retained=1 passed", oreName);
            dropped.setDead();
            return;
        }

        check(dropped.isDead || dropped.getItem().isEmpty(), "Accepted GT6 ore form was not consumed: " + oreName);
        check(contents.tagCount() == 1, "GT6 ore form produced wrong component count: " + oreName + " " + contents);
        NBTTagCompound content = contents.getCompoundTagAt(0);
        check(expectedMaterial.getRegistryName().toString().equals(content.getString("Material")),
                "Wrong GT6 ore-form target for " + oreName + ": " + content);
        check(content.getLong("Amount") == expectedAmount,
                "Wrong GT6 ore-form amount for " + oreName + ": " + content.getLong("Amount") +
                        ", expected " + expectedAmount);
        LogManager.getLogger("CrucibleParitySmoke").info(
                "CRUCIBLE_WORLD_ORE_FORM oreName={} accepted=true amount={} passed", oreName, expectedAmount);
    }

    private static MetaTileEntityCrucible place(WorldServer world, int index, FakePlayer player) {
        BlockPos pos = new BlockPos(800 + 16 * index, 70, 400);
        ItemStack machine = MetaTileEntityHandler.CRUCIBLE_HU[15].getStackForm();
        MachineItemBlock block = (MachineItemBlock) machine.getItem();
        check(world.isAirBlock(pos), "Input fixture position occupied");
        check(block.placeBlockAt(machine, player, world, pos, EnumFacing.UP, .5F, .5F, .5F,
                block.getBlock().getDefaultState()), "Input vessel placement failed");
        return (MetaTileEntityCrucible) GTUtility.getMetaTileEntity(world, pos);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
