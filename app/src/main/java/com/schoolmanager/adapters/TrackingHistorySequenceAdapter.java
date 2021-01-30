package com.schoolmanager.adapters;

import android.content.Context;

import com.schoolmanager.model.TrackingHistoryItem;
import com.transferwise.sequencelayout.SequenceAdapter;
import com.transferwise.sequencelayout.SequenceStep;

import java.util.ArrayList;

public class TrackingHistorySequenceAdapter extends SequenceAdapter<TrackingHistoryItem> {

    private Context context;
    private ArrayList<TrackingHistoryItem> historyList;

    public TrackingHistorySequenceAdapter(Context context, ArrayList<TrackingHistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @Override
    public void bindView(SequenceStep sequenceStep, TrackingHistoryItem historyItem) {
        sequenceStep.setActive(historyItem.isActive());
        sequenceStep.setTitle(historyItem.getTitle());
        sequenceStep.setTitleTextAppearance(android.R.style.TextAppearance_Material_Large);
        sequenceStep.setAnchor(historyItem.getAnchor());
        sequenceStep.setAnchorTextAppearance(android.R.style.TextAppearance_Material_Small);
        sequenceStep.setSubtitle(historyItem.getSubTitle());
        sequenceStep.setSubtitleTextAppearance(android.R.style.TextAppearance_Material_Caption);
    }

    @Override
    public int getCount() {
        return historyList.size();
    }

    @Override
    public TrackingHistoryItem getItem(int i) {
        return historyList.get(i);
    }
}
