package com.example.tralalero.feature.invitations;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tralalero.R;
import com.example.tralalero.data.repository.InvitationRepositoryImpl;
import com.example.tralalero.domain.model.Invitation;
import com.example.tralalero.domain.repository.IInvitationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display user's pending project invitations
 */
public class InvitationsFragment extends Fragment {
    private static final String TAG = "InvitationsFragment";

    private RecyclerView recyclerView;
    private InvitationsAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private IInvitationRepository invitationRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invitations, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.invitations_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InvitationsAdapter(new ArrayList<>(), new InvitationsAdapter.InvitationActionListener() {
            @Override
            public void onAccept(Invitation invitation) {
                acceptInvitation(invitation);
            }

            @Override
            public void onDecline(Invitation invitation) {
                declineInvitation(invitation);
            }
        });
        recyclerView.setAdapter(adapter);

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadInvitations);

        // Initialize repository
        invitationRepository = new InvitationRepositoryImpl(requireContext());

        // Load invitations
        loadInvitations();

        return view;
    }

    private void loadInvitations() {
        showLoading(true);
        invitationRepository.getMyInvitations(new IInvitationRepository.RepositoryCallback<List<Invitation>>() {
            @Override
            public void onSuccess(List<Invitation> invitations) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        updateUI(invitations);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Failed to load invitations: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void acceptInvitation(Invitation invitation) {
        showLoading(true);
        invitationRepository.acceptInvitation(invitation.getId(), new IInvitationRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Invitation accepted! Welcome to " + invitation.getProjectName(), Toast.LENGTH_SHORT).show();
                        
                        // Broadcast event to reload workspaces in HomeActivity
                        Intent intent = new Intent("WORKSPACE_UPDATED");
                        androidx.localbroadcastmanager.content.LocalBroadcastManager
                                .getInstance(requireContext())
                                .sendBroadcast(intent);
                        
                        // Reload invitations
                        loadInvitations();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Failed to accept: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void declineInvitation(Invitation invitation) {
        showLoading(true);
        invitationRepository.declineInvitation(invitation.getId(), new IInvitationRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                        // Reload invitations
                        loadInvitations();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Failed to decline: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updateUI(List<Invitation> invitations) {
        adapter.updateInvitations(invitations);

        if (invitations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(show);
        } else {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
