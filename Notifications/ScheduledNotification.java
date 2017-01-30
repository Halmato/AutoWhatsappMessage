package com.twinc.halmato.autowhatsappmessage.Notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.twinc.halmato.autowhatsappmessage.MainActivity;
import com.twinc.halmato.autowhatsappmessage.R;

// Creates a notification immediately.
// Do not add the delay/scheduling here.
public class ScheduledNotification extends BroadcastReceiver {

    private static int DEFAULT_NOTIFICATION_ID = 0;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onReceive(Context context, Intent intent) {

        Intent openApplicationIntent = new Intent(context,MainActivity.class);
        PendingIntent openApplicationPendingIntent = PendingIntent.getActivity(context, 0, openApplicationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_fab_plus)
                .setContentText("CONTENT")
                .setContentTitle("TITLE")
                .setContentIntent(openApplicationPendingIntent)
                .setAutoCancel(true)
                .build();
        notification.defaults = Notification.DEFAULT_ALL;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, notification);
    }
}
