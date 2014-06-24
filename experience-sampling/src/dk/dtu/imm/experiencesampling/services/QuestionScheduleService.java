package dk.dtu.imm.experiencesampling.services;

import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import dk.dtu.imm.experiencesampling.Config;
import dk.dtu.imm.experiencesampling.ConfigUtils;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class QuestionScheduleService extends Service {

    private static final String TAG = "QuestionScheduleService";

    public static final String PREF_QUESTION_TIMESTAMPS_KEY = "question_timestamps";
    private static final int TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;

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

        if (isTokenPartOfSubset()) {
            Log.d(TAG, "Token is a part of subset. Questions will be asked");
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            if (!isDailyQuestionsLimitReached() && isTimeForQuestion()) {
                Log.d(TAG, "Time for question");

                // Check database
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (getApplicationContext() != null) {
                            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());

                            // Check if there are pending questions. If not, start prepare service and stop this service.
                            if (dbHelper.getPendingQuestionsCount() < 1) {
                                Log.d(TAG, "No more pending questions - starting prepare question service");
                                Intent prepareQuestionsService = new Intent(getApplicationContext(), QuestionsPrepareService.class);
                                getApplicationContext().startService(prepareQuestionsService);
                                dbHelper.closeDatabase();
                                stopSelf();
                            } else {
                                // Register screen receiver
                                if (mDisplayReceiver == null) {
                                    mDisplayReceiver = new ScreenReceiver();
                                }
                                registerReceiver(mDisplayReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
                                registerReceiver(mDisplayReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
                            }
                            dbHelper.closeDatabase();
                        }
                    }
                }).start();

            } else {
                Log.d(TAG, "NOT time for question");
                stopSelf();
            }
        } else {
            Log.d(TAG, "Token not a part of subset. Questions will not be asked");
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
    }

    private void saveQuestionAttemptTimestamp() {
        Set<String> timestamps = new HashSet<String>(sharedPrefs.getStringSet(PREF_QUESTION_TIMESTAMPS_KEY, new HashSet<String>()));
        timestamps.add(Long.toString(new Date().getTime()));
        sharedPrefs.edit().putStringSet(PREF_QUESTION_TIMESTAMPS_KEY, timestamps).apply();
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

        double distributionFactor = 24.0 / ConfigUtils.getConfigFromPrefs(getApplicationContext()).getDailyQuestionLimit();
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
        return timestamps.size() > ConfigUtils.getConfigFromPrefs(getApplicationContext()).getDailyQuestionLimit();
    }

    private boolean isTokenPartOfSubset() {
        Config config = ConfigUtils.getConfigFromPrefs(this);
        String token = ConfigUtils.getSensibleAccessToken(this).trim();

        if (token != null && token.length() > 0) {
            String firstTokenLetter = token.substring(0,1);

            String allowedTokenLetters = config.getTokenSubsetLetters();
            if (allowedTokenLetters != null && allowedTokenLetters.length() > 0) {
                if (allowedTokenLetters.equalsIgnoreCase("all")) {
                    return true;
                } else {
                    String[] allowedLetters = allowedTokenLetters.split(",");
                    for (String allowedLetter : allowedLetters) {
                        allowedLetter = allowedLetter.trim();
                        if (firstTokenLetter.equalsIgnoreCase(allowedLetter)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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

                    // Start service which collects info and fires the question
                    Intent serviceIntent = new Intent(context, QuestionLaunchService.class);
                    context.startService(serviceIntent);

                    // Save question attempt timestamp
                    saveQuestionAttemptTimestamp();

                    // Stop service after question is started
                    stopSelf();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onReceive during start of QuestionLaunchService: " + e.getMessage());
            }
        }
    }

}
