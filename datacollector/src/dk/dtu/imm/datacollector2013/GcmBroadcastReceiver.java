package dk.dtu.imm.datacollector2013;

/**
 * Created with IntelliJ IDEA.
 * User: piotr
 * Date: 7/18/13
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Handling of GCM messages.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
    static final String TAG = "AUTH_AuthActivity_broadcastReceiver";
    public static final String EVENT_MSG_RECEIVED = "EVENT_MSG_RECEIVED";
    
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    Context ctx;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received: " + intent.getExtras().toString());
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        ctx = context;
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            sendNotification("Error while sending", "Send error: " + intent.getExtras().toString());
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            sendNotification("Deleted messages", "Deleted messages on server: " +
                    intent.getExtras().toString());
        } else {
        	Intent notifyIntent = new Intent(EVENT_MSG_RECEIVED);
            Bundle extras = intent.getExtras();
        	notifyIntent.putExtras(extras);
        	LocalBroadcastManager.getInstance(context).sendBroadcast(notifyIntent);
            if (extras.getString("type", "").equals("url")) {
                sendUrlNotification(extras.getString("title"),
                        extras.getString("message"),
                        extras.getString("url"));
            } else {
                sendNotification(intent.getExtras().getString("title", "SensibleDTU"),
                        intent.getExtras().getString("message"));
            }
        }
        setResultCode(Activity.RESULT_OK);
    }

    // Put the GCM message into a notification and post it.
    private void sendNotification(String title, String msg) {
        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                new Intent(ctx, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.red_logo5)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }    // Put the GCM message into a notification and post it.

    private void sendUrlNotification(String title, String msg, String url) {
        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                new Intent(Intent.ACTION_VIEW, Uri.parse(url)), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.red_logo5)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
