package com.twinc.halmato.autowhatsappmessage.Notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.twinc.halmato.autowhatsappmessage.MainActivity;
import com.twinc.halmato.autowhatsappmessage.WeekdayNotificationPreference;

import java.util.ArrayList;
import java.util.Calendar;
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

    public static void scheduleNotificationOnceOff(Context context, long delay, int notificationId) {

        long timeToTrigger = System.currentTimeMillis() + delay;

        Intent alarmIntent = new Intent(context, ScheduledNotification.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, alarmIntent, 0);  // If same notificationId, overwrites the previous pendingIntent

        // Add pendingIntent to list so that I can cancel if needed
        pendingNotifications.add(pendingIntent);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,timeToTrigger,pendingIntent);
    }

    public static void setNextSetScheduledNotifications(Context context, int amountInAdvance) {

        long[] delays = calculateDelaysToNextSetScheduledNotificationsInMillis(context,amountInAdvance);
        int notificationId = 0;

        for ( long delay : delays) {

            Toast.makeText(context,"Notification scheduled in \n"+((int)(delay/1000/60/60/24)) + " days, "+ ((int)(delay / 1000 / 60 / 60) % 24) + " hours, " + String.valueOf(((delay / 1000 / 60) % 60)) + " minutes", Toast.LENGTH_SHORT).show();

            scheduleNotificationOnceOff(context, delay, notificationId++);
        }
    }

    private static long[] calculateDelaysToNextSetScheduledNotificationsInMillis(Context context, int amountInAdvance) {

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

    private static long calculateDelayToNextScheduledNotificationInMillis(Context context) {

        long delay = 0;

        Calendar c = Calendar.getInstance();

        int currentDayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;  // -1 because the base of Calendar.DAY_OF_WEEK is 1, and I want it 0
        long currentHourInMillis = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
        long currentMinuteInMillis = c.get(Calendar.MINUTE) * 60 * 1000;
        long currentTime = currentHourInMillis + currentMinuteInMillis;
        long timeOfScheduledNotification = getHourAndMinutesOfScheduledNotificationInMillis(context);

        int start = currentDayOfWeek;
        // if current time is later than time set on the timer, add a day to the currentDayOfWeek since we already missed today's deadline
        if(currentTime > timeOfScheduledNotification) {
            start++;
        }

        // Look for the next closest checkbox that is checked
        for (int i = start; i < start + WeekdayNotificationPreference.AMOUNT_OF_DAYS_IN_WEEK; i++) {

            int dayIndex =  i % WeekdayNotificationPreference.AMOUNT_OF_DAYS_IN_WEEK;

            if(weekdayCheckboxIsChecked(context, dayIndex)) {

                long daysApartInMillis = (long)((i - currentDayOfWeek) * 24 * 60 * 60 * 1000);
                delay += daysApartInMillis;
                break;
            }
        }

        delay += timeOfScheduledNotification - currentTime;

        return delay;
    }

    private static long getHourAndMinutesOfScheduledNotificationInMillis(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        long scheduledHourAndMinute = sharedPreferences.getLong(WeekdayNotificationPreference.NOTIFICATION_TIME_KEY, 0l);

        return scheduledHourAndMinute;
    }

    public static boolean weekdayCheckboxIsChecked(Context context, int dayIndex) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String weekdayKey = WeekdayNotificationPreference.WEEKDAY_KEYS[dayIndex];

        boolean isChecked = sharedPreferences.getBoolean(weekdayKey,false);

        //Toast.makeText(context, weekdayKey + " is checked: " +isChecked, Toast.LENGTH_SHORT).show();

        return isChecked;
    }



    public static void setScheduledNotifications(Context context)  {

        if(notificationsShouldBeScheduled(context))
            setNextSetScheduledNotifications(context, MainActivity.AMOUNT_OF_NOTIFICATIONS_SET_IN_ADVANCE);
    }

    private static boolean notificationsShouldBeScheduled(Context context) {
        return notificationsAreActivated(context) && WeekdayNotificationPreference.atLeastOneNotificationDayIsSelected(context);
    }

    private static boolean notificationsAreActivated(Context context) {
        return WeekdayNotificationPreference.notificationPreferenceIsActive(context);
    }
}
