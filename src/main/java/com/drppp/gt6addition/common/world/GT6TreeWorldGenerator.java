package com.drppp.gt6addition.common.world;

import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import com.drppp.gt6addition.common.block.tree.GT6TreeSpecies;
import com.drppp.gt6addition.common.config.GT6AdditionConfig;
import com.drppp.gt6addition.common.block.tree.BlockGT6TreeSapling;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Loader;

import java.util.Locale;
import java.util.Random;

/** GT6-style biome and rarity rules for the five ported tree species. */
public final class GT6TreeWorldGenerator implements IWorldGenerator {

    private static final TreeEntry[] TREES = {
            new TreeEntry(GT6TreeSpecies.MAPLE, 5),
            new TreeEntry(GT6TreeSpecies.WILLOW, 4),
            new TreeEntry(GT6TreeSpecies.HAZEL, 32),
            new TreeEntry(GT6TreeSpecies.COCONUT, 1),
            new TreeEntry(GT6TreeSpecies.BLUE_SPRUCE, 32)
    };

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!GT6AdditionConfig.generateGT6Trees || world.isRemote) return;

        for (TreeEntry tree : TREES) {
            if (!isDimensionSupported(world, tree.species) || random.nextInt(tree.probability) != 0) continue;

            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            BlockPos surface = findSurface(world, x, z);
            if (surface == null || !isSuitableBiome(world.getBiome(surface), tree.species)) continue;

            BlockGT6TreeSapling sapling = GT6AdditionBlocks.getTreeSapling(tree.species);
            IBlockState soil = world.getBlockState(surface);
            boolean plantable = soil.getBlock().canSustainPlant(soil, world, surface, EnumFacing.UP, sapling);
            if (!plantable && tree.species == GT6TreeSpecies.COCONUT) {
                plantable = soil.getBlock() == Blocks.SAND;
            }
            if (!plantable) continue;

            new GT6TreeGenerator(tree.species).generate(world, random, surface.up());
        }
    }

    private static BlockPos findSurface(World world, int x, int z) {
        for (int y = world.getActualHeight() - 1; y >= 1; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block == Blocks.FARMLAND) return null;
            if (state.getMaterial().isLiquid()) return null;
            if (!state.isOpaqueCube() || block.isWood(world, pos) || block.isLeaves(state, world, pos)) continue;
            return pos;
        }
        return null;
    }

    private static boolean isSuitableBiome(Biome biome, GT6TreeSpecies species) {
        String name = biome.getBiomeName().toLowerCase(Locale.ROOT);
        switch (species) {
            case MAPLE:
                return biome == net.minecraft.init.Biomes.FOREST
                        || biome == net.minecraft.init.Biomes.FOREST_HILLS
                        || contains(name, "autumn forest", "elysian forest", "meadow forest", "seasonal forest",
                        "forested hills", "forested island", "snow forest", "temperate forest", "maple woods",
                        "firefly forest", "forest island", "forested archipelago", "forested mountains",
                        "forested valley");
            case WILLOW:
                return BiomeDictionary.hasType(biome, Type.SWAMP)
                        || contains(name, "green swamplands", "deep swamp", "land of lakes marsh", "marsh",
                        "lush swamp", "moor", "mire", "bog", "twilight swamp", "submerged swamp");
            case HAZEL:
                return BiomeDictionary.hasType(biome, Type.PLAINS)
                        || contains(name, "meadow", "grassland", "flower field", "sunflower plains", "clearing",
                        "elysian fields", "lowlands", "origin valley", "grassy archipelago", "alfheim");
            case COCONUT:
                return biome == net.minecraft.init.Biomes.BEACH
                        || contains(name, "tropical ocean", "tropical beach", "tropical river", "tropical lake",
                        "tropical archipelago", "tropical island", "tropics", "oasis");
            case BLUE_SPRUCE:
                return BiomeDictionary.hasType(biome, Type.MOUNTAIN)
                        || contains(name, "mountain", "highlands", "plateau", "alps", "cliffs", "alpine",
                        "stone canyon", "rocky hills", "rocky desert", "thornlands", "valley");
            default:
                return false;
        }
    }

    private static boolean isDimensionSupported(World world, GT6TreeSpecies species) {
        if (world.provider.getDimension() == 0) return true;
        String provider = world.provider.getClass().getName().toLowerCase(Locale.ROOT);
        if (provider.contains("twilightforest")) {
            return Loader.isModLoaded("twilightforest") && species != GT6TreeSpecies.COCONUT;
        }
        if (provider.contains("erebus")) {
            return Loader.isModLoaded("erebus") && (species == GT6TreeSpecies.MAPLE
                    || species == GT6TreeSpecies.WILLOW || species == GT6TreeSpecies.HAZEL);
        }
        if (provider.contains("alfheim")) {
            return Loader.isModLoaded("alfheim") && species == GT6TreeSpecies.HAZEL;
        }
        if (provider.contains("atum") || provider.contains("tropic")) {
            return species == GT6TreeSpecies.COCONUT
                    && (Loader.isModLoaded("atum") || Loader.isModLoaded("tropicraft"));
        }
        return false;
    }

    private static boolean contains(String name, String... fragments) {
        for (String fragment : fragments) if (name.contains(fragment)) return true;
        return false;
    }

    private static final class TreeEntry {
        private final GT6TreeSpecies species;
        private final int probability;

        private TreeEntry(GT6TreeSpecies species, int probability) {
            this.species = species;
            this.probability = probability;
        }
    }
}
