package com.systsync.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import com.systsync.bubble.BubbleActivity;
import com.systsync.ui.MainActivity;

public class AppService extends Service {
    private static final String CHANNEL_ID = "benbook_service_channel";
    private static final int NOTIF_ID = 4001;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent openApp = new Intent(this, MainActivity.class);
        PendingIntent piApp = PendingIntent.getActivity(this, 0, openApp, PendingIntent.FLAG_IMMUTABLE);

        Intent openBubble = new Intent(this, BubbleActivity.class);
        openBubble.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piBubble = PendingIntent.getActivity(this, 1, openBubble, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
            new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);

        builder.setContentTitle("Benbook Companion Ready")
            .setContentText("Tap 🫧 Quick Capture to save snippets on top of any app")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentIntent(piApp)
            .addAction(android.R.drawable.ic_input_add, "🫧 Quick Capture", piBubble)
            .setOngoing(true);

        startForeground(NOTIF_ID, builder.build());
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID,
                "Benbook Floating Companion",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager mgr = getSystemService(NotificationManager.class);
            if (mgr != null) mgr.createNotificationChannel(ch);
        }
    }

    public static void start(Context ctx) {
        Intent i = new Intent(ctx, AppService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(i);
        } else {
            ctx.startService(i);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
