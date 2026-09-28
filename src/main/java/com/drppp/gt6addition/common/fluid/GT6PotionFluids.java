package com.drppp.gt6addition.common.fluid;

import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fluid forms for the vanilla 1.12 potion types, using the legacy GT6 fluid names.
 */
public final class GT6PotionFluids {

    public static final int POTION_VOLUME = 250;

    private static final List<PotionDefinition> DEFINITIONS = Collections.unmodifiableList(Arrays.asList(
            new PotionDefinition("water", "water"),
            new PotionDefinition("mundane", "mundane"),
            new PotionDefinition("thick", "thick"),
            new PotionDefinition("awkward", "awkward"),
            new PotionDefinition("night_vision", "nightvision"),
            new PotionDefinition("long_night_vision", "nightvision.long"),
            new PotionDefinition("invisibility", "invisibility"),
            new PotionDefinition("long_invisibility", "invisibility.long"),
            new PotionDefinition("leaping", "jump"),
            new PotionDefinition("long_leaping", "jump.long"),
            new PotionDefinition("strong_leaping", "jump.strong"),
            new PotionDefinition("fire_resistance", "fireresistance"),
            new PotionDefinition("long_fire_resistance", "fireresistance.long"),
            new PotionDefinition("swiftness", "speed"),
            new PotionDefinition("long_swiftness", "speed.long"),
            new PotionDefinition("strong_swiftness", "speed.strong"),
            new PotionDefinition("slowness", "slowness"),
            new PotionDefinition("long_slowness", "slowness.long"),
            new PotionDefinition("water_breathing", "waterbreathing"),
            new PotionDefinition("long_water_breathing", "waterbreathing.long"),
            new PotionDefinition("healing", "health"),
            new PotionDefinition("strong_healing", "health.strong"),
            new PotionDefinition("harming", "damage"),
            new PotionDefinition("strong_harming", "damage.strong"),
            new PotionDefinition("poison", "poison"),
            new PotionDefinition("long_poison", "poison.long"),
            new PotionDefinition("strong_poison", "poison.strong"),
            new PotionDefinition("regeneration", "regen"),
            new PotionDefinition("long_regeneration", "regen.long"),
            new PotionDefinition("strong_regeneration", "regen.strong"),
            new PotionDefinition("strength", "strength"),
            new PotionDefinition("long_strength", "strength.long"),
            new PotionDefinition("strong_strength", "strength.strong"),
            new PotionDefinition("weakness", "weakness"),
            new PotionDefinition("long_weakness", "weakness.long"),
            new PotionDefinition("luck", "luck")
    ));

    private static boolean registered;

    private GT6PotionFluids() {}

    public static void register() {
        if (registered) return;
        registered = true;

        for (PotionDefinition definition : DEFINITIONS) {
            PotionType potionType = getPotionType(definition.registryPath);
            if (potionType == null) continue;

            int color = 0xFF000000 | PotionUtils.getPotionColor(potionType);
            for (PotionForm form : PotionForm.values()) {
                String fluidName = getFluidName(definition, form);
                if (FluidRegistry.getFluid(fluidName) == null) {
                    PotionFluid fluid = new PotionFluid(fluidName, definition.fluidPath, form, color);
                    FluidRegistry.registerFluid(fluid);
                }
            }
        }
    }

    public static List<PotionDefinition> getDefinitions() {
        return DEFINITIONS;
    }

    public static PotionType getPotionType(String registryPath) {
        return PotionType.REGISTRY.getObject(new ResourceLocation("minecraft", registryPath));
    }

    public static PotionDefinition getDefinition(PotionType potionType) {
        ResourceLocation id = PotionType.REGISTRY.getNameForObject(potionType);
        if (id == null || !"minecraft".equals(id.getNamespace())) return null;

        for (PotionDefinition definition : DEFINITIONS) {
            if (definition.registryPath.equals(id.getPath())) return definition;
        }
        return null;
    }

    public static Fluid getFluid(PotionType potionType, PotionForm form) {
        PotionDefinition definition = getDefinition(potionType);
        return definition == null ? null : FluidRegistry.getFluid(getFluidName(definition, form));
    }

    public static FluidStack getFluidStack(PotionType potionType, PotionForm form, int amount) {
        Fluid fluid = getFluid(potionType, form);
        return fluid == null ? null : new FluidStack(fluid, amount);
    }

    private static String getFluidName(PotionDefinition definition, PotionForm form) {
        return "potion." + definition.fluidPath + form.fluidSuffix;
    }

    public enum PotionForm {
        DRINKABLE("", "drinkable"),
        SPLASH(".splash", "splash"),
        LINGERING(".lingering", "lingering");

        private final String fluidSuffix;
        private final String translationKey;

        PotionForm(String fluidSuffix, String translationKey) {
            this.fluidSuffix = fluidSuffix;
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    public static final class PotionDefinition {
        private final String registryPath;
        private final String fluidPath;

        private PotionDefinition(String registryPath, String fluidPath) {
            this.registryPath = registryPath;
            this.fluidPath = fluidPath;
        }

        public String getRegistryPath() {
            return registryPath;
        }

        public PotionType getPotionType() {
            return GT6PotionFluids.getPotionType(registryPath);
        }
    }

    private static final class PotionFluid extends Fluid {
        private final String fluidPath;
        private final PotionForm form;

        private PotionFluid(String name, String fluidPath, PotionForm form, int color) {
            super(name,
                    new ResourceLocation("minecraft", "blocks/water_still"),
                    new ResourceLocation("minecraft", "blocks/water_flow"));
            this.fluidPath = fluidPath;
            this.form = form;
            setColor(color);
            setDensity(1000);
            setViscosity(1000);
            setUnlocalizedName("gt6addition.potion.generic");
        }

        @Override
        public String getLocalizedName(FluidStack stack) {
            return I18n.translateToLocalFormatted("gt6addition.fluid.potion.format",
                    I18n.translateToLocal("gt6addition.fluid.potion.form." + form.getTranslationKey()),
                    I18n.translateToLocal("gt6addition.fluid.potion.type." + fluidPath));
        }
    }
}
