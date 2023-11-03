package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.data.Coordinates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import static com.goldberg.games2d.data.Coordinates.coordinateChange;

/**
 * Basics of user-controlled character: moves in response to movement keys if there is a target for it.
 * @author antonymouse
 * @since 0.0
 */
public class Player implements BehaviorStyle {

    public static final Logger logger = LogManager.getLogger(Player.class);
    private float spritesVelocity;
    private Sprite.State controlledState;
    public final String DEFAULT_COMMAND = "STAY";


    private CommandSet myCommands;


    /**
     * If there is no user input, we still want to draw the Sprite wherever it is or update state.
     * Some of behaviors can generate extra commands from the inside, too
     * @param currentTime current game time
     */
    public void selectGoal(long currentTime){
        if(controlledState.currentCommand == null){
            logger.debug("Setting current command to {}",myCommands.byName(DEFAULT_COMMAND));
            controlledState.setCurrentCommand(myCommands.byName(DEFAULT_COMMAND));
        } else if (controlledState.completion_time<=currentTime) {
            executeCommand(currentTime);
            controlledState.setCurrentCommand(myCommands.byName(DEFAULT_COMMAND));
        } else{
            executeCommand(currentTime); // continue executing the current command
        }
    }
    

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

    @Override
    public void selectGoal(@NotNull Command keyPressed, Coordinates target,
                           long currentTime) {
        if (target != null) {
            executeCommand(keyPressed, currentTime, target);
        } else {
            // current command is still active and will be active after OR
            // it should've completed before previous call here and now.
            executeCommand(currentTime);
        }
    }
    private void executeCommand(@NotNull Command command, long currentTime, Coordinates target) {
        if (controlledState.currentCommand == null || controlledState.currentCommand.isInterruptable()
        ||  currentTime>=controlledState.completion_time){ // complete the command if this is the end
            // new state, let's see where we end up
            controlledState.end.assign(target);
            controlledState.start.assign(controlledState.current);
            controlledState.setCurrentCommand(command);
            controlledState.start_time = currentTime;
            double distance =Coordinates.distance(controlledState.start.getX(), controlledState.start.getY(),
                    controlledState.end.getX(),controlledState.end.getY());
            controlledState.completion_time = (long) (distance / this.spritesVelocity) + currentTime;
        }
        logger.debug("Executing command {} for key {}",command.getName(), (command instanceof KeyCommand)?
                ((KeyCommand)command).getKey() : "not a key command");
        //continue executing the current state or execute the new one
        executeCommand(currentTime);
    }
    private void executeCommand(long currentTime) {
        // handle move
        controlledState.stateChangedAt = currentTime;
        controlledState.current.setX(coordinateChange(controlledState.start.getX(), controlledState.end.getX(),
                controlledState.start_time, controlledState.completion_time, currentTime));
        controlledState.current.setY(coordinateChange(controlledState.start.getY(), controlledState.end.getY(),
                controlledState.start_time, controlledState.completion_time, currentTime));
    }

}
