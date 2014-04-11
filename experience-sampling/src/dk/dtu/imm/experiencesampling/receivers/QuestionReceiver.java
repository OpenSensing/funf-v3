package dk.dtu.imm.experiencesampling.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import dk.dtu.imm.experiencesampling.services.QuestionScheduleService;
import dk.dtu.imm.experiencesampling.services.QuestionsPrepareService;

// todo: not necessary when used within the sensible dtu data-collector
public class QuestionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Receives boot complete- and question intents only.
        Intent scheduleQuestionService = new Intent(context, QuestionScheduleService.class);
        context.startService(scheduleQuestionService);
    }

}
