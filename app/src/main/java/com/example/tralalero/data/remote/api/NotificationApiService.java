package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.notification.NotificationDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface NotificationApiService {

    /**
     * Get all notifications for current user
     * GET /notifications
     */
    @GET("notifications")
    Call<List<NotificationDTO>> getNotifications();

    /**
     * Get unread notifications count
     * GET /notifications/unread/count
     */
    @GET("notifications/unread/count")
    Call<Integer> getUnreadCount();

    /**
     * Mark notification as read
     * PATCH /notifications/{id}/read
     */
    @PATCH("notifications/{id}/read")
    Call<NotificationDTO> markAsRead(@Path("id") String notificationId);

    /**
     * Mark all notifications as read
     * PATCH /notifications/read-all
     */
    @PATCH("notifications/read-all")
    Call<Void> markAllAsRead();

    /**
     * Delete notification
     * DELETE /notifications/{id}
     */
    @DELETE("notifications/{id}")
    Call<Void> deleteNotification(@Path("id") String notificationId);
}

