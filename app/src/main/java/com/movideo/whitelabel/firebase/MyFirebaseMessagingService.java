package com.movideo.whitelabel.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.movideo.whitelabel.MainActivity;
import com.movideo.whitelabel.R;

import static android.content.ContentValues.TAG;

/**
 * Created by ThanhTam on 11/30/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        sendNotification(remoteMessage.getNotification().getBody());
    }

    public void sendNotification(String message){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotification = new NotificationCompat.Builder(this);
        mNotification.setContentTitle("Danet")
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_ALL) // must requires VIBRATE permission
                    .setPriority(NotificationCompat.PRIORITY_HIGH)  //must give priority to High, Max which will considered as heads-up notification
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if((android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.LOLLIPOP) ||
                (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) ){
            mNotification.setSmallIcon(R.drawable.danet_icon_transparent_2);
            mNotification.setColor(Color.parseColor("#9aca3c"));
        }
        else
            mNotification.setSmallIcon(R.drawable.danet_icon);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,mNotification.build());
        //turnScreenOn(5,getApplicationContext());

    }

//    public static void turnScreenOn(int sec, final Context context)
//    {
//        final int seconds = sec;
//
//        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
//        boolean isScreenOn = pm.isScreenOn();
//
//        if( !isScreenOn )
//        {
//            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
//            wl.acquire(seconds*1000);
//            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
//            wl_cpu.acquire(seconds*1000);
//        }
//    }
}
