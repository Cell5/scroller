package com.exride.scroller;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

//// Created by Pavel Isupov 01.02.2019 ////

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<SmokePuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private Explosion explosion;
    private Random rand = new Random();
    private SoundPool sounds;   // Init Sound pool
    private boolean reset;
    private boolean disappear;
    private boolean started;
    private int best;
    private int maxBorderHeight;
    private int minBorderHeight;
    private int sndExplosion;   // SFX for explosion impact
    private long startReset;
    private long smokeStartTime;
    private long missileStartTime;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    // Increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;

    // Set dimensions of the screen according to background image resolution
    public static int WIDTH;
    public static int HEIGHT;
    public static final int MOVESPEED = -5;


    // Get screen dimensions
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public GamePanel(Context context) {
        super(context);

        // add callback to the surface folder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        // make game panel focusable for handling events
        setFocusable(true);

        // SFX controller
        sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        sndExplosion = sounds.load(context, R.raw.explosion, 1); // Explosion SFX
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // Set BG on each iteration
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.background));

        // Set player object
        if (getScreenWidth() < 1600 || getScreenHeight() < 800) {
            player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.tank_khaki),
                    80, 60, 7);
        } else {
            player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.tank_khaki),
                    168, 126, 7);
        }

        // Smoke surface, missiles and borders
        smoke = new ArrayList<SmokePuff>();
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        // Get BG real dimensions
        Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        WIDTH = bg.getWidth();
        HEIGHT = bg.getHeight();

        thread = new MainThread(getHolder(), this);
        // start the game loop
        thread.setRunning(true);
        thread.start();
    }

    // Control Player by touch
    @Override
    public boolean onTouchEvent(MotionEvent e) {

        // Get touch Y
        int y = (int)e.getY();
        int playerY = player.y + 130;

        // Player action when touched
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            // If player is not playing
            if (!player.getPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
            }

            if (player.getPlaying()) {
                if (!started) started = true;
                reset = false;
                if (y < playerY) {
                    player.setUp(true);
                }
                if (y > playerY){
                    player.setDown(true);
                }
            }
            return true;
        }

        // Player action when touch released
        if (e.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            player.setDown(false);
            return true;
        }
        return true;

//        return super.onTouchEvent(e);
    }

    public void update() {

        // When player is playing calling update the player and BG
        if (player.getPlaying()) {
            if(botborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }

            if(topborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();


            // Calculate the threshold of height the border can have based on the score
            // max and min border heart are updated, and the border switched direction when either max or
            // min is met

            maxBorderHeight = 30 + player.getScore()/progressDenom;
            //cap max border height so that borders can only take up a total of 1/2 the screen
            if(maxBorderHeight > HEIGHT / 4)maxBorderHeight = HEIGHT / 4;
            minBorderHeight = 5 + player.getScore()/progressDenom;

            //check bottom border collision
            for(int i = 0; i < botborder.size(); i++) {
                if(collision(botborder.get(i), player))
                    player.setPlaying(false);
            }

            //check top border collision
            for(int i = 0; i < topborder.size(); i++) {
                if(collision(topborder.get(i), player))
                    player.setPlaying(false);
            }

            //update top border
            this.updateTopBorder();

            //update bottom border
            this.updateBottomBorder();

            // Add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore()/4)) {

                // First missile always goes down the middle
                if (missiles.size() == 0) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile),WIDTH + 10, HEIGHT/2, 120, 40, player.getScore(), 13));
                } else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))
                            + maxBorderHeight),120,40, player.getScore(),13));
                }

                // Reset timer
                missileStartTime = System.nanoTime();
            }
            // Loop through every missile and check collision and remove
            for (int i = 0; i < missiles.size(); i++)
            {
                // Update missile
                missiles.get(i).update();

                if(collision(missiles.get(i),player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                // Remove missile if it is way off the screen
                if(missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;
                }
            }

            // Make some smoke on timer
            long elapsed = (System.nanoTime() - smokeStartTime/1000000);
            if (elapsed > 120) {
                smoke.add(new SmokePuff(player.getX() + 70, player.getY() + 70));
                smokeStartTime = System.nanoTime();
            }

            // Manage smoke puffs updates
            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < 80) {
                    smoke.remove(i);
                }
            }
        } else {
            player.resetDY();
            if(!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),
                        player.getX()-50,player.getY()-100, 262, 262, 25);
            }

            explosion.update();
            long resetElapsed = (System.nanoTime()-startReset)/1000000;
            if(resetElapsed > 2500 && !newGameCreated) {
                newGame();
            }
        }
    }

    // For collision
    public boolean collision(GameObject a, GameObject b) {
        if(Rect.intersects(a.getRectangle(), b.getRectangle())) {
            // SFX for explosion
            if (WelcomeScreen.sfxOn == 'y') {
                sounds.play(sndExplosion, 0.3f, 0.3f, 0, 0, 1.0f);
            }
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // Scale factor depends on screen dimensions
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        // Scale BG to fit screen resolution
        if(canvas != null) {

            // Save canvas state before scaling
            final int savedState = canvas.save();

            // Scale BG
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);

            // Draw player if not disappear
            if(!disappear) {
                player.draw(canvas);
            }

            // Draw smoke
            for (SmokePuff sp: smoke) {
                sp.draw(canvas);
            }

            // Draw missile
            for(Missile m: missiles) {
                m.draw(canvas);
            }

            // Draw top border
            for(TopBorder tb: topborder) {
                tb.draw(canvas);
            }

            // Draw bottom border
            for(BotBorder bb: botborder) {
                bb.draw(canvas);
            }

            // Draw explosion
            if(started) {
                explosion.draw(canvas);
            }

            drawText(canvas);

            // Return BG to saved state
            canvas.restoreToCount(savedState);
        }
    }

    public void updateTopBorder()
    {
        //every 50 points, insert randomly placed top blocks that break the pattern
        if(player.getScore()%50 == 0) {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
            ),topborder.get(topborder.size()-1).getX()+20,0,(int)((rand.nextDouble()*(maxBorderHeight
            ))+1)));
        }

        for(int i = 0; i < topborder.size(); i++) {
            topborder.get(i).update();
            if(topborder.get(i).getX()<-20) {
                topborder.remove(i);
                //remove element of arraylist, replace it by adding a new one

                //calculate topdown which determines the direction the border is moving (up or down)
                if(topborder.get(topborder.size()-1).getHeight() >= maxBorderHeight) {
                    topDown = false;
                }

                if(topborder.get(topborder.size()-1).getHeight() <= minBorderHeight) {
                    topDown = true;
                }

                //new border added will have larger height
                if(topDown) {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()+1));
                }
                //new border added wil have smaller height
                else {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void updateBottomBorder() {
        //every 40 points, insert randomly placed bottom blocks that break pattern
        if(player.getScore()%40 == 0) {
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size() - 1).getX() + 20, (int)((rand.nextDouble()
                    * maxBorderHeight)+(HEIGHT - maxBorderHeight))));
        }

        //update bottom border
        for(int i = 0; i < botborder.size(); i++) {
            botborder.get(i).update();

            //if border is moving off screen, remove it and add a corresponding new one
            if(botborder.get(i).getX() < -20) {
                botborder.remove(i);

                //determine if border will be moving up or down
                if (botborder.get(botborder.size() - 1).getY() <= HEIGHT - maxBorderHeight) {
                    botDown = true;
                }
                if (botborder.get(botborder.size() - 1).getY() >= HEIGHT - minBorderHeight) {
                    botDown = false;
                }

                if (botDown) {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() + 1));
                } else {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() - 1));
                }
            }
        }
    }

    public void newGame() {
        disappear = false;

        botborder.clear();
        topborder.clear();

        missiles.clear();
        smoke.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);

        if(player.getScore()>best) {
            best = player.getScore();
        }

        //create initial borders

        //initial top border
        for(int i = 0; i*20 < WIDTH + 40; i++) {
            //first top border create
            if(i == 0) {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, 10));
            } else {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, topborder.get(i-1).getHeight()+1));
            }
        }
        //initial bottom border
        for(int i = 0; i*20 < WIDTH+40; i++) {
            //first border ever created
            if(i == 0) {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick)
                        ,i*20,HEIGHT - minBorderHeight));
            }
            //adding borders until the initial screen is filed
            else {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, botborder.get(i - 1).getY() - 1));
            }
        }
        newGameCreated = true;
    }

    public void drawText(Canvas canvas) {

        int txtbig = 60;
        int txtsm = 30;

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setTextSize(txtbig);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(getResources().getString(R.string.distance) + ": " + (player.getScore()*3), 10, HEIGHT - 10, paint);
        canvas.drawText(getResources().getString(R.string.best) + ": " + best, WIDTH - 350, HEIGHT - 10, paint);

        if(!player.getPlaying() && newGameCreated && reset) {
            paint.setColor(Color.BLACK);
            Paint paint1 = new Paint();
            paint1.setTextSize(txtbig);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(getResources().getString(R.string.press_to_start) + " !", WIDTH/2-50, HEIGHT/2, paint1);

            paint1.setTextSize(txtsm);
            canvas.drawText(getResources().getString(R.string.slide_up), WIDTH/2 - 50, HEIGHT/2 + 20 + txtsm, paint1);
            canvas.drawText(getResources().getString(R.string.slide_down), WIDTH/2 - 50, HEIGHT/2 + txtsm*3, paint1);
        }
    }
}
