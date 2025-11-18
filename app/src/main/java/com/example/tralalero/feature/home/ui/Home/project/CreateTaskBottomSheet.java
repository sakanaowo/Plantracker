package com.example.tralalero.feature.home.ui.Home.project;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tralalero.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CreateTaskBottomSheet extends BottomSheetDialogFragment {

    public interface OnTaskCreatedListener {
        void onTaskCreated(String title);
    }

    private TextInputEditText etTaskTitle;
    private MaterialButton btnCreate;
    private MaterialButton btnCancel;
    private OnTaskCreatedListener listener;

    public static CreateTaskBottomSheet newInstance() {
        return new CreateTaskBottomSheet();
    }

    public void setOnTaskCreatedListener(OnTaskCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_create_task, container, false);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // ✅ Cho phép BottomSheet resize khi bàn phím hiện lên
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            );
        }

        // Initialize views
        etTaskTitle = view.findViewById(R.id.etTaskTitle);
        btnCreate = view.findViewById(R.id.btnCreate);
        btnCancel = view.findViewById(R.id.btnCancel);

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

        // Auto focus and show keyboard
        if (etTaskTitle != null) {
            etTaskTitle.requestFocus();
            etTaskTitle.postDelayed(() -> {
                android.view.inputmethod.InputMethodManager imm = 
                    (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etTaskTitle, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }, 200);
        }
    }
}
