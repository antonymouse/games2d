package com.goldberg.games2d;

import com.goldberg.games2d.data.Level;
import com.goldberg.games2d.exceptions.GraphicsException;
import com.goldberg.games2d.gamelogic.PredefinedCommand;
import com.goldberg.games2d.gamelogic.Sprite;
import com.goldberg.games2d.gamelogic.UserInputTriggeredState;
import com.goldberg.games2d.hardware.KeyPublisher;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import jakarta.inject.Qualifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
@interface GameUserInputHandlerMap {}

//@Qualifier
//@Retention(RUNTIME)
//@interface DataDirectoryPath {}

/**
 * Initializes and runs the game, including the GUI, sound etc
 */
public class Game {
    private GraphicsDevice device; // the default screen, need to keep the reference to restore at the end (todo)
    private Level currentLevel;
    private static final Logger logger = LogManager.getLogger();
    private final Map<Integer, UserInputTriggeredState> userInputHandlers;
    private final int PAUSE_KEY;
    private final int EXIT_KEY;
    private final String dataDirPath;
    @Inject
    private Sprite frog;

    @Inject
    public Game(BlockingQueue<int[]> comingKeys,
                @GameUserInputHandlerMap Map<Integer, UserInputTriggeredState> handlers,
                @Named("PAUSE_KEY") String PAUSE_KEY_CODE,
                @Named("EXIT_KEY") String EXIT_KEY_CODE,
                @Named("DataDirectoryPath") String dataDirPath) {
        this.PAUSE_KEY = Integer.parseInt(PAUSE_KEY_CODE);
        this.EXIT_KEY = Integer.parseInt(EXIT_KEY_CODE);
        this.userInputHandlers = handlers;
        this.comingKeys = comingKeys;
        this.dataDirPath = dataDirPath;
    }

    /**
     * The queue for the keys pressed while painting was going on. Singleton shared between
     * the key listener and the game loop.
     */
    private final BlockingQueue<int[]> comingKeys;



    public static void main(String[] args) {
        try {
            Configurator.initialize("games2d","configuration/log4j2.xml");
            Injector injector = Guice.createInjector(new MainModule());
            Game game = injector.getInstance(Game.class);
            KeyPublisher keysSource = injector.getInstance(KeyPublisher.class);
            game.initGUI();
            game.initGameParts();
            game.device.getFullScreenWindow().addKeyListener(keysSource);
            logger.debug("about to go into the gameLoop");
            game.runTheGameLoop();
        }
        catch (Throwable t) {
            System.out.println(t);
            t.printStackTrace();
        } 
        finally {
            System.exit(0);
        }
    }

    private void initGUI(){
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        device = environment.getDefaultScreenDevice();
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setIgnoreRepaint(true);
        frame.setResizable(false);

        device.setFullScreenWindow(frame);

        if (device.isDisplayChangeSupported())
        {
            // not really clear if we need to do this, but let's set for the first release (todo)
            DisplayMode displayMode = new DisplayMode(1024, 768, 32, 0);
            logger.debug("display mode set ok");
            try {
                device.setDisplayMode(displayMode);
            }
            catch (IllegalArgumentException e) {
                logger.debug("Unable to set display mode we wanted",e);
            }
            // fix for mac os x
            frame.setSize(displayMode.getWidth(), displayMode.getHeight());
        }
        frame.createBufferStrategy(2);
    }

    /**
     * todo change to something intelligent
     */
    private void initGameParts(){
        currentLevel = new Level();
        currentLevel.read(dataDirPath+"level1.txt");
        logger.debug("level loaded");
        // Sprite initialization
    }


    private void processUserInput(Graphics2D g, long currentGameTime){
        boolean spritesProcessed = false; // for more than 1 sprite, make this an array
        while(comingKeys.peek()!=null){
            try {
                int[] message = comingKeys.take();
                if(userInputHandlers.containsKey(message[0])){
                    logger.debug("processing key {}",message[0]);
                    userInputHandlers.get(message[0]).processMessage(message);
                } else if (PredefinedCommand.valueOfKey(message[0])!=null) {
                    frog.processMessage(message,currentGameTime,currentLevel,g);
                    spritesProcessed = true;
                }
            } catch (InterruptedException ie){
                logger.error("Take from non-empty communication queue got interrupted. Shouldn't be happening",ie);
            }
        }
        if(!spritesProcessed){
            // for more than 1 sprite, make this a loop
            frog.processGameTick(currentGameTime,g);
        }
    }
   
    /**
     * executes the game loop - i.e. a sequence of game steps with post-processing.
     */
    private void runTheGameLoop(){
        UserInputTriggeredState quit = userInputHandlers.get(EXIT_KEY);
        UserInputTriggeredState pause = userInputHandlers.get(PAUSE_KEY);
        try {
            do {
                gameStep(pause);
            } while (!quit.isActive());
        }finally {
            closeGui();
        }
    }

    private void pause(Graphics2D g) {
        Color current = g.getColor();
        g.setColor(Color.RED);
        g.drawString("PAUSED",getWidth()/2-g.getFont().getSize()*3,getHeight()/2-g.getFont().getSize()/2);
        g.setColor(current);
    }

    /**
     * Executes all the stuff that needs to be done in single game time tick.
     */
    private void gameStep(UserInputTriggeredState pause){
        BufferStrategy strategy;
        int infiniteLoopCounter = 0;
        long currentGameTime = System.currentTimeMillis();
        strategy = getWindowBufferStrategy();
        if(strategy == null){
            logger.fatal("received null buffer strategy, nowhere to draw.");
            return;
        }
        do {
            // draw the screen
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            logger.debug("about to draw the level, loop counter = {}",infiniteLoopCounter);
            try {
                currentLevel.draw(g, getWidth(), getHeight());
                processUserInput(g,currentGameTime);
                if(pause.isActive()){
                    pause(g);
                }
                g.dispose();
            } catch (Throwable t) {
                logger.error("Couldn't finish drawing the level as,", t);
            } finally {
                logger.debug("done drawing the level");
            }
            infiniteLoopCounter++;
        }while (strategy.contentsRestored() && infiniteLoopCounter<10);
        if(infiniteLoopCounter >=10){
            throw new GraphicsException("Drawing a level couldn't be completed - the memory is too volatile.");
        }
        //todo might need an external loop while (strategy.contentsLost());
        try {
            if (!strategy.contentsLost()) {
//                    logger.debug("about to show the strategy");
                    strategy.show();
            }
            else {
                logger.debug("STRATEGY.CONTENT LOST()");
            }
        }catch (Throwable t){
            logger.error("unable to show the strategy due to",t);
        }
//        finally {
//            logger.debug("Strategy shown");
//        }
        // Sync the display on some systems.
        // (on Linux, this fixes event queue problems)
        Toolkit.getDefaultToolkit().sync();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return strategy for the drawing window or null if there is a problem
     */
    public BufferStrategy getWindowBufferStrategy() {
        Window window = device.getFullScreenWindow();
        if (window != null) {
            try {
//                logger.debug("getting the strategy for the window");
                return window.getBufferStrategy();
            }catch (Throwable t){
                logger.error("Couldn't get graphics from the window",t);
                return null;
            }
//            finally {
//                logger.debug("got strategy, coming back");
//            }
        }
        else {
            logger.fatal("returning null strategy bcz the window is null");
            return null;
        }
    }
    public int getWidth() {
        Window window = device.getFullScreenWindow();
        if (window != null) {
            return window.getWidth();
        }
        else {
            return 0;
        }
    }


    /**
     Returns the height of the window currently used in full
     screen mode. Returns 0 if the device is not in full
     screen mode.
     */
    public int getHeight() {
        Window window = device.getFullScreenWindow();
        if (window != null) {
            return window.getHeight();
        }
        else {
            return 0;
        }
    }
    private void closeGui(){
        Window window = device.getFullScreenWindow();
        if (window != null) {
            window.dispose();
        }
        device.setFullScreenWindow(null);
    }
}