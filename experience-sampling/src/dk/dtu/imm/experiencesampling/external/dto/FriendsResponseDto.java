package dk.dtu.imm.experiencesampling.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FriendsResponseDto {

    @JsonProperty("friends")
    private List<String> friends;
    @JsonProperty("connections")
    private List<List<String>> connections;

    public FriendsResponseDto() {
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<List<String>> getConnections() {
        return connections;
    }

    public void setConnections(List<List<String>> connections) {
        this.connections = connections;
    }
}
