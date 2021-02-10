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
import com.schoolmanager.adapters.StudentsRecyclerAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.model.ComplaintItem;
import com.schoolmanager.model.StudentItem;
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

public class StudentsList extends BaseActivity {

    private static final String TAG = "students_list_activity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewWithEmptyView studentsRecycler;
    private LinearLayout noDataLayout;
    private ProgressBar loadingProgress;

    private final List<StudentItem> studentList = new ArrayList<>();
    private StudentsRecyclerAdapter adapter;

    private String search = "";
    private int currentPage = Common.PAGE_START;
    private boolean isLastPage = false;
    private int totalPage;
    private boolean isNextPageCalled = false;

    private int classId, sectionId;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
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

        swipeRefreshLayout = findViewById(R.id.srlStudent);
        studentsRecycler = findViewById(R.id.rvStudent);
        noDataLayout = findViewById(R.id.llNoData);
        loadingProgress = findViewById(R.id.progressLoading);

        if (getIntent() != null) {
            classId = getIntent().getIntExtra("class_id", 0);
            sectionId = getIntent().getIntExtra("section_id", 0);
        }

        studentsRecycler.setLayoutManager(new GridLayoutManager(this, 1,
                RecyclerView.VERTICAL, false));
        studentsRecycler.addItemDecoration(new SpaceItemDecoration(this, RecyclerView.VERTICAL,
                1, R.dimen.recycler_vertical_offset, R.dimen.recycler_horizontal_offset,
                false));
        studentsRecycler.setEmptyView(noDataLayout);
        adapter = new StudentsRecyclerAdapter(this, studentList, new StudentsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onClick(StudentItem studentItem, int position) {
                /**
                 * Redirect to ChatBoard to chat with Teacher
                 *
                 */
                Intent intent = new Intent(StudentsList.this, ChatBoardActivity.class);
                ComplaintItem complaintItem = new ComplaintItem(
                        "0",
                        "",
                        "",
                        0,
                        studentItem.getParentImage(),
                        studentItem.getParentName(),
                        studentItem.getParentId(),
                        1,
                        1,
                        1,
                        0
                );
                intent.putExtra("complaint_data", new Gson().toJson(complaintItem));
                startActivity(intent);
            }

            @Override
            public void onCall(StudentItem studentItem, int position) {
                /**
                 * Redirect to Voice call with this parent
                 */
                startActivity(new Intent(StudentsList.this, VoiceCall.class)
                        .putExtra("channel_name", userId + studentItem.getParentId())
                        .putExtra("from_user_id", userId)
                        .putExtra("to_user_type", "1")
                        .putExtra("to_user_id", studentItem.getParentId() + "")
                        .putExtra("type", "init")
                );
            }
        });
        studentsRecycler.setAdapter(adapter);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        swipeRefreshLayout.setColorSchemeResources(typedValue.resourceId);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = Common.PAGE_START;
            getListOfStudents(currentPage);
        });

        studentsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        getListOfStudents(currentPage);
    }

    private void loadMoreItems() {
        currentPage++;
//        progressLoading.setVisibility(View.VISIBLE);
        Log.e("ProductList", "loadMoreItems: " + currentPage);
        getListOfStudents(currentPage);
    }

    private void getListOfStudents(int pageNumber) {
        if (!detector.isConnectingToInternet()) {
            Snackbar.make(studentsRecycler, getString(R.string.you_are_not_connected),
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
            AndroidNetworking.post(Common.BASE_URL + "app-student-list")
                    .addBodyParameter("user_app_code", Common.APP_CODE)
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("fcm_token", fcmToken)
                    .addBodyParameter("device_type", "1")
                    .addBodyParameter("search_text", search)
                    .addBodyParameter("page_no", String.valueOf(pageNumber))
                    .addBodyParameter("section_id", String.valueOf(sectionId).equals("0") ? "" : String.valueOf(sectionId))
                    .addBodyParameter("class_id", String.valueOf(classId).equals("0") ? "" : String.valueOf(classId))
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
                                        studentList.clear();
                                    }

                                    int itemsInList = studentList.size();

                                    JSONArray students = data.getJSONArray("student_list");
                                    for (int i = 0; i < students.length(); i++) {
                                        JSONObject student = students.getJSONObject(i);
                                        StudentItem studentItem = new StudentItem();
                                        studentItem.setStudentId(student.getInt("student_id"));
                                        studentItem.setStudentName(student.getString("student_name"));
                                        studentItem.setClassName(student.getString("class_name"));
                                        studentItem.setSectionName(student.getString("section_name"));
                                        studentItem.setParentId(student.getInt("parent_id"));
                                        studentItem.setParentName(student.getString("parent_name"));
                                        studentItem.setParentImage(student.getString("parent_profile"));

                                        studentList.add(studentItem);
                                    }

                                    if (currentPage == Common.PAGE_START) {
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        adapter.notifyItemRangeInserted(itemsInList, students.length());
                                    }

                                    isLastPage = currentPage == totalPage;
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(StudentsList.this, message, Toast.LENGTH_SHORT).show();
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
                getListOfStudents(currentPage);
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
                getListOfStudents(currentPage);
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