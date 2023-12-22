package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.data.Coordinates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import static com.goldberg.games2d.data.Coordinates.coordinateChange;

/**
 * Implements an actual interaction logic between a Frog and a Plant 
 * @author antonymouse
 * @since 0.0
 */
public class FrogPlantBinaryInteraction implements BinaryInteraction{
    protected static final Logger logger = LogManager.getLogger();
    public static final String HUNT = "HUNT";
    private final String [] INTERACTION_TYPES = {"IMMOVABLE", "PLAYER"};
    @Override
    public int getInteractionDistance() {
        return 2; // this is Plant's min distance. Can be configurable but that only adds more complexity
    }

    @Override
    public String[] getInteractingTypes() {
        return INTERACTION_TYPES;
    }

    @Override
    public void interact(@NotNull Sprite s1, @NotNull Sprite s2, long gameTick) {
        Sprite player = (s1.isOfType("PLAYER"))? s1 : s2;
        Sprite plant = (s1.isOfType("PLAYER"))? s2 : s1;
        // plant part
        if (plant.getState().currentCommand.getName().equals(HUNT)) {
            if(plant.getState().completion_time <= gameTick ) {
                //if the HUNT should be over, the next command is STAY
                plant.getState().setCurrentCommand("STAY");
                logger.debug("switched from HUNT to STAY, completion time {} current time {}",
                        plant.getState().completion_time, gameTick);
            }
            else{
                // we are still hunting
                executeCommand(plant,gameTick);
                logger.debug("hunting...");
            }
        }else {
            // not hunting yet
            Coordinates intercept = Coordinates.intersection(player.getCurrentLocation(),
                    player.getCurrentTarget(), player.getVelocity(), plant.getCurrentLocation(), plant.getVelocity());
            if(intercept==null || player.getState().getEnergyLevel()<=0) // either no intercept or the frog is dead
            {
                return;
            }
            logger.debug("Intercept found at {}, executing", intercept);
            plant.getState().changeEnergyLevel(100); // no purpose right now, but food!
            player.getState().changeEnergyLevel(-10000);
            executeHunt(plant,intercept, gameTick);
        }
    }
    private void executeHunt(Sprite plant, Coordinates target, long gameTime) {
        // new state, let's see where we end up
        plant.getState().end.assign(target);
        plant.getState().start.assign(plant.getState().current);
        plant.getState().setCurrentCommand(HUNT);
        plant.getState().start_time = gameTime;
        double distance = Coordinates.distance(plant.getState().end.getX(), plant.getState().end.getY(),
                plant.getState().start.getX(), plant.getState().start.getY());
        plant.getState().completion_time = (long) (distance / plant.getVelocity()) + gameTime;
        logger.debug("Executing HUNT ");
        //continue executing the current state or execute the new one
        executeCommand(plant,gameTime);
    }
    private void executeCommand(Sprite plant, long currentTime) {
        // handle move
        plant.getState().stateChangedAt = currentTime;
        plant.getState().current.setX(coordinateChange(plant.getState().start.getX(), plant.getState().end.getX(),
                plant.getState().start_time, plant.getState().completion_time, currentTime));
        plant.getState().current.setY(coordinateChange(plant.getState().start.getY(), plant.getState().end.getY(),
                plant.getState().start_time, plant.getState().completion_time, currentTime));
    }

}
