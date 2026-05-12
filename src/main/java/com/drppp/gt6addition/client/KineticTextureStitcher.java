package com.drppp.gt6addition.client;

import com.drppp.gt6addition.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class KineticTextureStitcher {

    private static final String[] DYNAMIC_BLOCK_TEXTURES = {
            "gregtech:blocks/machines/kinetic/iconsets/axle",
            "gregtech:blocks/machines/kinetic/iconsets/axle_clockwise",
            "gregtech:blocks/machines/kinetic/iconsets/axle_counterclockwise",
            "gregtech:blocks/machines/kinetic/iconsets/axle_horizontal",
            "gregtech:blocks/machines/kinetic/iconsets/axle_vertical",
            "gregtech:blocks/machines/kinetic/iconsets/gear",
            "gregtech:blocks/machines/kinetic/iconsets/gear_clockwise",
            "gregtech:blocks/machines/kinetic/iconsets/gear_counterclockwise",
            "gregtech:blocks/machines/kinetic/iconsets/gearbox",
            "gregtech:blocks/machines/kinetic/iconsets/gearbox_axle",
            "gregtech:blocks/machines/automation/hopper/colored/bottom",
            "gregtech:blocks/machines/automation/hopper/colored/side",
            "gregtech:blocks/machines/automation/hopper/colored/top",
            "gregtech:blocks/machines/automation/hopper/overlay/bottom",
            "gregtech:blocks/machines/automation/hopper/overlay/side",
            "gregtech:blocks/machines/automation/hopper/overlay/top",
            "gregtech:blocks/machines/automation/queuehopper/colored/bottom",
            "gregtech:blocks/machines/automation/queuehopper/colored/side",
            "gregtech:blocks/machines/automation/queuehopper/colored/top",
            "gregtech:blocks/machines/automation/queuehopper/overlay/bottom",
            "gregtech:blocks/machines/automation/queuehopper/overlay/side",
            "gregtech:blocks/machines/automation/queuehopper/overlay/top",
            "gregtech:blocks/machines/engines/kinetic_steam/colored/back",
            "gregtech:blocks/machines/engines/kinetic_steam/colored/cage",
            "gregtech:blocks/machines/engines/kinetic_steam/colored/engine",
            "gregtech:blocks/machines/engines/kinetic_steam/colored/engine_hull",
            "gregtech:blocks/machines/engines/kinetic_steam/colored/front",
            "gregtech:blocks/machines/engines/kinetic_steam/colored/pipe",
            "gregtech:blocks/machines/engines/kinetic_steam/colored/pipe_side",
            "gregtech:blocks/machines/engines/kinetic_steam/colored/side",
            "gregtech:blocks/machines/engines/kinetic_steam/overlay/back",
            "gregtech:blocks/machines/engines/kinetic_steam/overlay/cage",
            "gregtech:blocks/machines/engines/kinetic_steam/overlay/engine",
            "gregtech:blocks/machines/engines/kinetic_steam/overlay/engine_hull",
            "gregtech:blocks/machines/engines/kinetic_steam/overlay/front",
            "gregtech:blocks/machines/engines/kinetic_steam/overlay/pipe",
            "gregtech:blocks/machines/engines/kinetic_steam/overlay/pipe_side",
            "gregtech:blocks/machines/engines/kinetic_steam/overlay/side",
            "gt6addition:blocks/machines/pump/rotation/colored/front",
            "gt6addition:blocks/machines/pump/rotation/colored/back",
            "gt6addition:blocks/machines/pump/rotation/colored/side",
            "gt6addition:blocks/machines/pump/rotation/overlay/front",
            "gt6addition:blocks/machines/pump/rotation/overlay/back",
            "gt6addition:blocks/machines/pump/rotation/overlay/side",
            "gt6addition:blocks/machines/pump/rotation/overlay_active/front",
            "gt6addition:blocks/machines/pump/rotation/overlay_active/back",
            "gt6addition:blocks/machines/pump/rotation/overlay_active/side",
            "gt6addition:blocks/machines/tools/mortar/colored/bottom",
            "gt6addition:blocks/machines/tools/mortar/colored/insides",
            "gt6addition:blocks/machines/tools/mortar/colored/middleside",
            "gt6addition:blocks/machines/tools/mortar/colored/middletop",
            "gt6addition:blocks/machines/tools/mortar/colored/sides",
            "gt6addition:blocks/machines/tools/mortar/colored/top",
            "gt6addition:blocks/machines/tools/mortar/overlay/bottom",
            "gt6addition:blocks/machines/tools/mortar/overlay/insides",
            "gt6addition:blocks/machines/tools/mortar/overlay/middleside",
            "gt6addition:blocks/machines/tools/mortar/overlay/middletop",
            "gt6addition:blocks/machines/tools/mortar/overlay/sides",
            "gt6addition:blocks/machines/tools/mortar/overlay/top"
    };

    private KineticTextureStitcher() {
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (String texture : DYNAMIC_BLOCK_TEXTURES) {
            event.getMap().registerSprite(new ResourceLocation(texture));
        }
    }
}
