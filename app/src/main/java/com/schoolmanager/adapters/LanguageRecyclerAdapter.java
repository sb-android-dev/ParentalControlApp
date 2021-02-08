package com.schoolmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolmanager.R;
import com.schoolmanager.model.LanguageItem;

import java.util.List;

public class LanguageRecyclerAdapter extends RecyclerView.Adapter<LanguageRecyclerAdapter.LanguageViewHolder> {

    private Context context;
    private final List<LanguageItem> languages;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(LanguageItem item, int position);
    }

    public LanguageRecyclerAdapter(Context context, List<LanguageItem> languages, OnItemClickListener listener) {
        this.context = context;
        this.languages = languages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        holder.bindView(languages.get(position));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    class LanguageViewHolder extends RecyclerView.ViewHolder {
        TextView langName;
        ImageView checkedImage;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            langName = itemView.findViewById(R.id.tvLanguage);
            checkedImage = itemView.findViewById(R.id.ivChecked);
        }

        public void bindView(LanguageItem languageItem) {
            langName.setText(languageItem.getLangName());

            if (languageItem.isSelected()) {
                checkedImage.setVisibility(View.VISIBLE);
            } else {
                checkedImage.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(v -> listener.onClick(languageItem, getAbsoluteAdapterPosition()));
        }
    }
}
