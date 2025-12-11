package com.example.tralalero.feature.project.labels;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tralalero.R;
import com.example.tralalero.data.remote.dto.label.LabelDTO;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateLabelDialog extends DialogFragment {
    
    private TextInputLayout tilLabelName;
    private TextInputEditText etLabelName;
    private RecyclerView rvColorPalette;
    private Chip chipPreview;
    private TextView tvDialogTitle;
    private Button btnCreate;
    
    private ColorPaletteAdapter colorAdapter;
    private OnLabelActionListener listener;
    private LabelDTO editingLabel = null;

    public interface OnLabelActionListener {
        void onCreateLabel(String name, String color);
        void onUpdateLabel(String labelId, String name, String color);
    }

    public static CreateLabelDialog newInstance() {
        return new CreateLabelDialog();
    }

    public static CreateLabelDialog newInstance(LabelDTO label) {
        CreateLabelDialog dialog = new CreateLabelDialog();
        Bundle args = new Bundle();
        args.putString("labelId", label.getId());
        args.putString("labelName", label.getName());
        args.putString("labelColor", label.getColor());
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnLabelActionListener(OnLabelActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
            .inflate(R.layout.dialog_create_label, null);

        // Check if editing
        final boolean isEditing;
        if (getArguments() != null) {
            String labelId = getArguments().getString("labelId");
            if (labelId != null) {
                isEditing = true;
                editingLabel = new LabelDTO();
                editingLabel.setId(labelId);
                editingLabel.setName(getArguments().getString("labelName"));
                editingLabel.setColor(getArguments().getString("labelColor"));
            } else {
                isEditing = false;
            }
        } else {
            isEditing = false;
        }

        // Initialize views
        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        tilLabelName = view.findViewById(R.id.tilLabelName);
        etLabelName = view.findViewById(R.id.etLabelName);
        rvColorPalette = view.findViewById(R.id.rvColorPalette);
        chipPreview = view.findViewById(R.id.chipPreview);
        btnCreate = view.findViewById(R.id.btnCreate);

        // Set title and button text
        if (isEditing) {
            tvDialogTitle.setText("Edit Label");
            btnCreate.setText("Update");
            etLabelName.setText(editingLabel.getName());
        }

        // Setup color palette
        setupColorPalette();

        // If editing, set selected color
        if (isEditing && editingLabel.getColor() != null) {
            colorAdapter.setSelectedColor(editingLabel.getColor());
            updatePreview(editingLabel.getColor());
        } else {
            updatePreview(colorAdapter.getSelectedColor());
        }

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        btnCreate.setOnClickListener(v -> handleSave(isEditing));

        return new AlertDialog.Builder(requireContext())
            .setView(view)
            .create();
    }

    private void setupColorPalette() {
        colorAdapter = new ColorPaletteAdapter();
        colorAdapter.setOnColorSelectedListener(this::updatePreview);
        
        rvColorPalette.setLayoutManager(new GridLayoutManager(requireContext(), 5));
        rvColorPalette.setAdapter(colorAdapter);
    }

    private void updatePreview(String colorHex) {
        try {
            int color = Color.parseColor(colorHex);
            chipPreview.setChipBackgroundColor(ColorStateList.valueOf(color));
        } catch (Exception e) {
            chipPreview.setChipBackgroundColor(ColorStateList.valueOf(Color.GRAY));
        }
    }

    private void handleSave(boolean isEditing) {
        String name = etLabelName.getText().toString().trim();
        
        if (TextUtils.isEmpty(name)) {
            tilLabelName.setError("Label name is required");
            return;
        }
        
        tilLabelName.setError(null);
        String color = colorAdapter.getSelectedColor();
        
        if (listener != null) {
            if (isEditing && editingLabel != null) {
                listener.onUpdateLabel(editingLabel.getId(), name, color);
            } else {
                listener.onCreateLabel(name, color);
            }
        }
        dismiss();
    }
}
