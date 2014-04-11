package dk.dtu.imm.experiencesampling.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;
import dk.dtu.imm.experiencesampling.external.SensibleDtuService;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestion;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * This class receives some json from a server or the database with generated answers
 * Then it schedules the question to be asked. // todo
 */
public class QuestionsPrepareService extends IntentService {

    private static final String TAG = "QuestionsPrepareService";

    private DatabaseHelper dbHelper;

    public QuestionsPrepareService() {
        super("QuestionsPrepareService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        dbHelper = new DatabaseHelper(this);

        // Receive new pending questions and store them as pending questions
        SensibleDtuService sensibleDtuService = new SensibleDtuService(getApplicationContext());
        try {
            Set<PendingQuestion> pendingQuestions = sensibleDtuService.getPendingQuestions();
            dbHelper.insertPendingQuestions(pendingQuestions);
        } catch (IOException e) {
            Log.e(TAG, "Error while processing pending questions json");
        }
        dbHelper.closeDatabase();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDatabase();
        }
    }
}
