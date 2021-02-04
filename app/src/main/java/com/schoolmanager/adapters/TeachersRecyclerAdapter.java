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
import com.schoolmanager.model.TeacherItem;

import java.util.ArrayList;
import java.util.List;

public class TeachersRecyclerAdapter extends RecyclerView.Adapter<TeachersRecyclerAdapter.StudentViewHolder> {

    private Context context;
    private List<TeacherItem> teacherList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener{
        void onClick(TeacherItem teacherItem, int position);
    }

    public TeachersRecyclerAdapter(Context context, List<TeacherItem> teacherList, OnItemClickListener listener) {
        this.context = context;
        this.teacherList = teacherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_teachers, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bindView(teacherList.get(position));
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder{
        TextView teacherName, subjectName;
        ImageView teacherImage;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            teacherName = itemView.findViewById(R.id.tvTeacher);
            subjectName = itemView.findViewById(R.id.tvSubjectName);
            teacherImage = itemView.findViewById(R.id.ivTeacherImage);
        }

        public void bindView(TeacherItem teacherItem) {
            teacherName.setText(teacherItem.getTeacherName());
            subjectName.setText(teacherItem.getTeacherPhoneNo());

            Glide.with(context).load(teacherItem.getTeacherImage())
                    .placeholder(R.drawable.ic_person)
                    .apply(new RequestOptions()
                            .transform(new CenterCrop(), new RoundedCorners(context.getResources().getDimensionPixelSize(R.dimen.recycler_image_corner_radius))))
                    .into(teacherImage);

            itemView.setOnClickListener(v -> listener.onClick(teacherItem, getAbsoluteAdapterPosition()));
        }
    }
}
