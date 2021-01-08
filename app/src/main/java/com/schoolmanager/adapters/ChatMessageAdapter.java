package com.schoolmanager.adapters;

import android.app.Activity;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.schoolmanager.R;
import com.schoolmanager.ZoomImage;
import com.schoolmanager.databinding.RawApntMessageAudioBinding;
import com.schoolmanager.databinding.RawApntMessageImageBinding;
import com.schoolmanager.databinding.RawApntMessageTextBinding;
import com.schoolmanager.databinding.RawSelfMessageAudioBinding;
import com.schoolmanager.databinding.RawSelfMessageImageBinding;
import com.schoolmanager.databinding.RawSelfMessageTextBinding;
import com.schoolmanager.model.ChatMessageModal;
import com.schoolmanager.utilities.UserSessionManager;
import com.schoolmanager.view.MessageFileDownloadProgressbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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


    public ChatMessageAdapter(Activity activity, ArrayList<ChatMessageModal> mList) {
        this.activity = activity;
        this.mList = mList;

        UserSessionManager sessionManager = new UserSessionManager(activity);
        HashMap<String, String> hashMap = sessionManager.getEssentials();
        user_id = hashMap.get(UserSessionManager.KEY_USER_ID);
        userType = hashMap.get(UserSessionManager.KEY_USER_TYPE);
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
    }

    private void bindApntText(ApntTextViewHolder holder, int position) {
        holder.binding.txtRowApntMessage.setText(mList.get(position).getMessage_text());
        holder.binding.txtRowApntTime.setText(getTimeOfMessage(mList.get(position).getMessage_time()));
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
    }

    private String getTimeOfMessage(String time) {

        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        long time_sec = Long.parseLong(time);
        long time_mili_sec = time_sec * 1000;
        calendar.setTimeInMillis(time_mili_sec);
        String str_fromated_time = formatter.format(calendar.getTime());
        Log.e("TIME ", time);
        Log.e("TIME", str_fromated_time);
        return str_fromated_time;

      /*
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        Date date = new Date();
        date.setTime(Long.parseLong(time));
        String str_fromated_time = simpleDateFormat.format(date);
        Log.e("TIME ", time);
        Log.e("TIME", str_fromated_time);
        return str_fromated_time;*/
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

    public void addMessageToList(ChatMessageModal mModal) {
        this.mList.add(mModal);
        int itemsInList = this.mList.size();
        notifyItemRangeChanged(itemsInList, 1);
    }

    private void fullScreenImage(String image) {
        activity.startActivity(new Intent(activity, ZoomImage.class)
                .putExtra("image", image));
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
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


}
