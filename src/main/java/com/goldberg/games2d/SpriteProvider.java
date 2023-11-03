package com.goldberg.games2d;

import com.goldberg.games2d.gamelogic.BehaviorStyle;
import com.goldberg.games2d.gamelogic.CommandSet;
import com.goldberg.games2d.gamelogic.Interaction;
import com.goldberg.games2d.gamelogic.Sprite;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import java.util.Map;

/**
 * Provider for {@link com.goldberg.games2d.gamelogic.Sprite} instances
 * @author antonymouse
 * @since 0.0
 */
public class SpriteProvider implements Provider<Sprite> {
    private final String dataDirPath;
    private final CommandSet defaultCommands;
    private final Map<String, BehaviorStyle> behaviors;
    private final Map<String, Interaction> interactions;

    @Inject
    public SpriteProvider(@Named("DataDirectoryPath") String dataDirPath, CommandSet defaultCommands,
                          Map<String, BehaviorStyle> behaviors, Map<String, Interaction> interactions) {
        this.dataDirPath = dataDirPath;
        this.defaultCommands = defaultCommands;
        this.behaviors = behaviors;
        this.interactions = interactions;
    }
    /**
     * @return a new instance of a Sprite 
     */
    @Override
    public Sprite get() {
        return new Sprite(dataDirPath,defaultCommands.copy(),behaviors,interactions);
    }
}
