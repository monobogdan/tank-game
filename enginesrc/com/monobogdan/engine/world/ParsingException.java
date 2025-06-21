package com.monobogdan.engine.world;

public class ParsingException extends RuntimeException {

    public ParsingException(String message, Throwable containedException) {
        super(message, containedException);
    }
}
