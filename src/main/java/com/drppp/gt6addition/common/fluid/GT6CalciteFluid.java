package com.drppp.gt6addition.common.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/** Standalone molten-calcite fluid; it deliberately does not register or mutate a GT Material. */
public final class GT6CalciteFluid {

    public static final String FLUID_NAME = "molten.calcite";
    public static final int TEMPERATURE = 1612;
    public static final int MILLIBUCKETS_PER_DUST = 72;

    private static Fluid fluid;

    private GT6CalciteFluid() {}

    public static void register() {
        if (fluid != null) return;

        fluid = FluidRegistry.getFluid(FLUID_NAME);
        if (fluid != null) return;

        Fluid created = new MoltenCalciteFluid();
        if (FluidRegistry.registerFluid(created)) {
            fluid = created;
            FluidRegistry.addBucketForFluid(created);
            return;
        }

        // Another mod may have registered the same legacy GT6 fluid during this event.
        fluid = FluidRegistry.getFluid(FLUID_NAME);
        if (fluid == null) {
            throw new IllegalStateException("Could not register or resolve fluid " + FLUID_NAME);
        }
    }

    public static Fluid getFluid() {
        return fluid != null ? fluid : FluidRegistry.getFluid(FLUID_NAME);
    }

    public static FluidStack getFluidStack(int amount) {
        Fluid registeredFluid = getFluid();
        return registeredFluid == null || amount <= 0 ? null : new FluidStack(registeredFluid, amount);
    }

    private static final class MoltenCalciteFluid extends Fluid {
        private MoltenCalciteFluid() {
            super(FLUID_NAME,
                    new ResourceLocation("minecraft", "blocks/lava_still"),
                    new ResourceLocation("minecraft", "blocks/lava_flow"));
            setColor(0xFFF2E1C7);
            setDensity(2800);
            setViscosity(6000);
            setTemperature(TEMPERATURE);
            setUnlocalizedName("gt6addition.molten_calcite");
        }

        @Override
        public String getLocalizedName(FluidStack stack) {
            return I18n.translateToLocal("gt6addition.fluid.molten_calcite");
        }
    }
}
