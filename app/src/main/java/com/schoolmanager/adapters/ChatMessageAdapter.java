package com.schoolmanager.adapters;

import android.app.Activity;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.utils.Utils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.schoolmanager.R;
import com.schoolmanager.ZoomImage;
import com.schoolmanager.common.Common;
import com.schoolmanager.databinding.RawApntMessageAudioBinding;
import com.schoolmanager.databinding.RawApntMessageImageBinding;
import com.schoolmanager.databinding.RawApntMessageTextBinding;
import com.schoolmanager.databinding.RawSelfMessageAudioBinding;
import com.schoolmanager.databinding.RawSelfMessageImageBinding;
import com.schoolmanager.databinding.RawSelfMessageTextBinding;
import com.schoolmanager.databinding.TopDateViewBinding;
import com.schoolmanager.model.ChatMessageModal;
import com.schoolmanager.utilities.UserSessionManager;
import com.schoolmanager.view.MessageFileDownloadProgressbar;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TYPE_SELF = 0;
    private final int TYPE_SELF_IMAGE = 1;
    private final int TYPE_SELF_AUDIO = 2;
    private final int TYPE_APONENT = 3;
    private final int TYPE_APONENT_IMAGE = 4;
    private final int TYPE_APONENT_AUDIO = 5;

    private Activity activity;
    private ArrayList<ChatMessageModal> mList;

    private String user_id = "", userType = ""; //get it from preferance
    private int chat_read_permission = 0;


    public ChatMessageAdapter(Activity activity, ArrayList<ChatMessageModal> mList, int chat_read_permission) {
        this.activity = activity;
        this.mList = mList;

        UserSessionManager sessionManager = new UserSessionManager(activity);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        user_id = hashMap.get(UserSessionManager.KEY_USER_ID);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        this.chat_read_permission = chat_read_permission;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_SELF:
                RawSelfMessageTextBinding binding_self_text = DataBindingUtil.inflate(layoutInflater, R.layout.raw_self_message_text, parent, false);
                viewHolder = new SelfTextViewHolder(binding_self_text);
                break;
            case TYPE_SELF_IMAGE:
                RawSelfMessageImageBinding binding_self_img = DataBindingUtil.inflate(layoutInflater, R.layout.raw_self_message_image, parent, false);
                viewHolder = new SelfImageViewHolder(binding_self_img);
                break;
            case TYPE_SELF_AUDIO:
                RawSelfMessageAudioBinding binding_self_audio = DataBindingUtil.inflate(layoutInflater, R.layout.raw_self_message_audio, parent, false);
                viewHolder = new SelfAudioViewHolder(binding_self_audio);
                break;
            case TYPE_APONENT:
                RawApntMessageTextBinding binding_apnt_text = DataBindingUtil.inflate(layoutInflater, R.layout.raw_apnt_message_text, parent, false);
                viewHolder = new ApntTextViewHolder(binding_apnt_text);
                break;
            case TYPE_APONENT_IMAGE:
                RawApntMessageImageBinding binding_apnt_img = DataBindingUtil.inflate(layoutInflater, R.layout.raw_apnt_message_image, parent, false);
                viewHolder = new ApntImageViewHolder(binding_apnt_img);
                break;
            case TYPE_APONENT_AUDIO:
                RawApntMessageAudioBinding binding_apnt_audio = DataBindingUtil.inflate(layoutInflater, R.layout.raw_apnt_message_audio, parent, false);
                viewHolder = new ApntAudioViewHolder(binding_apnt_audio);
                break;
        }

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SelfTextViewHolder) {
            bindSelfText((SelfTextViewHolder) holder, position);
        }
        if (holder instanceof SelfImageViewHolder) {
            bindSelfImage((SelfImageViewHolder) holder, position);
        }
        if (holder instanceof SelfAudioViewHolder) {
            bindSelfAudio((SelfAudioViewHolder) holder, position);
        }
        if (holder instanceof ApntTextViewHolder) {
            bindApntText((ApntTextViewHolder) holder, position);
        }
        if (holder instanceof ApntImageViewHolder) {
            bindApntImage((ApntImageViewHolder) holder, position);
        }
        if (holder instanceof ApntAudioViewHolder) {
            bindApntAudio((ApntAudioViewHolder) holder, position);
        }
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {

        ChatMessageModal messageModal = mList.get(position);
        int msg_type = messageModal.getMessage_type();

        boolean is_self = messageModal.getMessage_sender_type().equals(userType);

        switch (msg_type) {
            case 2:
                return is_self ? TYPE_SELF_IMAGE : TYPE_APONENT_IMAGE;
            case 1:
                return is_self ? TYPE_SELF : TYPE_APONENT;
            case 3:
                return is_self ? TYPE_SELF_AUDIO : TYPE_APONENT_AUDIO;
            default:
                return is_self ? TYPE_SELF : TYPE_APONENT;
        }
    }

    private void bindSelfText(SelfTextViewHolder holder, int position) {
        holder.binding.txtRowSelfMessage.setText(mList.get(position).getMessage_text());
        holder.binding.txtRowSelfTime.setText(getTimeOfMessage(mList.get(position).getMessage_time()));
        deleteMessage(holder.binding.getRoot(), holder.binding.txtRowSelfMessage, mList.get(position), position);
        showSingleAndDoubleTik(mList.get(position), holder.binding.imgRowSelfTick);
        bindTopDate(position,
                holder.binding.topDateView.relTopDateView,
                holder.binding.topDateView.txtTopDateViewDate);
    }


    private void bindSelfImage(SelfImageViewHolder holder, int position) {
        Glide.with(activity).load(mList.get(position).getMessage_file_url()).into(holder.binding.imgRowSelfImageImg);
        holder.binding.txtRowSelfImageTime.setText(getTimeOfMessage(mList.get(position).getMessage_time()));
        holder.binding.imgRowSelfImageImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenImage(mList.get(position).getMessage_file_url());
            }
        });
        deleteMessage(holder.binding.getRoot(), holder.binding.imgRowSelfImageImg, mList.get(position), position);
        showSingleAndDoubleTik(mList.get(position), holder.binding.imgRowSelfImageTick);

        bindTopDate(position,
                holder.binding.topDateView.relTopDateView,
                holder.binding.topDateView.txtTopDateViewDate);
    }

    private void bindSelfAudio(SelfAudioViewHolder holder, int position) {

        File filedir = new File(Environment.getExternalStorageDirectory(), "ParentalControll");

        String file_url = mList.get(position).getMessage_file_url();
        String fileName = file_url.substring(file_url.lastIndexOf('/') + 1, file_url.length());
        String fileNameWithoutExtn = fileName.substring(0, fileName.lastIndexOf('.'));
        File fileDecrypted = new File(filedir, fileName);

        if (fileDecrypted.exists()) {
            holder.binding.apRawSelfAudioAudio.setVisibility(View.VISIBLE);
            holder.binding.llRowMessageAponentAudioAudioMessage.setVisibility(View.INVISIBLE);
        } else {
            holder.binding.apRawSelfAudioAudio.setVisibility(View.INVISIBLE);
            holder.binding.llRowMessageAponentAudioAudioMessage.setVisibility(View.VISIBLE);
        }

        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setActivity(activity);
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setFileType("audio");
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setFileName(fileName);
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setFile_url(file_url);
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setUp();
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setListener(new MessageFileDownloadProgressbar.OnDownloadCompleteListener() {
            @Override
            public void onDosnloadComplete() {
                notifyItemChanged(holder.getAdapterPosition());
            }
        });


        holder.binding.apRawSelfAudioAudio.setAudioTarget(fileDecrypted.getAbsolutePath());
        holder.binding.apRawSelfAudioAudio.commitClickEvents();
        holder.binding.txtRowSelfTime.setText(getTimeOfMessage(mList.get(position).getMessage_time()));
        deleteMessage(holder.binding.getRoot(), holder.binding.txtRowSelfTime, mList.get(position), position);
        showSingleAndDoubleTik(mList.get(position), holder.binding.imgRowSelfImageTick);
        bindTopDate(position,
                holder.binding.topDateView.relTopDateView,
                holder.binding.topDateView.txtTopDateViewDate);
    }

    private void bindApntText(ApntTextViewHolder holder, int position) {
        holder.binding.txtRowApntMessage.setText(mList.get(position).getMessage_text());
        holder.binding.txtRowApntTime.setText(getTimeOfMessage(mList.get(position).getMessage_time()));
        bindTopDate(position,
                holder.binding.topDateView.relTopDateView,
                holder.binding.topDateView.txtTopDateViewDate);
    }

    private void bindApntImage(ApntImageViewHolder holder, int position) {
        Glide.with(activity).load(mList.get(position).getMessage_file_url()).into(holder.binding.imgRowApntfImageImg);
        holder.binding.txtRowApntImageTime.setText(getTimeOfMessage(mList.get(position).getMessage_time()));
        holder.binding.imgRowApntfImageImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenImage(mList.get(position).getMessage_file_url());
            }
        });
        bindTopDate(position,
                holder.binding.topDateView.relTopDateView,
                holder.binding.topDateView.txtTopDateViewDate);
    }

    private void bindApntAudio(ApntAudioViewHolder holder, int position) {
        File filedir = new File(Environment.getExternalStorageDirectory(), "ParentalControll");

        String file_url = mList.get(position).getMessage_file_url();
        String fileName = file_url.substring(file_url.lastIndexOf('/') + 1, file_url.length());
        String fileNameWithoutExtn = fileName.substring(0, fileName.lastIndexOf('.'));
        File fileDecrypted = new File(filedir, fileName);

        if (fileDecrypted.exists()) {
            holder.binding.apRawApntAudioAudio.setVisibility(View.VISIBLE);
            holder.binding.llRowMessageAponentAudioAudioMessage.setVisibility(View.INVISIBLE);
        } else {
            holder.binding.apRawApntAudioAudio.setVisibility(View.INVISIBLE);
            holder.binding.llRowMessageAponentAudioAudioMessage.setVisibility(View.VISIBLE);
        }

        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setActivity(activity);
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setFileType("audio");
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setFileName(fileName);
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setFile_url(file_url);
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setUp();
        holder.binding.mfdpRowMessageAponentAudioAudioMessage.setListener(new MessageFileDownloadProgressbar.OnDownloadCompleteListener() {
            @Override
            public void onDosnloadComplete() {
                notifyItemChanged(holder.getAdapterPosition());
            }
        });


        holder.binding.apRawApntAudioAudio.setAudioTarget(fileDecrypted.getAbsolutePath());
        holder.binding.apRawApntAudioAudio.commitClickEvents();
        holder.binding.txtRawApntAudioTime.setText(getTimeOfMessage(mList.get(position).getMessage_time()));
        bindTopDate(position,
                holder.binding.topDateView.relTopDateView,
                holder.binding.topDateView.txtTopDateViewDate);
    }

    private String getTimeOfMessage(String time) {

        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        long time_sec = Long.parseLong(time);
        long time_mili_sec = time_sec * 1000;
        calendar.setTimeInMillis(time_mili_sec);
        String str_fromated_time = formatter.format(calendar.getTime());
        Log.e("TIME ", time);
        Log.e("TIME", str_fromated_time);
        return str_fromated_time;
    }

    public void addData(boolean clear, ArrayList<ChatMessageModal> mList) {
        if (clear) {
            this.mList.clear();
        }

        Collections.reverse(mList);

        int itemsInList = this.mList.size();
        this.mList.addAll(mList);
        if (clear) {
            notifyItemRangeChanged(itemsInList, mList.size());
        } else {
            notifyDataSetChanged();
        }

    }

    public void replaceMyLastMessage(ChatMessageModal modal) {
        try {
            ChatMessageModal lastMessageModal = mList.get(mList.size() - 1);
            if (lastMessageModal != null) {
                if (StringUtils.isNotEmpty(lastMessageModal.getMessage_id())) {
                    mList.set(mList.size() - 1, modal);
                }
            }
            notifyItemChanged(mList.size() - 1);
            Log.e("MESSAGE_REPLACED", new Gson().toJson(mList.get(mList.size() - 1)));
        } catch (Exception e) {
            e.getMessage();
        }

    }

    public void addMessageToList(ChatMessageModal mModal) {
        this.mList.add(mModal);
        int itemsInList = this.mList.size();
        notifyItemRangeChanged(itemsInList, 1);
    }

    public void readMessage(String messageId) {
        int index = 0;
        for (ChatMessageModal chatMessageModal : mList) {
            if (chatMessageModal.getMessage_id().equals(messageId)) {
                chatMessageModal.setMessage_is_read(1);
                notifyItemChanged(index);
                break;
            }
            index += 1;
        }
    }

    public void deleteThisMessage(String message_id) {
        int index = 0;
        int index_of_delete_mesage = 0;

        for (ChatMessageModal modal : mList) {
            if (modal.getMessage_id().equals(message_id)) {
                index_of_delete_mesage = index;
                break;
            }
            index = index + 1;
        }

        mList.remove(index_of_delete_mesage);
        notifyItemRemoved(index_of_delete_mesage);
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    private void fullScreenImage(String image) {
        activity.startActivity(new Intent(activity, ZoomImage.class)
                .putExtra("image", image));
    }

    class SelfTextViewHolder extends RecyclerView.ViewHolder {

        RawSelfMessageTextBinding binding;


        public SelfTextViewHolder(@NonNull RawSelfMessageTextBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class SelfImageViewHolder extends RecyclerView.ViewHolder {

        RawSelfMessageImageBinding binding;

        public SelfImageViewHolder(@NonNull RawSelfMessageImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class SelfAudioViewHolder extends RecyclerView.ViewHolder {

        RawSelfMessageAudioBinding binding;

        public SelfAudioViewHolder(@NonNull RawSelfMessageAudioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class ApntTextViewHolder extends RecyclerView.ViewHolder {

        RawApntMessageTextBinding binding;

        public ApntTextViewHolder(@NonNull RawApntMessageTextBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class ApntImageViewHolder extends RecyclerView.ViewHolder {

        RawApntMessageImageBinding binding;

        public ApntImageViewHolder(@NonNull RawApntMessageImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class ApntAudioViewHolder extends RecyclerView.ViewHolder {

        RawApntMessageAudioBinding binding;

        public ApntAudioViewHolder(@NonNull RawApntMessageAudioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void showSingleAndDoubleTik(ChatMessageModal modal, ImageView tikImageView) {
        if (chat_read_permission == 1) {
            tikImageView.setVisibility(View.VISIBLE);
        } else {
            tikImageView.setVisibility(View.INVISIBLE);
        }
        if (modal.getMessage_is_read() == 0) {
            tikImageView.setImageResource(R.drawable.single_tick);
        } else {
            tikImageView.setImageResource(R.drawable.double_tick);
        }
    }

    private void deleteMessage(View view, View viewForPopup, ChatMessageModal messageModal, int position) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                PopupMenu menu = new PopupMenu(activity, viewForPopup);
                menu.inflate(R.menu.message_menu);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        apiCallDeleteMessage(messageModal, position);
                        return false;
                    }
                });

                MenuPopupHelper menuHelper = new MenuPopupHelper(activity, (MenuBuilder) menu.getMenu(), viewForPopup);
                menuHelper.setForceShowIcon(true);
                menuHelper.setGravity(Gravity.END);
                menuHelper.show();

                return false;
            }
        });
    }

    private void apiCallDeleteMessage(ChatMessageModal messageModal, int position) {

        UserSessionManager sessionManager = new UserSessionManager(activity.getBaseContext());
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        String userId = hashMap.get(UserSessionManager.KEY_USER_ID);
        String userToken = hashMap.get(UserSessionManager.KEY_USER_TOKEN);
        String userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
        String deviceId = hashMap.get(UserSessionManager.KEY_DEVICE_ID);
        String fcmToken = sessionManager.getFcmToken();

        AndroidNetworking.post(Common.BASE_URL + "app-delete-message")
                .addBodyParameter("user_id", userId)
                .addBodyParameter("user_token", userToken)
                .addBodyParameter("user_type", userType)
                .addBodyParameter("user_app_code", Common.APP_CODE)
                .addBodyParameter("receiver_id", String.valueOf(messageModal.getMessage_receiver_id()))
                .addBodyParameter("receiver_type", String.valueOf(messageModal.getMessage_receiver_type()))
                .addBodyParameter("device_id", deviceId)
                .addBodyParameter("device_type", "1")
                .addBodyParameter("message_id", messageModal.getMessage_id())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int success = response.getInt("success");
                            String message = response.getString("message");
                            Log.e("DELETE_MESSAGE==>", message);
                            mList.remove(messageModal);
                            notifyItemRemoved(position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void setTimeTextVisibility(long ts1, long ts2, CardView cardView, TextView timeText) {

        if (ts2 == 0) {
            cardView.setVisibility(View.VISIBLE);
            timeText.setText(formateDate(ts1 * 1000));
        } else {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTimeInMillis(ts1);
            cal2.setTimeInMillis(ts2);

            boolean sameMonth = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);

            if (sameMonth) {
                timeText.setVisibility(View.GONE);
                timeText.setText("");
            } else {
                timeText.setVisibility(View.VISIBLE);
                timeText.setText(formateDate(ts2 * 1000));
            }

        }
    }

    private String formateDate(long milis) {
        // New date object from millis
        Date date = new Date(milis);
        // formattter
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, EEEE");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Pass date object
        String formatted = formatter.format(date);

        return formatted;
    }

    private void bindTopDate(int position, CardView relTopDateView, TextView txtTopDateViewDate) {
        final ChatMessageModal m = mList.get(position);
        long previousTs = 0;
        if (position > 1) {
            ChatMessageModal cm = mList.get(position - 1);
            previousTs = Long.parseLong(cm.getMessage_time());
        }
        setTimeTextVisibility(Long.parseLong(m.getMessage_time()), previousTs, relTopDateView, txtTopDateViewDate);

    }
}
