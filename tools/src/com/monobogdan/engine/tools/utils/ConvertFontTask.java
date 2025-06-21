package com.monobogdan.engine.tools.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.util.*;

public class ConvertFontTask {
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Info {
        @XmlAttribute(name = "face")
        public String face;
        @XmlAttribute(name = "size")
        public int size;
        @XmlAttribute(name = "bold")
        public int bold;
        @XmlAttribute(name = "italic")
        public int italic;
        @XmlAttribute(name = "unicode")
        public int unicode;
        @XmlAttribute(name = "stretchH")
        public int stretchH;
        @XmlAttribute(name = "smooth")
        public int smooth;
        @XmlAttribute(name = "aa")
        public int aa;
        @XmlAttribute(name = "padding")
        public String padding;
        @XmlAttribute(name = "spacing")
        public String spacing;
        @XmlAttribute(name = "outline")
        public int outline;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class common {
        public int lineHeight;
        public int base;
        public int scaleW;
        public int scaleH;
        public int pages;
        public int packed;
        public int alphaChnl;
        public int redChnl;
        public int greenChnl;
        public int blueChnl;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class page {
        @XmlAttribute(name = "id")
        public int id;
        @XmlAttribute(name = "file")
        public String file;
    }

    public static class pages {
        @XmlElement(name = "page")
        public page[] pageDescriptors;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class _char {
        @XmlAttribute(name = "id")
        public int id;
        @XmlAttribute(name = "x")
        public int x;
        @XmlAttribute(name = "y")
        public int y;
        @XmlAttribute(name = "width")
        public int width;
        @XmlAttribute(name = "height")
        public int height;
        @XmlAttribute(name = "xoffset")
        public int xoffset;
        @XmlAttribute(name = "yoffset")
        public int yoffset;
        public int xadvance;
        @XmlAttribute(name = "page")
        public int page;
        public int chnl;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class chars {
        @XmlElement(name = "char")
        public _char[] chars;
        @XmlAttribute(name = "count")
        public int count;
    }

    @XmlRootElement(name = "font")
    public static class font {
        public Info info;
        public common common;
        public pages pages;
        public chars chars;
    }

    public static void convert(InputStream strm, OutputStream outputStream) throws IOException {
        try {
            DataOutputStream output = new DataOutputStream(outputStream);

            JAXBContext jaxb = JAXBContext.newInstance(font.class);
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();

            font f = (font) unmarshaller.unmarshal(new InputStreamReader(strm));
            System.out.println("Converting font " + f.info.face);

            output.writeInt(0x1337); // Header
            output.writeShort(f.chars.count); // Number of exported characters
            output.writeByte(f.pages.pageDescriptors.length); // Number of pages
            output.writeUTF(f.info.face);
            output.writeByte(f.info.size);

            // Descriptors
            for(page p : f.pages.pageDescriptors)
                output.writeUTF(p.file.substring(0, p.file.lastIndexOf('.')));

            for(_char chr : f.chars.chars) {
                /* Format:
                  uint16 utfCodePoint;
                  byte x;
                  byte y;
                  byte width;
                  byte height;
                  byte page;
                */
                output.writeShort(chr.id);
                output.writeByte(chr.x);
                output.writeByte(chr.y);
                output.writeByte(chr.width);
                output.writeByte(chr.height);
                output.writeByte(chr.yoffset);
                output.writeByte(chr.page);
            }
        } catch (JAXBException e) {
            throw new IOException("Failed to process font file", e);
        }
    }
}
