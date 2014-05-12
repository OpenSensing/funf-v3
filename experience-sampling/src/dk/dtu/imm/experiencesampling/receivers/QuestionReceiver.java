package dk.dtu.imm.experiencesampling.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import dk.dtu.imm.experiencesampling.Config;
import dk.dtu.imm.experiencesampling.ConfigUtils;
import dk.dtu.imm.experiencesampling.services.QuestionScheduleService;

public class QuestionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Config config = ConfigUtils.getConfigFromPrefs(context);
        // todo: check access token letter
        Log.e("Jeppe", "Main config: " + config.getDailyQuestionLimit() + ", " + config.getFriendsUpdateInterval() + ", " + config.getTokenSubsetLetters());

        // Receives boot complete- and question intents only.
        Intent scheduleQuestionService = new Intent(context, QuestionScheduleService.class);
        context.startService(scheduleQuestionService);
    }

}
