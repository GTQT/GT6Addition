package com.drppp.gt6addition.api.utils;

import java.util.HashMap;
import java.util.Map;

public class MaterialColorUtil {
    public static Map<MaterialName, Integer> MaterialColor = new HashMap<>();
    public static void init()
    {
        MaterialColor.put(MaterialName.lead,0x251945);
        MaterialColor.put(MaterialName.bronze,0x815024);
        MaterialColor.put(MaterialName.steel,0x4F4F4E);
        MaterialColor.put(MaterialName.invar,0x87875C);
        MaterialColor.put(MaterialName.chrome,0xA39393);
        MaterialColor.put(MaterialName.titanium,0x896495);
        MaterialColor.put(MaterialName.tungsten,0x1D1D1D);
        MaterialColor.put(MaterialName.tungsten_steel,0x3C3C61);
        MaterialColor.put(MaterialName.tungsten_carbide,0x4F4F4F);
        MaterialColor.put(MaterialName.aluminum,0x8bd4d2);
        MaterialColor.put(MaterialName.stain_steel,0x90a5b6);
        MaterialColor.put(MaterialName.galvanized_steel,0xd9d0d0);
    }
    public enum MaterialName
    {
        lead,
        bronze,
        steel,
        invar,
        chrome,
        titanium,
        tungsten,
        tungsten_steel,
        tungsten_carbide,
        aluminum,
        stain_steel,
        galvanized_steel
    }
}
