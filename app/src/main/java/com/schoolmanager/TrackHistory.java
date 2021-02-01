package com.schoolmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;
import com.schoolmanager.adapters.TrackingHistorySequenceAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.model.TrackingHistoryItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.SpinnerDatePicker.DatePicker;
import com.schoolmanager.utilities.SpinnerDatePicker.DatePickerDialog;
import com.schoolmanager.utilities.SpinnerDatePicker.SpinnerDatePickerDialogBuilder;
import com.schoolmanager.utilities.UserSessionManager;
import com.transferwise.sequencelayout.SequenceLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TrackHistory extends AppCompatActivity {

    private static final String TAG = "track_history_activity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView trackingDate;
    private SequenceLayout sequenceLayout;
    private LinearLayout noDataLayout;

    private String trackDate = "", scanDate="";
    private Calendar calendar;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat tf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_history);
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
        studentId = sessionManager.getStudentId();

        swipeRefreshLayout = findViewById(R.id.srlTracking);
        trackingDate = findViewById(R.id.tvTrackingDate);
        sequenceLayout = findViewById(R.id.slTrackingHistory);
        noDataLayout = findViewById(R.id.llNoData);

        calendar = Calendar.getInstance();
        trackDate = calendar.get(Calendar.YEAR) + "-"
                + new DecimalFormat("00").format(calendar.get(Calendar.MONTH)+1) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH);
        scanDate = sdf2.format(new Date(calendar.getTimeInMillis()));
        getTrackingDataFor(trackDate);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        swipeRefreshLayout.setColorSchemeResources(typedValue.resourceId);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getTrackingDataFor(trackDate);
//                swipeRefreshLayout.setRefreshing(false);
        });

    }

//    private void getTrackingData() {
//        if(calendar == null){
//            calendar = Calendar.getInstance();
//        }
//        String tD = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date(calendar.getTimeInMillis()));
//        trackingDate.setText(tD);
//
//        ArrayList<TrackingHistoryItem> historyList = new ArrayList<>();
//        historyList.add(new TrackingHistoryItem("Driver Picked", "Going to school", "07:00 AM", false));
//        historyList.add(new TrackingHistoryItem("At School", "Reached", "07:30 AM", false));
//        historyList.add(new TrackingHistoryItem("Leaved School", "Coming to home", "12:30 PM", false));
//        historyList.add(new TrackingHistoryItem("Driver Dropped", "Leave the bus", "01:00 PM", true));
//        historyList.add(new TrackingHistoryItem("Arrived", "", "01:05 PM", false));
//
//        sequenceLayout.setAdapter(new TrackingHistorySequenceAdapter(this, historyList));
//    }

    private void getTrackingDataFor(String date){
        if (!detector.isConnectingToInternet()) {
            Snackbar.make(sequenceLayout, getString(R.string.you_are_not_connected),
                    Snackbar.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            swipeRefreshLayout.setRefreshing(true);
            AndroidNetworking.post(Common.BASE_URL + "app-student-track-history")
                    .setPriority(Priority.HIGH)
                    .addBodyParameter("user_app_code", Common.APP_CODE)
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("device_type", "1")
                    .addBodyParameter("student_id", studentId)
                    .addBodyParameter("track_date", date)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e(TAG, "onResponse: " + response);
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");

                                if (success == 1) {
                                    JSONObject data = response.getJSONObject("data");

                                    trackingDate.setText(scanDate);

                                    ArrayList<TrackingHistoryItem> historyList = new ArrayList<>();

                                    JSONObject status1 = data.getJSONObject("status_1");
                                    boolean active1 = status1.getInt("active")==1;
                                    String time1 = status1.getString("time");
                                    String t1 = "";
                                    if(!time1.isEmpty()){
                                        t1 = tf.format(sdf.parse(time1));
                                    }
                                    historyList.add(new TrackingHistoryItem("Driver Picked", "Going to school", t1, active1));
                                    JSONObject status2 = data.getJSONObject("status_2");
                                    boolean active2 = status2.getInt("active")==1;
                                    String time2 = status2.getString("time");
                                    String t2 = "";
                                    if(!time2.isEmpty()){
                                        t2 = tf.format(sdf.parse(time2));
                                    }
                                    historyList.add(new TrackingHistoryItem("At School", "Reached", t2, active2));
                                    JSONObject status3 = data.getJSONObject("status_3");
                                    boolean active3 = status3.getInt("active")==1;
                                    String time3 = status3.getString("time");
                                    String t3 = "";
                                    if(!time3.isEmpty()){
                                        t3 = tf.format(sdf.parse(time3));
                                    }
                                    historyList.add(new TrackingHistoryItem("Leaved School", "Coming to home", t3, active3));
                                    JSONObject status4 = data.getJSONObject("status_4");
                                    boolean active4 = status4.getInt("active")==1;
                                    String time4 = status4.getString("time");
                                    String t4 = "";
                                    if(!time4.isEmpty()){
                                        t4 = tf.format(sdf.parse(time4));
                                    }
                                    historyList.add(new TrackingHistoryItem("Driver Dropped", "Leave the bus", t4, active4));
//                                    historyList.add(new TrackingHistoryItem("Arrived", "", "01:05 PM", false));
                                    sequenceLayout.setAdapter(new TrackingHistorySequenceAdapter(TrackHistory.this, historyList));

                                    if(active1 || active2 || active3 || active4){
                                        noDataLayout.setVisibility(View.INVISIBLE);
                                        sequenceLayout.setVisibility(View.VISIBLE);
                                        trackingDate.setVisibility(View.VISIBLE);
                                    }else{
                                        noDataLayout.setVisibility(View.VISIBLE);
                                        sequenceLayout.setVisibility(View.INVISIBLE);
                                        trackingDate.setVisibility(View.INVISIBLE);
                                    }

                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(TrackHistory.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse: " + e.getLocalizedMessage());

                                noDataLayout.setVisibility(View.VISIBLE);
                                sequenceLayout.setVisibility(View.INVISIBLE);
                                trackingDate.setVisibility(View.INVISIBLE);
                            }
                            if (swipeRefreshLayout.isRefreshing())
                                swipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: " + anError.getErrorCode());
                            Log.e(TAG, "onError: " + anError.getErrorDetail());
                            Log.e(TAG, "onError: " + anError.getErrorBody());

                            noDataLayout.setVisibility(View.VISIBLE);
                            sequenceLayout.setVisibility(View.INVISIBLE);
                            trackingDate.setVisibility(View.INVISIBLE);

                            if (swipeRefreshLayout.isRefreshing())
                                swipeRefreshLayout.setRefreshing(false);
                        }
                    });
        }
    }

    private void getCalendar() {
        if(calendar == null){
            calendar = Calendar.getInstance();
        }
        new SpinnerDatePickerDialogBuilder().context(this)
                .callback(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        trackDate = year + "-" + new DecimalFormat("00").format(monthOfYear+1) + "-" + dayOfMonth;
                        scanDate = sdf2.format(new Date(calendar.getTimeInMillis()));
                        getTrackingDataFor(trackDate);
                    }

                    @Override
                    public void onClearDate(DatePicker view) {
                        calendar = Calendar.getInstance();
                        trackDate = calendar.get(Calendar.YEAR) + "-"
                                + new DecimalFormat("00").format(calendar.get(Calendar.MONTH)+1) + "-"
                                + calendar.get(Calendar.DAY_OF_MONTH);
                        scanDate = sdf2.format(new Date(calendar.getTimeInMillis()));
                        getTrackingDataFor(trackDate);
                    }
                })
                .spinnerTheme(R.style.DatePickerSpinner)
                .defaultDate(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                .build().show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tracking_history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_calendar) {
            getCalendar();
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