package com.schoolmanager.adapters;

import android.app.Activity;
import android.icu.util.Calendar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolmanager.R;
import com.schoolmanager.databinding.RawApntMessageTextBinding;
import com.schoolmanager.model.BroadCastMessageItem;
import com.schoolmanager.model.ChatMessageModal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class BroadcastMessageAdapter extends RecyclerView.Adapter<BroadcastMessageAdapter.BroadcastViewHolder> {

    private AppCompatActivity appCompatActivity;
    private ArrayList<BroadCastMessageItem> mListBroadCastMsg;

    public BroadcastMessageAdapter(AppCompatActivity appCompatActivity, ArrayList<BroadCastMessageItem> mListBroadCastMsg) {
        this.appCompatActivity = appCompatActivity;
        this.mListBroadCastMsg = mListBroadCastMsg;
    }

    @NonNull
    @Override
    public BroadcastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RawApntMessageTextBinding rawApntMessageTextBinding
                = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.raw_apnt_message_text,
                parent,
                false);
        return new BroadcastViewHolder(rawApntMessageTextBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BroadcastViewHolder holder, int position) {
        holder.rawApntMessageTextBinding.txtRowApntMessage.setText(mListBroadCastMsg.get(position).getBroadcast_message());
        holder.rawApntMessageTextBinding.txtRowApntTime.setText(getTimeOfMessage(mListBroadCastMsg.get(position).getBroadcast_time()));
        bindTopDate(position,
                holder.rawApntMessageTextBinding.topDateView.relTopDateView,
                holder.rawApntMessageTextBinding.topDateView.txtTopDateViewDate);
    }

    @Override
    public int getItemCount() {
        return mListBroadCastMsg.size();
    }

    public class BroadcastViewHolder extends RecyclerView.ViewHolder {
        RawApntMessageTextBinding rawApntMessageTextBinding;

        public BroadcastViewHolder(@NonNull RawApntMessageTextBinding rawApntMessageTextBinding) {
            super(rawApntMessageTextBinding.getRoot());
            this.rawApntMessageTextBinding = rawApntMessageTextBinding;
        }
    }

    private String getTimeOfMessage(String time) {

        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
//        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

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

    private void setTimeTextVisibility(long ts1, long ts2, CardView cardView, TextView timeText) {

        if (ts2 == 0) {
            cardView.setVisibility(View.VISIBLE);
            timeText.setText(formateDate(ts1*1000));
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
                timeText.setText(formateDate(ts2*1000));
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
        final BroadCastMessageItem m = mListBroadCastMsg.get(position);
        long previousTs = 0;
        if (position > 1) {
            BroadCastMessageItem cm = mListBroadCastMsg.get(position - 1);
            previousTs = Long.parseLong(cm.getBroadcast_time());
        }
        setTimeTextVisibility(Long.parseLong(m.getBroadcast_time()), previousTs, relTopDateView, txtTopDateViewDate);

    }

    public static Date dateToUTC(Date date) {
        return new Date(date.getTime() - Calendar.getInstance().getTimeZone().getOffset(date.getTime()));
    }

    public void addData(boolean clear, ArrayList<BroadCastMessageItem> mList) {
        if (clear) {
            this.mListBroadCastMsg.clear();
        }

        Collections.reverse(mList);

        int itemsInList = this.mListBroadCastMsg.size();
        this.mListBroadCastMsg.addAll(mList);
        if (clear) {
            notifyItemRangeChanged(itemsInList, mList.size());
        } else {
            notifyDataSetChanged();
        }

    }

    public void addMessageToList(BroadCastMessageItem mModal) {
        this.mListBroadCastMsg.add(mModal);
        int itemsInList = this.mListBroadCastMsg.size();
        notifyItemRangeChanged(itemsInList, 1);
    }

    public void clear() {
        this.mListBroadCastMsg.clear();
        notifyDataSetChanged();
    }
}
