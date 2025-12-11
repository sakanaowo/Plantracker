package com.example.tralalero.data.remote.dto.member;

import com.google.gson.annotations.SerializedName;

public class InviteMemberDTO {
    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    public InviteMemberDTO(String email, String role) {
        this.email = email;
        this.role = role;
    }

    // Getters
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
