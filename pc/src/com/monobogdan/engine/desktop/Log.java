package com.monobogdan.engine.desktop;

import java.io.*;

public class Log {
    private FileOutputStream foStream;
    private BufferedWriter bufWriter;
    private PrintWriter printWriter;

    public Log(String fileName) {
        try {
            foStream = new FileOutputStream(fileName);
            bufWriter = new BufferedWriter(new OutputStreamWriter(foStream));
            printWriter = new PrintWriter(bufWriter);
        } catch (FileNotFoundException e) {
            printStandardOutput("Unable to open log file " + fileName);
        }
    }

    private void printStandardOutput(String str) {
        System.out.println(str);
    }

    public void print(String fmt, Object... args) {
        String formatted = String.format(fmt, args);

        printStandardOutput(formatted);

        if(foStream != null) {
            printWriter.println(formatted);
            printWriter.flush();
        }
    }

    public void printException(Throwable throwable) {
        throwable.printStackTrace(printWriter);
        throwable.printStackTrace(System.out);

        printWriter.flush();
    }

    public void flush() {
        if(foStream != null) {
            try {
                bufWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
