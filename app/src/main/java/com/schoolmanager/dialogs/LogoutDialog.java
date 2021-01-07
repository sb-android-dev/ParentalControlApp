package com.schoolmanager.dialogs;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.schoolmanager.Dashboard;
import com.schoolmanager.R;
import com.schoolmanager.utilities.UserSessionManager;

import java.util.HashMap;

public class LogoutDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "logout_dialog";

    private Activity mActivity = null;

    private Button logout;
    private ProgressBar progressLogOut;

    private UserSessionManager sessionManager;
    private String userId, userToken;

    public LogoutDialog() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_logout, container, false);

        sessionManager = new UserSessionManager(mActivity);
        HashMap<String, String> hashMap = sessionManager.getUserDetails();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);

        Button cancel = view.findViewById(R.id.btnCancel);
        logout = view.findViewById(R.id.btnLogout);

        cancel.setOnClickListener(this);
        logout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnCancel) {
            dismiss();
        } else if (id == R.id.btnLogout) {
            dismiss();
            ((Dashboard)mActivity).onLogOut();
            //                ((MainActivity)getActivity()).logout();
//            logOutUser();
//            dismiss();
//            sessionManager.logoutUser();
//            startActivity(new Intent(getActivity(), Splash.class));
//            Objects.requireNonNull(getActivity()).finish();
        }
    }

//    private void sendResult(int REQUEST_CODE, int sortCode){
//        Intent intent = new Intent();
//        intent.putExtra("sort_code", sortCode);
//        getTargetFragment().onActivityResult(getTargetRequestCode(), REQUEST_CODE, intent);
//    }

//    private void logOutUser(){
//        progressLogOut.setVisibility(View.VISIBLE);
//        logout.setVisibility(View.INVISIBLE);
//        StringRequest stringRequest = new StringRequest(Request.Method.POST,
//                urlManager.getUrlDetails().get(UrlSessionManager.KEY_URL) + ApiClient.BASE_URL + "app-user-logout",
//                response -> {
//                    try {
//                        JSONObject jsonObject = new JSONObject(response);
//                        int success = jsonObject.getInt("success");
//                        String message = jsonObject.getString("message");
//                        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
//                        if(success == 1){
//                            dismiss();
//                            sessionManager.logoutUser();
//                            startActivity(new Intent(getActivity(), Splash.class));
//                            Objects.requireNonNull(getActivity()).finish();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Log.e(TAG, "logOutUser -> onResponse -> " + e.getLocalizedMessage());
//                    }
//                    progressLogOut.setVisibility(View.INVISIBLE);
//                    logout.setVisibility(View.VISIBLE);
//                },
//                error -> {
//                    progressLogOut.setVisibility(View.INVISIBLE);
//                    logout.setVisibility(View.VISIBLE);
//                    Log.e(TAG, "logOutUser -> onError -> " + error.getLocalizedMessage());
//                    Toast.makeText(mActivity, "Could not log out!", Toast.LENGTH_LONG).show();
//                }){
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("user_id", userId);
//                params.put("user_token", userToken);
//                params.put("device", "android");
//                return params;
//            }
//        };
//
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
//        requestQueue.add(stringRequest);
//    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }
}
