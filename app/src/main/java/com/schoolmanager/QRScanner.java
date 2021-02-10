package com.schoolmanager;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.zxing.Result;
import com.schoolmanager.common.Common;
import com.schoolmanager.model.ScanItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.DBHandler;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.schoolmanager.common.Common.APP_CODE;
import static com.schoolmanager.common.Common.BASE_URL;

public class QRScanner extends BaseActivity {

    private static final String TAG = "qr_scanner_activity";

    private CodeScanner mCodeScanner;

    private int status;

    private MediaPlayer mp;
    private final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    private ConnectionDetector detector;
    private DBHandler dbHandler;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scanner);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        detector = new ConnectionDetector(this);
        dbHandler = new DBHandler(this);
        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);

        CodeScannerView scannerView = findViewById(R.id.scanner_view);

        if(getIntent() != null){
            status = getIntent().getIntExtra("scan_code", 0);
        }

        mp = MediaPlayer.create(getApplicationContext(), alarmSound);

        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setScanMode(ScanMode.SINGLE);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                QRScanner.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "run: " + result.getTimestamp());
                        Log.e(TAG, "run: " + result.getText());
                        if(mp.isPlaying()){
                            mp.stop();
                            mp.release();
                            mp = MediaPlayer.create(getApplicationContext(), alarmSound);
                        }
                        mp.start();

                        uploadOrStoreResult(result.getText(), result.getTimestamp()/1000);

//                        UploadScanResult dialogU = new UploadScanResult();
//                        dialogU.setCancelable(false);
//                        Bundle bundle = new Bundle();
//                        bundle.putString("student_id", result.getText());
//                        bundle.putString("status", String.valueOf(status));
//                        bundle.putString("scan_time", String.valueOf(result.getTimestamp()/1000));
//                        dialogU.setArguments(bundle);
//                        dialogU.show(getSupportFragmentManager(), UploadScanResult.TAG);
                    }
                });
            }
        });

//        scannerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Dexter.withContext(QRScanner.this)
//                        .withPermission(Manifest.permission.CAMERA)
//                        .withListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
//                        mCodeScanner.startPreview();
//                    }
//
//                    @Override
//                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
//                        Toast.makeText(QRScanner.this, "Please give camera permission from settings to scan QR code.", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
//                        permissionToken.continuePermissionRequest();
//                    }
//                }).check();
//            }
//        });
    }

    public void startPreview(){
        mCodeScanner.startPreview();
    }

    private void uploadOrStoreResult(String studentId, long scanTime){
        if (!detector.isConnectingToInternet()) {
            ScanItem scanItem = new ScanItem();
            scanItem.setUserId(userId);
            scanItem.setUserToken(userToken);
            scanItem.setUserType(userType);
            scanItem.setStudentId(studentId);
            scanItem.setTrackStatus(String.valueOf(status));
            scanItem.setTrackTime(String.valueOf(scanTime));
            dbHandler.addScanItem(scanItem);
            mCodeScanner.startPreview();
        } else {
            AndroidNetworking
                    .post(BASE_URL + "app-track-student")
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("user_app_code", APP_CODE)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("device_type", "1")
                    .addBodyParameter("student_id", studentId)
                    .addBodyParameter("track_status", String.valueOf(status))
                    .addBodyParameter("track_time", String.valueOf(scanTime))
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");
                                Toast.makeText(QRScanner.this, message, Toast.LENGTH_SHORT).show();
                                if (success == 1) {
                                    mCodeScanner.startPreview();
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    ScanItem scanItem = new ScanItem();
                                    scanItem.setUserId(userId);
                                    scanItem.setUserToken(userToken);
                                    scanItem.setUserType(userType);
                                    scanItem.setStudentId(studentId);
                                    scanItem.setTrackStatus(String.valueOf(status));
                                    scanItem.setTrackTime(String.valueOf(scanTime));
                                    dbHandler.addScanItem(scanItem);
                                    mCodeScanner.startPreview();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse -> " + e.getLocalizedMessage());
                                ScanItem scanItem = new ScanItem();
                                scanItem.setUserId(userId);
                                scanItem.setUserToken(userToken);
                                scanItem.setUserType(userType);
                                scanItem.setStudentId(studentId);
                                scanItem.setTrackStatus(String.valueOf(status));
                                scanItem.setTrackTime(String.valueOf(scanTime));
                                dbHandler.addScanItem(scanItem);
                                mCodeScanner.startPreview();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError -> " + anError.getLocalizedMessage());
                            ScanItem scanItem = new ScanItem();
                            scanItem.setUserId(userId);
                            scanItem.setUserToken(userToken);
                            scanItem.setUserType(userType);
                            scanItem.setStudentId(studentId);
                            scanItem.setTrackStatus(String.valueOf(status));
                            scanItem.setTrackTime(String.valueOf(scanTime));
                            dbHandler.addScanItem(scanItem);
                            mCodeScanner.startPreview();
                        }
                    });
        }
    }

    public void onLogOut() {
        if (TrackingService.isTracking) {
            Intent serviceIntent = new Intent(this, TrackingService.class);
            serviceIntent.setAction(Common.ACTION_STOP_SERVICE);
            startService(serviceIntent);
        }
        new UserSessionManager(this).logoutUser();
        Intent i = new Intent(this, LogIn.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}