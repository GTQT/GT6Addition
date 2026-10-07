package com.drppp.gt6addition.common.material;

import gregtech.api.unification.material.Material;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GT6MachineMaterialsTest {
    @Test void onlyAnthraciteRemainsInTheRegistrationApi() {
        assertArrayEquals(new String[]{"ANTHRACITE"}, Arrays.stream(GT6MachineMaterials.class.getDeclaredFields())
                .filter(field -> field.getType() == Material.class).map(java.lang.reflect.Field::getName)
                .toArray(String[]::new));
        assertEquals(3200, GT6MachineMaterials.ANTHRACITE_BURN_TIME);
    }
}
