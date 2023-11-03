package com.goldberg.games2d.gamelogic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * What a Sprite needs to do
 * equals() and hashCode() are overwritten so comparison by name does the right thing.
 * @author antonymouse
 * @since 0.0
 */
public class Command {
    private final int xCellIncrement, yCellIncrement;
    private final String name;
    private static final Logger logger = LogManager.getLogger();

    /**
     * 
     * @return true if the command is interruptable (i.e. another command can start before
     * animation for this one ends), false otherwise
     */
    public boolean isInterruptable() {
        return interruptable;
    }

    private final boolean interruptable;

    /**
     * 
     * @param value - command definition string: an int x direction; an int y direction; (optional) int key code for
     *              keyboard-driven commands; (optional) boolean true if the command is interruptable, default - false
     * @return true if the passed string conforms to command definition template, false otherwise
     */
    public static boolean isCommandDescription(@NotNull String value) {
        logger.debug("matching string->{}<",value);
        return value.matches("-?[0-9]+;-?[0-9]+;[0-9]*;(true|false)*");
    }


    /**
     * The direction the Sprite needs to move given this command
     * @return x cell direction (given x-axis right, y-axis down)
     */
    public int getxCellIncrement() {
        return xCellIncrement;
    }

    /**
     * The direction the Sprite needs to move given this command
     * @return y cell direction (given x-axis right, y-axis down)
     */
    public int getyCellIncrement() {
        return yCellIncrement;
    }

    /**
     * Overwritten to support comparison and sorting of commands by name
     * @return int code, same as name's
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // If the object is compared with itself then return true  
        if (obj == this) {
            return true;
        }
 
        //  "null instanceof [type]" also returns false
        if (!(obj instanceof Command)) {
            return false;
        }

        return name.equals(((Command) obj).name);
    }

    /**
     * Sets up this command
     *
     * @param xCellIncrement if the character moves with this command, what is the x direction and increment of the move
     * @param yCellIncrement if the character moves with this command, what is the y direction and increment of the move
     * @param interruptable if the command is interruptable (can be preempted by another command).
     */
   public Command(int xCellIncrement, int yCellIncrement, String name, boolean interruptable) {
       this.xCellIncrement = xCellIncrement;
       this.yCellIncrement = yCellIncrement;
       this.name = name;
       this.interruptable = interruptable;
   }
   
   @Override
   public String toString(){
       return getName();
   }

    /**
     * The unique name of this command
     * @return the name
     */
    public String getName() {
        return name;
    }
}
