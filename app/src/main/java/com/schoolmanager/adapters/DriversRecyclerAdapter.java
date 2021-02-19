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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.schoolmanager.R;
import com.schoolmanager.model.DriverItem;

import java.util.List;

public class DriversRecyclerAdapter extends RecyclerView.Adapter<DriversRecyclerAdapter.DriverViewHolder> {

    private Context context;
    private final List<DriverItem> drivers;
    private final OnItemClickListener listener;
    private boolean isForSelection;

    public interface OnItemClickListener {
        void onClick(DriverItem driverItem, int position);
    }

    public DriversRecyclerAdapter(Context context, List<DriverItem> drivers, OnItemClickListener listener) {
        this.context = context;
        this.drivers = drivers;
        this.listener = listener;
        this.isForSelection = false;
    }

    public DriversRecyclerAdapter(Context context, List<DriverItem> drivers, OnItemClickListener listener, boolean isForSelection) {
        this.context = context;
        this.drivers = drivers;
        this.listener = listener;
        this.isForSelection = isForSelection;
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

    class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView driverName, vehicleNo;
        ImageView driverImage, checkedImage;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            driverName = itemView.findViewById(R.id.tvDriver);
            vehicleNo = itemView.findViewById(R.id.tvVehicleNo);
            driverImage = itemView.findViewById(R.id.ivDriverImage);
            checkedImage = itemView.findViewById(R.id.ivChecked);
        }

        public void bindView(DriverItem driverItem) {
            driverName.setText(driverItem.getDriverName());

            if(driverItem.getPhoneNo() != null && !driverItem.getPhoneNo().isEmpty()
                    && !driverItem.getPhoneNo().equalsIgnoreCase("null")) {
                vehicleNo.setText(driverItem.getPhoneNo());
                vehicleNo.setVisibility(View.VISIBLE);
            } else {
                vehicleNo.setVisibility(View.GONE);
            }

            if(isForSelection) {
                if (driverItem.isSelected()) {
                    checkedImage.setVisibility(View.VISIBLE);
                    driverImage.setVisibility(View.INVISIBLE);
                } else {
                    Glide.with(context).load(driverItem.getDriverImage())
                            .placeholder(R.drawable.ic_person)
                            .apply(new RequestOptions()
                                    .transform(new CenterCrop(), new RoundedCorners(context.getResources().getDimensionPixelSize(R.dimen.image_corner_radius))))
                            .into(driverImage);

                    checkedImage.setVisibility(View.INVISIBLE);
                    driverImage.setVisibility(View.VISIBLE);
                }
            } else {
                Glide.with(context).load(driverItem.getDriverImage())
                        .placeholder(R.drawable.ic_person)
                        .apply(new RequestOptions()
                                .transform(new CenterCrop(), new RoundedCorners(context.getResources().getDimensionPixelSize(R.dimen.image_corner_radius))))
                        .into(driverImage);

                driverImage.setVisibility(View.VISIBLE);
                checkedImage.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(v -> listener.onClick(driverItem, getAbsoluteAdapterPosition()));
        }
    }
}
