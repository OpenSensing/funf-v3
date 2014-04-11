package dk.dtu.imm.experiencesampling.models.answers;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.QuestionType;

import java.io.Serializable;

@JsonPropertyOrder({"question_type", "answer_type", "start_timestamp", "end_timestamp", "first_seen_timestamp", "friend_uid", "rating"})
public class RateOneFriend extends Answer implements Serializable {

    @JsonProperty("friend_uid")
    private String friendId;
    @JsonProperty("rating")
    private int rating;

    public RateOneFriend() {
        super();
    }

    public RateOneFriend(QuestionType questionType, AnswerType answerType, String friendId, int rating, long startTimestamp, long endTimestamp, long firstSeenTimestamp) {
        super(questionType, answerType, startTimestamp, endTimestamp, firstSeenTimestamp);
        this.id = String.format("%s:%s:%s:%s", questionType, answerType, friendId, endTimestamp);
        this.friendId = friendId;
        this.rating = rating;
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
