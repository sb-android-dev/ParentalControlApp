package com.schoolmanager;

import android.Manifest;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordListener;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iceteck.silicompressorr.FileUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.schoolmanager.adapters.ChatMessageAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.databinding.ActivityChatBoardBinding;
import com.schoolmanager.events.EventNewMessageArrives;
import com.schoolmanager.model.ChatMessageModal;
import com.schoolmanager.model.ComplaintItem;
import com.schoolmanager.model.NotificationItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.UserSessionManager;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChatBoardActivity extends BaseActivity {

    private static final String TAG = "chat_board_activity";


    private ActivityChatBoardBinding binding;

    private ComplaintItem mComplaintModal = null;
    private ChatMessageAdapter chatMessageAdapter;
    private LinearLayoutManager mLayoutManager = null;

    private int total_page = 1;
    private int current_page = 1;
    private String current_usr_id = "1";  // Get this id from shession...
    private String mAudioFile = "";

    private MediaRecorder mediaRecorder;
    private ConnectionDetector detector;

    private int currentPage = Common.PAGE_START;
    private boolean isLastPage = false;
    private int totalPage;
    private boolean isNextPageCalled = false;
    private UserSessionManager sessionManager;
    private String userId, userToken, userType, deviceId, fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_board);
        binding.setHandler(new HandlerChatBoard());

        EventBus.getDefault().register(this);

        getIntentData();
        init();

    }

    private void getIntentData() {
        if (getIntent().getExtras() != null) {
            mComplaintModal = new Gson().fromJson(getIntent().getStringExtra("complaint_data"),
                    ComplaintItem.class);
        }
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

        binding.edChatboardMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.imgChatboardAudio.setVisibility(charSequence.length() > 0 ? View.GONE : View.VISIBLE);
                binding.imgChatboardSend.setVisibility(charSequence.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.txtChatBoardUserName.setText(mComplaintModal.getChat_receiver_name());
        binding.txtChatBoardStudentName.setText("Student name");

        Glide.with(this)
                .load(mComplaintModal.getChat_receiver_image())
                .placeholder(R.drawable.ic_person)
                .into(binding.imgChatBoardUser);

        chatMessageAdapter = new ChatMessageAdapter(this, new ArrayList<>());
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);

        binding.resViewChatBoard.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        binding.resViewChatBoard.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (i6 < i7) {
                    scrollToBottomResView();
                }
            }
        });
        binding.resViewChatBoard.setLayoutManager(mLayoutManager);
        binding.resViewChatBoard.setAdapter(chatMessageAdapter);

        setAudioRecordngButton();

        //Fetch message data
        apiCallFetchMessages(current_page);
    }

    private void apiCallFetchMessages(int pageNumber) {

        if (!detector.isConnectingToInternet()) {

            Snackbar.make(binding.resViewChatBoard, getString(R.string.you_are_not_connected),
                    Snackbar.LENGTH_LONG).show();
            isNextPageCalled = false;

            return;
        }

        binding.pBarChatBoard.setVisibility(View.VISIBLE);
        isNextPageCalled = true;

        AndroidNetworking.post(Common.BASE_URL + "app-all-messages")
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("receiver_id", String.valueOf(mComplaintModal.getChat_receiver_id()))
                .addBodyParameter("receiver_type", String.valueOf(mComplaintModal.getChat_receiver_type()))
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
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
                                    chatMessageAdapter.clear();
                                }

                                String message_list = data.getJSONArray("message_list").toString();
                                ArrayList<ChatMessageModal> mMessageList = new Gson().fromJson(
                                        message_list,
                                        new TypeToken<ArrayList<ChatMessageModal>>() {
                                        }.getType());


                                scrollToBottomResView();


                                if (mMessageList.size() > 0) {
                                    if (currentPage == Common.PAGE_START) {
                                        chatMessageAdapter.addData(true, mMessageList);
                                    } else {
                                        chatMessageAdapter.addData(false, mMessageList);
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
                            binding.pBarChatBoard.setVisibility(View.GONE);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                        Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                        isNextPageCalled = false;
                        binding.pBarChatBoard.setVisibility(View.GONE);
                    }
                });


    }

    private void apiCallSendMessage(int m_message_type, String message, String file_url) {

        if (!detector.isConnectingToInternet()) {

            Snackbar.make(binding.resViewChatBoard, getString(R.string.you_are_not_connected),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        switch (m_message_type) {
            case 1:
                pushLocalTextMessage(message, userType, String.valueOf(mComplaintModal.getChat_receiver_type()));
                break;
            case 2:
                pushLocalImageMessage(file_url, userType, String.valueOf(mComplaintModal.getChat_receiver_type()));
                break;
            case 3:
                pushLocalAudioMessage(file_url, userType, String.valueOf(mComplaintModal.getChat_receiver_type()));
                break;
        }

        File file = (m_message_type == 3) ? new File(mAudioFile) : FileUtils.getFile(ChatBoardActivity.this, Uri.parse(file_url));

        binding.edChatboardMessage.setText("");

        AndroidNetworking.upload(Common.BASE_URL + "app-send-message")
                .addMultipartFile("message_file", file)
                .addMultipartParameter("user_id", userId)
                .addMultipartParameter("user_token", userToken)
                .addMultipartParameter("user_type", userType)
                .addMultipartParameter("message_type", String.valueOf(m_message_type))
                .addMultipartParameter("message_text", message)
                .addMultipartParameter("receiver_id", String.valueOf(mComplaintModal.getChat_receiver_id()))
                .addMultipartParameter("receiver_type", String.valueOf(mComplaintModal.getChat_receiver_type()))
                .addMultipartParameter("user_app_code", Common.APP_CODE)
                .addMultipartParameter("device_id", deviceId)
                .addMultipartParameter("device_type", "1")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        EventBus.getDefault().post(new EventNewMessageArrives(null, true));
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, anError.getMessage());
                    }
                });
    }

    private void pushLocalTextMessage(String message, String sender_type, String receiver_type) {
        long currentTime = System.currentTimeMillis()/1000;

        ChatMessageModal messageModal = new ChatMessageModal(
                "0",
                String.valueOf(currentTime),
                message,
                1,
                "",
                current_usr_id,
                String.valueOf(mComplaintModal.getChat_receiver_id()),
                sender_type,
                receiver_type);

        chatMessageAdapter.addMessageToList(messageModal);
        scrollToBottomResView();
    }

    private void pushLocalImageMessage(String file_url, String sender_type, String receiver_type) {
        long currentTime = System.currentTimeMillis()/1000;

        ChatMessageModal messageModal = new ChatMessageModal(
                "0",
                String.valueOf(currentTime),
                "",
                2,
                file_url,
                current_usr_id,
                String.valueOf(mComplaintModal.getChat_receiver_id()),
                sender_type,
                receiver_type);

        chatMessageAdapter.addMessageToList(messageModal);
        scrollToBottomResView();
    }

    private void pushLocalAudioMessage(String file_url, String sender_type, String receiver_type) {
        long currentTime = System.currentTimeMillis()/1000;

        ChatMessageModal messageModal = new ChatMessageModal(
                "0",
                String.valueOf(currentTime),
                "",
                3,
                file_url,
                current_usr_id,
                String.valueOf(mComplaintModal.getChat_receiver_id()),
                sender_type,
                receiver_type);

        chatMessageAdapter.addMessageToList(messageModal);
        scrollToBottomResView();
    }

    private void scrollToBottomResView() {
        binding.resViewChatBoard.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (chatMessageAdapter.getItemCount() > 0) {
                    mLayoutManager.smoothScrollToPosition(binding.resViewChatBoard, null, chatMessageAdapter.getItemCount() - 1);
                }

            }
        }, 500);
    }

    private void openCameraOrGallery() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {

                            ImagePicker.create(ChatBoardActivity.this)
                                    .limit(1)
                                    .theme(R.style.ImagePickerTheme)
                                    .start();
                        } else {

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void setAudioRecordngButton() {

        binding.imgChatboardAudio.setRecordView(binding.recordViewChatBoard);
        binding.recordViewChatBoard.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {

                binding.cardChatBoardMessage.setVisibility(View.INVISIBLE);

                Dexter.withContext(ChatBoardActivity.this)
                        .withPermissions(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                startMediaRecording();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, 1000);

                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();


            }

            @Override
            public void onCancel() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.cardChatBoardMessage.setVisibility(View.VISIBLE);
                    }
                },1000);


                if (mediaRecorder != null) {
                    try {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;

                        if (StringUtils.isNotEmpty(mAudioFile)) {
                            File file = new File(mAudioFile);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFinish(long recordTime) {
                binding.cardChatBoardMessage.setVisibility(View.VISIBLE);

                if (mediaRecorder != null) {
                    try {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("RECORDTIME==>",recordTime+"");
                                if(recordTime >= 2000){
                                    apiCallSendMessage(3, "", mAudioFile);
                                }
                            }
                        }, 1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onLessThanSecond() {
                binding.cardChatBoardMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startMediaRecording() throws IOException {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = new MediaRecorder();
        } else {
            mediaRecorder = new MediaRecorder();
        }


        File fileDir = new File(Environment.getExternalStorageDirectory(), "ParentalControll");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File mFileAudio = new File(fileDir, "ParentalControllAudio_" + System.currentTimeMillis() + ".m4a");

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncodingBitRate(192000);


        mAudioFile = mFileAudio.getAbsolutePath();
        mediaRecorder.setOutputFile(mFileAudio.getAbsolutePath());


        mediaRecorder.prepare();
        mediaRecorder.start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            List<com.esafirm.imagepicker.model.Image> images = ImagePicker.getImages(data);
            Log.e("IMAGE", images.get(0).getUri().toString());
            // or get a single image only
            Image image = ImagePicker.getFirstImageOrNull(data);
            apiCallSendMessage(2, "", images.get(0).getUri().toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class HandlerChatBoard {

        public void onBackPress(View view) {
            onBackPressed();
        }

        public void onSend(View view) {
            if (StringUtils.isNotEmpty(binding.edChatboardMessage.getText().toString())) {
                apiCallSendMessage(1, binding.edChatboardMessage.getText().toString(), "");
            }
        }

        public void onImageAttachmentClick(View view) {
            openCameraOrGallery();

        }

        public void onAudioClick(View view) {

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
        NotificationItem notificationItem = event.getNotificationItem();

        if (notificationItem != null) {
            if (notificationItem.getNotification_message_sender_id()
                    == mComplaintModal.getChat_receiver_id()) {

                ChatMessageModal chatMessageModal = new ChatMessageModal(
                        "",
                        notificationItem.getNotification_message_time(),
                        notificationItem.getNotification_message_text(),
                        notificationItem.getNotification_message_type(),
                        notificationItem.getNotification_message_file_url(),
                        String.valueOf(notificationItem.getNotification_message_sender_id()),
                        String.valueOf(notificationItem.getNotification_message_receiver_id()),
                        String.valueOf(notificationItem.getNotification_message_sender_type()),
                        String.valueOf(notificationItem.getNotification_message_receiver_type())
                );

                switch (chatMessageModal.getMessage_type()) {
                    case 1:
                        pushLocalTextMessage(
                                chatMessageModal.getMessage_text(),
                                chatMessageModal.getMessage_sender_type(),
                                chatMessageModal.getMessage_receiver_type());
                        break;
                    case 2:
                        pushLocalImageMessage(
                                chatMessageModal.getMessage_file_url(),
                                chatMessageModal.getMessage_sender_type(),
                                chatMessageModal.getMessage_receiver_type());
                    case 3:
                        pushLocalAudioMessage(
                                chatMessageModal.getMessage_file_url(),
                                chatMessageModal.getMessage_sender_type(),
                                chatMessageModal.getMessage_receiver_type());
                        break;
                }
            }
        }
    }


}