package com.example.rc.operr_android_quizz_rcolon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import java.util.Objects;

public class CustomStickyService extends Service {
    private static final int SERVICE_ID = 555;
    private static final int SERVICE_DONE_NOTIFICATION_ID = 444;
    //    private static final int BREAK_DURATION = 1800000;  //30 minutes =  1,800,000 ms
    private static final int BREAK_DURATION = 10000;  //10 seconds =  10000 ms - for debugging purposes
    private static final String CHANNEL_ID = "CustomStickyService_channel";
    private static final String CHANNEL_NAME = "CustomStickyService";
    private static final String CHANNEL_DESCRIPTION = "CustomStickyService notification channel";
    private NotificationManager notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null;}

    public CustomStickyService() {}  //empty required constructor

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //init global notification service manager
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //init break countdown service notification builder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis() + BREAK_DURATION)  //set timer countdown to BREAK_DURATION into the future
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)  //allows for service to cancel notification itself
                .setOngoing(false)
                .setUsesChronometer(true)  //shows countdown timer based on setWhen
                .setContentTitle("Your are on break.");

        //Android API 26+ requires notification channel, with more notification features based on priority
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            Objects.requireNonNull(notificationManager).createNotificationChannel(notificationChannel);
        } else {  //for previous APIs, set priority to MAX
            notificationBuilder.setPriority(Notification.PRIORITY_MAX);
        }

        Notification notification = notificationBuilder.build();
        //notifies notification services about this service's notification
        //note: using "Objects.requireNonNull" only to ignore warnings
        Objects.requireNonNull(notificationManager).notify(SERVICE_ID, notification);
        //starts this service in the foreground thread (e.g. it will continue running regardless of exiting/hiding app)
        startForeground(SERVICE_ID, notification);
        initServiceStopper();  //see method documentation for details
        //returns flag to OS for keeping the Service running at all times or until stopped programmatically or killed by Android
        return START_STICKY;
    }

    /**
     * For the purpose of this exercise, we are firing up this other timed background thread to handle when the break time is up.  Possibly a more elegant or less intrusive alternative would be used in real case scenarios like: using a broadcaster, using an alarm manager, implementing a timer countdown CountDownTimer to use for updating the notification stopper timer and notify when it ends, etc.
     */
    private void initServiceStopper() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setContentText(getString(R.string.break_is_up))
                        .setSmallIcon(R.mipmap.ic_launcher);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    notificationBuilder.setPriority(Notification.PRIORITY_MAX);
                }
                notificationManager.notify(SERVICE_DONE_NOTIFICATION_ID, notificationBuilder.build());
            }
        }, BREAK_DURATION);
    }
}
