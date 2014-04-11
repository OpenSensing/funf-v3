package dk.dtu.imm.experiencesampling.external.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"question_type", "friend_one", "friend_two"})
@JsonInclude(Include.NON_NULL)
public class PendingQuestionDto {

    @JsonProperty("question_type")
    private String questionType;
    @JsonProperty("friend_one")
    private FriendDto friendOne;
    @JsonProperty("friend_two")
    private FriendDto friendTwo;

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public FriendDto getFriendOne() {
        return friendOne;
    }

    public void setFriendOne(FriendDto friendOne) {
        this.friendOne = friendOne;
    }

    public FriendDto getFriendTwo() {
        return friendTwo;
    }

    public void setFriendTwo(FriendDto friendTwo) {
        this.friendTwo = friendTwo;
    }
}
