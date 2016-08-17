package com.abdulfatir.concanalyzer;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.InputStreamReader;

public class SendCrashReportActivity extends AppCompatActivity {

    private String trace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trace = getIntent().getStringExtra("StackTrace");
        setContentView(R.layout.activity_send_crash_report);
    }

    private String extractLog() {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        InputStreamReader reader = null;
        String output = "";

        // write output stream
        output += ("Android version: " + Build.VERSION.SDK_INT + "\n");
        output += ("Device: " + model + "\n");
        output += ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");
        output += trace;


        //Log.d(getClass().getName(),output);
        return output;
    }

    public void exitApp(View view) {
        Intent intent = new Intent(this, ChooserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);

    }


    public void sendLog(View view) {
        Intent fback = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        fback.putExtra(Intent.EXTRA_EMAIL, new String[]{"abdulfatirs@gmail.com"});
        fback.putExtra(Intent.EXTRA_SUBJECT, "App Feedback");
        fback.putExtra(Intent.EXTRA_TEXT, extractLog());
        startActivity(Intent.createChooser(fback, "Choose e-mail client ..."));
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}