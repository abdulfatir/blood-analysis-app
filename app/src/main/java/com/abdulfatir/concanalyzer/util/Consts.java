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

    public static final String MODE_KEY = "mode";

    public static final String CARD_TYPE_KEY = "cardType";

    public static final String DEFAULT_VALUES_KEY = "defaultValues";

    public static final int DEMO_MODE = 0;

    public static final int AUTO_MODE = 1;

    public static final int MANUAL_MODE = 2;

    public static final int SIX_SAMPLE_CARD = 0;

    public static final int FIVE_SAMPLE_CARD = 1;
    public static final String QC_INDICES_KEY = "qcIndices";
}
