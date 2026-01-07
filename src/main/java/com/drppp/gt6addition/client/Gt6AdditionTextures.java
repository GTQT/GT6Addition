package com.drppp.gt6addition.client;

import com.cleanroommc.modularui.drawable.UITexture;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;

import static com.cleanroommc.modularui.drawable.UITexture.fullImage;

public class Gt6AdditionTextures {
    public static final UITexture GREGTECH6_LOGO = fullImage("gregtech","textures/gui/icon/gregtech6.png");
    public static OrientedOverlayRenderer HU_BASE_BURRING_BOX;
    public static OrientedOverlayRenderer HU_BASE_BURRING_BOX_LIQUID;
    public static SimpleOverlayRenderer HU_BURRING_BOX_SIDE_OVERLAY;
    public static  SimpleOverlayRenderer HU_BURRING_BOX_SIDE_FULL_OVERLAY;
    public static final SimpleSidedCubeRenderer BASE_BURRING_BOX_TEXTURE = new SimpleSidedCubeRenderer("casings/gt6base/base");
    public static final SimpleSidedCubeRenderer BASE_NULL_TEXTURE = new SimpleSidedCubeRenderer("base/null");
    public static final SimpleSidedCubeRenderer[] MACHINE_BASES = new SimpleSidedCubeRenderer[6];
    public static OrientedOverlayRenderer RU_BENDER;
    public static OrientedOverlayRenderer RU_WIREMILL;
    public static OrientedOverlayRenderer RU_STEAM_TURBINE;
    public static OrientedOverlayRenderer RU_ELECTRIC_MOTOR;
    public static OrientedOverlayRenderer RU_DIESEL_ENGINE;
    public static OrientedOverlayRenderer RU_KU_ENGINE;
    public static OrientedOverlayRenderer RU_LATHE;
    public static OrientedOverlayRenderer RU_CUTTING_SAW;
    public static OrientedOverlayRenderer RU_CENTRIFUGE;
    public static OrientedOverlayRenderer RU_LOOM;
    public static OrientedOverlayRenderer RU_ORE_WASHER;
    public static OrientedOverlayRenderer RU_MIXER;
    public static OrientedOverlayRenderer HU_OVEN;
    public static OrientedOverlayRenderer HU_DISTILLERY;
    public static OrientedOverlayRenderer HU_LAMINATOR;
    public static OrientedOverlayRenderer HU_ROASTER;
    public static OrientedOverlayRenderer HU_FERMENTER;
    public static OrientedOverlayRenderer HU_EXTRUDER;
    public static OrientedOverlayRenderer KU_COMPRESSOR;
    public static OrientedOverlayRenderer KU_HAMMER;
    public static OrientedOverlayRenderer KU_SIFTER;
    public static OrientedOverlayRenderer KU_FORMING_PRESS;
    public static void init()
    {
        HU_BASE_BURRING_BOX= new OrientedOverlayRenderer("machines/hu_base_burring_box");
        HU_BASE_BURRING_BOX_LIQUID= new OrientedOverlayRenderer("machines/hu_base_burring_box_liquid");
        HU_BURRING_BOX_SIDE_OVERLAY = new SimpleOverlayRenderer("casings/hu_burring_box_side_overlay");
        HU_BURRING_BOX_SIDE_FULL_OVERLAY = new SimpleOverlayRenderer("casings/hu_burring_box_side_full_overlay");
        RU_BENDER= new OrientedOverlayRenderer("machines/ru_machines/ru_bender");
        RU_WIREMILL= new OrientedOverlayRenderer("machines/ru_machines/ru_wiremill");
        MACHINE_BASES[1] = new SimpleSidedCubeRenderer("base/bronze");
        MACHINE_BASES[2] = new SimpleSidedCubeRenderer("base/steel");
        MACHINE_BASES[3] = new SimpleSidedCubeRenderer("base/stainless_steel");
        MACHINE_BASES[4] = new SimpleSidedCubeRenderer("base/titanium");
        MACHINE_BASES[5] = new SimpleSidedCubeRenderer("base/tungsten_steel");
        RU_STEAM_TURBINE = new OrientedOverlayRenderer("machines/ru_machines/steam_turbine");
        RU_ELECTRIC_MOTOR = new OrientedOverlayRenderer("machines/ru_machines/electric_motor");
        RU_DIESEL_ENGINE = new OrientedOverlayRenderer("machines/ru_machines/diesel_engine");
        RU_KU_ENGINE = new OrientedOverlayRenderer("machines/ku_machines/ru_ku_engine");
        RU_LATHE = new OrientedOverlayRenderer("machines/ru_machines/lathe");
        RU_CUTTING_SAW = new OrientedOverlayRenderer("machines/ru_machines/cutting_saw");
        RU_CENTRIFUGE = new OrientedOverlayRenderer("machines/ru_machines/centrifuge");
        RU_LOOM = new OrientedOverlayRenderer("machines/ru_machines/loom");
        RU_ORE_WASHER = new OrientedOverlayRenderer("machines/ru_machines/orewasher");
        RU_MIXER = new OrientedOverlayRenderer("machines/ru_machines/mixer");
        HU_OVEN = new OrientedOverlayRenderer("machines/hu_machines/oven");
        HU_DISTILLERY = new OrientedOverlayRenderer("machines/hu_machines/distillery");
        HU_LAMINATOR = new OrientedOverlayRenderer("machines/hu_machines/laminator");
        HU_ROASTER = new OrientedOverlayRenderer("machines/hu_machines/roaster");
        HU_FERMENTER = new OrientedOverlayRenderer("machines/hu_machines/fermenter");
        HU_EXTRUDER = new OrientedOverlayRenderer("machines/hu_machines/extruder");
        KU_COMPRESSOR = new OrientedOverlayRenderer("machines/ku_machines/compressor");
        KU_HAMMER = new OrientedOverlayRenderer("machines/ku_machines/hammer");
        KU_SIFTER = new OrientedOverlayRenderer("machines/ku_machines/sifter");
        KU_FORMING_PRESS = new OrientedOverlayRenderer("machines/ku_machines/forming_press");
    }
}
