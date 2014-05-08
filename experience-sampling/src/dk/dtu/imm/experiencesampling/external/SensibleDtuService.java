package dk.dtu.imm.experiencesampling.external;

import android.content.Context;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;
import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.exceptions.*;
import dk.dtu.imm.experiencesampling.external.dto.*;
import dk.dtu.imm.experiencesampling.mappers.PendingQuestionMapper;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestion;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

public class SensibleDtuService {

    private static final String TAG = "SensibleDtuService";
    private static final String BASE_URL = "https://www.sensible.dtu.dk/sensible-dtu/connectors/connector_answer/v1";

    Context context;

    public SensibleDtuService(Context context) {
        this.context = context;
    }

    public FriendsResponseDto requestFriendsInfo(String bearerToken) throws SensibleDtuException {
        String url = buildRequest("/facebook_friends_question/get_friends_connections/", bearerToken);
        Log.d(TAG, "Getting friends and connections: " + url);
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            String jsonResponse = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(jsonResponse, FriendsResponseDto.class);
            }
        } catch (IOException e) {
            throw new SensibleDtuException("Error during friends and connections request: " + e.getMessage());
        }
        throw new SensibleDtuException("Error during friends and connections request");
    }

    private String buildRequest(String action, String bearerToken) {
        return String.format("%s/%s?bearer_token=%s", BASE_URL, action, bearerToken);
    }

    public Set<PendingQuestion> getPendingQuestions() throws IOException {
        Set<PendingQuestion> pendingQuestions = new HashSet<PendingQuestion>(); // HashSet, so it is unordered.

        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType javaType = typeFactory.constructCollectionType(List.class, PendingQuestionDto.class);

        String pendingQuestionsJson = generateQuestionsJson();
        Log.d(TAG, "Received pending questions: " + pendingQuestionsJson);

        List<PendingQuestionDto> pendingQuestionDtoList = objectMapper.readValue(pendingQuestionsJson, javaType);
        for (PendingQuestionDto pendingQuestionDto : pendingQuestionDtoList) {
            try {
                pendingQuestions.add(PendingQuestionMapper.map(pendingQuestionDto));
            } catch (MissingFieldsException e) {
                Log.e(TAG, "Error while mapping pending question: " + e.getMessage());
            }
        }
        return pendingQuestions;
    }

    /**
     *
     * @return 1 of each question as a json string.
     * This method will later be replaced with a GET /questions call to the Sensible DTU backend, which will take care of the "who to ask" logic.
     */
    private String generateQuestionsJson() {
        List<PendingQuestionDto> dtos = new ArrayList<PendingQuestionDto>();
        for (QuestionType questionType : QuestionType.getAll()) {
            try {
                dtos.addAll(getPendingQuestionDto(questionType, 1));
            } catch (NotEnoughFriendsException e) {
                Log.w(TAG, "Not enough friends for [" + questionType.name() + "] questions.");
            } catch (NotEnoughFriendConnectionsException e) {
                Log.w(TAG, "Not enough friend connections for [" + questionType.name() + "] questions.");
            }
        }

        String jsonQuestionInfo = null;
        try {
            jsonQuestionInfo = new ObjectMapper().writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Error during answers info to json");
            e.printStackTrace();
        }
        return jsonQuestionInfo;
    }

    private List<PendingQuestionDto> getPendingQuestionDto(QuestionType questionType, int number) throws NotEnoughFriendsException, NotEnoughFriendConnectionsException {
        List<PendingQuestionDto> dtos = new ArrayList<PendingQuestionDto>();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        for (int i=0; i < number; i++) {
            PendingQuestionDto dto = new PendingQuestionDto();
            dto.setQuestionType(questionType.name());
            if (QuestionType.SOCIAL_CLOSER_FRIEND.equals(questionType) || QuestionType.SOCIAL_RATE_TWO_FRIENDS.equals(questionType)) {
                List<Friend> friends = dbHelper.getTwoConnectedFriends(questionType);
                FriendDto friendOneDto = new FriendDto();
                FriendDto friendTwoDto = new FriendDto();

                friendOneDto.setUid(friends.get(0).getUserId());
                friendOneDto.setName(friends.get(0).getName());

                friendTwoDto.setUid(friends.get(1).getUserId());
                friendTwoDto.setName(friends.get(1).getName());

                dto.setFriendOne(friendOneDto);
                dto.setFriendTwo(friendTwoDto);
            } else if (QuestionType.SOCIAL_RATE_ONE_FRIEND.equals(questionType)) {
                Friend friend = dbHelper.getRandomFriend(questionType);
                FriendDto friendDto = new FriendDto();

                friendDto.setUid(friend.getUserId());
                friendDto.setName(friend.getName());

                dto.setFriendOne(friendDto);
            }
            dtos.add(dto);
        }
        dbHelper.closeDatabase();
        return dtos;
    }
}
