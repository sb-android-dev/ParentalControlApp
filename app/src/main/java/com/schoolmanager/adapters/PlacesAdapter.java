package com.schoolmanager.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolmanager.BaseActivity;
import com.schoolmanager.R;
import com.schoolmanager.databinding.RecyclerPlacesBinding;
import com.schoolmanager.model.PlacesItem;

import java.util.ArrayList;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder> {


    private BaseActivity activity;
    private ArrayList<PlacesItem> mList;
    private OnClickPlaceItem onClickPlaceItem;

    public PlacesAdapter(BaseActivity activity, ArrayList<PlacesItem> mList, OnClickPlaceItem onClickPlaceItem) {
        this.activity = activity;
        this.mList = mList;
        this.onClickPlaceItem = onClickPlaceItem;
    }

    @NonNull
    @Override
    public PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerPlacesBinding binding = DataBindingUtil.inflate(
                activity.getLayoutInflater(),
                R.layout.recycler_places,
                parent,
                false
        );
        return new PlacesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesViewHolder holder, int position) {
        RecyclerPlacesBinding binding = holder.binding;
        binding.tvPlace.setText(mList.get(position).getPlace_name());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickPlaceItem.onClick(mList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        private RecyclerPlacesBinding binding;

        public PlacesViewHolder(@NonNull RecyclerPlacesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void addData(boolean isClear, ArrayList<PlacesItem> mLits) {
        if (isClear) {
            this.mList.clear();
        }
        int itemsInList = mList.size();
        this.mList.addAll(mLits);
        if (isClear) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(itemsInList, this.mList.size());
        }
    }

    public interface OnClickPlaceItem {
        void onClick(PlacesItem placesItem);
    }
}
