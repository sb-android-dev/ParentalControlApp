package com.schoolmanager;

import android.app.Application;
import android.media.MediaPlayer;
import android.net.Uri;

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
