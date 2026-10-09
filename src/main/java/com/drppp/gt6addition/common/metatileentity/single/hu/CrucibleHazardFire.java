package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.GTValues;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

/** GT6 WD.fire's block selection, adapted to Forge 1.12's directional API. */
public final class CrucibleHazardFire {
    private static final String THAUMCRAFT_MOD_ID = "thaumcraft";
    private static final String THAUMCRAFT_NODE_API = "thaumcraft.api.nodes.INode";

    private static volatile boolean thaumcraftNodeLookupComplete;
    private static volatile Class<?> thaumcraftNodeApi;

    private CrucibleHazardFire() {}

    public static boolean tryIgnite(World world, BlockPos pos, boolean checkFlammability) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        if (material == Material.LAVA || material == Material.FIRE) return false;
        if (material != Material.CARPET && state.getCollisionBoundingBox(world, pos) != null) return false;
        if (isThaumcraftNode(world, pos)) return false;
        // Forge 1.12 has no UNKNOWN facing. Check all real faces for the
        // replaceable block rather than guessing a single directional value.
        for (EnumFacing side : EnumFacing.VALUES) {
            if (state.getBlock().getFlammability(world, pos, side) > 0) {
                return world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
            }
        }
        // GT6's IItemGT guard prevents its own item-bearing blocks from being
        // overwritten by synthetic fire. In CEu the corresponding marker is
        // a GregTech-owned block which has a registered ItemBlock.
        if (isGregTechItemBlock(state.getBlock())) return false;
        if (!checkFlammability) return world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos neighbor = pos.offset(side);
            IBlockState adjacent = world.getBlockState(neighbor);
            if (adjacent.getBlock() == Blocks.CHEST || adjacent.getBlock() == Blocks.TRAPPED_CHEST ||
                    adjacent.getBlock().getFlammability(world, neighbor, side.getOpposite()) > 0) {
                return world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
            }
        }
        return false;
    }

    private static boolean isThaumcraftNode(World world, BlockPos pos) {
        // Loader.isModLoaded dereferences its mod index/controller directly.
        // During isolated logic tests (and before discovery completes) the
        // index can still be empty, so avoid querying the controller then.
        if (!Loader.instance().getIndexedModList().containsKey(THAUMCRAFT_MOD_ID) ||
                !Loader.isModLoaded(THAUMCRAFT_MOD_ID)) return false;
        TileEntity tile = world.getTileEntity(pos);
        return implementsOptionalNodeApi(tile, thaumcraftNodeApi());
    }

    private static Class<?> thaumcraftNodeApi() {
        if (thaumcraftNodeLookupComplete) return thaumcraftNodeApi;
        synchronized (CrucibleHazardFire.class) {
            if (!thaumcraftNodeLookupComplete) {
                try {
                    thaumcraftNodeApi = Class.forName(THAUMCRAFT_NODE_API, false,
                            CrucibleHazardFire.class.getClassLoader());
                } catch (ClassNotFoundException | LinkageError ignored) {
                    // Thaumcraft is optional; a missing/broken API must not
                    // create a hard dependency during normal mod startup.
                    thaumcraftNodeApi = null;
                }
                thaumcraftNodeLookupComplete = true;
            }
        }
        return thaumcraftNodeApi;
    }

    static boolean implementsOptionalNodeApi(TileEntity tile, Class<?> optionalNodeApi) {
        return tile != null && optionalNodeApi != null && optionalNodeApi.isInstance(tile);
    }

    static boolean isGregTechItemBlock(Block block) {
        if (block == null) return false;
        Item item = Item.getItemFromBlock(block);
        return isGregTechItemBlock(block.getRegistryName(), item instanceof ItemBlock);
    }

    static boolean isGregTechItemBlock(ResourceLocation blockId, boolean hasItemBlock) {
        return blockId != null && hasItemBlock && GTValues.MODID.equals(blockId.getNamespace());
    }
}
