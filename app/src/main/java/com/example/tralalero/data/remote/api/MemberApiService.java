package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.member.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface MemberApiService {
    @POST("projects/{projectId}/members/invite")
    Call<MemberDTO> inviteMember(
        @Path("projectId") String projectId,
        @Body InviteMemberDTO dto
    );

    @GET("projects/{projectId}/members")
    Call<MemberListResponse> getMembers(
        @Path("projectId") String projectId
    );

    @PATCH("projects/{projectId}/members/{memberId}")
    Call<MemberDTO> updateMemberRole(
        @Path("projectId") String projectId,
        @Path("memberId") String memberId,
        @Body UpdateMemberRoleDTO dto
    );

    @DELETE("projects/{projectId}/members/{memberId}")
    Call<Void> removeMember(
        @Path("projectId") String projectId,
        @Path("memberId") String memberId
    );

    // Response wrapper
    class MemberListResponse {
        public List<MemberDTO> data;
        public int count;
    }
}
