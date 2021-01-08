package com.schoolmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.schoolmanager.common.Common;
import com.schoolmanager.dialogs.ChildArrivedDialog;
import com.schoolmanager.dialogs.ChildNotArrivedDialog;
import com.schoolmanager.dialogs.LogoutDialog;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.GpsUtils;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private static final String TAG = "dashboard_activity";

    private ConstraintLayout complaintLayout, driversLayout, studentsLayout, giveComplaintLayout,
            locateChildLayout, arrivedLayout;
    private MaterialCardView complaintCard, locationCard;
    private TextView userName, userTypeName, complaintNo;
    private ImageView userImage, logOut;
    private SwitchMaterial locationSwitch;
    private TextView arrived, notArrived;
    private RelativeLayout notArrivedLayout;
    private ProgressBar progressArrived, progressComplaint;

    private String type;

    private LocationManager locationManager;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId,userType, userToken, uType, uName, uImage, deviceId, fcmToken;
    private String studentId;

    private MediaPlayer mp;
    private final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        detector = new ConnectionDetector(this);
        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getUserDetails();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        uName = hashMap.get(UserSessionManager.KEY_USER_NAME);
        uType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        uImage = hashMap.get(UserSessionManager.KEY_USER_IMAGE);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        studentId = hashMap.get(UserSessionManager.KEY_STUDENT_ID);
        fcmToken = sessionManager.getFcmToken();

        mp = MediaPlayer.create(getApplicationContext(), alarmSound);

        userName = findViewById(R.id.tvUsername);
        userTypeName = findViewById(R.id.tvUserType);
        userImage = findViewById(R.id.ivUserProfile);
        logOut = findViewById(R.id.ivLogOut);
        complaintCard = findViewById(R.id.mcvComplaint);
        complaintNo = findViewById(R.id.tvComplaintNo);
        locationCard = findViewById(R.id.mcvShareLocation);
        locationSwitch = findViewById(R.id.switchLocation);
        complaintLayout = findViewById(R.id.clComplaint);
        driversLayout = findViewById(R.id.clDrivers);
        studentsLayout = findViewById(R.id.clStudents);
        giveComplaintLayout = findViewById(R.id.clGiveComplaint);
        locateChildLayout = findViewById(R.id.clLocateChild);
        arrivedLayout = findViewById(R.id.clArrive);
        arrived = findViewById(R.id.btnArrived);
        progressArrived = findViewById(R.id.progressArrived);
        notArrivedLayout = findViewById(R.id.rlNotArrived);
        notArrived = findViewById(R.id.btnNotArrived);
        progressComplaint = findViewById(R.id.progressNotArrived);

        userName.setText(uName);
        Glide.with(this).load(uImage)
                .placeholder(R.drawable.ic_avatar)
                .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(getResources().getDimensionPixelSize(R.dimen.image_corner_radius))))
                .into(userImage);
//        String uType = getIntent().getStringExtra("type");
        switch (uType) {
            // For Drivers
            case "3":
                complaintCard.setVisibility(View.GONE);
                complaintLayout.setVisibility(View.GONE);
                driversLayout.setVisibility(View.GONE);
                studentsLayout.setVisibility(View.GONE);
                giveComplaintLayout.setVisibility(View.GONE);
                locateChildLayout.setVisibility(View.GONE);
                arrivedLayout.setVisibility(View.GONE);

                Dexter.withContext(this)
                        .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                        .withListener(DialogOnAnyDeniedMultiplePermissionsListener.Builder
                                .withContext(this)
                                .withTitle("Location Permission")
                                .withMessage("Location permission needed to track your location")
                                .withButtonText("OK")
                                .withIcon(R.drawable.ic_my_location).build())
                        .check();

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (TrackingService.isTracking) {
                    locationSwitch.setChecked(true);
                }
                break;

            //For Parents
            case "1":
                complaintCard.setVisibility(View.GONE);
                complaintLayout.setVisibility(View.GONE);
                driversLayout.setVisibility(View.GONE);
                studentsLayout.setVisibility(View.GONE);
                arrivedLayout.setVisibility(View.GONE);

                Dexter.withContext(this)
                        .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                        .withListener(DialogOnAnyDeniedMultiplePermissionsListener.Builder
                                .withContext(this)
                                .withTitle("Location Permission")
                                .withMessage("Location permission needed to track your location")
                                .withButtonText("OK")
                                .withIcon(R.drawable.ic_my_location).build())
                        .check();

                locationSwitch.setText("See live locations");
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (TrackingService.isTracking) {
                    locationSwitch.setChecked(true);
                }
                break;

            // For Teachers or default
            case "2":
            default:
                locationCard.setVisibility(View.GONE);
                giveComplaintLayout.setVisibility(View.GONE);
                locateChildLayout.setVisibility(View.GONE);
                arrivedLayout.setVisibility(View.GONE);
        }

        fecthGeneralData();

        logOut.setOnClickListener(v -> {
            LogoutDialog dialogF = new LogoutDialog();
            dialogF.show(getSupportFragmentManager(), LogoutDialog.TAG);
        });

        complaintCard.setOnClickListener(v -> {
            // Open chat list of parents that have messaged for teacher.
            startActivity(new Intent(Dashboard.this, ComplainList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        complaintLayout.setOnClickListener(v -> {
            /**
             * Jay
             */
            startActivity(new Intent(Dashboard.this, ComplainList.class));//TeachersList
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        driversLayout.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, DriversList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        studentsLayout.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, ClassList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        giveComplaintLayout.setOnClickListener(v -> {
            /**
             * Jay
             */
            startActivity(new Intent(Dashboard.this, ComplainList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        locateChildLayout.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, DriversList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        arrived.setOnClickListener(v -> {
            childArrived();
        });
        notArrived.setOnClickListener(v -> {
            childNotArrived();
        });

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    new GpsUtils(Dashboard.this).turnGPSOn(isGPSEnable -> {
                        // turn on GPS
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            locationSwitch.setChecked(true);
                        }
//                isGPS = isGPSEnable;
//                askLocationPermission();
                    });
                    sendCommandToService(Common.ACTION_START_SERVICE);
                } else {
                    sendCommandToService(Common.ACTION_STOP_SERVICE);
                }
            }
        });

    }

    private void childArrived() {
        progressArrived.setVisibility(View.VISIBLE);
        arrived.setVisibility(View.INVISIBLE);
        AndroidNetworking.post(Common.BASE_URL + "app-save-student-arrival")
                .addBodyParameter("user_app_code", Common.APP_CODE) /* App code for app */
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", uType)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("fcm_token", fcmToken)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "onResponse: " + response.toString());
                        try {
                            int success = response.getInt("success");
                            String message = response.getString("message");

                            if (success == 1) {
                                if(mp.isPlaying()){
                                    mp.stop();
                                    mp.release();
                                    mp = MediaPlayer.create(getApplicationContext(), alarmSound);
                                }
                                mp.start();
                                ChildArrivedDialog dialogA = new ChildArrivedDialog();
                                dialogA.show(getSupportFragmentManager(), ChildArrivedDialog.TAG);
                                arrivedLayout.setVisibility(View.GONE);
                                sessionManager.registerComplaint(false);
                            } else if (success == 2) {
                                onLogOut();
                            } else  {
                                Toast.makeText(Dashboard.this, message, Toast.LENGTH_SHORT).show();
                                progressArrived.setVisibility(View.INVISIBLE);
                                arrived.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                            progressArrived.setVisibility(View.INVISIBLE);
                            arrived.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                        Log.e(TAG, "onError: " + anError.getErrorBody());
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        progressArrived.setVisibility(View.INVISIBLE);
                        arrived.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void childNotArrived() {
        progressComplaint.setVisibility(View.VISIBLE);
        notArrived.setVisibility(View.INVISIBLE);
        AndroidNetworking.post(Common.BASE_URL + "app-save-complaint")
                .addBodyParameter("user_app_code", Common.APP_CODE) /* App code for app */
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", uType)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("fcm_token", fcmToken)
                .addBodyParameter("student_id", studentId)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "onResponse: " + response.toString());
                        try {
                            int success = response.getInt("status");
                            String message = response.getString("message");
                            if (success == 1) {
                                if(mp.isPlaying()){
                                    mp.stop();
                                    mp.release();
                                    mp = MediaPlayer.create(getApplicationContext(), alarmSound);
                                }
                                mp.start();
                                ChildNotArrivedDialog dialogA = new ChildNotArrivedDialog();
                                dialogA.show(getSupportFragmentManager(), ChildNotArrivedDialog.TAG);
                                sessionManager.updateNotificationStatus(false);
                                sessionManager.registerComplaint(true);
                                notArrivedLayout.setVisibility(View.GONE);
                            } else if (success == 2) {
                                onLogOut();
                            } else {
                                progressComplaint.setVisibility(View.INVISIBLE);
                                notArrived.setVisibility(View.VISIBLE);
                                Toast.makeText(Dashboard.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                            progressComplaint.setVisibility(View.INVISIBLE);
                            notArrived.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                        Log.e(TAG, "onError: " + anError.getErrorBody());
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        progressComplaint.setVisibility(View.INVISIBLE);
                        notArrived.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void fecthGeneralData() {
        if (!detector.isConnectingToInternet()) {
            return;
        }

        AndroidNetworking.post(Common.BASE_URL + "app-general-data")
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int success = response.getInt("success");
                            if (success == 1) {
                                JSONObject data = response.getJSONObject("data");
                                int todays_complaints = data.getInt("todays_complaints");
                                complaintNo.setText(String.valueOf(todays_complaints));

                                if(data.has("is_arrived")) {
                                    boolean isArrived = data.getInt("is_arrived") == 1;
                                    if (userType.equals("1")) {
                                        if (!isArrived) {
                                            arrivedLayout.setVisibility(View.VISIBLE);
                                        } else {
                                            arrivedLayout.setVisibility(View.GONE);
                                        }

                                        if(sessionManager.getIsComplaintRegistered()){
                                            notArrivedLayout.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            } else if (success == 2) {
                                onLogOut();
                            } else {
                                Log.e(TAG, "general data get failed");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getErrorBody());
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                    }
                });
    }

    void sendCommandToService(String action) {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            Intent serviceIntent = new Intent(Dashboard.this, TrackingService.class);
                            serviceIntent.setAction(action);
                            serviceIntent.putExtra("name", uName);
                            serviceIntent.putExtra("type", Integer.valueOf(uType));
                            startService(serviceIntent);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationSwitch.setChecked(true);
        }
        fecthGeneralData();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check for the integer request code originally supplied to startResolutionForResult().
        if (requestCode == Common.GPS_REQUEST) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    locationSwitch.setChecked(true);
                    // Nothing to do. startLocationupdates() gets called in onResume again.
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    locationSwitch.setChecked(false);
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TrackingService.isTracking) {
            locationSwitch.setChecked(true);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}