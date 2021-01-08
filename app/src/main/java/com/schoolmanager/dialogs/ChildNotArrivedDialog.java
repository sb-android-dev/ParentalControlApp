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

public class ChildNotArrivedDialog extends DialogFragment {

    public static final String TAG = "child_not_arrived_dialog";

    Button thankYou;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_child_not_arrived, container, false);

        thankYou = view.findViewById(R.id.btnThankYou);
        thankYou.setOnClickListener(v -> {
            dismiss();
        });

        return view;
    }
}
