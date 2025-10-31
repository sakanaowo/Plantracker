package com.example.tralalero.data.remote.dto.member;

import com.google.gson.annotations.SerializedName;

public class UpdateMemberRoleDTO {
    @SerializedName("role")
    private String role;

    public UpdateMemberRoleDTO(String role) {
        this.role = role;
    }

    public String getRole() { return role; }
}
