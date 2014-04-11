package dk.dtu.imm.experiencesampling;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ConfigUtils {

    private static final String TAG = "Config";

    public static final String PREF_CONFIG__KEY = "experience_sampling_config_key";
    public static final String QUESTION_INTENT = "dk.dtu.imm.experiencesampling.intent.QUESTION";

    // This is how funf obtains the access token too
    public static String getSensibleAccessToken(Context context) {
        Config config = getConfigFromPrefs(context);
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(config.getAuthPrefKey(), Context.MODE_PRIVATE);
        return prefs.getString(config.getTokenPrefKey(),"");
    }

    public static void saveConfigInPrefs(Context context, Config config) {
        String json = null;
        try {
            json = new ObjectMapper().writeValueAsString(config);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Unable to parse and store config");
        }
        Log.d(TAG, "Saving experience sampling config: " + json);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.edit().putString(PREF_CONFIG__KEY, json).commit();
    }

    public static Config getConfigFromPrefs(Context context) {
        Config config;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPrefs.getString(PREF_CONFIG__KEY, null);
        Log.d(TAG, "Loading experience sampling config: " + json);

        if (json != null) {
            try {
                config = new ObjectMapper().readValue(json, Config.class);
            } catch (IOException e) {
                Log.e(TAG, "Unable to read config - default returned");
                config = new Config();
            }
        } else {
            config = new Config();
        }
        return config;
    }

}
