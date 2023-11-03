package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.data.Coordinates;
import org.jetbrains.annotations.NotNull;

/**
 * This is a Strategy for various kinds of movement, player and NPC. There can be as many of these as needed,
 * but for a basic game we would likely need no more than 3 or 4. Every Sprite gets configured with one of these and 
 * when a Sprite reaches a target it calls the implementation with the current state and all the other game information 
 * (other sprites, level). The movement sets the new target and a new command.
 * Lifecycle:
 * {@link 1.#initialize(Sprite.State, float, CommandSet)} is called before any other method.
 * {@link 2.#selectGoal(Command, Coordinates, long)} will be called first if there is any external command to process.
 * otherwise {@link #selectGoal(long)} is called. It's guaranteed the implementation will not miss any commands.
 * There can be spurious repetitive calls to {@link #selectGoal(long)} with the same gameTime.
 * @author antonymouse
 * @since 0.0
 */
public interface BehaviorStyle {
    /**
     * Used by the {@link Sprite} to "localize" the logic at the construction of a Sprite controlled by this
     * behavior
     *
     * @param controlledState the {@link Sprite}'s state
     * @param velocity        the movement velocity the {@link Sprite} was configured with
     */
    void initialize(Sprite.State controlledState, float velocity, CommandSet myCommands);
    /**
     * Adjusts the state as needed to reflect the movement algorithm
     *
     * @param keyPressed the command coming in
     * @param target for the command, if any
     * @param gameTime current game time
     */
    void selectGoal(@NotNull Command keyPressed, Coordinates target, long gameTime);
    /**
     * Adjusts the state as needed to reflect the movement algorithm
     *
     * @param gameTime current game time
     */
    void selectGoal(long gameTime);
}
