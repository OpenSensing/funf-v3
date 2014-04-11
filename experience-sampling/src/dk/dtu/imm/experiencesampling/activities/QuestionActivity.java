package dk.dtu.imm.experiencesampling.activities;

import android.app.Fragment;
import android.os.Bundle;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.fragments.questions.*;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.Place;

import java.util.ArrayList;

public class QuestionActivity extends BaseActivity {

    Fragment questionFragment;
    ArrayList<Friend> friends;
    ArrayList<Place> topPlaces;
    ArrayList<Place> allPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        if (savedInstanceState == null) {
            Bundle extras = (getIntent() != null) ? getIntent().getBundleExtra("info") : null;
            if (extras != null) {
                QuestionType type = (QuestionType) extras.getSerializable("type");
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
                finish(); // finish activity if there is no question fragment - should never happen, because current- or previous place can always be asked.
            }
        }
    }

}
