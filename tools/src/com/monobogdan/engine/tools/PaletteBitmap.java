package com.monobogdan.engine.tools;

import java.awt.image.BufferedImage;
import java.util.*;

public class PaletteBitmap {
    public static class Color {
        public int Usage;
        public int R, G, B;

        public Color(int r, int g, int b) {
            R = r;
            G = g;
            B = b;

            Usage = 1;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj.getClass() != Color.class)
                return false;

            Color color = (Color)obj;
            return color.R == R && color.G == G && color.B == B;
        }

        @Override
        public int hashCode() {
            return (R * 10) + (G * 15) + (B * 20);
        }
    }

    public static class Bitmap {
        public int Width;
        public int Height;
        public byte[] Pixels;
        public Color[] Palette;
    }

    private static void incrementColorUsage(ArrayList<Color> colors, int r, int g, int b) {
        int idx = 0;
        Color col = new Color(r, g, b);

        if((idx = colors.indexOf(col)) >= 0)
            colors.get(idx).Usage++;
        else
            colors.add(new Color(r, g, b));
    }

    private static ArrayList<Color> buildColorUsageList(byte[] bitmap, int width, int height) {
        // Find most-used colors in bitmap
        ArrayList<Color> colors = new ArrayList<Color>();

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                int offset = (i * width + j) * 3;

                incrementColorUsage(colors, bitmap[offset], bitmap[offset + 1], bitmap[offset + 2]);
            }
        }

        Collections.sort(colors, new Comparator<Color>() {
            @Override
            public int compare(Color o1, Color o2) {
                return o1.Usage < o2.Usage ? 1 : (o1.Usage > o2.Usage ? -1 : 0);
            }
        });

        return colors;
    }

    public static int getNearestColorFromPalette(int r, int g, int b, Color[] palette) {
        int rDiff = 255, gDiff = 255, bDiff = 255;
        int idx = 0;

        for(int i = 0; i < palette.length; i++) {
            if(Math.abs(palette[i].R - r) < rDiff && Math.abs(palette[i].G - g) < gDiff && Math.abs(palette[i].B - b) < bDiff) {
                rDiff = Math.abs(palette[i].R - r);
                gDiff = Math.abs(palette[i].G - g);
                bDiff = Math.abs(palette[i].B - b);

                idx = i;
            }
        }

        return idx;
    }

    public static Bitmap convertRGBToPalette(byte[] bitmap, int width, int height) {
        ArrayList<Color> colors = buildColorUsageList(bitmap, width, height);
        System.out.println("Memory usage: " + Runtime.getRuntime().freeMemory());

        // Pick 16 most used colors
        Color[] palette = new Color[256];
        for(int i = 0; i < palette.length; i++)
            palette[i] = colors.get(i);

        Bitmap ret = new Bitmap();
        ret.Pixels = new byte[width * height];
        ret.Palette = palette;
        ret.Width = width;
        ret.Height = height;

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                int offset = (i * width + j) * 3;

                ret.Pixels[i * width + j] = (byte)getNearestColorFromPalette(bitmap[offset], bitmap[offset + 1], bitmap[offset + 2], palette);
            }
        }

        return ret;
    }
}
