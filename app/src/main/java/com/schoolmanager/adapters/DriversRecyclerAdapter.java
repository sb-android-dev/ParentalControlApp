package com.schoolmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolmanager.R;
import com.schoolmanager.model.DriverItem;

import java.util.ArrayList;
import java.util.List;

public class DriversRecyclerAdapter extends RecyclerView.Adapter<DriversRecyclerAdapter.DriverViewHolder> {

    private Context context;
    private List<DriverItem> drivers = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener{
        void onClick(DriverItem driverItem, int position);
    }

    public DriversRecyclerAdapter(Context context, List<DriverItem> drivers, OnItemClickListener listener) {
        this.context = context;
        this.drivers = drivers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_drivers, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        holder.bindView(drivers.get(position));
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    class DriverViewHolder extends RecyclerView.ViewHolder{
        TextView driverName, vehicleNo;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            driverName = itemView.findViewById(R.id.tvDriver);
            vehicleNo = itemView.findViewById(R.id.tvVehicleNo);
        }

        public void bindView(DriverItem driverItem) {
            driverName.setText(driverItem.getDriverName());
            vehicleNo.setText(driverItem.getPhoneNo());

            itemView.setOnClickListener(v -> listener.onClick(driverItem, getAbsoluteAdapterPosition()));
        }
    }
}
