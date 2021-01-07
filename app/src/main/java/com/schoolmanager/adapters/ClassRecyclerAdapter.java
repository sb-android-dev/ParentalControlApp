package com.schoolmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolmanager.R;
import com.schoolmanager.model.ClassItem;

import java.util.ArrayList;
import java.util.List;

public class ClassRecyclerAdapter extends RecyclerView.Adapter<ClassRecyclerAdapter.ClassViewHolder> {

    private Context context;
    private List<ClassItem> classList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener{
        void onClick(ClassItem classItem, int position);
    }

    public ClassRecyclerAdapter(Context context, List<ClassItem> classList, OnItemClickListener listener) {
        this.context = context;
        this.classList = classList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        holder.bindView(classList.get(position));
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    class ClassViewHolder extends RecyclerView.ViewHolder{
        TextView className;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            className = itemView.findViewById(R.id.tvClass);
        }

        public void bindView(ClassItem classItem) {
            className.setText(classItem.getClassName());

            itemView.setOnClickListener(v -> listener.onClick(classItem, getAbsoluteAdapterPosition()));
        }
    }
}
