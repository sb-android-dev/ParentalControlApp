package com.schoolmanager.common;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.schoolmanager.R;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.schoolmanager.common.Common.APP_CODE;
import static com.schoolmanager.common.Common.BASE_URL;
import static com.schoolmanager.common.Common.LOG_OUT_FAILED;
import static com.schoolmanager.common.Common.LOG_OUT_SUCCESS;

public class LogOutUser {

    public static final String TAG = "log_out_user_class";

    private Context mContext;
    private OnCompleteListener listener;
    String userId;
    String userToken;
    String userType;
    String deviceId;

    private ConnectionDetector detector;

    private LogOutUser logOutUser;

    public static LogOutUser getInstance(Context mContext, OnCompleteListener listener){
        return new LogOutUser(mContext, listener);
    }

    public LogOutUser(Context mContext, OnCompleteListener listener) {
        this.mContext = mContext;
        this.listener = listener;
        detector = new ConnectionDetector(mContext);
        UserSessionManager sessionManager = new UserSessionManager(mContext);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
    }

    public void performLogOut(){
        if (!detector.isConnectingToInternet()) {
            Toast.makeText(mContext, mContext.getString(R.string.you_are_not_connected),
                    Toast.LENGTH_LONG).show();
            listener.onComplete(LOG_OUT_FAILED);
        } else {
            AndroidNetworking
                    .post(BASE_URL + "app-user-logout")
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("user_app_code", APP_CODE)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("device_type", "1")
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                if (success == 1 || success == 2) {
                                    listener.onComplete(LOG_OUT_SUCCESS);
                                } else {
                                    listener.onComplete(LOG_OUT_FAILED);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse -> " + e.getLocalizedMessage());
                                listener.onComplete(LOG_OUT_FAILED);
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError -> " + anError.getLocalizedMessage());
                            Toast.makeText(mContext, "Could not sign out!", Toast.LENGTH_LONG).show();
                            listener.onComplete(LOG_OUT_FAILED);
                        }
                    });
        }
    }

    public interface OnCompleteListener{
        void onComplete(int status);
    }
}
