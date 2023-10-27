package com.goldberg.games2d.data;

import com.goldberg.games2d.exceptions.LevelBuildingException;
import com.goldberg.games2d.gamelogic.PredefinedCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class represents a level in the game. It's capable of reading the map, validating it, providing map-based
 * information.
 * @author antonymouse
 * @since 0.0
 */
public class Level {
    private int TILE_SIZE_BITS;
    public static final String COMMENT_DESIGNATOR = "==";
    private Map<String,Tile> tiles;
    private Tile[][] map;
    private static final Logger logger = LogManager.getLogger();

    public Level() {
    }
    /**
     * Reads the map from the file
     */
    public void read(String levelMapFile){
        try {
            List<String> allLines = Files.readAllLines(FileSystems.getDefault().getPath(levelMapFile));
            tiles = readTiles(allLines);
            // all tiles within a level must be the same size
            TILE_SIZE_BITS = (int)(Math.log(tiles.entrySet().iterator().next().getValue().TILE_SIZE) / Math.log(2));
            map = readMap(allLines);
            logger.debug("read map of size {} {} y,x",map.length,map[0].length);
        }catch (IOException ioe){
            throw new LevelBuildingException("Unable to read the level's file:"+levelMapFile,ioe);
        }

    }

    /**
     * Locates (if exists) a tile of the given type in the given direction within the given distance from the
     * starting point.
     * @param startingPoint where to start at, pixels
     * @param direction where to go
     * @param tileType what to look for
     * @param distance in tiles
     * @return the coordinates of the target tile (in pixels) or null if not found
     */
    public Coordinates findTile(@NotNull Coordinates startingPoint, @NotNull PredefinedCommand direction,
                                @NotNull String tileType, float distance){
        int cellsToGo = (int) Math.ceil(distance);
        logger.debug("looking for {} cells in {} direction",cellsToGo,direction.name());
        int x = pixelsToTiles(startingPoint.getX());
        int y = pixelsToTiles(startingPoint.getY());
        for(int step = 1;step <= cellsToGo; step++){
            x+=direction.getxCellIncrement();
            y+=direction.getyCellIncrement();
            if(x<0 || x>=map[0].length || y>=map.length || y<0) break;
            if(map[y][x].getSymbol().compareTo(tileType)==0){
                // target location: middle of the cell
                logger.debug("found target for tile {},{}, x,y {},{} and direction {}, current at x,y {},{} tile {},{}",
                        x,y,tilesToPixels(x),tilesToPixels(y),direction.name(),startingPoint.getX(),startingPoint.getY(),
                        pixelsToTiles(startingPoint.getX()),pixelsToTiles(startingPoint.getY()));
                return new Coordinates(tilesToPixels(x),tilesToPixels(y));
            }
        }
        //couldn't find the cell of the given type
        logger.debug("unable to find target for tile {},{} and direction {}, current at x,y {},{} tile {},{}", 
                x,y,direction.name(),startingPoint.getX(),startingPoint.getY(),
                pixelsToTiles(startingPoint.getX()),pixelsToTiles(startingPoint.getY()));
        return null;
    }

    private Tile[][] readMap(List<String> allLines) {
        int lineNumber = 0;
        List<Tile[]> levelHorizontals = new ArrayList<>();
        while(Tile.isTileLine(allLines.get(lineNumber)) || isCommentLine(allLines.get(lineNumber))){
            lineNumber++;
        }
        int levelHorizontalTiles = 0;
        for(;lineNumber<allLines.size();lineNumber++){
            String currentLine = allLines.get(lineNumber);
            if(isCommentLine(currentLine)){
                continue;
            }
            if(levelHorizontalTiles == 0){
                //set the level width by the first line, they all must be of the same length anyway
                levelHorizontalTiles = currentLine.length();
            }
            Tile[] currentHorizontal = new Tile[levelHorizontalTiles];
            levelHorizontals.add(currentHorizontal);
            for(int i=0; i<levelHorizontalTiles;i++){
                currentHorizontal[i]=tiles.get(currentLine.substring(i,i+1));
            }
        }
        return levelHorizontals.toArray(new Tile[levelHorizontals.size()][levelHorizontalTiles]);
    }

    private Map<String, Tile> readTiles(List<String> allLines) {
        Map<String,Tile> newTiles = new HashMap<>();
        int lineNumber = 0;
        while(lineNumber < allLines.size() &&
                (Tile.isTileLine(allLines.get(lineNumber)) || isCommentLine(allLines.get(lineNumber)))){
            if(Tile.isTileLine(allLines.get(lineNumber))){
                Tile tile = new Tile(allLines.get(lineNumber));
                newTiles.put(tile.getSymbol(),tile);
            }
            lineNumber++;
        }
        if(newTiles.isEmpty()){
            throw new LevelBuildingException("No tiles discovered, can't form a level.");
        }
        return newTiles;
    }

    /**
     * @return the length of that many tiles in pixels
     */
    private int tilesToPixels(int numTiles) {
        // use of the shift is more elegant and in the spirit of gaming ;) works only if tile size is power of 2.
        return numTiles << TILE_SIZE_BITS;
    }
    /**
     * @return number of tiles in that many pixels
     */
    private int pixelsToTiles(int pixels) {
        // use shifting to get correct values for negative pixels
        return pixels >> TILE_SIZE_BITS;
    }
    /**
     Draws the level, showing only the visible part.
     Screen parameters can change if the game is running in a window.
     */
    public void draw(Graphics2D g, int screenWidth, int screenHeight)
    {
        // otherwise it always paints the window the default color white, which generates blinking
        g.setColor(Color.black);
        g.fillRect(0, 0, screenWidth, screenHeight);
        int mapWidth = tilesToPixels(map[0].length);

        //adjust for the "player" later
        int offsetX = 0;
//        offsetX = Math.max(offsetX, screenWidth - mapWidth);
//        logger.debug("OffsetX is {}",offsetX);
        // get the y offset to draw all sprites and tiles
        int offsetY = 0;
//        int offsetY = screenHeight - tilesToPixels(map.length);
//        logger.debug("offsetY is {}",offsetY);
        // draw the visible tiles
//        int firstTileX = pixelsToTiles(-offsetX);
        int firstTileX = 0;
//        logger.debug("firstTileX is {}",firstTileX);
        int lastTileX = firstTileX + pixelsToTiles(screenWidth) + 1;
//        logger.debug("lastTileX is {}",lastTileX);
        for (int y=0; y<map.length; y++) {
            for (int x=firstTileX; x <= lastTileX; x++) {
//                logger.debug("drawing tile {},{}",x,y);
                if(x<0 || x>=map[0].length || y>=map.length) continue;
                map[y][x].draw(g,tilesToPixels(x)+offsetX,tilesToPixels(y)+offsetY);
            }
        }
    }
    
    /**
     *
     * @param line string to check
     * @return true if the line is a comment
     */
    private boolean isCommentLine(String line){
        return line!=null && line.startsWith(COMMENT_DESIGNATOR);
    }



}
