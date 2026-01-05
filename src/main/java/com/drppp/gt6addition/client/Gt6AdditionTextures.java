package com.drppp.gt6addition.client;

import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;

public class Gt6AdditionTextures {
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
    }
}
