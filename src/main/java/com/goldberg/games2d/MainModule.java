package com.goldberg.games2d;

import com.goldberg.games2d.exceptions.Games2dException;
import com.goldberg.games2d.gamelogic.GamePause;
import com.goldberg.games2d.gamelogic.GameQuit;
import com.goldberg.games2d.gamelogic.Sprite;
import com.goldberg.games2d.gamelogic.UserInputTriggeredState;
import com.goldberg.games2d.hardware.KeyPublisher;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

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
        Names.bindProperties(binder(), keyMap);
        bind(Game.class).in(Singleton.class);
        bind(KeyListener.class).to(KeyPublisher.class).in(Singleton.class);
    }
    @Provides @Singleton
    BlockingQueue<int[]> makeKeyCommunicationQueue(){
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
    @Provides @Singleton
    Sprite makeSprite(@Named("DataDirectoryPath") String dataDirPath){
        return new Sprite(dataDirPath,"frog.txt");
    }


    @Provides @Named("DataDirectoryPath")
    String dataDirectoryPath(){ return "data/"; }
}
