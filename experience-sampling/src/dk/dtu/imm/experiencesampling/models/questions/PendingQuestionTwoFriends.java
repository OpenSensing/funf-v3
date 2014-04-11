package dk.dtu.imm.experiencesampling.models.questions;

import dk.dtu.imm.experiencesampling.models.Friend;


public class PendingQuestionTwoFriends extends PendingQuestion {

    private Friend friendOne;
    private Friend friendTwo;

    public Friend getFriendOne() {
        return friendOne;
    }

    public void setFriendOne(Friend friendOne) {
        this.friendOne = friendOne;
    }

    public Friend getFriendTwo() {
        return friendTwo;
    }

    public void setFriendTwo(Friend friendTwo) {
        this.friendTwo = friendTwo;
    }
}
