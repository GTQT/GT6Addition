package com.drppp.gt6addition.smoketest;

import com.drppp.gt6addition.common.material.GT6MaterialCompatibility;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Exercises real GTQTCore registry identities without registering or mutating materials. */
final class CrucibleGTQTCoreWorldSmoke {
    private static final Logger LOG = LogManager.getLogger("CrucibleParitySmoke");

    private CrucibleGTQTCoreWorldSmoke() {}

    static int run() {
        if (!Loader.isModLoaded("gtqtcore")) {
            LOG.info("CRUCIBLE_WORLD_OPTIONAL_GTQTCORE unavailable; checks not executed");
            return 0;
        }

        Material adamantium = GregTechAPI.materialManager.getMaterial("gtqtcore:adamantium");
        check(adamantium != null, "GTQTCore did not register its existing Adamantium material");
        check("gtqtcore:adamantium".equals(adamantium.getRegistryName()),
                "GTQTCore Adamantium resolved to a different registry identity");
        check(GT6MaterialCompatibility.findExternal("adamantium") == adamantium &&
                        GT6MaterialCompatibility.findExternal("gtqtcore:adamantium") == adamantium,
                "Compatibility did not preserve the exact GTQTCore Adamantium object");
        check(adamantium.hasProperty(PropertyKey.INGOT) && adamantium.hasProperty(PropertyKey.FLUID),
                "GTQTCore Adamantium lost its source ingot/fluid properties");
        check(GregTechAPI.materialManager.getMaterial("gt6addition:adamantium") == null,
                "Retired addon Adamantium identity was recreated");
        check(GregTechAPI.materialManager.getMaterial("gtqtcore:adamantine") == null &&
                        GT6MaterialCompatibility.findExternal("gtqtcore:adamantine") == null &&
                        GT6MaterialCompatibility.findExternal("adamantine") == null,
                "Missing Adamantine was substituted with a similarly named material");
        LOG.info("CRUCIBLE_WORLD_GTQTCORE_ADAMANTIUM passed registry={} properties=ingot,fluid missingAdamantine=refused",
                adamantium.getRegistryName());

        Material fluorite = GregTechAPI.materialManager.getMaterial("gtqtcore:fluorite");
        check(fluorite != null, "GTQTCore did not register its existing Fluorite material");
        check("gtqtcore:fluorite".equals(fluorite.getRegistryName()) &&
                        GT6MaterialCompatibility.findExternal("fluorite") == fluorite &&
                        GT6MaterialCompatibility.findExternal("gtqtcore:fluorite") == fluorite,
                "Compatibility did not preserve the exact GTQTCore Fluorite object");
        check(fluorite.hasProperty(PropertyKey.GEM) && !fluorite.hasProperty(PropertyKey.INGOT) &&
                        !fluorite.hasProperty(PropertyKey.FLUID),
                "GTQTCore Fluorite properties were changed or misread");
        check(GregTechAPI.materialManager.getMaterial("gt6addition:fluorite") == null,
                "Retired addon Fluorite identity was recreated");
        LOG.info("CRUCIBLE_WORLD_GTQTCORE_FLUORITE passed registry={} properties=gem,no-ingot,no-fluid",
                fluorite.getRegistryName());
        return 2;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
