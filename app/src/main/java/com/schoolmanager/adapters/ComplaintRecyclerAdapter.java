package com.schoolmanager.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.schoolmanager.ChatBoardActivity;
import com.schoolmanager.R;
import com.schoolmanager.databinding.RecyclerComplaintsBinding;
import com.schoolmanager.model.ComplaintItem;
import com.schoolmanager.utilities.TimeAgo;

import java.util.ArrayList;

public class ComplaintRecyclerAdapter extends RecyclerView.Adapter<ComplaintRecyclerAdapter.MyViewHolder> {

    private Activity activity;
    private ArrayList<ComplaintItem> mList;

    public ComplaintRecyclerAdapter(Activity activity, ArrayList<ComplaintItem> mList) {
        this.activity = activity;
        this.mList = mList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerComplaintsBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.recycler_complaints,
                parent,
                false
        );
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        ComplaintItem modal = mList.get(position);
        RecyclerComplaintsBinding binding = holder.binding;

        String ago = TimeAgo.getTimeAgo((Long.parseLong(modal.getChat_last_message_time())*1000L));

        holder.binding.txtRawChatListLastMessage.setText(modal.getChat_last_message());
        holder.binding.txtRawChatListTime.setText(ago);
        holder.binding.txtRawChatListName.setText(modal.getChat_receiver_name());


        Glide.with(activity)
                .load(modal.getChat_receiver_image())
                .placeholder(R.drawable.ic_person)
                .into(binding.imgRawChatListPic);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(activity, ChatBoardActivity.class)
                        .putExtra("complaint_data", new Gson().toJson(modal)));
            }
        });

        if(modal.getChat_unread_count() > 0){
            holder.binding.txtRawChatListCount.setText(String.valueOf(modal.getChat_unread_count()));
            holder.binding.txtRawChatListCount.setVisibility(View.VISIBLE);
            holder.binding.txtRawChatListTime.setTextColor(ContextCompat.getColor(activity,R.color.colorPrimary));
        }else {
            holder.binding.txtRawChatListCount.setVisibility(View.INVISIBLE);
            holder.binding.txtRawChatListTime.setTextColor(ContextCompat.getColor(activity,R.color.light_gray));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        RecyclerComplaintsBinding binding;

        public MyViewHolder(@NonNull RecyclerComplaintsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
