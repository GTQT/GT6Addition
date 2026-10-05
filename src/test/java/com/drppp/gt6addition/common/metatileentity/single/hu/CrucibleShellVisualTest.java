package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrucibleShellVisualTest {
    @Test
    void warningUsesGt6StrictHundredKelvinBoundaryWithoutOverflow() {
        assertFalse(CrucibleShellVisual.isWarning(4033, 4133));
        assertTrue(CrucibleShellVisual.isWarning(4034, 4133));
        assertTrue(CrucibleShellVisual.isWarning(Long.MAX_VALUE, Integer.MAX_VALUE));
        assertFalse(CrucibleShellVisual.isWarning(Long.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    void warningRgbUsesGt6ChannelFormulasAndSaturation() {
        assertEquals(0x445566, CrucibleShellVisual.color(0x445566, false));
        assertEquals(0xBADC65, CrucibleShellVisual.color(0x445566, true));
        assertEquals(0x323232, CrucibleShellVisual.color(0x000000, true));
        assertEquals(0xFFFFB1, CrucibleShellVisual.color(0xFFFFFF, true));
        assertEquals(0xFFFFB1, CrucibleShellVisual.color(0xFFFFFFFF, true));
        assertTrue(CrucibleShellVisual.isEmissive(true, null));
        assertFalse(CrucibleShellVisual.isEmissive(false, null));
    }

    @Test
    void shellDrawsOnlyGt6ExposedFacesForItsFivePasses() {
        int faceCount = 0;
        for (int pass = 0; pass < 5; pass++) {
            for (EnumFacing face : EnumFacing.VALUES) {
                boolean expected = pass == 4 ? face.getAxis() == EnumFacing.Axis.Y :
                        face == EnumFacing.UP || face.getAxis() ==
                                (pass % 2 == 0 ? EnumFacing.Axis.X : EnumFacing.Axis.Z);
                assertEquals(expected, CrucibleShellVisual.shouldRenderFace(pass, face));
                if (expected) faceCount++;
            }
        }
        assertEquals(14, faceCount);
        for (EnumFacing face : EnumFacing.VALUES) {
            assertFalse(CrucibleShellVisual.shouldRenderFace(-1, face));
            assertFalse(CrucibleShellVisual.shouldRenderFace(5, face));
        }
    }
}
