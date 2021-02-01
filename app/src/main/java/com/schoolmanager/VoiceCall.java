package com.schoolmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;
import java.util.Locale;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class VoiceCall extends AppCompatActivity {

    private RtcEngine mRtcEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
    }

    private void initMicroPhonePermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if(multiplePermissionsReport.areAllPermissionsGranted()){
                            initAgoraEngineAndJoinChannel();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();
    }


    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();
        joinChannel();
    }

    private void initializeAgoraEngine() {
        try {

            final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

                // Listen for the onUserOffline callback.
                // This callback occurs when the remote user leaves the channel or drops offline.
                @Override
                public void onUserOffline(final int uid, final int reason) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        onRemoteUserLeft(uid, reason);
                        }
                    });
                }

                // Listen for the onUserMuterAudio callback.
                // This callback occurs when a remote user stops sending the audio stream.
                @Override
                public void onUserMuteAudio(final int uid, final boolean muted) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        onRemoteUserVoiceMuted(uid, muted);
                        }
                    });
                }
            };


            mRtcEngine = RtcEngine.create(getBaseContext(), "", mRtcEventHandler);
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        } catch (Exception e) {
            Log.e("LOG_TAG", Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void joinChannel() {
        String accessToken = "";
        if (TextUtils.equals(accessToken, "") || TextUtils.equals(accessToken, "#YOUR ACCESS TOKEN#")) {
            accessToken = null; // default, no token
        }

        // Call the joinChannel method to join a channel.
        // The uid is not specified. The SDK will assign one automatically.
        mRtcEngine.joinChannel(accessToken, "pr_app_channel", "Extra Optional Data", 0);
    }

    private void onRemoteUserLeft(int uid, int reason) {
        showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));
        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
        tipMsg.setVisibility(View.VISIBLE);
    }

    private void onRemoteUserVoiceMuted(int uid, boolean muted) {
        showLongToast(String.format(Locale.US, "user %d muted or unmuted %b", (uid & 0xFFFFFFFFL), muted));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    private void showLongToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}