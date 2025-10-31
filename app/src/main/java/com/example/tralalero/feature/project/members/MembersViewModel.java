package com.example.tralalero.feature.project.members;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tralalero.domain.model.Member;
import com.example.tralalero.domain.repository.IMemberRepository;

import java.util.List;

public class MembersViewModel extends ViewModel {
    
    private IMemberRepository repository;
    private MutableLiveData<List<Member>> membersLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> successLiveData = new MutableLiveData<>();

    public MembersViewModel(IMemberRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Member>> getMembers() {
        return membersLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<String> getSuccess() {
        return successLiveData;
    }

    public void loadMembers(String projectId) {
        loadingLiveData.setValue(true);
        
        repository.getProjectMembers(projectId, new IMemberRepository.RepositoryCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> data) {
                loadingLiveData.setValue(false);
                membersLiveData.setValue(data);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void inviteMember(String projectId, String email, String role) {
        loadingLiveData.setValue(true);
        
        repository.inviteMember(projectId, email, role, 
            new IMemberRepository.RepositoryCallback<Member>() {
                @Override
                public void onSuccess(Member data) {
                    loadingLiveData.setValue(false);
                    successLiveData.setValue("Member invited successfully!");
                    // Reload members
                    loadMembers(projectId);
                }

                @Override
                public void onError(String error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error);
                }
            }
        );
    }

    public void updateMemberRole(String projectId, String memberId, String role) {
        loadingLiveData.setValue(true);
        
        repository.updateMemberRole(projectId, memberId, role, 
            new IMemberRepository.RepositoryCallback<Member>() {
                @Override
                public void onSuccess(Member data) {
                    loadingLiveData.setValue(false);
                    successLiveData.setValue("Role updated successfully!");
                    // Reload members
                    loadMembers(projectId);
                }

                @Override
                public void onError(String error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error);
                }
            }
        );
    }

    public void removeMember(String projectId, String memberId) {
        loadingLiveData.setValue(true);
        
        repository.removeMember(projectId, memberId, 
            new IMemberRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    loadingLiveData.setValue(false);
                    successLiveData.setValue("Member removed successfully!");
                    // Reload members
                    loadMembers(projectId);
                }

                @Override
                public void onError(String error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(error);
                }
            }
        );
    }
}
