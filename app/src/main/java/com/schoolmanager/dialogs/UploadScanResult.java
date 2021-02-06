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
import com.schoolmanager.QRScanner;
import com.schoolmanager.R;
import com.schoolmanager.model.ScanItem;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.DBHandler;
import com.schoolmanager.utilities.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.schoolmanager.common.Common.APP_CODE;
import static com.schoolmanager.common.Common.BASE_URL;

public class UploadScanResult extends BottomSheetDialogFragment {

    public static final String TAG = "upload_scan_dialog";

    private Activity mActivity = null;

    private Button upload;
    private ProgressBar progressUpload;

    private ConnectionDetector detector;
    private DBHandler dbHandler;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId;

    private String studentId, status, scanTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_upload_result, container, false);

        detector = new ConnectionDetector(mActivity);
        dbHandler = new DBHandler(mActivity);
        sessionManager = new UserSessionManager(mActivity);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);

        Button cancel = view.findViewById(R.id.btnCancel);
        upload = view.findViewById(R.id.btnUpload);
        progressUpload = view.findViewById(R.id.progressUpload);

        Bundle bundle = getArguments();
        if(bundle != null){
            studentId = bundle.getString("student_id");
            status = bundle.getString("status");
            scanTime = bundle.getString("scan_time");
        }

        cancel.setOnClickListener(v -> {
            dismiss();
            ((QRScanner) mActivity).startPreview();
        });
        upload.setOnClickListener(v -> {
            uploadOrStoreResult();
        });

        return view;
    }

    private void uploadOrStoreResult(){
        if (!detector.isConnectingToInternet()) {
            ScanItem scanItem = new ScanItem();
            scanItem.setUserId(userId);
            scanItem.setUserToken(userToken);
            scanItem.setUserType(userType);
            scanItem.setStudentId(studentId);
            scanItem.setTrackStatus(status);
            scanItem.setTrackTime(scanTime);
            dbHandler.addScanItem(scanItem);
            dismiss();
            ((QRScanner) mActivity).startPreview();
        } else {
            progressUpload.setVisibility(View.VISIBLE);
            upload.setVisibility(View.INVISIBLE);
            AndroidNetworking
                    .post(BASE_URL + "app-track-student")
                    .addBodyParameter("user_id", userId)
                    .addBodyParameter("user_token", userToken)
                    .addBodyParameter("user_type", userType)
                    .addBodyParameter("user_app_code", APP_CODE)
                    .addBodyParameter("device_id", deviceId)
                    .addBodyParameter("device_type", "1")
                    .addBodyParameter("student_id", studentId)
                    .addBodyParameter("track_status", status)
                    .addBodyParameter("track_time", scanTime)
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
                                    ((QRScanner) mActivity).startPreview();
                                } else if (success == 2) {
                                    dismiss();
                                    ((QRScanner) mActivity).onLogOut();
                                } else {
                                    ScanItem scanItem = new ScanItem();
                                    scanItem.setUserId(userId);
                                    scanItem.setUserToken(userToken);
                                    scanItem.setUserType(userType);
                                    scanItem.setStudentId(studentId);
                                    scanItem.setTrackStatus(status);
                                    scanItem.setTrackTime(scanTime);
                                    dbHandler.addScanItem(scanItem);
                                    dismiss();
                                    ((QRScanner) mActivity).startPreview();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onResponse -> " + e.getLocalizedMessage());
                                ScanItem scanItem = new ScanItem();
                                scanItem.setUserId(userId);
                                scanItem.setUserToken(userToken);
                                scanItem.setUserType(userType);
                                scanItem.setStudentId(studentId);
                                scanItem.setTrackStatus(status);
                                scanItem.setTrackTime(scanTime);
                                dbHandler.addScanItem(scanItem);
                                dismiss();
                                ((QRScanner) mActivity).startPreview();
                            }
                            progressUpload.setVisibility(View.INVISIBLE);
                            upload.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e(TAG, "onError -> " + anError.getLocalizedMessage());
                            progressUpload.setVisibility(View.INVISIBLE);
                            upload.setVisibility(View.VISIBLE);
                            ScanItem scanItem = new ScanItem();
                            scanItem.setUserId(userId);
                            scanItem.setUserToken(userToken);
                            scanItem.setUserType(userType);
                            scanItem.setStudentId(studentId);
                            scanItem.setTrackStatus(status);
                            scanItem.setTrackTime(scanTime);
                            dbHandler.addScanItem(scanItem);
                            dismiss();
                            ((QRScanner) mActivity).startPreview();
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
