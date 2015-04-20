package dk.dtu.imm.goactive;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * User: radugatej
 */
public class BluetoothTimeoutBroadcastReceiver extends BroadcastReceiver{
    private static final int NOTIFICATION_ID = 2;
    public static final String RESTART_POPUP_ACTION = "dk.dtu.imm.datacollector2013.restart_bluetooth_popup";

    @Override
    public void onReceive(Context context, Intent intent) {
        sendRestartNotification(context);
    }

    private void sendRestartNotification(Context context) {
        NotificationManager notificationService = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(RESTART_POPUP_ACTION);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String restartMessage = MainActivity.RESTART_DEVICE_MESSAGE;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.red_logo5)
                        .setContentTitle(MainActivity.RESTART_DEVICE_MESSAGE_TITLE)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(restartMessage))
                        .setContentText(restartMessage)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_ALL;
        notificationService.notify(NOTIFICATION_ID, notification);
    }
}
