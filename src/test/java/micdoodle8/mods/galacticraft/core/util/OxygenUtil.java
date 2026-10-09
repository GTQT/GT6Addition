package micdoodle8.mods.galacticraft.core.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Test-only stand-in for the optional Galacticraft oxygen query. */
public final class OxygenUtil {
    public static boolean oxygenAvailable;
    public static BlockPos lastPosition;

    private OxygenUtil() {}

    public static boolean checkTorchHasOxygen(World world, BlockPos position) {
        lastPosition = position;
        return oxygenAvailable;
    }
}
