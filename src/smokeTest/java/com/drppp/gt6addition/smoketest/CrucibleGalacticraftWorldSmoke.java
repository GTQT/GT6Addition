package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Exercises the real optional Galacticraft API only in an isolated opt-in Forge server. */
public final class CrucibleGalacticraftWorldSmoke {
    private static final Logger LOG = LogManager.getLogger("CrucibleParitySmoke");
    private static final String PROVIDER_API =
            "micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider";
    private static final String DIMENSIONS = "micdoodle8.mods.galacticraft.core.dimension.GCDimensions";

    private CrucibleGalacticraftWorldSmoke() {}

    public static int run(WorldServer overworld, net.minecraft.server.MinecraftServer server) throws Exception {
        if (!Loader.isModLoaded("galacticraftcore")) {
            LOG.info("CRUCIBLE_WORLD_GALACTICRAFT_OXYGEN skipped (mod not loaded)");
            return 0;
        }

        boolean normalDimensionHasOxygen = CrucibleOxygenCompatibility.hasOxygen(overworld, new BlockPos(0, 128, 0));
        check(normalDimensionHasOxygen,
                "A non-Galacticraft dimension must retain GT6's default oxygen behavior");

        ClassLoader loader = CrucibleGalacticraftWorldSmoke.class.getClassLoader();
        Class<?> dimensionsClass = Class.forName(DIMENSIONS, true, loader);
        Field moonField = dimensionsClass.getField("MOON");
        Object moonType = moonField.get(null);
        check(moonType != null, "Galacticraft did not register its Moon dimension type");
        Method getId = moonType.getClass().getMethod("getId");
        int moonId = ((Number) getId.invoke(moonType)).intValue();
        DimensionManager.initDimension(moonId);
        WorldServer moon = server.getWorld(moonId);
        check(moon != null, "Galacticraft Moon world failed to initialize");

        Class<?> providerApi = Class.forName(PROVIDER_API, false, loader);
        check(providerApi.isInstance(moon.provider),
                "Moon WorldProvider does not implement the Galacticraft provider API");
        BlockPos probe = new BlockPos(0, 128, 0);
        boolean moonHasOxygen = CrucibleOxygenCompatibility.hasOxygen(moon, probe);
        check(!moonHasOxygen,
                "Galacticraft OxygenUtil must reject a Moon location without oxygen blocks/bubbles");

        LOG.info("CRUCIBLE_WORLD_GALACTICRAFT_OXYGEN passed moonDimension={} normalOxygen={} moonOxygen={}",
                moonId, normalDimensionHasOxygen, moonHasOxygen);
        return 3;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
