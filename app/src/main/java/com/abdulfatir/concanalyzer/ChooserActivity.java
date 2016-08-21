package com.abdulfatir.concanalyzer;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.abdulfatir.concanalyzer.util.Consts;

/**
 * Presents the user with a choice of loading a picture from local storage or to take picture.
 */
public class ChooserActivity extends AppCompatActivity {

    private static final String DONT_SHOW_KEY = "dontShow";
    public static final String DEMO_MODE_KEY = "demoMode";
    public static final String FIRST_RUN_KEY = "firstRun";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);
        prefs = getSharedPreferences(Consts.PREFS_NAME, MODE_PRIVATE);
        if (isFirstRun())
            showSettingsDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                showSettingsDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettingsDialog() {
        final Dialog settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_settings);
        final CheckBox demoMode = (CheckBox) settingsDialog.findViewById(R.id.demo);
        Button apply = (Button) settingsDialog.findViewById(R.id.apply);
        if (isDemoOn()) {
            demoMode.setChecked(true);
        }
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDemoMode(demoMode.isChecked());
                settingsDialog.dismiss();
            }
        });
        settingsDialog.setTitle(R.string.settingsText);
        settingsDialog.show();
    }

    private boolean isFirstRun() {
        boolean firstRun = prefs.getBoolean(FIRST_RUN_KEY, true);
        if (firstRun)
            prefs.edit().putBoolean(FIRST_RUN_KEY, false).apply();
        return firstRun;
    }

    private boolean isDemoOn() {
        return prefs.getBoolean(DEMO_MODE_KEY, true);
    }

    private void setDemoMode(boolean mode) {
        prefs.edit().putBoolean(DEMO_MODE_KEY, mode).apply();
    }

    /**
     * Load image.
     *
     * @param view the view
     */
    public void loadImage(View view) {
        Intent analyzer = new Intent(this, AnalyzerActivity.class);
        analyzer.putExtra(Consts.CHOICE_KEY, Consts.CHOOSE_FROM_LIBRARY);
        startActivity(analyzer);
    }

    /**
     * Take picture.
     *
     * @param view the view
     */
    public void takePicture(View view) {
        boolean displayInstructions = !(prefs.getBoolean(DONT_SHOW_KEY, false));
        if (displayInstructions) {
            final Dialog instructionDialog = new Dialog(this);
            instructionDialog.setContentView(R.layout.dialog_instructions);
            instructionDialog.setTitle(R.string.instructionsText);
            final CheckBox dontShow = (CheckBox) instructionDialog.findViewById(R.id.checkBox);
            Button OK = (Button) instructionDialog.findViewById(R.id.button4);
            OK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dontShow.isChecked()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(DONT_SHOW_KEY, true);
                        editor.apply();
                    }
                    instructionDialog.dismiss();
                    startAnalyzeActivity();
                }
            });
            instructionDialog.setCancelable(false);
            instructionDialog.show();
        } else {
            startAnalyzeActivity();
        }
    }

    private void startAnalyzeActivity() {
        Intent analyzer = new Intent(this, AnalyzerActivity.class);
        analyzer.putExtra(Consts.CHOICE_KEY, Consts.TAKE_PICTURE);
        startActivity(analyzer);
    }


}
