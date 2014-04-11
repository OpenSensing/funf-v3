package dk.dtu.imm.experiencesampling.models.questions;

import dk.dtu.imm.experiencesampling.models.Friend;


public class PendingQuestionOneFriend extends PendingQuestion {

    private Friend friend;

    public Friend getFriend() {
        return friend;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
    }
}
