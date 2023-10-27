package com.goldberg.games2d.gamelogic;

/**
 * Holds a game state (such as pause, quit etc) and implements logic handling the state change in response to
 * user's input.
 * The implementations don't have to be thread-safe
 */
public interface UserInputTriggeredState {
    /**
     * Called to process a key (or other user's input)
     * @param message the input
     */
    void processMessage(int[] message);

    /**
     * 
     * @return true is the state is active and false if it's not
     */
    boolean isActive();

    /**
     * @return the (key)code this state is mapped to on the keyboard (or other user input source)
     */
    int codeMappedTo();
}
