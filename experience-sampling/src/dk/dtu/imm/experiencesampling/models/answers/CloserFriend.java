package dk.dtu.imm.experiencesampling.models.answers;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.QuestionType;

import java.io.Serializable;


@JsonPropertyOrder({"question_type", "answer_type", "start_timestamp", "end_timestamp", "loaded_timestamp", "friend_one_uid", "friend_two_uid", "choice"})
public class CloserFriend extends Answer implements Serializable {

    @JsonProperty("friend_one_uid")
    private String friendOneId;
    @JsonProperty("friend_two_uid")
    private String friendTwoId;
    @JsonProperty("choice")
    private String choice;

    public CloserFriend() {
        super();
        this.questionType = QuestionType.SOCIAL_CLOSER_FRIEND;
    }

    private CloserFriend(QuestionType questionType, AnswerType answerType, String friendOneId, String friendTwoId, String choice, long startTimestamp, long endTimestamp, long loadedTimestamp) {
        super(questionType, answerType, startTimestamp, endTimestamp, loadedTimestamp);
        this.id = String.format("%s:%s:%s:%s:%s", questionType, answerType, friendOneId, friendTwoId, endTimestamp);
        this.friendOneId = friendOneId;
        this.friendTwoId = friendTwoId;
        this.choice = choice;
    }

    public CloserFriend(AnswerType answerType, String friendOneId, String friendTwoId, String choice, long startTimestamp, long endTimestamp, long firstSeenTimestamp) {
        this(QuestionType.SOCIAL_CLOSER_FRIEND, answerType, friendOneId, friendTwoId, choice, startTimestamp, endTimestamp, firstSeenTimestamp);
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    public String getFriendOneId() {
        return friendOneId;
    }

    public void setFriendOneId(String friendOneId) {
        this.friendOneId = friendOneId;
    }

    public String getFriendTwoId() {
        return friendTwoId;
    }

    public void setFriendTwoId(String friendTwoId) {
        this.friendTwoId = friendTwoId;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }
}
