package com.exride.scroller;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Background {

    private Bitmap image;
    private int x, y, dx;

    public Background (Bitmap res) {

        image = res;

        // Get speed value
        dx = GamePanel.MOVESPEED;
    }

    public void update() {

        // Move background by X coordinate
        x += dx;

        // If BG is out of the screen do reset it
        if (x < -GamePanel.WIDTH) {
            x=0;
        }
    }

    public void draw(Canvas canvas) {

        // Draw BG on coordinates
        canvas.drawBitmap(image, x, y, null);

        // When image ends draw another after that
        if (x < 0) {
            canvas.drawBitmap(image, x+GamePanel.WIDTH, y, null);
        }
    }
}

