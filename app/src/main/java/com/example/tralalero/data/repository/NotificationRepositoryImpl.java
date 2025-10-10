package com.example.tralalero.data.repository;

import com.example.tralalero.data.mapper.NotificationMapper;
import com.example.tralalero.data.remote.api.NotificationApiService;
import com.example.tralalero.data.remote.dto.notification.NotificationDTO;
import com.example.tralalero.domain.model.Notification;
import com.example.tralalero.domain.repository.INotificationRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepositoryImpl implements INotificationRepository {
    private final NotificationApiService apiService;

    public NotificationRepositoryImpl(NotificationApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void getNotifications(RepositoryCallback<List<Notification>> callback) {
        apiService.getNotifications().enqueue(new Callback<List<NotificationDTO>>() {
            @Override
            public void onResponse(Call<List<NotificationDTO>> call, Response<List<NotificationDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(NotificationMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch notifications: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<NotificationDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getUnreadNotifications(RepositoryCallback<List<Notification>> callback) {
        apiService.getUnreadNotifications().enqueue(new Callback<List<NotificationDTO>>() {
            @Override
            public void onResponse(Call<List<NotificationDTO>> call, Response<List<NotificationDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(NotificationMapper.toDomainList(response.body()));
                } else {
                    callback.onError("Failed to fetch unread notifications: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<NotificationDTO>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void getNotificationById(String notificationId, RepositoryCallback<Notification> callback) {
        apiService.getNotificationById(notificationId).enqueue(new Callback<NotificationDTO>() {
            @Override
            public void onResponse(Call<NotificationDTO> call, Response<NotificationDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(NotificationMapper.toDomain(response.body()));
                } else {
                    callback.onError("Notification not found: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NotificationDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void markAsRead(String notificationId, RepositoryCallback<Void> callback) {
        apiService.markAsRead(notificationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to mark as read: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void markAllAsRead(RepositoryCallback<Void> callback) {
        apiService.markAllAsRead().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to mark all as read: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteNotification(String notificationId, RepositoryCallback<Void> callback) {
        apiService.deleteNotification(notificationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete notification: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void deleteAllNotifications(RepositoryCallback<Void> callback) {
        callback.onError("Delete all notifications not yet implemented in API");
    }

    @Override
    public void getUnreadCount(RepositoryCallback<Integer> callback) {
        apiService.getUnreadCount().enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get unread count: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
