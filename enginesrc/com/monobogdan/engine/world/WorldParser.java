package com.monobogdan.engine.world;

import com.monobogdan.engine.math.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WorldParser {
    private static final int PARSER_UNKNOWN = 0;
    private static final int PARSER_TAGS = 1;
    private static final int PARSER_OBJECTS = 2;

    public interface ParserImplementation {
        void processTag(String tag, String value);
        void processGameObject(String obj, Vector position, Vector rotation, String[] values);
    }

    private static Vector tryParseVector(String[] str, int offset) {
        return new Vector(Float.parseFloat(str[offset]), Float.parseFloat(str[offset + 1]), Float.parseFloat(str[offset + 2]));
    }

    public static void parse(InputStream strm, ParserImplementation impl) throws IOException {
        if(strm == null)
            throw new NullPointerException("InputStream was null");

        BufferedReader reader = new BufferedReader(new InputStreamReader(strm));
        String line;
        int state = PARSER_UNKNOWN;
        int lineNum = 0;

        while((line = reader.readLine()) != null) {
            if(line.length() < 1 || line.charAt(0) == '#')
                continue;

            line = line.trim();
            String lowerCase = line.toLowerCase();
            lineNum++;

            if(lowerCase.equals("tags:")) {
                state = PARSER_TAGS;
                continue;
            }

            if(lowerCase.equals("objects:")) {
                state = PARSER_OBJECTS;
                continue;
            }

            String[] split = line.split(" ");

            if(split.length < 2)
                throw new RuntimeException("Argument expected at line " + lineNum);

            switch(state) {
                case PARSER_TAGS:
                    impl.processTag(split[0], split[1]);
                    break;
                case PARSER_OBJECTS:
                    if(split.length < 7)
                        throw new RuntimeException("GameObject declaration expects at least 6 arguments. Only " + (split.length - 1) + " is present");

                    String[] array = null;

                    if(split.length - 7 > 0) {
                        array = new String[split.length - 7];
                        System.arraycopy(split, 7, array, 0, array.length);
                    }

                    impl.processGameObject(split[0], tryParseVector(split, 1), tryParseVector(split, 4), array);
                    break;
                default:
                    throw new ParsingException("Unexpected token " + line + " at line " + lineNum, null);
            }
        }
    }
}
