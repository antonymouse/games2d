package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.data.Coordinates;
import com.goldberg.games2d.data.Level;
import com.goldberg.games2d.exceptions.AnimationException;
import com.goldberg.games2d.exceptions.LevelBuildingException;
import com.goldberg.games2d.hardware.ImageInfo;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.KeyEvent;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;

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
    private final Map<Command,Animation> animations;
    private final Map<Command,String> actionTargets;
    private final List<BehaviorStyle> myBehaviors;
    // these 2 are required at the configuration only
    private final  Map<String, BehaviorStyle> availableBehaviors;
    private final String dataDirPath;
    private final BlockingQueue<ImageInfo> levelDrawingQueue;
    private static final Logger logger = LogManager.getLogger();

    /**
     * The key defining the velocity of the sprite (one for all movements) in pixels per unit of time (todo define).
     */
    private static final String VELOCITY_KEY = "VELOCITY";
    private static final String STARTING_COORDINATES_KEY = "START_AT";
    private static final String MAX_DISTANCE_MOVE_KEY = "MAX_MOVE"; // max distance in tiles
    private static final String MOVEMENT_STYLE_KEY = "BEHAVIOR";
    private static final String COMMAND_KEY = "COMMAND";
    private final CommandSet myCommands;
    private float velocity;

    /**
     * 
     * @param dataDirPath  path to the folder with all the data files
     * @param copy sprite-specific copy of the command set
     * @param availableBehaviors all behaviors on this level
     */
    public Sprite(String dataDirPath, CommandSet copy, Map<String, BehaviorStyle> availableBehaviors,
                  @Named("LevelDrawingQueue")BlockingQueue<ImageInfo> levelDrawingQueue) {
        this.dataDirPath = dataDirPath;
        this.levelDrawingQueue = levelDrawingQueue;
        animations = new HashMap<>();
        actionTargets = new HashMap<>();
        logger.debug("available behaviors {} in the system",availableBehaviors.size());
        myBehaviors = new ArrayList<>();
        myCommands = copy;
        this.availableBehaviors = availableBehaviors;
    }

    public float getMaxMoveDistance() {
        return maxMoveDistance;
    }

    private float maxMoveDistance; // max distance in tiles
    private final List<String> spriteType = new ArrayList<>();

    public double calcDistance(Sprite sprite1) {
        return Coordinates.distance(state.current.getX(),state.current.getY(),sprite1.state.current.getX(),sprite1.state.current.getY());
    }


    public class State{
        // When falls to 0, character dies
        int energyLevel;
        Command currentCommand;
        long completion_time;
        long start_time;
        Coordinates start, end, current;
        long stateChangedAt;
        Animation currentAnimation;

        /**
         * Lets the configured {@link BehaviorStyle} choose animation
         * @param startingX initial coordinate
         * @param startingY initial coordinate
         */
        public State(int startingX, int startingY) {
            start = new Coordinates(0,0);
            end = new Coordinates(0,0);
            energyLevel = 100; // make it configurable, when needed. When falls to 0, character dies
            current = new Coordinates(startingX,startingY);
        }

        /**
         * Changes current command and animation to the animation set for that command
         * @param currentCommand the command to set
         */
        public void setCurrentCommand(Command currentCommand) {
            this.currentCommand = currentCommand;
            currentAnimation = animations.get(this.currentCommand);
        }
        public void setCurrentCommand(String currentCommand) {
            this.currentCommand = myCommands.byName(currentCommand);
            currentAnimation = animations.get(this.currentCommand);
        }

        /**
         * Increments or decrements energy level by the given value (positive or negative). If energy level falls
         * below 0, it's set to 0
         * @param delta the change value
         */
        public void changeEnergyLevel(int delta){
            energyLevel+=delta;
            if(energyLevel < 0){
                energyLevel = 0;
            }
        }
        /**
         * @return current energy level
         */
        public int getEnergyLevel(){
            return state.energyLevel;
        }

        /**
         * @return current position of the Sprite.
         */
        public Coordinates getCurrent(){
            return current;
        }
    }

    private State state;
    public State getState(){
        return state;
    }
    /**
     * The properties file specifies mappings of keys to animation descriptors (see {@link Animation} property files)
     * and other properties, such as interactions and behaviors for this Sprite.
     *
     * @param file properties file - Sprite's descriptor
     * @return configured instance (the same Sprite, but configured now).       
     */
    public Sprite configureFromFile(String file) {
        Properties descriptor = new Properties();
        // let's reuse the animations if they are used for more than 1 key
        HashMap<Path, Animation> usedAnimations = new HashMap<>();
        Path spriteDescriptor = FileSystems.getDefault().getPath(dataDirPath+file);
        int startingX=0,startingY=0;
        try {
            descriptor.load(Files.newInputStream(spriteDescriptor));
            logger.debug("value for STAY is  >{}<", descriptor.get("STAY"));
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
                } else if (keyName.startsWith(COMMAND_KEY)){
                    if(! Command.isCommandDescription(descriptor.getProperty(keyName))) {
                        logger.fatal("Command {} has invalid definition >{}<",keyName,descriptor.getProperty(keyName));
                        continue;
                    }
                    logger.debug("Adding command {}",keyName.substring(COMMAND_KEY.length()+1));
                    myCommands.addCommand(
                            CommandFactory.processCommandDescription(keyName.substring(COMMAND_KEY.length()+1)
                                    ,descriptor.getProperty(keyName)));
                } else if (keyName.startsWith(MOVEMENT_STYLE_KEY)) {
                    logger.debug("Looking for a behavior by name {}",descriptor.getProperty(keyName));
                    BehaviorStyle currentBehavior = availableBehaviors.get(descriptor.getProperty(keyName));
                    if(currentBehavior == null){
                        throw new LevelBuildingException("Unable to locate neither a behavior nor an interaction for " +
                                "name "+descriptor.getProperty(keyName));
                    } else {
                        spriteType.add(descriptor.getProperty(keyName));
                        myBehaviors.add(currentBehavior);
                    }
                } else {
                    String animationPath = descriptor.getProperty(keyName).split(";")[0];
                    String actionTarget = descriptor.getProperty(keyName).split(";")[1];
                    Path currentPath = FileSystems.getDefault().getPath(dataDirPath,animationPath);
                    logger.debug("Loading animation for key {}",keyName);
                    Animation currentAnimation = usedAnimations.get(currentPath);
                    if (currentAnimation == null) {
                        logger.debug("creating new animation for path {}",currentPath);
                        currentAnimation = new Animation(dataDirPath, animationPath, levelDrawingQueue);
                        usedAnimations.put(currentPath, currentAnimation);
                    }
                    animations.put(myCommands.byName(keyName), currentAnimation);
                    actionTargets.put(myCommands.byName(keyName),actionTarget);
                }
            }
        } catch (Throwable e) {
            throw new AnimationException("Unable to load sprite's description from " + spriteDescriptor, e);
        }
        if(myBehaviors.isEmpty() || myBehaviors.get(0)==null){
            throw new LevelBuildingException("Unable to locate any behaviors for this character "+file);
        }else {
            // starting animation. change to configurable in the file when needed.
            // state and behaviors need to be initialized only after all configuration is read
            state = new State(startingX,startingY);
            logger.debug("initialized a sprite with velocity {}, max distance {} and {} animations, " +
                            "starting point {},{}",velocity,
                    maxMoveDistance, animations.size(),startingX,startingY);
            // todo this HAS to change for more than one plant or any other setup where more than one instance
            //      can have the same behavior
            myBehaviors.forEach(behavior-> behavior.initialize(state,velocity,myCommands));
        }
        return this;
    }
    
    /**
     * Lets the Sprite react to user's input
     *
     * @param message           some keyboard event
     * @param currentTime       current game time
     */
    public void processMessage(int[] message, long currentTime, Level level){
        //state management
        if(state.energyLevel==0)
            return; // if it's dead, it's dead (but can come back if something rises the level)
        if(message[1]== KeyEvent.KEY_RELEASED){
            // ignore key release - not a command FOR THIS SPRITE
            processGameTick(currentTime);
            return;
        }
        //we haven't seen this time tick before
        if(state.stateChangedAt<currentTime){
            //let's do some generic preparations so behavior has some data to work with
            KeyCommand command = myCommands.valueOfKey(message[0]);
            Coordinates target;
            if(command!=null && actionTargets.get(command)!=null){
                target = level.findTile(state.current, command, actionTargets.get(command),this.maxMoveDistance);
            } else {
                target = null;
            }
            myBehaviors.forEach(behaviorStyle -> behaviorStyle.selectGoal(command, target, currentTime ));
//            myInteractions.forEach(interaction -> interaction.processInteraction(currentTime));
            
        }
        if(state.currentCommand == null){
            logger.fatal("Current command is not set.");
        }
        drawCurrentPosition(currentTime);
    }

    /**
     * If there is no user input, we still want to draw the Sprite wherever it is or update state. This is executed
     * if there is no command, but a game time has passed OR we need to redraw because screen buffer didn't survive
     *
     * @param currentTime       current game time
     */
    public void processGameTick(long currentTime){
        if(state.energyLevel==0)
            return; // if it's dead, it's dead (but can come back if something rises the level)
        if (state.stateChangedAt < currentTime) {
            myBehaviors.forEach(bs->bs.selectGoal(currentTime));
//            myInteractions.forEach(interaction -> interaction.processInteraction(currentTime));
        }
        if(state.currentCommand == null){
            logger.fatal("Current command is not set.");
        } 
        drawCurrentPosition(currentTime);
    }


    /**
     * Draws a frame of current animation in the current position if the buffer needs to be redrawn. Minimizes the
     * calculations and avoids calculating deltas
     * @param currentTime current game time
     */
    private void drawCurrentPosition(long currentTime){
        if(state.currentCommand == null || state.currentAnimation ==null) {
            String behaviorStyle = (myBehaviors == null || myBehaviors.isEmpty())? "null" : myBehaviors.get(0).toString();
            int numBehaviors = (myBehaviors == null || myBehaviors.isEmpty())? 0: myBehaviors.size();
            logger.error("No current command or animation set on sprite with behavior {} out of {}, with command {}" +
                    " and animation {}",behaviorStyle,numBehaviors,state.currentCommand, state.currentAnimation);
        }
            
        state.currentAnimation.draw(state.start_time, state.completion_time, currentTime,state.current.getX() ,
                state.current.getY());
    }

    /**
     * @param type (behavior) the caller is looking for
     * @return true if the sprite is of that type (has that behavior), false otherwise
     */
    public boolean isOfType(String type){
        return spriteType.contains(type);
    }

    /**
     * @return copy (change to the values is not reflected by the internal state) of the current location
     */
    Coordinates getCurrentLocation(){
        return state.current.copy();
    }
    /**
     * @return copy (change to the values is not reflected by the internal state) of the current target
     */
    Coordinates getCurrentTarget(){
        if(state.end!=null)
            return state.end.copy();
        return null;
    }
    float getVelocity(){
        return velocity;
    }
}
