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
    public void onReceive(Context context, Intent intent) {

        PendingIntent pendingIntent = createOpenApplicationPendingIntent(context, MainActivity.class);

        showNotification(context, pendingIntent);
    }

    private PendingIntent createOpenApplicationPendingIntent(Context context, Class classToOpen) {

        Intent openApplicationIntent = new Intent(context,classToOpen);
        return PendingIntent.getActivity(context, 0, openApplicationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showNotification(Context context,PendingIntent pendingIntent) {

        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_fab_plus)
                .setContentText("CONTENT")
                .setContentTitle("TITLE")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        notification.defaults = Notification.DEFAULT_ALL;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, notification);
    }
}
