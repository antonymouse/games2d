package com.goldberg.games2d.gamelogic;

/**
 * Implements an interaction logic between 2 Sprites. Accesses Sprites internal state
 * @author antonymouse
 * @since 0.0
 */
public interface BinaryInteraction {
    /**
     * 
     * @return the max distance where the {@link Sprite}s can interact
     */
    int getInteractionDistance();

    /**
     * 
     * @return String[2] array with types of {@link Sprite}s that are covered by this interaction
     */
    String[] getInteractingTypes();

    /**
     * Implements the interaction itself (can be doing nothing based on the states of the actual {@link Sprite}s).
     * Gets called by the Level after "process" call to the Sprites
     * @param s1 one of the Sprites
     * @param s2 the other Sprite. Can't be the same as the first
     * @param gameTick current game time
     */
    void interact(Sprite s1, Sprite s2, long gameTick);
}
