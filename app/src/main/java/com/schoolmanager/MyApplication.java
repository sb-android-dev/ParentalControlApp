package com.schoolmanager;

import android.media.MediaPlayer;

import com.androidnetworking.AndroidNetworking;
import com.zeugmasolutions.localehelper.LocaleAwareApplication;

public class MyApplication extends LocaleAwareApplication {

    public static MediaPlayer mp;
    public static MediaPlayer mpCall;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());


    }
}
