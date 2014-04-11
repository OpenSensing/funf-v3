package dk.dtu.imm.experiencesampling.exceptions;

// todo: not necessary when used within the sensible dtu data-collector
public class NotEnoughFacebookFriendsException extends Exception {

    public NotEnoughFacebookFriendsException(String message) {
        super(message);
    }
}
