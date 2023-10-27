package com.goldberg.games2d.data;

import com.goldberg.games2d.gamelogic.Sprite;

public class Coordinates {
    int x, y;

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
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
}
