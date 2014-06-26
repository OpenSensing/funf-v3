package dk.dtu.imm.experiencesampling.activities;

import android.app.Fragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import dk.dtu.imm.experiencesampling.QuestionScheduleUtils;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.fragments.questions.*;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.Place;
import dk.dtu.imm.experiencesampling.models.answers.*;
import dk.dtu.imm.experiencesampling.services.QuestionSaveService;

import java.util.ArrayList;

public class QuestionActivity extends BaseActivity {

    private static final String TAG = "QuestionActivity";

    private boolean hasBeenVisible;

    Fragment questionFragment;
    QuestionType type;
    ArrayList<Friend> friends;
    ArrayList<Place> topPlaces;
    ArrayList<Place> allPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        Log.i(TAG, "Question launched");

        if (savedInstanceState == null) {
            Bundle extras = (getIntent() != null) ? getIntent().getBundleExtra("info") : null;
            if (extras != null) {
                type = (QuestionType) extras.getSerializable("type");
                if (type != null) {
                    switch (type) {
                        case SOCIAL_CLOSER_FRIEND:
                            friends = (ArrayList<Friend>) extras.getSerializable("friends");
                            if (friends != null && friends.size() > 1) {
                                questionFragment = QCloserFriendFragment.newInstance(friends.get(0), friends.get(1));
                            }
                            break;
                        case SOCIAL_RATE_ONE_FRIEND:
                            friends = (ArrayList<Friend>) extras.getSerializable("friends");
                            if (friends != null && friends.size() > 0) {
                                questionFragment = QRateOneFriendFragment.newInstance(friends.get(0));
                            }
                            break;
                        case SOCIAL_RATE_TWO_FRIENDS:
                            friends = (ArrayList<Friend>) extras.getSerializable("friends");
                            if (friends != null && friends.size() > 1) {
                                questionFragment = QRateTwoFriendsFragment.newInstance(friends.get(0), friends.get(1));
                            }
                            break;
                        case LOCATION_CURRENT:
                            topPlaces = (ArrayList<Place>) extras.getSerializable("top_places");
                            allPlaces = (ArrayList<Place>) extras.getSerializable("all_places");
                            questionFragment = QCurrentLocationFragment.newInstance(topPlaces, allPlaces);
                            break;
                        case LOCATION_PREVIOUS:
                            topPlaces = (ArrayList<Place>) extras.getSerializable("top_places");
                            allPlaces = (ArrayList<Place>) extras.getSerializable("all_places");
                            questionFragment = QPreviousLocationFragment.newInstance(topPlaces, allPlaces);
                            break;
                        default:
                            questionFragment = null;
                            break;
                    }
                }
            }

            if (questionFragment != null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, questionFragment)
                        .commit();
            } else {
                Log.w(TAG, "The fragment was null - Shouldn't be a problem, but check the log if the application crashed");
                finish(); // finish activity if there is no question fragment - should never happen, because current- or previous place can always be asked.
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If keyguard is unlocked here the activity has been seen by the user.
        if (!((KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()) {
            hasBeenVisible = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionFragment != null) {
            BaseQuestionFragment baseQuestionFragment = (BaseQuestionFragment) getFragmentManager().findFragmentById(questionFragment.getId());
            if (baseQuestionFragment != null && !baseQuestionFragment.isAnswered()) {
                AnswerType answerType;
                if (hasBeenVisible) {
                    Log.d(TAG, "Question destroyed: " + AnswerType.NOT_ANSWERED);
                    answerType = AnswerType.NOT_ANSWERED;
                } else {
                    Log.d(TAG, "Question destroyed: " + AnswerType.NEVER_SEEN);
                    answerType = AnswerType.NEVER_SEEN;
                    try {
                        QuestionScheduleUtils.addASAPQuestionTime(getBaseContext());
                    } catch (Exception e) {
                        Log.e(TAG, "Could not add ASAP question: " + e.getMessage());
                    }
                }

                // Save question with empty fields
                Answer answer = null;
                if (type != null) {
                    switch (type) {
                        case SOCIAL_CLOSER_FRIEND:
                            CloserFriend closerFriend = new CloserFriend();
                            closerFriend.setAnswerType(answerType);
                            closerFriend.setLoadedTimestamp(baseQuestionFragment.getLoadedTimestamp());
                            answer = closerFriend;
                            break;
                        case SOCIAL_RATE_ONE_FRIEND:
                            RateOneFriend rateOneFriend = new RateOneFriend();
                            rateOneFriend.setAnswerType(answerType);
                            rateOneFriend.setLoadedTimestamp(baseQuestionFragment.getLoadedTimestamp());
                            answer = rateOneFriend;
                            break;
                        case SOCIAL_RATE_TWO_FRIENDS:
                            RateTwoFriends rateTwoFriends = new RateTwoFriends();
                            rateTwoFriends.setAnswerType(answerType);
                            rateTwoFriends.setLoadedTimestamp(baseQuestionFragment.getLoadedTimestamp());
                            answer = rateTwoFriends;
                            break;
                        case LOCATION_CURRENT:
                            CurrentLocation currentLocation = new CurrentLocation();
                            currentLocation.setAnswerType(answerType);
                            currentLocation.setLoadedTimestamp(baseQuestionFragment.getLoadedTimestamp());
                            answer = currentLocation;
                            break;
                        case LOCATION_PREVIOUS:
                            PreviousLocation previousLocation = new PreviousLocation();
                            previousLocation.setAnswerType(answerType);
                            previousLocation.setLoadedTimestamp(baseQuestionFragment.getLoadedTimestamp());
                            answer = previousLocation;
                            break;
                        default:
                            questionFragment = null;
                            break;
                    }
                    saveQuestion(answer);
                }
            } else {
                Log.e(TAG, "Question destroyed: answered correct");
            }
        }
    }

    protected void saveQuestion(Answer answer) {
        if (answer != null) {
            Intent saveIntent = new Intent(this, QuestionSaveService.class);
            saveIntent.putExtra("answer", answer);
            startService(saveIntent);
        }
    }

}
