package dk.dtu.imm.experiencesampling.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import dk.dtu.imm.experiencesampling.Config;
import dk.dtu.imm.experiencesampling.ConfigUtils;
import dk.dtu.imm.experiencesampling.QuestionScheduleUtils;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;

public class QuestionScheduleService extends Service {

    private static final String TAG = "QuestionScheduleService";

    private BroadcastReceiver mDisplayReceiver;

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

            boolean questionTime = false;
            try {
                // Schedules question for the next day if they are not already scheduled.
                QuestionScheduleUtils.scheduleNextDayQuestions(getBaseContext());

                // Checks if it is question time.
                questionTime = QuestionScheduleUtils.isQuestionTime(getBaseContext());
            } catch (Exception e) {
                Log.e(TAG, "Error while scheduling or checking question time:" + e.getMessage());
                e.printStackTrace();
            }

            if (questionTime) {
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

                    // Stop service after question is started
                    stopSelf();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onReceive during start of QuestionLaunchService: " + e.getMessage());
            }
        }
    }

}
