package com.example.tralalero.feature.home.ui.Home.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tralalero.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

public class AboutBoardActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ShapeableImageView ivOwnerAvatar;
    private TextView tvOwnerInitials;
    private TextView tvOwnerName;
    private TextView tvOwnerUsername;
    private EditText etDescription;
    private LinearLayout llEditActions;
    private MaterialButton btnSaveDescription;
    private MaterialButton btnCancelEdit;

    private String projectId;
    private String projectName;
    private String originalDescription;
    private String ownerName;
    private String ownerUsername;
    private String ownerAvatarUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_board);

        // Get data from intent
        projectId = getIntent().getStringExtra("projectId");
        projectName = getIntent().getStringExtra("projectName");
        originalDescription = getIntent().getStringExtra("description");
        ownerName = getIntent().getStringExtra("ownerName");
        ownerUsername = getIntent().getStringExtra("ownerUsername");
        ownerAvatarUrl = getIntent().getStringExtra("ownerAvatarUrl");

        initViews();
        setupToolbar();
        setupData();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivOwnerAvatar = findViewById(R.id.ivOwnerAvatar);
        tvOwnerInitials = findViewById(R.id.tvOwnerInitials);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvOwnerUsername = findViewById(R.id.tvOwnerUsername);
        etDescription = findViewById(R.id.etDescription);
        llEditActions = findViewById(R.id.llEditActions);
        btnSaveDescription = findViewById(R.id.btnSaveDescription);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About this board");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupData() {
        // Set owner info
        if (ownerName != null && !ownerName.isEmpty()) {
            tvOwnerName.setText(ownerName);
            
            // Generate initials
            String initials = getInitials(ownerName);
            tvOwnerInitials.setText(initials);
        }

        if (ownerUsername != null && !ownerUsername.isEmpty()) {
            tvOwnerUsername.setText("@" + ownerUsername);
        }

        // Load description from SharedPreferences first
        SharedPreferences prefs = getSharedPreferences("ProjectData", MODE_PRIVATE);
        String savedDescription = prefs.getString("description_" + projectId, null);
        
        if (savedDescription != null) {
            originalDescription = savedDescription;
            etDescription.setText(savedDescription);
        } else if (originalDescription != null && !originalDescription.isEmpty()) {
            etDescription.setText(originalDescription);
        }

        // TODO: Load avatar image if ownerAvatarUrl is provided
        // Glide.with(this).load(ownerAvatarUrl).into(ivOwnerAvatar);
    }

    private void setupListeners() {
        // Show save/cancel buttons when user starts editing
        etDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                llEditActions.setVisibility(View.VISIBLE);
            }
        });

        // Track text changes
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                llEditActions.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Save button
        btnSaveDescription.setOnClickListener(v -> {
            String newDescription = etDescription.getText().toString().trim();
            updateProjectDescription(newDescription);
        });

        // Cancel button
        btnCancelEdit.setOnClickListener(v -> {
            // Revert to original description
            etDescription.setText(originalDescription);
            etDescription.clearFocus();
            llEditActions.setVisibility(View.GONE);
        });
    }

    private void updateProjectDescription(String description) {
        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("ProjectData", MODE_PRIVATE);
        prefs.edit().putString("description_" + projectId, description).apply();
        
        Toast.makeText(this, "Description updated", Toast.LENGTH_SHORT).show();
        
        originalDescription = description;
        etDescription.clearFocus();
        llEditActions.setVisibility(View.GONE);

        // Return result to caller
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedDescription", description);
        setResult(RESULT_OK, resultIntent);
        
        // TODO: Call API to update project description
        // projectViewModel.updateDescription(projectId, description);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].charAt(0));
            }
        }

        return initials.toString().toUpperCase();
    }
}
