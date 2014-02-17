package dk.dtu.imm.datacollector2013;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * User: radugatej
 */
public class BluetoothTimeoutBroadcastReceiver extends BroadcastReceiver{
    private static final int NOTIFICATION_ID = 2;
    public static final String RESTART_POPUP_ACTION = "dk.dtu.imm.datacollector2013.restart_bluetooth_popup";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Radu's test", "Broadcast received");
        sendRestartNotification(context);
    }

    private void sendRestartNotification(Context context) {
        NotificationManager notificationService = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(RESTART_POPUP_ACTION);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String restartMessage = "Due to a bug in Android 4.4 Bluetooth data collection is affected. Please update your device in order to fix the issue";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.red_logo5)
                        .setContentTitle("Restart device")
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
