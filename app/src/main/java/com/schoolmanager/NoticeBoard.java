package com.schoolmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.schoolmanager.adapters.NoticeRecyclerAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.model.DriverItem;
import com.schoolmanager.model.NoticeItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.RecyclerViewWithEmptyView;
import com.schoolmanager.utilities.SpaceItemDecoration;
import com.schoolmanager.utilities.SpinnerDatePicker.DatePicker;
import com.schoolmanager.utilities.SpinnerDatePicker.DatePickerDialog;
import com.schoolmanager.utilities.SpinnerDatePicker.SpinnerDatePickerDialogBuilder;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class NoticeBoard extends BaseActivity {

    private static final String TAG = "notice_board_activity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewWithEmptyView noticesRecycler;
    private LinearLayout noDataLayout;
    private ProgressBar loadingProgress;

    private final List<NoticeItem> noticeList = new ArrayList<>();
    private NoticeRecyclerAdapter adapter;

    private String search = "", noticeDate = "";
    private Calendar calendar;
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
        setContentView(R.layout.activity_notice_board);
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

        swipeRefreshLayout = findViewById(R.id.srlNotice);
        noticesRecycler = findViewById(R.id.rvNotice);
        noDataLayout = findViewById(R.id.llNoData);
        loadingProgress = findViewById(R.id.progressLoading);

        noticesRecycler.setLayoutManager(new GridLayoutManager(this, 1,
                RecyclerView.VERTICAL, false));
        noticesRecycler.addItemDecoration(new SpaceItemDecoration(this, RecyclerView.VERTICAL,
                1, R.dimen._12dp, R.dimen._12dp,
                false));
        noticesRecycler.setEmptyView(noDataLayout);

        adapter = new NoticeRecyclerAdapter(this, noticeList);
        noticesRecycler.setAdapter(adapter);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        swipeRefreshLayout.setColorSchemeResources(typedValue.resourceId);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = Common.PAGE_START;
            getListOfNotice(currentPage);
//                swipeRefreshLayout.setRefreshing(false);
        });

        noticesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        getListOfNotice(currentPage);
    }

    private void loadMoreItems() {
        currentPage++;
//        progressLoading.setVisibility(View.VISIBLE);
        Log.e("ProductList", "loadMoreItems: " + currentPage);
        getListOfNotice(currentPage);
    }

    private void getListOfNotice(int pageNumber) {
        if (!detector.isConnectingToInternet()) {
            Snackbar.make(noticesRecycler, getString(R.string.you_are_not_connected),
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
            AndroidNetworking.post(Common.BASE_URL + "app-user-notices")
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
                    .addBodyParameter("notice_date", noticeDate)
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
                                        noticeList.clear();
                                    }

                                    int itemsInList = noticeList.size();

                                    JSONArray notices = data.getJSONArray("notice_list");
                                    for (int i = 0; i < notices.length(); i++) {
                                        JSONObject notice = notices.getJSONObject(i);
                                        NoticeItem noticeItem = new NoticeItem();
                                        noticeItem.setNoticeId(notice.getInt("notice_id"));
                                        noticeItem.setNoticeName(notice.getString("notice_name"));
                                        if (!notice.isNull("notice_details")
                                                && !notice.getString("notice_details").isEmpty()
                                                && !notice.getString("notice_details").equals("null"))
                                            noticeItem.setNoticeDetail(notice.getString("notice_details"));
                                        if (!notice.isNull("notice_thumb_image")
                                                && !notice.getString("notice_thumb_image").isEmpty()
                                                && !notice.getString("notice_thumb_image").equals("null"))
                                            noticeItem.setNoticeThumbImage(notice.getString("notice_thumb_image"));
                                        if (!notice.isNull("notice_main_image")
                                                && !notice.getString("notice_main_image").isEmpty()
                                                && !notice.getString("notice_main_image").equals("null"))
                                            noticeItem.setNoticeMainImage(notice.getString("notice_main_image"));
                                        noticeItem.setNoticeTime(Long.parseLong(notice.getString("notice_time"))*1000);

                                        noticeList.add(noticeItem);
                                    }

                                    if (currentPage == Common.PAGE_START) {
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        adapter.notifyItemRangeInserted(itemsInList, notices.length());
                                    }

                                    isLastPage = currentPage == totalPage;
                                } else if (success == 2) {
                                    onLogOut();
                                } else {
                                    Toast.makeText(NoticeBoard.this, message, Toast.LENGTH_SHORT).show();
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

    private void getCalendar() {
        if(calendar == null){
            calendar = Calendar.getInstance();
        }
        new SpinnerDatePickerDialogBuilder().context(this)
                .callback(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        noticeDate = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                        currentPage = Common.PAGE_START;
                        getListOfNotice(currentPage);
                    }

                    @Override
                    public void onClearDate(DatePicker view) {
                        calendar = null;
                        noticeDate = "";
                        currentPage = Common.PAGE_START;
                        getListOfNotice(currentPage);
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
        getMenuInflater().inflate(R.menu.notice_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search = query;
                currentPage = Common.PAGE_START;
                getListOfNotice(currentPage);
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
                getListOfNotice(currentPage);
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