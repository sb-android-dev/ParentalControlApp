package com.schoolmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;
import com.schoolmanager.common.Common;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

public class LogIn extends BaseActivity {

    private static final String TAG = "log_in_activity";

    private TextInputEditText username, password;
    private Button logIn;
    private ProgressBar progressSignIn;

    private UserSessionManager sessionManager;
    private String deviceId, fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        String userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        fcmToken = sessionManager.getFcmToken();
        Log.e(TAG, "onCreate: deviceId -> " + deviceId);

        if (fcmToken == null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if(!task.isSuccessful()){
                    Log.w("getInstanceId failed", task.getException());
//                    return;
                }

                // Get new Instance ID token
                sessionManager.upsertFcmToken(Objects.requireNonNull(task.getResult()));
                Log.e("MainActivity", "onComplete: " + sessionManager.getFcmToken());
                fcmToken = task.getResult();

            });
        } else {
            Log.e("MainActivity", "onCreate: checking for token -> " + sessionManager.getFcmToken());
        }

        assert userId != null;
        if(Integer.parseInt(userId) > 0){
            Intent dashboardIntent = new Intent(LogIn.this, Dashboard.class);
            startActivity(dashboardIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }

        username = findViewById(R.id.etUserName);
        password = findViewById(R.id.etPassword);
        logIn = findViewById(R.id.btnLogIn);
        progressSignIn = findViewById(R.id.progressSignIn);

        logIn.setOnClickListener(v -> validateInputs());

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()) {
                    username.setError(null);
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()) {
                    password.setError(null);
                }
            }
        });

    }

    private void validateInputs(){
        String u = Objects.requireNonNull(username.getText()).toString();
        String p = Objects.requireNonNull(password.getText()).toString();

        if(u.isEmpty() || p.isEmpty()){
            if(p.isEmpty()){
                password.setError("Please enter password!");
                password.requestFocus();
            }
            if(u.isEmpty()){
                username.setError("Please enter username!");
                username.requestFocus();
            }
        }else{
//            String type;
//            switch (u){
//                case "student":
//                    type = "1";
//                    break;
//                case "driver":
//                    type = "3";
//                    break;
//                case "teacher":
//                default:
//                    type = "2";
//            }
//            Intent dashboardIntent = new Intent(LogIn.this, Dashboard.class);
//            dashboardIntent.putExtra("type", type);
//            startActivity(dashboardIntent);
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            logInUser(u, p);
        }
    }

    /**
     * Log in user with below parameter
     * @param username - Username of user
     * @param password - password of user
     * This API returns status of request & user profile data.
     * If user is teacher it returns user's name and the subject he/she teaches.
     * If user is driver it return user's name and the vehicle no.
     * If user is parent it return child's name, class & section of child.
     * Result JSON is something like:
     * {
     *     "status" : 1,
     *     "message" : "User has successfully signed in"
     *     "user_details" : {
     *         "name" : "Smit Patel",
     *         "type" : 1,
     *         "subject" : "Science"
     *     }
     * }
     */
    private void logInUser(String username, String password){
        progressSignIn.setVisibility(View.VISIBLE);
        logIn.setVisibility(View.INVISIBLE);
        AndroidNetworking.post(Common.BASE_URL + "app-user-login")
                .addBodyParameter("user_app_code", Common.APP_CODE) /* App code for app */
                .addBodyParameter("user_access_code", username)
                .addBodyParameter("user_password", password)
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
                            if(success == 1){
                                JSONObject data = response.getJSONObject("data");
                                int userId = data.getInt("user_id");
                                String userToken = data.getString("user_token");
                                int userType = data.getInt("user_type");
                                String userName = data.getString("user_name");
                                String userPhoneNo = data.getString("user_phone_no");
                                String userImage = data.getString("user_image_url");
                                boolean isLastSeenEnabled = data.getInt("user_last_seen_status")==1;
                                boolean isReadUnreadMessageEnabled = data.getInt("user_read_status")==1;

                                if(userType == 1){
                                    int studentId = data.getInt("user_student_id");
                                    int driverId = data.getInt("user_driver_id");
                                    String driverName = data.getString("user_driver_name");
                                    String driverPhoneNo = data.getString("user_driver_phone_no");
                                    sessionManager.createUserLoginSession(userId, userToken, userName,
                                            userType, userPhoneNo, userImage, studentId);
                                    sessionManager.upsertDriver(driverId, driverName, driverPhoneNo);
                                }else{
                                    sessionManager.createUserLoginSession(userId, userToken, userName,
                                            userType, userPhoneNo, userImage);
                                }
                                sessionManager.updateLastSeenFlag(isLastSeenEnabled);
                                sessionManager.updateReadUnreadMessagesFlag(isReadUnreadMessageEnabled);

//                                Snackbar.make(logIn, "Done!!!", Snackbar.LENGTH_SHORT).show();

                                Intent dashboardIntent = new Intent(LogIn.this, Dashboard.class);
                                startActivity(dashboardIntent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            } else {
                                Snackbar.make(logIn, message, Snackbar.LENGTH_SHORT).show();
                                progressSignIn.setVisibility(View.INVISIBLE);
                                logIn.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                            progressSignIn.setVisibility(View.INVISIBLE);
                            logIn.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                        Log.e(TAG, "onError: " + anError.getErrorBody());
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        Snackbar.make(logIn, "Error while signing in. Try again later", Snackbar.LENGTH_SHORT).show();
                        progressSignIn.setVisibility(View.INVISIBLE);
                        logIn.setVisibility(View.VISIBLE);
                    }
                });
    }
}