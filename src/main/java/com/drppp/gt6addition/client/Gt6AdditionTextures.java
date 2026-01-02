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
    public static void init()
    {
        HU_BASE_BURRING_BOX= new OrientedOverlayRenderer("machines/hu_base_burring_box");
        HU_BASE_BURRING_BOX_LIQUID= new OrientedOverlayRenderer("machines/hu_base_burring_box_liquid");
        HU_BURRING_BOX_SIDE_OVERLAY = new SimpleOverlayRenderer("casings/hu_burring_box_side_overlay");
        HU_BURRING_BOX_SIDE_FULL_OVERLAY = new SimpleOverlayRenderer("casings/hu_burring_box_side_full_overlay");
    }
}
