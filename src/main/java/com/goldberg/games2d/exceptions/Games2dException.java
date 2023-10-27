package com.goldberg.games2d.exceptions;

/**
 * Base level exception. All games2d exceptions are children of this one. Makes them easy to catch.
 * @author antonymouse
 * @since 0.0
 */
public class Games2dException extends RuntimeException{
    /**
     * @param message why this happened, what to do.
     */
    public Games2dException(String message) {
        super(message);
    }

    /**
     * @param message why this happened, what to do.
     * @param cause the original error for debugging.
     */
    public Games2dException(String message, Throwable cause) {
        super(message, cause);
    }
}
