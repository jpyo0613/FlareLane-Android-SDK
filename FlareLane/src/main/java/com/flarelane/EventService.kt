package com.flarelane

import com.flarelane.notification.Notification
import com.flarelane.notification.NotificationClickedEvent
import org.json.JSONArray
import org.json.JSONObject

internal object EventService {
    @JvmField
    var unhandledNotificationClickEvent: NotificationClickedEvent? = null

    @Throws(Exception::class)
    internal fun createNotificationClicked(projectId: String, deviceId: String?, event: NotificationClickedEvent) {
        create(projectId, deviceId, event.notification.id, EventType.NOTIFICATION_CLICKED)
        if (FlareLane.notificationClickedHandler != null) {
            FlareLane.notificationClickedHandler.onClicked(event)
        } else {
            unhandledNotificationClickEvent = event
        }
    }

    @Throws(Exception::class)
    internal fun createNotificationAction(
        projectId: String,
        deviceId: String?,
        event: NotificationClickedEvent
    ) {
        if (FlareLane.notificationClickedHandler != null) {
            FlareLane.notificationClickedHandler.onClicked(event)
        } else {
            unhandledNotificationClickEvent = event
        }
        val body = JSONObject()
        body.put("type", EventType.NOTIFICATION_BUTTON_CLICKED)
        body.put("platform", "android")
        body.put("deviceId", deviceId)
        body.put("createdAt", Utils.getISO8601DateString())
        body.put("notificationId", event.notification.id)
        body.put("actionId", event.action.id)
        HTTPClient.post(
            "internal/v1/projects/$projectId/events-v2",
            body,
            HTTPClient.ResponseHandler()
        )
    }

    @Throws(Exception::class)
    internal fun createBackgroundReceived(
        projectId: String,
        deviceId: String?,
        notification: Notification
    ) {
        create(projectId, deviceId, notification.id, EventType.BACKGROUND_RECEIVED)
    }

    @Throws(Exception::class)
    internal fun createForegroundReceived(
        projectId: String,
        deviceId: String?,
        notification: Notification
    ) {
        create(projectId, deviceId, notification.id, EventType.FOREGROUND_RECEIVED)
    }

    @Throws(Exception::class)
    private fun create(projectId: String, deviceId: String?, notificationId: String, type: String) {
        val body = JSONObject()
        body.put("notificationId", notificationId)
        body.put("deviceId", deviceId)
        body.put("platform", "android")
        body.put("type", type)
        body.put("createdAt", Utils.getISO8601DateString())
        HTTPClient.post(
            "internal/v1/projects/$projectId/events",
            body,
            HTTPClient.ResponseHandler()
        )
    }

    @JvmStatic
    @Throws(Exception::class)
    fun trackEvent(
        projectId: String,
        subjectType: String?,
        subjectId: String?,
        type: String?,
        data: JSONObject?
    ) {
        val event = JSONObject()
            .put("type", type)
            .put("subjectType", subjectType)
            .put("subjectId", subjectId)
            .put("createdAt", Utils.getISO8601DateString())
        if (data != null) {
            event.put("data", data)
        }
        val body = JSONObject().put("events", JSONArray().put(event))
        HTTPClient.post(
            "internal/v1/projects/$projectId/events-v2",
            body,
            HTTPClient.ResponseHandler()
        )
    }
}
