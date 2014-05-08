package dk.dtu.imm.experiencesampling.mappers;

import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.exceptions.MissingFieldsException;
import dk.dtu.imm.experiencesampling.external.dto.PendingQuestionDto;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestion;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestionOneFriend;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestionTwoFriends;

public class PendingQuestionMapper {

    public static PendingQuestion map(PendingQuestionDto dto) throws MissingFieldsException {
        PendingQuestion pendingQuestion = new PendingQuestion();

        QuestionType type = QuestionType.getQuestionType(dto.getQuestionType());
        if (type.equals(QuestionType.UNKNOWN)) {
            throw new MissingFieldsException("The question type is not specified or unknown");
        }

        // Attach friend data if social question
        if (type.equals(QuestionType.SOCIAL_CLOSER_FRIEND) || type.equals(QuestionType.SOCIAL_RATE_TWO_FRIENDS)) {
            if (dto.getFriendOne() == null || dto.getFriendTwo() == null) {
                throw new MissingFieldsException("Missing friends dto fields");
            }

            Friend friendOne = new Friend();
            friendOne.setUserId(dto.getFriendOne().getUid());
            friendOne.setName(dto.getFriendOne().getName());

            Friend friendTwo = new Friend();
            friendTwo.setUserId(dto.getFriendTwo().getUid());
            friendTwo.setName(dto.getFriendTwo().getName());

            if (!friendOne.isValid() || !friendTwo.isValid()) {
                throw new MissingFieldsException("Missing friend fields");
            }
            pendingQuestion = new PendingQuestionTwoFriends();
            ((PendingQuestionTwoFriends) pendingQuestion).setFriendOne(friendOne);
            ((PendingQuestionTwoFriends) pendingQuestion).setFriendTwo(friendTwo);
        } else if (type.equals(QuestionType.SOCIAL_RATE_ONE_FRIEND)) {
            if (dto.getFriendOne() == null) {
                throw new MissingFieldsException("Missing friend dto fields");
            }

            Friend friend = new Friend();
            friend.setUserId(dto.getFriendOne().getUid());
            friend.setName(dto.getFriendOne().getName());

            if (!friend.isValid()) {
                throw new MissingFieldsException("Missing friend fields");
            }
            pendingQuestion = new PendingQuestionOneFriend();
            ((PendingQuestionOneFriend) pendingQuestion).setFriend(friend);
        }

        pendingQuestion.setQuestionType(type);
        return pendingQuestion;
    }
}
