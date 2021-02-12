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
import com.schoolmanager.model.ThemeItem;

import java.util.List;

public class ThemeRecyclerAdapter extends RecyclerView.Adapter<ThemeRecyclerAdapter.ThemeViewHolder> {

    private Context context;
    private final List<ThemeItem> themes;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(ThemeItem item, int position);
    }

    public ThemeRecyclerAdapter(Context context, List<ThemeItem> themes, OnItemClickListener listener) {
        this.context = context;
        this.themes = themes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_theme, parent, false);
        return new ThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        holder.bindView(themes.get(position));
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    class ThemeViewHolder extends RecyclerView.ViewHolder {
        TextView themeName;
        ImageView checkedImage;

        public ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            themeName = itemView.findViewById(R.id.tvTheme);
            checkedImage = itemView.findViewById(R.id.ivChecked);
        }

        public void bindView(ThemeItem themeItem) {
            themeName.setText(themeItem.getThemeName());

            if (themeItem.isSelected()) {
                checkedImage.setVisibility(View.VISIBLE);
            } else {
                checkedImage.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(v -> listener.onClick(themeItem, getAbsoluteAdapterPosition()));
        }
    }
}
