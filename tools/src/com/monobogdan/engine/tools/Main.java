package com.monobogdan.engine.tools;

import com.monobogdan.engine.tools.formats.BmpFile;
import com.monobogdan.engine.tools.utils.ConvertFontTask;
import com.monobogdan.engine.tools.utils.CopyDataTask;
import com.monobogdan.engine.tools.utils.ConvertTextureTask;
import com.monobogdan.engine.tools.utils.ConvertModelTask;

import java.io.*;

public class Main {
    public static boolean forcePalette;

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if(args.length < 1)
            return;

        if(args[0].equals("-copydata")) {
            CopyDataTask.run();

            return;
        }

        if(args.length > 1) {
            if(args[1].equals("-4bit"))
                forcePalette = true;
        }

        String fileName = args[0];
        File file = new File(fileName);

        if(!file.exists() || file.isDirectory())
            System.out.println(fileName + " is not file or doesn't exist");

        if(fileName.endsWith(".obj")) {
            ConvertModelTask.convert(new FileInputStream(fileName), new FileOutputStream(fileName.substring(0, fileName.lastIndexOf('.')) + ".mdl"));

            return;
        }

        if(fileName.endsWith(".fnt")) {
            ConvertFontTask.convert(new FileInputStream(fileName), new FileOutputStream(fileName.substring(0, fileName.lastIndexOf('.')) + ".font"));
        }

        if(fileName.endsWith(".png") || fileName.endsWith(".bmp") || fileName.endsWith(".jpg")) {
            ConvertTextureTask.convert(new FileInputStream(fileName), new FileOutputStream(fileName.substring(0, fileName.lastIndexOf('.')) + ".tex"));

            return;
        }

        System.out.println("Unsupported file");
    }
}
