package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Member;
import java.util.List;

public interface IMemberRepository {
    void inviteMember(String projectId, String email, String role, 
        RepositoryCallback<Member> callback);
    
    void getProjectMembers(String projectId, 
        RepositoryCallback<List<Member>> callback);
    
    void updateMemberRole(String projectId, String memberId, String role,
        RepositoryCallback<Member> callback);
    
    void removeMember(String projectId, String memberId,
        RepositoryCallback<Void> callback);
    
    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
