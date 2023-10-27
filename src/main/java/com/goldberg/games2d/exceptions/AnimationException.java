package com.goldberg.games2d.exceptions;

/**
 * Gets thrown if there is a problem with an Animation
 * @author antonymouse
 * @since 0.0
 */
public class AnimationException extends Games2dException{
    public AnimationException(String message) {
        super(message);
    }

    public AnimationException(String message, Throwable cause) {
        super(message, cause);
    }
}
