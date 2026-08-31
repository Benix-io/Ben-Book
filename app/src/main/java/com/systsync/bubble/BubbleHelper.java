package com.systsync.bubble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import com.systsync.R;

public class BubbleHelper {
    private static final String CHANNEL_ID = "benbook_bubbles";

    public static void displayBubble(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Ben Book Floating Capture",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setAllowBubbles(true);
            manager.createNotificationChannel(channel);
        }

        Intent bubbleIntent = new Intent(context, BubbleActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            bubbleIntent,
            PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Icon icon = Icon.createWithResource(context, android.R.drawable.ic_input_add);

        Person bot = new Person.Builder()
            .setBot(true)
            .setName("Ben Book Quick Capture")
            .setIcon(icon)
            .setKey("benbook_capture_shortcut")
            .build();

        Notification.BubbleMetadata bubbleMetadata = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bubbleMetadata = new Notification.BubbleMetadata.Builder(pendingIntent, icon)
                .setDesiredHeight(600)
                .setAutoExpandBubble(true)
                .setSuppressNotification(false)
                .build();
        }

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }

        builder.setContentTitle("Ben Book Floating Note")
            .setContentText("Tap to capture code or notes on top of any app")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setStyle(new Notification.MessagingStyle(bot).addMessage("Capture Snippet", System.currentTimeMillis(), bot))
            .setContentIntent(pendingIntent);

        if (bubbleMetadata != null) {
            builder.setBubbleMetadata(bubbleMetadata);
        }

        manager.notify(1001, builder.build());
    }
}
