package dk.dtu.imm.experiencesampling.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookFriendsResponse {

    @JsonProperty("data")
    private List<FacebookFriend> friends;

    public FacebookFriendsResponse() {
    }

    public List<FacebookFriend> getFriends() {
        return friends;
    }

    public void setFriends(List<FacebookFriend> friends) {
        this.friends = friends;
    }
}
