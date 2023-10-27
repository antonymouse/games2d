package com.goldberg.games2d.gamelogic;

import com.google.inject.name.Named;
import org.jetbrains.annotations.NotNull;

import javax.naming.Name;
import java.awt.event.KeyEvent;

/**
 * The default for "quit" is escape. Triggers on pressed.
 * @author antonymouse
 * @since 0.0
 */
public class GameQuit implements UserInputTriggeredState{
    private boolean quit = false;

    public GameQuit(@Named("EXIT_KEY") String EXIT_CODE) {
        this.EXIT_KEY_CODE = Integer.parseInt(EXIT_CODE);
    }

    private int EXIT_KEY_CODE;
    /**
     * {@inheritDoc}
     */
    @Override
    public void processMessage(int @NotNull [] message) {
        if(message[0] == codeMappedTo()){
            if(message[1] == KeyEvent.KEY_PRESSED && !quit){
                quit = true;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return quit;
    }

    /**
     * {@inheritDoc}
     * the default is "esc"
     */
    @Override
    public int codeMappedTo() {
        return KeyEvent.VK_ESCAPE;
    }

}
