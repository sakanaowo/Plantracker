package com.example.tralalero.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tralalero.domain.usecase.notification.GetNotificationsUseCase;
import com.example.tralalero.domain.usecase.notification.GetUnreadNotificationsUseCase;
import com.example.tralalero.domain.usecase.notification.GetNotificationByIdUseCase;
import com.example.tralalero.domain.usecase.notification.GetNotificationCountUseCase;
import com.example.tralalero.domain.usecase.notification.MarkAsReadUseCase;
import com.example.tralalero.domain.usecase.notification.MarkAllAsReadUseCase;
import com.example.tralalero.domain.usecase.notification.DeleteNotificationUseCase;
import com.example.tralalero.domain.usecase.notification.DeleteAllNotificationsUseCase;

public class NotificationViewModelFactory implements ViewModelProvider.Factory {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final GetUnreadNotificationsUseCase getUnreadNotificationsUseCase;
    private final GetNotificationByIdUseCase getNotificationByIdUseCase;
    private final GetNotificationCountUseCase getNotificationCountUseCase;
    private final MarkAsReadUseCase markAsReadUseCase;
    private final MarkAllAsReadUseCase markAllAsReadUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;
    private final DeleteAllNotificationsUseCase deleteAllNotificationsUseCase;

    public NotificationViewModelFactory(
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
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotificationViewModel.class)) {
            return (T) new NotificationViewModel(
                    getNotificationsUseCase,
                    getUnreadNotificationsUseCase,
                    getNotificationByIdUseCase,
                    getNotificationCountUseCase,
                    markAsReadUseCase,
                    markAllAsReadUseCase,
                    deleteNotificationUseCase,
                    deleteAllNotificationsUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
