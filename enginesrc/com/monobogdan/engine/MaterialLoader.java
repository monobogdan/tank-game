package com.monobogdan.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class MaterialLoader {
    private static Field[] materialFields;

    static {
        // Prepare fields
        materialFields = Material.class.getFields();

        for(int i = 0; i < materialFields.length; i++)
            materialFields[i].setAccessible(true);
    }

    private static void setField(Object obj, String fieldName, String value) {
        try {
            // Find field
            Field field = null;

            for(int i = 0; i < materialFields.length; i++) {
                if(materialFields[i].getName().toLowerCase().equals(fieldName)) {
                    field = materialFields[i];
                    break;
                }
            }

            if(field == null)
                throw new RuntimeException("Field " + fieldName + " is not found");

            // Determine type

            // Int
            if(field.getType() == float.class) {
                field.set(obj, Float.parseFloat(value));

                return;
            }

            if(field.getType() == int.class) {
                field.set(obj, Integer.parseInt(value));

                return;
            }

            if(field.getType() == boolean.class) {
                field.set(obj, Integer.parseInt(value) > 0);

                return;
            }

            throw new ClassCastException("Field " + field.getName() + " is not of type int, float, bool");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("WTF?", e);
        }
    }

    private static boolean isNumber(String str) {
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if(!Character.isDigit(c) && c != '.')
                return false;
        }

        return true;
    }

    private static Material.ShaderInstance parseCombiner(Material material, ArrayList<String> textures, int lineNum, String[] val) {
        String field = val[0];

        if(!FFPShaders.ShaderHashMap.containsKey(field))
            throw new RuntimeException("Shader " + field + " is not found");

        BaseGraphics.FixedFunctionShader shader = FFPShaders.ShaderHashMap.get(field);

        float[] params = new float[val.length - 1];

        for(int i = 1; i < val.length; i++) {
            int idx = textures.indexOf(val[i]);

            if(idx != -1)
                params[i - 1] = idx;
            else {
                if(!isNumber(val[i]))
                    throw new RuntimeException("Texture " + val[i] + " doesn't exist");

                params[i - 1] = Float.parseFloat(val[i]);
            }
        }

        return new Material.ShaderInstance(shader, params);
    }

    public static Material load(Runtime runtime, String fileName) {
        try {
            return load(runtime, fileName.substring(fileName.lastIndexOf('/') + 1), runtime.Platform.openFile(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Material not found: " + fileName);
        }
    }

    public static Material load(Runtime runtime, String name, InputStream inputStream) throws IOException {
        final int STAGE_ROOT = 0;
        final int STAGE_SECTION_TEXTURE = 1;
        final int STAGE_SECTION_RENDERSTATE = 2;
        final int STAGE_SECTION_COMBINERS = 3;
        final int STAGE_SECTION_SHADERS = 4;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        int lineNum = 0;
        int stage = 0;

        ArrayList<String> texNames = new ArrayList<String>(Material.COMBINER_STAGE_COUNT);
        int texOffset = 0;
        ArrayList<Material.ShaderInstance> shaders = new ArrayList<Material.ShaderInstance>();
        Material ret = new Material(runtime, name);

        while((line = reader.readLine()) != null) {
            lineNum++;
            if(line.length() < 1 || line.charAt(0) == '#')
                continue;

            line = line.trim().toLowerCase();
            String[] split = line.split(" ");

            if(line.startsWith("[") && line.endsWith("]")) {
                String sectionName = line.substring(1, line.length() - 1);

                stage = STAGE_ROOT;

                if(sectionName.equals("texture"))
                    stage = STAGE_SECTION_TEXTURE;

                if(sectionName.equals("renderstates"))
                    stage = STAGE_SECTION_RENDERSTATE;

                if(sectionName.equals("combiners"))
                    stage = STAGE_SECTION_COMBINERS;



                if(stage == STAGE_ROOT)
                    throw new RuntimeException("Expected texture, renderstates or combiners section on line " + lineNum);

                continue;
            }

            if(stage == STAGE_SECTION_TEXTURE) {
                if(split.length != 3)
                    throw new RuntimeException("Expected TextureName = Texture on line " + lineNum);

                String texName = split[0];
                String texPath = split[2];

                if(texNames.contains(texName))
                    throw new RuntimeException("Material " + name + " already contains texture " + texName);

                if(texOffset == Material.COMBINER_STAGE_COUNT)
                    throw new RuntimeException("Maximum " + texOffset + " textures is allowed");

                Texture2D tex = runtime.ResourceManager.getTexture(texPath);
                texNames.add(texName);
                ret.Textures[texOffset] = tex;
                texOffset++;
            }

            if(stage == STAGE_SECTION_RENDERSTATE)
                setField(ret, split[0], split[2]);

            if(stage == STAGE_SECTION_COMBINERS)
                shaders.add(parseCombiner(ret, texNames, lineNum, split));
        }

        ret.Shaders = new Material.ShaderInstance[shaders.size()];
        shaders.toArray(ret.Shaders);

        return ret;
    }
}
