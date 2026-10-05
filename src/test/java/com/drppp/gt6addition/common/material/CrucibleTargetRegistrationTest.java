package com.drppp.gt6addition.common.material;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.material.registry.MarkerMaterialRegistry;
import gregtech.core.unification.material.internal.MaterialRegistryManager;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Real dependency builders and registry, without pretending to load GTQTCore. */
class CrucibleTargetRegistrationTest {
    private IMaterialRegistryManager previousManager;
    private MarkerMaterialRegistry previousMarkers;
    private MaterialRegistryManager registry;
    private final Map<Field, Object> previousFields = new LinkedHashMap<>();

    @BeforeEach void setUp() throws Exception {
        Bootstrap.register();
        previousManager = GregTechAPI.materialManager;
        previousMarkers = GregTechAPI.markerMaterialRegistry;
        if (previousMarkers == null) GregTechAPI.markerMaterialRegistry = MarkerMaterialRegistry.getInstance();
        for (String name : new String[]{"BandedIron", "Calcium", "Fluorine", "Niobium", "Oxygen", "Pyrolusite"}) {
            Field field = Materials.class.getField(name);
            previousFields.put(field, field.get(null));
        }
        for (String name : new String[]{"HEMATITE", "FLUORITE", "NIOBIUM_PENTOXIDE", "COLUMBITE",
                "ADAMANTIUM", "ADAMANTINE", "DOLAMIDE"}) {
            Field field = GT6MachineMaterials.class.getField(name);
            previousFields.put(field, field.get(null));
        }
        Constructor<MaterialRegistryManager> constructor = MaterialRegistryManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        registry = constructor.newInstance();
        GregTechAPI.materialManager = registry;
        registry.createRegistry("gt6addition");
        registry.createRegistry("gtqtcore");
        registry.unfreezeRegistries();
        int id = 1;
        for (String name : new String[]{"BandedIron", "Calcium", "Fluorine", "Niobium", "Oxygen", "Pyrolusite"}) {
            Materials.class.getField(name).set(null, new Material.Builder(id++,
                    new ResourceLocation("gregtech", "fixture_" + name.toLowerCase(java.util.Locale.ROOT))).dust().build());
        }
    }

    @AfterEach void restore() throws Exception {
        for (Map.Entry<Field, Object> entry : previousFields.entrySet()) entry.getKey().set(null, entry.getValue());
        GregTechAPI.materialManager = previousManager;
        GregTechAPI.markerMaterialRegistry = previousMarkers;
    }

    @Test void missingTargetsAreRegisteredWithValidHostProperties() {
        GT6MachineMaterials.registerCrucibleTargets();
        assertSame(Materials.BandedIron, GT6MachineMaterials.HEMATITE);
        assertEquals("gt6addition:fluorite", GT6MachineMaterials.FLUORITE.getRegistryName());
        assertTrue(GT6MachineMaterials.FLUORITE.hasProperty(PropertyKey.GEM));
        assertFalse(GT6MachineMaterials.FLUORITE.hasProperty(PropertyKey.INGOT));
        assertTrue(GT6MachineMaterials.FLUORITE.hasProperty(PropertyKey.FLUID));
        assertTrue(GT6MachineMaterials.ADAMANTIUM.hasProperty(PropertyKey.FLUID));
        assertFalse(GT6MachineMaterials.ADAMANTINE.hasProperty(PropertyKey.FLUID));
        assertFalse(GT6MachineMaterials.DOLAMIDE.hasProperty(PropertyKey.FLUID));
        assertSame(GT6MachineMaterials.ADAMANTIUM,
                GT6MachineMaterials.ADAMANTINE.getMaterialComponents().get(0).material);
        assertEquals(3, GT6MachineMaterials.ADAMANTINE.getMaterialComponents().get(0).amount);
        assertColumbiteComponents();
    }

    @Test void verifiedCoreTargetsAreReusedRatherThanDuplicated() {
        Material coreFluorite = new Material.Builder(1, new ResourceLocation("gtqtcore", "fluorite")).gem().ore().build();
        Material coreOxide = new Material.Builder(2, new ResourceLocation("gtqtcore", "niobium_pentoxide")).dust().build();
        GT6MachineMaterials.registerCrucibleTargets();
        assertSame(coreFluorite, GT6MachineMaterials.FLUORITE);
        assertSame(coreOxide, GT6MachineMaterials.NIOBIUM_PENTOXIDE);
        assertNull(registry.getMaterial("gt6addition:fluorite"));
        assertNull(registry.getMaterial("gt6addition:niobium_pentoxide"));
        assertColumbiteComponents();
    }

    @Test void absentCoreNamespaceCannotMisidentifyHostFallbackAsCoreMaterial() throws Exception {
        // A fresh manager without the optional namespace reproduces CEu's
        // getRegistry fallback. The actual identity must still be gregtech.
        Constructor<MaterialRegistryManager> constructor = MaterialRegistryManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        registry = constructor.newInstance();
        GregTechAPI.materialManager = registry;
        registry.createRegistry("gt6addition");
        registry.unfreezeRegistries();
        Material hostFluorite = new Material.Builder(1, new ResourceLocation("gregtech", "fluorite")).gem().ore().build();
        Material hostOxide = new Material.Builder(2, new ResourceLocation("gregtech", "niobium_pentoxide")).dust().build();
        GT6MachineMaterials.registerCrucibleTargets();
        assertSame(hostFluorite, GT6MachineMaterials.FLUORITE);
        assertSame(hostOxide, GT6MachineMaterials.NIOBIUM_PENTOXIDE);
        assertNull(registry.getMaterial("gt6addition:fluorite"));
        assertColumbiteComponents();
    }

    private static void assertColumbiteComponents() {
        assertEquals(2, GT6MachineMaterials.COLUMBITE.getMaterialComponents().size());
        assertSame(GT6MachineMaterials.NIOBIUM_PENTOXIDE,
                GT6MachineMaterials.COLUMBITE.getMaterialComponents().get(0).material);
        assertEquals(7, GT6MachineMaterials.COLUMBITE.getMaterialComponents().get(0).amount);
        assertSame(Materials.Pyrolusite, GT6MachineMaterials.COLUMBITE.getMaterialComponents().get(1).material);
        assertEquals(1, GT6MachineMaterials.COLUMBITE.getMaterialComponents().get(1).amount);
    }
}
