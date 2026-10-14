package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Forge-world audit of the production GT6 alloy matcher and commit path. */
public final class CrucibleGt6AlloyWorldSmoke {
    private static final Logger LOG = LogManager.getLogger("CrucibleParitySmoke");
    private static final BigInteger GT6_U = BigInteger.valueOf(GT6AlloyRecipes.GT6_U);

    private CrucibleGt6AlloyWorldSmoke() {}

    public static int run(MetaTileEntityCrucible vessel) {
        try {
            Field contentsField = MetaTileEntityCrucible.class.getDeclaredField("contents");
            contentsField.setAccessible(true);
            Class<?> matchType = Class.forName(MetaTileEntityCrucible.class.getName() + "$AlloyMatch");
            Method findMatch = MetaTileEntityCrucible.class.getDeclaredMethod(
                    "findBestGt6RecipeMatch", Set.class, List.class);
            findMatch.setAccessible(true);
            Method applyMatch = MetaTileEntityCrucible.class.getDeclaredMethod("applyAlloy", matchType);
            applyMatch.setAccessible(true);
            Method processAlloys = MetaTileEntityCrucible.class.getDeclaredMethod("processAlloys");
            processAlloys.setAccessible(true);
            Method getMeltingTemperature = MetaTileEntityCrucible.class.getDeclaredMethod(
                    "getMeltingTemperature", Material.class);
            getMeltingTemperature.setAccessible(true);
            Field capacityField = MetaTileEntityCrucible.class.getDeclaredField("CAPACITY");
            capacityField.setAccessible(true);
            long capacity = capacityField.getLong(null);

            List<GT6AlloyRecipes> recipes = GT6AlloyRecipes.getRecipes();
            check(recipes.size() == 147, "Expected 147 authoritative GT6 alloy rows, got " + recipes.size());
            int checked = 0;
            int converted = 0;
            int unavailableMaterials = 0;
            int checks = 1;
            List<String> unavailable = new ArrayList<>();

            for (int recipeIndex = 0; recipeIndex < recipes.size(); recipeIndex++) {
                GT6AlloyRecipes recipe = recipes.get(recipeIndex);
                Material output = MetaTileEntityCrucible.resolveMaterial(recipe.getOutputName());
                String[] inputNames = recipe.getInputNames();
                long[] inputUnits = recipe.getInputUnits();
                check(inputNames.length > 0 && inputNames.length == inputUnits.length &&
                                recipe.getOutputUnits() > 0,
                        "Malformed GT6 alloy row index=" + recipeIndex + " output=" + recipe.getOutputName());
                checks++;

                List<Material> inputMaterials = new ArrayList<>();
                List<Long> aggregatedCoefficients = new ArrayList<>();
                BigInteger totalInputUnits = BigInteger.ZERO;
                String missing = output == null || output == Materials.NULL ?
                        "output=" + recipe.getOutputName() : null;
                for (int i = 0; i < inputNames.length; i++) {
                    check(inputUnits[i] > 0, "Non-positive GT6 alloy input row=" + recipeIndex + " index=" + i);
                    checks++;
                    Material input = MetaTileEntityCrucible.resolveMaterial(inputNames[i]);
                    if (input == null || input == Materials.NULL) {
                        if (missing == null) missing = "input=" + inputNames[i];
                        continue;
                    }
                    int existing = identityIndex(inputMaterials, input);
                    if (existing < 0) {
                        inputMaterials.add(input);
                        aggregatedCoefficients.add(inputUnits[i]);
                    } else {
                        aggregatedCoefficients.set(existing,
                                Math.addExact(aggregatedCoefficients.get(existing), inputUnits[i]));
                    }
                    totalInputUnits = totalInputUnits.add(BigInteger.valueOf(inputUnits[i]));
                }
                if (missing != null) {
                    unavailableMaterials++;
                    unavailable.add(recipeIndex + ":" + recipe.getOutputName() + "(" + missing + ")");
                    continue;
                }

                // Keep each fixture below 7/8 of the live shared capacity to
                // leave room for one occupancy unit per fractional stack,
                // while exercising fractional conversions for large recipes.
                BigInteger capacityBudget = BigInteger.valueOf(capacity).multiply(BigInteger.valueOf(7))
                        .divide(BigInteger.valueOf(8));
                BigInteger conversionBudget = GT6_U.multiply(capacityBudget)
                        .divide(BigInteger.valueOf(GTValues.M).multiply(totalInputUnits));
                long requestedConversions = conversionBudget.compareTo(GT6_U) >= 0 ?
                        GT6AlloyRecipes.GT6_U : conversionBudget.longValueExact();
                check(requestedConversions > 0,
                        "No representable in-capacity conversion for GT6 alloy row=" + recipeIndex);
                checks++;

                LinkedHashMap<Material, CrucibleFluidUnits.Quantity> fixtureInputs = new LinkedHashMap<>();
                long fixtureTemperature = 1L;
                for (int i = 0; i < inputMaterials.size(); i++) {
                    Material material = inputMaterials.get(i);
                    long atoms = Math.multiplyExact(aggregatedCoefficients.get(i), requestedConversions);
                    CrucibleFluidUnits.Quantity amount = CrucibleFluidUnits.fromGt6Atoms(atoms);
                    check(amount != null && (amount.amount > 0 || amount.remainder > 0) &&
                                    CrucibleFluidUnits.gt6AtomicAmount(CrucibleFluidUnits.storedNumerator(
                                            amount.amount, amount.remainder)) == atoms,
                            "GT6 alloy fixture amount was not exact row=" + recipeIndex +
                                    " material=" + material.getRegistryName());
                    fixtureInputs.put(material, amount);
                    fixtureTemperature = Math.max(fixtureTemperature,
                            (long) (Integer) getMeltingTemperature.invoke(vessel, material) + 1L);
                    checks++;
                }
                fixtureTemperature = Math.max(fixtureTemperature,
                        (long) (Integer) getMeltingTemperature.invoke(vessel, output) + 1L);

                NBTTagCompound state = vessel.writeToNBT(new NBTTagCompound());
                state.setLong("Temperature", fixtureTemperature);
                state.setLong("OldTemperature", fixtureTemperature);
                state.setInteger("FluidQuantityVersion", 3);
                state.setInteger("WaterUnitVersion", 1);
                NBTTagList inputContents = new NBTTagList();
                for (Map.Entry<Material, CrucibleFluidUnits.Quantity> entry : fixtureInputs.entrySet()) {
                    NBTTagCompound item = new NBTTagCompound();
                    item.setString("Material", entry.getKey().getRegistryName());
                    item.setLong("Amount", entry.getValue().amount);
                    item.setLong("FluidRemainder", entry.getValue().remainder);
                    item.setBoolean("Molten", true);
                    inputContents.appendTag(item);
                }
                state.setTag("Contents", inputContents);
                vessel.readFromNBT(state);

                Set<Material> matchedOutputs = Collections.newSetFromMap(new IdentityHashMap<Material, Boolean>());
                Object match = findMatch.invoke(vessel, matchedOutputs, Collections.singletonList(recipe));
                check(match != null, "Production GT6 alloy matcher rejected registered row=" + recipeIndex +
                        " output=" + output.getRegistryName());
                Field matchOutput = matchType.getDeclaredField("alloy");
                Field matchConversions = matchType.getDeclaredField("conversions");
                matchOutput.setAccessible(true);
                matchConversions.setAccessible(true);
                check(matchOutput.get(match) == output,
                        "Production GT6 alloy matcher changed output identity row=" + recipeIndex);
                long conversions = matchConversions.getLong(match);
                long expectedConversions = Long.MAX_VALUE;
                for (int i = 0; i < inputNames.length; i++) {
                    Material input = MetaTileEntityCrucible.resolveMaterial(inputNames[i]);
                    CrucibleFluidUnits.Quantity stored = fixtureInputs.get(input);
                    long availableAtoms = CrucibleFluidUnits.gt6AtomicAmount(CrucibleFluidUnits.storedNumerator(
                            stored.amount, stored.remainder));
                    expectedConversions = Math.min(expectedConversions, availableAtoms / inputUnits[i]);
                }
                check(conversions > 0 && conversions == expectedConversions,
                        "Production GT6 alloy matcher returned invalid conversions row=" + recipeIndex +
                                " expected=" + expectedConversions + " actual=" + conversions);
                checks += 3;

                Map<String, BigInteger> expectedContents = new LinkedHashMap<>();
                for (Map.Entry<Material, CrucibleFluidUnits.Quantity> entry : fixtureInputs.entrySet()) {
                    BigInteger amount = CrucibleFluidUnits.storedNumerator(
                            entry.getValue().amount, entry.getValue().remainder);
                    expectedContents.put(String.valueOf(entry.getKey().getRegistryName()), amount);
                }
                for (int i = 0; i < inputNames.length; i++) {
                    Material input = MetaTileEntityCrucible.resolveMaterial(inputNames[i]);
                    CrucibleFluidUnits.Quantity consumed = CrucibleFluidUnits.fromGt6Atoms(
                            Math.multiplyExact(inputUnits[i], conversions));
                    String key = String.valueOf(input.getRegistryName());
                    BigInteger remaining = expectedContents.get(key).subtract(CrucibleFluidUnits.storedNumerator(
                            consumed.amount, consumed.remainder));
                    // Production removal clamps at available stock. Preserve that
                    // exact host behavior for colliding resolved input identities.
                    expectedContents.put(key, remaining.signum() < 0 ? BigInteger.ZERO : remaining);
                }
                CrucibleFluidUnits.Quantity expectedOutput = CrucibleFluidUnits.fromGt6Atoms(
                        Math.multiplyExact(recipe.getOutputUnits(), conversions));
                String outputKey = String.valueOf(output.getRegistryName());
                expectedContents.merge(outputKey, CrucibleFluidUnits.storedNumerator(
                        expectedOutput.amount, expectedOutput.remainder), BigInteger::add);
                expectedContents.values().removeIf(value -> value.signum() == 0);

                applyMatch.invoke(vessel, match);
                NBTTagList result = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
                Map<String, BigInteger> actualContents = new LinkedHashMap<>();
                for (int i = 0; i < result.tagCount(); i++) {
                    NBTTagCompound item = result.getCompoundTagAt(i);
                    actualContents.merge(item.getString("Material"), CrucibleFluidUnits.storedNumerator(
                            item.getLong("Amount"), item.getLong("FluidRemainder")), BigInteger::add);
                }
                check(actualContents.equals(expectedContents),
                        "Production GT6 alloy input/output commit mismatch row=" + recipeIndex +
                                " output=" + output.getRegistryName() + " expected=" + expectedContents +
                                " actual=" + actualContents);
                checked++;
                converted++;
                checks++;
            }

            check(checked + unavailableMaterials == recipes.size(),
                    "GT6 alloy runtime audit did not account for every row");
            checks++;
            LOG.info("CRUCIBLE_WORLD_GT6_ALLOY_COMMITS passed rows={} executed={} unavailableMaterials={} checks={}",
                    recipes.size(), converted, unavailableMaterials, checks);
            for (String row : unavailable) LOG.info("CRUCIBLE_WORLD_GT6_ALLOY_UNAVAILABLE {}", row);
            checks += verifyGlobalPriority(vessel, processAlloys, getMeltingTemperature, capacity);
            return checks;
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Could not invoke the production GT6 alloy path", failure);
        }
    }

    /**
     * Runs competing recipes through the actual per-tick production selector.
     * Cu/Ag/Au simultaneously enable annealed copper (3 U), sterling silver
     * (5 U), rose gold (5 U), and electrum (8 U); GT6 must select the greatest
     * commonDivider * conversions yield and commit only that one reaction.
     */
    private static int verifyGlobalPriority(MetaTileEntityCrucible vessel, Method processAlloys,
                                            Method getMeltingTemperature, long capacity)
            throws ReflectiveOperationException {
        String[] inputNames = {"copper", "silver", "gold"};
        long[] inputUnits = {3L, 4L, 4L};
        Map<String, Material> inputs = new LinkedHashMap<>();
        long totalAmount = 0L;
        long temperature = 0L;
        NBTTagList contents = new NBTTagList();
        int checks = 0;

        for (int i = 0; i < inputNames.length; i++) {
            Material material = MetaTileEntityCrucible.resolveMaterial(inputNames[i]);
            check(material != null && material != Materials.NULL,
                    "Missing exact host material for global alloy priority fixture: " + inputNames[i]);
            CrucibleFluidUnits.Quantity amount = CrucibleFluidUnits.fromGt6Atoms(
                    Math.multiplyExact(inputUnits[i], GT6_U.longValueExact()));
            check(amount != null && amount.remainder == 0L,
                    "Global alloy priority fixture is not exactly representable: " + inputNames[i]);
            totalAmount = Math.addExact(totalAmount, amount.amount);
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Material", String.valueOf(material.getRegistryName()));
            entry.setLong("Amount", amount.amount);
            entry.setLong("FluidRemainder", amount.remainder);
            entry.setBoolean("Molten", true);
            contents.appendTag(entry);
            inputs.put(inputNames[i], material);
            temperature = Math.max(temperature,
                    (long) (Integer) getMeltingTemperature.invoke(vessel, material) + 1L);
            checks += 3;
        }

        String[] candidateOutputs = {"annealed_copper", "sterling_silver", "rose_gold", "electrum"};
        Map<String, Material> outputs = new LinkedHashMap<>();
        for (String name : candidateOutputs) {
            Material output = MetaTileEntityCrucible.resolveMaterial(name);
            check(output != null && output != Materials.NULL,
                    "Missing exact host alloy output for global priority fixture: " + name);
            outputs.put(name, output);
            temperature = Math.max(temperature,
                    (long) (Integer) getMeltingTemperature.invoke(vessel, output) + 1L);
            checks += 2;
        }
        Map<String, Long> candidateYields = new LinkedHashMap<>();
        for (String name : candidateOutputs) candidateYields.put(name, 0L);
        for (GT6AlloyRecipes recipe : GT6AlloyRecipes.getRecipes()) {
            if (!candidateYields.containsKey(recipe.getOutputName())) continue;
            long conversions = Long.MAX_VALUE;
            for (int i = 0; i < recipe.getInputNames().length; i++) {
                long available = 0L;
                for (int j = 0; j < inputNames.length; j++) {
                    if (inputNames[j].equals(recipe.getInputNames()[i])) available = inputUnits[j] * GT6_U.longValueExact();
                }
                if (available == 0L) {
                    conversions = 0L;
                    break;
                }
                conversions = Math.min(conversions, available / recipe.getInputUnits()[i]);
            }
            if (conversions > 0L) {
                candidateYields.put(recipe.getOutputName(), Math.max(candidateYields.get(recipe.getOutputName()),
                        Math.multiplyExact(recipe.getOutputUnits(), conversions)));
            }
        }
        Map<String, Long> expectedCandidateYields = new LinkedHashMap<>();
        expectedCandidateYields.put("annealed_copper", 3L * GT6_U.longValueExact());
        expectedCandidateYields.put("sterling_silver", 5L * GT6_U.longValueExact());
        expectedCandidateYields.put("rose_gold", 5L * GT6_U.longValueExact());
        expectedCandidateYields.put("electrum", 8L * GT6_U.longValueExact());
        check(candidateYields.equals(expectedCandidateYields),
                "Unexpected competing GT6 alloy candidates/GT6-unit yields: " + candidateYields);
        checks++;
        check(totalAmount <= capacity, "Global alloy priority fixture exceeds crucible capacity");
        checks++;

        NBTTagCompound state = vessel.writeToNBT(new NBTTagCompound());
        state.setLong("Temperature", temperature);
        state.setLong("OldTemperature", temperature);
        state.setInteger("FluidQuantityVersion", 3);
        state.setInteger("WaterUnitVersion", 1);
        state.setTag("Contents", contents);
        vessel.readFromNBT(state);
        processAlloys.invoke(vessel);

        Map<String, BigInteger> expected = new LinkedHashMap<>();
        CrucibleFluidUnits.Quantity remainingCopper = CrucibleFluidUnits.fromGt6Atoms(3L * GT6_U.longValueExact());
        CrucibleFluidUnits.Quantity electrum = CrucibleFluidUnits.fromGt6Atoms(8L * GT6_U.longValueExact());
        expected.put(String.valueOf(inputs.get("copper").getRegistryName()),
                CrucibleFluidUnits.storedNumerator(remainingCopper.amount, remainingCopper.remainder));
        expected.put(String.valueOf(outputs.get("electrum").getRegistryName()),
                CrucibleFluidUnits.storedNumerator(electrum.amount, electrum.remainder));

        NBTTagList result = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
        Map<String, BigInteger> actual = new LinkedHashMap<>();
        for (int i = 0; i < result.tagCount(); i++) {
            NBTTagCompound entry = result.getCompoundTagAt(i);
            actual.merge(entry.getString("Material"), CrucibleFluidUnits.storedNumerator(
                    entry.getLong("Amount"), entry.getLong("FluidRemainder")), BigInteger::add);
        }
        check(actual.equals(expected), "GT6 global alloy priority/one-per-tick mismatch: expected=" +
                expected + " actual=" + actual);
        checks++;
        LOG.info("CRUCIBLE_WORLD_GT6_ALLOY_PRIORITY passed selected={} yieldGt6Units=8 oneCommit=true checks={}",
                outputs.get("electrum").getRegistryName(), checks);
        return checks;
    }

    /**
     * Exercises every pair of GT6 recipes whose exact material identities are
     * present in this host. Both recipes are made feasible in the same vessel;
     * where the full catalogue has a unique highest-yield candidate, the real
     * production matcher must choose that candidate. Equal-yield cases are
     * accounted separately because GT6's source reference set is a HashSet and
     * does not define a portable tie order.
     */
    private static int verifyPairwiseGlobalSelection(MetaTileEntityCrucible vessel, Method findMatch,
                                                      Class<?> matchType, List<GT6AlloyRecipes> recipes,
                                                      long capacity) throws ReflectiveOperationException {
        Map<String, Material> materialBindings = new HashMap<>();
        List<ResolvedRecipe> availableRows = new ArrayList<>();
        int checks = 0;
        for (GT6AlloyRecipes recipe : recipes) {
            Material output = resolveCached(recipe.getOutputName(), materialBindings);
            String[] inputNames = recipe.getInputNames();
            long[] inputUnits = recipe.getInputUnits();
            Material[] inputs = new Material[inputNames.length];
            boolean available = output != null && output != Materials.NULL && inputNames.length > 0 &&
                    inputNames.length == inputUnits.length && recipe.getOutputUnits() > 0;
            for (int i = 0; i < inputNames.length && available; i++) {
                inputs[i] = resolveCached(inputNames[i], materialBindings);
                available = inputs[i] != null && inputs[i] != Materials.NULL && inputUnits[i] > 0;
            }
            if (available) availableRows.add(new ResolvedRecipe(recipe, output, inputs, inputUnits));
        }
        check(availableRows.size() >= 2,
                "Need at least two exact host GT6 alloy rows for pairwise priority audit");
        checks++;

        BigInteger capacityBudget = BigInteger.valueOf(capacity).multiply(BigInteger.valueOf(7))
                .divide(BigInteger.valueOf(8));
        BigInteger maxStoredNumerator = BigInteger.valueOf(capacity)
                .multiply(BigInteger.valueOf(CrucibleFluidUnits.STORAGE_UNIT));
        int pairs = 0;
        int uniqueScenarios = 0;
        int tiedScenarios = 0;
        int unrepresentableScenarios = 0;
        Field matchOutput = matchType.getDeclaredField("alloy");
        Field matchConversions = matchType.getDeclaredField("conversions");
        matchOutput.setAccessible(true);
        matchConversions.setAccessible(true);

        for (int firstIndex = 0; firstIndex < availableRows.size(); firstIndex++) {
            ResolvedRecipe first = availableRows.get(firstIndex);
            for (int secondIndex = firstIndex + 1; secondIndex < availableRows.size(); secondIndex++) {
                ResolvedRecipe second = availableRows.get(secondIndex);
                pairs++;

                List<Material> fixtureMaterials = new ArrayList<>();
                List<Long> fixtureCoefficients = new ArrayList<>();
                appendComponents(first, fixtureMaterials, fixtureCoefficients);
                appendComponents(second, fixtureMaterials, fixtureCoefficients);
                BigInteger totalInputUnits = BigInteger.ZERO;
                for (Long coefficient : fixtureCoefficients) {
                    totalInputUnits = totalInputUnits.add(BigInteger.valueOf(coefficient));
                }
                BigInteger conversionBudget = GT6_U.multiply(capacityBudget)
                        .divide(BigInteger.valueOf(GTValues.M).multiply(totalInputUnits));
                long fixtureConversions = conversionBudget.compareTo(GT6_U) >= 0 ?
                        GT6AlloyRecipes.GT6_U : conversionBudget.longValueExact();
                if (fixtureConversions <= 0) {
                    unrepresentableScenarios++;
                    continue;
                }

                IdentityHashMap<Material, Long> storedAtoms = new IdentityHashMap<>();
                BigInteger occupied = BigInteger.ZERO;
                NBTTagList fixtureContents = new NBTTagList();
                for (int i = 0; i < fixtureMaterials.size(); i++) {
                    Material material = fixtureMaterials.get(i);
                    long atoms = Math.multiplyExact(fixtureCoefficients.get(i), fixtureConversions);
                    CrucibleFluidUnits.Quantity amount = CrucibleFluidUnits.fromGt6Atoms(atoms);
                    check(amount != null && CrucibleFluidUnits.gt6AtomicAmount(
                                    CrucibleFluidUnits.storedNumerator(amount.amount, amount.remainder)) == atoms,
                            "Pairwise GT6 fixture quantity is not exact: " + material.getRegistryName());
                    storedAtoms.put(material, atoms);
                    occupied = occupied.add(CrucibleFluidUnits.storedNumerator(amount.amount, amount.remainder));
                    NBTTagCompound entry = new NBTTagCompound();
                    entry.setString("Material", String.valueOf(material.getRegistryName()));
                    entry.setLong("Amount", amount.amount);
                    entry.setLong("FluidRemainder", amount.remainder);
                    entry.setBoolean("Molten", true);
                    fixtureContents.appendTag(entry);
                    checks++;
                }
                check(occupied.compareTo(maxStoredNumerator) <= 0,
                        "Pairwise GT6 fixture exceeds vessel capacity");
                checks++;

                ResolvedRecipe winner = null;
                long winnerConversions = 0L;
                long winnerYield = -1L;
                boolean tiedForBest = false;
                boolean firstFeasible = false;
                boolean secondFeasible = false;
                for (ResolvedRecipe candidate : availableRows) {
                    long conversions = availableConversions(candidate, storedAtoms);
                    if (conversions <= 0) continue;
                    if (candidate == first) firstFeasible = true;
                    if (candidate == second) secondFeasible = true;
                    long yield = Math.multiplyExact(candidate.recipe.getOutputUnits(), conversions);
                    if (yield > winnerYield) {
                        winner = candidate;
                        winnerConversions = conversions;
                        winnerYield = yield;
                        tiedForBest = false;
                    } else if (yield == winnerYield) {
                        tiedForBest = true;
                    }
                }
                check(firstFeasible && secondFeasible,
                        "Pairwise fixture did not enable both target rows: " +
                                first.recipe.getOutputName() + " vs " + second.recipe.getOutputName());
                checks++;
                if (tiedForBest) {
                    tiedScenarios++;
                    continue;
                }
                check(winner != null && winnerYield > 0,
                        "Pairwise fixture has no positive-yield winner");
                checks++;

                NBTTagCompound state = vessel.writeToNBT(new NBTTagCompound());
                state.setLong("Temperature", Integer.MAX_VALUE);
                state.setLong("OldTemperature", Integer.MAX_VALUE);
                state.setInteger("FluidQuantityVersion", 3);
                state.setInteger("WaterUnitVersion", 1);
                state.setTag("Contents", fixtureContents);
                vessel.readFromNBT(state);

                Set<Material> matchedOutputs = Collections.newSetFromMap(new IdentityHashMap<Material, Boolean>());
                Object actualMatch = findMatch.invoke(vessel, matchedOutputs, recipes);
                check(actualMatch != null && matchOutput.get(actualMatch) == winner.output &&
                                matchConversions.getLong(actualMatch) == winnerConversions,
                        "Pairwise GT6 selector chose the wrong unique-yield winner: expected=" +
                                winner.recipe.getOutputName() + "×" + winnerConversions +
                                " actual=" + (actualMatch == null ? "none" :
                                matchOutput.get(actualMatch) + "×" + matchConversions.getLong(actualMatch)));
                uniqueScenarios++;
                checks++;
            }
        }

        int expectedPairs = availableRows.size() * (availableRows.size() - 1) / 2;
        check(pairs == expectedPairs && uniqueScenarios + tiedScenarios + unrepresentableScenarios == pairs,
                "GT6 pairwise priority audit failed to account for all executable row pairs");
        check(uniqueScenarios > 0, "GT6 pairwise audit produced no unique-yield scenarios");
        checks += 2;
        LOG.info("CRUCIBLE_WORLD_GT6_ALLOY_PAIRWISE passed rows={} pairs={} unique={} tied={} unrepresentable={} checks={}",
                availableRows.size(), pairs, uniqueScenarios, tiedScenarios, unrepresentableScenarios, checks);
        return checks;
    }

    private static Material resolveCached(String name, Map<String, Material> bindings) {
        if (!bindings.containsKey(name)) bindings.put(name, MetaTileEntityCrucible.resolveMaterial(name));
        return bindings.get(name);
    }

    private static void appendComponents(ResolvedRecipe recipe, List<Material> materials,
                                         List<Long> coefficients) {
        for (int i = 0; i < recipe.inputs.length; i++) {
            int index = identityIndex(materials, recipe.inputs[i]);
            if (index < 0) {
                materials.add(recipe.inputs[i]);
                coefficients.add(recipe.inputUnits[i]);
            } else {
                coefficients.set(index, Math.addExact(coefficients.get(index), recipe.inputUnits[i]));
            }
        }
    }

    private static long availableConversions(ResolvedRecipe recipe, IdentityHashMap<Material, Long> storedAtoms) {
        long conversions = Long.MAX_VALUE;
        for (int i = 0; i < recipe.inputs.length; i++) {
            Long available = storedAtoms.get(recipe.inputs[i]);
            if (available == null) return 0L;
            conversions = Math.min(conversions, available / recipe.inputUnits[i]);
        }
        return conversions == Long.MAX_VALUE ? 0L : conversions;
    }

    private static final class ResolvedRecipe {
        private final GT6AlloyRecipes recipe;
        private final Material output;
        private final Material[] inputs;
        private final long[] inputUnits;

        private ResolvedRecipe(GT6AlloyRecipes recipe, Material output, Material[] inputs, long[] inputUnits) {
            this.recipe = recipe;
            this.output = output;
            this.inputs = inputs;
            this.inputUnits = inputUnits;
        }
    }

    private static int identityIndex(List<Material> materials, Material target) {
        for (int i = 0; i < materials.size(); i++) if (materials.get(i) == target) return i;
        return -1;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
