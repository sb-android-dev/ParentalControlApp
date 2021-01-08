package com.schoolmanager;

import android.app.Application;
import android.media.MediaPlayer;

import com.androidnetworking.AndroidNetworking;

public class MyApplication extends Application {

    public static MediaPlayer mp;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());
    }
}
