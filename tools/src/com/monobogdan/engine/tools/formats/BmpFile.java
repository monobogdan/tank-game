package com.monobogdan.engine.tools.formats;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BmpFile {

    public BmpFile(InputStream strm) throws IOException {
        final int IDENT = 19778;

        byte[] data = new byte[strm.available()];
        strm.read(data);

        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        // Read header
        if(buf.getShort() != IDENT)
            throw new RuntimeException("Not a BMP file");

        int size = buf.getInt();
        int reserved = buf.getInt();
        int pixelDataOffset = buf.getInt();

        // Read DIB structure
        int dibSz = buf.getInt();

        if(dibSz != 40)
            throw new RuntimeException("DIB format is not supported");

        int width = buf.getInt();
        int height = buf.getInt();
        int colorPlanes = buf.getShort();
        int bpp = buf.getShort();

        if(bpp != 4)
            throw new RuntimeException("Only palette 4-bit BMP are supported");

        int compression = buf.getInt();
        int imageSize = buf.getInt();
        int xPerPel = buf.getInt();
        int yPerPel = buf.getInt();
        int paletteNum = buf.getInt();
        int importantColors = buf.getInt();

        if(paletteNum == 0)
            throw new RuntimeException("Only palette 4-bit BMP are supported");


    }
}
