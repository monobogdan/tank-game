package com.monobogdan.engine.ui;

import com.monobogdan.engine.Runtime;
import com.monobogdan.engine.Texture2D;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class BitmapFont {
    public static class CharacterInfo {
        public int X;
        public int Y;
        public int YOffset;
        public int Width;
        public int Height;
        public int Page;
    }

    public int Size;
    public Texture2D[] Pages;
    private HashMap<Character, CharacterInfo> chrHashMap;

    private CharacterInfo questionMark;

    public BitmapFont(int size, Texture2D[] pages, HashMap<Character, CharacterInfo> characters) {
        if(pages == null)
            throw new NullPointerException("pages can't be null");

        if(characters == null)
            throw new NullPointerException("characters can't be null");

        if(size <= 1)
            throw new IllegalArgumentException("size can't be less than 1");

        Pages = pages;
        chrHashMap = characters;
        questionMark = chrHashMap.get('?');

        if(questionMark == null)
            throw new RuntimeException("Incorrect character map supplied");
    }

    public CharacterInfo getCharacter(char character) {
        if(chrHashMap.containsKey(character))
            return chrHashMap.get(character);
        else
            return questionMark; // Unknown character
    }

    public static BitmapFont load(Runtime runtime, String fileName) {
        if(runtime == null)
            throw new NullPointerException("runtime was null");

        try {
            return load(runtime, runtime.Platform.openFile(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read font file " + fileName, e);
        }
    }

    public static BitmapFont load(Runtime runtime, InputStream strm) throws IOException {
        final int HEADER = 0x1337;
        DataInputStream ds = new DataInputStream(strm);

        if(ds.readInt() != HEADER)
            throw new RuntimeException("Incorrect font header");

        short chrCount = ds.readShort();
        int pageNum = ds.readByte();
        String face = ds.readUTF();
        int size = ds.readByte();

        runtime.Platform.log("Reading font '%s' of size %d", face, size);

        HashMap<Character, CharacterInfo> chars = new HashMap<Character, CharacterInfo>(chrCount);
        Texture2D[] pages = new Texture2D[pageNum];

        for(int i = 0; i < pages.length; i++)
            pages[i] = runtime.ResourceManager.getTexture("font/" + ds.readUTF() + ".tex");

        for(int i = 0; i < chrCount; i++) {
            short codePoint = ds.readShort();
            CharacterInfo info = new CharacterInfo();
            info.X = ds.readByte();
            info.Y = ds.readByte();
            info.Width = ds.readByte();
            info.Height = ds.readByte();
            info.YOffset = ds.readByte();
            info.Page = ds.readByte();

            if(chars.containsKey(codePoint))
                throw new RuntimeException("Duplicate character of code point " + Short.toString(codePoint));

            chars.put((char)codePoint, info);
        }

        return new BitmapFont(size, pages, chars);
    }
}
