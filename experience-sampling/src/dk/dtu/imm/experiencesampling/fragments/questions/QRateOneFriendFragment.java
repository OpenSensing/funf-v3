package dk.dtu.imm.experiencesampling.fragments.questions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.custom.ProfilePicture;
import dk.dtu.imm.experiencesampling.custom.RatingBar;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.answers.RateOneFriend;

import java.util.Date;

public class QRateOneFriendFragment extends BaseQuestionFragmentSocial {

    private static final String FRIEND_KEY = "friend_key";

    Friend friend;

    public static final QRateOneFriendFragment newInstance(Friend friend) {
        QRateOneFriendFragment fragment = new QRateOneFriendFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(FRIEND_KEY, friend);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            friend = (Friend) getArguments().getSerializable(FRIEND_KEY);
        }

        if (friend == null) {
            if (getActivity() != null)
                getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_q_rate_one_friend, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSubmit = (Button) view.findViewById(R.id.social_question_btn_submit);

        // Set friend info
        ProfilePicture profilePicture = (ProfilePicture) view.findViewById(R.id.social_q_profile_picture);
        profilePicture.setProfileImage(getFacebookImageUrl(friend.getUserId()));
        profilePicture.setProfileName(friend.getName());

        // Get rating bar
        ratingBar = (RatingBar) view.findViewById(R.id.social_q_seek_bar);
        ratingBar.setOnSeekBarChangeListener(new SeekBarChangeListener());

        // Create onClickListener for submit buttons
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {

                    AnswerType answerType;
                    int rating = ratingBar.getRating();

                    int viewId = view.getId();
                    if (viewId == R.id.social_question_btn_submit) {
                        answerType = AnswerType.ANSWERED_SUBMIT;
                    } else if (viewId == R.id.social_question_btn_not_know) {
                        answerType = AnswerType.ANSWERED_DONT_KNOW;
                    } else  {
                        answerType = AnswerType.ANSWERED_NO_THANKS;
                    }

                    RateOneFriend answer = new RateOneFriend(answerType, friend.getUserId(), rating, startTimestamp, new Date().getTime(), loadedTimestamp);
                    saveQuestion(answer);
                    getActivity().finish();
                }
            }
        };

        // Submit buttons
        btnSubmit = (Button) view.findViewById(R.id.social_question_btn_submit);
        Button btnNotKnow = (Button) view.findViewById(R.id.social_question_btn_not_know);
        Button btnNoThanks = (Button) view.findViewById(R.id.social_question_btn_no_thanks);

        // Set defined listener from above
        btnSubmit.setOnClickListener(onClickListener);
        btnNotKnow.setOnClickListener(onClickListener);
        btnNoThanks.setOnClickListener(onClickListener);

        enableSubmitButton(false);
    }
}
