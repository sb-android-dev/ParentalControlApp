package com.schoolmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.schoolmanager.adapters.PlacesAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.common.LogOutUser;
import com.schoolmanager.databinding.ActivityStudentPlaceScreenBinding;
import com.schoolmanager.model.PlacesItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.SpaceItemDecoration;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import static com.schoolmanager.common.Common.LOG_OUT_SUCCESS;

public class StudentPlaceScreen extends BaseActivity {

    private ActivityStudentPlaceScreenBinding binding;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken, driver_id;
    private int currentPage = Common.PAGE_START;
    private boolean isLastPage = false;
    private int totalPage;
    private boolean isNextPageCalled = false;
    private PlacesAdapter placesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_student_place_screen);
        init();
    }

    private void init() {
        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        detector = new ConnectionDetector(this);
        sessionManager = new UserSessionManager(this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        fcmToken = sessionManager.getFcmToken();

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        binding.srlStudnetPlaceScreen.setColorSchemeResources(typedValue.resourceId);
        binding.srlStudnetPlaceScreen.setOnRefreshListener(() -> {
            currentPage = Common.PAGE_START;
            apiCallGetListOfPlaces(currentPage);
        });

        binding.rvStudnetPlaceScreen.addItemDecoration(new SpaceItemDecoration(this, RecyclerView.VERTICAL,
                1, R.dimen.recycler_vertical_offset, R.dimen.recycler_horizontal_offset,
                false));
        binding.rvStudnetPlaceScreen.setLayoutManager(new LinearLayoutManager(this));
        placesAdapter = new PlacesAdapter(this, new ArrayList<>(), new PlacesAdapter.OnClickPlaceItem() {
            @Override
            public void onClick(PlacesItem placesItem) {
                startActivity(new Intent(StudentPlaceScreen.this, StudentsList.class)
                        .putExtra("place_id", String.valueOf(placesItem.getPlace_id())));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        binding.rvStudnetPlaceScreen.setEmptyView(binding.llNoData);
        binding.rvStudnetPlaceScreen.setAdapter(placesAdapter);
        binding.rvStudnetPlaceScreen.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLastPage && !isNextPageCalled) {
                    if (detector.isConnectingToInternet()){
//                        loadMoreItems();
                    }

                }
            }
        });
        apiCallGetListOfPlaces(currentPage);
    }

    private void apiCallGetListOfPlaces(int pageNumber) {
        if (!detector.isConnectingToInternet()) {
            Snackbar.make(binding.rvStudnetPlaceScreen, getString(R.string.you_are_not_connected),
                    Snackbar.LENGTH_LONG).show();
            binding.srlStudnetPlaceScreen.setRefreshing(false);
            isNextPageCalled = false;
        } else {
            if (pageNumber == Common.PAGE_START) {
                binding.srlStudnetPlaceScreen.setRefreshing(true);
                binding.progressLoading.setVisibility(View.INVISIBLE);
            } else {
                if (binding.srlStudnetPlaceScreen.isRefreshing())
                    binding.srlStudnetPlaceScreen.setRefreshing(false);
                binding.progressLoading.setVisibility(View.VISIBLE);
            }
            isNextPageCalled = true;
            AndroidNetworking.post(Common.BASE_URL + "app-driver-places")
                    .addBodyParameter("user_app_code", Common.APP_CODE)
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("fcm_token", fcmToken)
                    .addBodyParameter("device_type", "1")
                    .addBodyParameter("driver_id", userId)
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
//                                    totalPage = data.getInt("total_page");
//                                    currentPage = data.getInt("current_page");

                                    String driver_places = data.getJSONArray("driver_places").toString();
                                    Type type = new TypeToken<ArrayList<PlacesItem>>() {
                                    }.getType();
                                    ArrayList<PlacesItem> mList = new Gson().fromJson(driver_places, type);
                                    if (currentPage == Common.PAGE_START) {
                                        placesAdapter.addData(true, mList);
                                    } else {
                                        placesAdapter.addData(false, mList);
                                    }

                                    isLastPage = currentPage == totalPage;
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(StudentPlaceScreen.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("TAG", "onResponse: " + e.getLocalizedMessage());
                            }
                            if (binding.srlStudnetPlaceScreen.isRefreshing())
                                binding.srlStudnetPlaceScreen.setRefreshing(false);
                            isNextPageCalled = false;
                            binding.progressLoading.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e("TAG", "onError: " + anError.getLocalizedMessage());
                            if (binding.srlStudnetPlaceScreen.isRefreshing())
                                binding.srlStudnetPlaceScreen.setRefreshing(false);
                            isNextPageCalled = false;
                            binding.progressLoading.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    private void loadMoreItems() {
        currentPage++;
        apiCallGetListOfPlaces(currentPage);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}