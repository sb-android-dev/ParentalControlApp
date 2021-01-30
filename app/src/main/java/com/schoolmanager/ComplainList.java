package com.schoolmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.ferfalk.simplesearchview.SimpleOnQueryTextListener;
import com.ferfalk.simplesearchview.SimpleSearchViewListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.schoolmanager.adapters.ComplaintRecyclerAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.databinding.ActivityCompaintListBinding;
import com.schoolmanager.events.EventNewMessageArrives;
import com.schoolmanager.model.ComplaintItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.UserSessionManager;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class ComplainList extends AppCompatActivity {

    private static final String TAG = "complaint_list_activity";

    private ActivityCompaintListBinding binding;
    private String mFilter = "";
    private ArrayList<ComplaintItem> mList = new ArrayList<ComplaintItem>();
    private ConnectionDetector detector;
    private ComplaintRecyclerAdapter adapter;

    private int currentPage = Common.PAGE_START;
    private boolean isLastPage = false;
    private int totalPage;
    private boolean isNextPageCalled = false;


    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_compaint_list);
        binding.setHandler(new HanlderComplaintList());
        EventBus.getDefault().register(this);
        init();

    }

    private void init() {
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
        binding.swipyComplaintLlistChatList.setColorSchemeResources(typedValue.resourceId);
        binding.swipyComplaintLlistChatList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = Common.PAGE_START;
                apiCallFetchComplainList(currentPage);
            }
        });

        initRecyclerview(mList);
        initSearch();
        apiCallFetchComplainList(currentPage);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void apiCallFetchComplainList(int pageNumber) {

        if (!detector.isConnectingToInternet()) {
            binding.llComplaintEmptyView.setVisibility(View.VISIBLE);
            binding.txtComplaintListEmptyMessage.setText(getString(R.string.you_are_not_connected));

            if (binding.swipyComplaintLlistChatList.isRefreshing())
                binding.swipyComplaintLlistChatList.setRefreshing(false);
            isNextPageCalled = false;

            return;
        }

        binding.llComplaintEmptyView.setVisibility(View.GONE);
        binding.pBarCompaintList.setVisibility(View.VISIBLE);

        if (pageNumber == Common.PAGE_START) {
            binding.swipyComplaintLlistChatList.setRefreshing(true);
            binding.pBarCompaintList.setVisibility(View.INVISIBLE);
        } else {
            if (binding.swipyComplaintLlistChatList.isRefreshing())
                binding.swipyComplaintLlistChatList.setRefreshing(false);
            binding.pBarCompaintList.setVisibility(View.VISIBLE);
        }
        isNextPageCalled = true;

        AndroidNetworking.post(Common.BASE_URL + "app-chat-list")
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("search_text", "")
                .addBodyParameter("filter", mFilter)
                .addBodyParameter("user_app_code", Common.APP_CODE)
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
                                    mList.clear();
                                }

                                int itemsInList = mList.size();

                                String chat_list = data.getJSONArray("chat_list").toString();
                                ArrayList<ComplaintItem> mChatList = new Gson().fromJson(chat_list, new TypeToken<ArrayList<ComplaintItem>>() {
                                }.getType());

                                if (mChatList.size() > 0) {
                                    mList.addAll(mChatList);
                                    if (currentPage == Common.PAGE_START) {
                                        initRecyclerview(mList);
                                    } else {
                                        adapter.notifyItemRangeInserted(itemsInList, chat_list.length());
                                    }
                                    isLastPage = currentPage == totalPage;
                                } else {
                                    binding.resViewComplaintLlistChatList.setAdapter(null);
                                    binding.llComplaintEmptyView.setVisibility(View.VISIBLE);
                                    binding.txtComplaintListEmptyMessage.setText("No complaints found");
                                }

                            } else if (success == 2) {
                                onLogOut();
                            } else {
                                binding.llComplaintEmptyView.setVisibility(View.VISIBLE);
                                binding.txtComplaintListEmptyMessage.setText(message);
                            }

                            if (binding.swipyComplaintLlistChatList.isRefreshing())
                                binding.swipyComplaintLlistChatList.setRefreshing(false);
                            isNextPageCalled = false;
                            binding.pBarCompaintList.setVisibility(View.GONE);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {


                        binding.llComplaintEmptyView.setVisibility(View.VISIBLE);
                        binding.txtComplaintListEmptyMessage.setText(anError.getLocalizedMessage());

                        Log.e(TAG, "onError: " + anError.getMessage());
                        if (binding.swipyComplaintLlistChatList.isRefreshing())
                            binding.swipyComplaintLlistChatList.setRefreshing(false);
                        isNextPageCalled = false;
                        binding.pBarCompaintList.setVisibility(View.GONE);
                    }
                });


    }

    private void initRecyclerview(ArrayList<ComplaintItem> mList) {
        adapter = new ComplaintRecyclerAdapter(this, mList);
        binding.resViewComplaintLlistChatList.setLayoutManager(
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        binding.resViewComplaintLlistChatList.setAdapter(adapter);
        binding.resViewComplaintLlistChatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLastPage && !isNextPageCalled) {
                    if (detector.isConnectingToInternet())
                        currentPage++;
                    apiCallFetchComplainList(currentPage);
                }
            }
        });

    }

    private void showFilterPopup(View view) {
        PopupMenu popup = new PopupMenu(ComplainList.this, view);
        popup.getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.all:
                        mFilter = "";
                        break;
                    case R.id.today:
                        mFilter = "1";
                        break;
                    case R.id.yesterday:
                        mFilter = "2";
                        break;
                    case R.id.one_week_ago:
                        mFilter = "3";
                        break;
                }
                apiCallFetchComplainList(currentPage);
                return true;
            }
        });

        popup.show();//showing popup menu
    }

    private void initSearch() {
        binding.searchView.setOnSearchViewListener(new SimpleSearchViewListener() {
            @Override
            public void onSearchViewClosed() {
                super.onSearchViewClosed();
                initRecyclerview(mList);
            }
        });
        binding.searchView.setOnQueryTextListener(new SimpleOnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (StringUtils.isNotEmpty(newText)) {
                    new Filter(newText).execute();
                }
                return super.onQueryTextChange(newText);
            }

        });
    }

    @SuppressLint("StaticFieldLeak")
    class Filter extends AsyncTask<Void, Void, ArrayList<ComplaintItem>> {

        CharSequence s;

        Filter(CharSequence s) {
            this.s = s;
        }

        @Override
        protected ArrayList<ComplaintItem> doInBackground(Void... voids) {
            s = s.toString().toLowerCase();
            final ArrayList<ComplaintItem> filter_list = new ArrayList<ComplaintItem>();

            for (int i = 0; i < mList.size(); i++) {

                String text = mList.get(i).getChat_receiver_name();
                if (StringUtils.isNotEmpty(text)) {
                    text = text.toLowerCase();
                    if (text.contains(s)) {
                        filter_list.add(mList.get(i));
                    }
                }
            }


            return filter_list;
        }

        @Override
        protected void onPostExecute(ArrayList<ComplaintItem> jsCCModals) {
            super.onPostExecute(jsCCModals);

            initRecyclerview(jsCCModals);

            if (jsCCModals.size() > 0) {
                binding.llComplaintEmptyView.setVisibility(View.INVISIBLE);
            } else {
                binding.llComplaintEmptyView.setVisibility(View.VISIBLE);
                binding.txtComplaintListEmptyMessage.setText(R.string.no_any_complaint_matching_search);
            }
        }
    }

    public class HanlderComplaintList {
        public void onBackClick(View view) {
            onBackPressed();
        }

        public void onMenuClick(View view) {
            showFilterPopup(view);
        }

        public void onSearchClick(View view) {
            binding.searchView.showSearch();
        }

        public void onAddButtonClick(View view) {
            //Parent
            if (userType.equals("1")) {
                startActivity(new Intent(ComplainList.this, TeachersList.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
            //Teacher
            if (userType.equals("2")) {
                startActivity(new Intent(ComplainList.this, StudentsList.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
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
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewMessageArrives(EventNewMessageArrives event) {
        if (event.getReload_list()) {
            currentPage = Common.PAGE_START;
            apiCallFetchComplainList(currentPage);
        }
    }

    ;
}