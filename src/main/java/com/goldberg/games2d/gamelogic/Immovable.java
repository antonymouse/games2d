package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.data.Coordinates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The class name says it's all - doesn't move from where it's placed. Reacts to one command - STAY
 * @author antonymouse
 * @since 0.0
 */
public class Immovable implements BehaviorStyle {
    public static final Logger logger = LogManager.getLogger(Immovable.class);
    private float spritesVelocity;
    private Sprite.State controlledState;
    public final String DEFAULT_COMMAND = "STAY";
    private CommandSet myCommands;

    
    /**
     * @param controlledState the {@link Sprite}'s state
     * @param velocity        the movement velocity the {@link Sprite} was configured with
     */
    @Override
    public void initialize(Sprite.State controlledState, float velocity, CommandSet myCommands) {
        this.controlledState = controlledState;
        this.spritesVelocity = velocity;
        this.myCommands = myCommands;
        controlledState.setCurrentCommand(myCommands.byName(DEFAULT_COMMAND));
        logger.debug("Initialized behavior with command {}",DEFAULT_COMMAND);
    }

    /**
     * We don't care if any key was pressed. This character doesn't move in response to key command
     */
    @Override
    public void selectGoal(@NotNull Command keyPressed, Coordinates target, long gameTime) {

    }

    /**
     * We don't care if any key was pressed. This character doesn't move in response to key command
     */
    @Override
    public void selectGoal(long gameTime) {

    }
}
