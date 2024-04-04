package com.flarelane.notification

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.flarelane.BaseErrorHandler
import com.flarelane.BaseSharedPreferences
import com.flarelane.Constants
import com.flarelane.EventService
import com.flarelane.Logger
import com.flarelane.util.AndroidUtils
import com.flarelane.util.IntentUtil
import com.flarelane.util.getParcelableDataClass
import com.flarelane.webview.FlareLaneWebViewActivity

internal class NotificationClickedActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.verbose("NotificationClickedActivity onCreate")
        val notifyId = intent.getIntExtra("notifyId", -1).also {
            if (it == -1) {
                return
            }
        }
        try {
            val notification = intent.getParcelableDataClass(Notification::class.java) ?: return
            val notificationAction =
                intent.getParcelableDataClass(NotificationAction::class.java) ?: return
            val projectId =
                BaseSharedPreferences.getProjectId(this.applicationContext, false) ?: return
            val deviceId =
                BaseSharedPreferences.getDeviceId(this.applicationContext, false)

            val event = NotificationClickedEvent(notification, notificationAction)
            Logger.verbose("NotificationClickedActivity event=$event")

            handleNotificationClicked(event)

            when (event.action.type) {
                NotificationActionType.CLICKED_BODY -> {
                    EventService.createNotificationClicked(projectId, deviceId, event)
                }

                NotificationActionType.CLICKED_BUTTON -> {
                    EventService.createNotificationAction(projectId, deviceId, event)
                }
            }
            Logger.verbose("NotificationClickedActivity notification=$notification")
        } catch (e: Exception) {
            BaseErrorHandler.handle(e)
        } finally {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notifyId)
            finish()
        }
    }

    private fun handleNotificationClicked(event: NotificationClickedEvent) {
        val url = event.action.url
        if (url.isNullOrEmpty()) {
            launchApp()
        } else {
            val isIgnoreLaunchUrl = AndroidUtils.getManifestMetaBoolean(
                this, Constants.DISMISS_LAUNCH_URL
            ) || event.notification.dataJsonObject?.optString(Constants.DISMISS_LAUNCH_URL) == "true"
            if (isIgnoreLaunchUrl) {
                Logger.verbose("Works natively without automatic URL processing")
                launchApp()
                return
            }

            IntentUtil.createIntentIfResolveActivity(this, url)?.let {
                try {
                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(it)
                } catch (_: Exception) {
                    Logger.verbose("Url is not available. url=$url")
                    launchApp()
                }
            } ?: FlareLaneWebViewActivity.show(this, url)
        }
    }

    private fun launchApp() {
        if (isTaskRoot) {
            Logger.verbose("This is last activity in the stack")
            packageManager.getLaunchIntentForPackage(packageName)?.let {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(it)
            }
        }
    }
}
