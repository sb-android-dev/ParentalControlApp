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
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.schoolmanager.common.Common;
import com.schoolmanager.common.LogOutUser;
import com.schoolmanager.dialogs.ChildArrivedDialog;
import com.schoolmanager.dialogs.ChildNotArrivedDialog;
import com.schoolmanager.model.ScanItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.DBHandler;
import com.schoolmanager.utilities.GpsUtils;
import com.schoolmanager.utilities.SpeakerBox;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.schoolmanager.common.Common.APP_CODE;
import static com.schoolmanager.common.Common.BASE_URL;
import static com.schoolmanager.common.Common.LOG_OUT_SUCCESS;

public class Dashboard extends BaseActivity {

    private static final String TAG = "dashboard_activity";

    private MaterialCardView complaintLayout, driversLayout, studentsLayout, giveComplaintLayout,
            locateChildLayout, teachersLayout, trackingLayout, scanLayout, noticeLayout,
            managementMsgLayout, parentLayout;
    private Button complaints, drivers, students, giveComplaint, locateChild, teachers, tracking, scan,
            notice, managementMsg, parent;
    private ImageView driverStatus;
    private ConstraintLayout arrivedLayout;
//    private MaterialCardView complaintCard, locationCard;
    private WebView webView;
    private TextView userName/*, userTypeName, complaintNo*/;
    private ImageView greetingImage, settings;
    private SwitchMaterial locationSwitch;
    private TextView arrived, notArrived, ok;
    private RelativeLayout notArrivedLayout;
    private ProgressBar progressArrived, progressComplaint;

    private String type;

    private LocationManager locationManager;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userType, userToken, uName, uImage, deviceId, fcmToken;
    private String studentId;
    private int driverId;

    private MediaPlayer mpl;
    private final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    private SpeakerBox speakerBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        if (getIntent().getAction() != null) {
            Log.e(TAG, "onCreate: " + getIntent().getAction());
            if (getIntent().getAction().equals(Common.ACTION_OPEN_TRACKING)) {
                startActivity(new Intent(Dashboard.this, TrackHistory.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }

        speakerBox = new SpeakerBox(getApplication());
        speakerBox.setActivity(this);

        detector = new ConnectionDetector(this);
        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getUserDetails();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        Log.e(TAG, "onCreate: userId -> " + userId);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        Log.e(TAG, "onCreate: userToken -> " + userToken);
        uName = hashMap.get(UserSessionManager.KEY_USER_NAME);
        uImage = hashMap.get(UserSessionManager.KEY_USER_IMAGE);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        Log.e(TAG, "onCreate: userType -> " + userType);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        Log.e(TAG, "onCreate: deviceId -> " + deviceId);
        studentId = hashMap.get(UserSessionManager.KEY_STUDENT_ID);
        driverId = sessionManager.getDriverId();

        fcmToken = sessionManager.getFcmToken();

        mpl = MediaPlayer.create(getApplicationContext(), alarmSound);

        webView = findViewById(R.id.webView);
        userName = findViewById(R.id.tvUserName);
//        userTypeName = findViewById(R.id.tvUserType);
        greetingImage = findViewById(R.id.ivGreetingImage);
        settings = findViewById(R.id.ivSettings);
//        complaintCard = findViewById(R.id.mcvComplaint);
//        complaintNo = findViewById(R.id.tvComplaintNo);
//        locationCard = findViewById(R.id.mcvShareLocation);
        locationSwitch = findViewById(R.id.switchLocation);
        complaintLayout = findViewById(R.id.mcvComplaints);
        complaints = findViewById(R.id.btnComplaints);
        driversLayout = findViewById(R.id.mcvDrivers);
        drivers = findViewById(R.id.btnDrivers);
        studentsLayout = findViewById(R.id.mcvStudents);
        students = findViewById(R.id.btnStudents);
        giveComplaintLayout = findViewById(R.id.mcvGiveComplaint);
        giveComplaint = findViewById(R.id.btnGiveComplaint);
        locateChildLayout = findViewById(R.id.mcvLocateChild);
        locateChild = findViewById(R.id.btnLocateChild);
        driverStatus = findViewById(R.id.ivDriverStatus);
        teachersLayout = findViewById(R.id.mcvTeacherList);
        teachers = findViewById(R.id.btnTeacherList);
        trackingLayout = findViewById(R.id.mcvTrackingHistory);
        tracking = findViewById(R.id.btnTrackingHistory);
        scanLayout = findViewById(R.id.mcvScanCode);
        scan = findViewById(R.id.btnScanCode);
        noticeLayout = findViewById(R.id.mcvNoticeBoard);
        notice = findViewById(R.id.btnNoticeBoard);
        managementMsgLayout = findViewById(R.id.mcvManagementMsg);
        managementMsg = findViewById(R.id.btnManagementMsg);
        parentLayout = findViewById(R.id.mcvParent);
        parent = findViewById(R.id.btnParent);
        arrivedLayout = findViewById(R.id.clArrive);
        arrived = findViewById(R.id.btnArrived);
        progressArrived = findViewById(R.id.progressArrived);
        notArrivedLayout = findViewById(R.id.rlNotArrived);
        notArrived = findViewById(R.id.btnNotArrived);
        progressComplaint = findViewById(R.id.progressNotArrived);
        ok = findViewById(R.id.btnOk);

        initializeWebView();

        greetingUser();
//        String uType = getIntent().getStringExtra("type");
        switch (userType) {
            case "5":
                locationSwitch.setVisibility(View.GONE);
//                locationCard.setVisibility(View.GONE);
//                complaintCard.setVisibility(View.GONE);
                complaintLayout.setVisibility(View.GONE);
                driversLayout.setVisibility(View.GONE);
                studentsLayout.setVisibility(View.GONE);
                giveComplaintLayout.setVisibility(View.GONE);
                locateChildLayout.setVisibility(View.GONE);
                teachersLayout.setVisibility(View.GONE);
                trackingLayout.setVisibility(View.GONE);
                noticeLayout.setVisibility(View.GONE);
                managementMsgLayout.setVisibility(View.GONE);
                arrivedLayout.setVisibility(View.GONE);
                parentLayout.setVisibility(View.GONE);
                break;
            case "4":
                locationSwitch.setVisibility(View.GONE);
//                complaintCard.setVisibility(View.GONE);
                complaintLayout.setVisibility(View.GONE);
                driversLayout.setVisibility(View.GONE);
                studentsLayout.setVisibility(View.GONE);
                giveComplaintLayout.setVisibility(View.GONE);
                locateChildLayout.setVisibility(View.GONE);
                teachersLayout.setVisibility(View.GONE);
                trackingLayout.setVisibility(View.GONE);
                scanLayout.setVisibility(View.GONE);
                noticeLayout.setVisibility(View.GONE);
                managementMsgLayout.setVisibility(View.GONE);
                arrivedLayout.setVisibility(View.GONE);
                parentLayout.setVisibility(View.GONE);
                break;
            // For Drivers
            case "3":
//                complaintCard.setVisibility(View.GONE);
                complaintLayout.setVisibility(View.GONE);
                driversLayout.setVisibility(View.GONE);
                studentsLayout.setVisibility(View.GONE);
                giveComplaintLayout.setVisibility(View.GONE);
                locateChildLayout.setVisibility(View.GONE);
                teachersLayout.setVisibility(View.GONE);
                trackingLayout.setVisibility(View.GONE);
//                noticeLayout.setVisibility(View.GONE);
                managementMsgLayout.setVisibility(View.GONE);
                arrivedLayout.setVisibility(View.GONE);
                parentLayout.setVisibility(View.VISIBLE);

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
//                complaintCard.setVisibility(View.GONE);
                complaintLayout.setVisibility(View.GONE);
                driversLayout.setVisibility(View.GONE);
                studentsLayout.setVisibility(View.GONE);
                scanLayout.setVisibility(View.GONE);
                arrivedLayout.setVisibility(View.GONE);
                parentLayout.setVisibility(View.GONE);

                if(driverId == 0){
                    locateChildLayout.setVisibility(View.GONE);
                } else {
                    locateChildLayout.setVisibility(View.VISIBLE);
                }

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

//                locationSwitch.setText(R.string.see_live_locations);
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (TrackingService.isTracking) {
                    locationSwitch.setChecked(true);
                }
                break;

            // For Teachers or default
            case "2":
            default:
                locationSwitch.setVisibility(View.GONE);
                giveComplaintLayout.setVisibility(View.GONE);
                locateChildLayout.setVisibility(View.GONE);
                teachersLayout.setVisibility(View.GONE);
                trackingLayout.setVisibility(View.GONE);
                scanLayout.setVisibility(View.GONE);
                managementMsgLayout.setVisibility(View.GONE);
                arrivedLayout.setVisibility(View.GONE);
                parentLayout.setVisibility(View.GONE);
        }

        if(getIntent().hasExtra("notification_for_arrive")
                && getIntent().getBooleanExtra("notification_for_arrive", false)){
            arrivedLayout.setVisibility(View.VISIBLE);
        }

//        if(MyApplication.mp != null && MyApplication.mp.isPlaying())
//            arrivedLayout.setVisibility(View.VISIBLE);

        fetchGeneralData();
        if(userType.equals("1"))
            getDriverStatus();

        settings.setOnClickListener(v -> {
            startActivity(new Intent(this, Settings.class));
        });

//        complaintCard.setOnClickListener(v -> {
//            // Open chat list of parents that have messaged for teacher.
//            startActivity(new Intent(Dashboard.this, ComplainList.class));
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//        });
        complaints.setOnClickListener(v -> {
            /**
             * Jay
             */
            startActivity(new Intent(Dashboard.this, ComplainList.class));//TeachersList
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        drivers.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, DriversList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        students.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, ClassList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        giveComplaint.setOnClickListener(v -> {
            /**
             * Jay
             */
            startActivity(new Intent(Dashboard.this, ComplainList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        locateChild.setOnClickListener(v -> {
            HashMap<String, String> driverDetail = sessionManager.getDriverDetails();
            if (!driverDetail.get(UserSessionManager.KEY_DRIVER_ID).equals("0")) {
                Intent nextIntent = new Intent(Dashboard.this, LocateOnMap.class);
                nextIntent.putExtra("driver_name", driverDetail.get(UserSessionManager.KEY_DRIVER_NAME));
                nextIntent.putExtra("driver_phone", driverDetail.get(UserSessionManager.KEY_DRIVER_PHONE));
                nextIntent.putExtra("driver_id", Integer.parseInt(driverDetail.get(UserSessionManager.KEY_DRIVER_ID)));
                nextIntent.putExtra("driver_image", driverDetail.get(UserSessionManager.KEY_DRIVER_IMAGE));
                startActivity(nextIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
//            startActivity(new Intent(Dashboard.this, DriversList.class));
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        teachers.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, TeachersList.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        scan.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, ScanQRCode.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        tracking.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, TrackHistory.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        notice.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, NoticeBoard.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        managementMsg.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, BroadCastMessage.class));
        });
        arrived.setOnClickListener(v -> {
            stopAlertSound();
            childArrived();
        });
        notArrived.setOnClickListener(v -> {
            stopAlertSound();
            childNotArrived();
        });
        ok.setOnClickListener(v -> {
            stopAlertSound();
        });

        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
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
        });

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Dashboard.this, StudentPlaceScreen.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        navigateToChatBoardFromNotification(getIntent());
        navigateToBroadcastFromNotification(getIntent());
    }

    private void initializeWebView() {
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void childArrived() {
        progressArrived.setVisibility(View.VISIBLE);
        arrived.setVisibility(View.INVISIBLE);
        AndroidNetworking.post(Common.BASE_URL + "app-save-student-arrival")
                .addBodyParameter("user_app_code", Common.APP_CODE) /* App code for app */
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
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
                                if (mpl.isPlaying()) {
                                    mpl.stop();
                                    mpl.release();
                                    mpl = null;
                                    mpl = MediaPlayer.create(getApplicationContext(), alarmSound);
                                }
                                mpl.start();
                                ChildArrivedDialog dialogA = new ChildArrivedDialog();
                                dialogA.show(getSupportFragmentManager(), ChildArrivedDialog.TAG);
                                arrivedLayout.setVisibility(View.GONE);
                                sessionManager.registerComplaint(false);
                            } else if (success == 2) {
                                onLogOut();
                            } else {
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
                .addBodyParameter("user_type", userType)
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
                                if (mpl.isPlaying()) {
                                    mpl.stop();
                                    mpl.release();
                                    mpl = null;
                                    mpl = MediaPlayer.create(getApplicationContext(), alarmSound);
                                }
                                mpl.start();
                                ChildNotArrivedDialog dialogA = new ChildNotArrivedDialog();
                                dialogA.show(getSupportFragmentManager(), ChildNotArrivedDialog.TAG);
//                                sessionManager.updateNotificationStatus(false);
//                                sessionManager.registerComplaint(true);
//                                notArrivedLayout.setVisibility(View.GONE);
                                progressComplaint.setVisibility(View.INVISIBLE);
                                notArrived.setVisibility(View.VISIBLE);
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

    private void fetchGeneralData() {
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
//                                complaintNo.setText(String.valueOf(todays_complaints));

                                if (data.has("is_arrived")) {
                                    boolean isArrived = data.getInt("is_arrived") == 1;
                                    if (userType.equals("1")) {
//                                        if (!isArrived) {
//                                            arrivedLayout.setVisibility(View.VISIBLE);
//                                        } else {
//                                            arrivedLayout.setVisibility(View.GONE);
//                                        }

//                                        if (sessionManager.getIsComplaintRegistered()) {
//                                            notArrivedLayout.setVisibility(View.GONE);
//                                        }
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

    private void uploadStoredResult() {
        DBHandler db = new DBHandler(this);

        ScanItem scanItem = db.getScanItem();
        if (scanItem != null)
            uploadResults(db, scanItem);

    }

    public void uploadResults(DBHandler db, ScanItem scanItem) {
        if (detector.isConnectingToInternet()) {
            AndroidNetworking
                    .post(BASE_URL + "app-track-student")
                    .addBodyParameter("user_id", scanItem.getUserId())
                    .addBodyParameter("user_token", scanItem.getUserToken())
                    .addBodyParameter("user_type", scanItem.getUserType())
                    .addBodyParameter("user_app_code", APP_CODE)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("device_type", "1")
                    .addBodyParameter("student_id", scanItem.getStudentId())
                    .addBodyParameter("track_status", scanItem.getTrackStatus())
                    .addBodyParameter("track_time", scanItem.getTrackTime())
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");
                                if (success == 1) {
                                    db.deleteScan(scanItem.getScanId());
                                    uploadStoredResult();
                                } else if (success == 2) {
                                    onLogOut();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse -> " + e.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError -> " + anError.getLocalizedMessage());
                        }
                    });
        }
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

    private void getDriverStatus() {
        if (!detector.isConnectingToInternet()) {
            return;
        }

        AndroidNetworking.post(Common.BASE_URL + "app-driver-location-status")
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("driver_id", String.valueOf(sessionManager.getDriverId()))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int success = response.getInt("success");
                            if (success == 1) {
                                int status = response.getJSONObject("data").getInt("status");
                                if(status == 1){
                                    Glide.with(Dashboard.this)
                                            .load(Uri.parse("file:///android_asset/driver_location_enabled_rounded.gif"))
                                            .fitCenter()
                                            .into(driverStatus);
                                } else {
                                    Glide.with(Dashboard.this)
                                            .load(R.drawable.driver_location_disabled_rounded)
                                            .fitCenter()
                                            .into(driverStatus);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.e(TAG, "onCreate: " + intent.getAction());
        if (intent.getAction().equals(Common.ACTION_OPEN_TRACKING)) {
            startActivity(new Intent(Dashboard.this, TrackHistory.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
//        else if(intent.getAction().equals(Common.ACTION_OPEN_LOCATE_ON_MAP)) {
//            Intent i = new Intent(Dashboard.this, LocateOnMap.class);
//            Intent nextIntent = new Intent(Dashboard.this, LocateOnMap.class);
//            nextIntent.putExtra("driver_name", driverDetail.get(UserSessionManager.KEY_DRIVER_NAME));
//            nextIntent.putExtra("driver_phone", driverDetail.get(UserSessionManager.KEY_DRIVER_PHONE));
//            nextIntent.putExtra("driver_id", Integer.parseInt(driverDetail.get(UserSessionManager.KEY_DRIVER_ID)));
//            nextIntent.putExtra("driver_image", driverDetail.get(UserSessionManager.KEY_DRIVER_IMAGE));
//            startActivity(i);
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//        }

        if(intent.hasExtra("notification_for_arrive")
                && intent.getBooleanExtra("notification_for_arrive", false)){
            arrivedLayout.setVisibility(View.VISIBLE);
        }

        if(sessionManager.isAlertNotifying())
            arrivedLayout.setVisibility(View.VISIBLE);

        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationSwitch.setChecked(true);
            }
        }

        fetchGeneralData();

        navigateToChatBoardFromNotification(intent);
        navigateToBroadcastFromNotification(intent);

    }

    public void stopAlertSound(){
        if(MyApplication.mp != null && MyApplication.mp.isPlaying()){
            MyApplication.mp.stop();
            MyApplication.mp.release();
            MyApplication.mp = null;
        }
        sessionManager.notifyForAlert(false);
    }

    public void onLogOut() {
        LogOutUser.getInstance(this, status -> {
            if(status == LOG_OUT_SUCCESS){
                if (TrackingService.isTracking) {
                    sendCommandToService(Common.ACTION_STOP_SERVICE);
                }
                sessionManager.logoutUser();
                Intent i = new Intent(this, LogIn.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        }).performLogOut();

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

        uImage = sessionManager.getUserImage();
//        Glide.with(this).load(uImage)
//                .placeholder(R.drawable.ic_avatar)
//                .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(getResources().getDimensionPixelSize(R.dimen.image_corner_radius))))
//                .into(userImage);

        if (detector.isConnectingToInternet())
            uploadStoredResult();

        if(userType.equals("1")){
            driverId = sessionManager.getDriverId();
            if(driverId == 0)
                locateChildLayout.setVisibility(View.GONE);
            else
                locateChildLayout.setVisibility(View.VISIBLE);
        }

        if(sessionManager.isAlertNotifying())
            arrivedLayout.setVisibility(View.VISIBLE);
    }

    private void greetingUser(){
        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay < 12){
            userName.setText(String.format("%s, %s",  "Good Morning", uName));
            Glide.with(this).load(R.drawable.good_morning_greeting).into(greetingImage);
            webView.loadUrl("https://my.famous.co/morning/");
        } else if(timeOfDay < 16){
            userName.setText(String.format("%s, %s",  "Good Afternoon", uName));
            Glide.with(this).load(R.drawable.good_afternoon_greeting).into(greetingImage);
            webView.loadUrl("https://my.famous.co/noon/");
        } else if(timeOfDay < 20){
            userName.setText(String.format("%s, %s",  "Good Evening", uName));
            Glide.with(this).load(R.drawable.good_evening_greeting).into(greetingImage);
            webView.loadUrl("https://my.famous.co/evening/");
        } else {
            userName.setText(String.format("%s, %s",  "Good Night", uName));
            Glide.with(this).load(R.drawable.good_night_greeting).into(greetingImage);
            webView.loadUrl("https://my.famous.co/night/");
        }

        speakerBox.play(userName.getText().toString());

//        webView.loadData("<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head><body><img src=\"" + mStringUrl + "\"></body></html>", "text/html", "utf-8");
    }

    @Override
    public void finish() {
        super.finish();
        stopAlertSound();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void navigateToChatBoardFromNotification(Intent intent) {
        if (intent.getExtras() != null) {
            if (intent.hasExtra("redirect_to_chat")) {
                startActivity(new Intent(Dashboard.this, ChatBoardActivity.class)
                        .putExtra("complaint_data", intent.getStringExtra("redirect_to_chat")));
            }
        }
    }

    private void navigateToBroadcastFromNotification(Intent intent) {
        if (intent.getExtras() != null) {
            if (intent.hasExtra("redirect_to_broadcast")) {
                startActivity(new Intent(Dashboard.this, BroadCastMessage.class));
            }
        }
    }
}