package com.monobogdan.engine;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

class TextureLoader {
    static class MipDesc {
        public int Width;
        public int Height;
        public ByteBuffer Buffer;
    }
    private static final int HEADER = 0x12345;

    public static final int TEXTURE_UNCOMPRESSED = 0;
    public static final int TEXTURE_ZLIB = 1; // Default

    public static final int FORMAT_RGB = 0;
    public static final int FORMAT_RGBA = 1;
    public static final int FORMAT_RGB565 = 2;
    public static final int FORMAT_PALETTE = 3; // Uncompressed by loader on fly into RGB

    private static Inflater inflater = new Inflater();

    public static Texture2D load(Runtime runtime, String fileName) {
        try {
            return load(runtime, runtime.Platform.openFile(fileName), fileName.substring(fileName.lastIndexOf('/') + 1));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open texture: " + fileName);
        }
    }

    public static int getMipMapCount(int width, int height) {
        int mipCount = 0;

        while(width > 1 && height > 1) {
            width /= 2;
            height /= 2;
        }

        return mipCount;
    }

    public static Texture2D load(Runtime runtime, InputStream strm, String name) {
        if(runtime == null)
            runtime.Platform.log("Runtime can't be null");

        if(name == null)
            runtime.Platform.log("Warning: Attempt to load unnamed mesh");

        if(strm == null)
            throw new NullPointerException("Input stream can't be null for TextureLoader");

        runtime.Platform.log("[Resources] Loading texture %s", name);

        try {
            DataInputStream inputStream = new DataInputStream(strm);

            int header = inputStream.readInt();
            if(header != HEADER)
                throw new RuntimeException("Not a texture file");

            byte compression = inputStream.readByte();
            final byte format = inputStream.readByte();

            if(compression > TEXTURE_ZLIB)
                throw new RuntimeException("Compression type is not supported");

            byte[] palette = null;

            final int mipCount = inputStream.readInt();

            if(mipCount == 0)
                throw new AssertionError("mipcount == 0");

            if(format == FORMAT_PALETTE) {
                int paletteSize = inputStream.readInt();

                if(paletteSize == 0)
                    throw new RuntimeException("Palette can't be empty");

                palette = new byte[paletteSize * 3];
                inputStream.read(palette);
            }

            byte[] buf = null;
            byte[] decompressionBuf = null;

            final MipDesc mipLevels[] = new MipDesc[mipCount];
            final Texture2D ret = new Texture2D(name, runtime);

            for(int i = 0; i < mipCount; i++) {
                int width = inputStream.readShort();
                int height = inputStream.readShort();
                int length = inputStream.readInt();
                int compressedLength = compression == TEXTURE_ZLIB ? inputStream.readInt() : 0;

                if(width == 0 || height == 0)
                    throw new RuntimeException("Incorrect MIP level size");

                if(length == 0)
                    throw new RuntimeException("Incorrect MIP level length");

                if(buf == null)
                    buf = new byte[length]; // First mip contains largest buffer

                if(compression == TEXTURE_UNCOMPRESSED) {
                    int len = inputStream.read(buf, 0, length);
                } else {
                    if(decompressionBuf == null)
                        decompressionBuf = new byte[length]; // As with uncompressed-variant, we allocate largest decompression buf for MIP0

                    inputStream.read(decompressionBuf, 0, length);

                    // Decompress
                    inflater.reset();
                    inflater.setInput(decompressionBuf);
                    try {
                        if(inflater.inflate(buf) < compressedLength)
                            throw new RuntimeException("Not sufficient data for MIP level decompression (expected " + compressedLength + " got " + length + ") for MIP-level " + i);
                    } catch (DataFormatException e) {
                        throw new RuntimeException("Corrupted deflate stream");
                    }
                }

                mipLevels[i] = new MipDesc();
                mipLevels[i].Width = width;
                mipLevels[i].Height = height;

                if(format == FORMAT_PALETTE) {
                    // Decompress palette texture back to RGB
                    mipLevels[i].Buffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());

                    if(palette.length / 3 == 16) {
                        // 4-bit palette unpacking
                        for (int j = 0; j < (width * height) / 2; j++) {
                            int pixel1 = (buf[j] & 0xF) * 3;
                            int pixel2 = ((buf[j] >> 4) & 0xF) * 3;

                            mipLevels[i].Buffer.put(palette[pixel1 + 2]);
                            mipLevels[i].Buffer.put(palette[pixel1 + 1]);
                            mipLevels[i].Buffer.put(palette[pixel1]);
                            mipLevels[i].Buffer.put((byte) 255);

                            mipLevels[i].Buffer.put(palette[pixel2 + 2]);
                            mipLevels[i].Buffer.put(palette[pixel2 + 1]);
                            mipLevels[i].Buffer.put(palette[pixel2]);
                            mipLevels[i].Buffer.put((byte) 255);
                        }
                    } else {
                        for (int j = 0; j < width * height; j++) {
                            int paletteSample = (buf[j] & 0xFF) * 3;

                            mipLevels[i].Buffer.put(palette[paletteSample + 2]);
                            mipLevels[i].Buffer.put(palette[paletteSample + 1]);
                            mipLevels[i].Buffer.put(palette[paletteSample]);
                            mipLevels[i].Buffer.put((byte) 255);
                        }
                    }


                } else {
                    mipLevels[i].Buffer = ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder());
                    mipLevels[i].Buffer.put(buf, 0, length);
                }

                mipLevels[i].Buffer.rewind();
            }

            runtime.Scheduler.runOnMainThreadIfNeeded(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < mipCount; i++)
                        ret.upload(mipLevels[i].Buffer, mipLevels[i].Width, mipLevels[i].Height, format == FORMAT_PALETTE ? FORMAT_RGB : format);
                }
            });

            return ret;
        } catch (IOException e) {
            runtime.Platform.log("[Resources] Failed to load mesh %s", name);
            runtime.Platform.log(e.getMessage());

            return null;
        }
    }
}
