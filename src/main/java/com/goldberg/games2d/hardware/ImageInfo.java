package com.goldberg.games2d.hardware;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Used to pass image information for the drawing
 * @author antonymouse
 * @since 0.2
 */
public class ImageInfo {
    private Image image;
    private int absoluteX, absoluteY;

    public Image getImage() {
        return image;
    }

    public int getAbsoluteX() {
        return absoluteX;
    }

    public int getAbsoluteY() {
        return absoluteY;
    }

    /**
     * Stores the image for drawing
     * @param image the image
     * @param absoluteX x in absolute (map) coordinates
     * @param absoluteY y in absolute (map) coordinates
     */
    public ImageInfo(@NotNull Image image, int absoluteX, int absoluteY) {
        this.image = image;
        this.absoluteX = absoluteX;
        this.absoluteY = absoluteY;
    }
}
