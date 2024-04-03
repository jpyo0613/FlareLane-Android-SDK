package com.flarelane.notification

data class NotificationClickedEvent(
    @JvmField val notification: Notification,
    @JvmField val action: NotificationAction
)
