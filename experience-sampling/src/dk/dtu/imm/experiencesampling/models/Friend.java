package dk.dtu.imm.experiencesampling.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class Friend implements Serializable, Comparable<Friend> {

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

    @JsonIgnore
    public boolean isValid() {
        return this.userId != null && this.name != null;
    }

    @Override
    public int compareTo(Friend another) {
        return this.getUserId().compareTo(another.getUserId());
    }
}
