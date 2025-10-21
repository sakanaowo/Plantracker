package com.example.tralalero.domain.repository;

import com.example.tralalero.domain.model.Notification;

import java.util.List;

public interface INotificationRepository {
    void getNotifications(RepositoryCallback<List<Notification>> callback);

    void getUnreadNotifications(RepositoryCallback<List<Notification>> callback);

    void getNotificationById(String notificationId, RepositoryCallback<Notification> callback);

    void markAsRead(String notificationId, RepositoryCallback<Void> callback);

    void markAllAsRead(RepositoryCallback<Void> callback);

    void deleteNotification(String notificationId, RepositoryCallback<Void> callback);

    void deleteAllNotifications(RepositoryCallback<Void> callback);

    void getUnreadCount(RepositoryCallback<Integer> callback);

    interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
