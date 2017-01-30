package com.twinc.halmato.autowhatsappmessage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;

import com.twinc.halmato.autowhatsappmessage.Notifications.NotificationScheduler;
import com.twinc.halmato.autowhatsappmessage.Notifications.NotificationUtilities;

/**
 * Created by Tiaan on 12/9/2016.
 */

public class WeekdayNotificationPreference extends DialogPreference {

    public static final String NOTIFICATION_TIME_KEY = "NOTIFICATION_TIME_KEY";

    public static final int AMOUNT_OF_DAYS_IN_WEEK = 7;
    public static final String[] WEEKDAY_KEYS = {
            "SundayKey",
            "MondayKey",
            "TuesdayKey",
            "WednesdayKey",
            "ThursdayKey",
            "FridayKey",
            "SaturdayKey"
    };

    private Resources resources = getContext().getResources();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private View weekdayPickerView;
    private TimePicker timePickerView;

    private CheckBox[] weekdaysCheckboxes = new CheckBox[7];

    // Constructors
    public WeekdayNotificationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public WeekdayNotificationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    @Override
    protected View onCreateDialogView() {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        weekdayPickerView = inflater.inflate(R.layout.weekday_picker_layout,null,false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferencesEditor = sharedPreferences.edit();

        initializeComponents(weekdayPickerView);

        setInitialState();

        return weekdayPickerView;
    }

    private void initializeComponents(View view) {

        initializeTimePicker(view);
        initializeWeekdayCheckBoxes(view);
    }
    private void initializeTimePicker(View view) {

        timePickerView = (TimePicker) view.findViewById(R.id.simpleTimePicker);
    }
    private void initializeWeekdayCheckBoxes(View view) {

        int[] checkBoxIDs = {
                R.id.cb_sunday,
                R.id.cb_monday,
                R.id.cb_tuesday,
                R.id.cb_wednesday,
                R.id.cb_thursday,
                R.id.cb_friday,
                R.id.cb_saturday
        };

        for (int i = 0; i < 7; i++) {
            CheckBox cb = (CheckBox)view.findViewById(checkBoxIDs[i]);
            weekdaysCheckboxes[i] = cb;
        }
    }

    private void setInitialState()  {

        setInitialStateOfWeekdayCheckBoxes();
        setInitialStateOfTimePicker();
    }
    private void setInitialStateOfWeekdayCheckBoxes() {

        for (int i = 0; i < weekdaysCheckboxes.length; i++) {
            weekdaysCheckboxes[i].setChecked(getWeekdaySharedPreferenceCheckedState(i));
        }
    }
    private void setInitialStateOfTimePicker() {

        int time = (int)getTimePickerTimeSharedPreferencesInMillis();
        int hour =  time / (60*60*1000);
        int minute =  (time % (60*60*1000)) / ( 60 * 1000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerView.setHour(hour);
            timePickerView.setMinute(minute);
        }
    }

    private long getTimePickerTimeSharedPreferencesInMillis() {
        return sharedPreferences.getLong(NOTIFICATION_TIME_KEY,0);
    }
    private boolean getWeekdaySharedPreferenceCheckedState(int checkBoxIndex){
        return sharedPreferences.getBoolean(WEEKDAY_KEYS[checkBoxIndex],false);
    }

    @Override
    protected void onDialogClosed(boolean closedWithPositiveResult){
        if(closedWithPositiveResult) {
            updateWeekdayPickerSharedPreferences();
            updateTimePickerSharedPreferences();
        }
        else {
            setInitialState();
        }

        NotificationUtilities.setScheduledNotifications(getContext());
    }

    private void updateWeekdayPickerSharedPreferences() {

        for (int i = 0; i < weekdaysCheckboxes.length; i++) {

              sharedPreferencesEditor.putBoolean(WEEKDAY_KEYS[i],weekdaysCheckboxes[i].isChecked());
        }

        sharedPreferencesEditor.commit();
    }

    private void updateTimePickerSharedPreferences() {
        int selectedTime = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            selectedTime += timePickerView.getHour() * 60 * 60 * 1000;
            selectedTime += timePickerView.getMinute() * 60 * 1000;
        }

        long time = (long)selectedTime;

        sharedPreferencesEditor.putLong(NOTIFICATION_TIME_KEY,time).commit();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 1);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

    }

}
