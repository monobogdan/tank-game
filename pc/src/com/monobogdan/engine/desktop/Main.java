package com.monobogdan.engine.desktop;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

public class Main {
    private static Context context;

    private static boolean parseArgs(String[] args) {
        return true;
    }

    public static void main(String[] args) {
        String dataFolder = "data/";

        if(args.length >= 1)
            dataFolder = args[0];

        if(parseArgs(args)) {
            context = new Context(dataFolder);
            context.run();

            Runtime.getRuntime().halt(0); // TODO: Dangerous approach. System.exit for some reason is hanging.
        }
    }
}
