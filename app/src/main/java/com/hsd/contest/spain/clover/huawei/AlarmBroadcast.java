package com.hsd.contest.spain.clover.huawei;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

public class AlarmBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Click on Notification
        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Notification Builder
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent1, PendingIntent.FLAG_ONE_SHOT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "notify_001");

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);

        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        contentView.setOnClickPendingIntent(R.id.flashButton, pendingSwitchIntent);
        //contentView.setTextViewText(R.id.message, text);
        //contentView.setTextViewText(R.id.date, date);
        mBuilder.setSmallIcon(R.drawable.head);
        mBuilder.setColorized(true);
        mBuilder.setColor(context.getColor(R.color.colorTextBox));
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(false);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        mBuilder.setOnlyAlertOnce(false);
        //mBuilder.setContent(contentView);
        
        // TODO: Añadir el nombre del usuario y el del tratamiento.
        mBuilder.setContentText(context.getString(R.string.nueva_dosis));
        mBuilder.setSubText(context.getString(R.string.nueva_dosis_pendiente));
        
        // Cuando se haga clic en la notificación, lleva al usuario a este Intent.
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setGroup("groupkey");
        mBuilder.build().flags = Notification.PRIORITY_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(channelId, "channel name", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        Notification notification = mBuilder.build();
        notificationManager.notify(1, notification);
    }
}