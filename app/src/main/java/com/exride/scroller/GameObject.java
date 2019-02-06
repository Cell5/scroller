package com.exride.scroller;

import android.graphics.Rect;

// For game object to check collisions

public abstract class GameObject {
    protected int x;
    protected int y;
    protected int dy;
    protected int dx;
    protected int width;
    protected int height;

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    // Object hitbox in the game
    public Rect getRectangle() {
        return new Rect(x, y, x+width, y+height);
    }
}
