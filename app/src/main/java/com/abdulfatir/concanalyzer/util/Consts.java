package com.abdulfatir.concanalyzer.util;

import android.Manifest;

/**
 * Created by Abdul Fatir on 7/3/2016.
 */
public class Consts {
    /**
     * The constant CHOOSE_FROM_LIBRARY.
     */
    public static final int CHOOSE_FROM_LIBRARY = 0;
    /**
     * The constant TAKE_PICTURE.
     */
    public static final int TAKE_PICTURE = 1;
    /**
     * The constant CHOICE_KEY.
     */
    public static final String CHOICE_KEY = "CHOICE";
    /**
     * The constant STORAGE_PERMISSIONS.
     */
    public static final String STORAGE_PERMISSIONS[] = {"android.permission.READ_EXTERNAL_STORAGE",
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * The constant PREFS_NAME.
     */
    public static final String PREFS_NAME = "com.abdulfatir.ChooserActivity";
}
