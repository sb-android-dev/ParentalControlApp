package com.schoolmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.schoolmanager.adapters.BroadcastMessageAdapter;
import com.schoolmanager.adapters.ChatMessageAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.databinding.ActivityBroadCastMessageBinding;
import com.schoolmanager.model.BroadCastMessageItem;
import com.schoolmanager.model.ChatMessageModal;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class BroadCastMessage extends AppCompatActivity {

    private static final String TAG = "borad_cast_message_activity";

    private ActivityBroadCastMessageBinding broadCastMessageBinding;
    private ConnectionDetector detector;

    private BroadcastMessageAdapter broadcastMessageAdapter;
    private LinearLayoutManager mLayoutManager = null;

    private int currentPage = Common.PAGE_START;
    private boolean isLastPage = false;
    private int totalPage;
    private boolean isNextPageCalled = false;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadCastMessageBinding = DataBindingUtil.setContentView(this, R.layout.activity_broad_cast_message);
        broadCastMessageBinding.setHandler(new HandlerBroadcastMessage());
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

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        broadcastMessageAdapter = new BroadcastMessageAdapter(this, new ArrayList<>());
        broadCastMessageBinding.resViewBroadCastMessage.setLayoutManager(mLayoutManager);
        broadCastMessageBinding.resViewBroadCastMessage.setAdapter(broadcastMessageAdapter);

        broadCastMessageBinding.resViewBroadCastMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
             /*   if (!isLastPage && !isNextPageCalled) {
                    if (detector.isConnectingToInternet())
                        currentPage++;
                    apiCallFetchMessages(currentPage);
                }*/
            }
        });

        broadCastMessageBinding.resViewBroadCastMessage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (i6 < i7) {
                    scrollToBottomResView();
                }
            }
        });



        apiCallFetchBroadCastMessage(currentPage);
    }

    private void apiCallFetchBroadCastMessage(int pageNumber){

        if (!detector.isConnectingToInternet()) {

            Snackbar.make(broadCastMessageBinding.resViewBroadCastMessage, "Looks like you're not connected with internet!",
                    Snackbar.LENGTH_LONG).show();
            isNextPageCalled = false;

            return;
        }

        broadCastMessageBinding.pBarBroadCastMessage.setVisibility(View.VISIBLE);
        isNextPageCalled = true;


        AndroidNetworking.post(Common.BASE_URL + "app-user-broadcasts")
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("page_no", String.valueOf(pageNumber))
                .addBodyParameter("search_text", "")
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
                                    broadcastMessageAdapter.clear();
                                }

                                String message_list = data.getJSONArray("broadcast_list").toString();
                                ArrayList<BroadCastMessageItem> mMessageList = new Gson().fromJson(
                                        message_list,
                                        new TypeToken<ArrayList<BroadCastMessageItem>>() {
                                        }.getType());


                                scrollToBottomResView();


                                if (mMessageList.size() > 0) {
                                    if (currentPage == Common.PAGE_START) {
                                        broadcastMessageAdapter.addData(true, mMessageList);
                                    } else {
                                        broadcastMessageAdapter.addData(false, mMessageList);
                                    }
                                    isLastPage = currentPage == totalPage;
                                } else {
                                    //Empty list...
                                }

                            } else if (success == 2) {
                                onLogOut();
                            } else {
                                //Status false ...
                            }

                            isNextPageCalled = false;
                            broadCastMessageBinding.pBarBroadCastMessage.setVisibility(View.GONE);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                        Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                        isNextPageCalled = false;
                        broadCastMessageBinding.pBarBroadCastMessage.setVisibility(View.GONE);
                    }
                });


    }

    private void scrollToBottomResView() {
        broadCastMessageBinding.resViewBroadCastMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (broadcastMessageAdapter.getItemCount() > 0) {
                    mLayoutManager.smoothScrollToPosition(broadCastMessageBinding.resViewBroadCastMessage, null, broadcastMessageAdapter.getItemCount() - 1);
                }

            }
        }, 500);
    }

    public class HandlerBroadcastMessage {
        public void onBackPress(View view) {
            onBackPressed();
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

}


