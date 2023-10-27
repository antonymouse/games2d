package com.goldberg.games2d.data;

import com.goldberg.games2d.exceptions.LevelBuildingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Defines a tile. Can read itself from an image file, has a type in level.
 * @author antonymouse
 * @since 0.0
 */
public class Tile {
    public static final char TILE_DELIMITER = ':';
    /**
     * All tiles within a level must be the same size
     */
    final int TILE_SIZE ;

    /**
     *
     * @param line string to check
     * @return true if the line is a tile definition line
     */
    public static boolean isTileLine(String line){
        return line!=null && line.length()>2 && line.charAt(1)== TILE_DELIMITER;
    }
    private final String symbol;
    private final BufferedImage image;
    private static final Logger logger = LogManager.getLogger(Tile.class);

    /**
     *
     * @return the symbol used in the level's map to position and define this tile.
     */
    public String getSymbol() {
        return symbol;
    }

    public Tile(String tileDefinitionString){
        if(!isTileLine(tileDefinitionString)){
            throw new LevelBuildingException("Tile definition string is improperly formatted:"+tileDefinitionString);
        }
        symbol=tileDefinitionString.substring(0,1);
        try {
            image = ImageIO.read(new File(tileDefinitionString.substring(2)));
            TILE_SIZE = image.getHeight();
            logger.debug("Loaded image from {} size {}",tileDefinitionString.substring(2),TILE_SIZE);
        } catch (IOException e) {
            throw new LevelBuildingException("unable to load image:"+tileDefinitionString.substring(2));
        }
//        image = new ImageIcon(tileDefinitionString.substring(2)).getImage();
//        logger.debug("loaded image {}, width {} height {}",tileDefinitionString.substring(2),
//                image.getWidth(null),image.getHeight(null));
    }

    /**
     * Draws the tile's image using tile's internal knowledge (i.e. if the tile needs to do something special)
     * @param g the graphics to use
     * @param x screen coordinate x to place the tile
     * @param y screen coordinate y to place the tile
     */
    public void draw(@NotNull Graphics2D g, int x, int y){
        g.drawImage(image,x,y,null);
//        System.out.println("Drawing image "+symbol+" at "+x+" "+y);
    }
}
