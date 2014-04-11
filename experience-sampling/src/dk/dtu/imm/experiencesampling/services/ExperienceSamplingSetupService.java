package dk.dtu.imm.experiencesampling.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import dk.dtu.imm.experiencesampling.ConfigUtils;
import dk.dtu.imm.experiencesampling.Config;

public class ExperienceSamplingSetupService extends IntentService {

    private static final String TAG = "ExperienceSamplingSetupService";

    public ExperienceSamplingSetupService() {
        super("ExperienceSamplingSetupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Config config = ConfigUtils.getConfigFromPrefs(this);
        setupScheduleQuestionAlarm(this, config.getQuestionScheduleInterval());
        return super.onStartCommand(intent, flags, startId);
    }

    private static void setupScheduleQuestionAlarm(Context context, long interval) {
        Log.d(TAG, "Setup question schedule with interval: " + interval);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent receiverIntent = new Intent(ConfigUtils.QUESTION_INTENT);
        PendingIntent pendingReceiverIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, interval, pendingReceiverIntent);
    }
}
