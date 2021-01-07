package com.schoolmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.schoolmanager.common.Common;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class LocateOnMap extends AppCompatActivity implements OnMapReadyCallback, TrackingService.Callbacks {

    private static final String TAG = "locate_on_map_activity";

    private LocationManager locationManager;
    //    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult.getLocations().size() > 0) {
                Log.d(TAG, "onLocationResult: locations size -> " + locationResult.getLocations().size());
                Log.d(TAG, "onLocationResult: location -> " + locationResult.getLocations().get(0).getLatitude()
                        + ", " + locationResult.getLocations().get(0).getLongitude());
                Log.d(TAG, "onLocationResult: accuracy -> " + locationResult.getLocations().get(0).getAccuracy());
                updateLocation(locationResult.getLocations().get(0));
            }
        }
    };

    private GoogleMap mMap;
    private MarkerOptions myLocationOption, driverLocationOption;
    private Marker myLocation, driverLocation;
    private UiSettings uiSettings;

    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            locateDriver();
            handler.postDelayed(runnable, 10000);
        }
    };

    private boolean mShouldUnbind;
    TrackingService trackingService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
//            Toast.makeText(LocateOnMap.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            TrackingService.LocalBinder binder = (TrackingService.LocalBinder) service;
            trackingService = binder.getServiceInstance(); //Get instance of your service!
            trackingService.registerClient(LocateOnMap.this); //Activity register in the service as client for callabcks!
//            tvServiceState.setText("Connected to service...");
//            tbStartTask.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            trackingService = null;
//            Toast.makeText(LocateOnMap.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
//            tvServiceState.setText("Service disconnected");
//            tbStartTask.setEnabled(false);
        }
    };

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private LatLng currentLatLng, driverLatLng;
    private String driverName;
    private int driverId;

    // Keys for storing activity state.
    private static final String KEY_LOCATION = "location";

    private float locationAccuracy = -1f;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_on_map);

        detector = new ConnectionDetector(this);
        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        fcmToken = sessionManager.getFcmToken();

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        fusedLocationProviderClient = getFusedLocationProviderClient(this);
//
//        new GpsUtils(LocateOnMap.this).turnGPSOn(isGPSEnable -> {
//            // turn on GPS
//            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//                startLocationUpdates();
//            }
//        });

        if (getIntent() != null) {
            driverLatLng = getIntent().getParcelableExtra("location");
            driverName = getIntent().getStringExtra("driver_name");
            driverId = getIntent().getIntExtra("driver_id", 0);
        }

        doBindService();

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setCompassEnabled(true);
        if (mMap != null) {
            if(driverLatLng != null) {
                driverLocationOption = new MarkerOptions().position(driverLatLng).title(driverName)
                        .icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_pin));
                driverLocation = mMap.addMarker(driverLocationOption);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(driverLatLng, 17);
                mMap.animateCamera(cameraUpdate);
            }

            locateLiveDriver();

//            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
//            getMyLocation();
//            startLocationUpdates();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
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

    public void updateLocation(Location location) {
        if (location != null) {
            if (locationAccuracy == -1) {
                locationAccuracy = location.getAccuracy();
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.e(TAG, "updateLocation: " + currentLatLng.latitude + ", " + currentLatLng.longitude);
                if (myLocationOption == null) {
                    myLocationOption = new MarkerOptions().position(currentLatLng).title("My Location")
                            .icon(bitmapDescriptorFromVector(this, R.drawable.ic_my_location_pin));
                    myLocation = mMap.addMarker(myLocationOption);
//                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 17);
//                    mMap.animateCamera(cameraUpdate);
                } else {
                    myLocation.setPosition(currentLatLng);
                }

//                if (detector.isConnectingToInternet())
//                    updateLocationOnServer(currentLatLng);
            } else if (location.getAccuracy() <= locationAccuracy) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//                Log.e(TAG, "updateLocation: " + currentLatLng.latitude + ", " + currentLatLng.longitude);
                if (myLocationOption == null) {
                    myLocationOption = new MarkerOptions().position(currentLatLng).title("My Location")
                            .icon(bitmapDescriptorFromVector(this, R.drawable.ic_my_location_pin));
                    myLocation = mMap.addMarker(myLocationOption);
//                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 17);
//                    mMap.animateCamera(cameraUpdate);
                } else {
                    myLocation.setPosition(currentLatLng);
                }

//                if (detector.isConnectingToInternet())
//                    updateLocationOnServer(currentLatLng);
            }
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
                .addBodyParameter("fcm_token", fcmToken)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("lat", String.valueOf(currentLatLng.latitude))
                .addBodyParameter("long", String.valueOf(currentLatLng.longitude))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "onResponse: " + response.toString());
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getErrorDetail());
                        Log.e(TAG, "onError: " + anError.getErrorBody());
                    }
                });
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private void locateLiveDriver() {
        handler = new Handler();
        handler.postDelayed(runnable, 10000);
    }

    private void locateDriver() {
        AndroidNetworking.post(Common.BASE_URL + "app-track-driver-location")
                .setPriority(Priority.HIGH)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("fcm_token", fcmToken)
                .addBodyParameter("driver_id", String.valueOf(driverId))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "onResponse: " + response.toString());
                        try {
                            int success = response.getInt("success");
                            String message = response.getString("message");
                            if (success == 1) {
                                JSONObject data = response.getJSONObject("data");
                                driverLatLng = new LatLng(Double.parseDouble(data.getString("lat")),
                                        Double.parseDouble(data.getString("long")));
                                driverLocation.setPosition(driverLatLng);
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(driverLatLng, 17);
                                mMap.animateCamera(cameraUpdate);
                            } else if (success == 2) {
                                onLogOut();
                            } else {
                                Toast.makeText(LocateOnMap.this, message, Toast.LENGTH_SHORT).show();
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

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        // Check that Google Play services is available
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = googleApiAvailability.getErrorDialog(this, resultCode,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check for the integer request code originally supplied to startResolutionForResult().
        if (requestCode == Common.GPS_REQUEST) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    // Nothing to do. startLocationupdates() gets called in onResume again.
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    break;
            }
        }
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (mMap != null) {
//            savedInstanceState.putParcelable(KEY_LOCATION, currentLocation);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

//    private void stopLocationUpdates() {
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
//    }

    @Override
    protected void onResume() {
        super.onResume();

        // Display the connection status

//        if (currentLocation != null) {
//            Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
//            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
//            mMap.animateCamera(cameraUpdate);
//        } else {
//            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
//        }
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        stopLocationUpdates();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        handler.removeCallbacks(runnable);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    void doBindService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        if (bindService(new Intent(LocateOnMap.this, TrackingService.class), mConnection,
                Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        } else {
            Log.e("MY_APP_TAG", "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }


    void doUnbindService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            unbindService(mConnection);
            mShouldUnbind = false;
        }
    }


//                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
//                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                        if(lastLocation != null) {
//                            userLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//                            mMap.clear();
//                            mMap.addMarker(new MarkerOptions().position(userLatLng).title("Current Location"));
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
//                        }
}