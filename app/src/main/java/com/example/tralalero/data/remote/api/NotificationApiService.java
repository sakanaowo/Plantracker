package com.example.tralalero.data.remote.api;

import com.example.tralalero.data.remote.dto.notification.NotificationDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface NotificationApiService {


    @GET("notifications")
    Call<List<NotificationDTO>> getNotifications();


    @GET("notifications/unread")
    Call<List<NotificationDTO>> getUnreadNotifications();


    @GET("notifications/{id}")
    Call<NotificationDTO> getNotificationById(@Path("id") String notificationId);


    @GET("notifications/unread/count")
    Call<Integer> getUnreadCount();


    @PATCH("notifications/{id}/read")
    Call<Void> markAsRead(@Path("id") String notificationId);


    @PATCH("notifications/read-all")
    Call<Void> markAllAsRead();

  
    @DELETE("notifications/{id}")
    Call<Void> deleteNotification(@Path("id") String notificationId);
}
