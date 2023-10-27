package com.goldberg.games2d.exceptions;

/**
 * Gets thrown id there is something wrong with drawing the stuff on screen
 */
public class GraphicsException extends Games2dException{
    public GraphicsException(String message) {
        super(message);
    }

    public GraphicsException(String message, Throwable cause) {
        super(message, cause);
    }
}
