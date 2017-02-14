package com.twinc.halmato.autowhatsappmessage.Notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.twinc.halmato.autowhatsappmessage.MainActivity;
import com.twinc.halmato.autowhatsappmessage.R;
import com.twinc.halmato.autowhatsappmessage.WeekdayNotificationPreference;

import java.util.Calendar;

/**
 * Created by Tiaan on 1/30/2017.
 */

public class NotificationUtilities {

    public static final int AMOUNT_OF_NOTIFICATIONS_SET_IN_ADVANCE = 3;


    public static boolean weekdayCheckboxIsChecked(Context context, int dayIndex) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String weekdayKey = WeekdayNotificationPreference.WEEKDAY_KEYS[dayIndex];

        boolean isChecked = sharedPreferences.getBoolean(weekdayKey,false);

        //Toast.makeText(context, weekdayKey + " is checked: " +isChecked, Toast.LENGTH_SHORT).show();

        return isChecked;
    }

    public static long[] calculateDelaysToNextSetScheduledNotificationsInMillis(Context context, int amountInAdvance) {

        long[] delays = new long[amountInAdvance];
        int currentDelayIndex = 0;

        Calendar c = Calendar.getInstance();

        int currentDayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;  // -1 because the base of Calendar.DAY_OF_WEEK is 1, and I want it 0
        long currentHourInMillis = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
        long currentMinuteInMillis = c.get(Calendar.MINUTE) * 60 * 1000;
        long currentTime = currentHourInMillis + currentMinuteInMillis;
        long timeOfScheduledNotification = getHourAndMinutesOfScheduledNotificationInMillis(context);

        int dayIterator= currentDayOfWeek;

        // add a day to the dayIterator since we already missed today's deadline
        if(currentTime > timeOfScheduledNotification) {
            dayIterator++;
        }

        // just for safety...do not think it is needed.
        int safeguardCheck = 0;
        int maxLoopsRequired = amountInAdvance * 7 + 1;

        while((currentDelayIndex < amountInAdvance) && (safeguardCheck++ < maxLoopsRequired)) {  // cannot go into infinite loop if at least one box is checked. Cannot come into this method if all boxes are unchecked.

            int weekdayIndex =  dayIterator % WeekdayNotificationPreference.AMOUNT_OF_DAYS_IN_WEEK;

            if(weekdayCheckboxIsChecked(context, weekdayIndex)) {

                long daysApartInMillis = (long)((dayIterator - currentDayOfWeek) * 24 * 60 * 60 * 1000);
                daysApartInMillis = daysApartInMillis + timeOfScheduledNotification - currentTime;
                delays[currentDelayIndex++] = daysApartInMillis;
            }

            dayIterator++;
        }

        if(safeguardCheck == maxLoopsRequired) {
            Toast.makeText(context, "ERROR#1: Did not find " + amountInAdvance + " scheduling times. Please report this error.", Toast.LENGTH_LONG).show();
        }

        return delays;
    }

    public static long getHourAndMinutesOfScheduledNotificationInMillis(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        long scheduledHourAndMinute = sharedPreferences.getLong(WeekdayNotificationPreference.NOTIFICATION_TIME_KEY, 0l);

        return scheduledHourAndMinute;
    }

    public static long addDelayToCurrentTime(long delay) {
        return System.currentTimeMillis() + delay;
    }

    public static void setNextSetScheduledNotifications(Context context, int amountInAdvance) {

        long[] delays = calculateDelaysToNextSetScheduledNotificationsInMillis(context,amountInAdvance);
        int notificationId = 0;

        for ( long delay : delays) {

            Toast.makeText(context,"Notification scheduled in \n"+((int)(delay/1000/60/60/24)) + " days, "+ ((int)(delay / 1000 / 60 / 60) % 24) + " hours, " + String.valueOf(((delay / 1000 / 60) % 60)) + " minutes", Toast.LENGTH_SHORT).show();

            long timeToTrigger = addDelayToCurrentTime(delay);

            NotificationScheduler.scheduleNotification(context, notificationId++, timeToTrigger);
        }
    }

    public static boolean notificationPreferenceIsActive(Context context) {

        String notificationActiveKey = context.getResources().getString(R.string.notifications_settings_key);

        boolean isActive = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(notificationActiveKey,false);

        return isActive;
    }

    public static boolean atLeastOneNotificationDayIsSelected(Context context) {

        for (int i = 0; i < WeekdayNotificationPreference.AMOUNT_OF_DAYS_IN_WEEK; i++) {

            if(weekdayCheckboxIsChecked(context,i)) {

                return true;
            }
        }

        return false;
    }

    public static void setScheduledNotifications(Context context)  {

        NotificationScheduler.removeAllScheduledNotifications(context);

        if(notificationsShouldBeScheduled(context))
            setNextSetScheduledNotifications(context, AMOUNT_OF_NOTIFICATIONS_SET_IN_ADVANCE);
    }

    public static boolean notificationsShouldBeScheduled(Context context) {
        return notificationsAreActivated(context) && atLeastOneNotificationDayIsSelected(context);
    }

    public static boolean notificationsAreActivated(Context context) {
        return notificationPreferenceIsActive(context);
    }

}
