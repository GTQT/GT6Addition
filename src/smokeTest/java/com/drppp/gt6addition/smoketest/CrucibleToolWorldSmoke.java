package com.drppp.gt6addition.smoketest;

import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.mojang.authlib.GameProfile;
import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.RecyclingHandler;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.recipes.ingredients.nbtmatch.NBTTagType;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.RecyclingData;
import gregtech.api.util.GTUtility;
import gregtech.common.crafting.ShapedOreEnergyTransferRecipe;
import gregtech.common.crafting.ToolHeadReplaceRecipe;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Exercises real registered recipes and transformed host lifecycle calls, not helper-only fixtures. */
final class CrucibleToolWorldSmoke {
    private static final String ACCOUNT = "gt6addition.electricMaterials";
    private final WorldServer world;
    private int nextPosition, cases;

    private CrucibleToolWorldSmoke(WorldServer world) { this.world = world; }

    static int run(WorldServer world) {
        CrucibleToolWorldSmoke test = new CrucibleToolWorldSmoke(world);
        test.damagedManualTool();
        Assembly craftedHull = test.craftedHull();
        Assembly machineHull = test.assembledHull(false);
        check(!craftedHull.materials.equals(machineHull.materials), "Different native hull manufacturing routes collapsed");
        test.blockedHullOutput();
        test.assembledHull(true);
        test.metadataAndLegacyProtection(craftedHull);
        Assembly lithium = null;
        int batteryIndex = 0;
        for (ItemStack battery : new ItemStack[]{MetaItems.BATTERY_LV_LITHIUM.getStackForm(),
                MetaItems.BATTERY_LV_CADMIUM.getStackForm(), MetaItems.BATTERY_LV_SODIUM.getStackForm()}) {
            Assembly filled = test.filledBattery(battery, batteryIndex++ == 1 ? machineHull : craftedHull);
            if (lithium == null) test.extractedHull(filled, craftedHull);
            Assembly power = test.powerUnit(filled);
            if (lithium == null) lithium = power;
        }
        check(lithium != null, "No registered power unit recipe was exercised");
        test.electricToolLifecycle(lithium);
        return test.cases;
    }

    private void damagedManualTool() {
        ItemStack tool = ToolItems.PICKAXE.get(Materials.Steel);
        IRecipe recipe = findRecipe(tool, null, false);
        InventoryCrafting grid = grid((ShapedOreRecipe) recipe, null);
        Map<String, Long> pristine = consumedMaterials(grid, null);
        tool = recipe.getCraftingResult(grid);
        check(!tool.isEmpty(), "Registered manual tool recipe returned empty");
        int maximum = tool.getMaxDamage();
        tool.setItemDamage(maximum / 2);
        recover(tool, scaled(pristine, maximum - tool.getItemDamage(), maximum), "DAMAGED_MANUAL_TOOL");
    }

    private Assembly powerUnit(Assembly manufacturedBattery) {
        ItemStack battery = manufacturedBattery.stack.copy();
        electric(battery).charge(1234, Integer.MAX_VALUE, true, false);
        IRecipe recipe = findRecipe(MetaItems.POWER_UNIT_LV.getStackForm(), battery, true);
        InventoryCrafting grid = grid((ShapedOreRecipe) recipe, battery);
        Map<String, Long> materials = consumedMaterials(grid, manufacturedBattery);
        ItemStack originalBattery = battery.copy();
        ItemStack output = recipe.getCraftingResult(grid);
        check(electric(output).getCharge() == 1234, "Power assembly did not transfer actual battery charge");
        check(electric(output).getMaxCharge() == electric(battery).getMaxCharge(), "Wrong assembled battery capacity");
        check(ItemStack.areItemStacksEqual(originalBattery, battery), "Crafting result changed supplied battery");
        checkAccount(output, materials);
        recover(output, materials, "POWER_UNIT_" + battery.getMetadata());
        return new Assembly(output, materials);
    }

    private Assembly craftedHull() {
        IRecipe recipe = findRecipe(MetaItems.BATTERY_HULL_LV.getStackForm(), null, false);
        InventoryCrafting inventory = grid((ShapedOreRecipe) recipe, null);
        Map<String, Long> expected = consumedMaterials(inventory, null);
        ItemStack result = recipe.getCraftingResult(inventory);
        check(result.hasTagCompound(), "Actual hull crafting did not capture account");
        check(account(result.getTagCompound().getCompoundTag("gt6addition.componentMaterials"), "materials").equals(expected),
                "Crafted hull differs from real consumed items");
        // Real ItemStack save/reload must retain the manufacturing route.
        result = new ItemStack(result.writeToNBT(new NBTTagCompound()));
        recover(result, expected, "CRAFTED_BATTERY_HULL");
        return new Assembly(result, expected);
    }

    private Assembly assembledHull(boolean parallel) {
        Recipe recipe = machineRecipe(RecipeMaps.ASSEMBLER_RECIPES, MetaItems.BATTERY_HULL_LV.getStackForm());
        List<ItemStack> inputs = machineInputs(recipe);
        List<FluidStack> fluids = machineFluids(recipe);
        Map<String, Long> expected = machineMaterials(inputs, fluids, null);
        int count = parallel ? 2 : 1;
        for (ItemStack input : inputs) input.setCount(input.getCount() * count);
        for (FluidStack fluid : fluids) fluid.amount *= count;
        MetaTileEntity machine = nativeMachine(MetaTileEntities.ASSEMBLER[GTValues.LV]);
        insertMachineInputs(machine, inputs, fluids);
        AbstractRecipeLogic logic = recipeLogic(machine);
        logic.setParallelLimit(count);
        check(logic.prepareRecipe(recipe), "Actual assembler failed to prepare native hull recipe");
        check(logic.getItemOutputs().size() == 1 && logic.getItemOutputs().get(0).getCount() == count,
                "Parallel hull output count did not match actual consumption");
        // Persist the in-progress account through the host's real trait NBT.
        NBTTagCompound save = logic.serializeNBT();
        logic.deserializeNBT(save.copy());
        ItemStack result = finishMachine(machine, logic);
        check(result.getCount() == count, "Actual assembler failed to deliver hull outputs");
        check(account(result.getTagCompound().getCompoundTag("gt6addition.componentMaterials"), "materials").equals(expected),
                "Assembler hull/parallel per-item materials were wrong");
        ItemStack unit = result.copy(); unit.setCount(1);
        recover(unit, expected, parallel ? "PARALLEL_ASSEMBLED_BATTERY_HULL" : "ASSEMBLED_BATTERY_HULL");
        return new Assembly(unit, expected);
    }

    private Assembly filledBattery(ItemStack template, Assembly hull) {
        Recipe recipe = machineRecipe(RecipeMaps.CANNER_RECIPES, template);
        List<ItemStack> inputs = machineInputs(recipe);
        for (int i = 0; i < inputs.size(); i++) {
            if (ItemStack.areItemsEqual(inputs.get(i), hull.stack)) inputs.set(i, hull.stack.copy());
        }
        List<FluidStack> fluids = machineFluids(recipe);
        Map<String, Long> expected = machineMaterials(inputs, fluids, hull);
        MetaTileEntity machine = nativeMachine(MetaTileEntities.CANNER[GTValues.LV]);
        insertMachineInputs(machine, inputs, fluids);
        AbstractRecipeLogic logic = recipeLogic(machine);
        Recipe found = RecipeMaps.CANNER_RECIPES.findRecipe(GTValues.V[GTValues.LV], machine.getImportItems(), machine.getImportFluids());
        check(found != null && ItemStack.areItemsEqual(found.getOutputs().get(0), template),
                "Actual canner indexed recipe search rejected tracked hull NBT");
        check(logic.prepareRecipe(found), "Actual canner failed native battery filling recipe: " + logic.getWhyFailed());
        ItemStack result = finishMachine(machine, logic);
        checkAccount(result, expected);
        recover(result, expected, "FILLED_BATTERY_" + template.getMetadata());
        return new Assembly(result, expected);
    }

    private void extractedHull(Assembly battery, Assembly hull) {
        Recipe recipe = RecipeMaps.EXTRACTOR_RECIPES.getRecipeList().stream()
                .filter(it -> it.getOutputs().size() == 1 && ItemStack.areItemsEqual(it.getOutputs().get(0), hull.stack) &&
                        it.getInputs().stream().anyMatch(input -> input.acceptsStack(battery.stack)))
                .findFirst().orElseThrow(() -> new AssertionError("Missing registered native battery extraction recipe"));
        MetaTileEntity machine = nativeMachine(MetaTileEntities.EXTRACTOR[GTValues.LV]);
        insertMachineInputs(machine, Collections.singletonList(battery.stack.copy()), Collections.emptyList());
        AbstractRecipeLogic logic = recipeLogic(machine);
        check(logic.prepareRecipe(recipe), "Extractor refused actual tracked battery");
        ItemStack result = finishMachine(machine, logic);
        check(account(result.getTagCompound().getCompoundTag("gt6addition.componentMaterials"), "materials").equals(hull.materials),
                "Extraction duplicated battery filler or changed original hull route");
        recover(result, hull.materials, "EXTRACTED_BATTERY_HULL");
    }

    private void blockedHullOutput() {
        Recipe recipe = machineRecipe(RecipeMaps.ASSEMBLER_RECIPES, MetaItems.BATTERY_HULL_LV.getStackForm());
        MetaTileEntity machine = nativeMachine(MetaTileEntities.ASSEMBLER[GTValues.LV]);
        insertMachineInputs(machine, machineInputs(recipe), machineFluids(recipe));
        check(machine.getExportItems().getSlots() == 1, "Output-space fixture requires actual single assembler slot");
        machine.getExportItems().setStackInSlot(0, MetaItems.BATTERY_HULL_LV.getStackForm(63));
        NBTTagCompound before = machine.writeToNBT(new NBTTagCompound());
        long energyBefore = machine.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null).getEnergyStored();
        AbstractRecipeLogic logic = recipeLogic(machine);
        check(!logic.prepareRecipe(recipe), "Plain legacy hull incorrectly merged with tracked hull");
        NBTTagCompound after = machine.writeToNBT(new NBTTagCompound());
        for (String key : new String[]{"ImportInventory", "ExportInventory", "ImportFluidInventory", "ExportFluidInventory"}) {
            check(before.getTag(key) != null && before.getTag(key).equals(after.getTag(key)), "Blocked output consumed " + key);
        }
        check(logic.getProgress() == 0 && logic.getItemOutputs().isEmpty(), "Blocked output started a manufacturing recipe");
        check(energyBefore == machine.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null).getEnergyStored(),
                "Blocked output consumed EU");
        passed("TRACKED_HULL_OUTPUT_SPACE_PRESERVED");
    }

    private void metadataAndLegacyProtection(Assembly hull) {
        ItemStack original = hull.stack.copy();
        GTRecipeInput ordinary = new GTRecipeItemInput(MetaItems.BATTERY_HULL_LV.getStackForm());
        check(ordinary.acceptsStack(hull.stack), "Ordinary input could not ignore own bookkeeping");
        GTRecipeInput explicit = new GTRecipeItemInput(MetaItems.BATTERY_HULL_LV.getStackForm())
                .setNBTMatchingCondition(NBTMatcher.EQUAL_TO, NBTCondition.create(NBTTagType.COMPOUND,
                        "gt6addition.componentMaterials", hull.stack.getTagCompound()
                                .getCompoundTag("gt6addition.componentMaterials").copy()));
        check(explicit.acceptsStack(hull.stack), "Explicit NBT matcher did not see original material account");
        explicit = new GTRecipeItemInput(MetaItems.BATTERY_HULL_LV.getStackForm())
                .setNBTMatchingCondition(NBTMatcher.EQUAL_TO, NBTCondition.create(NBTTagType.COMPOUND,
                        "gt6addition.componentMaterials", new NBTTagCompound()));
        check(!explicit.acceptsStack(hull.stack), "Explicit account constraint was bypassed");
        ItemStack payload = hull.stack.copy();
        payload.getTagCompound().setInteger("ThirdPartyPayload", 1);
        check(!ordinary.acceptsStack(payload), "Other mod's NBT was ignored by default input matching");
        check(RecipeMaps.CANNER_RECIPES.findRecipe(GTValues.V[GTValues.LV],
                java.util.Arrays.asList(payload, OreDictUnifier.get(OrePrefix.dust, Materials.Lithium, 2)),
                Collections.emptyList()) == null, "Recipe index ignored third-party NBT");
        ItemStack unrelated = new ItemStack(Items.STICK);
        unrelated.setTagCompound(new NBTTagCompound());
        unrelated.getTagCompound().setTag("gt6addition.componentMaterials", new NBTTagCompound());
        check(!new GTRecipeItemInput(new ItemStack(Items.STICK)).acceptsStack(unrelated),
                "Bookkeeping exemptions applied to unrelated item types");
        check(ItemStack.areItemStacksEqual(original, hull.stack), "Recipe matching changed original account/NBT");
        passed("BOOKKEEPING_NBT_MATCHING_SCOPE");

        for (int fault = 0; fault < 6; fault++) {
            ItemStack invalid = hull.stack.copy();
            NBTTagCompound data = invalid.getTagCompound().getCompoundTag("gt6addition.componentMaterials");
            switch (fault) {
                case 0: data.setInteger("version", 99); break;
                case 1: data.setString("item", "minecraft:stick"); break;
                case 2: data.setInteger("metadata", invalid.getMetadata() + 1); break;
                case 3: data.getTagList("materials", 10).getCompoundTagAt(0).setLong("amount", 0); break;
                case 4: data.getTagList("materials", 10).getCompoundTagAt(0).setString("material", "unknown:missing"); break;
                default: invalid.getTagCompound().setInteger("ThirdPartyPayload", 1); break;
            }
            reject(invalid);
        }
        reject(MetaItems.BATTERY_LV_LITHIUM.getStackForm());
        Recipe recipe = machineRecipe(RecipeMaps.CANNER_RECIPES, MetaItems.BATTERY_LV_LITHIUM.getStackForm());
        MetaTileEntity machine = nativeMachine(MetaTileEntities.CANNER[GTValues.LV]);
        // A legitimate legacy manufacturing recipe may run, but an unknown
        // hull history must not be rewritten as the workbench or assembler route.
        insertMachineInputs(machine, machineInputs(recipe), machineFluids(recipe));
        AbstractRecipeLogic logic = recipeLogic(machine);
        check(logic.prepareRecipe(recipe), "Legacy native battery filling recipe was changed");
        ItemStack unknown = finishMachine(machine, logic);
        check(unknown.hasTagCompound() && unknown.getTagCompound().hasKey(ACCOUNT, 10) &&
                unknown.getTagCompound().getCompoundTag(ACCOUNT).isEmpty(), "Unknown hull history became a guessed account");
        reject(unknown);
        IRecipe powerRecipe = findRecipe(MetaItems.POWER_UNIT_LV.getStackForm(), unknown, true);
        ItemStack unknownPower = powerRecipe.getCraftingResult(grid((ShapedOreRecipe) powerRecipe, unknown));
        check(unknownPower.getTagCompound().getCompoundTag(ACCOUNT).isEmpty(), "Invalid history regained a valid power account");
        reject(unknownPower);
        passed("INVALID_AND_LEGACY_COMPONENTS_PRESERVED");
    }

    private static Recipe machineRecipe(RecipeMap<?> map, ItemStack output) {
        return map.getRecipeList().stream()
                .filter(it -> it.getOutputs().size() == 1 && ItemStack.areItemsEqual(it.getOutputs().get(0), output))
                .findFirst().orElseThrow(() -> new AssertionError("Missing registered manufacturing recipe for " + output));
    }

    private static List<ItemStack> machineInputs(Recipe recipe) {
        List<ItemStack> result = new ArrayList<>();
        recipe.getInputs().forEach(input -> {
            ItemStack[] options = input.getInputStacks();
            check(options != null && options.length > 0, "Native machine ingredient has no real stack");
            ItemStack stack = options[0].copy(); stack.setCount(input.getAmount()); result.add(stack);
        });
        return result;
    }

    private static List<FluidStack> machineFluids(Recipe recipe) {
        List<FluidStack> result = new ArrayList<>();
        recipe.getFluidInputs().forEach(input -> result.add(input.getInputFluidStack().copy()));
        return result;
    }

    private static Map<String, Long> machineMaterials(List<ItemStack> inputs, List<FluidStack> fluids, Assembly known) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (ItemStack input : inputs) {
            Map<String, Long> unit = known != null && ItemStack.areItemsEqual(input, known.stack) ? known.materials : staticMaterials(input);
            unit.forEach((material, amount) -> result.merge(material, amount * input.getCount(), Long::sum));
        }
        fluids.forEach(fluid -> result.merge(gregtech.api.unification.FluidUnifier.getMaterialFromFluid(fluid.getFluid()).getRegistryName(),
                (long) GTValues.M * fluid.amount / GTValues.L, Long::sum));
        return result;
    }

    private void insertMachineInputs(MetaTileEntity machine, List<ItemStack> inputs, List<FluidStack> fluids) {
        for (int i = 0; i < inputs.size(); i++) machine.getImportItems().setStackInSlot(i, inputs.get(i).copy());
        for (FluidStack fluid : fluids) check(machine.getImportFluids().fill(fluid.copy(), true) == fluid.amount,
                "Native machine fluid fixture failed to fill");
        check(machine.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null) != null, "No native machine EU input");
        machine.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null).changeEnergy(1000000);
    }

    private static AbstractRecipeLogic recipeLogic(MetaTileEntity machine) {
        AbstractRecipeLogic logic = machine.getCapability(GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC, null);
        check(logic != null, "No actual host recipe logic");
        logic.setAllowOverclocking(false);
        logic.setBatchEnable(false);
        logic.setParallelLimit(1);
        logic.setCanRecipeProgress(true);
        return logic;
    }

    private static ItemStack finishMachine(MetaTileEntity machine, AbstractRecipeLogic logic) {
        for (int i = 0; i < 2000 && logic.getProgress() > 0; i++) {
            logic.setCanRecipeProgress(true);
            logic.update();
        }
        check(logic.getProgress() == 0, "Native recipe did not finish");
        ItemStack result = machine.getExportItems().getStackInSlot(0).copy();
        check(!result.isEmpty(), "Native manufacturing output was lost");
        return result;
    }

    private MetaTileEntity nativeMachine(MetaTileEntity template) {
        BlockPos pos = new BlockPos(2800 + 16 * nextPosition++, 70, 1000);
        ItemStack stack = template.getStackForm();
        MachineItemBlock block = (MachineItemBlock) stack.getItem();
        check(world.isAirBlock(pos), "Manufacturing fixture position occupied");
        check(block.placeBlockAt(stack, player(), world, pos, EnumFacing.UP, .5F, .5F, .5F,
                block.getBlock().getDefaultState()), "Native manufacturing machine placement failed");
        return GTUtility.getMetaTileEntity(world, pos);
    }

    private void electricToolLifecycle(Assembly power) {
        ItemStack template = ToolItems.SCREWDRIVER_LV.get(Materials.Steel);
        IRecipe recipe = findRecipe(template, null, true);
        InventoryCrafting grid = grid((ShapedOreRecipe) recipe, power.stack);
        Map<String, Long> all = consumedMaterials(grid, power);
        ItemStack fresh = recipe.getCraftingResult(grid);
        checkAccount(fresh, all);
        check(electric(fresh).getCharge() == electric(power.stack).getCharge(), "Tool assembly changed stored EU");
        check(electric(fresh).getMaxCharge() == electric(power.stack).getMaxCharge(), "Tool lost battery capacity");
        Map<String, Long> retained = account(fresh.getTagCompound().getCompoundTag(ACCOUNT), "powerMaterials");
        check(retained.equals(power.materials), "Tool account did not retain exact assembled power materials");

        ItemStack damaged = fresh.copy();
        int maximum = damaged.getMaxDamage();
        damaged.setItemDamage(maximum / 2);
        recover(damaged, scaled(all, maximum - damaged.getItemDamage(), maximum), "DAMAGED_ELECTRIC_TOOL");

        ItemStack head = OreDictUnifier.get(OrePrefix.toolHeadScrewdriver, Materials.Titanium);
        check(!head.isEmpty(), "Registered titanium screwdriver head is missing");
        IRecipe replacement = null;
        for (IRecipe candidate : ForgeRegistries.RECIPES) {
            if (candidate instanceof ToolHeadReplaceRecipe) { replacement = candidate; break; }
        }
        check(replacement != null, "Registered host replacement recipe is missing");
        InventoryCrafting swap = emptyGrid();
        swap.setInventorySlotContents(0, damaged.copy());
        swap.setInventorySlotContents(1, head.copy());
        check(replacement.matches(swap, world), "Actual two-input head replacement did not match");
        ItemStack newTool = replacement.getCraftingResult(swap);
        Map<String, Long> newMaterials = new LinkedHashMap<>(power.materials);
        add(newMaterials, staticMaterials(head));
        checkAccount(newTool, newMaterials);
        check(electric(newTool).getCharge() == electric(damaged).getCharge() &&
                electric(newTool).getMaxCharge() == electric(damaged).getMaxCharge(), "Head replacement changed energy");
        check(newTool.getItemDamage() == 0, "Replacement head was not pristine");
        recover(newTool, newMaterials, "REPLACED_ELECTRIC_HEAD");

        ItemStack exhausted = fresh.copy();
        exhausted.getTagCompound().getCompoundTag("GT.Tool").setInteger("MaxDurability", 1);
        exhausted.setItemDamage(exhausted.getMaxDamage()); // Host breaks only at damage > max.
        electric(exhausted).discharge(Long.MAX_VALUE, Integer.MAX_VALUE, true, false, false);
        ItemStack before = exhausted.copy();
        ItemStack craftingRemainder = exhausted.getItem().getContainerItem(exhausted);
        check(ItemStack.areItemsEqual(craftingRemainder, power.stack), "Crafting exhaustion did not return power unit");
        check(ItemStack.areItemStacksEqual(before, exhausted), "Host container call mutated original crafting tool");
        checkAccount(craftingRemainder, power.materials);
        check(electric(craftingRemainder).getCharge() == 0 &&
                electric(craftingRemainder).getMaxCharge() == electric(exhausted).getMaxCharge(),
                "Crafting break recreated EU or lost installed battery capacity");
        recover(craftingRemainder, power.materials, "CRAFTING_BROKEN_POWER_UNIT");

        FakePlayer player = player();
        // Invoke the real Forge destroy-item event entry, not our enrichment helper.
        ItemStack broken = fresh.copy();
        broken.setItemDamage(broken.getMaxDamage() + 1);
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        ForgeEventFactory.onPlayerDestroyItem(player, broken, EnumHand.MAIN_HAND);
        ItemStack returned = player.getHeldItemMainhand();
        check(ItemStack.areItemsEqual(returned, power.stack), "Destroy event did not return one power unit to hand");
        check(returned.getCount() == 1 && electric(returned).getCharge() == electric(broken).getCharge() &&
                electric(returned).getMaxCharge() == electric(broken).getMaxCharge(), "Broken power unit delivery/energy changed");
        checkAccount(returned, power.materials);
        recover(returned, power.materials, "PLAYER_BROKEN_POWER_UNIT");

        ItemStack invalid = power.stack.copy();
        invalid.getTagCompound().getCompoundTag(ACCOUNT).setString("item", "minecraft:stick");
        reject(invalid);
        invalid = fresh.copy();
        invalid.getTagCompound().getCompoundTag("GT.Tool").setString("Material", Materials.Titanium.getRegistryName());
        reject(invalid);
        passed("INVALID_ELECTRIC_ACCOUNT_PRESERVED");
    }

    private void recover(ItemStack input, Map<String, Long> expected, String name) {
        MetaTileEntityCrucible vessel = place();
        IItemHandler handler = items(vessel);
        NBTTagCompound before = vessel.writeToNBT(new NBTTagCompound());
        ItemStack original = input.copy();
        check(handler.insertItem(0, input, true).isEmpty(), "Simulation refused buffer for " + name);
        check(before.equals(vessel.writeToNBT(new NBTTagCompound())) && ItemStack.areItemStacksEqual(original, input),
                "Simulated input changed material/charge/NBT for " + name);
        check(handler.insertItem(0, input.copy(), false).isEmpty(), "Actual buffer insertion failed for " + name);
        vessel.update();
        long total = expected.values().stream().mapToLong(Long::longValue).sum();
        if (total > MetaTileEntityCrucible.getMaterialCapacity()) {
            check(vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10).isEmpty(), "Oversized tool was consumed");
            check(ItemStack.areItemStacksEqual(original, handler.getStackInSlot(0)), "Oversized item was lost");
        } else {
            check(handler.getStackInSlot(0).isEmpty(), "Valid tool/power unit remained unprocessed: " + name);
            Map<String, Long> actual = new LinkedHashMap<>();
            NBTTagList contents = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
            for (int i = 0; i < contents.tagCount(); i++) {
                NBTTagCompound entry = contents.getCompoundTagAt(i);
                check(entry.getInteger("FluidRemainder") == 0, "Unexpected fractional item recovery");
                actual.merge(entry.getString("Material"), entry.getLong("Amount"), Long::sum);
            }
            check(expected.equals(actual), "Wrong recovered materials for " + name + ": " + actual + " expected " + expected);
        }
        check(vessel.writeToNBT(new NBTTagCompound()).getLong("StoredHeat") == 0, "Stored EU became HU during " + name);
        check(ItemStack.areItemStacksEqual(original, input), "Insertion mutated original tool/power stack");
        passed(name);
    }

    private void reject(ItemStack input) {
        MetaTileEntityCrucible vessel = place();
        IItemHandler handler = items(vessel);
        check(handler.insertItem(0, input.copy(), false).isEmpty(), "Invalid item could not enter recovery buffer");
        vessel.update();
        check(ItemStack.areItemStacksEqual(input, handler.getStackInSlot(0)), "Invalid account item was consumed or modified");
        check(vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10).isEmpty(), "Invalid account created contents");
        check(vessel.getPendingItemCount() == 0, "Invalid account was silently queued");
    }

    private static IRecipe findRecipe(ItemStack output, ItemStack battery, boolean electric) {
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (!(recipe instanceof ShapedOreRecipe) || (recipe instanceof ShapedOreEnergyTransferRecipe) != electric ||
                    !ItemStack.areItemsEqual(output, recipe.getRecipeOutput())) continue;
            if (output.hasTagCompound() && output.getTagCompound().hasKey("GT.Tool", 10) &&
                    !output.getTagCompound().getCompoundTag("GT.Tool").getString("Material").equals(
                            recipe.getRecipeOutput().getTagCompound().getCompoundTag("GT.Tool").getString("Material"))) continue;
            if (battery != null && recipe.getIngredients().stream().noneMatch(it -> it.apply(battery))) continue;
            return recipe;
        }
        throw new AssertionError("Missing registered recipe for " + output + ", battery=" + battery);
    }

    private InventoryCrafting grid(ShapedOreRecipe recipe, ItemStack substitution) {
        InventoryCrafting grid = emptyGrid();
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            Ingredient ingredient = recipe.getIngredients().get(i);
            ItemStack[] alternatives = ingredient.getMatchingStacks();
            if (alternatives.length == 0) continue;
            ItemStack input = substitution != null && ingredient.apply(substitution) ? substitution.copy() : alternatives[0].copy();
            if (substitution == null || !ingredient.apply(substitution)) {
                // Real returned tools, rather than the host's first disposable
                // alternative which consumes an item with no recycling data.
                for (ItemStack alternative : alternatives) {
                    if (alternative.getItem().hasContainerItem(alternative.copy())) { input = alternative.copy(); break; }
                }
            }
            input.setCount(1);
            grid.setInventorySlotContents(i / recipe.getRecipeWidth() * 3 + i % recipe.getRecipeWidth(), input);
        }
        check(recipe.matches(grid, world), "Constructed inventory does not match actual recipe " + recipe.getRegistryName());
        return grid;
    }

    private static InventoryCrafting emptyGrid() {
        return new InventoryCrafting(new Container() {
            @Override public boolean canInteractWith(EntityPlayer player) { return true; }
        }, 3, 3);
    }

    private static Map<String, Long> consumedMaterials(InventoryCrafting grid, Assembly power) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (int i = 0; i < grid.getSizeInventory(); i++) {
            ItemStack input = grid.getStackInSlot(i);
            if (input.isEmpty() || input.getItem().hasContainerItem(input.copy())) continue;
            add(result, power != null && ItemStack.areItemsEqual(input, power.stack) ? power.materials : staticMaterials(input));
        }
        check(!result.isEmpty(), "Registered assembly had no consumable material data");
        return result;
    }

    private static Map<String, Long> staticMaterials(ItemStack input) {
        ItemStack unit = input.copy();
        unit.setCount(1);
        RecyclingData data = RecyclingHandler.getRecyclingIngredients(1,
                Collections.singletonList(new GTRecipeItemInput(unit)), null);
        check(data != null && !data.getMaterials().isEmpty(), "Missing real recycling data for " + input);
        Map<String, Long> result = new LinkedHashMap<>();
        for (MaterialStack component : data.getMaterials()) result.merge(component.material.getRegistryName(), component.amount, Long::sum);
        return result;
    }

    private static void add(Map<String, Long> target, Map<String, Long> source) {
        source.forEach((material, amount) -> target.merge(material, amount, Long::sum));
    }

    private static Map<String, Long> scaled(Map<String, Long> materials, int remaining, int maximum) {
        Map<String, Long> result = new LinkedHashMap<>();
        materials.forEach((material, amount) -> { long value = amount * remaining / maximum; if (value > 0) result.put(material, value); });
        return result;
    }

    private static void checkAccount(ItemStack stack, Map<String, Long> expected) {
        check(stack.hasTagCompound() && stack.getTagCompound().hasKey(ACCOUNT, 10), "Lifecycle did not capture materials: " + stack);
        NBTTagCompound data = stack.getTagCompound().getCompoundTag(ACCOUNT);
        check(data.getInteger("version") == 2 && data.getString("item").equals(stack.getItem().getRegistryName().toString()) &&
                data.getInteger("metadata") == stack.getMetadata(), "Account binding is missing/wrong");
        check(account(data, "materials").equals(expected), "Assembly account differs from actual consumed inputs: " + data + ", " + expected);
    }

    private static Map<String, Long> account(NBTTagCompound data, String key) {
        Map<String, Long> materials = new LinkedHashMap<>();
        NBTTagList list = data.getTagList(key, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            materials.merge(entry.getString("material"), entry.getLong("amount"), Long::sum);
        }
        return materials;
    }

    private static IElectricItem electric(ItemStack stack) {
        IElectricItem electric = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        check(electric != null, "Missing actual electric capability on " + stack);
        return electric;
    }

    private MetaTileEntityCrucible place() {
        BlockPos pos = new BlockPos(2800 + 16 * nextPosition++, 70, 1000);
        ItemStack machine = MetaTileEntityHandler.CRUCIBLE_HU[15].getStackForm();
        MachineItemBlock block = (MachineItemBlock) machine.getItem();
        check(world.isAirBlock(pos), "Tool recovery fixture position occupied");
        check(block.placeBlockAt(machine, player(), world, pos, EnumFacing.UP, .5F, .5F, .5F,
                block.getBlock().getDefaultState()), "Tool recovery vessel placement failed");
        return (MetaTileEntityCrucible) GTUtility.getMetaTileEntity(world, pos);
    }

    private FakePlayer player() { return new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[ToolSmoke]")); }

    private static IItemHandler items(MetaTileEntityCrucible vessel) {
        IItemHandler handler = vessel.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        check(handler != null, "Missing actual crucible item capability");
        return handler;
    }

    private void passed(String name) {
        cases++;
        LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_TOOL {} passed", name);
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }

    private static final class Assembly {
        final ItemStack stack;
        final Map<String, Long> materials;
        Assembly(ItemStack stack, Map<String, Long> materials) { this.stack = stack; this.materials = materials; }
    }
}
