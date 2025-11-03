package com.example.tralalero.feature.invitations;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Invitation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying project invitations in RecyclerView
 */
public class InvitationsAdapter extends RecyclerView.Adapter<InvitationsAdapter.InvitationViewHolder> {
    private List<Invitation> invitations;
    private final InvitationActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface InvitationActionListener {
        void onAccept(Invitation invitation);
        void onDecline(Invitation invitation);
    }

    public InvitationsAdapter(List<Invitation> invitations, InvitationActionListener listener) {
        this.invitations = invitations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InvitationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invitation, parent, false);
        return new InvitationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationViewHolder holder, int position) {
        Invitation invitation = invitations.get(position);
        holder.bind(invitation);
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    public void updateInvitations(List<Invitation> newInvitations) {
        this.invitations = newInvitations;
        notifyDataSetChanged();
    }

    class InvitationViewHolder extends RecyclerView.ViewHolder {
        private final TextView projectNameText;
        private final TextView inviterText;
        private final TextView roleText;
        private final TextView dateText;
        private final Button acceptButton;
        private final Button declineButton;

        public InvitationViewHolder(@NonNull View itemView) {
            super(itemView);
            projectNameText = itemView.findViewById(R.id.project_name);
            inviterText = itemView.findViewById(R.id.inviter_name);
            roleText = itemView.findViewById(R.id.role_text);
            dateText = itemView.findViewById(R.id.date_text);
            acceptButton = itemView.findViewById(R.id.accept_button);
            declineButton = itemView.findViewById(R.id.decline_button);
        }

        public void bind(Invitation invitation) {
            projectNameText.setText(invitation.getProjectName());
            inviterText.setText("Invited by " + invitation.getInviterName());
            roleText.setText("Role: " + invitation.getRole());

            if (invitation.getCreatedAt() != null) {
                dateText.setText("Sent on " + dateFormat.format(invitation.getCreatedAt()));
            }

            acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccept(invitation);
                }
            });

            declineButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecline(invitation);
                }
            });

            // Disable buttons if expired
            if (invitation.isExpired()) {
                acceptButton.setEnabled(false);
                declineButton.setEnabled(false);
                dateText.setText("Expired");
            }
        }
    }
}
