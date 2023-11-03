package com.goldberg.games2d.gamelogic;

import org.jetbrains.annotations.NotNull;

/**
 * Factory making instances of {@link Command}. Can be made more complex if needed
 * @author antonymouse
 * @since 0.0
 */
public class CommandFactory {
    /**
     * A command is defined as a name = x;y;[key] (i.e. the key is optional) If it is present,
     * @param name the name of the command
     * @param value the mapping string
     * @return the {@link Command} or its child class instance
     */
    public static Command processCommandDescription(@NotNull String name, @NotNull String value) {
        String [] values = value.split(";");
        int incrementX = Integer.parseInt(values[0]);
        int incrementY = Integer.parseInt(values[1]);
        boolean interruptable = false;
        Command currentCommand;
        if(values.length>2 && values[2]!=null && !values[2].isEmpty()){
            int code = Integer.parseInt(values[2]);
            currentCommand = new KeyCommand(incrementX,incrementY,name,code);
        }else {
            if (values.length > 3 && values[3] != null && !values[3].isEmpty()) {
                interruptable = Boolean.parseBoolean(values[3]);
            }
            currentCommand = new Command(incrementX, incrementY, name, interruptable);
        }
        return currentCommand;
    }

}
