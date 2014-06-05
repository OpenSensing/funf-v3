package dk.dtu.imm.experiencesampling.fragments.questions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.custom.ProfilePicture;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.answers.CloserFriend;

import java.util.Date;

public class QCloserFriendFragment extends BaseQuestionFragmentSocial {

    private static final String FRIEND_ONE_KEY = "friend_one";
    private static final String FRIEND_TWO_KEY = "friend_two";

    Friend friendOne;
    Friend friendTwo;

    public static final QCloserFriendFragment newInstance(Friend friendOne, Friend friendTwo) {
        QCloserFriendFragment fragment = new QCloserFriendFragment();
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
        return inflater.inflate(R.layout.fragment_q_closer_friend, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set friend 1 info
        final ProfilePicture profilePictureOne = (ProfilePicture) view.findViewById(R.id.social_q_profile_picture_one);
        profilePictureOne.setProfileImage(getFacebookImageUrl(friendOne.getUserId()));
        profilePictureOne.setProfileName(friendOne.getName());

        // Set friend 2 info
        final ProfilePicture profilePictureTwo = (ProfilePicture) view.findViewById(R.id.social_q_profile_picture_two);
        profilePictureTwo.setProfileImage(getFacebookImageUrl(friendTwo.getUserId()));
        profilePictureTwo.setProfileName(friendTwo.getName());

        // Create onClickListener for buttons and pictures
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null && getView() != null) {
                    AnswerType answerType;
                    String choice = null;

                    // As of sdk r14 these IDs cannot be treated as constants in library modules, and therefore switch case cannot be used.
                    int viewId = view.getId();
                    if (viewId == R.id.social_q_profile_picture_one) {
                        profilePictureOne.setImageClicked();
                        answerType = AnswerType.ANSWERED_SUBMIT;
                        choice = friendOne.getUserId();
                    } else if (viewId == R.id.social_q_profile_picture_two) {
                        profilePictureTwo.setImageClicked();
                        answerType = AnswerType.ANSWERED_SUBMIT;
                        choice = friendTwo.getUserId();
                    } else if (viewId == R.id.social_question_btn_not_know) {
                        answerType = AnswerType.ANSWERED_DONT_KNOW;
                    } else {
                        answerType = AnswerType.ANSWERED_NO_THANKS;
                    }

                    CloserFriend answer = new CloserFriend(answerType, friendOne.getUserId(), friendTwo.getUserId(), choice, startTimestamp, new Date().getTime(), loadedTimestamp);
                    saveQuestion(answer);
                    getActivity().finish();
                }
            }
        };

        // Buttons
        Button btnNotKnow = (Button) view.findViewById(R.id.social_question_btn_not_know);
        Button btnNoThanks = (Button) view.findViewById(R.id.social_question_btn_no_thanks);

        // Set defined listener from above
        btnNotKnow.setOnClickListener(onClickListener);
        btnNoThanks.setOnClickListener(onClickListener);
        profilePictureOne.setOnClickListener(onClickListener);
        profilePictureTwo.setOnClickListener(onClickListener);
    }

}
