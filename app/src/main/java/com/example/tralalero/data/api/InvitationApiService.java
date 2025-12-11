package com.example.tralalero.data.api;

import com.example.tralalero.data.model.InvitationResponse;
import com.example.tralalero.data.model.RespondToInvitationRequest;
import com.example.tralalero.data.model.RespondToInvitationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * API Service for handling project invitations
 */
public interface InvitationApiService {

    /**
     * Get user's pending invitations
     * GET /invitations/my
     */
    @GET("invitations/my")
    Call<List<InvitationResponse>> getMyInvitations();

    /**
     * Respond to an invitation (accept or decline)
     * POST /invitations/{invitationId}/respond
     */
    @POST("invitations/{invitationId}/respond")
    Call<RespondToInvitationResponse> respondToInvitation(
            @Path("invitationId") String invitationId,
            @Body RespondToInvitationRequest request
    );
}
