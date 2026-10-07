package com.drppp.gt6addition.common.event;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.common.material.GT6MachineMaterials;
import com.drppp.gt6addition.api.utils.MaterialColorUtil;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class EventHandlers {

    @SubscribeEvent
    public static void registerMTERegistry(MTEManager.MTERegistryEvent event) {
        GregTechAPI.mteManager.createRegistry(Tags.MOD_ID);
    }

    @SubscribeEvent
    public static void createMaterialRegistry(MaterialRegistryEvent event) {
        GregTechAPI.materialManager.createRegistry(Tags.MOD_ID);
    }

    @SubscribeEvent
    public static void registerMaterials(MaterialEvent event) {
        GT6MachineMaterials.register();
        MaterialColorUtil.init();
    }

}
