package com.example.tralalero.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for invitation response action
 */
public class RespondToInvitationResponse {
    @SerializedName("invitation")
    private InvitationData invitation;

    @SerializedName("action")
    private String action;

    @SerializedName("message")
    private String message;

    public static class InvitationData {
        @SerializedName("id")
        private String id;

        @SerializedName("status")
        private String status;

        @SerializedName("updated_at")
        private String updatedAt;

        public String getId() {
            return id;
        }

        public String getStatus() {
            return status;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }

    public InvitationData getInvitation() {
        return invitation;
    }

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }
}
