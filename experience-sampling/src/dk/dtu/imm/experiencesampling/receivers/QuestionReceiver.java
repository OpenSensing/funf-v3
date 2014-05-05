package dk.dtu.imm.experiencesampling.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import dk.dtu.imm.experiencesampling.services.QuestionScheduleService;

public class QuestionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Receives boot complete- and question intents only.
        Intent scheduleQuestionService = new Intent(context, QuestionScheduleService.class);
        context.startService(scheduleQuestionService);
    }

}
