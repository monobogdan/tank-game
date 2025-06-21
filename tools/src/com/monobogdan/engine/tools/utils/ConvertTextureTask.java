package com.monobogdan.engine.tools.utils;

import com.monobogdan.engine.tools.Main;
import com.monobogdan.engine.tools.PaletteBitmap;
import sun.awt.image.BytePackedRaster;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class ConvertTextureTask {
    public static final int HEADER = 0x12345;

    public static final int TEXTURE_UNCOMPRESSED = 0;
    public static final int TEXTURE_ZLIB = 1; // Default

    public static final int FORMAT_RGB = 0;
    public static final int FORMAT_RGBA = 1;
    public static final int FORMAT_RGB565 = 2;
    public static final int FORMAT_PALETTE = 3; // Uncompressed by loader on fly into RGB

    private static byte[] deflateStaticBuffer = new byte[8192000]; // Should be enough

    private static Deflater deflater = new Deflater(Deflater.DEFLATED, false);
    private static int[] formatTable = { BufferedImage.TYPE_4BYTE_ABGR, FORMAT_RGBA,
            BufferedImage.TYPE_3BYTE_BGR, FORMAT_RGB,
            BufferedImage.TYPE_USHORT_565_RGB, FORMAT_RGB565,
            BufferedImage.TYPE_BYTE_INDEXED, FORMAT_PALETTE,
            BufferedImage.TYPE_BYTE_BINARY, FORMAT_PALETTE
    };
    private static String[] formatStringTable = { "RGB", "RGBA", "RGB565", "Palette" };

    private static byte[] pixelDataToBytes(int[] buf) {
        byte[] data = new byte[buf.length * 4];
        ByteBuffer byteBuf = ByteBuffer.wrap(data);

        for(int i = 0; i < buf.length; i++) {
            byteBuf.putInt(buf[i]);
        }

        return data;
    }

    private static void writePixelData(DataOutputStream stream, byte[] buf, int compression, int bufLen) throws IOException {
        final int COMPRESSION_CHUNK_SIZE = 4096;

        if(compression == TEXTURE_UNCOMPRESSED) {
            stream.writeInt(bufLen); // Pixel data length

            System.out.println("MIP size " + bufLen);

            stream.write(buf, 0, bufLen);
        } else {

            deflater.reset();
            deflater.setInput(buf, 0, bufLen);
            deflater.finish();
            int compressedLength = deflater.deflate(deflateStaticBuffer);

            stream.writeInt(bufLen);
            stream.writeInt(compressedLength);

            System.out.println("MIP size " + compressedLength);

            stream.write(deflateStaticBuffer, 0, compressedLength);
        }
    }

    private static void swap(byte[] buf, int pos1, int pos2) {
        byte tmp = buf[pos1];
        buf[pos1] = buf[pos2];
        buf[pos2] = tmp;
    }

    public static void convert(InputStream inputStream, OutputStream outputStream) throws IOException {
        BufferedImage img = ImageIO.read(inputStream);
        int type = img.getType();
        int compression = TEXTURE_ZLIB;
        int format = -1;

        if(img.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
            throw new RuntimeException("Unsupported color space");

        for(int i = 0; i < formatTable.length / 2; i++) {
            if(type == formatTable[i * 2])
                format = formatTable[i * 2 + 1];
        }

        if(format == -1)
            throw new RuntimeException("Color format is not supported " + img.getType());

        System.out.println("Texture format " + formatStringTable[format]);

        DataOutputStream stream = new DataOutputStream(outputStream);

        stream.writeInt(HEADER);

        stream.writeByte((byte)compression); // Compression type
        stream.writeByte((byte)format);
        stream.writeInt(1); // Mipmap count

        int paletteSize = 0;
        byte[] r = null, g = null, b = null;

        if(format == FORMAT_PALETTE) {
            System.out.println("Building palette");

            if(img.getColorModel().getClass() != IndexColorModel.class)
                throw new IllegalStateException("ColorModel is not indexed");

            IndexColorModel model = (IndexColorModel)img.getColorModel();

            paletteSize = model.getMapSize();

            if(paletteSize != 256 && paletteSize != 16)
                throw new IllegalStateException("Not a 8-bit paletted bitmap (palette size " + model.getMapSize() + ")");

            stream.writeInt(paletteSize);

            r = new byte[paletteSize];
            g = new byte[paletteSize];
            b = new byte[paletteSize];

            model.getReds(r);
            model.getGreens(g);
            model.getBlues(b);

            for(int i = 0; i < r.length; i++) {
                stream.writeByte(b[i]);
                stream.writeByte(g[i]);
                stream.writeByte(r[i]);
            }
        }

        stream.writeShort(img.getWidth());
        stream.writeShort(img.getHeight());

        byte[] pixelData = null;

        pixelData = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();

        if(type == BufferedImage.TYPE_4BYTE_ABGR) {
            // Swap endianness to RGBA
            for(int i = 0; i < pixelData.length / 4; i++) {
                swap(pixelData, i * 4, i * 4 + 3);
                swap(pixelData, i * 4 + 1, i * 4 + 2);
            }
        }

        if(type == BufferedImage.TYPE_3BYTE_BGR) {
            // Swap endianness to RGB
            for(int i = 0; i < pixelData.length / 4; i++) {
                swap(pixelData, i * 4, i * 4 + 2);
            }
        }

        /*if(type == BufferedImage.TYPE_BYTE_BINARY) {
            byte[] realData = new byte[pixelData.length * 2];

            for(int i = 0; i < realData.length / 2; i++) {
                realData[i * 2] = (byte)(pixelData[i] & 0xF);
                realData[i * 2 + 1] = (byte)((pixelData[i] >> 4) & 0xF);
            }

            //pixelData = realData;
        }*/

        writePixelData(stream, pixelData, compression, pixelData.length);
    }

    // With mipmap chain support. Archived due to bugged palette bitmap implementation in awt
   // public static void convert(InputStream inputStream, OutputStream outputStream) throws IOException {
        /*
        BufferedImage img = ImageIO.read(inputStream);
        int type = img.getType();
        int compression = TEXTURE_UNCOMPRESSED;
        int format = -1;

        if(img.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
            throw new RuntimeException("Unsupported color space");

        for(int i = 0; i < formatTable.length / 2; i++) {
            if(type == formatTable[i * 2])
                format = formatTable[i * 2 + 1];
        }

        if(format == -1)
            throw new RuntimeException("Color format is not supported " + img.getType());

        System.out.println("Texture format " + formatStringTable[format]);

        System.out.println("Generating mipmap chain");

        ArrayList<BufferedImage> mipMapChain = new ArrayList<BufferedImage>();

        int mipWidth = img.getWidth();
        int mipHeight = img.getHeight();

        while(mipWidth != 1 && mipHeight != 1) {
            BufferedImage mip = new BufferedImage(mipWidth, mipHeight, img.getType());
            Graphics2D graphics = mip.createGraphics();

            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics.drawImage(img, 0, 0, mipWidth, mipHeight, null);

            mipMapChain.add(mip);

            mipWidth /= 2;
            mipHeight /= 2;
        }

        System.out.println("Mipmap chain contains " + mipMapChain.size() + " mip levels");

        DataOutputStream stream = new DataOutputStream(outputStream);

        stream.writeInt(HEADER);

        stream.writeByte((byte)compression); // Compression type
        stream.writeByte((byte)format);
        stream.writeInt(mipMapChain.size()); // Mipmap count

        int paletteSize = 0;
        byte[] r = null, g = null, b = null;

        if(format == FORMAT_PALETTE) {
            System.out.println("Building palette");

            if(img.getColorModel().getClass() != IndexColorModel.class)
                throw new IllegalStateException("ColorModel is not indexed");

            IndexColorModel model = (IndexColorModel)img.getColorModel();

            paletteSize = model.getMapSize();

            if(paletteSize != 256 && paletteSize != 16)
                throw new IllegalStateException("Not a 8-bit paletted bitmap (palette size " + model.getMapSize() + ")");

            stream.writeInt(paletteSize);

            r = new byte[paletteSize];
            g = new byte[paletteSize];
            b = new byte[paletteSize];

            model.getReds(r);
            model.getGreens(g);
            model.getBlues(b);

            for(int i = 0; i < r.length; i++) {
                stream.writeByte(r[i]);
                stream.writeByte(g[i]);
                stream.writeByte(b[i]);
            }
        }

        // Each mip has small header (width, height) and corresponding texture data
        for(BufferedImage mip : mipMapChain) {

            stream.writeShort(mip.getWidth());
            stream.writeShort(mip.getHeight());

            int[] buf = mip.getRaster().getPixels(0, 0, mip.getWidth(), mip.getHeight(), (int[])null);

            byte[] pixelData = null;

            pixelData = ((DataBufferByte)mip.getRaster().getDataBuffer()).getData();

            if(type == BufferedImage.TYPE_4BYTE_ABGR) {
                // Swap endianness to RGBA
                for(int i = 0; i < pixelData.length / 4; i++) {
                    swap(pixelData, i * 4, i * 4 + 3);
                    swap(pixelData, i * 4 + 1, i * 4 + 2);
                }
            }

            if(type == BufferedImage.TYPE_3BYTE_BGR) {
                // Swap endianness to RGB
                for(int i = 0; i < pixelData.length / 4; i++) {
                    swap(pixelData, i * 4, i * 4 + 2);
                }
            }

            int dataLen = pixelData.length;

            writePixelData(stream, pixelData, compression, dataLen);
        }*/
  //  }
}
