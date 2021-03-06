package com.abdulfatir.concanalyzer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;

/**
 * Created by Abdul Fatir on 7/26/2016.
 */
public class ConcAnalyzer extends Application {
    private static final String KEY_APP_CRASHED = "ConcAnalyzerCrashed";
    private static final String TAG = "com.abdulfatir";
    private String trace;

    public void onCreate() {


        super.onCreate();
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });

        boolean bRestartAfterCrash = getSharedPreferences(TAG, Context.MODE_PRIVATE)
                .getBoolean(KEY_APP_CRASHED, false);
        if (bRestartAfterCrash) {
            getSharedPreferences(TAG, Context.MODE_PRIVATE).edit()
                    .putBoolean(KEY_APP_CRASHED, false).apply();
            Intent intent = new Intent(this, ChooserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * Handle uncaught exception.
     *
     * @param thread the thread
     * @param e      the e
     */
    public void handleUncaughtException(Thread thread, Throwable e) {


        trace = Log.getStackTraceString(e);
        Intent intent = new Intent();
        intent.putExtra("StackTrace", trace);
        intent.setAction("com.abdulfatir.concanalyzer.SEND_LOG");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


}
