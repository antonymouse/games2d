package com.goldberg.games2d.gamelogic;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a hardcoded set of commands with mappings to keys
 * (movements and the primary action). If we want to have configurable keys just make this a class.
 */
public enum PredefinedCommand {
    DOWN(KeyEvent.VK_DOWN,0,1),
    DOWN_RIGHT(KeyEvent.VK_PAGE_DOWN,1,1),
    RIGHT(KeyEvent.VK_RIGHT,1,0),
    UP_RIGHT(KeyEvent.VK_PAGE_UP,1,-1),
    UP(KeyEvent.VK_UP,0,-1),
    UP_LEFT(KeyEvent.VK_HOME,-1,-1),
    LEFT(KeyEvent.VK_LEFT,-1,0),
    DOWN_LEFT(KeyEvent.VK_END,-1,1);

    public int getxCellIncrement() {
        return xCellIncrement;
    }

    public int getyCellIncrement() {
        return yCellIncrement;
    }

    private static final Map<Integer, PredefinedCommand> BY_KEY = new HashMap<>();
    static {
        for (PredefinedCommand e: values()) {
            BY_KEY.put(e.key, e);
        }
    }

    /**
     * 
     * @param iKey the key on the keyboard (VK_...)
     * @return value of the PredefinedCommand for this key on the keyboard, null if none
     */
    public static PredefinedCommand valueOfKey(Integer iKey) {
        return BY_KEY.get(iKey);
    }
    public int getKey() {
        return key;
    }

    private final int key, xCellIncrement, yCellIncrement;


    PredefinedCommand(int keyToMap, int xCellIncrement, int yCellIncrement) {
        this.key = keyToMap;
        this.xCellIncrement = xCellIncrement;
        this.yCellIncrement = yCellIncrement;
    }
}
