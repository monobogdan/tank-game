package com.monobogdan.engine;

public abstract class NamedResource {
    private StringBuilder nameBuilder = new StringBuilder();

    public Runtime Runtime;
    public String Name;

    public NamedResource(Runtime runtime, String name) {
        Name = name;

        if(Name == null)
            Name = "Unnamed " + getClass().getSimpleName();

        Runtime = runtime;
    }

    @Override
    public String toString() {
        nameBuilder.delete(0, nameBuilder.length());
        return nameBuilder.append(getClass().getSimpleName()).append(": ").append(Name).toString();
    }
}
