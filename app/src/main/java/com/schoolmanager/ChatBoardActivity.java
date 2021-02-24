package com.schoolmanager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.schoolmanager.adapters.ChatMessageAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.common.LogOutUser;
import com.schoolmanager.databinding.ActivityChatBoardBinding;
import com.schoolmanager.events.EventDeleteMessage;
import com.schoolmanager.events.EventNewMessageArrives;
import com.schoolmanager.events.EventReadMessage;
import com.schoolmanager.model.ChatMessageModal;
import com.schoolmanager.model.ComplaintItem;
import com.schoolmanager.model.NotificationItem;
import com.schoolmanager.services.TrackingService;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.IImageCompressTaskListener;
import com.schoolmanager.utilities.ImageCompressTask;
import com.schoolmanager.utilities.PathFinder;
import com.schoolmanager.utilities.TimeAgo;
import com.schoolmanager.utilities.UserSessionManager;
import com.vanniktech.emoji.EmojiPopup;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.schoolmanager.common.Common.LOG_OUT_SUCCESS;


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
    private ImageCompressTask imageCompressTask;
    private IImageCompressTaskListener compressTaskListener;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private EmojiPopup emojiPopup;

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

        Glide.with(this)
                .load(mComplaintModal.getChat_receiver_image())
                .placeholder(R.drawable.ic_person)
                .into(binding.imgChatBoardUser);

        chatMessageAdapter = new ChatMessageAdapter(this, new ArrayList<>(), mComplaintModal.getChat_read_permission());
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        binding.swipyChatboard.setColorSchemeResources(typedValue.resourceId);
        binding.swipyChatboard.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLastPage && !isNextPageCalled) {
                    if (detector.isConnectingToInternet())
                        currentPage++;
                    apiCallFetchMessages(currentPage);
                } else {
                    binding.swipyChatboard.setRefreshing(false);
                }
            }
        });


        binding.resViewChatBoard.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });

        binding.resViewChatBoard.setLayoutManager(mLayoutManager);
        binding.resViewChatBoard.setAdapter(chatMessageAdapter);

        emojiPopup = EmojiPopup.Builder.fromRootView(binding.llChatBoardMain).build(binding.edChatboardMessage);

        if (mComplaintModal.getChat_receiver_last_seen() != 0) {

            int offset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
            Date localDateLastSeen = new Date((mComplaintModal.getChat_receiver_last_seen() * 1000L) - (offset));

            binding.txtChatBoardLastSeen.setText(
                    String.format("%s %s",
                            getString(R.string.last_seen),
                            TimeAgo.getTimeAgo(localDateLastSeen.getTime())));
        } else {
            binding.txtChatBoardLastSeen.setText("");
        }

        setAudioRecordngButton();

        //Fetch message data
        apiCallFetchMessages(current_page);

        if (mComplaintModal.getChat_last_seen_permission() == 1) {
            binding.txtChatBoardLastSeen.setVisibility(View.VISIBLE);
        } else {
            binding.txtChatBoardLastSeen.setVisibility(View.GONE);
        }
    }

    private String getLastSeenTime(int time) {

        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        long time_sec = (long) time;
        long time_mili_sec = time_sec * 1000;
        calendar.setTimeInMillis(time_mili_sec);
        String str_fromated_time = formatter.format(calendar.getTime());
        Log.e("TIME ", time + "");
        Log.e("TIME", str_fromated_time);
        return str_fromated_time;
    }

    private void apiCallFetchMessages(int pageNumber) {

        if (!detector.isConnectingToInternet()) {

            Snackbar.make(binding.resViewChatBoard, "Looks like you're not connected with internet!",
                    Snackbar.LENGTH_LONG).show();
            isNextPageCalled = false;
            binding.swipyChatboard.setRefreshing(false);
            return;
        }

        if (!binding.swipyChatboard.isRefreshing()) {
            binding.pBarChatBoard.setVisibility(View.VISIBLE);
        }

        isNextPageCalled = true;

        Log.e("user_id", userId);
        Log.e("user_token", userToken);
        Log.e("user_type", userType);
        Log.e("user_app_code", Common.APP_CODE);
        Log.e("receiver_id", String.valueOf(mComplaintModal.getChat_receiver_id()));
        Log.e("receiver_type", String.valueOf(mComplaintModal.getChat_receiver_type()));
        Log.e("device_id", deviceId);
        Log.e("device_type", "1");
        Log.e("page_no", String.valueOf(pageNumber));

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
                        if (binding.swipyChatboard.isRefreshing()) {
                            binding.swipyChatboard.setRefreshing(false);
                        }

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


                                if (pageNumber == Common.PAGE_START) {
                                    scrollToBottomResView();
                                }

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
                        if (binding.swipyChatboard.isRefreshing()) {
                            binding.swipyChatboard.setRefreshing(false);
                        }
                    }
                });


    }

    private void apiCallSendMessage(int m_message_type, String message, String file_url, File image_file) {

        if (emojiPopup.isShowing()) {
            binding.emoji.setImageResource(R.drawable.ic_emoji);
            emojiPopup.dismiss();
        }

        if (!detector.isConnectingToInternet()) {

            Snackbar.make(binding.resViewChatBoard, "Looks like you're not connected with internet!",
                    Snackbar.LENGTH_LONG).show();
            return;
        }


        switch (m_message_type) {
            case 1:
//                Toast.makeText(this, "Test : Local message pushed", Toast.LENGTH_SHORT).show();
                pushLocalTextMessage("0", message, userType, String.valueOf(mComplaintModal.getChat_receiver_type()));
                break;
            case 2:
                pushLocalImageMessage("0", Uri.fromFile(image_file).toString(), userType, String.valueOf(mComplaintModal.getChat_receiver_type()));
                break;
            case 3:
                pushLocalAudioMessage("0", file_url, userType, String.valueOf(mComplaintModal.getChat_receiver_type()));
                break;
        }


        File file = (m_message_type == 3) ? new File(mAudioFile) : image_file;

        binding.edChatboardMessage.setText("");

//        Toast.makeText(this, "Test : Api call init", Toast.LENGTH_SHORT).show();


        String toastText =
                "message_file: " + file +
                        "\nuser_id: " + userId +
                        "\nuser_token: " + userToken +
                        "\nuser_type: " + userType +
                        "\nmessage_type: " + String.valueOf(m_message_type) +
                        "\nmessage_text: " + message +
                        "\nreceiver_id: " + String.valueOf(mComplaintModal.getChat_receiver_id()) +
                        "\nreceiver_type: " + String.valueOf(mComplaintModal.getChat_receiver_type()) +
                        "\nuser_app_code: " + Common.APP_CODE +
                        "\ndevice_id: " + deviceId +
                        "\ndevice_type: " + "1";

//        Toast.makeText(this, "Test : Api call params\n" + toastText, Toast.LENGTH_LONG).show();

        AndroidNetworking.upload(Common.BASE_URL + "app-send-message")
                .setPriority(Priority.HIGH)
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
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        if (bytesUploaded == totalBytes) {
                            EventBus.getDefault().post(new EventNewMessageArrives(null, true));
                        }
                    }


                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
//                            Toast.makeText(ChatBoardActivity.this, "Test : Api call response\n" + response.toString(), Toast.LENGTH_LONG).show();
                            Log.e("CHAT_RESPONSE", response.toString());
                            binding.edChatboardMessage.setText("");
                            try {
                                int success = response.getInt("success");
                                String message = response.getString("message");

                                if (success == 1) {
                                    JSONObject data = response.getJSONObject("data");

                                    ChatMessageModal chatMessageModal = new Gson().fromJson(data.toString(), ChatMessageModal.class);
                                    chatMessageAdapter.replaceMyLastMessage(chatMessageModal);

//                                    Toast.makeText(ChatBoardActivity.this, "Test : Api call success\n" + message, Toast.LENGTH_LONG).show();

                                } else {
//                                    Toast.makeText(ChatBoardActivity.this, "Test : Api call false\n" + message, Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
//                                Toast.makeText(ChatBoardActivity.this, "Test : Api call catch one\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            Toast.makeText(ChatBoardActivity.this, "Test : Api call catch two\n" + e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        Log.e("ERROR", anError.getErrorDetail() + "");
                        Log.e("ERROR", anError.getMessage() + "");
//                        Toast.makeText(ChatBoardActivity.this, "Test : Api call onError\n" + anError.getErrorDetail(), Toast.LENGTH_LONG).show();
//                        Toast.makeText(ChatBoardActivity.this, "Test : Api call body\n" + anError.getErrorBody(), Toast.LENGTH_LONG).show();
//                        Toast.makeText(ChatBoardActivity.this, "Test : Api call detail\n" + anError.getErrorDetail(), Toast.LENGTH_LONG).show();

                    }

                });
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void pushLocalTextMessage(String message_id, String message, String sender_type, String receiver_type) {

        long currentTime = System.currentTimeMillis() / 1000;
        currentTime = currentTime + 19800;

        ChatMessageModal messageModal = new ChatMessageModal(
                message_id,
                String.valueOf(currentTime),
                message,
                1,
                "",
                current_usr_id,
                String.valueOf(mComplaintModal.getChat_receiver_id()),
                sender_type,
                receiver_type,
                0);

        chatMessageAdapter.addMessageToList(messageModal);
        scrollToBottomResView();
    }

    private void pushLocalImageMessage(String message_id, String file_url, String sender_type, String receiver_type) {
        long currentTime = System.currentTimeMillis() / 1000;
        currentTime = currentTime + 19800;
        ChatMessageModal messageModal = new ChatMessageModal(
                message_id,
                String.valueOf(currentTime),
                "",
                2,
                file_url,
                current_usr_id,
                String.valueOf(mComplaintModal.getChat_receiver_id()),
                sender_type,
                receiver_type,
                0);

        chatMessageAdapter.addMessageToList(messageModal);
        scrollToBottomResView();
    }

    private void pushLocalAudioMessage(String message_id, String file_url, String sender_type, String receiver_type) {
        long currentTime = System.currentTimeMillis() / 1000;
        currentTime = currentTime + 19800;

        ChatMessageModal messageModal = new ChatMessageModal(
                message_id,
                String.valueOf(currentTime),
                "",
                3,
                file_url,
                current_usr_id,
                String.valueOf(mComplaintModal.getChat_receiver_id()),
                sender_type,
                receiver_type,
                0);

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

//                            ImagePicker.create(ChatBoardActivity.this)
//                                    .limit(1)
//                                    .theme(R.style.ImagePickerTheme)
//                                    .start();

                            Intent intentPick = new Intent(Intent.ACTION_PICK);
                            // Sets the type as image/*. This ensures only components of type image are selected
                            intentPick.setType("image/*");
                            //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                            String[] mimeTypes = {"image/jpeg", "image/png"};
                            intentPick.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                            startActivityForResult(intentPick, Common.REQUEST_IMAGE_PICKER);
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
                }, 1000);


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
                                Log.e("RECORDTIME==>", recordTime + "");
                                if (recordTime >= 2000) {
                                    apiCallSendMessage(3, "", mAudioFile, null);
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
       /* if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            List<com.esafirm.imagepicker.model.Image> images = ImagePicker.getImages(data);
            Log.e("IMAGE", images.get(0).getUri().toString());
            // or get a single image only
            Image image = ImagePicker.getFirstImageOrNull(data);
            apiCallSendMessage(2, "", images.get(0).getUri().toString());
        }*/
        if (requestCode == Common.REQUEST_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
//            apiCallSendMessage(2, "", selectedImageUri.toString());
            Log.e(TAG, "onActivityResult: selectedImageUri -> " + selectedImageUri);

            imageCompressTask = new ImageCompressTask(this,
                    new PathFinder(ChatBoardActivity.this).getPath(selectedImageUri), 250, 250,
                    new IImageCompressTaskListener() {
                        @Override
                        public void onComplete(List<File> compressed) {
                            if (compressed.get(0) != null) {
                                File imageFile = compressed.get(0);
                                Log.d("ImageCompressor", "New photo size ==> " + imageFile.length());

                                apiCallSendMessage(2, "", "", imageFile);

                            } else {
                                Log.e("ImageCompressor", "onComplete: received result is null");

                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            Log.e(TAG, "onError: " + error.getLocalizedMessage());

                        }
                    });
            mExecutorService.execute(imageCompressTask);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class HandlerChatBoard {

        public void onBackPress(View view) {
            onBackPressed();
        }

        public void onSend(View view) {
            if (StringUtils.isNotEmpty(binding.edChatboardMessage.getText().toString())) {
//                Toast.makeText(ChatBoardActivity.this, "Test : Send message", Toast.LENGTH_SHORT).show();
                apiCallSendMessage(1, binding.edChatboardMessage.getText().toString(), "", null);
            }
        }

        public void onImageAttachmentClick(View view) {
            openCameraOrGallery();

        }

        public void onAudioClick(View view) {

        }

        public void onClickEmoji(View view) {
            if (emojiPopup.isShowing()) {
                binding.emoji.setImageResource(R.drawable.ic_emoji);
                emojiPopup.dismiss();
            } else {
                binding.emoji.setImageResource(R.drawable.ic_keyboard);
                emojiPopup.toggle();
            }
        }
    }

    public void onLogOut() {
        LogOutUser.getInstance(this, status -> {
            if(status == LOG_OUT_SUCCESS){
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
        }).performLogOut();
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
                        String.valueOf(notificationItem.getNotification_message_id()),
                        notificationItem.getNotification_message_time(),
                        notificationItem.getNotification_message_text(),
                        notificationItem.getNotification_message_type(),
                        notificationItem.getNotification_message_file_url(),
                        String.valueOf(notificationItem.getNotification_message_sender_id()),
                        String.valueOf(notificationItem.getNotification_message_receiver_id()),
                        String.valueOf(notificationItem.getNotification_message_sender_type()),
                        String.valueOf(notificationItem.getNotification_message_receiver_type()),
                        0
                );

                switch (chatMessageModal.getMessage_type()) {
                    case 1:
                        pushLocalTextMessage(
                                chatMessageModal.getMessage_id(),
                                chatMessageModal.getMessage_text(),
                                chatMessageModal.getMessage_sender_type(),
                                chatMessageModal.getMessage_receiver_type());
                        break;
                    case 2:
                        pushLocalImageMessage(
                                chatMessageModal.getMessage_id(),
                                chatMessageModal.getMessage_file_url(),
                                chatMessageModal.getMessage_sender_type(),
                                chatMessageModal.getMessage_receiver_type());
                    case 3:
                        pushLocalAudioMessage(
                                chatMessageModal.getMessage_id(),
                                chatMessageModal.getMessage_file_url(),
                                chatMessageModal.getMessage_sender_type(),
                                chatMessageModal.getMessage_receiver_type());
                        break;
                }

                apiCallReadMessage(notificationItem.getNotification_message_id());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageRead(EventReadMessage readMessage) {
        chatMessageAdapter.readMessage(readMessage.getMessage_id());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageDelete(EventDeleteMessage deleteMesage) {
        chatMessageAdapter.deleteThisMessage(deleteMesage.getMessgae_id());
    }

    private void apiCallReadMessage(int message_id) {
        AndroidNetworking.upload(Common.BASE_URL + "app-read-message")
                .addMultipartParameter("user_id", userId)
                .addMultipartParameter("user_token", userToken)
                .addMultipartParameter("user_type", userType)
                .addMultipartParameter("message_id", String.valueOf(message_id))
                .addMultipartParameter("sender_id", String.valueOf(mComplaintModal.getChat_receiver_id()))
                .addMultipartParameter("sender_type", String.valueOf(mComplaintModal.getChat_receiver_type()))
                .addMultipartParameter("user_app_code", Common.APP_CODE)
                .addMultipartParameter("device_id", deviceId)
                .addMultipartParameter("device_type", "1")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("MESSGAE_READ", response.toString());
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

}