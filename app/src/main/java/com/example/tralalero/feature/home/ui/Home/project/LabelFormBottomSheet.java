package com.example.tralalero.feature.home.ui.Home.project;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Label;
import com.example.tralalero.common.constants.LabelColors;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class LabelFormBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "LabelFormBottomSheet";

    private static final String ARG_LABEL = "label";
    private static final String ARG_PROJECT_ID = "project_id";

    private TextView btnCancel, btnSave, tvTitle, tvLabelPreview;
    private EditText etLabelName;
    private LinearLayout layoutLabelPreview;
    private RecyclerView rvColorPicker;
    private MaterialButton btnDeleteLabel;

    private ColorPickerAdapter colorPickerAdapter;
    private String selectedColor = "#EF4444";  // Default to Red from palette
    private Label existingLabel;
    private String projectId;
    private OnLabelSaveListener listener;

    public interface OnLabelSaveListener {
        void onLabelSaved(Label label);
        void onLabelDeleted(String labelId);
    }

    public static LabelFormBottomSheet newInstance(String projectId, @Nullable Label label) {
        LabelFormBottomSheet fragment = new LabelFormBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        if (label != null) {
            args.putSerializable(ARG_LABEL, label);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            existingLabel = (Label) getArguments().getSerializable(ARG_LABEL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_label_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupColorPicker();
        setupListeners();
        populateData();
    }

    private void initViews(View view) {
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvLabelPreview = view.findViewById(R.id.tvLabelPreview);
        etLabelName = view.findViewById(R.id.etLabelName);
        layoutLabelPreview = view.findViewById(R.id.layoutLabelPreview);
        rvColorPicker = view.findViewById(R.id.rvColorPicker);
        btnDeleteLabel = view.findViewById(R.id.btnDeleteLabel);
    }

    private void setupColorPicker() {
        Log.d(TAG, "setupColorPicker: selectedColor=" + selectedColor);
        
        colorPickerAdapter = new ColorPickerAdapter(color -> {
            Log.d(TAG, "Color selected from adapter: " + color);
            selectedColor = color;
            updatePreview();
        });

        // Set default selected color
        colorPickerAdapter.setSelectedColor(selectedColor);

        // Use 6 columns for 18 colors (3 rows)
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 6);
        rvColorPicker.setLayoutManager(layoutManager);
        rvColorPicker.setAdapter(colorPickerAdapter);
        
        Log.d(TAG, "ColorPicker setup complete");
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> saveLabel());

        btnDeleteLabel.setOnClickListener(v -> deleteLabel());

        etLabelName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void populateData() {
        if (existingLabel != null) {
            // Edit mode
            tvTitle.setText("Sửa nhãn");
            etLabelName.setText(existingLabel.getName());
            selectedColor = existingLabel.getColor();
            btnDeleteLabel.setVisibility(View.VISIBLE);
        } else {
            // Add mode
            tvTitle.setText("Thêm nhãn");
            btnDeleteLabel.setVisibility(View.GONE);
        }

        colorPickerAdapter.setSelectedColor(selectedColor);
        updatePreview();
    }

    private void updatePreview() {
        String name = etLabelName.getText().toString().trim();
        tvLabelPreview.setText(name.isEmpty() ? "" : name);

        try {
            layoutLabelPreview.setBackgroundColor(Color.parseColor(selectedColor));
        } catch (Exception e) {
            layoutLabelPreview.setBackgroundColor(Color.GRAY);
        }
    }

    private void saveLabel() {
        String name = etLabelName.getText().toString().trim();

        if (selectedColor == null || selectedColor.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a color", Toast.LENGTH_SHORT).show();
            return;
        }

        Label label;
        if (existingLabel != null) {
            // Update existing label
            label = new Label(
                existingLabel.getId(),
                projectId,
                name,
                selectedColor
            );
        } else {
            // Create new label
            label = new Label(
                "", // Backend will generate ID
                projectId,
                name,
                selectedColor
            );
        }

        if (listener != null) {
            listener.onLabelSaved(label);
        }

        dismiss();
    }

    private void deleteLabel() {
        if (existingLabel == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Label")
                .setMessage("Are you sure you want to delete this label?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (listener != null) {
                        listener.onLabelDeleted(existingLabel.getId());
                    }
                    dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void setOnLabelSaveListener(OnLabelSaveListener listener) {
        this.listener = listener;
    }
}
