package com.schoolmanager;

import android.app.Application;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatDelegate;

import com.androidnetworking.AndroidNetworking;
import com.schoolmanager.utilities.UserSessionManager;
import com.zeugmasolutions.localehelper.LocaleAwareApplication;

public class MyApplication extends LocaleAwareApplication {

    public static MediaPlayer mp;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());

        initTheme(new UserSessionManager(this).getTheme());
    }

    public static void initTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
