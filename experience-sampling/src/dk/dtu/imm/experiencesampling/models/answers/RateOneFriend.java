package dk.dtu.imm.experiencesampling.models.answers;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.QuestionType;

import java.io.Serializable;

@JsonPropertyOrder({"question_type", "answer_type", "start_timestamp", "end_timestamp", "loaded_timestamp", "friend_uid", "rating"})
public class RateOneFriend extends Answer implements Serializable {

    @JsonProperty("friend_uid")
    private String friendId;
    @JsonProperty("rating")
    private int rating;

    public RateOneFriend() {
        super();
        this.questionType = QuestionType.SOCIAL_RATE_ONE_FRIEND;
    }

    private RateOneFriend(QuestionType questionType, AnswerType answerType, String friendId, int rating, long startTimestamp, long endTimestamp, long loadedTimestamp) {
        super(questionType, answerType, startTimestamp, endTimestamp, loadedTimestamp);
        this.id = String.format("%s:%s:%s:%s", questionType, answerType, friendId, endTimestamp);
        this.friendId = friendId;
        this.rating = rating;
    }

    public RateOneFriend(AnswerType answerType, String friendId, int rating, long startTimestamp, long endTimestamp, long firstSeenTimestamp) {
        this(QuestionType.SOCIAL_RATE_ONE_FRIEND, answerType, friendId, rating, startTimestamp, endTimestamp, firstSeenTimestamp);
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
