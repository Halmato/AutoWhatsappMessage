package com.twinc.halmato.autowhatsappmessage.Notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tiaan on 1/27/2017.
 */

public class NotificationScheduler {

    public static List<PendingIntent> pendingNotifications = new ArrayList<>();

    public static void removeAllScheduledNotifications(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (PendingIntent pendingIntent : pendingNotifications) {

            alarmManager.cancel(pendingIntent);
        }

        pendingNotifications.clear();
    }

    public static void scheduleNotification(Context context, int notificationId, long timeToTrigger) {

        Intent alarmIntent = new Intent(context, ScheduledNotification.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, alarmIntent, 0);  // If same notificationId, overwrites the previous pendingIntent

        pendingNotifications.add(pendingIntent);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeToTrigger, pendingIntent);
    }


}
