package com.abdulfatir.concanalyzer.util;

import android.Manifest;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;

/**
 * Created by Abdul Fatir on 7/3/2016.
 */
public class Consts {
    public static final int CHOOSE_FROM_LIBRARY = 0;
    public static final int TAKE_PICTURE = 1;
    public static final String CHOICE_KEY = "CHOICE";
    public static final String STORAGE_PERMISSIONS[] = {"android.permission.READ_EXTERNAL_STORAGE",
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final String PREFS_NAME = "com.abdulfatir.ChooserActivity";
}
