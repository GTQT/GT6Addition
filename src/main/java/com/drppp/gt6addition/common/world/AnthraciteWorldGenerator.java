package com.drppp.gt6addition.common.world;

import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import com.drppp.gt6addition.common.config.GT6AdditionConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Loader;

import java.util.Random;

/** Generates the large, irregular horizontal anthracite layers independently of CEu ore veins. */
public final class AnthraciteWorldGenerator implements IWorldGenerator {

    private static final int ORIGIN_SPACING_CHUNKS = 3;
    private static final int LAYER_SIZE = 32;
    private static final int DENSITY = 6;
    private static final int OVERWORLD_MIN_Y = 50;
    private static final int OVERWORLD_MAX_Y = 80;
    private static final int TWILIGHT_MIN_Y = 16;
    private static final int TWILIGHT_MAX_Y = 32;
    private static final int ANTHRACITE_WEIGHT = 80;
    private static final int OTHER_LARGE_COAL_LAYER_WEIGHT = 160;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!GT6AdditionConfig.generateAnthraciteVeins || world.isRemote || !isSupportedDimension(world)) {
            return;
        }

        int minOriginX = firstOriginAtOrAfter(chunkX - 4);
        int minOriginZ = firstOriginAtOrAfter(chunkZ - 4);
        for (int originChunkX = minOriginX; originChunkX <= chunkX + 4;
             originChunkX += ORIGIN_SPACING_CHUNKS) {
            for (int originChunkZ = minOriginZ; originChunkZ <= chunkZ + 4;
                 originChunkZ += ORIGIN_SPACING_CHUNKS) {
                generateLayerInChunk(world, chunkX, chunkZ, originChunkX, originChunkZ);
            }
        }
    }

    private static boolean isSupportedDimension(World world) {
        if (world.provider.getDimension() == 0) {
            return true;
        }
        return Loader.isModLoaded("twilightforest") &&
                world.provider.getClass().getName().toLowerCase(java.util.Locale.ROOT).contains("twilightforest");
    }

    /** GT6 large deposits select source chunks whose coordinates are congruent to 1 modulo 3. */
    private static int firstOriginAtOrAfter(int chunk) {
        return chunk + Math.floorMod(1 - chunk, ORIGIN_SPACING_CHUNKS);
    }

    private static void generateLayerInChunk(World world, int chunkX, int chunkZ,
                                             int originChunkX, int originChunkZ) {
        long seed = world.getSeed();
        seed ^= (long) world.provider.getDimension() * 341873128712L;
        seed ^= (long) originChunkX * 132897987541L;
        seed ^= (long) originChunkZ * 42317861L;
        Random layerRandom = new Random(seed);
        // Match GT6's large-coal relative weight (80) beside lignite (160): one source cell in three.
        if (layerRandom.nextInt(ANTHRACITE_WEIGHT + OTHER_LARGE_COAL_LAYER_WEIGHT) >= ANTHRACITE_WEIGHT) {
            return;
        }

        int originX = originChunkX * 16;
        int originZ = originChunkZ * 16;
        int minX = originX - layerRandom.nextInt(LAYER_SIZE);
        int maxX = originX + 16 + layerRandom.nextInt(LAYER_SIZE);
        int minZ = originZ - layerRandom.nextInt(LAYER_SIZE);
        int maxZ = originZ + 16 + layerRandom.nextInt(LAYER_SIZE);
        boolean overworld = world.provider.getDimension() == 0;
        int minY = overworld ? OVERWORLD_MIN_Y : TWILIGHT_MIN_Y;
        int maxY = overworld ? OVERWORLD_MAX_Y : TWILIGHT_MAX_Y;
        int baseY = minY + layerRandom.nextInt(maxY - minY - 5);

        int chunkMinX = chunkX * 16;
        int chunkMinZ = chunkZ * 16;
        int startX = Math.max(minX, chunkMinX);
        int endX = Math.min(maxX, chunkMinX + 15);
        int startZ = Math.max(minZ, chunkMinZ);
        int endZ = Math.min(maxZ, chunkMinZ + 15);
        if (startX > endX || startZ > endZ) {
            return;
        }

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                int distanceX = Math.max(Math.abs(minX - x), Math.abs(maxX - x));
                int distanceZ = Math.max(Math.abs(minZ - z), Math.abs(maxZ - z));
                Random columnRandom = new Random(columnSeed(seed, x, z));

                for (int y = baseY - 1; y <= baseY + 5; y++) {
                    if (passesDensity(columnRandom, distanceX, distanceZ)) {
                        placeIfHostStone(world, x, y, z);
                    }
                }
            }
        }
    }

    private static boolean passesDensity(Random random, int distanceX, int distanceZ) {
        int chanceZ = Math.max(1, distanceZ / DENSITY);
        int chanceX = Math.max(1, distanceX / DENSITY);
        return random.nextInt(chanceZ) == 0 || random.nextInt(chanceX) == 0;
    }

    private static void placeIfHostStone(World world, int x, int y, int z) {
        if (y <= 0 || y >= world.getHeight()) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        if (isHostStone(world, state.getBlock())) {
            world.setBlockState(pos, GT6AdditionBlocks.ANTHRACITE_ORE.getDefaultState(), 2);
        }
    }

    private static boolean isHostStone(World world, Block block) {
        if (block == Blocks.STONE) {
            return true;
        }
        if (world.provider.getDimension() == 0 || !Loader.isModLoaded("twilightforest")) {
            return false;
        }
        if (block.getMaterial(block.getDefaultState()) != Material.ROCK || block.getRegistryName() == null ||
                !"twilightforest".equals(block.getRegistryName().getNamespace())) {
            return false;
        }
        String path = block.getRegistryName().getPath();
        return path.equals("twilight_stone") || path.equals("twilightstone") || path.equals("stone");
    }

    private static long columnSeed(long seed, int x, int z) {
        long mixed = seed ^ (long) x * 0x9E3779B97F4A7C15L ^ (long) z * 0xC2B2AE3D27D4EB4FL;
        mixed ^= mixed >>> 30;
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= mixed >>> 27;
        mixed *= 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }
}
