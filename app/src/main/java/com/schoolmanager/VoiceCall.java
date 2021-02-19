package com.schoolmanager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.schoolmanager.common.Common;
import com.schoolmanager.events.EventCallEnd;
import com.schoolmanager.events.EventCallReceive;
import com.schoolmanager.events.EventCallRinging;
import com.schoolmanager.utilities.UserSessionManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.HashMap;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;


public class VoiceCall extends AppCompatActivity {
    private static final String TAG = VoiceCall.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;

    // Permission WRITE_EXTERNAL_STORAGE is not mandatory
    // for Agora RTC SDK, just in case if you wanna save
    // logs to external sdcard.
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;

    private FrameLayout mLocalContainer;
    private RelativeLayout mRemoteContainer;
    private VideoCanvas mLocalVideo;
    private VideoCanvas mRemoteVideo;

    private ImageView mCallBtn;
    private ImageView mMuteBtn;
    private ImageView mSwitchCameraBtn;
    private ImageView img_VoiceCall_propic;
    private TextView voiceCallNameOfUser;
    private TextView voiceCallStatus;

    /**
     * Event handler registered into RTC engine for RTC callbacks.
     * Note that UI operations needs to be in UI thread because RTC
     * engine deals with the events in a separate thread.
     */
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        /**
         * Occurs when the local user joins a specified channel.
         * The channel name assignment is based on channelName specified in the joinChannel method.
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         *
         * @param channel Channel name.
         * @param uid User ID.
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered.
         */
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                    joinChannel();
                }
            });
        }

        /**
         * Occurs when the first remote video frame is received and decoded.
         * This callback is triggered in either of the following scenarios:
         *
         *     The remote user joins the channel and sends the video stream.
         *     The remote user stops sending the video stream and re-sends it after 15 seconds. Possible reasons include:
         *         The remote user leaves channel.
         *         The remote user drops offline.
         *         The remote user calls the muteLocalVideoStream method.
         *         The remote user calls the disableVideo method.
         *
         * @param uid User ID of the remote user sending the video streams.
         * @param width Width (pixels) of the video stream.
         * @param height Height (pixels) of the video stream.
         * @param elapsed Time elapsed (ms) from the local user calling the joinChannel method until this callback is triggered.
         */
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         *     Leave the channel: When the user/host leaves the channel, the user/host sends a
         *     goodbye message. When this message is received, the SDK determines that the
         *     user/host leaves the channel.
         *
         *     Drop offline: When no data packet of the user or host is received for a certain
         *     period of time (20 seconds for the communication profile, and more for the live
         *     broadcast profile), the SDK assumes that the user/host drops offline. A poor
         *     network connection may lead to false detections, so we recommend using the
         *     Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who leaves the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         *     USER_OFFLINE_QUIT(0): The user left the current channel.
         *     USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         *     USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "User offline, uid: " + (uid & 0xFFFFFFFFL));

                    voiceCallStatus.setText(getString(R.string.call_decliend));
                    apiCallEnd();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            leaveChannel();
                            finish();
                            onRemoteUserLeft(uid);
                        }
                    }, 1000);


                }
            });
        }
    };

    private void setupRemoteVideo(int uid) {
        ViewGroup parent = mRemoteContainer;
        if (parent.indexOfChild(mLocalVideo.view) > -1) {
            parent = mLocalContainer;
        }

        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        if (mRemoteVideo != null) {
            return;
        }

        /*
          Creates the video renderer view.
          CreateRendererView returns the SurfaceView type. The operation and layout of the view
          are managed by the app, and the Agora SDK renders the view provided by the app.
          The video display view must be created using this method instead of directly
          calling SurfaceView.
         */
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(parent == mLocalContainer);
        parent.addView(view);
        mRemoteVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        // Initializes the video view of a remote user.
        mRtcEngine.setupRemoteVideo(mRemoteVideo);
    }

    private void onRemoteUserLeft(int uid) {
        if (mRemoteVideo != null && mRemoteVideo.uid == uid) {
            removeFromParent(mRemoteVideo);
            // Destroys remote view
            mRemoteVideo = null;
        }
    }

    private String channel_name = ""; //cahnnel name combination of from_user_id+to_user_id
    private String from_user_id = ""; //Loged in user id
    private String to_user_type = "";  // Typpe of user whome you want to call
    private String to_user_id = ""; // Id of user whome you want to call
    private String type = ""; // init : to init call , receive : to receive call
    private String name = ""; // name of apponent
    private String image = ""; // image of apponent

    private String call_id = ""; // string : api response id of initiated call

    private long startCallTime = System.currentTimeMillis();
    private long endCallTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        EventBus.getDefault().register(this);

        //Initialize ids
        initUI();


        if (MyApplication.mpCall != null && MyApplication.mpCall.isPlaying()) {
            MyApplication.mpCall.stop();
            MyApplication.mpCall.release();
            MyApplication.mpCall = null;
        }

        type = getIntent().getStringExtra("type");
        channel_name = getIntent().getStringExtra("channel_name");
        from_user_id = getIntent().getStringExtra("from_user_id");
        to_user_type = getIntent().getStringExtra("to_user_type");
        to_user_id = getIntent().getStringExtra("to_user_id");
        call_id = getIntent().hasExtra("call_id") ? getIntent().getStringExtra("call_id") : "";
        name = getIntent().hasExtra("name") ? getIntent().getStringExtra("name") : "";
        image = getIntent().hasExtra("image") ? getIntent().getStringExtra("image") : "";


        Glide.with(this)
                .load(image)
                .into(img_VoiceCall_propic);

        voiceCallNameOfUser.setText(name);

        if (type.equals("init")) {
            apiCallInit();
        }
        if (type.equals("receive")) {

            apiCallCalReceive();
            // Ask for permissions at runtime.
            // This is just an example set of permissions. Other permissions
            // may be needed, and please refer to our online documents.
            if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                    checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                    checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
                initEngineAndJoinChannel();
            }

            UserSessionManager userSessionManager = new UserSessionManager(this);
            userSessionManager.setInitiatedCallId(0);
        }

        showVoiceCallNotification();
    }

    private void apiCallCalReceive() {


        UserSessionManager sessionManager = new UserSessionManager(VoiceCall.this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        String userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        String userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        String userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        String deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        String fcmToken = sessionManager.getFcmToken();


        AndroidNetworking.post(Common.BASE_URL + "app-call-receive")
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("receiver_id", to_user_id)
                .addBodyParameter("receiver_type", to_user_type)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("call_token", channel_name)
                .addBodyParameter("call_id", call_id)
                .addBodyParameter("call_status", "1")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int success = response.getInt("success");
                            String message = response.getString("message");
                            Log.e("RECEIVE_CALL==>", message);
                            voiceCallStatus.setText(getString(R.string.call_started));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

    }

    private void apiCallInit() {

        UserSessionManager sessionManager = new UserSessionManager(VoiceCall.this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        String userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        String userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        String userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        String deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        String fcmToken = sessionManager.getFcmToken();

        AndroidNetworking.post(Common.BASE_URL + "app-call-init")
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("receiver_id", to_user_id)
                .addBodyParameter("receiver_type", to_user_type)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("call_token", channel_name)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int success = response.getInt("success");
                            String message = response.getString("message");
                            Log.e("INIT_CALL==>", message);
                            Log.e("INIT_CALL==>", response.toString());

                            if (success == 1) {
                                JSONObject data = response.getJSONObject("data");
                                call_id = data.getString("call_id");
                                // Ask for permissions at runtime.
                                // This is just an example set of permissions. Other permissions
                                // may be needed, and please refer to our online documents.
                                if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                                        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                                        checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
                                    initEngineAndJoinChannel();
                                }
                            } else {
                                voiceCallStatus.setText(message);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 1000);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                            voiceCallStatus.setText(getString(R.string.call_error));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 1000);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        voiceCallStatus.setText(getString(R.string.call_error));
                        apiCallEnd();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    }
                });

    }

    private void apiCallEnd() {
        clearInitNotificaion();

        UserSessionManager sessionManager = new UserSessionManager(VoiceCall.this);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        String userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        String userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        String userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        String deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        String fcmToken = sessionManager.getFcmToken();

        AndroidNetworking.post(Common.BASE_URL + "app-call-end")
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("receiver_id", to_user_id)
                .addBodyParameter("receiver_type", to_user_type)
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("call_token", channel_name)
                .addBodyParameter("call_id", call_id)
                .addBodyParameter("call_status", "1")
                .addBodyParameter("call_start_time", startCallTime + "")
                .addBodyParameter("call_end_time", System.currentTimeMillis() + "")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int success = response.getInt("success");
                            String message = response.getString("message");
                            Log.e("END_CALL==>", message);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

    }

    private void initUI() {
        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);

        mCallBtn = findViewById(R.id.btn_call);
        mMuteBtn = findViewById(R.id.btn_mute);
        mSwitchCameraBtn = findViewById(R.id.btn_switch_camera);
        voiceCallStatus = findViewById(R.id.voiceCallStatus);
        img_VoiceCall_propic = findViewById(R.id.img_VoiceCall_propic);
        voiceCallNameOfUser = findViewById(R.id.voiceCallNameOfUser);

        // Sample logs are optional.
        showSampleLogs();

        voiceCallStatus.setText(getString(R.string.calling));
    }

    private void showSampleLogs() {
        Log.e(TAG, "Welcome to Agora 1v1 video call");
        Log.e(TAG, "You will see custom logs here");
        Log.e(TAG, "You can also use this to show errors");
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
        joinChannel();
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.setDefaultAudioRoutetoSpeakerphone(false);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(true);
        mLocalContainer.addView(view);
        // Initializes the local video view.
        // RENDER_MODE_HIDDEN: Uniformly scale the video until it fills the visible boundaries. One dimension of the video may have clipped contents.
        mLocalVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(mLocalVideo);
    }

    private void joinChannel() {
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name that
        // you use to generate this token.
        String token = getString(R.string.agora_access_token);
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
            token = null; // default, no token
        }
        mRtcEngine.joinChannel(null, channel_name, "Extra Optional Data", 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (!mCallEnd) {
            leaveChannel();
            apiCallEnd();
        }
        /*
          Destroys the RtcEngine instance and releases all resources used by the Agora SDK.

          This method is useful for apps that occasionally make voice or video calls,
          to free up resources for other operations when not making calls.
         */
        RtcEngine.destroy();
    }

    private void leaveChannel() {
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
    }

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);
    }

    public void onSwitchCameraClicked(View view) {
        // Switches between front and rear cameras.
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            mCallBtn.setImageResource(R.drawable.btn_startcall);
        }

        showButtons(!mCallEnd);
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
    }

    private void endCall() {
        removeFromParent(mLocalVideo);
        mLocalVideo = null;
        removeFromParent(mRemoteVideo);
        mRemoteVideo = null;
        leaveChannel();
        apiCallEnd();
        voiceCallStatus.setText(getString(R.string.call_decliend));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);
    }

    private ViewGroup removeFromParent(VideoCanvas canvas) {
        if (canvas != null) {
            ViewParent parent = canvas.view.getParent();
            if (parent != null) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(canvas.view);
                return group;
            }
        }
        return null;
    }

    private void switchView(VideoCanvas canvas) {
        ViewGroup parent = removeFromParent(canvas);
        if (parent == mLocalContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }
            mRemoteContainer.addView(canvas.view);
        } else if (parent == mRemoteContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }
            mLocalContainer.addView(canvas.view);
        }
    }

    public void onLocalContainerClick(View view) {
        switchView(mLocalVideo);
        switchView(mRemoteVideo);
    }

    @Override
    public void onBackPressed() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallEnd(EventCallEnd callEnd) {
        if (call_id == callEnd.getCall_id()) {
            endCall();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallReceive(EventCallReceive callReceive) {
        if (call_id.equals(callReceive.getCall_id())) {
            voiceCallStatus.setText(getString(R.string.call_started));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallRinging(EventCallRinging callRinging) {
        if (call_id.equals(callRinging.getCall_id())) {
            voiceCallStatus.setText(getString(R.string.call_ringing));
        }
    }


    private void showVoiceCallNotification() {


        long when = System.currentTimeMillis();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createVoiceCallNotificationChannel(mNotificationManager);
        }

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.content_voice_call_notification);

       /* if (StringUtils.isNotEmpty(image)) {
            URL appImgUrlLink = null;
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                StrictMode.setThreadPolicy(policy);

                appImgUrlLink = new URL(image);
                contentView.setImageViewBitmap(R.id.image_app, BitmapFactory.decodeStream(appImgUrlLink.openConnection().getInputStream()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }*/

        contentView.setTextViewText(R.id.title, name);


        Intent notificationIntent = new Intent(getApplicationContext(), VoiceCall.class);
        notificationIntent.putExtra("channel_name", channel_name)
                .putExtra("from_user_id", from_user_id)
                .putExtra("to_user_type", to_user_type)
                .putExtra("to_user_id", to_user_id)
                .putExtra("to_user_id", to_user_type)
                .putExtra("call_id", call_id)
                .putExtra("type", "receive")
                .putExtra("name", name)
                .putExtra("image", image);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), Common.VOICE_CALL_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(contentView)
                .setContentTitle(name)
                .setContentIntent(contentIntent)
                .setAutoCancel(false)
                .setWhen(when);

        mNotificationManager.notify(111111, notificationBuilder.build());

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createVoiceCallNotificationChannel(NotificationManager notificationManager) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(Common.VOICE_CALL_NOTIFICATION_CHANNEL_ID,
                Common.VOICE_CALL_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.setDescription("Voice call notification channel");
        notificationChannel.setLightColor(Color.BLUE);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public void clearInitNotificaion() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getSystemService(ns);
        nMgr.cancel(111111);
    }
}
