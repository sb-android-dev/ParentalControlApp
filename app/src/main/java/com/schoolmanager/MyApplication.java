package com.schoolmanager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatDelegate;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.schoolmanager.common.Common;
import com.schoolmanager.utilities.UserSessionManager;
import com.schoolmanager.utilities.UserSessionManager;
import com.zeugmasolutions.localehelper.LocaleAwareApplication;

import org.json.JSONObject;

import java.util.HashMap;

public class MyApplication extends LocaleAwareApplication implements Application.ActivityLifecycleCallbacks  {

    public static MediaPlayer mp;
    public static MediaPlayer mpCall;
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    public static boolean mIsAppInForground = true;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());
        registerActivityLifecycleCallbacks(this);
        initTheme(new UserSessionManager(this).getTheme());

    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            apiCallLastSeen(activity);
        }
    }

    @Override
    public void onActivityPostStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPrePaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            apiCallLastSeen(activity);
        }
    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {

    }


    public static void apiCallLastSeen(Context context){
        UserSessionManager sessionManager = new UserSessionManager(context);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        String userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        String userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        String userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        String deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        String fcmToken = sessionManager.getFcmToken();

        AndroidNetworking.post(Common.BASE_URL + "app-user-save-last-seen")
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int success = response.getInt("success");
                            String message = response.getString("message");
                            Log.e("LAST_SEEN==>", message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

        initTheme(new UserSessionManager(this).getTheme());
    }

    public static void initTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
