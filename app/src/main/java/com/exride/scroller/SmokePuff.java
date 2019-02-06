package com.exride.scroller;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class SmokePuff extends GameObject {

    // Radius of smoke
    public int r;

    public SmokePuff(int x, int y) {
        r = 15;
        super.x = x;
        super.y = y;
    }

    // Smoke speed
    public void update() {
        x -= 80;
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.FILL);

        // Draw smoke animation with 3 different circles
        canvas.drawCircle(x-r, y-r, r, paint);
        canvas.drawCircle(x-r-30, y-r-5, r+8, paint);
        canvas.drawCircle(x-r-70, y-r+10, r+15, paint);
    }
}