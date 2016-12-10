package com.abdulfatir.concanalyzer;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.abdulfatir.concanalyzer.models.DefaultsModel;
import com.abdulfatir.concanalyzer.models.SamplesListViewAdapter;
import com.abdulfatir.concanalyzer.util.Consts;

import java.util.ArrayList;

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
        Button apply = (Button) settingsDialog.findViewById(R.id.apply);
        final RadioGroup mode = (RadioGroup) settingsDialog.findViewById(R.id.modeChooser);
        switch (getMode())
        {
            case Consts.DEMO_MODE:
                mode.check(R.id.demo);
                break;
            case Consts.AUTO_MODE:
                mode.check(R.id.auto);
                break;
            case Consts.MANUAL_MODE:
                mode.check(R.id.manual);
                break;
        }
        final Spinner cardType = (Spinner) settingsDialog.findViewById(R.id.cardChooser);
        cardType.setSelection(getCardType());
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCardType(cardType.getSelectedItemPosition());
                int chosenMode = -1;
                switch (mode.getCheckedRadioButtonId())
                {
                    case R.id.demo:
                        chosenMode = Consts.DEMO_MODE;
                        break;
                    case R.id.auto:
                        chosenMode = Consts.AUTO_MODE;
                        break;
                    case R.id.manual:
                        chosenMode = Consts.MANUAL_MODE;
                        break;
                }
                setMode(chosenMode);
                settingsDialog.dismiss();
                if(chosenMode == Consts.AUTO_MODE)
                    showSetDefaultValuesDialog(cardType.getSelectedItemPosition());
            }
        });
        settingsDialog.setTitle(R.string.settingsText);
        settingsDialog.show();
    }

    private void showSetDefaultValuesDialog(int cardType) {
        final Dialog setDefsDialog = new Dialog(this);
        setDefsDialog.setContentView(R.layout.dialog_defaults);
        final ListView samplesListView = (ListView) setDefsDialog.findViewById(R.id.samples_list_view);
        int length = 0;
        switch (cardType)
        {
            case Consts.SIX_SAMPLE_CARD:
                length = 6;
                break;
            case Consts.FIVE_SAMPLE_CARD:
                length = 5;
                break;
        }
        final ArrayList<DefaultsModel> data = new ArrayList<>();
        for(int i=0;i<length;i++)
            data.add(new DefaultsModel(i+1, 0, DefaultsModel.SampleType.KNOWN));
        SamplesListViewAdapter adapter = new SamplesListViewAdapter(this, data);
        samplesListView.setAdapter(adapter);
        Button saveButton = (Button) setDefsDialog.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String defVals = "";
                String qcIndices = "";
                for(int i=0;i<data.size();i++)
                {
                    View row = samplesListView.getChildAt(i);
                    defVals += ((EditText)row.findViewById(R.id.conc)).getText().toString()+" ";
                    if(((Spinner)row.findViewById(R.id.sampleType)).getSelectedItemPosition() == 1)
                        qcIndices += i;
                }
                Log.d("debug", defVals);
                prefs.edit().putString(Consts.DEFAULT_VALUES_KEY, defVals.trim()).commit();
                prefs.edit().putString(Consts.QC_INDICES_KEY, qcIndices).commit();
                Toast.makeText(ChooserActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                setDefsDialog.dismiss();
            }
        });
        setDefsDialog.setTitle("Set Defaults");
        setDefsDialog.setCancelable(false);
        setDefsDialog.show();
    }

    private void setMode(int chosenMode) {
        prefs.edit().putInt(Consts.MODE_KEY, chosenMode).commit();
    }

    private void setCardType(int type) {
        prefs.edit().putInt(Consts.CARD_TYPE_KEY, type).commit();

    }

    private boolean isFirstRun() {
        boolean firstRun = prefs.getBoolean(FIRST_RUN_KEY, true);
        if (firstRun)
            prefs.edit().putBoolean(FIRST_RUN_KEY, false).apply();
        return firstRun;
    }

    private int getMode()
    {
        return prefs.getInt(Consts.MODE_KEY, 0);
    }

    private int getCardType()
    {
        return prefs.getInt(Consts.CARD_TYPE_KEY, 0);
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
