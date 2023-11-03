package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.exceptions.LevelBuildingException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * All the commands defined in the game, read from a configuration file
 * If created through {@link com.goldberg.games2d.MainModule} Guice injection, comes predefined with 8 movement
 * commands mapped to numerical cursor keys
 * @author antonymouse
 * @since 0.0
 */
public class CommandSet {
    private final Map<String,Command> commands;
    private final Map<Integer,KeyCommand> keyCommands;

    /**
     * Used as a builder method
     * @param command what command to add
     * @return the set being configured
     */
    public CommandSet addCommand(Command command){
        if(commands.put(command.getName(), command)!=null)
            throw new LevelBuildingException("Duplicate command found:"+command.getName());
        else if (command instanceof KeyCommand) {
            keyCommands.put( ((KeyCommand) command).getKey(), (KeyCommand) command);
        }
        return this;
    }
    public CommandSet(){
        commands = new HashMap<>();
        keyCommands = new HashMap<>();
    } 
    
    /**
     * Copy constructor, copies the maps' contents
     * @param currentCommands initial content for commands
     * @param currentKeyCommands initial content for key commands
     */
    private CommandSet(Map<String,Command> currentCommands, Map<Integer, KeyCommand> currentKeyCommands){
        this.commands = new HashMap<>(currentCommands);
        this.keyCommands = new HashMap<>(currentKeyCommands);
    }

    /**
     * @param name what command to look for
     * @return the {@link Command} or null if no command by this name
     */
    public Command byName(@NotNull String name){
        return commands.get(name);
    }
    public KeyCommand valueOfKey(int key){
        return keyCommands.get(key);
    }

    /**
     * Creates a NEW instance with commands COPIED (e.g. changes in the original has no impact on the copy)
     * @return a copy
     */
    public CommandSet copy(){
        return  new CommandSet(this.commands, this.keyCommands);
    }
}
