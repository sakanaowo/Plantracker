package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Invitation;

import java.util.List;

/**
 * Repository interface for managing project invitations
 */
public interface IInvitationRepository {

    /**
     * Get user's pending invitations
     */
    void getMyInvitations(RepositoryCallback<List<Invitation>> callback);

    /**
     * Accept an invitation
     */
    void acceptInvitation(String invitationId, RepositoryCallback<String> callback);

    /**
     * Decline an invitation
     */
    void declineInvitation(String invitationId, RepositoryCallback<String> callback);

    /**
     * Callback interface for repository operations
     */
    interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}
