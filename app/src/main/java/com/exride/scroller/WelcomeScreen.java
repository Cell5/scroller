package com.exride.scroller;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.exride.scroller.multilanguage.LocaleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class WelcomeScreen extends AppCompatActivity {

    // Add views those need to translate
    TextView textView;
    TextView textView2;

    Button btn1;
    Button btn2;
    Spinner spinner;
    Switch switch1;
    Switch switch2;
    String currentLang;
    MediaPlayer bgmusic;

    // Sound options
    public static char bgchecked;
    public static char sfxOn;         // y = turn on; n = turn off

    // Attach PaperDB context
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Call the required screen view
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_screen);

        // Define all views those need to update
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);

        // Init PaperDB first
        Paper.init(this);

        // Default language is English
        String language = Paper.book().read("language");
        if(language == null)
            Paper.book().write("language","en");
        updateView((String)Paper.book().read("language"));

        // Call button method
        launchBtn();
        exitBtn();

        // Create welcome_background music
        bgmusic = MediaPlayer.create(this, R.raw.welcome);
        bgmusic.setLooping(true);
        bgmusic.start();

        // Define language change by spinner
        spinner = findViewById(R.id.spinner);
        currentLang = Locale.getDefault().getLanguage();

        List<String> list = new ArrayList<String>();

        list.add("Select language");
        list.add(getString(R.string.language_en));
        list.add(getString(R.string.language_zh));
        list.add(getString(R.string.language_ru));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                switch (position) {
                    case 0:
                        break;
                    case 1:
                        Paper.book().write("language", "en");
                        updateView((String)Paper.book().read("language"));
                        refreshIntent();
                        break;
                    case 2:
                        Paper.book().write("language", "zh");
                        updateView((String)Paper.book().read("language"));
                        refreshIntent();
                        break;
                    case 3:
                        Paper.book().write("language", "ru");
                        updateView((String)Paper.book().read("language"));
                        refreshIntent();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    protected void onPause() {
        bgmusic.release();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        bgmusic.start();
        super.onPostResume();
    }

    @Override
    protected void onStop() {
        bgmusic.release();
        finish();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        bgmusic.release();
        finish();
    }

    // Define btn1 Launch button method
    public void launchBtn() {
        Switch switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    // Switcher is checked
                    bgchecked = 'y';
                    // Start playing welcome_background music on Welcome screen immediately
                    bgmusic.start();
                    Toast.makeText(getApplicationContext(), getString(R.string.option_turn_bg_music_off) + ": " + getString(R.string.text_on), Toast.LENGTH_SHORT).show();
                } else {
                    // Switcher is unchecked
                    bgchecked = 'n';
                    // Pause playing welcome_background music on Welcome screen immediately
                    bgmusic.pause();
                    Toast.makeText(getApplicationContext(), getString(R.string.option_turn_bg_music_off) + ": " + getString(R.string.text_off), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Switch switch2 = findViewById(R.id.switch2);
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    sfxOn = 'y';
                    Toast.makeText(getApplicationContext(), getString(R.string.option_turn_sfx_off) + ": " + getString(R.string.text_on), Toast.LENGTH_SHORT).show();
                } else {
                    sfxOn = 'n';
                    Toast.makeText(getApplicationContext(), getString(R.string.option_turn_sfx_off) + ": " + getString(R.string.text_off), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Game.class);
                startActivityForResult(intent, 0);
            }
        });

        // Reset Music and SFX settings
        bgchecked = 'y';
        sfxOn = 'y';
    }

    // Define btn2 Exit button method
    public void exitBtn () {
        btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                // Switch on welcome_background music on exit button click
                bgchecked = 'y';
                bgmusic.release();
                finish();
            }
        });
    }

    // Update text strings implementation
    private void updateView(String lang) {
        Context context = LocaleHelper.setLocale(this, lang);
        Resources resources = context.getResources();

        // Update views
        textView.setText(resources.getString(R.string.options_headline));
        textView2.setText(resources.getString(R.string.game_title));
        btn1.setText(resources.getString(R.string.button_launchgame));
        btn2.setText(resources.getString(R.string.button_exit));
        switch1.setText(resources.getString(R.string.option_turn_bg_music_off));
        switch2.setText(resources.getString(R.string.option_turn_sfx_off));

    }

    private void refreshIntent(){
        // Check if selected language is not the same with the system
        if (currentLang != Locale.getDefault().getLanguage()) {
            Intent refresh = new Intent(this, WelcomeScreen.class);
            startActivity(refresh);
        } else {
            Toast.makeText(WelcomeScreen.this, R.string.language_already_selected, Toast.LENGTH_SHORT).show();
        }
    }
}


