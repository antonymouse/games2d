package com.goldberg.games2d.gamelogic;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Defines an interaction between a {@link Sprite} and a set of other {@link Sprite}s that are within interaction 
 * distance of Sprites. If there is no Interaction, but the Sprites are within the
 * Sprite's interaction radius, the sprite ignores another sprite.
 * The lifecycle:
 * {@link 1.#setAllSprites(List, Map)} is guaranteed to be called before any other method.  
 * {@link 2.#initialize(Sprite, CommandSet)} is guarateed to be called after setAllSprites but before any other method
 * {@link 3.#processInteraction(long)} by the time of this call, all behaviors were activated and the matrix of distances
 * is correct.
 * @author antonymouse
 * @since 0.1
 */
public interface Interaction {
    /**
     * @param allSprites all sprites of a given {@link com.goldberg.games2d.data.Level}
     * @param distances the distances from this sprite to all sprites of this level (including itself)
     *                  can be all 0 at the time of call
     * @return this Interaction, but initialized with all Sprites.
     */
    Interaction setAllSprites(List<Sprite> allSprites, Map<Sprite,int[]> distances);
    /**
     * Provides the behavior with the set of Sprite-specific context. Callable from the Sprite.
     * @param me the sprite this interaction is operating on
     * @param spriteCommands commands, available to that Sprite
     */
    void initialize(final @NotNull Sprite me, final @NotNull CommandSet spriteCommands);

    /**
     * Activates interaction when two or more sprites are within interaction distance (defined as the least of max 
     * distances)
     * @param gameTime gameTime current game time
     */
    void processInteraction(long gameTime);

    /**
     * Checks if the interaction between this sprite and another sprite actually happened.
     * Care needs to be taken on the code level to make sure this check is implemented symmetrically on both ends.
     * @param me this sprite
     * @param other the other sprite
     * @param distance the distance between sprites
     * @return true if happened, false otherwise.
     */
    boolean hasInteractionHappened(final @NotNull Sprite me, final @NotNull Sprite other, int distance);
}
