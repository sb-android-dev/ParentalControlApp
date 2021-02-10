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
import com.schoolmanager.model.StudentItem;

import java.util.ArrayList;
import java.util.List;

public class StudentsRecyclerAdapter extends RecyclerView.Adapter<StudentsRecyclerAdapter.StudentViewHolder> {

    private Context context;
    private List<StudentItem> studentList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener{
        void onClick(StudentItem studentItem, int position);
    }

    public StudentsRecyclerAdapter(Context context, List<StudentItem> studentList, OnItemClickListener listener) {
        this.context = context;
        this.studentList = studentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_students, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bindView(studentList.get(position));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder{
        TextView studentName, classSectionName;
        ImageView studentImage;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.tvStudent);
            classSectionName = itemView.findViewById(R.id.tvClassSection);
            studentImage = itemView.findViewById(R.id.ivStudentImage);
        }

        public void bindView(StudentItem studentItem) {
            studentName.setText(studentItem.getStudentName());

            String cS = studentItem.getClassName() + " / " + studentItem.getSectionName();
            classSectionName.setText(cS);

            Glide.with(context).load(studentItem.getParentImage())
                    .placeholder(R.drawable.ic_person)
                    .apply(new RequestOptions()
                            .transform(new CenterCrop(), new RoundedCorners(context.getResources().getDimensionPixelSize(R.dimen.recycler_image_corner_radius))))
                    .into(studentImage);

            itemView.setOnClickListener(v -> listener.onClick(studentItem, getAbsoluteAdapterPosition()));
        }
    }
}
