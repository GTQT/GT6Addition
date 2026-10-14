package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Runtime registration audit for every positive-ID GT6 material conversion row. */
public final class CrucibleGt6LiteralBindingWorldSmoke {
    private static final Logger LOG = LogManager.getLogger("CrucibleParitySmoke");

    private CrucibleGt6LiteralBindingWorldSmoke() {}

    public static int run() {
        int boundSources = 0;
        int missingSources = 0;
        int hotTargetsChecked = 0;
        int coldTargetsChecked = 0;
        int quantitiesChecked = 0;
        int missingHotTargets = 0;
        int missingColdTargets = 0;
        int checks = 0;
        List<String> missingSourceNames = new ArrayList<>();
        List<String> missingTargetNames = new ArrayList<>();

        for (GT6LiteralTargetRows.Row row : GT6LiteralTargetRows.ROWS) {
            GT6LiteralTargetData.Profile profile = GT6LiteralTargetData.findById(row.id);
            check(profile != null, "Missing literal target profile id=" + row.id);
            String expectedMechanicsName = GT6MaterialIdentity.mechanicsName(row.id);
            check(expectedMechanicsName != null && expectedMechanicsName.equals(profile.mechanicsName),
                    "GT6 numeric identity no longer matches target row id=" + row.id +
                            " source=" + row.sourceName);
            checks += 2;

            Material source = MetaTileEntityCrucible.resolveMaterial(profile.mechanicsName);
            if (source == null || source == Materials.NULL) {
                missingSources++;
                missingSourceNames.add(row.id + ":" + row.sourceName + "[" + profile.mechanicsName + "]");
                check(MetaTileEntityCrucible.resolveMaterial(profile.mechanicsName) == null,
                        "Unresolved GT6 source became available without registration id=" + row.id);
                checks++;
                continue;
            }

            boundSources++;
            String actualMechanicsName = GT6MaterialIdentity.canonicalMechanicsName(source);
            GT6LiteralTargetData.Profile boundProfile = GT6LiteralTargetData.find(actualMechanicsName);
            check(boundProfile != null,
                    "Host material does not resolve to an unambiguous GT6 target contract id=" + row.id +
                            " gt6=" + expectedMechanicsName + " host=" + source.getRegistryName() +
                            " mechanics=" + actualMechanicsName);
            checks++;

            Material expectedHot = expectedTarget(source, profile.id, profile.hotTargetId, profile.hotAmount,
                    profile.hotTargetName);
            if (profile.hotAmount > 0 && profile.hotTargetId != profile.id && expectedHot == null) {
                missingHotTargets++;
                missingTargetNames.add(row.id + ":" + row.sourceName + "->" + profile.hotTargetId + ":" +
                        profile.hotTargetName + " hot=" + profile.hotAmount);
            }
            Material actualHot = MetaTileEntityCrucible.getSmeltingTarget(source);
            check(actualHot == expectedHot,
                    "Hot target binding mismatch id=" + row.id + " source=" + source.getRegistryName() +
                            " expected=" + registryName(expectedHot) + " actual=" + registryName(actualHot));
            hotTargetsChecked++;
            checks++;
            if (profile.hotAmount > 0 && profile.hotTargetId != profile.id && expectedHot == null) {
                long outputAmount = MetaTileEntityCrucible.getSmeltingOutputAmount(source, GTValues.M);
                check(outputAmount == 0L,
                        "Missing hot target must reject conversion without output id=" + row.id +
                                " source=" + source.getRegistryName() + " output=" + outputAmount);
                checks++;
            }

            Material expectedCold = expectedTarget(source, profile.id, profile.coldTargetId, profile.coldAmount,
                    profile.coldTargetName);
            if (profile.coldAmount > 0 && profile.coldTargetId != profile.id && expectedCold == null) {
                missingColdTargets++;
                missingTargetNames.add(row.id + ":" + row.sourceName + "->" + profile.coldTargetId + ":" +
                        profile.coldTargetName + " cold=" + profile.coldAmount);
            }
            Material actualCold = MetaTileEntityCrucible.getSolidifyingTarget(source);
            check(actualCold == expectedCold,
                    "Cold target binding mismatch id=" + row.id + " source=" + source.getRegistryName() +
                            " expected=" + registryName(expectedCold) + " actual=" + registryName(actualCold));
            coldTargetsChecked++;
            checks++;

            if (actualHot != null && profile.hotAmount > 0 && actualHot != Materials.Lava) {
                long expectedAmount = BigInteger.valueOf(GTValues.M)
                        .multiply(BigInteger.valueOf(profile.hotAmount))
                        .divide(BigInteger.valueOf(GT6LiteralTargetData.GT6_U)).longValueExact();
                long actualAmount = MetaTileEntityCrucible.getSmeltingOutputAmount(source, GTValues.M);
                check(actualAmount == expectedAmount,
                        "Hot target quantity mismatch id=" + row.id + " source=" + source.getRegistryName() +
                                " expected=" + expectedAmount + " actual=" + actualAmount);
                quantitiesChecked++;
                checks++;
            }
        }

        check(GT6LiteralTargetRows.ROWS.size() == 1084,
                "Expected 1084 positive-ID GT6 target rows, got " + GT6LiteralTargetRows.ROWS.size());
        LOG.info("CRUCIBLE_WORLD_GT6_LITERAL_BINDINGS passed rows={} boundSources={} missingSources={} " +
                        "missingHotTargets={} missingColdTargets={} hotTargets={} coldTargets={} quantities={} checks={}",
                GT6LiteralTargetRows.ROWS.size(), boundSources, missingSources, missingHotTargets,
                missingColdTargets, hotTargetsChecked, coldTargetsChecked, quantitiesChecked, checks + 1);
        for (int start = 0; start < missingSourceNames.size(); start += 100) {
            int end = Math.min(missingSourceNames.size(), start + 100);
            LOG.info("CRUCIBLE_WORLD_GT6_LITERAL_MISSING {}-{} {}", start + 1, end,
                    String.join(", ", missingSourceNames.subList(start, end)));
        }
        for (String missingTarget : missingTargetNames) {
            LOG.info("CRUCIBLE_WORLD_GT6_LITERAL_MISSING_TARGET {}", missingTarget);
        }
        if (!missingTargetNames.isEmpty()) {
            List<String> matchingHostMaterials = new ArrayList<>();
            for (Material candidate : gregtech.api.GregTechAPI.materialManager.getRegisteredMaterials()) {
                if (candidate == null || candidate == Materials.NULL) continue;
                String identity = normalize(String.valueOf(candidate.getRegistryName()) + candidate.getName());
                if (identity.contains("tungsten") || identity.contains("ceramic") || identity.contains("netherbrick")) {
                    matchingHostMaterials.add(candidate.getRegistryName() + "[" + candidate.getName() + "]");
                }
            }
            LOG.info("CRUCIBLE_WORLD_GT6_LITERAL_TARGET_CANDIDATES {}", matchingHostMaterials);
        }
        return checks + 1;
    }

    /**
     * Exercises the production melt commit against every exact GT6 source
     * material that is actually present in this Forge registry. The separate
     * binding audit above checks all 1084 rows; this method deliberately does
     * not fabricate absent optional materials or targets.
     */
    public static int runRuntimePhaseTransitions(MetaTileEntityCrucible vessel) {
        try {
            java.lang.reflect.Field contentsField = MetaTileEntityCrucible.class.getDeclaredField("contents");
            contentsField.setAccessible(true);
            Class<?> storedType = Class.forName(MetaTileEntityCrucible.class.getName() + "$StoredMaterial");
            java.lang.reflect.Method melt = MetaTileEntityCrucible.class.getDeclaredMethod("melt", storedType);
            melt.setAccessible(true);

            int checkedSources = 0;
            int converted = 0;
            int safelyRejected = 0;
            int skippedLavaSpecial = 0;
            int absentSources = 0;
            int checks = 0;
            CrucibleFluidUnits.Quantity oneGt6Unit = CrucibleFluidUnits.fromGt6Atoms(GT6LiteralTargetData.GT6_U);
            check(oneGt6Unit != null &&
                            CrucibleFluidUnits.gt6AtomicAmount(CrucibleFluidUnits.storedNumerator(
                                    oneGt6Unit.amount, oneGt6Unit.remainder)) == GT6LiteralTargetData.GT6_U,
                    "Could not represent one exact GT6 source unit in current storage format");
            checks++;

            for (GT6LiteralTargetRows.Row row : GT6LiteralTargetRows.ROWS) {
                GT6LiteralTargetData.Profile profile = GT6LiteralTargetData.findById(row.id);
                Material source = MetaTileEntityCrucible.resolveMaterial(profile.mechanicsName);
                if (source == null || source == Materials.NULL) {
                    absentSources++;
                    continue;
                }

                Material target = MetaTileEntityCrucible.getSmeltingTarget(source);
                if (target == Materials.Lava) {
                    // Lava output has a project-specific bucket conversion,
                    // covered by the dedicated 1000 mB/obsidian tests.
                    skippedLavaSpecial++;
                    continue;
                }

                NBTTagCompound state = vessel.writeToNBT(new NBTTagCompound());
                state.setLong("Temperature", 3000L);
                state.setLong("OldTemperature", 3000L);
                state.setInteger("FluidQuantityVersion", 3);
                state.setInteger("WaterUnitVersion", 1);
                NBTTagList inputContents = new NBTTagList();
                NBTTagCompound input = new NBTTagCompound();
                input.setString("Material", source.getRegistryName());
                input.setLong("Amount", oneGt6Unit.amount);
                input.setLong("FluidRemainder", oneGt6Unit.remainder);
                input.setBoolean("Molten", false);
                inputContents.appendTag(input);
                state.setTag("Contents", inputContents);
                vessel.readFromNBT(state);

                List<?> stored = (List<?>) contentsField.get(vessel);
                check(stored.size() == 1, "GT6 phase fixture failed to load " + row.sourceName);
                boolean didMelt = (Boolean) melt.invoke(vessel, stored.get(0));
                NBTTagList resultContents = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
                boolean shouldReject = profile.hotAmount <= 0 || target == null || target == Materials.NULL;
                if (shouldReject) {
                    check(!didMelt && resultContents.tagCount() == 1 &&
                                    source.getRegistryName().equals(resultContents.getCompoundTagAt(0).getString("Material")) &&
                                    resultContents.getCompoundTagAt(0).getLong("Amount") == oneGt6Unit.amount &&
                                    resultContents.getCompoundTagAt(0).getLong("FluidRemainder") == oneGt6Unit.remainder,
                            "Unavailable GT6 target consumed or rewrote " + row.sourceName);
                    safelyRejected++;
                } else {
                    CrucibleFluidUnits.Quantity expected = CrucibleFluidUnits.scaleStored(
                            oneGt6Unit.amount, oneGt6Unit.remainder,
                            profile.hotAmount, GT6LiteralTargetData.GT6_U);
                    check(expected != null && (expected.amount > 0 || expected.remainder > 0),
                            "Invalid exact output fixture for " + row.sourceName);
                    check(didMelt && resultContents.tagCount() == 1,
                            "Production melt path failed for registered GT6 material " + row.sourceName);
                    NBTTagCompound output = resultContents.getCompoundTagAt(0);
                    check(target.getRegistryName().equals(output.getString("Material")) &&
                                    expected.amount == output.getLong("Amount") &&
                                    expected.remainder == output.getLong("FluidRemainder") &&
                                    output.getBoolean("Molten"),
                            "Production melt identity/quantity mismatch for " + row.sourceName +
                                    " -> " + target.getRegistryName());
                    converted++;
                    checks += 2;
                }
                checkedSources++;
                checks++;
            }
            check(checkedSources > 0, "No source GT6 materials were available for runtime phase conversion");
            checks++;
            LOG.info("CRUCIBLE_WORLD_GT6_PHASE_COMMITS passed sourceRows={} converted={} safelyRejected={} " +
                            "skippedLavaSpecial={} absentSources={} checks={}",
                    checkedSources, converted, safelyRejected, skippedLavaSpecial, absentSources, checks);
            return checks;
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Could not invoke the production crucible melt path", failure);
        }
    }

    /**
     * Exercises the production solidify commit against every exact GT6 source
     * material that is present in the Forge registry. Missing optional source
     * or target identities remain absent and must leave the molten stack intact.
     */
    public static int runRuntimeSolidification(MetaTileEntityCrucible vessel) {
        try {
            java.lang.reflect.Field contentsField = MetaTileEntityCrucible.class.getDeclaredField("contents");
            contentsField.setAccessible(true);
            java.lang.reflect.Field capacityField = MetaTileEntityCrucible.class.getDeclaredField("CAPACITY");
            capacityField.setAccessible(true);
            java.lang.reflect.Method totalAmount = MetaTileEntityCrucible.class.getDeclaredMethod("getTotalAmount");
            totalAmount.setAccessible(true);
            Class<?> storedType = Class.forName(MetaTileEntityCrucible.class.getName() + "$StoredMaterial");
            java.lang.reflect.Method solidify = MetaTileEntityCrucible.class.getDeclaredMethod("solidify", storedType);
            solidify.setAccessible(true);

            int checkedSources = 0;
            int converted = 0;
            int safelyRejected = 0;
            int skippedLavaSpecial = 0;
            int absentSources = 0;
            int checks = 0;
            CrucibleFluidUnits.Quantity oneGt6Unit = CrucibleFluidUnits.fromGt6Atoms(GT6LiteralTargetData.GT6_U);
            check(oneGt6Unit != null &&
                            CrucibleFluidUnits.gt6AtomicAmount(CrucibleFluidUnits.storedNumerator(
                                    oneGt6Unit.amount, oneGt6Unit.remainder)) == GT6LiteralTargetData.GT6_U,
                    "Could not represent one exact GT6 source unit for solidification");
            checks++;
            long capacity = capacityField.getLong(null);

            for (GT6LiteralTargetRows.Row row : GT6LiteralTargetRows.ROWS) {
                GT6LiteralTargetData.Profile profile = GT6LiteralTargetData.findById(row.id);
                check(profile != null, "Missing cold target profile id=" + row.id);
                Material source = MetaTileEntityCrucible.resolveMaterial(profile.mechanicsName);
                if (source == null || source == Materials.NULL) {
                    absentSources++;
                    continue;
                }
                if (source == Materials.Lava) {
                    // Lava solidification is handled in complete bucket portions
                    // by the dedicated project rule and is verified separately.
                    skippedLavaSpecial++;
                    continue;
                }

                NBTTagCompound state = vessel.writeToNBT(new NBTTagCompound());
                state.setLong("Temperature", 300L);
                state.setLong("OldTemperature", 300L);
                state.setInteger("FluidQuantityVersion", 3);
                state.setInteger("WaterUnitVersion", 1);
                NBTTagList inputContents = new NBTTagList();
                NBTTagCompound input = new NBTTagCompound();
                input.setString("Material", source.getRegistryName());
                input.setLong("Amount", oneGt6Unit.amount);
                input.setLong("FluidRemainder", oneGt6Unit.remainder);
                input.setBoolean("Molten", true);
                inputContents.appendTag(input);
                state.setTag("Contents", inputContents);
                vessel.readFromNBT(state);

                List<?> stored = (List<?>) contentsField.get(vessel);
                check(stored.size() == 1, "GT6 solidification fixture failed to load " + row.sourceName);
                CrucibleFluidUnits.Quantity expected = profile.coldAmount <= 0 ? null :
                        CrucibleFluidUnits.scaleStored(oneGt6Unit.amount, oneGt6Unit.remainder,
                                profile.coldAmount, GT6LiteralTargetData.GT6_U);
                long total = (Long) totalAmount.invoke(vessel);
                boolean fits = expected != null && (expected.amount > 0 || expected.remainder > 0) &&
                        CrucibleFluidUnits.replacementFits(total, capacity, oneGt6Unit.amount,
                                oneGt6Unit.remainder, expected.amount, expected.remainder, true);
                Material target = MetaTileEntityCrucible.getSolidifyingTarget(source);
                boolean unavailable = !fits || target == null || target == Materials.NULL;
                boolean didSolidify = (Boolean) solidify.invoke(vessel, stored.get(0));
                NBTTagList resultContents = vessel.writeToNBT(new NBTTagCompound()).getTagList("Contents", 10);
                if (unavailable) {
                    check(!didSolidify && resultContents.tagCount() == 1 &&
                                    source.getRegistryName().equals(resultContents.getCompoundTagAt(0).getString("Material")) &&
                                    resultContents.getCompoundTagAt(0).getLong("Amount") == oneGt6Unit.amount &&
                                    resultContents.getCompoundTagAt(0).getLong("FluidRemainder") == oneGt6Unit.remainder &&
                                    resultContents.getCompoundTagAt(0).getBoolean("Molten"),
                            "Unavailable GT6 cold target consumed or rewrote " + row.sourceName);
                    safelyRejected++;
                } else {
                    check(didSolidify && resultContents.tagCount() == 1,
                            "Production solidify path failed for registered GT6 material " + row.sourceName);
                    NBTTagCompound output = resultContents.getCompoundTagAt(0);
                    check(target.getRegistryName().equals(output.getString("Material")) &&
                                    expected.amount == output.getLong("Amount") &&
                                    expected.remainder == output.getLong("FluidRemainder") &&
                                    !output.getBoolean("Molten"),
                            "Production solidification identity/quantity mismatch for " + row.sourceName +
                                    " -> " + target.getRegistryName());
                    converted++;
                    checks += 2;
                }
                checkedSources++;
                checks++;
            }
            check(checkedSources > 0, "No source GT6 materials were available for runtime solidification");
            checks++;
            LOG.info("CRUCIBLE_WORLD_GT6_SOLIDIFY_COMMITS passed sourceRows={} converted={} safelyRejected={} " +
                            "skippedLavaSpecial={} absentSources={} checks={}",
                    checkedSources, converted, safelyRejected, skippedLavaSpecial, absentSources, checks);
            return checks;
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Could not invoke the production crucible solidify path", failure);
        }
    }

    private static Material expectedTarget(Material source, int sourceId, int targetId, long targetAmount,
                                           String targetName) {
        if (targetAmount <= 0) return null;
        if (targetId == sourceId) return source;
        Material target = MetaTileEntityCrucible.resolveMaterial(targetName);
        return target == Materials.NULL ? null : target;
    }

    private static String registryName(Material material) {
        return material == null ? "<null>" : String.valueOf(material.getRegistryName());
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
