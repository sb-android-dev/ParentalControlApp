package com.schoolmanager.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.schoolmanager.R;

public class ChildArrivedDialog extends DialogFragment {

    public static final String TAG = "child_arrived_dialog";

    Button okay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_child_arrived, container, false);

        okay = view.findViewById(R.id.btnOkay);
        okay.setOnClickListener(v -> {
            dismiss();
        });

        return view;
    }
}
