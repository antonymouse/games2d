package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.data.Coordinates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Frog's behavior when it's within the reach of a frog-eating plant
 * @author antonymouse
 * @since 0.0
 */
public class FrogIsEatenByPlantInteraction implements Interaction{
    private Sprite me;
    private List<Sprite> currentLevelSprites;
    private  Map<Sprite,int[]> distances;
    private int[] currentSpriteDistances;
    protected static final Logger logger = LogManager.getLogger();

    @Override
    public Interaction setAllSprites(List<Sprite> allSprites, Map<Sprite,int[]> distances) {
        currentLevelSprites = allSprites;
        this.distances = distances;
        logger.debug("have {} sprites and {} distances",allSprites.size(),this.distances.size());
        currentSpriteDistances = distances.get(me);
        return this;
    }

    @Override
    public void initialize(@NotNull Sprite me, @NotNull CommandSet spriteCommands) {
        this.me = me;
    }

    /**
     * Switches frog's state to "dead" if it's within movement distance of a plant
     */
    @Override
    public void processInteraction(long gameTime) {
        logger.debug("Processing FrogIsEaten");
        for (int i = 0; i < currentLevelSprites.size(); i++) {
            Sprite otherSprite =  currentLevelSprites.get(i);
            logger.debug("Current distance to sprite {} is {}",i,currentSpriteDistances[i]);
            if(hasInteractionHappened(me, otherSprite, currentSpriteDistances[i])) {
                me.changeEnergyLevel(-100000); // some really large number. if needed, formalize into kill()
                logger.debug("Adjusting player's energy level by -100000");
                break;
            }
        }
    }

    @Override
    public boolean hasInteractionHappened(@NotNull Sprite me, @NotNull Sprite other, int distance) {
        if(other!=me && other.isOfType("IMMOVABLE") && distance <=other.getMaxMoveDistance()) {
            Coordinates intercept = Coordinates.intersection(me.getCurrentLocation(),
                    me.getCurrentTarget(), me.getVelocity(), other.getCurrentLocation(), other.getVelocity());
            return intercept != null;
        }else {
            return false;
        }
    }
}
