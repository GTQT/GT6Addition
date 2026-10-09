package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Optional Galacticraft oxygen check matching GT6's WD.oxygen behavior. */
final class CrucibleOxygenCompatibility {
    private static final String GALACTICRAFT_PROVIDER =
            "micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider";
    private static final String GALACTICRAFT_OXYGEN_UTIL =
            "micdoodle8.mods.galacticraft.core.util.OxygenUtil";

    private static volatile Api cachedApi;

    private CrucibleOxygenCompatibility() {}

    /**
     * GT6 treats normal dimensions as oxygenated and asks Galacticraft only
     * when the current provider implements its world-provider API. A broken
     * optional oxygen API fails closed only for Galacticraft dimensions.
     */
    static boolean hasOxygen(World world, BlockPos position) {
        if (world == null || position == null) return false;
        Api api = api();
        if (!api.galacticraftPresent || !api.providerType.isInstance(world.provider)) return true;
        if (api.checkTorchHasOxygen == null) return false;
        try {
            return Boolean.TRUE.equals(api.checkTorchHasOxygen.invoke(null, world, position));
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | LinkageError ignored) {
            return false;
        }
    }

    private static Api api() {
        Api result = cachedApi;
        ClassLoader loader = CrucibleOxygenCompatibility.class.getClassLoader();
        if (result != null && result.loader == loader) return result;
        synchronized (CrucibleOxygenCompatibility.class) {
            result = cachedApi;
            if (result != null && result.loader == loader) return result;
            result = resolve(loader);
            cachedApi = result;
            return result;
        }
    }

    private static Api resolve(ClassLoader loader) {
        Class<?> providerType;
        try {
            providerType = Class.forName(GALACTICRAFT_PROVIDER, false, loader);
        } catch (ClassNotFoundException absent) {
            return Api.absent(loader);
        } catch (LinkageError brokenApi) {
            return Api.absent(loader);
        }

        try {
            Class<?> oxygenUtil = Class.forName(GALACTICRAFT_OXYGEN_UTIL, false, loader);
            Method check = oxygenUtil.getMethod("checkTorchHasOxygen", World.class, BlockPos.class);
            return new Api(loader, providerType, check, true);
        } catch (ReflectiveOperationException | LinkageError brokenApi) {
            return new Api(loader, providerType, null, true);
        }
    }

    private static final class Api {
        final ClassLoader loader;
        final Class<?> providerType;
        final Method checkTorchHasOxygen;
        final boolean galacticraftPresent;

        Api(ClassLoader loader, Class<?> providerType, Method checkTorchHasOxygen, boolean galacticraftPresent) {
            this.loader = loader;
            this.providerType = providerType;
            this.checkTorchHasOxygen = checkTorchHasOxygen;
            this.galacticraftPresent = galacticraftPresent;
        }

        static Api absent(ClassLoader loader) {
            return new Api(loader, null, null, false);
        }
    }
}
