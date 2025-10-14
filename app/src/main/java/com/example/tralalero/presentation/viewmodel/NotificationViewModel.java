package com.example.tralalero.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tralalero.domain.model.Notification;
import com.example.tralalero.domain.usecase.notification.DeleteAllNotificationsUseCase;
import com.example.tralalero.domain.usecase.notification.DeleteNotificationUseCase;
import com.example.tralalero.domain.usecase.notification.GetNotificationByIdUseCase;
import com.example.tralalero.domain.usecase.notification.GetNotificationCountUseCase;
import com.example.tralalero.domain.usecase.notification.GetNotificationsUseCase;
import com.example.tralalero.domain.usecase.notification.GetUnreadNotificationsUseCase;
import com.example.tralalero.domain.usecase.notification.MarkAllAsReadUseCase;
import com.example.tralalero.domain.usecase.notification.MarkAsReadUseCase;

import java.util.List;

public class NotificationViewModel extends ViewModel {
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final GetUnreadNotificationsUseCase getUnreadNotificationsUseCase;
    private final GetNotificationByIdUseCase getNotificationByIdUseCase;
    private final GetNotificationCountUseCase getNotificationCountUseCase;
    private final MarkAsReadUseCase markAsReadUseCase;
    private final MarkAllAsReadUseCase markAllAsReadUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;
    private final DeleteAllNotificationsUseCase deleteAllNotificationsUseCase;

    private final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Notification> selectedNotificationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public NotificationViewModel(
            GetNotificationsUseCase getNotificationsUseCase,
            GetUnreadNotificationsUseCase getUnreadNotificationsUseCase,
            GetNotificationByIdUseCase getNotificationByIdUseCase,
            GetNotificationCountUseCase getNotificationCountUseCase,
            MarkAsReadUseCase markAsReadUseCase,
            MarkAllAsReadUseCase markAllAsReadUseCase,
            DeleteNotificationUseCase deleteNotificationUseCase,
            DeleteAllNotificationsUseCase deleteAllNotificationsUseCase
    ) {
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.getUnreadNotificationsUseCase = getUnreadNotificationsUseCase;
        this.getNotificationByIdUseCase = getNotificationByIdUseCase;
        this.getNotificationCountUseCase = getNotificationCountUseCase;
        this.markAsReadUseCase = markAsReadUseCase;
        this.markAllAsReadUseCase = markAllAsReadUseCase;
        this.deleteNotificationUseCase = deleteNotificationUseCase;
        this.deleteAllNotificationsUseCase = deleteAllNotificationsUseCase;

        loadNotifications();
        loadUnreadCount();
    }

    //    getter
    public LiveData<List<Notification>> getNotifications() {
        return notificationsLiveData;
    }

    public LiveData<Notification> getSelectedNotification() {
        return selectedNotificationLiveData;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCountLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadNotifications() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getNotificationsUseCase.execute(new GetNotificationsUseCase.Callback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                loadingLiveData.setValue(false);
                notificationsLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadUnreadNotifications() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getUnreadNotificationsUseCase.execute(new GetUnreadNotificationsUseCase.Callback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                loadingLiveData.setValue(false);
                notificationsLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadNotificationById(String notificationId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        getNotificationByIdUseCase.execute(notificationId, new GetNotificationByIdUseCase.Callback<Notification>() {
            @Override
            public void onSuccess(Notification result) {
                loadingLiveData.setValue(false);
                selectedNotificationLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void loadUnreadCount() {
        getNotificationCountUseCase.execute(new GetNotificationCountUseCase.Callback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                unreadCountLiveData.setValue(result);
            }

            @Override
            public void onError(String error) {
                // Silent
            }
        });
    }

    public void markAsRead(String notificationId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        markAsReadUseCase.execute(notificationId, new MarkAsReadUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                loadNotifications();
                loadUnreadCount();
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void markAllAsRead() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        markAllAsReadUseCase.execute(new MarkAllAsReadUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                // Reload notifications and count
                loadNotifications();
                loadUnreadCount();
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void deleteNotification(String notificationId) {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        deleteNotificationUseCase.execute(notificationId, new DeleteNotificationUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                // Reload notifications and count
                loadNotifications();
                loadUnreadCount();
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void deleteAllNotifications() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        deleteAllNotificationsUseCase.execute(new DeleteAllNotificationsUseCase.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadingLiveData.setValue(false);
                notificationsLiveData.setValue(null);
                unreadCountLiveData.setValue(0);
            }

            @Override
            public void onError(String error) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
