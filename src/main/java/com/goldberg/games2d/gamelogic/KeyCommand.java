package com.goldberg.games2d.gamelogic;

/**
 * A command that can be associated to a certain keyboard key
 * @author antonymouse
 * @since 0.0
 */
public class KeyCommand extends Command{
    private final int key;
    /**
     * Sets up this command
     *
     * @param xCellIncrement @see {@link Command}
     * @param yCellIncrement @see {@link Command}
     * @param name the name of the command
     * @param key the keyboard key the command is associated with
     * @param interruptable does the command need to run for its full duration, or it can be preempted
     */
    public KeyCommand(int xCellIncrement, int yCellIncrement, String name, int key, boolean interruptable) {
        super(xCellIncrement, yCellIncrement, name, interruptable);
        this.key = key;
    }
    public KeyCommand(int xCellIncrement, int yCellIncrement, String name, int key) {
        super(xCellIncrement, yCellIncrement, name, false);
        this.key = key;
    }

    /**
     * @return the key code (keyboard) the command is mapped to
     */
    public int getKey() {
        return key;
    }
}
