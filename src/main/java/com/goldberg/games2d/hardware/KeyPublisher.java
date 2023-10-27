package com.goldberg.games2d.hardware;

import com.goldberg.games2d.exceptions.CommunicationException;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.BlockingQueue;

/**
 * The class listens to keyboard events and queues them for later processing by the game loop and interested
 * components
 */
public class KeyPublisher implements KeyListener {
    private static final Logger logger = LogManager.getLogger();
    /**
     * The queue for the keys pressed while painting was going on. Singleton shared between
     * the key listener and the game loop.
     */
    private final BlockingQueue<int[]> comingKeys;
    @Inject
    public KeyPublisher(BlockingQueue<int[]> comingKeys) {
        logger.debug("KeyPublisher instantiated");
        this.comingKeys = comingKeys;
    }

    /**
     * We ignore these for now.
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    /**
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        try {
            this.comingKeys.add(this.event2Message(e,KeyEvent.KEY_PRESSED));
        }catch(IllegalStateException ise){
            throw new CommunicationException("The keys queue is full, unable to push any more. " +
                    "Processing is likely too slow",ise);
        }
    }

    /**
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent e) {
        try {
            this.comingKeys.add(this.event2Message(e,KeyEvent.KEY_RELEASED));
        }catch(IllegalStateException ise){
            throw new CommunicationException("The keys queue is full, unable to push any more. " +
                    "Processing is likely too slow",ise);
        }
    }
    private int[] event2Message(KeyEvent e, int action){
        int[] message = new int[2];
        message[0] = e.getKeyCode();
        message[1] = action;
        e.consume();
        return message;
    }

}
