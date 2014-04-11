package dk.dtu.imm.experiencesampling.external;

import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.imm.experiencesampling.exceptions.FacebookException;
import dk.dtu.imm.experiencesampling.external.dto.FacebookFriend;
import dk.dtu.imm.experiencesampling.external.dto.FacebookFriendsResponse;
import dk.dtu.imm.experiencesampling.models.Friend;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// todo: not necessary when used within the sensible dtu data-collector
public class FacebookService {

    private static final String TAG = "FacebookService";
    private static final String BASE_URL = "https://graph.facebook.com";

    private String accessToken;

    public FacebookService(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<Friend> getFriends() throws FacebookException {
        List<Friend> friends = new ArrayList<Friend>();
        String url = buildRequest("/me/friends", accessToken);
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            String jsonResponse = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ObjectMapper mapper = new ObjectMapper();
                FacebookFriendsResponse fbFriendResponse = mapper.readValue(jsonResponse, FacebookFriendsResponse.class);
                if (fbFriendResponse != null && fbFriendResponse.getFriends().size() > 0) {
                    for (FacebookFriend fbFriend : fbFriendResponse.getFriends()) {
                        Friend friend = new Friend();
                        friend.setUserId(fbFriend.getUserId());
                        friend.setName(fbFriend.getName());
                        friends.add(friend);
                    }
                }
            } else {
                Log.d(TAG, "Error during facebook friend list request: " + jsonResponse);
                throw new FacebookException("Error during facebook friend list request");
            }
        } catch (IOException e) {
            throw new FacebookException("Error during facebook friend list request");
        }
        return friends;
    }

    private String buildRequest(String action, String accessToken) {
        return String.format("%s%s?access_token=%s", BASE_URL, action, accessToken);
    }
}
