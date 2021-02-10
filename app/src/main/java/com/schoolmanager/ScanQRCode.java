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
import android.widget.Button;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.UserSessionManager;

import java.util.HashMap;
import java.util.List;

import static com.schoolmanager.common.Common.AT_SCHOOL;
import static com.schoolmanager.common.Common.DRIVER_DROPPED;
import static com.schoolmanager.common.Common.HOME_TO_SCHOOL;
import static com.schoolmanager.common.Common.SCHOOL_TO_HOME;

public class ScanQRCode extends BaseActivity {

    private static final String TAG = "scan_qr_code_activity";

    private ConstraintLayout driverLayout, subAdminLayout;
    private Button dHomeToSchool, dSchoolToHome, sHomeToSchool, sSchoolToHome;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_q_r_code);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        detector = new ConnectionDetector(this);
        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        fcmToken = sessionManager.getFcmToken();

        driverLayout = findViewById(R.id.clDriverScanner);
        subAdminLayout = findViewById(R.id.clSubAdminScanner);
        dHomeToSchool = findViewById(R.id.btnDriverHomeToSchool);
        dSchoolToHome = findViewById(R.id.btnDriverSchoolToHome);
        sHomeToSchool = findViewById(R.id.btnSubAdminHomeToSchool);
        sSchoolToHome = findViewById(R.id.btnSubAdminSchoolToHome);

        if(userType.equals("3")){
            driverLayout.setVisibility(View.VISIBLE);
            subAdminLayout.setVisibility(View.GONE);
        }else{
            driverLayout.setVisibility(View.GONE);
            subAdminLayout.setVisibility(View.VISIBLE);
        }

        dHomeToSchool.setOnClickListener(v -> {
            openScanner(HOME_TO_SCHOOL);
        });
        dSchoolToHome.setOnClickListener(v -> {
            openScanner(DRIVER_DROPPED);
        });
        sHomeToSchool.setOnClickListener(v -> {
            openScanner(AT_SCHOOL);
        });
        sSchoolToHome.setOnClickListener(v -> {
            openScanner(SCHOOL_TO_HOME);
        });

    }

    private void openScanner(int scanCode){
        Dexter.withContext(getApplicationContext())
                .withPermissions(Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if(multiplePermissionsReport.areAllPermissionsGranted()){
                            Intent nextIntent = new Intent(ScanQRCode.this, QRScanner.class);
                            nextIntent.putExtra("scan_code", scanCode);
                            startActivity(nextIntent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
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