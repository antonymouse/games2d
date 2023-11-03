package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.data.Coordinates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.goldberg.games2d.data.Coordinates.coordinateChange;

/**
 * This is a plant's counterpart to {@link FrogIsEatenByPlantInteraction}: what does plant do while eating the frog.
 * @author antonymouse
 * @since 0.1
 */
public class PlantEatsFrogInteraction implements Interaction{
    private CommandSet spriteCommands;
    private Sprite.State spriteState;
    private Sprite me;
    private List<Sprite> currentLevelSprites;
    private int[] currentSpriteDistances;
    private Map<Sprite,int[]> distances;
    public final String DEFAULT_COMMAND = "STAY";

    protected static final Logger logger = LogManager.getLogger(PlantEatsFrogInteraction.class);

    @Override
    public Interaction setAllSprites(List<Sprite> allSprites, Map<Sprite,int[]> distances) {
        currentLevelSprites = allSprites;
        this.distances = distances;
        currentSpriteDistances = distances.get(me);
        return this;
    }

    @Override
    public void initialize(@NotNull Sprite me,@NotNull CommandSet spriteCommands) {
        this.spriteCommands = spriteCommands;
        this.spriteState = me.getState();
        this.me = me;
    }

    @Override
    public void processInteraction(long gameTime) {
        if (spriteState.currentCommand == null) {
            logger.debug("Setting current command to {}", spriteCommands.byName(DEFAULT_COMMAND));
            spriteState.setCurrentCommand(spriteCommands.byName(DEFAULT_COMMAND));
        } else if (spriteState.completion_time <= gameTime && spriteState.currentCommand.equals(spriteCommands.byName("HUNT"))) {
            executeCommand(gameTime);
            spriteState.setCurrentCommand(spriteCommands.byName(DEFAULT_COMMAND));
        } else {
            for (int i = 0; i < currentLevelSprites.size(); i++) {
                Sprite otherSprite = currentLevelSprites.get(i);
                if (hasInteractionHappened(me, otherSprite, currentSpriteDistances[i])) {
                    Coordinates intercept = Coordinates.intersection(otherSprite.getCurrentLocation(),
                            otherSprite.getCurrentTarget(), otherSprite.getVelocity(), me.getCurrentLocation(), me.getVelocity());
                    logger.debug("Intercept found at {}, executing", intercept);
                    me.changeEnergyLevel(100); // no purpose right now, but food!
                    executeHunt(intercept, gameTime);
                    break;
                }
            }
        }
    }
    private void executeHunt(Coordinates target, long gameTime) {
        if (spriteState.currentCommand != null && !spriteState.currentCommand.equals(spriteCommands.byName("HUNT"))){
            // new state, let's see where we end up
            spriteState.end.assign(target);
            spriteState.start.assign(spriteState.current);
            spriteState.setCurrentCommand(spriteCommands.byName("HUNT"));
            spriteState.start_time = gameTime;
            double distance = Coordinates.distance(spriteState.end.getX(), spriteState.end.getY(),
                    spriteState.start.getX(),spriteState.start.getY());
            spriteState.completion_time = (long) (distance / me.getVelocity()) + gameTime;
        }
        logger.debug("Executing HUNT ");
        //continue executing the current state or execute the new one
        executeCommand(gameTime);
    }
    private void executeCommand(long currentTime) {
        // handle move
        spriteState.stateChangedAt = currentTime;
        spriteState.current.setX(coordinateChange(spriteState.start.getX(), spriteState.end.getX(),
                spriteState.start_time, spriteState.completion_time, currentTime));
        spriteState.current.setY(coordinateChange(spriteState.start.getY(), spriteState.end.getY(),
                spriteState.start_time, spriteState.completion_time, currentTime));
    }
    @Override
    public boolean hasInteractionHappened(@NotNull Sprite me, @NotNull Sprite other, int distance) {
        if(other!=me && other.isOfType("PLAYER") && distance <=me.getMaxMoveDistance()) {
            Coordinates intercept = Coordinates.intersection(other.getCurrentLocation(),
                    other.getCurrentTarget(), other.getVelocity(), me.getCurrentLocation(), me.getVelocity());
            return intercept != null;
        }else {
            return false;
        }
    }

}
