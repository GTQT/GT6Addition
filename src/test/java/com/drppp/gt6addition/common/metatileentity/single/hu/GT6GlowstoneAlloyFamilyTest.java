package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Independent source membership and exact commonDivider regression for GT6's generated family recipes. */
class GT6GlowstoneAlloyFamilyTest {
    @Test
    void everyNonBaseGt6GlowstoneFamilyMemberHasBothGeneratedAlloyRecipes() throws Exception {
        List<String[]> sourceMembers = sourceMembers();
        assertEquals(6, sourceMembers.size());

        Set<String> expected = new HashSet<>();
        for (String[] row : sourceMembers) {
            assertEquals(3, row.length);
            int id = Integer.parseInt(row[0]);
            String sourceName = row[1];
            String familyMember = normalize(sourceName);
            assertTrue(expected.add(familyMember), sourceName);
            assertEquals(familyMember, normalize(GT6MaterialIdentity.name(id)), "GT6 identity " + row[0]);

            assertRecipe("energetic_alloy", 1,
                    new String[]{"inductive_alloy", familyMember}, new long[]{2, 1});
            assertRecipe("lumium", 4,
                    new String[]{"tin", "silver", familyMember}, new long[]{3, 1, 4});
        }

        Set<String> implementedMembers = new HashSet<>();
        int generatedRecipeCount = 0;
        for (GT6AlloyRecipes recipe : GT6AlloyRecipes.getRecipes()) {
            if (!("energetic_alloy".equals(recipe.output) || "lumium".equals(recipe.output))) continue;
            for (String input : recipe.inputs) {
                String name = normalize(input);
                if (!"glowstone".equals(name) && isGlowstoneFamilyName(name)) {
                    implementedMembers.add(name);
                    generatedRecipeCount++;
                }
            }
        }
        assertEquals(expected, implementedMembers, "No source family member may be omitted or invented");
        assertEquals(12, generatedRecipeCount, "Two generated recipes per each of six non-base members");
    }

    private static List<String[]> sourceMembers() throws Exception {
        InputStream stream = GT6GlowstoneAlloyFamilyTest.class
                .getResourceAsStream("/gt6-glowstone-family-members.txt");
        assertNotNull(stream);
        List<String[]> members = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                members.add(line.split("\\|", -1));
            }
        }
        return members;
    }

    private static void assertRecipe(String output, long outputUnits, String[] inputs, long[] inputUnits) {
        List<GT6AlloyRecipes> matches = new ArrayList<>();
        String[] expectedInputs = normalizedInputs(inputs);
        for (GT6AlloyRecipes recipe : GT6AlloyRecipes.getRecipes()) {
            if (output.equals(recipe.output) && Arrays.equals(expectedInputs, normalizedInputs(recipe.inputs))) matches.add(recipe);
        }
        assertEquals(1, matches.size(), output + " " + Arrays.toString(inputs));
        GT6AlloyRecipes recipe = matches.get(0);
        assertEquals(outputUnits, recipe.outputUnits, output);
        assertArrayEquals(inputUnits, recipe.inputUnits, output);
    }

    private static String[] normalizedInputs(String[] inputs) {
        String[] result = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) result[i] = normalize(inputs[i]);
        return result;
    }

    private static boolean isGlowstoneFamilyName(String name) {
        return name.startsWith("glowstone") || "gloomstone".equals(name);
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
