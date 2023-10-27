package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.data.Coordinates;
import com.goldberg.games2d.data.Level;
import com.goldberg.games2d.exceptions.AnimationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The class is a container for all {@link Animation}s for one character. Animations are stateless, this class is 
 * stateful. Maps animations to actions. Makes sure the actions properly flow one into another (as the character
 * needs).
 * Loads Sprite's structure from a file. The file maps commands (keys) to animations. Keys have fixed meaning (i.e.
 * a user can't redefine the keys).
 * LEFT, RIGHT, UP, DOWN, PGUP, PGDN, HOME, END - directions;target tile symbol
 * SPACE - primary action
 * VELOCITY - move velocity
 * START_AT initial position on a level
 * MAX_MOVE max distance of a single move in any direction
 * @author antonymouse
 * @since 0.0
 */
public class Sprite {
    private final Map<PredefinedCommand,Animation> animations;
    private final Map<PredefinedCommand,String> actionTargets;
    private static final Logger logger = LogManager.getLogger();

    /**
     * The key defining the velocity of the sprite (one for all movements) in pixels per unit of time (todo define).
     */
    private static final String VELOCITY_KEY = "VELOCITY";
    private static final String STARTING_COORDINATES_KEY = "START_AT";
    private static final String MAX_DISTANCE_MOVE_KEY = "MAX_MOVE"; // max distance in tiles
    private float velocity,maxMoveDistance; // max distance in tiles


    private static class State{
        private PredefinedCommand currentCommand;
        long completion_time;
        long start_time;
        Coordinates start, end, current;
        long stateChangedAt;
        Animation currentAnimation;

        public State(Animation currentAnimation, int startingX, int startingY) {
            this.currentAnimation = currentAnimation;
            start = new Coordinates(0,0);
            end = new Coordinates(0,0);
            current = new Coordinates(startingX,startingY);
        }

        public void setCurrentCommand(PredefinedCommand currentCommand) {
            this.currentCommand = currentCommand;
        }
    }

    private final State state;
    /**
     * The properties file specifies mappings of keys to animation descriptors (see {@link Animation} property files)
     * @param dataDirPath path to the folder with all the data files
     * @param file properties file - Sprite's descriptor
     */
    public Sprite(String dataDirPath, String file) {
        Properties descriptor = new Properties();
        // let's reuse the animations if they are used for more than 1 key
        HashMap<Path, Animation> usedAnimations = new HashMap<>();
        animations = new HashMap<>();
        actionTargets = new HashMap<>();
        Path spriteDescriptor = FileSystems.getDefault().getPath(dataDirPath+file);
        int startingX=0,startingY=0;
        try {
            descriptor.load(Files.newInputStream(spriteDescriptor));
            for (String keyName : descriptor.stringPropertyNames()) {
                // should be just one entry for each key in the file, but in the worst case they will just overwrite
                if (VELOCITY_KEY.compareTo(keyName) == 0) {
                    velocity = Float.parseFloat(descriptor.getProperty(VELOCITY_KEY));
                } else if (STARTING_COORDINATES_KEY.compareTo(keyName) == 0) {
                    String[] sCoords = descriptor.getProperty(STARTING_COORDINATES_KEY).split(";");
                    startingX = Integer.parseInt(sCoords[0]);
                    startingY = Integer.parseInt(sCoords[1]);
                } else if (MAX_DISTANCE_MOVE_KEY.compareTo(keyName) == 0) {
                    maxMoveDistance = Float.parseFloat(descriptor.getProperty(MAX_DISTANCE_MOVE_KEY)); // int tiles
                } else {
                    String animationPath = descriptor.getProperty(keyName).split(";")[0];
                    String actionTarget = descriptor.getProperty(keyName).split(";")[1];
                    Path currentPath = FileSystems.getDefault().getPath(dataDirPath,animationPath);
                    logger.debug("Loading animation for key {}",keyName);
                    Animation currentAnimation = usedAnimations.get(currentPath);
                    if (currentAnimation == null) {
                        logger.debug("creating new animation for path {}",currentPath);
                        currentAnimation = new Animation(dataDirPath, animationPath);
                        usedAnimations.put(currentPath, currentAnimation);
                    }
                    animations.put(PredefinedCommand.valueOf(keyName), currentAnimation);
                    actionTargets.put(PredefinedCommand.valueOf(keyName),actionTarget);
                }
            }
        } catch (Throwable e) {
            throw new AnimationException("Unable to load sprite's description from " + spriteDescriptor, e);
        }
        // starting animation. change to configurable in the file when needed.
        state = new State(animations.get(PredefinedCommand.RIGHT),startingX, startingY);
        logger.debug("initialized a sprite with velocity {}, max distance {} and {} animations",velocity,
                maxMoveDistance, animations.size());
    }

    /**
     * Lets the Sprite react to user's input
     *
     * @param message some keyboard event
     * @param currentTime current game time
     * @param g to draw on if needed
     */
    public void processMessage(int[] message, long currentTime, Level level, Graphics2D g){
        //state management
        if(message[1]== KeyEvent.KEY_RELEASED){
            // ignore key release - not a command FOR THIS SPRITE
            processGameTick(currentTime,g);
            return;
        }
        //no current command and we haven't seen this time tick before
        if(state.currentCommand==null && state.stateChangedAt<currentTime){
            PredefinedCommand command = PredefinedCommand.valueOfKey(message[0]);
            if(command!=null && actionTargets.get(command)!=null) {
                logger.debug("looking for target for the command {} and type {}",command.name(),actionTargets.get(command));
                Coordinates target = level.findTile(state.current,command, actionTargets.get(command),this.maxMoveDistance);
                if(target!=null)
                    executeCommand(command, currentTime, g, target);
            }
            else {
                // if command exists but isn't mapped to an action for any reason
                state.currentAnimation.drawDefaultFrame(state.current.getX(),state.current.getY(),g);
            }
        }else {
            // current command is still active and will be active after OR
            // it should've completed before previous call here and now.
            executeCommand(currentTime,g);
        }
    }

    /**
     * If there is no user input, we still want to draw the Sprite wherever it is or update state
     * @param currentTime current game time
     * @param g where to draw
     */
    public void processGameTick(long currentTime, Graphics2D g){
        if(state.currentCommand == null){
            state.currentAnimation.drawDefaultFrame(state.current.getX(),state.current.getY(),g);
        }
        else{
            executeCommand(currentTime, g);
        }
    }

    private void executeCommand(long currentTime, Graphics2D g) {
        if(state.stateChangedAt<currentTime){
            // handle move
            state.stateChangedAt = currentTime;
            state.current.setX(coordinateChange(state.start.getX(),state.end.getX(),
                    state.start_time, state.completion_time,currentTime));
            state.current.setY(coordinateChange(state.start.getY(),state.end.getY(),
                    state.start_time, state.completion_time,currentTime));

        }
        drawCurrentPosition(currentTime, g);
        // complete the command if this is the end
        if(currentTime>state.completion_time){
            state.setCurrentCommand(null);
        }
    }

    /**
     * Draws a frame of current animation in the current position if the buffer needs to be redrawn. Minimizes the
     * calculations and avoids calculating deltas
     * @param currentTime current game time
     * @param g where to draw
     */
    private void drawCurrentPosition(long currentTime, Graphics2D g){
        if(state.currentCommand == null){
            state.currentAnimation.drawDefaultFrame(state.current.getX(),state.current.getY(),g);
        }else {
            Animation currentAnimation = animations.get(state.currentCommand);
            currentAnimation.draw(state.start_time, state.completion_time, currentTime,state.current.getX() ,
                    state.current.getY(), g);
        }
    }
    private void executeCommand(PredefinedCommand command, long currentTime, Graphics2D g, Coordinates target) {
        if(state.stateChangedAt<currentTime){
            // new state, let's see where we end up
            state.end.assign(target);
            state.start.assign(state.current);
            state.setCurrentCommand(command);
            state.start_time = currentTime;
            double distance = Math.sqrt(Math.pow(Math.abs(state.end.getX() - state.start.getX()), 2) +
                    Math.pow(Math.abs(state.end.getY() - state.start.getY()), 2));
            state.completion_time = (long) (distance / velocity) + currentTime;
        }
        executeCommand(currentTime, g);
    }
    private int coordinateChange(int startValue, int endValue, long startTime, long endTime, long currentTime){
        return (currentTime>=endTime)?endValue:(int) (startValue + (float)(endValue - startValue)*
                (float)(currentTime-startTime)/(float)(endTime-startTime));
    }
}
