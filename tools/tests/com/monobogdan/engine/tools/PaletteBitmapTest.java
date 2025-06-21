package com.monobogdan.engine.tools;

import junit.framework.TestCase;

public class PaletteBitmapTest extends TestCase {

    public void testNearestColorFromPalette() {
        PaletteBitmap.Color[] palette = {
                new PaletteBitmap.Color(255, 0, 255),
                new PaletteBitmap.Color(128, 128, 128),
                new PaletteBitmap.Color(24, 32, 24)
        };

        assertEquals(1, PaletteBitmap.getNearestColorFromPalette(235, 228, 235, palette));
    }
}