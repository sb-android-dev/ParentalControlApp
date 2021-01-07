package com.schoolmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.schoolmanager.adapters.DriversRecyclerAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.model.DriverItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.RecyclerViewWithEmptyView;
import com.schoolmanager.utilities.SpaceItemDecoration;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DriversList extends AppCompatActivity {

    private static final String TAG = "driver_list_activity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewWithEmptyView driversRecycler;
    private LinearLayout noDataLayout;
    private ProgressBar loadingProgress;

    private List<DriverItem> driverList = new ArrayList<>();
    private DriversRecyclerAdapter adapter;

    private String search = "";
    private int currentPage = Common.PAGE_START;
    private boolean isLastPage = false;
    private int totalPage;
    private boolean isNextPageCalled = false;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_list);
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

        swipeRefreshLayout = findViewById(R.id.srlDriver);
        driversRecycler = findViewById(R.id.rvDrivers);
        noDataLayout = findViewById(R.id.llNoData);
        loadingProgress = findViewById(R.id.progressLoading);

        driversRecycler.setLayoutManager(new GridLayoutManager(this, 1,
                RecyclerView.VERTICAL, false));
        driversRecycler.addItemDecoration(new SpaceItemDecoration(this, RecyclerView.VERTICAL,
                1, R.dimen.recycler_vertical_offset, R.dimen.recycler_horizontal_offset,
                false));
        driversRecycler.setEmptyView(noDataLayout);
        adapter = new DriversRecyclerAdapter(this, driverList, (driverItem, position) -> {
//            Toast.makeText(DriversList.this, driverItem.getDriverName(), Toast.LENGTH_SHORT).show();
//            if(driverItem.getDriverLocation() != null) {
                Intent nextIntent = new Intent(DriversList.this, LocateOnMap.class);
                nextIntent.putExtra("location", driverItem.getDriverLocation());
                nextIntent.putExtra("driver_name", driverItem.getDriverName());
                nextIntent.putExtra("driver_id", driverItem.getDriverId());
                startActivity(nextIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//            }
        });
        driversRecycler.setAdapter(adapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = Common.PAGE_START;
            getListOfDrivers(currentPage);
//                swipeRefreshLayout.setRefreshing(false);
        });

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

        getListOfDrivers(currentPage);
    }

    private void loadMoreItems() {
        currentPage++;
//        progressLoading.setVisibility(View.VISIBLE);
        Log.e("ProductList", "loadMoreItems: " + currentPage);
        getListOfDrivers(currentPage);
    }

//    private List<DriverItem> getDriverList(){
//        List<DriverItem> drivers = new ArrayList<>();
//        drivers.add(new DriverItem(0, "Rameshbhai", "GJ03AJ4512"));
//        drivers.add(new DriverItem(1, "Munnabhai", "GJ03AN5623"));
//        drivers.add(new DriverItem(2, "Rakeshbhai", "GJ03DE4215"));
//        drivers.add(new DriverItem(3, "Bharatbhai", "GJ03EA7812"));
//        drivers.add(new DriverItem(4, "Ketanbhai", "GJ03FC7912"));
//        drivers.add(new DriverItem(5, "Rajubhai", "GJ03FG6152"));
//        drivers.add(new DriverItem(6, "Mohanbhai", "GJ03AZ2564"));
//        drivers.add(new DriverItem(7, "Hareshbhai", "GJ03CE3481"));
//        drivers.add(new DriverItem(8, "Sureshbhai", "GJ03BH8673"));
//        drivers.add(new DriverItem(9, "Rambhai", "GJ03DK4186"));
//        drivers.add(new DriverItem(10, "Shyambhai", "GJ03ED7936"));
//
//        return drivers;
//    }

    private void getListOfDrivers(int pageNumber) {
        if (!detector.isConnectingToInternet()) {
            Snackbar.make(driversRecycler, "Looks like you're not connected with internet!",
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
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");

                                if (success == 1) {
                                    JSONObject data = response.getJSONObject("data");
                                    totalPage = data.getInt("total_page");
                                    currentPage = data.getInt("current_page");

                                    if (currentPage == Common.PAGE_START) {
                                        driverList.clear();
                                    }

                                    int itemsInList = driverList.size();

                                    JSONArray drivers = data.getJSONArray("driver_list");
                                    for (int i = 0; i < drivers.length(); i++) {
                                        JSONObject driver = drivers.getJSONObject(i);
                                        DriverItem driverItem = new DriverItem();
                                        driverItem.setDriverId(driver.getInt("driver_id"));
                                        driverItem.setDriverName(driver.getString("driver_name"));
                                        if (!driver.isNull("driver_phone_no"))
                                        driverItem.setPhoneNo(driver.getString("driver_phone_no"));
                                        if (!driver.isNull("driver_address"))
                                            driverItem.setDriverAddress(driver.getString("driver_address"));
                                        if (!driver.isNull("driver_location")){
                                            JSONObject location = new JSONObject(driver.getString("driver_location"));
                                            LatLng latLng = new LatLng(Double.parseDouble(location.getString("lat")),
                                                    Double.parseDouble(location.getString("long")));
                                            driverItem.setDriverLocation(latLng);
                                        }

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
                                    Toast.makeText(DriversList.this, message, Toast.LENGTH_SHORT).show();
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