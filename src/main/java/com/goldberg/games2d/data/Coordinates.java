package com.goldberg.games2d.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Coordinates {
    int x, y;
    protected static final Logger logger = LogManager.getLogger();


    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Coordinates copy(){
        return new Coordinates(getX(),getY());
    }

    public void assign(Coordinates to) {
        // just to avoid too much garbage collection if some rarely touched objects graduate
        this.x = to.x;
        this.y = to.y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Calculates the distance between 2 points
     * @param x1 x of the first point
     * @param y1 y of the first point
     * @param x2 x of the second point
     * @param y2 y of the second point
     * @return the distance
     */
    public static double distance(int x1, int y1, int x2, int y2){
        return Math.sqrt(Math.pow(Math.abs(x1 - x2), 2) +
                Math.pow(Math.abs(y1 - y2), 2));
    }

    /**
     * Calculates the intercept point of entity 2 to entity 1. Entity 1 is moving down a straight line.
     * Entity 2 is located in some point and waiting.
     * @param x1s starting x coordinate of entity 1
     * @param y1s starting y coordinate of entity 1
     * @param x1e ending x coordinate of entity 1
     * @param y1e ending y coordinate of entity 1
     * @param v1 velocity of entity 1
     * @param x2s starting x coordinate of entity 2
     * @param y2s starting y coordinate of entity 2
     * @param v2 velocity of entity 2
     * @return the intercept coordinates or null if there is no intercept
     */
    public static Coordinates intersection(int x1s, int y1s, int x1e, int y1e, double v1, int x2s, int y2s, double v2){
        // Calculate the slope of the line connecting S(x1s,y1s) and E(x1e,y1e) using the formula: 
        // m = (yae - yas) / (xae - xas)
        double m = ((double) (y1e - y1s))/((double) (x1e - x1s));
        // Calculate the y-intercept of the line using the formula: b = y1s - m * x1s
        double b = (double)y1s - m * x1s;
        // Calculate the distance between player 2’s starting position and the line using the formula: 
        // d = |m * xbs - ybs + b| / sqrt(m^2 + 1)
        double d = Math.abs(m*x2s - y2s + b)/Math.sqrt(m*m + 1);
        //Calculate the time it would take for player B to reach the intercept point using the formula: t = d / Vb
        double t = d/v2;
        // calculate the time it would take for player 1 to reach the 
        //Calculate the coordinates of the intercept point using the formula: x = xas + Va * t * cos(arctan(m)) and y = yas + Va * t * sin(arctan(m))
        double xi = (double) x1s + v1*t*Math.cos(Math.atan(m));
        double yi = (double) y1s + v1*t*Math.sin(Math.atan(m));
        if(distance(x1s,y1s,x1e,y1e)<distance(x1s,y1s, (int) xi, (int) yi)){
            // the first entity reaches the goal sooner than the intercept
            return null;
        }
        return new Coordinates((int) xi, (int) yi);
    }

    /**
     * Calculates the intercept point of entity 2 to entity 1. Entity 1 is moving down a straight line.
     * Entity 2 is located in some point and waiting.
     * alias of {@link #intersection(int, int, int, int, double, int, int, double)}
     * @param start1 the origin of entity 1
     * @param finish1 the target coordinates of entity 1
     * @param v1 velocity of entity 1
     * @param start2 the origin of entity 2
     * @param v2 velocity of entity 2
     * @return the intercept coordinates or null if there is no intercept
     */
    public static Coordinates intersection(@NotNull Coordinates start1, Coordinates finish1, double v1, 
                                           @NotNull Coordinates start2, double v2){
        logger.debug("target is: {}", finish1);
        if(finish1 != null)
            return intersection(start1.getX(),start1.getY(), finish1.getX(), finish1.getY(), v1, start2.getX(), start2.getY(), v2);
        else 
            return start1;
    }

    /**
     * Calculates a new cartesian coordinate value, given initial and end values and time the move started and time 
     * the move it supposed to end, given constant velocity. Calculation is done using current time value
     * @param startValue where the move starts
     * @param endValue where the move ends
     * @param startTime time the move starts
     * @param endTime where the move ends
     * @param currentTime current time
     * @return a value between startValue and endValue if the time is less than endTime, endValue after that
     */
    public static int coordinateChange(int startValue, int endValue, long startTime, long endTime, long currentTime){
        return (currentTime>=endTime)?endValue:(int) (startValue + (float)(endValue - startValue)*
                (float)(currentTime-startTime)/(float)(endTime-startTime));
    }

    @Override
    public String toString(){
        return "["+x+","+y+"]";
    }
}
