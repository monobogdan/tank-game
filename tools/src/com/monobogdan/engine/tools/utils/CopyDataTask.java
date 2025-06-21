package com.monobogdan.engine.tools.utils;

import java.io.*;
import java.util.ArrayList;

public class CopyDataTask {
    public static final String DATA_SOURCE = "data/";
    public static final String DATA_PREPARED = "data/gamedata/";

    private static String[] sourceFiles = { "bmp", "png", "jpg", "jpeg", "tga", "obj", "fbx", "mtl", "fnt" };
    private static byte[] buffer = new byte[4096000];

    private static void recursiveDelete(String dir) {
        File file = new File(dir);

        if(file.isDirectory()) {
            for(File subFile : file.listFiles()) {
                if(subFile.isDirectory()) {
                    recursiveDelete(dir + "/" + subFile.getName());
                    subFile.delete();
                }
                else {
                    if(!subFile.delete())
                        throw new RuntimeException("Unable to delete file " + subFile.getName());
                }
            }
        } else {
            file.mkdirs();
        }
    }

    private static void collectFilesForCopy(String dir, ArrayList<String> files) {
        // Java 1.6 lacks Files class

        File file = new File(dir);

        if(file.isDirectory()) {
            for(File subFile : file.listFiles()) {
                String fileName = dir + "/" + subFile.getName();

                if(subFile.isDirectory())
                    collectFilesForCopy(fileName, files);
                else
                    files.add(fileName);
            }
        } else {
            throw new RuntimeException(dir + " is not a directory");
        }
    }

    private static boolean isSourceFile(String file) {
        for(String extension : sourceFiles) {
            if(file.endsWith(extension))
                return true;
        }

        return false;
    }

    private static void copyFile(String source, String dest) {
        try {
            System.out.println(Runtime.getRuntime().exec(String.format("cmd.exe /C copy %s %s", source.replace('/', '\\'), dest.replace('/', '\\'))).waitFor());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void run() {
        System.out.println("Copying game data");

        System.out.println("Wildcards: ");
        for(String str : sourceFiles) {
            System.out.printf("%s ", str);
        }
        System.out.println();

        System.out.println("Deleting old data " + DATA_PREPARED);
        recursiveDelete(DATA_PREPARED);

        ArrayList<String> files = new ArrayList<String>();
        collectFilesForCopy("data", files);

        for(String str : files) {
            if(!isSourceFile(str)) {
                String filePath = str.substring(str.indexOf('/'));
                String fileDirs = filePath.substring(1, filePath.lastIndexOf('/') + 1);

                new File(DATA_PREPARED + fileDirs).mkdirs();

                copyFile(str, DATA_PREPARED + filePath);
            }
        }
    }
}
