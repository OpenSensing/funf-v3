package dk.dtu.imm.experiencesampling.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import dk.dtu.imm.experiencesampling.activities.QuestionActivity;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.Place;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestion;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestionOneFriend;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestionTwoFriends;

import java.util.ArrayList;
import java.util.List;

/**
 * This service collects info for the next question and launches it.
 */
public class QuestionLaunchService extends IntentService {

    private static final String TAG = "QuestionLaunchService";

    private DatabaseHelper dbHelper;

    public QuestionLaunchService() {
        super("QuestionLaunchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);

        Bundle questionInfo = new Bundle();
        PendingQuestion pendingQuestion = dbHelper.popNextPendingQuestion();

        List<Friend> friends = new ArrayList<Friend>();
        List<Place> topPlaces = new ArrayList<Place>();
        List<Place> allPlaces = new ArrayList<Place>();

        if (pendingQuestion != null) {
            switch (pendingQuestion.getQuestionType()) {
                case SOCIAL_RATE_ONE_FRIEND:
                    friends.add(((PendingQuestionOneFriend) pendingQuestion).getFriend());
                    break;
                case SOCIAL_RATE_TWO_FRIENDS:
                case SOCIAL_CLOSER_FRIEND:
                    friends.add(((PendingQuestionTwoFriends) pendingQuestion).getFriendOne());
                    friends.add(((PendingQuestionTwoFriends) pendingQuestion).getFriendTwo());
                    break;
                case LOCATION_CURRENT:
                case LOCATION_PREVIOUS:
                    topPlaces = dbHelper.readTopPlaces(5);
                    allPlaces = dbHelper.readAllPlaces();
                    break;
            }

            Log.d(TAG, "Launching question: " + pendingQuestion.getQuestionType());

            questionInfo.putSerializable("type", pendingQuestion.getQuestionType());
            questionInfo.putSerializable("friends", (ArrayList<Friend>) friends);
            questionInfo.putSerializable("top_places", (ArrayList<Place>) topPlaces);
            questionInfo.putSerializable("all_places", (ArrayList<Place>) allPlaces);

            // Start question activity
            Intent questionActivity = new Intent(getApplicationContext(), QuestionActivity.class);
            questionActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            questionActivity.putExtra("info", questionInfo);

            if (getApplicationContext() != null) {
                getApplicationContext().startActivity(questionActivity);
            }
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
