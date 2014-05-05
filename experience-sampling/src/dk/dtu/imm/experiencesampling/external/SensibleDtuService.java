package dk.dtu.imm.experiencesampling.external;

import android.content.Context;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;
import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.exceptions.MissingFieldsException;
import dk.dtu.imm.experiencesampling.exceptions.NotEnoughFacebookFriendsException;
import dk.dtu.imm.experiencesampling.external.dto.FriendDto;
import dk.dtu.imm.experiencesampling.external.dto.PendingQuestionDto;
import dk.dtu.imm.experiencesampling.mappers.PendingQuestionMapper;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.questions.PendingQuestion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SensibleDtuService {

    private static final String TAG = "SensibleDtuService";

    // Only used if the questions asked should be totally random. Now it fixed, so they are equally distributed
    /*
    private static final int NUMBER_OF_QUESTION_CATEGORIES = 2;
    private static final int NUMBER_OF_SOCIAL_QUESTIONS = 3;
    private static final int NUMBER_OF_LOCATION_QUESTIONS = 2;
    */

    Context context;

    public SensibleDtuService(Context context) {
        this.context = context;
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
     * @return 10 equally distributed questions to be asked as a json string.
     * This method will later be replaced with a GET /answers call to the Sensible DTU backend, which will take care of the "who to ask" logic.
     */
    private String generateQuestionsJson() {
        List<PendingQuestionDto> questionDtos = new ArrayList<PendingQuestionDto>();
        questionDtos.addAll(getQuestionInfo(10));
        String jsonQuestionInfo = null;
        try {
            jsonQuestionInfo = new ObjectMapper().writeValueAsString(questionDtos);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Error during answers info to json");
            e.printStackTrace();
        }
        return jsonQuestionInfo;
    }

    private List<PendingQuestionDto> getQuestionInfo(int total) {
        int half = total / 2;

        List<PendingQuestionDto> pendingQuestionDtoList = new ArrayList<PendingQuestionDto>();
        pendingQuestionDtoList.addAll(getLocationQuestionInfo(half));
        try {
            pendingQuestionDtoList.addAll(getSocialQuestionInfo(half));
        } catch (NotEnoughFacebookFriendsException e) {
            Log.e(TAG, "Not enough friends for social question. Only location questions returned");
        }
        return pendingQuestionDtoList;
    }

    private List<PendingQuestionDto> getLocationQuestionInfo(int total) {
        List<PendingQuestionDto> pendingQuestionDtoList = new ArrayList<PendingQuestionDto>();
        int middle = total / 2;
        for (int i = 0; i < total; i++) {
            PendingQuestionDto pendingQuestionDto = new PendingQuestionDto();
            if (i <= middle) {
                pendingQuestionDto.setQuestionType(QuestionType.LOCATION_PREVIOUS.name()); // gives one more previous question than current
            } else {
                pendingQuestionDto.setQuestionType(QuestionType.LOCATION_CURRENT.name());
            }
            pendingQuestionDtoList.add(pendingQuestionDto);
        }
        return pendingQuestionDtoList;
    }

    private List<PendingQuestionDto> getSocialQuestionInfo(int total) throws NotEnoughFacebookFriendsException {
        List<PendingQuestionDto> pendingQuestionDtoList = new ArrayList<PendingQuestionDto>();
        int lowerBound = total / 3;
        int higherBound = total / 3 * 2;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        for (int i = 0; i < total; i++) {
            List<Friend> friends = dbHelper.getRandomFriends(2);

            PendingQuestionDto pendingQuestionDto = new PendingQuestionDto();
            FriendDto friendOneDto = new FriendDto();
            FriendDto friendTwoDto = new FriendDto();

            if (i < lowerBound) {
                pendingQuestionDto.setQuestionType(QuestionType.SOCIAL_RATE_ONE_FRIEND.name());
                friendOneDto.setUid(friends.get(0).getUserId());
                friendOneDto.setName(friends.get(0).getName());
                pendingQuestionDto.setFriendOne(friendOneDto);
            } else if (i > higherBound) {
                pendingQuestionDto.setQuestionType(QuestionType.SOCIAL_RATE_TWO_FRIENDS.name());

                friendOneDto.setUid(friends.get(0).getUserId());
                friendOneDto.setName(friends.get(0).getName());
                pendingQuestionDto.setFriendOne(friendOneDto);

                friendTwoDto.setUid(friends.get(1).getUserId());
                friendTwoDto.setName(friends.get(1).getName());
                pendingQuestionDto.setFriendTwo(friendTwoDto);
            } else {
                pendingQuestionDto.setQuestionType(QuestionType.SOCIAL_CLOSER_FRIEND.name());

                friendOneDto.setUid(friends.get(0).getUserId());
                friendOneDto.setName(friends.get(0).getName());
                pendingQuestionDto.setFriendOne(friendOneDto);

                friendTwoDto.setUid(friends.get(1).getUserId());
                friendTwoDto.setName(friends.get(1).getName());
                pendingQuestionDto.setFriendTwo(friendTwoDto);
            }
            pendingQuestionDtoList.add(pendingQuestionDto);
        }
        dbHelper.closeDatabase();
        return pendingQuestionDtoList;
    }


    /**
     *
     * @return 10 random questions to be asked as a json string.
     * This method will later be replaced with a GET /answers call to the Sensible DTU backend, which will take care of the "who to ask" logic.
     */
    /*
    private String generateRandomQuestionsJson() {
        List<PendingQuestionDto> questionDtos = new ArrayList<PendingQuestionDto>();
        for (int i = 0; i < 10; i++) {
            questionDtos.add(getRandomQuestionInfo());
        }

        String jsonQuestionInfo = null;
        try {
            jsonQuestionInfo = new ObjectMapper().writeValueAsString(questionDtos);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Error during questions info to json");
            e.printStackTrace();
        }
        return jsonQuestionInfo;
    }

    // Divides location- and social question categories equally, even if there are more types of social answers.
    private PendingQuestionDto getRandomQuestionInfo() {
        PendingQuestionDto pendingQuestionDto;
        try {
            int randomNumber = new Random().nextInt(NUMBER_OF_QUESTION_CATEGORIES);
            switch (randomNumber) {
                case 0:
                    pendingQuestionDto = getRandomSocialQuestionInfo();
                    break;
                default:
                    pendingQuestionDto = getRandomLocationQuestionInfo();
                    break;
            }
        } catch (NotEnoughFacebookFriendsException e) {
            // If not enough friends for social question, then ask location question.
            pendingQuestionDto = getRandomLocationQuestionInfo();
        }
        return pendingQuestionDto;
    }

    private PendingQuestionDto getRandomSocialQuestionInfo() throws NotEnoughFacebookFriendsException {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        List<Friend> friends = dbHelper.getRandomFriends(2);

        PendingQuestionDto pendingQuestionDto = new PendingQuestionDto();
        FriendDto friendOneDto = new FriendDto();
        FriendDto friendTwoDto = new FriendDto();

        int randomNumber = new Random().nextInt(NUMBER_OF_SOCIAL_QUESTIONS);
        switch (randomNumber) {
            case 0:
                pendingQuestionDto.setQuestionType(QuestionType.SOCIAL_RATE_ONE_FRIEND.name());
                friendOneDto.setUid(friends.get(0).getUserId());
                friendOneDto.setName(friends.get(0).getName());
                pendingQuestionDto.setFriendOne(friendOneDto);
                break;
            case 1:
                pendingQuestionDto.setQuestionType(QuestionType.SOCIAL_RATE_TWO_FRIENDS.name());

                friendOneDto.setUid(friends.get(0).getUserId());
                friendOneDto.setName(friends.get(0).getName());
                pendingQuestionDto.setFriendOne(friendOneDto);

                friendTwoDto.setUid(friends.get(1).getUserId());
                friendTwoDto.setName(friends.get(1).getName());
                pendingQuestionDto.setFriendTwo(friendTwoDto);
                break;
            default:
                pendingQuestionDto.setQuestionType(QuestionType.SOCIAL_CLOSER_FRIEND.name());

                friendOneDto.setUid(friends.get(0).getUserId());
                friendOneDto.setName(friends.get(0).getName());
                pendingQuestionDto.setFriendOne(friendOneDto);

                friendTwoDto.setUid(friends.get(1).getUserId());
                friendTwoDto.setName(friends.get(1).getName());
                pendingQuestionDto.setFriendTwo(friendTwoDto);
                break;
        }
        dbHelper.closeDatabase();
        return pendingQuestionDto;
    }

    private PendingQuestionDto getRandomLocationQuestionInfo() {
        PendingQuestionDto pendingQuestionDto = new PendingQuestionDto();
        int randomNumber = new Random().nextInt(NUMBER_OF_LOCATION_QUESTIONS);
        switch (randomNumber) {
            case 0:
                pendingQuestionDto.setQuestionType(QuestionType.LOCATION_CURRENT.name());
                break;
            default:
                pendingQuestionDto.setQuestionType(QuestionType.LOCATION_PREVIOUS.name());
                break;
        }
        return pendingQuestionDto;
    }
    */
}
