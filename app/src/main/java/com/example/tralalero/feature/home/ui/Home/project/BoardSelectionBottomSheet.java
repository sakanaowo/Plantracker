package com.example.tralalero.feature.home.ui.Home.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BoardSelectionBottomSheet extends BottomSheetDialogFragment {
    
    private static final String ARG_CURRENT_STATUS = "current_status";
    
    private Task.TaskStatus currentStatus;
    private OnBoardSelectedListener listener;
    
    private LinearLayout layoutToDo;
    private LinearLayout layoutInProgress;
    private LinearLayout layoutDone;
    private TextView tvToDo;
    private TextView tvInProgress;
    private TextView tvDone;
    
    public interface OnBoardSelectedListener {
        void onBoardSelected(Task.TaskStatus newStatus);
    }
    
    public static BoardSelectionBottomSheet newInstance(Task.TaskStatus currentStatus) {
        BoardSelectionBottomSheet fragment = new BoardSelectionBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CURRENT_STATUS, currentStatus != null ? currentStatus.name() : Task.TaskStatus.TO_DO.name());
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String statusStr = getArguments().getString(ARG_CURRENT_STATUS);
            try {
                currentStatus = Task.TaskStatus.valueOf(statusStr);
            } catch (Exception e) {
                currentStatus = Task.TaskStatus.TO_DO;
            }
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottomsheet_board_selection, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupListeners();
        disableCurrentBoard();
    }
    
    private void initViews(View view) {
        layoutToDo = view.findViewById(R.id.layoutToDo);
        layoutInProgress = view.findViewById(R.id.layoutInProgress);
        layoutDone = view.findViewById(R.id.layoutDone);
        tvToDo = view.findViewById(R.id.tvToDo);
        tvInProgress = view.findViewById(R.id.tvInProgress);
        tvDone = view.findViewById(R.id.tvDone);
    }
    
    private void setupListeners() {
        layoutToDo.setOnClickListener(v -> {
            if (listener != null && currentStatus != Task.TaskStatus.TO_DO) {
                listener.onBoardSelected(Task.TaskStatus.TO_DO);
                dismiss();
            }
        });
        
        layoutInProgress.setOnClickListener(v -> {
            if (listener != null && currentStatus != Task.TaskStatus.IN_PROGRESS) {
                listener.onBoardSelected(Task.TaskStatus.IN_PROGRESS);
                dismiss();
            }
        });
        
        layoutDone.setOnClickListener(v -> {
            if (listener != null && currentStatus != Task.TaskStatus.DONE) {
                listener.onBoardSelected(Task.TaskStatus.DONE);
                dismiss();
            }
        });
    }
    
    private void disableCurrentBoard() {
        // Disable and grey out current board
        switch (currentStatus) {
            case TO_DO:
                layoutToDo.setEnabled(false);
                layoutToDo.setAlpha(0.4f);
                tvToDo.setTextColor(0xFF999999);
                break;
            case IN_PROGRESS:
                layoutInProgress.setEnabled(false);
                layoutInProgress.setAlpha(0.4f);
                tvInProgress.setTextColor(0xFF999999);
                break;
            case DONE:
                layoutDone.setEnabled(false);
                layoutDone.setAlpha(0.4f);
                tvDone.setTextColor(0xFF999999);
                break;
        }
    }
    
    public void setOnBoardSelectedListener(OnBoardSelectedListener listener) {
        this.listener = listener;
    }
}
