package com.drppp.gt6addition.api.utils;

import com.drppp.gt6addition.common.material.GT6MachineMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** Material colours copied from GT6's gregapi.data.MT definitions. */
public final class MaterialColorUtil {
    private static final Map<MaterialName, Integer> COLORS = new EnumMap<>(MaterialName.class);
    private static final Map<Material, MaterialName> MATERIAL_NAMES = new IdentityHashMap<>();

    private MaterialColorUtil() {}

    public static void init() {
        color(MaterialName.LEAD, 60, 40, 110);
        color(MaterialName.BISMUTH, 100, 160, 160);
        color(MaterialName.BRONZE, 210, 130, 60);
        color(MaterialName.ARSENIC_COPPER, 210, 160, 60);
        color(MaterialName.ARSENIC_BRONZE, 200, 200, 222);
        color(MaterialName.INVAR, 220, 220, 150);
        color(MaterialName.STEEL, 130, 130, 130);
        color(MaterialName.CHROME, 255, 230, 230);
        color(MaterialName.TITANIUM, 220, 160, 240);
        color(MaterialName.ANCIENT_DEBRIS, 110, 80, 90);
        color(MaterialName.NETHERITE, 80, 70, 80);
        color(MaterialName.TUNGSTEN, 50, 50, 50);
        color(MaterialName.TUNGSTEN_STEEL, 100, 100, 160);
        color(MaterialName.TANTALUM_HAFNIUM_CARBIDE, 32, 128, 32);
        color(MaterialName.ALUMINIUM, 128, 200, 240);
        color(MaterialName.STAINLESS_STEEL, 200, 200, 220);
        color(MaterialName.GALVANIZED_STEEL, 250, 240, 240);
        color(MaterialName.TUNGSTEN_CARBIDE, 123, 123, 123);
        color(MaterialName.STONE, 205, 205, 205);
        color(MaterialName.BASALT, 60, 50, 50);
        color(MaterialName.BLACK_GRANITE, 20, 20, 20);
        color(MaterialName.RED_GRANITE, 160, 60, 70);
        color(MaterialName.NETHER_QUARTZ, 230, 210, 210);
        color(MaterialName.CARBON, 20, 20, 20);
        color(MaterialName.MOLYBDENUM, 180, 180, 220);
        color(MaterialName.NIOBIUM, 190, 180, 200);
        color(MaterialName.TANTALUM, 120, 120, 140);
        color(MaterialName.OSMIUM, 50, 50, 255);
        color(MaterialName.IRIDIUM, 240, 240, 245);
        color(MaterialName.NIOBIUM_TITANIUM, 29, 29, 41);
        color(MaterialName.VANADIUM, 50, 50, 50);
        color(MaterialName.CLAY, 200, 200, 220);
        color(MaterialName.CERAMIC, 220, 130, 70);
        color(MaterialName.BRICK, 183, 90, 64);

        bind(Materials.Lead, MaterialName.LEAD);
        bind(Materials.Bismuth, MaterialName.BISMUTH);
        bind(Materials.Bronze, MaterialName.BRONZE);
        bind(Materials.Invar, MaterialName.INVAR);
        bind(Materials.Steel, MaterialName.STEEL);
        bind(Materials.Chrome, MaterialName.CHROME);
        bind(Materials.Titanium, MaterialName.TITANIUM);
        bind(Materials.Tungsten, MaterialName.TUNGSTEN);
        bind(Materials.TungstenSteel, MaterialName.TUNGSTEN_STEEL);
        bind(Materials.Aluminium, MaterialName.ALUMINIUM);
        bind(Materials.StainlessSteel, MaterialName.STAINLESS_STEEL);
        bind(Materials.TungstenCarbide, MaterialName.TUNGSTEN_CARBIDE);
        bind(Materials.Stone, MaterialName.STONE);
        bind(Materials.Basalt, MaterialName.BASALT);
        bind(Materials.GraniteBlack, MaterialName.BLACK_GRANITE);
        bind(Materials.GraniteRed, MaterialName.RED_GRANITE);
        bind(Materials.NetherQuartz, MaterialName.NETHER_QUARTZ);
        bind(Materials.Carbon, MaterialName.CARBON);
        bind(Materials.Molybdenum, MaterialName.MOLYBDENUM);
        bind(Materials.Niobium, MaterialName.NIOBIUM);
        bind(Materials.Tantalum, MaterialName.TANTALUM);
        bind(Materials.Osmium, MaterialName.OSMIUM);
        bind(Materials.Iridium, MaterialName.IRIDIUM);
        bind(Materials.NiobiumTitanium, MaterialName.NIOBIUM_TITANIUM);
        bind(Materials.Vanadium, MaterialName.VANADIUM);
        bind(Materials.Clay, MaterialName.CLAY);
        bind(Materials.Brick, MaterialName.BRICK);
        bind(GT6MachineMaterials.ARSENIC_COPPER, MaterialName.ARSENIC_COPPER);
        bind(GT6MachineMaterials.ARSENIC_BRONZE, MaterialName.ARSENIC_BRONZE);
        bind(GT6MachineMaterials.ANCIENT_DEBRIS, MaterialName.ANCIENT_DEBRIS);
        bind(GT6MachineMaterials.NETHERITE, MaterialName.NETHERITE);
        bind(GT6MachineMaterials.TANTALUM_HAFNIUM_CARBIDE, MaterialName.TANTALUM_HAFNIUM_CARBIDE);
    }

    public static int get(MaterialName name) {
        Integer color = COLORS.get(name);
        if (color == null) throw new IllegalArgumentException("No GT6 colour for " + name);
        return color;
    }

    public static int get(Material material) {
        MaterialName name = MATERIAL_NAMES.get(material);
        if (name == null) throw new IllegalArgumentException("No GT6 colour mapped for " + material);
        return get(name);
    }

    private static void color(MaterialName name, int red, int green, int blue) {
        COLORS.put(name, (red << 16) | (green << 8) | blue);
    }

    private static void bind(Material material, MaterialName name) {
        if (material != null) MATERIAL_NAMES.put(material, name);
    }

    public enum MaterialName {
        LEAD, BISMUTH, BRONZE, ARSENIC_COPPER, ARSENIC_BRONZE, INVAR, STEEL, CHROME, TITANIUM,
        ANCIENT_DEBRIS, NETHERITE, TUNGSTEN, TUNGSTEN_STEEL, TANTALUM_HAFNIUM_CARBIDE, ALUMINIUM,
        STAINLESS_STEEL, GALVANIZED_STEEL, TUNGSTEN_CARBIDE, STONE, BASALT, BLACK_GRANITE,
        RED_GRANITE, NETHER_QUARTZ, CARBON, MOLYBDENUM, NIOBIUM, TANTALUM, OSMIUM, IRIDIUM,
        NIOBIUM_TITANIUM, VANADIUM, CLAY, CERAMIC, BRICK
    }
}
