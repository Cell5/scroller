package com.exride.scroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.exride.scroller.multilanguage.LocaleHelper;

public class Game extends Activity {

    MediaPlayer bgmusic;
    private int lastbgchecked = WelcomeScreen.bgchecked;

    // Attach PaperDB
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (lastbgchecked == 'y') {
            // Add background music
            bgmusic = MediaPlayer.create(Game.this, R.raw.backgroundmusic);
            bgmusic.setLooping(true);
            bgmusic.start();
        }

        setContentView(new GamePanel(this));
    }

    @Override
    protected void onPause() {
        if (lastbgchecked == 'y') {
            // Pause the music
            bgmusic.pause();
        }
        super.onPause();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // Return the game back to Welcome screen
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), WelcomeScreen.class);
        startActivity(intent);
    }
}
