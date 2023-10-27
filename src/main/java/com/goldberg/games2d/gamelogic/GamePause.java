package com.goldberg.games2d.gamelogic;

import com.google.inject.name.Named;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;

/**
 * Reacts to P (by default), triggers state "paused" when the key is pressed.
 * @author antonymouse
 * @since 0.0
 */
public class GamePause implements UserInputTriggeredState {
    private boolean paused = false;
    private final int PAUSE_KEY_CODE;

    public GamePause(@Named("PAUSE_KEY") String PAUSE_KEY_CODE) {
        this.PAUSE_KEY_CODE = Integer.parseInt(PAUSE_KEY_CODE);
    }

    /**
     * @param message the input 
     */
    @Override
    public void processMessage(int @NotNull [] message) {
        if(message[0] == codeMappedTo()){
            if(message[1] == KeyEvent.KEY_PRESSED){
                paused = !paused;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return paused;
    }
    
    /**
     * {@inheritDoc}
     * the default is "P"
     */
    @Override
    public int codeMappedTo() {
        return PAUSE_KEY_CODE;
    }
}
