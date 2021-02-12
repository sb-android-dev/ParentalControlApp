package com.schoolmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.schoolmanager.common.Common;
import com.schoolmanager.dialogs.LogoutDialog;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.UserSessionManager;

import java.util.HashMap;
import java.util.List;

public class Settings extends BaseActivity {

    private static final String TAG = "settings_activity";

    private ConstraintLayout profileLayout, driverLayout, languageLayout, themeLayout, signOutLayout;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userType, userToken, uName, deviceId, studentId, fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        detector = new ConnectionDetector(this);
        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getUserDetails();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        uName = hashMap.get(UserSessionManager.KEY_USER_NAME);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        studentId = hashMap.get(UserSessionManager.KEY_STUDENT_ID);
        fcmToken = sessionManager.getFcmToken();

        profileLayout = findViewById(R.id.clMyProfile);
        driverLayout = findViewById(R.id.clSetDriver);
        languageLayout = findViewById(R.id.clLanguage);
        themeLayout = findViewById(R.id.clAppTheme);
        signOutLayout = findViewById(R.id.clSignOut);

        if(!userType.equals("1")){
            driverLayout.setVisibility(View.GONE);
        }

        profileLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, MyProfile.class));
        });

        driverLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, SelectDriver.class));
        });

        languageLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, SelectLanguage.class));
        });

        themeLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangeAppTheme.class));
        });

        signOutLayout.setOnClickListener(v -> {
            LogoutDialog dialogF = new LogoutDialog();
            dialogF.show(getSupportFragmentManager(), LogoutDialog.TAG);
        });

    }

    void sendCommandToService(String action) {
        Dexter.withContext(this)
                .withPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            Intent serviceIntent = new Intent(Settings.this, TrackingService.class);
                            serviceIntent.setAction(action);
                            serviceIntent.putExtra("name", uName);
                            serviceIntent.putExtra("type", Integer.valueOf(userType));
                            startService(serviceIntent);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public void onLogOut() {
        if (TrackingService.isTracking) {
            sendCommandToService(Common.ACTION_STOP_SERVICE);
        }
        sessionManager.logoutUser();
        Intent i = new Intent(this, LogIn.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}