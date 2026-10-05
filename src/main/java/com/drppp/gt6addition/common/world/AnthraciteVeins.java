package com.drppp.gt6addition.common.world;

import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import com.drppp.gt6addition.common.config.GT6AdditionConfig;
import gregtech.api.worldgen.config.OreDepositBuilder;
import gregtech.api.worldgen.config.WorldGenRegistry;

import net.minecraft.world.WorldProvider;
import net.minecraftforge.fml.common.Loader;

import java.util.Locale;

/**
 * Anthracite layers, registered as ordinary CEu ore veins.
 *
 * <p>
 * This replaces the standalone {@code AnthraciteWorldGenerator}. GT6 spawns one large coal layer per 3x3 chunk
 * source cell and rolls anthracite against lignite with a 80:160 weight, i.e. one cell in three. CEu's vein grid is
 * also 3x3 chunks wide (48x48 blocks) and draws a single deposit per grid, so the original geometry maps directly
 * onto {@code OreDepositBuilder.slabGeneration(...)}:
 *
 * <ul>
 * <li>the original span {@code origin - rnd(32) .. origin + 16 + rnd(32)} is 16..78 blocks wide, i.e. a radius of
 * 8..39, hence {@code MIN_RADIUS = 8} and {@code MAX_RADIUS = 40} (the radius range excludes its upper bound);</li>
 * <li>the layer stays 7 blocks thick ({@code Y_RADIUS = 3});</li>
 * <li>the original rim-weighted per-block density is replaced by CEu's plain probability density
 * ({@code DENSITY = 0.25f}, which yields a comparable block count for the largest layers).</li>
 * </ul>
 *
 * <p>
 * The engine shrinks {@code maxHeight} by {@code getMaxSize().getY() / 2 + 4} = {@code Y_RADIUS + 4} = 7 and the slab
 * spans +-3 blocks around the vein centre, so the configured height limits are shifted by +2 to keep the original
 * centre range of 50..74 (overworld) and 16..26 (Twilight Forest). The vein centre itself is picked by the
 * generation engine, so the layers are no longer anchored to chunks congruent to 1 modulo 3.
 *
 * <p>
 * Host stone selection uses the stock {@code PREDICATE_STONE_TYPE} (any block that GT maps to a stone type).
 */
public final class AnthraciteVeins {

    private static final String OVERWORLD_VEIN_NAME = "gt6addition/anthracite_layer";
    private static final String TWILIGHT_VEIN_NAME = "gt6addition/anthracite_layer_twilight";
    private static final String TRANSLATION_KEY = "gt6addition.vein.anthracite";

    /** Radius 8..39 blocks, matching GT6's 16..78 block wide layer. */
    private static final int MIN_RADIUS = 8;
    private static final int MAX_RADIUS = 40;

    /** 7 blocks thick, matching GT6's {@code baseY - 1 .. baseY + 5} band. */
    private static final int Y_RADIUS = 3;

    private static final int OVERWORLD_MIN_Y = 52;
    private static final int OVERWORLD_MAX_Y = 83;
    private static final int TWILIGHT_MIN_Y = 18;
    private static final int TWILIGHT_MAX_Y = 35;

    /**
     * CEu draws one weighted deposit per 3x3 chunk grid; the overworld pool weighs roughly 1670 (22 ore veins, 4
     * stone spheres and the raw oil sphere), so ~650 puts anthracite at about one grid in three, like GT6's 80:160
     * roll. It does not consume the grid's vein slot ({@code countAsVein(false)}), so no ordinary vein is displaced.
     * Tune this single number if the layers should be rarer or more common.
     */
    private static final int WEIGHT = 650;

    private static final float DENSITY = 0.25f;

    private AnthraciteVeins() {}

    /** Registers both layers; call during {@code init}, after GT has initialized its own registry. */
    public static void register() {
        if (!GT6AdditionConfig.generateAnthraciteVeins) {
            return;
        }
        WorldGenRegistry registry = WorldGenRegistry.INSTANCE;
        OreDepositBuilder.definitionBuilder(OVERWORLD_VEIN_NAME)
                .translationKey(TRANSLATION_KEY)
                .weight(WEIGHT)
                .density(DENSITY)
                .countAsVein(false)
                .minHeight(OVERWORLD_MIN_Y)
                .maxHeight(OVERWORLD_MAX_Y)
                .dimensionId(0)
                .slabGeneration(MIN_RADIUS, MAX_RADIUS, Y_RADIUS)
                .simpleFill(GT6AdditionBlocks.ANTHRACITE_ORE.getDefaultState())
                .buildAndRegister(registry);

        OreDepositBuilder.definitionBuilder(TWILIGHT_VEIN_NAME)
                .translationKey(TRANSLATION_KEY)
                .weight(WEIGHT)
                .density(DENSITY)
                .countAsVein(false)
                .minHeight(TWILIGHT_MIN_Y)
                .maxHeight(TWILIGHT_MAX_Y)
                .dimensionFilter(AnthraciteVeins::isTwilightForest)
                .slabGeneration(MIN_RADIUS, MAX_RADIUS, Y_RADIUS)
                .simpleFill(GT6AdditionBlocks.ANTHRACITE_ORE.getDefaultState())
                .buildAndRegister(registry);
    }

    private static boolean isTwilightForest(WorldProvider worldProvider) {
        return Loader.isModLoaded("twilightforest") &&
                worldProvider.getClass().getName().toLowerCase(Locale.ROOT).contains("twilightforest");
    }
}
