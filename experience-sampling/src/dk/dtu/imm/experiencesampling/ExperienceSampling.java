package dk.dtu.imm.experiencesampling;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import dk.dtu.imm.experiencesampling.services.ExperienceSamplingSetupService;

public class ExperienceSampling {

    private static final String TAG = "ExperienceSampling";

    public static void startExperienceSampling(Context context, String authPrefKey, String tokenPrefKey) {
        Log.d(TAG, "The experience sampling is started");
        Config config = new Config(authPrefKey, tokenPrefKey);
        ConfigUtils.saveConfigInPrefs(context, config);
        startExperienceSamplingService(context);
    }

    public static void startExperienceSampling(Context context, String authPrefKey, String tokenPrefKey, int questionPerDayLimit, long questionScheduleInterval, long gpsTimeout) {
        Log.d(TAG, "The experience sampling is started with params");
        Config config = new Config(authPrefKey, tokenPrefKey, questionPerDayLimit, questionScheduleInterval, gpsTimeout);
        ConfigUtils.saveConfigInPrefs(context, config);
        startExperienceSamplingService(context);
    }

    private static void startExperienceSamplingService(Context context) {
        Intent intent = new Intent(context, ExperienceSamplingSetupService.class);
        context.startService(intent);
    }
}
