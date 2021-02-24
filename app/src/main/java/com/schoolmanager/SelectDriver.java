package com.schoolmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;
import com.schoolmanager.adapters.DriversRecyclerAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.common.LogOutUser;
import com.schoolmanager.model.DriverItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.SpaceItemDecoration;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.schoolmanager.common.Common.LOG_OUT_SUCCESS;


public class SelectDriver extends BaseActivity {

    private static final String TAG = "select_driver_activity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView driverName, driverPhoneNo;
    private RecyclerView driversRecycler;
    private ProgressBar loadingProgress;

    private final List<DriverItem> driverList = new ArrayList<>();
    private DriversRecyclerAdapter adapter;

    private String search = "";
    private int currentPage = Common.PAGE_START;
    private boolean isLastPage = false;
    private int totalPage;
    private boolean isNextPageCalled = false;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken;
    private int driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_driver);
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
        driverId = sessionManager.getDriverId();
        fcmToken = sessionManager.getFcmToken();

        swipeRefreshLayout = findViewById(R.id.srlSelectDriver);
        driverName = findViewById(R.id.tvDriver);
        driverPhoneNo = findViewById(R.id.tvVehicleNo);
        driversRecycler = findViewById(R.id.rvDriverList);
        loadingProgress = findViewById(R.id.progressLoading);

        driversRecycler.setLayoutManager(new GridLayoutManager(this, 1,
                RecyclerView.VERTICAL, false));
        driversRecycler.addItemDecoration(new SpaceItemDecoration(this, RecyclerView.VERTICAL,
                1, R.dimen.recycler_vertical_offset, R.dimen.recycler_horizontal_offset,
                false));
        adapter = new DriversRecyclerAdapter(this, driverList, (driverItem, position) -> {
            driverId = driverItem.getDriverId();
            setDriver();
        }, true);
        driversRecycler.setAdapter(adapter);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        swipeRefreshLayout.setColorSchemeResources(typedValue.resourceId);
        swipeRefreshLayout.setOnRefreshListener(this::getSelectedDriver);

        driversRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLastPage && !isNextPageCalled) {
                    if (detector.isConnectingToInternet())
                        loadMoreItems();
                }
            }
        });

        getSelectedDriver();
    }

    private void getSelectedDriver() {
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
                                    driverId = userProfile.getInt("user_driver_id");
                                    String dName = userProfile.getString("user_driver_name");
                                    String dPhone = userProfile.getString("user_driver_phone_no");

                                    if(driverId == 0){
                                        driverName.setText("None");
                                        driverPhoneNo.setText("");
                                        driverPhoneNo.setVisibility(View.GONE);
                                        sessionManager.upsertDriver(0, "None", "", "");
                                    }else{
                                        driverName.setText(dName);
                                        driverPhoneNo.setText(dPhone);
                                        driverPhoneNo.setVisibility(View.VISIBLE);
                                        sessionManager.upsertDriver(driverId, dName, dPhone, "");
                                    }


                                    currentPage = Common.PAGE_START;
                                    getListOfDrivers(currentPage);

                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(SelectDriver.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
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

    private void loadMoreItems() {
        currentPage++;
//        progressLoading.setVisibility(View.VISIBLE);
        Log.e("ProductList", "loadMoreItems: " + currentPage);
        getListOfDrivers(currentPage);
    }

    private void getListOfDrivers(int pageNumber) {
        if (!detector.isConnectingToInternet()) {
            Snackbar.make(driversRecycler, getString(R.string.you_are_not_connected),
                    Snackbar.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            isNextPageCalled = false;
        } else {
            if (pageNumber == Common.PAGE_START) {
                swipeRefreshLayout.setRefreshing(true);
                loadingProgress.setVisibility(View.INVISIBLE);
            }else{
                if(swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
                loadingProgress.setVisibility(View.VISIBLE);
            }
            isNextPageCalled = true;
            AndroidNetworking.post(Common.BASE_URL + "app-driver-list")
                    .setPriority(Priority.HIGH)
                    .addBodyParameter("user_app_code", Common.APP_CODE)
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("device_type", "1")
                    .addBodyParameter("fcm_token", fcmToken)
                    .addBodyParameter("search_text", search)
                    .addBodyParameter("page_no", String.valueOf(pageNumber))
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
                                    totalPage = data.getInt("total_page");
                                    currentPage = data.getInt("current_page");

                                    if (currentPage == Common.PAGE_START) {
                                        driverList.clear();

                                        DriverItem driverItem = new DriverItem();
                                        driverItem.setDriverId(0);
                                        driverItem.setDriverName("None");
                                        if(driverItem.getDriverId() == driverId)
                                            driverItem.setSelected(true);

                                        driverList.add(driverItem);
                                    }

                                    int itemsInList = driverList.size();

                                    JSONArray drivers = data.getJSONArray("driver_list");
                                    for (int i = 0; i < drivers.length(); i++) {
                                        JSONObject driver = drivers.getJSONObject(i);
                                        DriverItem driverItem = new DriverItem();
                                        driverItem.setDriverId(driver.getInt("driver_id"));
                                        driverItem.setDriverName(driver.getString("driver_name"));
                                        if (!driver.isNull("driver_phone_no")
                                                && !driver.getString("driver_phone_no").isEmpty()
                                                && !driver.getString("driver_phone_no").equals("null"))
                                            driverItem.setPhoneNo(driver.getString("driver_phone_no"));
                                        if (!driver.isNull("driver_address")
                                                && !driver.getString("driver_address").isEmpty()
                                                && !driver.getString("driver_address").equals("null"))
                                            driverItem.setDriverAddress(driver.getString("driver_address"));
                                        if(!driver.isNull("driver_profile")
                                                && !driver.getString("driver_profile").isEmpty()
                                                && !driver.getString("driver_profile").equals("null"))
                                            driverItem.setDriverImage(driver.getString("driver_profile"));

                                        if(driverItem.getDriverId() == driverId)
                                            driverItem.setSelected(true);

                                        driverList.add(driverItem);
                                    }

                                    if (currentPage == Common.PAGE_START) {
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        adapter.notifyItemRangeInserted(itemsInList, drivers.length());
                                    }

                                    isLastPage = currentPage == totalPage;
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(SelectDriver.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                            }
                            if (swipeRefreshLayout.isRefreshing())
                                swipeRefreshLayout.setRefreshing(false);
                            isNextPageCalled = false;
                            loadingProgress.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                            if (swipeRefreshLayout.isRefreshing())
                                swipeRefreshLayout.setRefreshing(false);
                            isNextPageCalled = false;
                            loadingProgress.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    private void setDriver(){
        if (!detector.isConnectingToInternet()) {
            Toast.makeText(this, getString(R.string.you_are_not_connected),
                    Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            swipeRefreshLayout.setRefreshing(true);
            AndroidNetworking.post(Common.BASE_URL + "app-select-student-driver")
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
                            Log.e(TAG, "onResponse: " + response);
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");

                                if (success == 1) {
                                    getSelectedDriver();
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(SelectDriver.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
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

    public void onLogOut() {
        LogOutUser.getInstance(this, status -> {
            if(status == LOG_OUT_SUCCESS){
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
        }).performLogOut();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search = query;
                currentPage = Common.PAGE_START;
                getListOfDrivers(currentPage);
                searchView.clearFocus();
//                searchView.setIconified(true);
//                searchView.clearFocus();
//                searchMenuItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search = newText;
                currentPage = Common.PAGE_START;
                getListOfDrivers(currentPage);
                return true;
            }
        });

        return true;
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