package dk.dtu.imm.experiencesampling.models.answers;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.QuestionType;

import java.io.Serializable;

@JsonPropertyOrder({"question_type", "answer_type", "start_timestamp", "end_timestamp", "loaded_timestamp", "friend_one_uid", "friend_two_uid", "rating"})
public class RateTwoFriends extends Answer implements Serializable {

    @JsonProperty("friend_one_uid")
    private String friendOneId;
    @JsonProperty("friend_two_uid")
    private String friendTwoId;
    @JsonProperty("rating")
    private int rating;

    public RateTwoFriends() {
        super();
        this.questionType = QuestionType.SOCIAL_RATE_TWO_FRIENDS;
    }

    private RateTwoFriends(QuestionType questionType, AnswerType answerType, String friendOneId, String friendTwoId, int rating, long startTimestamp, long endTimestamp, long loadedTimestamp) {
        super(questionType, answerType, startTimestamp, endTimestamp, loadedTimestamp);
        this.id = String.format("%s:%s:%s:%s:%s", questionType, answerType, friendOneId, friendTwoId, endTimestamp);
        this.friendOneId = friendOneId;
        this.friendTwoId = friendTwoId;
        this.rating = rating;
    }

    public RateTwoFriends(AnswerType answerType, String friendOneId, String friendTwoId, int rating, long startTimestamp, long endTimestamp, long firstSeenTimestamp) {
        this(QuestionType.SOCIAL_RATE_TWO_FRIENDS, answerType, friendOneId, friendTwoId, rating, startTimestamp, endTimestamp, firstSeenTimestamp);
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
