package com.twinc.halmato.autowhatsappmessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.twinc.halmato.autowhatsappmessage.Notifications.NotificationScheduler;
import com.twinc.halmato.autowhatsappmessage.Notifications.NotificationUtilities;


/**
 * Created by Tiaan on 12/13/2016.
 */

// onReceive gets called on boot
// which should schedule a notification to appear which would open the app if clicked
public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

    static final String SERVICE_OPERATION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (deviceJustBooted(intent))
            NotificationUtilities.setScheduledNotifications(context);
    }

    private boolean deviceJustBooted(Intent intent) {

        return intent.getAction().equals(SERVICE_OPERATION_BOOT_COMPLETED);
    }

    public static void test() {

    }

}
