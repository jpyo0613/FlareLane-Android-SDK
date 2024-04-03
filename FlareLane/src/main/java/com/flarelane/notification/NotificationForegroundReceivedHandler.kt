package com.flarelane.notification

interface NotificationForegroundReceivedHandler {
    fun onWillDisplay(event: NotificationReceivedEvent)
}
