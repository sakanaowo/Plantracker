package com.example.tralalero.data.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.tralalero.auth.remote.AuthManager;
import com.example.tralalero.data.remote.api.MemberApiService;
import com.example.tralalero.data.remote.dto.member.*;
import com.example.tralalero.data.mapper.MemberMapper;
import com.example.tralalero.domain.model.Member;
import com.example.tralalero.domain.repository.IMemberRepository;
import com.example.tralalero.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class MemberRepositoryImpl implements IMemberRepository {
    
    private static final String TAG = "MemberRepository";
    private MemberApiService apiService;

    public MemberRepositoryImpl(Context context) {
        Application app = (Application) context.getApplicationContext();
        AuthManager authManager = new AuthManager(app);
        this.apiService = ApiClient.get(authManager).create(MemberApiService.class);
    }

    @Override
    public void inviteMember(String projectId, String email, String role,
                            IMemberRepository.RepositoryCallback<Member> callback) {
        Log.d(TAG, "Inviting member: " + email + " with role: " + role);
        
        InviteMemberDTO dto = new InviteMemberDTO(email, role);
        
        apiService.inviteMember(projectId, dto).enqueue(new Callback<MemberDTO>() {
            @Override
            public void onResponse(Call<MemberDTO> call, Response<MemberDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Member invited successfully");
                    Member member = MemberMapper.toDomain(response.body());
                    callback.onSuccess(member);
                } else {
                    String error = "Failed to invite member: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<MemberDTO> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    @Override
    public void getProjectMembers(String projectId,
                                 IMemberRepository.RepositoryCallback<List<Member>> callback) {
        Log.d(TAG, "Fetching members for projectId: " + projectId);
        
        apiService.getMembers(projectId).enqueue(
            new Callback<MemberApiService.MemberListResponse>() {
                @Override
                public void onResponse(Call<MemberApiService.MemberListResponse> call,
                                     Response<MemberApiService.MemberListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Member> members = MemberMapper.toDomainList(response.body().data);
                        Log.d(TAG, "Loaded " + members.size() + " members");
                        callback.onSuccess(members);
                    } else {
                        String error = "Failed to load members: " + response.code();
                        Log.e(TAG, error);
                        callback.onError(error);
                    }
                }

                @Override
                public void onFailure(Call<MemberApiService.MemberListResponse> call,
                                    Throwable t) {
                    Log.e(TAG, "Network error: " + t.getMessage());
                    callback.onError(t.getMessage());
                }
            }
        );
    }

    @Override
    public void updateMemberRole(String projectId, String memberId, String role,
                                IMemberRepository.RepositoryCallback<Member> callback) {
        Log.d(TAG, "Updating member role: " + memberId + " to " + role);
        
        UpdateMemberRoleDTO dto = new UpdateMemberRoleDTO(role);
        
        apiService.updateMemberRole(projectId, memberId, dto).enqueue(new Callback<MemberDTO>() {
            @Override
            public void onResponse(Call<MemberDTO> call, Response<MemberDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Role updated successfully");
                    Member member = MemberMapper.toDomain(response.body());
                    callback.onSuccess(member);
                } else {
                    String error = "Failed to update role: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<MemberDTO> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    @Override
    public void removeMember(String projectId, String memberId,
                           IMemberRepository.RepositoryCallback<Void> callback) {
        Log.d(TAG, "Removing member: " + memberId);
        
        apiService.removeMember(projectId, memberId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Member removed successfully");
                    callback.onSuccess(null);
                } else {
                    String error = "Failed to remove member: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}
