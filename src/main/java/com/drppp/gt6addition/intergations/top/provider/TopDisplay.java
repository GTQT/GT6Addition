package com.drppp.gt6addition.intergations.top.provider;

import gregtech.api.util.TextFormattingUtil;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.Locale;

/** Shared TOP presentation. Translation tokens are resolved by the viewing client. */
final class TopDisplay {
    static final int GREEN = 0xFF77AA66;
    static final int HEAT = 0xFFFF6600;
    static final int COLD = 0xFF4DA6FF;
    static final int KINETIC = 0xFF66CCFF;
    private static final String COMMON = "gt6addition.top.common.";

    private TopDisplay() {}

    static String localize(String key) {
        return IProbeInfo.STARTLOC + key + IProbeInfo.ENDLOC;
    }

    static String common(String key) {
        return localize(COMMON + key);
    }

    static String number(long value) {
        return TextFormattingUtil.formatNumbers(value);
    }

    static void row(IProbeInfo info, String key, String value) {
        row(info, key, value, TextFormatting.GREEN);
    }

    static void row(IProbeInfo info, String key, String value, TextFormatting color) {
        info.text(TextFormatting.WHITE.toString() + TextFormatting.BOLD + localize(key) +
                TextFormatting.RESET + " " + color + value + TextFormatting.RESET);
    }

    static void commonRow(IProbeInfo info, String key, String value) {
        row(info, COMMON + key, value);
    }

    static void status(IProbeInfo info, String state, TextFormatting color) {
        row(info, COMMON + "status", common("state." + state), color);
    }

    static void energyType(IProbeInfo info, String unit) {
        String name;
        switch (unit) {
            case "HU": case "KU": case "RU": case "CU": case "MU": case "LU": case "EU":
                name = common("energy." + unit.toLowerCase(Locale.ROOT)) +
                        TextFormatting.GRAY + " (" + unit + ")";
                break;
            default:
                name = unit;
        }
        commonRow(info, "energy_type", name);
    }

    static void energyRate(IProbeInfo info, String label, long value, String unit) {
        commonRow(info, label, number(Math.max(0L, value)) + TextFormatting.GRAY + " " + unit + "/t");
    }

    static void item(IProbeInfo info, String label, ItemStack stack) {
        if (stack.isEmpty()) {
            commonRow(info, label, common("empty"));
            return;
        }
        IProbeInfo line = info.horizontal();
        line.item(stack.copy());
        commonRow(line, label, stack.getDisplayName() + TextFormatting.GRAY + " ×" + number(stack.getCount()));
    }

    static void tank(IProbeInfo info, String label, IFluidTank tank) {
        FluidStack fluid = tank.getFluid();
        String name = fluid == null ? common("empty") : fluid.getLocalizedName();
        commonRow(info, label, name + TextFormatting.GRAY + " · " +
                number(Math.max(0, tank.getFluidAmount())) + " / " + number(tank.getCapacity()) + " L");
    }

    static void bar(IProbeInfo info, String label, long value, long maximum, String unit, int color) {
        // Clamp the fill only: the caption must still reveal an over-limit temperature or buffer.
        long limit = Math.max(1L, maximum);
        String caption = number(Math.max(0L, value)) +
                " / " + number(Math.max(0L, maximum)) + (unit.isEmpty() ? "" : " " + unit);
        row(info, label, caption);
        // TOP 1.12 resolves translation tokens in text elements, not progress captions.
        info.progress(Math.max(0L, Math.min(value, limit)), limit, info.defaultProgressStyle()
                .showText(false).width(160).height(6)
                .filledColor(color).alternateFilledColor(color)
                .borderColor(0xFF555555).backgroundColor(0xFF111111));
    }

    static void percentBar(IProbeInfo info, String label, long value, long maximum, int color) {
        long percent = maximum <= 0L ? 0L :
                Math.round(Math.max(0.0D, Math.min(1.0D, (double) value / maximum)) * 100.0D);
        row(info, label, percent + "%");
        info.progress(percent, 100L, info.defaultProgressStyle()
                .showText(false).width(160).height(6)
                .filledColor(color).alternateFilledColor(color)
                .borderColor(0xFF555555).backgroundColor(0xFF111111));
    }

    static String direction(String direction) {
        return common("direction." + direction.toLowerCase(Locale.ROOT));
    }
}
