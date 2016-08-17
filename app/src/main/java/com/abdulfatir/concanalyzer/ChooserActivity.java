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

public class ChooserActivity extends AppCompatActivity {

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
        // Handle item selection
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
        settingsDialog.setTitle("Settings");
        settingsDialog.show();
    }

    private boolean isFirstRun() {
        boolean firstRun = prefs.getBoolean("firstRun", true);
        if (firstRun)
            prefs.edit().putBoolean("firstRun", false).apply();
        return firstRun;
    }

    private boolean isDemoOn() {
        return prefs.getBoolean("demoMode", true);
    }

    private void setDemoMode(boolean mode) {
        prefs.edit().putBoolean("demoMode", mode).apply();
    }

    public void loadImage(View view) {

        Intent analyzer = new Intent(this, AnalyzerActivity.class);
        analyzer.putExtra(Consts.CHOICE_KEY, Consts.CHOOSE_FROM_LIBRARY);
        startActivity(analyzer);
    }

    public void takePicture(View view) {

        boolean displayInstructions = !(prefs.getBoolean("dontShow", false));
        if (displayInstructions) {
            final Dialog instructionDialog = new Dialog(this);
            instructionDialog.setContentView(R.layout.dialog_instructions);
            instructionDialog.setTitle("Instructions");
            final CheckBox dontShow = (CheckBox) instructionDialog.findViewById(R.id.checkBox);
            Button OK = (Button) instructionDialog.findViewById(R.id.button4);

            OK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dontShow.isChecked()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("dontShow", true);
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
