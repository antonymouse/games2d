package com.goldberg.games2d.data;

import com.goldberg.games2d.exceptions.LevelBuildingException;
import com.goldberg.games2d.gamelogic.FrogPlantBinaryInteraction;
import com.goldberg.games2d.gamelogic.KeyCommand;
import com.goldberg.games2d.gamelogic.Sprite;
import com.goldberg.games2d.hardware.ImageInfo;
import com.google.inject.Provider;
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
import java.util.concurrent.BlockingQueue;

/**
 * The class represents a level in the game. It's capable of reading the map, validating it, providing map-based
 * information.
 * @author antonymouse
 * @since 0.0
 */
public class Level {
    private final String dataDirPath;
    private int TILE_SIZE_BITS;
    private int TILE_SIZE;
    private int MAP_WIDTH;
    public static final String COMMENT_DESIGNATOR = "==";
    public static final String SPRITE_DESIGNATOR = "sprite";
    private Map<String,Tile> tiles;
    private List<Sprite> mySprites;
    private final Provider<Sprite> spriteProvider;
    private final BlockingQueue<ImageInfo> drawingQueue;
    private Tile[][] map;
    /**
     * Distances between all sprites, recalculated at each step. N**2 algorithm - optimize if needed.
     */
    private final Map<Sprite,int[]> spriteDistances;
    private static final Logger logger = LogManager.getLogger();
    private Sprite player;
    private final FrogPlantBinaryInteraction frog2plant = new FrogPlantBinaryInteraction();

    public Level(Provider<Sprite> spriteProvider, String dataDirPath, 
                 BlockingQueue<ImageInfo> drawingQueue) {
        this.dataDirPath = dataDirPath;
        this.spriteProvider = spriteProvider;
        this.drawingQueue = drawingQueue;
        spriteDistances = new HashMap<>();
    }
    /**
     * Reads the map from the file
     */
    public void read(String levelMapFile){
        try {
            List<String> allLines = Files.readAllLines(FileSystems.getDefault().getPath(dataDirPath+levelMapFile));
            tiles = readTiles(allLines);
            mySprites = readSprites(allLines);
            // now that we know how many of them we got...
            mySprites.forEach(sprite -> spriteDistances.put(sprite, new int[mySprites.size()]));    
            // all tiles within a level must be the same size
            TILE_SIZE = tiles.entrySet().iterator().next().getValue().TILE_SIZE;
            TILE_SIZE_BITS = (int)(Math.log(TILE_SIZE) / Math.log(2));
            map = readMap(allLines);
            MAP_WIDTH = tilesToPixels(map[0].length);
            logger.debug("read map of size {} {} y,x",map.length,map[0].length);
        }catch (IOException ioe){
            throw new LevelBuildingException("Unable to read the level's file:"+levelMapFile,ioe);
        }
    }

    /**
     * Locates (if exists) a tile of the given type in the given direction within the given distance from the
     * starting point. Operates in absolute (map) coordinates
     * @param startingPoint where to start at, pixels
     * @param direction where to go
     * @param tileType what to look for
     * @param distance in tiles
     * @return the coordinates of the target tile (in pixels) or null if not found
     */
    public Coordinates findTile(@NotNull Coordinates startingPoint, @NotNull KeyCommand direction,
                                @NotNull String tileType, float distance){
        int cellsToGo = (int) Math.ceil(distance);
        logger.debug("looking for {} cells in {} direction",cellsToGo,direction.getName());
        int x = pixelsToTiles(startingPoint.getX());
        int y = pixelsToTiles(startingPoint.getY());
        for(int step = 1;step <= cellsToGo; step++){
            x+=direction.getxCellIncrement();
            y+=direction.getyCellIncrement();
            if(x<0 || x>=map[0].length || y>=map.length || y<0) break;
            if(map[y][x].getSymbol().compareTo(tileType)==0){
                // target location: middle of the cell
                logger.debug("found target for tile {},{}, x,y {},{} and direction {}, current at x,y {},{} tile {},{}",
                        x,y,tilesToPixels(x),tilesToPixels(y),direction.getName(),startingPoint.getX(),startingPoint.getY(),
                        pixelsToTiles(startingPoint.getX()),pixelsToTiles(startingPoint.getY()));
                return new Coordinates(tilesToPixels(x),tilesToPixels(y));
            }
        }
        //couldn't find the cell of the given type
        logger.debug("unable to find target for tile {},{} and direction {}, current at x,y {},{} tile {},{}", 
                x,y,direction.getName(),startingPoint.getX(),startingPoint.getY(),
                pixelsToTiles(startingPoint.getX()),pixelsToTiles(startingPoint.getY()));
        return null;
    }

    /**
     * Sends the message to each of the sprites on this level. See {@link Sprite}
     * @param message some keyboard event
     * @param currentTime current game time
     * @see Sprite#processMessage(int[], long, Level) Sprite's process message
     */
    public void processMessage(int[] message, long currentTime){
        if(mySprites!=null && !mySprites.isEmpty()){
            mySprites.forEach(sprite -> sprite.processMessage(message,currentTime,this));
            calculateSpriteDistances(currentTime);
        }
    }

    private void calculateSpriteDistances(long currentTime) {
        // N**2 algo, optimize if needed
        for (int i = 0; i < mySprites.size(); i++)  {
            Sprite sprite =  mySprites.get(i);
            int[] currentSpriteDistances = spriteDistances.get(sprite); 
            for (int j = i+1; j < mySprites.size(); j++) {
                Sprite sprite1 =  mySprites.get(j);
                currentSpriteDistances[j] = pixelsToTiles((int)Math.ceil(sprite.calcDistance(sprite1)));
                if( currentSpriteDistances[j]<= frog2plant.getInteractionDistance() && sprite!=sprite1 && 
                        (sprite.isOfType(frog2plant.getInteractingTypes()[0]) || sprite.isOfType(frog2plant.getInteractingTypes()[1])) &&
                        (sprite1.isOfType(frog2plant.getInteractingTypes()[0]) || sprite1.isOfType(frog2plant.getInteractingTypes()[1])))
                {
                    frog2plant.interact(sprite,sprite1,currentTime);
                }
            }
            
        }
    }

    /**
     * Passes the invocation to each of the sprites on this level, See {@link Sprite#processGameTick(long)}
     * @param currentTime current game time
     */
    public void processGameTick(long currentTime){
        if(mySprites!=null && !mySprites.isEmpty()){
            mySprites.forEach(sprite -> sprite.processGameTick(currentTime));
            calculateSpriteDistances(currentTime);
        }
    }
    private Tile[][] readMap(List<String> allLines) {
        ArrayList<Tile[]> levelHorizontals = allLines.stream().filter(line -> !(Tile.isTileLine(line) || isCommentLine(line) ||
                isSpriteLine(line))).map(line->{
            Tile[] currentHorizontal = new Tile[line.length()];
            for(int i=0; i<line.length();i++){
                currentHorizontal[i]=tiles.get(line.substring(i,i+1));
            }
            return currentHorizontal;
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        return levelHorizontals.toArray(new Tile[levelHorizontals.size()][levelHorizontals.get(0).length]);
    }

    private Map<String, Tile> readTiles(List<String> allLines) {
        Map<String,Tile> newTiles = new HashMap<>();
        allLines.stream().filter(Tile::isTileLine).forEach(line ->{
            Tile tile = new Tile(line);
            newTiles.put(tile.getSymbol(),tile);
        });
        if(newTiles.isEmpty()){
            throw new LevelBuildingException("No tiles discovered, can't form a level.");
        }
        return newTiles;
    }

    private List<Sprite> readSprites(List<String> allLines){
        List<Sprite> sprites = new ArrayList<>();
        allLines.stream().filter(Level::isSpriteLine).forEach(line ->
                {
                    Sprite currentSprite = spriteProvider.get().configureFromFile(line.substring(line.indexOf(":") + 1));
                    sprites.add(currentSprite);
                    if (currentSprite.isOfType("PLAYER")) {
                        player = currentSprite;
                    }
                }
        );
        return sprites;
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

        /*
         * The following block sets the viewport (visible part of the level) around the player. The player is placed
         * in the middle of the viewport, if possible.
         */
        int leftSideOfTheViewport = player.getState().getCurrent().getX() - TILE_SIZE - screenWidth/2;
        leftSideOfTheViewport = Math.max(leftSideOfTheViewport, 0);
        leftSideOfTheViewport = Math.min(leftSideOfTheViewport,MAP_WIDTH-screenWidth); //so we don't go over right side
        int offsetY = 0;
        int firstTileX = pixelsToTiles(leftSideOfTheViewport);
        int lastTileX = firstTileX + pixelsToTiles(screenWidth);

        for (int y = 0; y < map.length; y++) {
            for (int x = firstTileX; x <= lastTileX; x++) {
                if (x < 0 || x >= map[0].length) continue;
                map[y][x].draw(g, tilesToPixels(x) - leftSideOfTheViewport, tilesToPixels(y) + offsetY);
            }
        }
        while(drawingQueue.peek()!=null){
            ImageInfo currentImage = null;
            try {
                currentImage = drawingQueue.take();
            } catch (InterruptedException e) {
                logger.error("Retrieving an image from non-empty drawing queue was interrupted",e);
            }
            g.drawImage(currentImage.getImage(),currentImage.getAbsoluteX()-leftSideOfTheViewport,currentImage.getAbsoluteY(),null);
        }
    }
    /**
     *
     * @param line string to check
     * @return true if the line is a comment
     */
    private static boolean isCommentLine(String line){
        return line!=null && line.startsWith(COMMENT_DESIGNATOR);
    }
    private static boolean isSpriteLine(String line){
        return line!=null && line.startsWith(SPRITE_DESIGNATOR);
    }


}
