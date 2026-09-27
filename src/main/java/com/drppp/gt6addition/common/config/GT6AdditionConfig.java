package com.drppp.gt6addition.common.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

/** Global server-side policy for capabilities relayed by miniature portals. */
public final class GT6AdditionConfig {

    private static Configuration configuration;

    public static boolean portalRelayItems = true;
    public static boolean portalRelayFluids = true;
    public static boolean portalRelayGtEnergy = true;
    public static boolean portalRelayRedstoneComparator = true;
    public static boolean generateAnthraciteVeins = true;

    private GT6AdditionConfig() {
    }

    public static void init(File configFile) {
        configuration = new Configuration(configFile);
        sync();
    }

    private static void sync() {
        String category = "mini_portal";
        portalRelayItems = configuration.getBoolean("allowItemTransfer", category, true,
                "总开关：允许微型传送门跨维度传送物品。需要传送门自身也启用物品传送。");
        portalRelayFluids = configuration.getBoolean("allowFluidTransfer", category, true,
                "总开关：允许微型传送门跨维度传送流体。需要传送门自身也启用流体传送。");
        portalRelayGtEnergy = configuration.getBoolean("allowGtEnergyTransfer", category, true,
                "总开关：允许微型传送门跨维度传送 GregTech 能量。需要传送门自身也启用能量传送。");
        portalRelayRedstoneComparator = configuration.getBoolean("allowRedstoneAndComparator", category, true,
                "总开关：允许微型传送门跨维度传送红石信号和比较器信号。需要传送门自身也启用信号传送。");
        Property anthraciteVeins = configuration.get("world_generation", "generateAnthraciteVeins", true,
                "是否生成 GT6 风格的无烟煤矿层。\nWhether to generate GT6-style anthracite ore layers.");
        anthraciteVeins.setLanguageKey("gt6addition.config.world_generation.generate_anthracite_veins");
        generateAnthraciteVeins = anthraciteVeins.getBoolean(true);
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
