package com.schoolmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.schoolmanager.adapters.TeachersRecyclerAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.model.ComplaintItem;
import com.schoolmanager.model.TeacherItem;
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

public class TeachersList extends AppCompatActivity {

    private static final String TAG = "teacher_list_activity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewWithEmptyView teachersRecycler;
    private LinearLayout noDataLayout;
    private ProgressBar loadingProgress;

    private final List<TeacherItem> teacherList = new ArrayList<>();
    private TeachersRecyclerAdapter adapter;

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
        setContentView(R.layout.activity_teachers_list);
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

        swipeRefreshLayout = findViewById(R.id.srlTeachers);
        teachersRecycler = findViewById(R.id.rvTeachers);
        noDataLayout = findViewById(R.id.llNoData);
        loadingProgress = findViewById(R.id.progressLoading);

        teachersRecycler.setLayoutManager(new GridLayoutManager(this, 1,
                RecyclerView.VERTICAL, false));
        teachersRecycler.addItemDecoration(new SpaceItemDecoration(this, RecyclerView.VERTICAL,
                1, R.dimen.recycler_vertical_offset, R.dimen.recycler_horizontal_offset,
                false));
        teachersRecycler.setEmptyView(noDataLayout);
        adapter = new TeachersRecyclerAdapter(this, teacherList, (teacherItem, position) -> {
            /**
             * Redirect to ChatBoard to chat with Parent
             *
             */
            Intent intent = new Intent(TeachersList.this, ChatBoardActivity.class);
            ComplaintItem complaintItem = new ComplaintItem(
                    "0",
                    "",
                    "",
                    0,
                    "",
                    teacherItem.getTeacherName(),
                    teacherItem.getTeacherId(),
                    2,
                    1,
                    1
            );
            intent.putExtra("complaint_data", new Gson().toJson(complaintItem));
            startActivity(intent);

        });
        teachersRecycler.setAdapter(adapter);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        swipeRefreshLayout.setColorSchemeResources(typedValue.resourceId);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = Common.PAGE_START;
            getListOfTeachers(currentPage);
        });

        teachersRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        getListOfTeachers(currentPage);
    }

    private void loadMoreItems() {
        currentPage++;
//        progressLoading.setVisibility(View.VISIBLE);
        Log.e("ProductList", "loadMoreItems: " + currentPage);
        getListOfTeachers(currentPage);
    }

    private void getListOfTeachers(int pageNumber) {
        if (!detector.isConnectingToInternet()) {
            Snackbar.make(teachersRecycler, getString(R.string.you_are_not_connected),
                    Snackbar.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            isNextPageCalled = false;
        } else {
            if (pageNumber == Common.PAGE_START) {
                swipeRefreshLayout.setRefreshing(true);
                loadingProgress.setVisibility(View.INVISIBLE);
            } else {
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
                loadingProgress.setVisibility(View.VISIBLE);
            }
            isNextPageCalled = true;
            AndroidNetworking.post(Common.BASE_URL + "app-teacher-list")
                    .addBodyParameter("user_app_code", Common.APP_CODE)
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("fcm_token", fcmToken)
                    .addBodyParameter("device_type", "1")
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
                                        teacherList.clear();
                                    }

                                    int itemsInList = teacherList.size();

                                    JSONArray teachers = data.getJSONArray("teacher_list");
                                    for (int i = 0; i < teachers.length(); i++) {
                                        JSONObject teacher = teachers.getJSONObject(i);
                                        TeacherItem teacherItem = new TeacherItem();
                                        teacherItem.setTeacherId(teacher.getInt("teacher_id"));
                                        teacherItem.setTeacherName(teacher.getString("teacher_name"));
                                        if (!teacher.isNull("teacher_phone_no"))
                                            teacherItem.setTeacherPhoneNo(teacher.getString("teacher_phone_no"));

                                        teacherList.add(teacherItem);
                                    }

                                    if (currentPage == Common.PAGE_START) {
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        adapter.notifyItemRangeInserted(itemsInList, teachers.length());
                                    }

                                    isLastPage = currentPage == totalPage;
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(TeachersList.this, message, Toast.LENGTH_SHORT).show();
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
                getListOfTeachers(currentPage);
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
                getListOfTeachers(currentPage);
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