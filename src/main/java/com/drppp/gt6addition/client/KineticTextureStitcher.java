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
            "machines/kinetic/iconsets/axle",
            "machines/kinetic/iconsets/axle_clockwise",
            "machines/kinetic/iconsets/axle_counterclockwise",
            "machines/kinetic/iconsets/axle_horizontal",
            "machines/kinetic/iconsets/axle_vertical",
            "machines/kinetic/iconsets/gear",
            "machines/kinetic/iconsets/gear_clockwise",
            "machines/kinetic/iconsets/gear_counterclockwise",
            "machines/kinetic/iconsets/gearbox",
            "machines/kinetic/iconsets/gearbox_axle",
            "machines/automation/hopper/colored/bottom",
            "machines/automation/hopper/colored/side",
            "machines/automation/hopper/colored/top",
            "machines/automation/hopper/overlay/bottom",
            "machines/automation/hopper/overlay/side",
            "machines/automation/hopper/overlay/top",
            "machines/automation/queuehopper/colored/bottom",
            "machines/automation/queuehopper/colored/side",
            "machines/automation/queuehopper/colored/top",
            "machines/automation/queuehopper/overlay/bottom",
            "machines/automation/queuehopper/overlay/side",
            "machines/automation/queuehopper/overlay/top",
            "machines/engines/kinetic_steam/colored/back",
            "machines/engines/kinetic_steam/colored/cage",
            "machines/engines/kinetic_steam/colored/engine",
            "machines/engines/kinetic_steam/colored/engine_hull",
            "machines/engines/kinetic_steam/colored/front",
            "machines/engines/kinetic_steam/colored/pipe",
            "machines/engines/kinetic_steam/colored/pipe_side",
            "machines/engines/kinetic_steam/colored/side",
            "machines/engines/kinetic_steam/overlay/back",
            "machines/engines/kinetic_steam/overlay/cage",
            "machines/engines/kinetic_steam/overlay/engine",
            "machines/engines/kinetic_steam/overlay/engine_hull",
            "machines/engines/kinetic_steam/overlay/front",
            "machines/engines/kinetic_steam/overlay/pipe",
            "machines/engines/kinetic_steam/overlay/pipe_side",
            "machines/engines/kinetic_steam/overlay/side"
    };

    private KineticTextureStitcher() {
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (String texture : DYNAMIC_BLOCK_TEXTURES) {
            event.getMap().registerSprite(new ResourceLocation("gregtech", "blocks/" + texture));
        }
    }
}
