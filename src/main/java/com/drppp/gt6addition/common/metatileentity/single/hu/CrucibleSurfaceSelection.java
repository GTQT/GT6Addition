package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/** Density selection precedes extraction eligibility, as in GT6 Smeltery. */
final class CrucibleSurfaceSelection {
    private CrucibleSurfaceSelection() {}

    static <T> T lightest(Iterable<T> contents, Predicate<T> included, ToDoubleFunction<T> density) {
        T selected = null;
        double selectedDensity = 0;
        for (T entry : contents) {
            if (entry == null || !included.test(entry)) continue;
            double entryDensity = density.applyAsDouble(entry);
            if (selected == null || entryDensity < selectedDensity) {
                selected = entry;
                selectedDensity = entryDensity;
            }
        }
        return selected;
    }
}
