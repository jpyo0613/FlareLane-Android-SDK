package com.flarelane.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.flarelane.BaseErrorHandler
import com.flarelane.BaseSharedPreferences
import com.flarelane.ChannelManager
import com.flarelane.Constants
import com.flarelane.EventService
import com.flarelane.util.AndroidUtils
import com.flarelane.util.FileUtil
import com.flarelane.util.putParcelableDataClass
import java.util.Random

class NotificationReceivedEvent(
    private val context: Context,
    @JvmField val notification: Notification
) {
    fun display() {
        try {
            val flarelaneNotification = notification
            val projectId = BaseSharedPreferences.getProjectId(context, false) ?: return
            val deviceId = BaseSharedPreferences.getDeviceId(context, false)
            Thread {
                try {
                    val notifyId = System.currentTimeMillis().toInt()
                    val isForeground = AndroidUtils.appInForeground(context)
                    val clickedIntent = Intent(context, NotificationClickedActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    clickedIntent.putExtra("notifyId", notifyId)
                    clickedIntent.putParcelableDataClass(notification)
                    clickedIntent.putParcelableDataClass(
                        NotificationAction(
                            NotificationActionType.CLICKED_BODY,
                            notification.id,
                            notification.url,
                            notification.data
                        )
                    )

                    val contentIntent = PendingIntent.getActivity(
                        context,
                        Random().nextInt(543254),
                        clickedIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )

                    val builder =
                        NotificationCompat.Builder(context, ChannelManager.DEFAULT_CHANNEL_ID)
                            .setSmallIcon(getNotificationIcon(context))
                            .setContentText(flarelaneNotification.body)
                            .setContentTitle(
                                flarelaneNotification.title
                                    ?: context.applicationInfo.loadLabel(
                                        context.packageManager
                                    ).toString()
                            )
                            .setAutoCancel(true)
                            .setContentIntent(contentIntent)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                    AndroidUtils.getResourceString(
                        context, Constants.ID_NOTIFICATION_ACCENT_COLOR
                    )?.let {
                        builder.setColor(Color.parseColor(it))
                    }

                    val image = FileUtil.downloadImageToBitmap(notification.imageUrl)
                    if (image != null) {
                        builder.setLargeIcon(image).setStyle(
                            NotificationCompat.BigPictureStyle().bigPicture(image)
                                .bigLargeIcon(null).setSummaryText(flarelaneNotification.body)
                        )
                    } else {
                        builder.setStyle(
                            NotificationCompat.BigTextStyle().bigText(flarelaneNotification.body)
                        )
                    }

                    notification.buttonsJsonArray?.let { buttonsJsonArray ->
                        if (buttonsJsonArray.length() > 0) {
                            val length = buttonsJsonArray.length()
                            for (i in 0 until length) {
                                val actionIntent = Intent(context, NotificationClickedActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                actionIntent.putExtra("notifyId", notifyId)
                                actionIntent.putParcelableDataClass(notification)

                                val buttonJsonObject = buttonsJsonArray.getJSONObject(i)
                                val notificationAction = NotificationAction.create(
                                    NotificationActionType.CLICKED_BUTTON,
                                    buttonJsonObject
                                )
                                actionIntent.putParcelableDataClass(notificationAction)

                                val clickPendingIntent = PendingIntent.getActivity(
                                    context,
                                    notifyId + i,
                                    actionIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                val action = NotificationCompat.Action(
                                    null, buttonJsonObject.getString("label"), clickPendingIntent
                                )
                                builder.addAction(action)
                            }
                        }
                    }

                    val notification = builder.build()
                    notification.defaults =
                        notification.defaults or android.app.Notification.DEFAULT_SOUND
                    notification.defaults =
                        notification.defaults or android.app.Notification.DEFAULT_LIGHTS
                    notification.defaults =
                        notification.defaults or android.app.Notification.DEFAULT_VIBRATE

                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(notifyId, notification)

                    if (isForeground) {
                        EventService.createForegroundReceived(
                            projectId,
                            deviceId,
                            flarelaneNotification
                        )
                    } else {
                        EventService.createBackgroundReceived(
                            projectId,
                            deviceId,
                            flarelaneNotification
                        )
                    }
                } catch (e: Exception) {
                    BaseErrorHandler.handle(e)
                }
            }.start()
        } catch (e: Exception) {
            BaseErrorHandler.handle(e)
        }
    }

    private fun getNotificationIcon(context: Context): Int {
        return AndroidUtils.getResourceDrawableId(context, Constants.ID_IC_STAT_DEFAULT)
            ?: android.R.drawable.ic_menu_info_details
    }
}
