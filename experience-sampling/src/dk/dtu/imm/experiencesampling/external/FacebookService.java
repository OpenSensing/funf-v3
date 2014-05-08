package dk.dtu.imm.experiencesampling.external;

import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.imm.experiencesampling.exceptions.FacebookException;
import dk.dtu.imm.experiencesampling.external.dto.FacebookFriendDto;
import dk.dtu.imm.experiencesampling.models.Friend;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class FacebookService {

    private static final String TAG = "FacebookService";
    private static final String BASE_URL = "https://graph.facebook.com/v1.0";

    public Friend getFriend(String userId) throws FacebookException {
        String url = buildRequest(userId);
        Log.d(TAG, "Getting Facebook friend: " + url);
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            String jsonResponse = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ObjectMapper mapper = new ObjectMapper();
                FacebookFriendDto facebookFriendDto = mapper.readValue(jsonResponse, FacebookFriendDto.class);
                if (facebookFriendDto != null && facebookFriendDto.getName() != null) {
                    Friend friend = new Friend();
                    friend.setUserId(userId);
                    friend.setName(facebookFriendDto.getName());
                    return friend;
                }
            }
        } catch (IOException e) {
            throw new FacebookException("Error during facebook friend list request: " + e.getMessage());
        }
        throw new FacebookException("Error during facebook friend list request");
    }

    private String buildRequest(String action) {
        return String.format("%s/%s", BASE_URL, action);
    }
}
