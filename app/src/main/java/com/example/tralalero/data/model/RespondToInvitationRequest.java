package com.example.tralalero.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for responding to invitation
 */
public class RespondToInvitationRequest {
    @SerializedName("action")
    private String action; // "accept" or "decline"

    public RespondToInvitationRequest(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
