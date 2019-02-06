package com.exride.scroller;

import android.graphics.Bitmap;
import android.graphics.Canvas;


public class Player extends GameObject {
    private Bitmap spritesheet;
    private int score;
    private boolean up;
    private boolean down;
    private boolean playing;
    private Animation animation = new Animation();
    private long startTime;

    public Player(Bitmap res, int w, int h, int numFrames) {

        x = 200;    // Player start point X
        y = 500;    // Player start point Y
        dy = 0;
        score = 0;
        height = h;
        width = w;

        // Bitmap array for storing sprites
        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, i*width, 0, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(10);
        startTime = System.nanoTime();

    }

    // Motion event for object
    public void setUp(boolean b) {
        up = b;
    }

    public void setDown(boolean b) {
        down = b;
    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime)/1000000;
        if(elapsed > 100) {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

        // Move up
        if (up) {
            // Initial agility
            dy = -2;
            dy -= 1;
        }
        // Stop moving
        else {
            resetDY();
        }

        // Move down
        if (down) {
            // Initial agility
            dy = 2;
            dy += 1;
        }

        // Tank acceleration
        y += dy*2;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    public int getScore() {
        return score;
    }

    public boolean getPlaying() {
        return playing;
    }

    public void setPlaying(boolean b) {
        playing = b;
    }

    public void resetDY() {
        dy = 0;
    }

    public void resetScore() {
        score = 0;
    }
}