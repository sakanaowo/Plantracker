package com.example.tralalero.feature.project.members;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.tralalero.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InviteMemberDialog extends DialogFragment {
    
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private Spinner spinnerRole;
    private OnInviteListener listener;

    public interface OnInviteListener {
        void onInvite(String email, String role);
    }

    public static InviteMemberDialog newInstance() {
        return new InviteMemberDialog();
    }

    public void setOnInviteListener(OnInviteListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
            .inflate(R.layout.dialog_invite_member, null);

        tilEmail = view.findViewById(R.id.tilEmail);
        etEmail = view.findViewById(R.id.etEmail);
        spinnerRole = view.findViewById(R.id.spinnerRole);

        // Setup role spinner
        String[] roles = {"ADMIN", "MEMBER", "VIEWER"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
        spinnerRole.setSelection(1); // Default to MEMBER

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnInvite).setOnClickListener(v -> handleInvite());

        return new AlertDialog.Builder(requireContext())
            .setView(view)
            .create();
    }

    private void handleInvite() {
        String email = etEmail.getText().toString().trim();
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email address");
            return;
        }
        
        tilEmail.setError(null);
        
        String role = spinnerRole.getSelectedItem().toString();
        
        if (listener != null) {
            listener.onInvite(email, role);
        }
        dismiss();
    }
}
