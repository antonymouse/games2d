package com.goldberg.games2d;

import com.goldberg.games2d.data.Level;
import com.goldberg.games2d.exceptions.Games2dException;
import com.goldberg.games2d.gamelogic.*;
import com.goldberg.games2d.hardware.ImageInfo;
import com.goldberg.games2d.hardware.KeyPublisher;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * This class is mostly needed to configure shared communication components, enabling various parts of the application
 * to talk to each other without creation of direct code dependencies.
 */
@SuppressWarnings("unused")
public class MainModule extends AbstractModule {
    /**
     * Configures the bindings
     */
    @Override
    protected void configure() {
        Properties keyMap = new Properties();
        try {
            keyMap.load(Files.newInputStream(Path.of("configuration/keymap.properties")));
        } catch (IOException e) {
            throw new Games2dException("Unable to read key configuration file",e);
        }
        bind(Sprite.class).toProvider(SpriteProvider.class);
        Names.bindProperties(binder(), keyMap);
        bind(Game.class).in(Singleton.class);
        bind(KeyListener.class).to(KeyPublisher.class).in(Singleton.class);
    }
    @Provides @Singleton @Named("KeyInputQueue")
    BlockingQueue<int[]> makeKeyCommunicationQueue(){
        return new ArrayBlockingQueue<>(100);
    }

    @Provides @Singleton @Named("LevelDrawingQueue")
    BlockingQueue<ImageInfo> makeLevelDrawingQueue(){
        return new ArrayBlockingQueue<>(100);
    }

    /**
     * Considering type erasure we should start naming maps right away
     * @return a map of handlers - key to handler
     */
    @Provides @Singleton @GameUserInputHandlerMap
    Map<Integer, UserInputTriggeredState> makeGameUserHandlers(@Named("PAUSE_KEY") String PAUSE_KEY_CODE,
                                                               @Named("EXIT_KEY") String EXIT_KEY_CODE){
        HashMap<Integer,UserInputTriggeredState> handlers = new HashMap<>();
        UserInputTriggeredState state = new GamePause(PAUSE_KEY_CODE);
        handlers.put(state.codeMappedTo(),state);
        state = new GameQuit(EXIT_KEY_CODE);
        handlers.put(state.codeMappedTo(),state);
        return handlers;
    }
//    @Provides @Singleton
//    Sprite makeSprite(@Named("DataDirectoryPath") String dataDirPath, CommandSet defaultCommands, 
//                      Map<String,BehaviorStyle> behaviors){
//        return new Sprite(dataDirPath,"frog.txt", defaultCommands, behaviors);
//    }
    @Provides
    CommandSet makeCommandSet(){
        return new CommandSet().addCommand(new KeyCommand(0,1,"DOWN", KeyEvent.VK_DOWN))
                .addCommand(new KeyCommand(1,1,"DOWN_RIGHT",KeyEvent.VK_PAGE_DOWN))
                .addCommand(new KeyCommand(1,0,"RIGHT",KeyEvent.VK_RIGHT))
                .addCommand(new KeyCommand(1,-1,"UP_RIGHT",KeyEvent.VK_PAGE_UP))
                .addCommand(new KeyCommand(0,-1,"UP",KeyEvent.VK_UP))
                .addCommand(new KeyCommand(-1,-1,"UP_LEFT",KeyEvent.VK_HOME))
                .addCommand(new KeyCommand(-1,0,"LEFT",KeyEvent.VK_LEFT))
                .addCommand(new KeyCommand(-1,1,"DOWN_LEFT",KeyEvent.VK_END));
    }
    @Provides @Singleton
    Map<String,BehaviorStyle> makeBehaviors(Player player, Immovable immovable){
        HashMap<String, BehaviorStyle> behaviors = new HashMap<>();
        behaviors.put("PLAYER",player);
        behaviors.put("IMMOVABLE",immovable);
        return  behaviors;
    }
    @Provides
    Player makePlayer(){
        return new Player();
    }
    @Provides
    Immovable makeImmovable(){
        return new Immovable();
    }
    
    @Provides @Named("DataDirectoryPath")
    String dataDirectoryPath(){ return "data/"; }

    /**
     * todo needs to be extended to more than one level
     * @return an instance of game level. Currently, reads a single configuration file.
     */
    @Provides
    Level makeLevel(SpriteProvider spriteProvider, @Named("DataDirectoryPath") String dataDirPath, 
                    @Named("LevelDrawingQueue") BlockingQueue<ImageInfo> levelDrawingQueue){
        Level currentLevel = new Level(spriteProvider, dataDirPath, levelDrawingQueue);
        currentLevel.read("level1.txt");
        return currentLevel;
    }
}
