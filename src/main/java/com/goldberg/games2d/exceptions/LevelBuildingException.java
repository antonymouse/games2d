package com.goldberg.games2d.exceptions;

/**
 * Gets thrown if a {@link com.goldberg.games2d.data.Level} can't read itself or validation fails.
 * @author antonymouse
 * @since 0.0
 */
public class LevelBuildingException extends Games2dException{
    public LevelBuildingException(String message) {
        super(message);
    }

    public LevelBuildingException(String message, Throwable cause) {
        super(message, cause);
    }
}
