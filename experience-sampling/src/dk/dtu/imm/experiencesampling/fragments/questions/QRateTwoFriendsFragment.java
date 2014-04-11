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
import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.answers.RateTwoFriends;

import java.util.Date;

public class QRateTwoFriendsFragment extends BaseQuestionFragmentSocial {

    private static final String FRIEND_ONE_KEY = "friend_one";
    private static final String FRIEND_TWO_KEY = "friend_two";

    Friend friendOne;
    Friend friendTwo;

    public static final QRateTwoFriendsFragment newInstance(Friend friendOne, Friend friendTwo) {
        QRateTwoFriendsFragment fragment = new QRateTwoFriendsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(FRIEND_ONE_KEY, friendOne);
        bundle.putSerializable(FRIEND_TWO_KEY, friendTwo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            friendOne = (Friend) getArguments().getSerializable(FRIEND_ONE_KEY);
            friendTwo = (Friend) getArguments().getSerializable(FRIEND_TWO_KEY);
        }

        if (friendOne == null || friendTwo == null) {
            if (getActivity() != null)
                getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_q_rate_two_friends, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSubmit = (Button) view.findViewById(R.id.social_question_btn_submit);

        // Set friend 1 info
        ProfilePicture profilePictureOne = (ProfilePicture) view.findViewById(R.id.social_q_profile_picture_one);
        profilePictureOne.setProfileImage(getFacebookImageUrl(friendOne.getUserId()));
        profilePictureOne.setProfileName(friendOne.getName());

        // Set friend 2 info
        ProfilePicture profilePictureTwo = (ProfilePicture) view.findViewById(R.id.social_q_profile_picture_two);
        profilePictureTwo.setProfileImage(getFacebookImageUrl(friendTwo.getUserId()));
        profilePictureTwo.setProfileName(friendTwo.getName());

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
                        answerType = AnswerType.ANSWERED;
                    } else if (viewId == R.id.social_question_btn_not_know) {
                        answerType = AnswerType.DONT_KNOW;
                    } else {
                        answerType = AnswerType.NO_THANKS;
                    }

                    RateTwoFriends answer = new RateTwoFriends(QuestionType.SOCIAL_RATE_TWO_FRIENDS, answerType, friendOne.getUserId(), friendTwo.getUserId(), rating, startTimestamp, new Date().getTime(), firstSeenTimestamp);
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
