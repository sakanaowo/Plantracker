package com.example.tralalero.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.tralalero.App.App;
import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.api.InvitationApiService;
import com.example.tralalero.data.model.InvitationResponse;
import com.example.tralalero.data.model.RespondToInvitationRequest;
import com.example.tralalero.data.model.RespondToInvitationResponse;
import com.example.tralalero.domain.model.Invitation;
import com.example.tralalero.domain.repository.IInvitationRepository;
import com.example.tralalero.network.ApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository implementation for managing project invitations
 */
public class InvitationRepositoryImpl implements IInvitationRepository {
    private static final String TAG = "InvitationRepository";
    private final InvitationApiService apiService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

    public InvitationRepositoryImpl(Context context) {
        AuthManager authManager = App.authManager;
        this.apiService = ApiClient.get(authManager).create(InvitationApiService.class);
    }

    @Override
    public void getMyInvitations(RepositoryCallback<List<Invitation>> callback) {
        apiService.getMyInvitations().enqueue(new Callback<List<InvitationResponse>>() {
            @Override
            public void onResponse(Call<List<InvitationResponse>> call, Response<List<InvitationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Invitation> invitations = new ArrayList<>();
                    for (InvitationResponse item : response.body()) {
                        invitations.add(mapToDomain(item));
                    }
                    callback.onSuccess(invitations);
                } else {
                    String errorMsg = "Failed to fetch invitations: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<InvitationResponse>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void acceptInvitation(String invitationId, RepositoryCallback<String> callback) {
        respondToInvitation(invitationId, "accept", callback);
    }

    @Override
    public void declineInvitation(String invitationId, RepositoryCallback<String> callback) {
        respondToInvitation(invitationId, "decline", callback);
    }

    private void respondToInvitation(String invitationId, String action, RepositoryCallback<String> callback) {
        RespondToInvitationRequest request = new RespondToInvitationRequest(action);

        apiService.respondToInvitation(invitationId, request).enqueue(new Callback<RespondToInvitationResponse>() {
            @Override
            public void onResponse(Call<RespondToInvitationResponse> call, Response<RespondToInvitationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();
                    Log.d(TAG, "Invitation " + action + "ed: " + message);
                    callback.onSuccess(message);
                } else {
                    String errorMsg = "Failed to " + action + " invitation: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<RespondToInvitationResponse> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    private Invitation mapToDomain(InvitationResponse response) {
        Date expiresAt = parseDate(response.getExpiresAt());
        Date createdAt = parseDate(response.getCreatedAt());

        String projectName = response.getProjects() != null ? response.getProjects().getName() : "";
        String projectDescription = response.getProjects() != null ? response.getProjects().getDescription() : "";
        String inviterName = response.getInviter() != null ? response.getInviter().getName() : "";
        String inviterEmail = response.getInviter() != null ? response.getInviter().getEmail() : "";
        String inviterAvatarUrl = response.getInviter() != null ? response.getInviter().getAvatarUrl() : null;

        return new Invitation(
                response.getId(),
                response.getProjectId(),
                projectName,
                projectDescription,
                response.getUserId(),
                response.getRole(),
                response.getStatus(),
                response.getInvitedBy(),
                inviterName,
                inviterEmail,
                inviterAvatarUrl,
                expiresAt,
                createdAt
        );
    }

    private Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse date: " + dateString, e);
            return null;
        }
    }
}
