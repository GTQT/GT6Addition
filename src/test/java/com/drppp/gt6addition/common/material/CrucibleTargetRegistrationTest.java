package com.drppp.gt6addition.common.material;

import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
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

/** Isolated external registry fixtures are not production material registrations. */
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
        for (String name : new String[]{"Carbon", "Coal"}) {
            Field field = Materials.class.getField(name);
            previousFields.put(field, field.get(null));
        }
        Field anthracite = GT6MachineMaterials.class.getField("ANTHRACITE");
        previousFields.put(anthracite, anthracite.get(null));
        registry = newRegistry(true);
        Material carbon = new Material.Builder(1, new ResourceLocation("gregtech", "carbon")).dust().build();
        Materials.Carbon = carbon;
        Materials.Coal = new Material.Builder(2, new ResourceLocation("gregtech", "coal")).gem().build();
    }

    private MaterialRegistryManager newRegistry(boolean core) throws Exception {
        Constructor<MaterialRegistryManager> constructor = MaterialRegistryManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        MaterialRegistryManager manager = constructor.newInstance();
        GregTechAPI.materialManager = manager;
        manager.createRegistry("gt6addition");
        if (core) manager.createRegistry("gtqtcore");
        manager.createRegistry("thirdparty");
        manager.createRegistry("anothermod");
        manager.unfreezeRegistries();
        return manager;
    }

    @AfterEach void restore() throws Exception {
        for (Map.Entry<Field, Object> entry : previousFields.entrySet()) entry.getKey().set(null, entry.getValue());
        GregTechAPI.materialManager = previousManager;
        GregTechAPI.markerMaterialRegistry = previousMarkers;
    }

    @Test void onlyAnthraciteIsActuallyRegistered() {
        GT6MachineMaterials.register();
        registry.closeRegistries();
        int owned = 0;
        for (Material material : registry.getRegisteredMaterials()) {
            if (material.getRegistryName().startsWith("gt6addition:")) {
                assertSame(GT6MachineMaterials.ANTHRACITE, material);
                owned++;
            }
        }
        assertEquals(1, owned);
        assertEquals("gt6addition:anthracite", GT6MachineMaterials.ANTHRACITE.getRegistryName());
        assertEquals(3200, GT6MachineMaterials.ANTHRACITE.getProperty(PropertyKey.DUST).getBurnTime());
        assertTrue(GT6MachineMaterials.ANTHRACITE.hasProperty(PropertyKey.GEM));
        assertTrue(GT6MachineMaterials.ANTHRACITE.hasProperty(PropertyKey.ORE));
        assertEquals(Materials.Coal, GT6MachineMaterials.ANTHRACITE.getProperty(PropertyKey.ORE).getOreByProducts().get(0));
    }

    @Test void missingTargetsStayAbsent() {
        registry.closeRegistries();
        int before = registry.getRegisteredMaterials().size();
        for (String path : new String[]{"fluorite", "columbite", "adamantium", "adamantine", "dolamide", "meat_raw"}) {
            assertNull(GT6MaterialCompatibility.findExternal(path));
            assertNull(MetaTileEntityCrucible.resolveMaterial("gt6addition:" + path));
        }
        assertEquals(before, registry.getRegisteredMaterials().size());
    }

    @Test void verifiedExternalTargetsAreReusedWithoutPropertyMutation() {
        Material core = new Material.Builder(1, new ResourceLocation("gtqtcore", "fluorite")).gem().ore().build();
        new Material.Builder(3, new ResourceLocation("gregtech", "fluorite")).gem().build();
        assertSame(core, GT6MaterialCompatibility.findExternal("fluorite"));
        assertSame(core, MetaTileEntityCrucible.resolveMaterial("gt6addition:fluorite"));
        assertTrue(core.hasProperty(PropertyKey.GEM));
        assertFalse(core.hasProperty(PropertyKey.INGOT));
        assertFalse(core.hasProperty(PropertyKey.FLUID));
        assertNull(registry.getMaterial("gt6addition:fluorite"));
    }

    @Test void absentNamespaceCannotMisidentifyHostMaterial() throws Exception {
        registry = newRegistry(false);
        Material host = new Material.Builder(1, new ResourceLocation("gregtech", "fluorite")).gem().build();
        registry.closeRegistries();
        assertSame(host, GT6MaterialCompatibility.findExternal("fluorite"));
        assertSame(host, MetaTileEntityCrucible.resolveMaterial("gt6addition:fluorite"));
        assertNull(GT6MaterialCompatibility.findExternal("gtqtcore:fluorite"));
        assertNull(MetaTileEntityCrucible.resolveMaterial("absent:fluorite"));
    }

    @Test void otherModTargetsWorkButAmbiguousNamesAreRejected() {
        Material external = new Material.Builder(1, new ResourceLocation("thirdparty", "arsenic_copper")).ingot().build();
        assertSame(external, GT6MaterialCompatibility.findExternal("arsenic_copper"));
        assertSame(external, MetaTileEntityCrucible.resolveMaterial("gt6addition:arsenic_copper"));
        new Material.Builder(1, new ResourceLocation("anothermod", "arsenic_copper")).ingot().build();
        assertNull(GT6MaterialCompatibility.findExternal("arsenic_copper"));
        assertNull(MetaTileEntityCrucible.resolveMaterial("gt6addition:arsenic_copper"));
        assertSame(external, GT6MaterialCompatibility.findExternal("thirdparty:arsenic_copper"));
    }

    @Test void migrationIsRestrictedToKnownRetiredIdentities() {
        assertEquals("netherite", GT6MaterialCompatibility.retiredPath("gt6addition:netherite"));
        assertNull(GT6MaterialCompatibility.retiredPath("gt6addition:anthracite"));
        assertNull(GT6MaterialCompatibility.retiredPath("gt6addition:unknown_material"));
        assertNull(GT6MaterialCompatibility.retiredPath("anothermod:netherite"));
        assertNull(GT6MaterialCompatibility.retiredPath("netherite"));
        assertNull(GT6MaterialCompatibility.retiredPath(null));
    }
}
