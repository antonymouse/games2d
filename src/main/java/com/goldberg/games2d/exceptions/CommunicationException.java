package com.goldberg.games2d.exceptions;

/**
 * Thrown if there is a problem in communications between application's components
 */
public class CommunicationException extends Games2dException{
    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
