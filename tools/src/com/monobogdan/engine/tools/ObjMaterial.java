/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.monobogdan.engine.internals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 *
 * @author mono
 */
public class ObjMaterial {
    public static class Material {
        public String Texture;
    }

    public HashMap<String, Material> Materials;

    public ObjMaterial(InputStream strm) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(strm));
        String line = null;

        Materials = new HashMap<String, Material>();
        String currMat = null;

        while((line = reader.readLine()) != null) {
            line = line.trim();

            if(line.length() == 0 || line.charAt(0) == '#')
                continue;

            String[] split = line.split(" ");
            if(split[0].equals("newmtl")) {
                currMat = split[1];
                Materials.put(currMat, new Material());
            }

            if(split[0].equals("map_Kd")) {
                if(currMat == null)
                    throw new IllegalStateException("newmtl missing");

                Materials.get(currMat).Texture = new File(split[1]).getName();
            }
        }
    }
}
