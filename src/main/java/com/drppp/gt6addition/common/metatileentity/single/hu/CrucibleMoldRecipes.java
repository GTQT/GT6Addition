package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.unification.ore.OrePrefix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The 5 x 5 shape table used by the GT6-style crucible mold.
 *
 * <p>GT6 accepts rotations and mirrors of every registered shape.  Keeping
 * the symmetry expansion here makes the bit ordering explicit and avoids
 * coupling recipe data to the renderer.</p>
 */
final class CrucibleMoldRecipes {

    private static final Map<Integer, OrePrefix> RECIPES = new HashMap<>();

    static {
        // These entries mirror MultiTileEntityMold's native MOLD_RECIPES.
        // GT6 places each listed seed, then registers its rotations and
        // reflections. Prefixes without a GregTech CEu equivalent are omitted
        // rather than silently mapped to a different product.
        registerSymmetric(OrePrefix.plate,
                "#####", "#####", "#####", "#####", "#####");

        for (int column = 0; column < 3; column++) {
            registerSymmetric(OrePrefix.ingot, placedPattern(0, column,
                    "###", "###", "###", "###", "###"));
            registerSymmetric(OrePrefix.stick, placedPattern(0, column,
                    "#", "#", "#", "#", "#"));
        }

        registerSymmetric(OrePrefix.stickLong,
                "....#", "...#.", "..#..", ".#...", "#....");
        registerSymmetric(OrePrefix.gear,
                "#.#.#", ".###.", "##.##", ".###.", "#.#.#");
        registerSymmetric(OrePrefix.gearSmall,
                ".#.#.", "#####", ".#.#.", "#####", ".#.#.");

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                registerSymmetric(OrePrefix.ring, placedPattern(row, column,
                        "###", "#.#", "###"));
            }
        }

        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                // GT6's bolt seed is two carved cells, not a five-cell bar.
                registerSymmetric(OrePrefix.bolt, placedPattern(row, column, "#", "#"));
                registerSymmetric(OrePrefix.toolHeadScrewdriver,
                        placedPattern(row, column, "#", "#", "#", "#"));
            }
        }
    }

    private CrucibleMoldRecipes() {
    }

    static OrePrefix get(int shape) {
        if (shape == 0) {
            return null;
        }
        OrePrefix prefix = RECIPES.get(shape & ((1 << 25) - 1));
        return prefix == null ? OrePrefix.nugget : prefix;
    }

    static Map<Integer, OrePrefix> view() {
        return Collections.unmodifiableMap(RECIPES);
    }

    private static void registerSymmetric(OrePrefix prefix, String... rows) {
        int shape = pattern(rows);
        for (int rotation = 0; rotation < 4; rotation++) {
            int rotated = rotateClockwise(shape, rotation);
            RECIPES.put(rotated, prefix);
            RECIPES.put(mirrorHorizontal(rotated), prefix);
        }
    }

    private static String[] placedPattern(int startRow, int startColumn, String... seedRows) {
        char[][] rows = new char[5][5];
        for (int row = 0; row < 5; row++) {
            java.util.Arrays.fill(rows[row], '.');
        }
        for (int row = 0; row < seedRows.length; row++) {
            for (int column = 0; column < seedRows[row].length(); column++) {
                if (seedRows[row].charAt(column) == '#') {
                    rows[startRow + row][startColumn + column] = '#';
                }
            }
        }
        String[] result = new String[5];
        for (int row = 0; row < 5; row++) {
            result[row] = new String(rows[row]);
        }
        return result;
    }

    private static int pattern(String... rows) {
        if (rows.length != 5) {
            throw new IllegalArgumentException("A mold pattern must have five rows");
        }
        int shape = 0;
        for (int row = 0; row < 5; row++) {
            if (rows[row].length() != 5) {
                throw new IllegalArgumentException("A mold pattern row must have five columns");
            }
            for (int column = 0; column < 5; column++) {
                if (rows[row].charAt(column) == '#') {
                    shape |= 1 << (row * 5 + column);
                }
            }
        }
        return shape;
    }

    private static int rotateClockwise(int shape, int count) {
        int rotated = shape;
        for (int i = 0; i < count; i++) {
            int next = 0;
            for (int row = 0; row < 5; row++) {
                for (int column = 0; column < 5; column++) {
                    int source = row * 5 + column;
                    if ((rotated & (1 << source)) != 0) {
                        int targetRow = column;
                        int targetColumn = 4 - row;
                        next |= 1 << (targetRow * 5 + targetColumn);
                    }
                }
            }
            rotated = next;
        }
        return rotated;
    }

    private static int mirrorHorizontal(int shape) {
        int mirrored = 0;
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                int source = row * 5 + column;
                if ((shape & (1 << source)) != 0) {
                    mirrored |= 1 << (row * 5 + (4 - column));
                }
            }
        }
        return mirrored;
    }
}
