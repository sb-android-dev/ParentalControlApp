package com.schoolmanager;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.schoolmanager.databinding.ActivityZoomImageBinding;

public class ZoomImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityZoomImageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_zoom_image);

        String image = getIntent().getStringExtra("image");

        binding.imgZoomImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Glide.with(this)
                .load(image)
                .into(binding.imgZoomImageZoom);


    }
}