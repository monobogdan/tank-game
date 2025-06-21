package com.monobogdan.engine.scripting;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ScriptClassLoader extends ClassLoader {

    private ZipFile zipFile;

    public ScriptClassLoader(String jarFileName) throws IOException {
        zipFile = new ZipFile(jarFileName);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ZipEntry entry;

        if((entry = zipFile.getEntry(name.replace('.', '/'))) != null) {
            try {
                byte[] classData = new byte[(int)entry.getSize()];
                zipFile.getInputStream(entry).read(classData);

                return defineClass(name, classData, 0, classData.length);
            } catch (IOException e) {
                throw new ClassNotFoundException("Error while loading script file", e);
            }
        }

        throw new ClassNotFoundException("Class " + name + " is not found");
    }
}
