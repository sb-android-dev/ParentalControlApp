package com.schoolmanager.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.schoolmanager.Dashboard;
import com.schoolmanager.LogIn;
import com.schoolmanager.R;
import com.schoolmanager.common.Common;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class TrackingService extends Service {

    public static final String TAG = "tracking_service";

    Callbacks activity;
    private final IBinder mBinder = new LocalBinder();

    boolean isFirstRun = true;
    public static boolean isTracking = false;

    LatLng currentLatLng = new LatLng(0, 0);

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (isTracking && locationResult.getLocations().size() > 0) {
                Log.d(TAG, "onLocationResult: locations size -> " + locationResult.getLocations().size());
                Log.d(TAG, "onLocationResult: location -> " + locationResult.getLocations().get(0).getLatitude()
                        + ", " + locationResult.getLocations().get(0).getLongitude());
                updateLocation(locationResult.getLocations().get(0));
            }
//            onLocationChanged(locationResult.getLastLocation());
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case Common.ACTION_STOP_SERVICE:
                Log.d(TAG, "onStartCommand: service stopped");
                isTracking = false;
                updateLocationTracking();
                return super.onStartCommand(intent, flags, startId);
            case Common.ACTION_START_SERVICE:
            default:
                Log.d(TAG, "onStartCommand: service started");
                startForegroundService();
                return START_STICKY;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        detector = new ConnectionDetector(this);
        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        fcmToken = sessionManager.getFcmToken();

        fusedLocationProviderClient = getFusedLocationProviderClient(this);
    }

    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: tracking service is dead");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public TrackingService getServiceInstance() {
            return TrackingService.this;
        }
    }

    public void registerClient(Activity activity) {
        this.activity = (Callbacks) activity;
    }

    private void updateLocationTracking() {
        if (isTracking) {
            startLocationUpdates();
        } else {
            stopLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Common.UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Common.FASTEST_INTERVAL);

        Dexter.withContext(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                                Looper.myLooper());
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void updateLocation(Location location) {
        if (location != null) {
            if (activity != null)
                activity.updateLocation(location);
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Log.e(TAG, "updateLocation: " + currentLatLng.latitude + ", " + currentLatLng.longitude);

            if (detector.isConnectingToInternet())
                updateLocationOnServer(currentLatLng);
        }
    }

    private void updateLocationOnServer(LatLng currentLatLng) {
        AndroidNetworking.post(Common.BASE_URL + "app-location-track")
                .setPriority(Priority.HIGH)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("fcm_token", fcmToken)
                .addBodyParameter("lat", String.valueOf(currentLatLng.latitude))
                .addBodyParameter("long", String.valueOf(currentLatLng.longitude))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "onResponse: " + response.toString());
                        try {
                            int success = response.getInt("success");
                            if (success == 2) {
                                onLogOut();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        Log.e(TAG, "onError: " + anError.getErrorBody());
                    }
                });
    }

    private void startForegroundService() {
        isTracking = true;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager);
        }

        UserSessionManager sessionManager = new UserSessionManager(this);

        String who;
        switch (sessionManager.getUserType()) {
            case 1:
                who = "Parent";
                break;
            case 3:
                who = "Driver";
                break;
            case 2:
            default:
                who = "Teacher";
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Common.TRACKING_NOTIFICATION_CHANNEL_ID)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_my_location)
                        .setContentTitle("Tracking " + who)
                        .setContentText(sessionManager.getUserName())
                        .setContentIntent(getDashboardPendingIntent());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            startForeground(Common.TRACKING_NOTIFICATION_ID, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        else
            startForeground(Common.TRACKING_NOTIFICATION_ID, notificationBuilder.build());


        updateLocationTracking();
    }

    private PendingIntent getDashboardPendingIntent() {
        return PendingIntent.getActivity(this,
                0,
                new Intent(this, Dashboard.class).setAction(Common.ACTION_OPEN_DASHBOARD),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(Common.TRACKING_NOTIFICATION_CHANNEL_ID,
                Common.TRACKING_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

    private void stopLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true);
        stopSelf();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
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

    public interface Callbacks {
        public void updateLocation(Location location);
    }
}
