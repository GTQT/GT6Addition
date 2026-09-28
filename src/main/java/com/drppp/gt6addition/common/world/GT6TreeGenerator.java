package com.drppp.gt6addition.common.world;

import com.drppp.gt6addition.common.block.GT6AdditionBlocks;
import com.drppp.gt6addition.common.block.tree.BlockGT6TreeLeaves;
import com.drppp.gt6addition.common.block.tree.BlockGT6TreeLog;
import com.drppp.gt6addition.common.block.tree.GT6TreeSpecies;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** GT6-compatible silhouettes and height/clearance rules for the five migrated trees. */
public final class GT6TreeGenerator extends WorldGenAbstractTree {

    private final GT6TreeSpecies species;

    public GT6TreeGenerator(GT6TreeSpecies species) {
        super(false);
        this.species = species;
    }

    @Override
    public boolean generate(World world, Random random, BlockPos base) {
        int top = getTreeTop(world, base, random);
        if (top < 0 || !hasRequiredClearance(world, base, top)) return false;

        Map<BlockPos, IBlockState> placements = new HashMap<>();
        switch (species) {
            case MAPLE:
                buildMaple(placements, base, top);
                break;
            case WILLOW:
                buildWillow(placements, base, top);
                break;
            case HAZEL:
                buildHazel(placements, base);
                break;
            case COCONUT:
                buildCoconut(placements, base, top);
                break;
            case BLUE_SPRUCE:
                buildBlueSpruce(placements, base, top);
                break;
            default:
                return false;
        }

        for (Map.Entry<BlockPos, IBlockState> placement : placements.entrySet()) {
            BlockPos pos = placement.getKey();
            if (pos.getY() > 0 && pos.getY() < world.getActualHeight() && canReplace(world, pos)) {
                world.setBlockState(pos, placement.getValue(), 2);
            }
        }
        if (species == GT6TreeSpecies.BLUE_SPRUCE) makePodzolPatch(world, base);
        return true;
    }

    private int getTreeTop(World world, BlockPos base, Random random) {
        int maximum;
        switch (species) {
            case MAPLE:
                maximum = getMaxHeight(world, base, 11);
                return maximum < 9 ? -1 : base.getY() + 9 + random.nextInt(maximum - 8);
            case WILLOW:
                maximum = getMaxHeight(world, base, 7);
                return maximum < 5 ? -1 : base.getY() + 5 + random.nextInt(maximum - 4);
            case HAZEL:
                return getMaxHeight(world, base, 4) < 4 ? -1 : base.getY() + 2;
            case COCONUT:
                maximum = getMaxHeight(world, base, 12);
                return maximum < 8 ? -1 : base.getY() + 8 + random.nextInt(maximum - 7);
            case BLUE_SPRUCE:
                maximum = getMaxHeight(world, base, 16);
                return maximum < 16 ? -1 : base.getY() + maximum - random.nextInt(3);
            default:
                return -1;
        }
    }

    /** Mirrors GT6's vertical scan: the sapling's own position is not counted. */
    private static int getMaxHeight(World world, BlockPos base, int maxTreeHeight) {
        int height = 0;
        while (height++ < maxTreeHeight - 1) {
            BlockPos check = base.up(height);
            if (check.getY() >= world.getHeight() || !canReplace(world, check)) return height - 1;
        }
        return height;
    }

    /** GT6 rejects a few structural clearance cells, but lets individual foliage placements skip obstacles. */
    private boolean hasRequiredClearance(World world, BlockPos base, int top) {
        int radius;
        int y;
        switch (species) {
            case MAPLE:
                radius = 2;
                y = top - 4;
                break;
            case WILLOW:
                radius = 3;
                y = top - 2;
                break;
            case HAZEL:
                radius = 2;
                y = base.getY() + 2;
                break;
            case COCONUT:
                radius = 3;
                y = top;
                break;
            case BLUE_SPRUCE:
                radius = 3;
                y = top - 5;
                break;
            default:
                return false;
        }
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if ((x != 0 || z != 0) && !canReplace(world, base.add(x, y - base.getY(), z))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void buildMaple(Map<BlockPos, IBlockState> blocks, BlockPos base, int top) {
        addLogColumn(blocks, base, top);

        addLeaves(blocks, base.getX(), top, base.getZ());
        addLeaves(blocks, base.getX(), top + 1, base.getZ());
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            if (x != 0 || z != 0) addLeaves(blocks, base.getX() + x, top - 1, base.getZ() + z);
        }
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            if (x == 0 && z == 0) continue;
            if (Math.abs(x * z) < 2) addLeaves(blocks, base.getX() + x, top - 2, base.getZ() + z);
            if (Math.abs(x * z) < 4) {
                addLeaves(blocks, base.getX() + x, top - 3, base.getZ() + z);
                addLeaves(blocks, base.getX() + x, top - 4, base.getZ() + z);
            }
            addLeaves(blocks, base.getX() + x, top - 5, base.getZ() + z);
        }
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            if (x == 0 && z == 0) continue;
            if (Math.abs(x * z) < 9) {
                addLeaves(blocks, base.getX() + x, top - 2, base.getZ() + z);
                addLeaves(blocks, base.getX() + x, top - 3, base.getZ() + z);
                addLeaves(blocks, base.getX() + x, top - 6, base.getZ() + z);
            }
            addLeaves(blocks, base.getX() + x, top - 4, base.getZ() + z);
            addLeaves(blocks, base.getX() + x, top - 5, base.getZ() + z);
        }
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            if ((x != 0 || z != 0) && Math.abs(x * z) < 4) {
                addLeaves(blocks, base.getX() + x, top - 7, base.getZ() + z);
            }
        }
    }

    private void buildWillow(Map<BlockPos, IBlockState> blocks, BlockPos base, int top) {
        addLogColumn(blocks, base, top);

        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            if (Math.abs(x * z) < 9) {
                addLeaves(blocks, base.getX() + x, top + 1, base.getZ() + z);
                if (x != 0 || z != 0) addLeaves(blocks, base.getX() + x, top - 2, base.getZ() + z);
            }
            addLeaves(blocks, base.getX() + x, top, base.getZ() + z);
        }
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) {
            if (x == 0 && z == 0) continue;
            if (Math.abs(x * z) < 10) {
                addLeaves(blocks, base.getX() + x, top - 1, base.getZ() + z);
                if (Math.abs(x * z) > 6) {
                    for (int drop = 2; drop <= 5; drop++) {
                        if (top - drop > base.getY()) {
                            addLeaves(blocks, base.getX() + x, top - drop, base.getZ() + z);
                        }
                    }
                }
            }
        }
    }

    private void buildHazel(Map<BlockPos, IBlockState> blocks, BlockPos base) {
        addLog(blocks, base.getX(), base.getY(), base.getZ());
        addLog(blocks, base.getX(), base.getY() + 1, base.getZ());
        addLog(blocks, base.getX(), base.getY() + 2, base.getZ());

        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            addLeaves(blocks, base.getX() + x, base.getY() + 4, base.getZ() + z);
        }
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            if (x != 0 || z != 0) addLeaves(blocks, base.getX() + x, base.getY() + 2, base.getZ() + z);
            if (Math.abs(x * z) < 4) addLeaves(blocks, base.getX() + x, base.getY() + 3, base.getZ() + z);
        }
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            if ((x != 0 || z != 0) && Math.abs(x * z) < 9) {
                addLeaves(blocks, base.getX() + x, base.getY() + 1, base.getZ() + z);
            }
        }
    }

    private void buildCoconut(Map<BlockPos, IBlockState> blocks, BlockPos base, int top) {
        addLogColumn(blocks, base, top);

        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            if (x == z || x == -z) {
                if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                    addLeaves(blocks, base.getX() + x, top - 1, base.getZ() + z);
                    addLeaves(blocks, base.getX() + x, top - 2, base.getZ() + z);
                } else if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    addLeaves(blocks, base.getX() + x, top, base.getZ() + z);
                    addLeaves(blocks, base.getX() + x, top - 1, base.getZ() + z);
                } else {
                    addLeaves(blocks, base.getX() + x, top, base.getZ() + z);
                }
            }
        }
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) {
            if (x == 0 || z == 0) {
                if (Math.abs(x) == 4 || Math.abs(z) == 4) {
                    addLeaves(blocks, base.getX() + x, top - 1, base.getZ() + z);
                    addLeaves(blocks, base.getX() + x, top - 2, base.getZ() + z);
                } else if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                    addLeaves(blocks, base.getX() + x, top, base.getZ() + z);
                    addLeaves(blocks, base.getX() + x, top - 1, base.getZ() + z);
                } else {
                    addLeaves(blocks, base.getX() + x, top, base.getZ() + z);
                }
            }
        }
    }

    private void buildBlueSpruce(Map<BlockPos, IBlockState> blocks, BlockPos base, int top) {
        addLogColumn(blocks, base, top);

        addLeaves(blocks, base.getX(), top, base.getZ());
        addLeaves(blocks, base.getX(), top + 1, base.getZ());
        addLeaves(blocks, base.getX() + 1, top - 1, base.getZ());
        addLeaves(blocks, base.getX() - 1, top - 1, base.getZ());
        addLeaves(blocks, base.getX(), top - 1, base.getZ() + 1);
        addLeaves(blocks, base.getX(), top - 1, base.getZ() - 1);
        for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) {
            if (x == 0 && z == 0) continue;
            for (int layer = 1; layer <= 14; layer++) {
                if (x * x + z * z < layer * layer * 0.2D) {
                    addLeaves(blocks, base.getX() + x, top + 1 - layer, base.getZ() + z);
                }
            }
        }
    }

    private void addLogColumn(Map<BlockPos, IBlockState> blocks, BlockPos base, int top) {
        for (int y = base.getY(); y < top; y++) addLog(blocks, base.getX(), y, base.getZ());
    }

    private void addLog(Map<BlockPos, IBlockState> blocks, int x, int y, int z) {
        BlockGT6TreeLog log = GT6AdditionBlocks.getTreeLog(species);
        blocks.put(new BlockPos(x, y, z), log.getDefaultState()
                .withProperty(BlockGT6TreeLog.LOG_AXIS, BlockGT6TreeLog.EnumAxis.Y));
    }

    private void addLeaves(Map<BlockPos, IBlockState> blocks, int x, int y, int z) {
        BlockGT6TreeLeaves leaves = GT6AdditionBlocks.getTreeLeaves(species);
        BlockPos pos = new BlockPos(x, y, z);
        if (blocks.containsKey(pos)) return;
        blocks.put(pos, leaves.getDefaultState()
                .withProperty(BlockLeaves.DECAYABLE, true)
                .withProperty(BlockLeaves.CHECK_DECAY, false));
    }

    private static boolean canReplace(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return block.isReplaceable(world, pos)
                || block.canBeReplacedByLeaves(state, world, pos)
                || block instanceof BlockLeaves
                || block instanceof BlockSapling
                || material == Material.PLANTS
                || block == Blocks.SNOW_LAYER;
    }

    private static void makePodzolPatch(World world, BlockPos base) {
        IBlockState podzol = Blocks.DIRT.getStateFromMeta(2);
        for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) {
            if ((x == 0 && z == 0) || x * x + z * z > 30) continue;
            for (int depth = 0; depth <= 3; depth++) {
                BlockPos pos = base.add(x, -depth, z);
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (block.isAir(state, world, pos)) continue;
                if (block == Blocks.DIRT || block == Blocks.GRASS) {
                    world.setBlockState(pos, podzol, 2);
                }
                break;
            }
        }
    }
}

