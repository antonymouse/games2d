package com.goldberg.games2d.gamelogic;

import com.goldberg.games2d.exceptions.AnimationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Contains a sequence of images that need to be shown when a moving part makes a move.
 * Has a schedule, indicating how much time of total animation time should be dedicated to each frame.
 * Loads from a property file.
 * durations in the property file are semicolon-separated and must sum to 1.
 * format of frame file name is frameName before '.' + 2-digit number starting with 01 + frameName's extension
 * @author antonymouse
 * @since 0.0
 */
public class Animation {
    private static final String FRAME_NAME_KEY="frame.file";
    private static final String FRAMES_DURATION="frame.duration_sequence";
    private final float[] durationSequence;
    private final Image[] frames;
    private static final Logger logger = LogManager.getLogger();

    /**
     * Parses the descriptor and loads frames
     *
     * @param dataDirPath where all the data files are
     * @param animationDescriptor properties where the information about the animation is stored
     */
    public Animation(String dataDirPath, String animationDescriptor){
        Properties descriptor = new Properties();
        Path animationDescriptorPath = FileSystems.getDefault().getPath(dataDirPath,animationDescriptor);
        try {
            descriptor.load(Files.newInputStream(animationDescriptorPath));
            durationSequence = parseDurationSequence(descriptor.getProperty(FRAMES_DURATION));
            frames = loadImages(dataDirPath+descriptor.getProperty(FRAME_NAME_KEY));
        } catch (Throwable e) {
            throw new AnimationException("Unable to load animation description from "+animationDescriptor,e);
        }
        logger.debug("Loaded animation {} with {} frames",animationDescriptor, frames.length);
    }
    private float[] parseDurationSequence(String sequence){
        String[] durations = sequence.split(";");
        float total = 0;
        float[] ret = new float[durations.length];
        for (int i = 0; i < durations.length; i++) {
            ret[i] = Float.parseFloat(durations[i]);
            total+=ret[i];
        }
        if(total != 1.0f){
            throw new AnimationException("Animation durations don't sum to 1. Total="+total);
        }
        return ret;
    }
    private Image[] loadImages(String frameName) throws IOException {
        /*
         * format of image file name is frameName before '.' + 2-digit number starting with 01 + frameName's extension
         */
        int extensionStart = frameName.lastIndexOf('.');
        String frameNamePart = frameName.substring(0,extensionStart);
        String frameExtensionPart = frameName.substring(extensionStart);
        Image[] ret = new Image[durationSequence.length];
        for (int i = 0; i < ret.length; i++) {
            String frameToLoadName = String.format("%1$s%2$02d%3$s",frameNamePart,i,frameExtensionPart);
            System.out.println("Loading frame from:"+frameToLoadName);
            logger.debug("loading frame {}",frameToLoadName);
            ret[i] = ImageIO.read(new File(frameToLoadName));
        }
        return ret;
    }

    /**
     * Draws an appropriate frame at the given coordinates
     *
     * @param timeAnimationStart    the time this cycle of animation started
     * @param timeAnimationComplete the time this cycle of animation is completing
     * @param currentTime           the current time
     * @param xpos                  visible absolute x
     * @param ypos                  visible absolute y
     * @param g where to draw
     */
    public void draw(long timeAnimationStart, long timeAnimationComplete,long currentTime, int xpos, int ypos, 
                     Graphics2D g){
        //what image are we drawing?
        timeAnimationComplete = timeAnimationComplete - timeAnimationStart;
        currentTime = currentTime - timeAnimationStart;
        float currentPoint = (float)currentTime/(float)timeAnimationComplete;
        float currentDuration = 0;
        Image currentFrame=frames[0];
        for (int i = 0; i < durationSequence.length; i++) {
            currentFrame=frames[i];
            currentDuration+=durationSequence[i];
            if(currentDuration>=currentPoint){
                logger.debug("drawing animation frame {}",i);
                break;
            }
        }
        g.drawImage(currentFrame,xpos,ypos,null);
    }

    /**
     * When the animation is done moving, what's the frame to use.
     * currently uses the last frame
     * @param xpos pixels
     * @param ypos pixels
     * @param g where to draw
     */
    public void drawDefaultFrame(int xpos, int ypos, Graphics2D g){
        g.drawImage(frames[frames.length-1],xpos,ypos,null);
        logger.debug("drawing the default frame");
    }
}
