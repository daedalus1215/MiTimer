/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.miproducts.mitimer;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import com.miproducts.mitimer.util.Constants;

/**
 * Service class that manages notifications of the timer.
 */
public class TimerNotificationService extends IntentService {

    public static final String TAG = "TimerNotificationSvc";

    private Vibrator vibrator;

    public TimerNotificationService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onHandleIntent called with intent: " + intent);
        }
        String action = intent.getAction();
        if (Constants.ACTION_SHOW_ALARM.equals(action)) {
            showTimerDoneNotification();
        } else if (Constants.ACTION_DELETE_ALARM.equals(action)) {
            deleteTimer();
        } else if (Constants.ACTION_RESTART_ALARM.equals(action)) {
            restartAlarm();
        } else if(Constants.ACTION_START.equals(action)){
            log("started Service");


        }

        else {
            throw new IllegalStateException("Undefined constant used: " + action);
        }
    }

    private void restartAlarm() {
        log("Timer restarted.");

        Intent dialogIntent = new Intent(this, MainActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Timer restarted.");
        }
        if(vibrator != null)
            if(vibrator.hasVibrator())
                vibrator.cancel();            }

    private void deleteTimer() {
        log("deleteTimer");

        cancelCountdownNotification();

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(Constants.ACTION_SHOW_ALARM, null, this,
                TimerNotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.cancel(pendingIntent);

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Timer deleted.");
        }
        if(vibrator != null)
            if(vibrator.hasVibrator())
                vibrator.cancel();
    }

    private void cancelCountdownNotification() {
        log("CancelCountdownNotificatioj");

        NotificationManager notifyMgr =
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        notifyMgr.cancel(Constants.NOTIFICATION_TIMER_COUNTDOWN);
        if(vibrator != null)
            if(vibrator.hasVibrator())
                vibrator.cancel();
    }

    private void showTimerDoneNotification() {
        log("showTimerDoneNotification");
        // Cancel the countdown notification to show the "timer done" notification.
        cancelCountdownNotification();
        vibrate();
        // Create an intent to restart a timer.
        Intent restartIntent = new Intent(Constants.ACTION_RESTART_ALARM, null, this,
                TimerNotificationService.class);
        PendingIntent pendingIntentRestart = PendingIntent
                .getService(this, 0, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create notification that timer has expired.
        NotificationManager notifyMgr =
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        Notification notif = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Timer Done")
                .setContentText("Timer Done")
                .setUsesChronometer(true)
                .setWhen(System.currentTimeMillis())
                .addAction(android.R.drawable.ic_lock_idle_alarm, "Timer Restart",
                        pendingIntentRestart)
                .setLocalOnly(true)
                .build();
        notifyMgr.notify(Constants.NOTIFICATION_TIMER_EXPIRED, notif);

    }

    private void log(String showTimerDoneNotification) {
        Log.d("TimerNotificationServ", showTimerDoneNotification);
    }

    private void vibrate() {
        // 1 - don't repeat
        final int indexInPatternToRepeat = 1;
        //vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
        vibrator.vibrate(7000);
    }

}
