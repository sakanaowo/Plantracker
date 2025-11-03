package com.example.tralalero.feature.home.ui.Home.project;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tralalero.R;
import com.google.android.material.button.MaterialButton;

public class CreateTaskDialog extends Dialog {

    public interface OnTaskCreatedListener {
        void onTaskCreated(String title);
    }

    private EditText etTaskTitle;
    private MaterialButton btnCreate;
    private MaterialButton btnCancel;
    private OnTaskCreatedListener listener;

    public CreateTaskDialog(@NonNull Context context) {
        super(context);
    }

    public void setOnTaskCreatedListener(OnTaskCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_create_task);

        // Set transparent background for rounded corners
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Initialize views
        etTaskTitle = findViewById(R.id.etTaskTitle);
        btnCreate = findViewById(R.id.btnCreate);
        btnCancel = findViewById(R.id.btnCancel);

        // Set up listeners
        btnCancel.setOnClickListener(v -> dismiss());

        btnCreate.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter task title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onTaskCreated(title);
            }
            dismiss();
        });

        // Auto focus on title input
        if (etTaskTitle != null) {
            etTaskTitle.requestFocus();
        }
    }
}
