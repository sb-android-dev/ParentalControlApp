package com.schoolmanager;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.schoolmanager.common.Common;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.IImageCompressTaskListener;
import com.schoolmanager.utilities.ImageCompressTask;
import com.schoolmanager.utilities.PathFinder;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyProfile extends BaseActivity {

    private static final String TAG = "my_profile_activity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout profileImageLayout;
    private ImageView profileImage;
    private TextInputEditText name, phone, userName, password;
    private SwitchMaterial lastSeen, readUnread, switchReceiveCall;
    private Button update;
    private ProgressBar progressUpdate;

    //Range seekbar
    private LinearLayout relRange;
    private TextView txtRange;
    private SeekBar seekBarRang;

    private Uri selectedImageUri = null;
    private File profileFile = null;

    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    private ImageCompressTask imageCompressTask;
    private IImageCompressTaskListener compressTaskListener;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken, range;
    private boolean isLastSeenEnabled, isReadUnreadMessagesEnabled, isReceiveCall = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
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
        isLastSeenEnabled = sessionManager.getLastSeenFlag();
        isReadUnreadMessagesEnabled = sessionManager.getReadUnreadMessagesFlag();

        swipeRefreshLayout = findViewById(R.id.srlMyProfile);
        profileImageLayout = findViewById(R.id.rlProfile);
        profileImage = findViewById(R.id.ivProfile);
        name = findViewById(R.id.etName);
        phone = findViewById(R.id.etPhone);
        userName = findViewById(R.id.etUserName);
        password = findViewById(R.id.etPassword);
        lastSeen = findViewById(R.id.switchLastSeen);
        readUnread = findViewById(R.id.switchReadUnreadMsg);
        switchReceiveCall = findViewById(R.id.switchReceiveCall);
        relRange = findViewById(R.id.relRange);
        txtRange = findViewById(R.id.txtRange);
        seekBarRang = findViewById(R.id.seekBarRang);
        update = findViewById(R.id.btnUpdate);
        progressUpdate = findViewById(R.id.progressUpdate);


        if (userType.equals("2")) {
            readUnread.setVisibility(View.VISIBLE);
            lastSeen.setVisibility(View.VISIBLE);
        }

        //driver and teacher can set the dnd mode for
        //call receive
        //userType : value 3 for driver and 2 for teacher
        //Other user can't see this option
        if (userType.equals("3") || userType.equals("2")) {
            switchReceiveCall.setVisibility(View.VISIBLE);
            //set default value
            isReceiveCall = sessionManager.canReceiveCall();
            switchReceiveCall.setChecked(isReceiveCall);
        } else {
            switchReceiveCall.setVisibility(View.GONE);
        }

        if (userType.equals("1")) {
            relRange.setVisibility(View.VISIBLE);
            seekBarRang.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    txtRange.setText(String.format(" : %S %s", i * 10, " m"));
                    double km = i * 10;
                    km = km / 1000;
                    range = String.valueOf(km);
                    Log.e("RANGE", range);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        } else {
            relRange.setVisibility(View.GONE);
        }

        getUserProfile();

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        swipeRefreshLayout.setColorSchemeResources(typedValue.resourceId);
        swipeRefreshLayout.setOnRefreshListener(this::getUserProfile);

        update.setOnClickListener(v -> {
            validateInputs();
        });

        profileImageLayout.setOnClickListener(v -> {
            Dexter.withContext(this)
                    .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                Intent intentPick = new Intent(Intent.ACTION_PICK);
                                // Sets the type as image/*. This ensures only components of type image are selected
                                intentPick.setType("image/*");
                                //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                                String[] mimeTypes = {"image/jpeg", "image/png"};
                                intentPick.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                                startActivityForResult(intentPick, Common.REQUEST_IMAGE_PICKER);
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
        });

        lastSeen.setOnCheckedChangeListener((buttonView, isChecked) -> isLastSeenEnabled = isChecked);

        readUnread.setOnCheckedChangeListener((buttonView, isChecked) -> isReadUnreadMessagesEnabled = isChecked);

        switchReceiveCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isReceiveCall = b;
            }
        });

    }

    private void getUserProfile() {
        if (!detector.isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.you_are_not_connected),
                    Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            swipeRefreshLayout.setRefreshing(true);
            AndroidNetworking.post(Common.BASE_URL + "app-user-profile")
                    .setPriority(Priority.HIGH)
                    .addBodyParameter("user_app_code", Common.APP_CODE)
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
                            Log.e(TAG, "onResponse: " + response);
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");

                                if (success == 1) {
                                    JSONObject userProfile = response.getJSONObject("data").getJSONObject("user_profile");
                                    String uName = userProfile.getString("user_name");
                                    String pNumber = userProfile.getString("user_phone_no");
                                    String uImage = userProfile.getString("user_image");
                                    String uCode = userProfile.getString("user_access_code");
                                    range = userProfile.getString("user_notification_distance");
                                    double km = Double.parseDouble(range);
                                    km = km * 1000;
                                    range = String.valueOf(km/1000);
                                    seekBarRang.setProgress((int) km / 10);

                                    Log.e("RANGE", range);

                                    isLastSeenEnabled = userProfile.getInt("user_last_seen_status") == 1;
                                    isReadUnreadMessagesEnabled = userProfile.getInt("user_read_status") == 1;

                                    name.setText(uName);
                                    phone.setText(pNumber);
                                    Glide.with(MyProfile.this)
                                            .load(uImage)
                                            .apply(new RequestOptions()
                                                    .transform(new CenterCrop(),
                                                            new RoundedCorners(getResources().
                                                                    getDimensionPixelSize(R.dimen.image_corner_radius))))
                                            .into(profileImage);
                                    userName.setText(uCode);

                                    sessionManager.updateImageUrl(uImage);
                                    sessionManager.updateLastSeenFlag(isLastSeenEnabled);
                                    sessionManager.updateReadUnreadMessagesFlag(isReadUnreadMessagesEnabled);

                                    lastSeen.setChecked(isLastSeenEnabled);
                                    readUnread.setChecked(isReadUnreadMessagesEnabled);

                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(MyProfile.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                sessionManager.updateReceiveCall(isReceiveCall);
                                Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                            }
                            if (swipeRefreshLayout.isRefreshing())
                                swipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                            if (swipeRefreshLayout.isRefreshing())
                                swipeRefreshLayout.setRefreshing(false);
                        }
                    });
        }

    }

    private void validateInputs() {
        String uName = userName.getText().toString();
        String pWord = password.getText().toString();
        if (uName.isEmpty()) {
            userName.setError("Enter username!");
            userName.requestFocus();
        } else {
            progressUpdate.setVisibility(View.VISIBLE);
            update.setVisibility(View.INVISIBLE);
            if (selectedImageUri == null) {
                if (!pWord.isEmpty()) {
                    updateProfile(uName, pWord);
                } else {
                    updateProfile(uName);
                }
            } else {
                imageCompressTask = new ImageCompressTask(this,
                        new PathFinder(MyProfile.this).getPath(selectedImageUri), 250, 250,
                        new IImageCompressTaskListener() {
                            @Override
                            public void onComplete(List<File> compressed) {
                                if (compressed.get(0) != null) {
                                    profileFile = compressed.get(0);
                                    Log.d("ImageCompressor", "New photo size ==> " + profileFile.length());

                                    if (!pWord.isEmpty()) {
                                        updateProfile(profileFile, uName, pWord);
                                    } else {
                                        updateProfile(profileFile, uName);
                                    }

                                } else {
                                    Log.e("ImageCompressor", "onComplete: received result is null");
                                    progressUpdate.setVisibility(View.INVISIBLE);
                                    update.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(Throwable error) {
                                Log.e(TAG, "onError: " + error.getLocalizedMessage());
                                progressUpdate.setVisibility(View.INVISIBLE);
                                update.setVisibility(View.VISIBLE);
                            }
                        });
                mExecutorService.execute(imageCompressTask);
            }
        }

        //Update can receive call locally
        sessionManager.updateReceiveCall(isReceiveCall);
    }

    private void updateProfile(String userAccessCode) {
        if (!detector.isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.you_are_not_connected),
                    Toast.LENGTH_SHORT).show();
            progressUpdate.setVisibility(View.INVISIBLE);
            update.setVisibility(View.VISIBLE);
        } else {
            AndroidNetworking.post(Common.BASE_URL + "app-user-save-profile")
                    .setPriority(Priority.HIGH)
                    .addBodyParameter("user_app_code", Common.APP_CODE)
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("device_type", "1")
                    .addBodyParameter("fcm_token", fcmToken)
                    .addBodyParameter("user_access_code", userAccessCode)
                    .addBodyParameter("user_last_seen_status", isLastSeenEnabled ? "1" : "0")
                    .addBodyParameter("user_read_status", isReadUnreadMessagesEnabled ? "1" : "0")
                    .addBodyParameter("user_notification_distance", range)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");

                                if (success == 1) {
                                    Toast.makeText(MyProfile.this, "Your profile is updated.", Toast.LENGTH_SHORT).show();
                                    getUserProfile();
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(MyProfile.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                            }
                            progressUpdate.setVisibility(View.INVISIBLE);
                            update.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                            progressUpdate.setVisibility(View.INVISIBLE);
                            update.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void updateProfile(String userAccessCode, String userPassword) {
        if (!detector.isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.you_are_not_connected),
                    Toast.LENGTH_SHORT).show();
            progressUpdate.setVisibility(View.INVISIBLE);
            update.setVisibility(View.VISIBLE);
        } else {
            AndroidNetworking.post(Common.BASE_URL + "app-user-save-profile")
                    .setPriority(Priority.HIGH)
                    .addBodyParameter("user_app_code", Common.APP_CODE)
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("device_type", "1")
                    .addBodyParameter("fcm_token", fcmToken)
                    .addBodyParameter("user_access_code", userAccessCode)
                    .addBodyParameter("user_password", userPassword)
                    .addBodyParameter("user_last_seen_status", isLastSeenEnabled ? "1" : "0")
                    .addBodyParameter("user_read_status", isReadUnreadMessagesEnabled ? "1" : "0")
                    .addBodyParameter("user_notification_distance", range)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");

                                if (success == 1) {
                                    Toast.makeText(MyProfile.this, "Your profile is updated.", Toast.LENGTH_SHORT).show();
                                    getUserProfile();
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(MyProfile.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                            }
                            progressUpdate.setVisibility(View.INVISIBLE);
                            update.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                            progressUpdate.setVisibility(View.INVISIBLE);
                            update.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void updateProfile(File profileImageFile, String userAccessCode, String userPassword) {
        if (!detector.isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.you_are_not_connected),
                    Toast.LENGTH_SHORT).show();
            progressUpdate.setVisibility(View.INVISIBLE);
            update.setVisibility(View.VISIBLE);
        } else {
            AndroidNetworking.upload(Common.BASE_URL + "app-user-save-profile")
                    .setPriority(Priority.HIGH)
                    .addMultipartFile("user_image", profileImageFile)
                    .addMultipartParameter("user_app_code", Common.APP_CODE)
                    .addMultipartParameter("user_id", userId)
                    .addMultipartParameter("user_token", userToken)
                    .addMultipartParameter("user_type", userType)
                    .addMultipartParameter("device_id", deviceId)
                    .addMultipartParameter("device_type", "1")
                    .addMultipartParameter("fcm_token", fcmToken)
                    .addMultipartParameter("user_access_code", userAccessCode)
                    .addMultipartParameter("user_password", userPassword)
                    .addMultipartParameter("user_last_seen_status", isLastSeenEnabled ? "1" : "0")
                    .addMultipartParameter("user_read_status", isReadUnreadMessagesEnabled ? "1" : "0")
                    .addMultipartParameter("user_notification_distance", range)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");

                                if (success == 1) {
                                    Toast.makeText(MyProfile.this, "Your profile is updated.", Toast.LENGTH_SHORT).show();
                                    getUserProfile();
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(MyProfile.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                            }
                            progressUpdate.setVisibility(View.INVISIBLE);
                            update.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: " + anError.getErrorCode());
                            Log.e(TAG, "onError: " + anError.getErrorDetail());
                            Log.e(TAG, "onError: " + anError.getErrorBody());
                            progressUpdate.setVisibility(View.INVISIBLE);
                            update.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void updateProfile(File profileImageFile, String userAccessCode) {
        if (!detector.isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.you_are_not_connected),
                    Toast.LENGTH_SHORT).show();
            progressUpdate.setVisibility(View.INVISIBLE);
            update.setVisibility(View.VISIBLE);
        } else {
            AndroidNetworking.upload(Common.BASE_URL + "app-user-save-profile")
                    .setPriority(Priority.HIGH)
                    .addMultipartFile("user_image", profileImageFile)
                    .addMultipartParameter("user_app_code", Common.APP_CODE)
                    .addMultipartParameter("user_id", userId)
                    .addMultipartParameter("user_token", userToken)
                    .addMultipartParameter("user_type", userType)
                    .addMultipartParameter("device_id", deviceId)
                    .addMultipartParameter("device_type", "1")
                    .addMultipartParameter("fcm_token", fcmToken)
                    .addMultipartParameter("user_access_code", userAccessCode)
                    .addMultipartParameter("user_last_seen_status", isLastSeenEnabled ? "1" : "0")
                    .addMultipartParameter("user_read_status", isReadUnreadMessagesEnabled ? "1" : "0")
                    .addMultipartParameter("user_notification_distance", range)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e(TAG, "onResponse: " + response);
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");

                                if (success == 1) {
                                    Toast.makeText(MyProfile.this, "Your profile is updated.", Toast.LENGTH_SHORT).show();
                                    getUserProfile();
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(MyProfile.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                            }
                            progressUpdate.setVisibility(View.INVISIBLE);
                            update.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: " + anError.getErrorCode());
                            Log.e(TAG, "onError: " + anError.getErrorDetail());
                            Log.e(TAG, "onError: " + anError.getErrorBody());
                            progressUpdate.setVisibility(View.INVISIBLE);
                            update.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.REQUEST_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Log.e(TAG, "onActivityResult: selectedImageUri -> " + selectedImageUri);
            Glide.with(this).load(selectedImageUri).into(profileImage);
        }
    }

    public void onLogOut() {
        if (TrackingService.isTracking) {
            Intent serviceIntent = new Intent(this, TrackingService.class);
            serviceIntent.setAction(Common.ACTION_STOP_SERVICE);
            startService(serviceIntent);
        }
        sessionManager.logoutUser();
        Intent i = new Intent(this, LogIn.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
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