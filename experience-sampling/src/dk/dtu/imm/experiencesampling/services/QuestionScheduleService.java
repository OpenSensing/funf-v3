package dk.dtu.imm.experiencesampling.services;

import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import dk.dtu.imm.experiencesampling.ConfigUtils;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class QuestionScheduleService extends Service {

    private static final String TAG = "QuestionScheduleService";

    private static final String PREF_QUESTION_TIMESTAMPS_KEY = "question_timestamps";
    private static final int TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;

    private DatabaseHelper dbHelper;
    private BroadcastReceiver mDisplayReceiver;
    private SharedPreferences sharedPrefs;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service started");
        dbHelper = new DatabaseHelper(this);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if (!isDailyQuestionsLimitReached() && isTimeForQuestion()) {
            Log.d(TAG, "Time for question");
            // Register screen receiver
            if (mDisplayReceiver == null) {
                mDisplayReceiver = new ScreenReceiver();
            }
            registerReceiver(mDisplayReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
            registerReceiver(mDisplayReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        } else {
            Log.d(TAG, "NOT time for question");
            // Stop service if it is not question time
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service stopped");
        if (mDisplayReceiver != null) {
            unregisterReceiver(mDisplayReceiver);
        }
        if (dbHelper != null) {
            dbHelper.closeDatabase();
        }
    }

    private void saveQuestionAttemptTimestamp() {
        Set<String> timestamps = new HashSet<String>(sharedPrefs.getStringSet(PREF_QUESTION_TIMESTAMPS_KEY, new HashSet<String>()));
        timestamps.add(Long.toString(new Date().getTime()));
        sharedPrefs.edit().putStringSet(PREF_QUESTION_TIMESTAMPS_KEY, timestamps).commit();
    }

    private long getLatestQuestionAttemptTimestamp() {
        Set<String> timestamps = new HashSet<String>(sharedPrefs.getStringSet(PREF_QUESTION_TIMESTAMPS_KEY, new HashSet<String>()));
        long latestTimestamp = 0;
        for (String strTimestamp : timestamps) {
            long timestamp = Long.parseLong(strTimestamp);
            if (timestamp > latestTimestamp) {
                latestTimestamp = timestamp;
            }
        }
        return latestTimestamp;
    }

    private boolean isTimeForQuestion() {
        boolean timeForQuestion = false;

        double distributionFactor = 24.0 / ConfigUtils.getConfigFromPrefs(getApplicationContext()).getQuestionPerDayLimit();
        int questionMillisInterval = (int) (distributionFactor * 60 * 60 * 1000); // 24h / answers, to better distribute the answers during the day.

        Date now = new Date();
        Date lastQuestion = new Date(getLatestQuestionAttemptTimestamp());

        long diff = now.getTime() - lastQuestion.getTime();
        if (diff > questionMillisInterval) {
            timeForQuestion = true;
        }
        return timeForQuestion;
    }

    private boolean isDailyQuestionsLimitReached() {
        Set<String> timestamps = new HashSet<String>(sharedPrefs.getStringSet(PREF_QUESTION_TIMESTAMPS_KEY, new HashSet<String>()));
        Log.d(TAG, "Questions timestamps within 24 hours: " + timestamps.size());
        Date now = new Date();
        Set<String> oldTimestamps = new HashSet<String>();

        // Removes all old timestamps if diff > 24h.
        for (String timestamp : timestamps) {
            Date oldestQuestionDate = new Date(Long.parseLong(timestamp));
            if (now.getTime() - oldestQuestionDate.getTime() > TWENTY_FOUR_HOURS) {
                oldTimestamps.add(timestamp);
            }
        }
        Log.d(TAG, "Total question timestamps removed: " + oldTimestamps.size());
        timestamps.removeAll(oldTimestamps);
        sharedPrefs.edit().putStringSet(PREF_QUESTION_TIMESTAMPS_KEY, timestamps).commit();
        return timestamps.size() > ConfigUtils.getConfigFromPrefs(getApplicationContext()).getQuestionPerDayLimit();
    }

    private class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.d(TAG, "Screen ON!");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.d(TAG, "Screen OFF! - launching question");

                    if (dbHelper.getPendingQuestionsCount() > 0) {
                        // Start service which collects info and fires the question
                        Intent serviceIntent = new Intent(context, QuestionLaunchService.class);
                        context.startService(serviceIntent);

                        // Save question attempt timestamp
                        saveQuestionAttemptTimestamp();
                    } else {
                        Log.d(TAG, "No more pending questions - starting prepare question service");
                        Intent prepareQuestionsService = new Intent(context, QuestionsPrepareService.class);
                        context.startService(prepareQuestionsService);
                    }

                    // Stop service after question is started
                    stopSelf();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onReceive during start of QuestionLaunchService: " + e.getMessage());
            }
        }
    }

}
