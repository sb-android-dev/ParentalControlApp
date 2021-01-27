package com.schoolmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.schoolmanager.R;
import com.schoolmanager.model.NoticeItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeRecyclerAdapter extends RecyclerView.Adapter<NoticeRecyclerAdapter.NoticeViewHolder> {

    private Context context;
    private final List<NoticeItem> noticeList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public NoticeRecyclerAdapter(Context context, List<NoticeItem> noticeList) {
        this.context = context;
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        holder.bindView(noticeList.get(position));
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    class NoticeViewHolder extends RecyclerView.ViewHolder{
        TextView noticeName, noticeTime, noticeDetail;
        ImageView noticeImage;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            noticeName = itemView.findViewById(R.id.tvNoticeName);
            noticeTime = itemView.findViewById(R.id.tvNoticeTime);
            noticeDetail = itemView.findViewById(R.id.tvNoticeDetail);
            noticeImage = itemView.findViewById(R.id.ivNoticeImage);
        }

        public void bindView(NoticeItem noticeItem) {
            noticeName.setText(noticeItem.getNoticeName());

            String t = sdf.format(new Date(noticeItem.getNoticeTime()));
            noticeTime.setText(t);

            if(noticeItem.getNoticeDetail() != null && !noticeItem.getNoticeDetail().isEmpty()
                    && !noticeItem.getNoticeDetail().equals("null")) {
                noticeDetail.setText(noticeItem.getNoticeDetail());
                noticeDetail.setVisibility(View.VISIBLE);
            }else {
                noticeDetail.setVisibility(View.GONE);
            }

            if((noticeItem.getNoticeThumbImage() != null || noticeItem.getNoticeMainImage() != null)
                    && (!noticeItem.getNoticeThumbImage().isEmpty() || !noticeItem.getNoticeMainImage().isEmpty())
                    && (!noticeItem.getNoticeThumbImage().equals("null") || !noticeItem.getNoticeMainImage().equals("null"))){
                Glide.with(context)
                        .load(noticeItem.getNoticeMainImage())
                        .thumbnail(Glide.with(context).load(noticeItem.getNoticeThumbImage()))
                        .into(noticeImage);
                noticeImage.setVisibility(View.VISIBLE);
            } else {
                noticeImage.setVisibility(View.GONE);
            }

        }
    }
}
