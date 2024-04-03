package com.flarelane.example;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.flarelane.FlareLane;
import com.flarelane.notification.Notification;
import com.flarelane.notification.NotificationClickedHandler;
import com.flarelane.notification.NotificationForegroundReceivedHandler;
import com.flarelane.notification.NotificationReceivedEvent;
import com.flarelane.notification.NotificationClickedEvent;

import org.json.JSONObject;

public class MainApplication extends Application {
    private static final String FLARELANE_PROJECT_ID = "FLARELANE_PROJECT_ID";

    @Override
    public void onCreate() {
        super.onCreate();

        FlareLane.setLogLevel(Log.VERBOSE);
        FlareLane.initWithContext(this, FLARELANE_PROJECT_ID, false);
        FlareLane.setNotificationClickedHandler(new NotificationClickedHandler() {
            @Override
            public void onClicked(@NonNull NotificationClickedEvent event) {
                Log.d("FlareLane", "NotificationClickedHandler.onClicked: " + event);
            }
        });

        FlareLane.setNotificationForegroundReceivedHandler((new NotificationForegroundReceivedHandler() {
            @Override
            public void onWillDisplay(NotificationReceivedEvent notificationReceivedEvent) {
                Notification notification = notificationReceivedEvent.notification;
                Log.d("FlareLane", "NotificationForegroundReceivedHandler.onWillDisplay: " + notification.toString());

                try {
                    JSONObject data = new JSONObject(notification.data);
                    String dismissForegroundNotificationKey = "dismiss_foreground_notification";
                    boolean dismissForegroundNotification = data.has(dismissForegroundNotificationKey) ? data.getString(dismissForegroundNotificationKey).contentEquals("true") : false;
                    if (dismissForegroundNotification) return;

                    notificationReceivedEvent.display();
                } catch (Exception e) {}
            }
        }));
    }

}
