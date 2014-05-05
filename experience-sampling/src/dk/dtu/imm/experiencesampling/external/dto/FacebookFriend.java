package dk.dtu.imm.experiencesampling.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacebookFriend {

    @JsonProperty("id")
    private String userId;
    private String name;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
