package com.schoolmanager.dialogs;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.schoolmanager.R;
import com.schoolmanager.Settings;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.schoolmanager.common.Common.APP_CODE;
import static com.schoolmanager.common.Common.BASE_URL;

public class LogoutDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final String TAG = "logout_dialog";

    private Activity mActivity = null;

    private Button logout;
    private ProgressBar progressLogOut;

    private ConnectionDetector detector;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId;

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

        detector = new ConnectionDetector(mActivity);
        sessionManager = new UserSessionManager(mActivity);
        HashMap<String, String> hashMap = sessionManager.getUserDetails();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);

        Button cancel = view.findViewById(R.id.btnCancel);
        logout = view.findViewById(R.id.btnLogout);
        progressLogOut = view.findViewById(R.id.progressLogOut);

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
            logOutUser();
        }
    }

    private void logOutUser(){
        if (!detector.isConnectingToInternet()) {
            Snackbar.make(logout, getString(R.string.you_are_not_connected),
                    Snackbar.LENGTH_LONG).show();
            progressLogOut.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.VISIBLE);
        } else {
            progressLogOut.setVisibility(View.VISIBLE);
            logout.setVisibility(View.INVISIBLE);
            AndroidNetworking
                    .post(BASE_URL + "app-user-logout")
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("user_app_code", APP_CODE)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("device_type", "1")
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");
                                Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
                                if (success == 1) {
                                    dismiss();
                                    ((Settings) mActivity).onLogOut();
                                } else if (success == 2) {
                                    dismiss();
                                    ((Settings) mActivity).onLogOut();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse -> " + e.getLocalizedMessage());
                            }
                            progressLogOut.setVisibility(View.INVISIBLE);
                            logout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError -> " + anError.getLocalizedMessage());
                            Toast.makeText(mActivity, "Could not sign out!", Toast.LENGTH_LONG).show();
                            progressLogOut.setVisibility(View.INVISIBLE);
                            logout.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

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
