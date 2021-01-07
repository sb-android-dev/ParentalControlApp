package com.schoolmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.schoolmanager.utilities.UserSessionManager;

import java.util.HashMap;
import java.util.UUID;

public class Splash extends AppCompatActivity {

    private static final String TAG = "splash_activity";

    private UserSessionManager sessionManager;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);

        assert deviceId != null;
        if(deviceId.isEmpty()){
            String dId = UUID.randomUUID().toString();
            sessionManager.upsertDeviceId(dId);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(Splash.this, LogIn.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }, 2000);
    }
}