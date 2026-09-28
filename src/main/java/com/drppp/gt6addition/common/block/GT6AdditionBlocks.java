package com.drppp.gt6addition.common.block;

import com.drppp.gt6addition.common.block.tree.BlockGT6TreeLeaves;
import com.drppp.gt6addition.common.block.tree.BlockGT6TreeLog;
import com.drppp.gt6addition.common.block.tree.BlockGT6TreeSapling;
import com.drppp.gt6addition.common.block.tree.GT6TreeSpecies;
import net.minecraft.block.Block;

public final class GT6AdditionBlocks {

    public static final BlockAnthraciteOre ANTHRACITE_ORE = new BlockAnthraciteOre();
    public static final BlockGT6TreeLog[] TREE_LOGS = createLogs();
    public static final BlockGT6TreeSapling[] TREE_SAPLINGS = createSaplings();
    public static final BlockGT6TreeLeaves[] TREE_LEAVES = createLeaves();

    private GT6AdditionBlocks() {
    }

    private static BlockGT6TreeLog[] createLogs() {
        GT6TreeSpecies[] species = GT6TreeSpecies.values();
        BlockGT6TreeLog[] blocks = new BlockGT6TreeLog[species.length];
        for (int i = 0; i < species.length; i++) blocks[i] = new BlockGT6TreeLog(species[i]);
        return blocks;
    }

    private static BlockGT6TreeSapling[] createSaplings() {
        GT6TreeSpecies[] species = GT6TreeSpecies.values();
        BlockGT6TreeSapling[] blocks = new BlockGT6TreeSapling[species.length];
        for (int i = 0; i < species.length; i++) blocks[i] = new BlockGT6TreeSapling(species[i]);
        return blocks;
    }

    private static BlockGT6TreeLeaves[] createLeaves() {
        GT6TreeSpecies[] species = GT6TreeSpecies.values();
        BlockGT6TreeLeaves[] blocks = new BlockGT6TreeLeaves[species.length];
        for (int i = 0; i < species.length; i++) blocks[i] = new BlockGT6TreeLeaves(species[i]);
        return blocks;
    }

    public static BlockGT6TreeLog getTreeLog(GT6TreeSpecies species) {
        return TREE_LOGS[species.ordinal()];
    }

    public static BlockGT6TreeSapling getTreeSapling(GT6TreeSpecies species) {
        return TREE_SAPLINGS[species.ordinal()];
    }

    public static BlockGT6TreeLeaves getTreeLeaves(GT6TreeSpecies species) {
        return TREE_LEAVES[species.ordinal()];
    }

    public static Block[] getTreeBlocks() {
        Block[] blocks = new Block[TREE_LOGS.length + TREE_LEAVES.length + TREE_SAPLINGS.length];
        int index = 0;
        for (Block block : TREE_LOGS) blocks[index++] = block;
        for (Block block : TREE_LEAVES) blocks[index++] = block;
        for (Block block : TREE_SAPLINGS) blocks[index++] = block;
        return blocks;
    }
}
