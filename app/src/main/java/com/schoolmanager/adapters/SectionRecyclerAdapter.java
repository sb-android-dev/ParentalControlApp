package com.schoolmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolmanager.R;
import com.schoolmanager.model.SectionItem;

import java.util.ArrayList;
import java.util.List;

public class SectionRecyclerAdapter extends RecyclerView.Adapter<SectionRecyclerAdapter.SectionViewHolder> {

    private Context context;
    private List<SectionItem> sectionList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener{
        void onClick(SectionItem sectionItem, int position);
    }

    public SectionRecyclerAdapter(Context context, List<SectionItem> sectionList, OnItemClickListener listener) {
        this.context = context;
        this.sectionList = sectionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        holder.bindView(sectionList.get(position));
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    class SectionViewHolder extends RecyclerView.ViewHolder{
        TextView sectionName;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionName = itemView.findViewById(R.id.tvSection);
        }

        public void bindView(SectionItem sectionItem) {
            sectionName.setText(sectionItem.getSectionName());

            itemView.setOnClickListener(v -> listener.onClick(sectionItem, getAbsoluteAdapterPosition()));
        }
    }
}
