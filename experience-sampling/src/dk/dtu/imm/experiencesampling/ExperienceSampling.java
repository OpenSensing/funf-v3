package dk.dtu.imm.experiencesampling;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.imm.experiencesampling.services.ExperienceSamplingSetupService;

import java.io.IOException;

public class ExperienceSampling {

    private static final String TAG = "ExperienceSampling";

    private static final String JSON_KEY_PROBE_KEY = "dataRequests";
    private static final String JSON_KEY_EXPERIENCE_SAMPLING_PROBE = "edu.mit.media.funf.probe.builtin.ExperienceSamplingProbe";

    public static void startExperienceSampling(Context context, String authPrefKey, String tokenPrefKey, long questionScheduleInterval, long gpsTimeout, String mainConfigJson) {
        Log.d(TAG, "The experience sampling is started with params");

        Integer dailyQuestionLimit = getDailyQuestionLimit(mainConfigJson);
        Long friendsUpdateInterval = getFriendsUpdateInterval(mainConfigJson);
        String tokenSubsetLetters = getTokenSubsetLetters(mainConfigJson);

        Config config = new Config(authPrefKey, tokenPrefKey, dailyQuestionLimit, questionScheduleInterval, gpsTimeout, friendsUpdateInterval, tokenSubsetLetters);
        ConfigUtils.saveConfigInPrefs(context, config);
        startExperienceSamplingService(context);
    }

    private static void startExperienceSamplingService(Context context) {
        Intent intent = new Intent(context, ExperienceSamplingSetupService.class);
        context.startService(intent);
    }

    public static Integer getDailyQuestionLimit(String mainConfigJson) {
        JsonNode jsonNode = getValueFromExperienceSamplingProbeConfig(mainConfigJson, "DAILY_LIMIT");
        if (jsonNode != null && jsonNode.isInt()) {
            return jsonNode.intValue();
        }
        return null;
    }

    public static Long getFriendsUpdateInterval(String mainConfigJson) {
        JsonNode jsonNode = getValueFromExperienceSamplingProbeConfig(mainConfigJson, "FRIENDS_UPDATE_INTERVAL");
        if (jsonNode != null && (jsonNode.isInt() || jsonNode.isLong() || jsonNode.isBigDecimal() )) {
            return jsonNode.longValue();
        }
        return null;
    }

    public static String getTokenSubsetLetters(String mainConfigJson) {
        JsonNode jsonNode = getValueFromExperienceSamplingProbeConfig(mainConfigJson, "TOKEN_SUBSET_LETTERS");
        if (jsonNode != null) {
            return jsonNode.textValue();
        }
        return null;
    }

    private static JsonNode getValueFromExperienceSamplingProbeConfig(String mainConfigJson, String field) {
        if (mainConfigJson != null) {
            try {
                final JsonNode arrNode = new ObjectMapper().readTree(mainConfigJson).get(JSON_KEY_PROBE_KEY).get(JSON_KEY_EXPERIENCE_SAMPLING_PROBE);
                if (arrNode != null && arrNode.isArray()) {
                    for (final JsonNode objNode : arrNode) {
                        if (objNode.has(field)) {
                            return objNode.get(field);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading config values from json");
            }
        }
        return null;
    }
}
